<template>
  <div class="row main-row" v-title data-title="事件演化">
    <div class="col-md-12">
      <div class="r-panel hotnews-panel">
        <div class="panel-title-wrapper">
          <div class="panel-title">
            <span>演化路径</span>
            <div class="switch">
            </div>
          </div>
        </div>
        <div class="panel-event">
          <div class="events-wrapper">
            <v-desc-view :event="event"></v-desc-view>
          </div>
          <div class="chart">
            <Echarts theme="ring" :option="sankey_option" :loading="loading.sankey" :loadingOpts="{ text: '加载中...' }"></Echarts>
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
import 'echarts/lib/chart/sankey'
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
      sankey_option: {
        color: [
          '#c23531',
          '#2f4554',
          '#61a0a8',
          '#d48265',
          '#91c7ae',
          '#749f83',
          '#ca8622',
          '#bda29a',
          '#6e7074',
          '#546570',
          '#c4ccd3',
        ], // override configuration in theme `ring`.
        tooltip: {
          trigger: 'item',
          triggerOn: 'mousemove',
          formatter: function (param) {
            switch (param.dataType) {
              case 'node':
                return param.data.time + '<br />' + param.name;
              case 'edge':
                return '演化源：' + param.data.sourceName +
                  '<br />' + '演化结果：' + param.data.targetName;
              default:
                return '';
            }
          }
        },
        label: {
          normal: {
            formatter: function (param) {
              if (param.name.length <= 10) {
                return param.name;
              } else {
                return param.name.slice(0, 10) + '...';
              }
            }
          }
        },
        series: [{
          type: 'sankey',
          // layout: 'none',
          nodes: [{name: 'Dummy Node'}],
          links: [],
          itemStyle: {
            normal: {
              borderWidth: 1,
              borderColor: '#aaa'
            }
          },
          lineStyle: {
            normal: {
              color: 'source',
              curveness: 0.5
            }
          }
        }]
      },
      loading: {
        sankey: true,
      },
    };
  },
  created () {
    this.fetchEvoluation(this.$route.params.eventId);
  },
  methods: {
    fetchEvoluation (eventId) {
      axios.get('/api/legacy/event/evolution/' + eventId + '_' + eventId).then(response => {
        let data = response.data;

        // 只保留前16个节点
        data.nodes = _.take(data.nodes.sort((a, b) => { return b.hot - a.hot; }), 20);
        data.nodes = _.filter(data.nodes, n => {
          let found = false;
          _.each(data.links, function (e) {
            if (e.source === n.id) {
              if (_.find(data.nodes, n1 => { return e.target === n1.id; })) {
                found = true;
              }
            } else if (e.target === n.id) {
              if (_.find(data.nodes, n1 => { return e.source === n1.id; })) {
                found = true;
              }
            }
          });
          return found;
        });

        // 计算边的权值
        let nodes = {};
        _.each(data.nodes, node => {
          nodes[node.id] = node;
        });
        let links = response.data.links;
        _.each(nodes, (node, id) => {
          let ls = _.filter(links, e => {
            return e.source === id;
          });
          let total = 0;
          _.each(ls, e => {
            if (nodes[e.target]) {
              total += nodes[e.target].hot;
            }
          });
          _.each(ls, e => {
            if (nodes[e.source] && nodes[e.target]) {
              e.value = (nodes[e.target].hot * nodes[e.source].hot / total).toFixed(2);
            }
          });
        });
        // 画图
        this.loading.sankey = false;
        this.sankey_option.series[0].nodes = _.values(nodes);
        this.sankey_option.series[0].links = links;
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
