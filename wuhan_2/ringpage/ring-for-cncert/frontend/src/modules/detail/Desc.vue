<template>
  <div class="row main-row" v-title data-title="单个事件概览">
    <div class="col-md-12">
      <div class="r-panel hotnews-panel">
        <div class="panel-title-wrapper">
          <div class="panel-title">
            <span>事件概览</span>
            <div class="switch">
              <span class="fa"
                :class="{
                  'fa-toggle-on': translated,
                  'fa-toggle-off': !translated
                }"
                @click="translated = !translated"></span>
              <label class="toggle-label">翻译</label>
            </div>
          </div>
        </div>
        <div class="events-wrapper">
          <v-desc-view :event="event"></v-desc-view>
          <div class="event-panel event-panel-source">
            <div id="source-timeline" class="event-chart"></div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script type="text/ecmascript-6" scoped>

import Echarts from 'vue-echarts-v3/src/lite'
import 'echarts/lib/chart/pie'
import 'echarts/lib/component/title'
import 'echarts/lib/component/legend'
import 'echarts/lib/component/tooltip'
import 'echarts-wordcloud'

import DescView from 'components/event/DescView'

// css for timeline.
require('components/event/TimelineJS/timeline.css')

export default {
  props: {
    event: {
      type: Object,
    },
  },
  data () {
    return {
      // 源数据 id 和数据的映射
      unique_id: -1,
      sourceMap: {},
      // for timeline
      timeline: {},
      loading: {
        timeline: true,
        source: true,
        words: true,
      },
      translated: false,
    };
  },
  watch: {
    translated: function (nendtrans) {
      // 首先判断该Id的源数据存在且可以翻译。
      if (this.sourceMap[this.unique_id] && this.sourceMap[this.unique_id].origin_title) {
        let title = '';
        let content = '';
        if (nendtrans) {
          title = this.sourceMap[this.unique_id].title;
          content = this.sourceMap[this.unique_id].content;
        } else {
          title = this.sourceMap[this.unique_id].origin_title;
          content = this.sourceMap[this.unique_id].origin_content;
          this.translated = false;
        }
        const dom = _.find(this.$el.getElementsByClassName('tl-slide-text-only'), x => x.id === this.unique_id);
        const titleWrapper = dom.getElementsByClassName('tl-headline');
        if (titleWrapper.length > 0) {
          titleWrapper[0].getElementsByTagName('a')[0].innerText = title;
        }
        const contentWrapper = dom.getElementsByClassName('tl-text-content');
        if (contentWrapper.length > 0) {
          contentWrapper[0].getElementsByTagName('p')[0].innerText = content;
        }
      }
    },
    unique_id: id => {
      console.log(id);
    },
  },
  created () {
  },
  mounted () {
    require(['components/event/TimelineJS/timeline.js'], this.fetchTimeLine);
  },
  methods: {
    fetchTimeLine (TL) {
      axios.get('/api/legacy/getEventById', {params: {
        eventId: this.$route.params.eventId,
      }}).then(response1 => {
        if (response1.data) {
          axios.get('/api/legacy/getEventSourceById', {params: {
            eventId: this.$route.params.eventId,
            similarity: true,
          }}).then(response2 => {
            if (response2.data) {
              this.makeTimeLine(TL, response2.data, response1.data);
            } else {
              this.makeTimeLine(TL, [], response1.data);
            }
          })
        } else {
          // 如果该事件在events_v3中不存在，则getEventSourceById返回的结果是错误信息，fallback
          axios.get('/api/cache3/source/fetchValueFromIndex', {params: {
            index: this.$route.params.index,
            id: this.$route.params.eventId,
          }}).then(response2 => {
            let item = response2.data;
            // do reverse translation.
            item.eventId = item.id;
            item.description = item.title;
            this.makeTimeLine(TL, [], item);
          });
        }
      });
    },
    makeTimeLine (TL, data, event) {
      TL = TL.default;
      let sources = {
        events: [],
      };
      data = _.orderBy(data, 'releaseDate');

      let maxSim = 0.0;
      let maxSimIndex = -1;

      if (_.isEmpty(data)) { // 如果返回的源数据为空，显示事件本身。
        let date = new Date(event.time);
        sources.events = [{
          start_date: {
            year: date.getFullYear(),
            month: date.getMonth() + 1,
            day: date.getDate(),
            hour: date.getHours(),
            minute: date.getMinutes(),
            second: date.getSeconds(),
            display_date: date.format('yyyy-MM-dd hh:mm:ss'),
          },
          text: {
            headline: '<a href="#/event/detail/' + this.event.eventId + '" target="_blank">' + event.description + '</a>',
            text: '',
          },
          unique_id: event.eventId,
        }];
      } else {
        sources.events = _.map(data, item => {
          let date = new Date(item.releaseDate);

          let text = item.content;
          if (item.foreign && item.content) {
            text = '<div class="text-content-wrapper">' +
                   '<div class="text-content-inner text-content-left">' + item.origin_content + '</div>' +
                   '<div class="text-content-sep"></div>' +
                   '<div class="text-content-inner text-content-right">' + item.content + '</div>' +
                   '</div>';
          }

          let source = {
            start_date: {
              year: date.getFullYear(),
              month: date.getMonth() + 1,
              day: date.getDate(),
              hour: date.getHours(),
              minute: date.getMinutes(),
              second: date.getSeconds(),
              display_date: date.format('yyyy-MM-dd hh:mm:ss'),
            },
            text: {
              headline: '<a href="' + item.url + '" target="_blank">' + (item.origin_title || item.title) + '</a>',
              text: text,
            },
            unique_id: item.id,
          };
          return source;
        });
        this.unique_id = _.last(data).id;

        // calculate max similarity index.
        for (var i = 0; i < data.length; ++i) {
          if (data[i].similarity > maxSim) {
            maxSimIndex = i;
            maxSim = data[i].similarity;
          }
        }
      }
      let options = {
        language: 'zh_CN',
        start_at_end: true,
        debug: true,
        onloadedCallback: () => {
          // Patch the timeline view
          _.each(document.getElementsByClassName('tl-slide-content'), el => {
            el.style.width = '100%';
          });

          // Fix layout: on small screen, we need provide better timeline view.
          let hbase = document.getElementsByClassName('event-panel')[0].offsetHeight;
          let htl = document.getElementById('source-timeline').offsetHeight;
          if (hbase < 200 || hbase < htl) {
            let tl = document.getElementById('source-timeline');
            tl.removeChild(tl.children[1]);
            tl.removeChild(tl.children[1]);
            document.getElementsByClassName('tl-storyslider')[0].style.height = htl + 'px';
          } else {
            document.getElementsByClassName('tl-storyslider')[0].style.height = (htl - 165) + 'px';
          }

          // patch for sougou browser
          if (document.getElementsByClassName('tl-slidenav-next')) {
            document.getElementsByClassName('tl-slidenav-next')[0].style.top = '';
          }
          if (document.getElementsByClassName('tl-slidenav-previous')) {
            document.getElementsByClassName('tl-slidenav-previous')[0].style.top = '';
          }
        },
      };
      this.timeline = new TL.Timeline('source-timeline', sources, options);
      // event binding
      this.timeline.on('change', value => {
        this.unique_id = value.unique_id;
      });

      if (maxSimIndex >= 0) {
        this.timeline.goTo(maxSimIndex);
      }
      // Store source data in map.
      _.each(data, item => {
        this.sourceMap[item.id] = item;
      });

      // 外文数据构成比例图
      if (this.event.foreign) {
        let foreignNews = 0;
        let nonforeignNews = 0;
        _.each(data, item => {
          if (item.datatag === 'webnews') {
            if (item.foreign) {
              foreignNews += 1;
            } else {
              nonforeignNews += 1;
            }
          }
        });
        this.event_source_option.series[1].data = [
          {name: '外文新闻', value: foreignNews, selected: true},
          {name: '中文新闻', value: nonforeignNews},
        ];
      }
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
  display: flex
  flex-direction: column
  flex: 1

.event-panel
  height: calc(100% - 250px)
  display: flex
  flex: 1 1 auto
  overflow-y: auto

.event-panel.event-panel-source
  flex: 1 1 0
  height: auto
  overflow-y: hidden
  padding: 0 1.5rem 1rem
  .event-chart
    height: 100% // inherit
    display: flex
    flex: 1 1 0

.r-panel
  .event-chart
    display: flex
    flex: 1 1 0
</style>

<style lang="sass">

/*css for timeline.*/

.tl-slide
  overflow-y: hidden!important
  .tl-slide-content-container
    .tl-slide-content
      .tl-text
        overflow: hidden
        .tl-headline-date
          font-size: 14px
          margin-bottom: 4px
          color: #999
        h2.tl-headline
          font-size: 24px
          line-height: 28px
          max-height: 56px
          overflow: hidden
          font-weight: bold
          margin-bottom: 8px
          a
            /*color: #414b54*/
            transition: .3s
            &:focus, &:hover
              color: #256bcc
        .tl-text-content
          max-height: 140px
          overflow: hidden
          p
            font-size: 14px
            line-height: 20px
            margin-top: 0
            color: #454545

.tl-storyslider
  height: 100%
  .tl-slidenav-title
    display: none!important
  .tl-slidenav-description
    display: none!important
.tl-slidenav-icon
  color: #999!important
  transition: .3s
  &:hover
    color: #454545!important

/* Timeline里面外文/中文内容并排显示 */
.text-content-wrapper
  display: flex
  justify-content: space-between
  .text-content-inner
    width: 46%
    display: inline-block
  .text-content-sep
    width: 1px
    border-left: 2px solid
    display: inline-block

</style>
