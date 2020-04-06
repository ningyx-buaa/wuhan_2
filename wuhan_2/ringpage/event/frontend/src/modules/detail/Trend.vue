<template>
  <div class="row main-row" v-title data-title="事件趋势">
    <div class="col-md-12">
      <div class="r-panel hotnews-panel">
        <div class="panel-title-wrapper">
          <div class="panel-title">
            <span>事件趋势</span>
          </div>
        </div>
        <div class="panel-event">
          <div class="events-wrapper">
            <v-desc-view :event="event"></v-desc-view>
          </div>
          <div class="chart">
            <Echarts theme="ring" :option="trend_option" :loading="loading.trend" :loadingOpts="{ text: '加载中...' }"></Echarts>
          </div>
        </div>
      </div>
    </div>
    <!-- <div class="col-md-4">
      <div class="r-panel people-panel">
        <div class="panel-title-wrapper">
          <div class="panel-title">
            <span>人物</span>
          </div>
        </div>
        <div class="people-wrapper">
        </div>
      </div>
    </div> -->
  </div>
</template>

<script type="text/ecmascript-6">

import Echarts from 'vue-echarts-v3/src/lite'
import 'echarts/lib/chart/line'
import 'echarts/lib/component/legend'
import 'echarts/lib/component/tooltip'

import DescView from 'components/event/DescView'

export default {
  props: {
    event: {
      type: Object,
    },
  },
  data () {
    return {
      trend_option: {
        title: {
          text: '事件热度趋势图',
          left: 'center',
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          name: '时间',
          type: 'category',
          boundaryGap: false,
          data: []
        },
        yAxis: {
          name: '热度',
          type: 'value',
          axisLabel: {
            formatter: '{value}'
          }
        },
        series: [
          {
            name: '事件热度',
            type: 'line',
            data: [],
            markPoint: {
              data: [
                { type: 'max', name: '最大值' },
                { type: 'min', name: '最小值' }
              ]
            },
          },
        ]
      },
      loading: {
        trend: true,
      },
    };
  },
  created () {
    this.fetchEvent(this.$route.params.eventId);
  },
  methods: {
    fetchEvent (eventId) {
      axios.get('/api/legacy/getEventById', {params: {
        eventId: this.$route.params.eventId,
      }}).then(response => {
        let event = response.data;
        let dates = [];
        let datas = [];
        _.each(event.eventTrend.split(','), item => {
          let vs = item.split('_');
          if (vs.length > 1) {
            let date = vs[0];
            dates.push(date.substring(0, 4) + '-' + date.substring(4, 6) + '-' + date.substring(6, 8));
            datas.push(parseInt(vs[1]));
          }
        });

        if (dates.length === 0) {
          dates.push(event.eventSpanDateString.substring(0, 10));
          datas.push(event.wf_hot);
        }

        // 画图
        this.loading.trend = false;
        this.trend_option.xAxis.data = dates;
        this.trend_option.series[0].data = datas;
      });
    },
  },
  components: {
    'Echarts': Echarts,
    'v-desc-view': DescView,
  },
};
</script>

<style lang="sass" scoped>
@import "~assets/sass/mixin"

.r-panel
  height: 100%

.events-wrapper
  width: 100%
  flex: 1

.panel-event
  height: calc(100% - 50px)
  display: flex
  flex-direction: column
  flex: 1
  .chart
    display: flex
    width: 100%
    height: calc(100% - 200px)

</style>
