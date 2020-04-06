<template>
  <div class="row main-row" v-title data-title="事件溯源">
    <div class="col-md-12">
      <div class="r-panel">
        <div class="panel-title-wrapper">
          <div class="panel-title">
            <span>事件溯源</span>
            <div class="switch">
              <label class="sort-label">排序方式</label>
              <b-form-select v-model="sortcmp" :options="sortopts">
              </b-form-select>
            </div>
          </div>
        </div>
        <div class="event-panel">
          <div class="events-wrapper">
            <v-desc-view :event="event"></v-desc-view>
          </div>
          <div class="timeline-panel">
            <v-timeline :points="events"></v-timeline>
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
        <div class="event-panel">
        </div>
      </div>
    </div> -->
  </div>
</template>

<script type="text/ecmascript-6">

import TimeLine from 'components/common/timeline'
import DescView from 'components/event/DescView'

export default {
  props: {
    event: {
      type: Object,
    },
  },
  data () {
    return {
      events: [],
      sortopts: [
        {text: '时间', value: 'time'},
        {text: '相关度', value: 'sim'},
        {text: '新颖度', value: 'novelty'},
      ],
      sortcmp: 'time',
    };
  },
  watch: {
    '$route': function (to, from) {
      this.fetchRelated(this.$route.params.eventId);
    },
    sortcmp: function (cmp) {
      this.events = _.orderBy(this.events, cmp, 'desc');
    },
  },
  created () {
    this.fetchRelated(this.$route.params.eventId);
  },
  methods: {
    fetchRelated (eventId) {
      axios.get('/api/legacy/event/evolution/' + eventId + '_' + eventId, {params: {
        force: 1,
        trace: true,
      }}).then(response => {
        _.each(response.data, event => {
          event.active = event.eventId === event.eventRid;
          event.url = '#/event/detail/events_v3/' + event.eventId;
        });
        this.events = response.data;
      });
    },
  },
  components: {
    'v-timeline': TimeLine,
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
  display: flex
  flex-direction: column
  flex: 1

.event-panel
  height: calc(100% - 50px)
  overflow-y: hidden

.switch
  label.sort-label
    font-size: 2.0rem
    margin-right: 10px

.timeline-panel
  padding: 0 1.5rem
  display: flex
  flex: 1 1 auto
  overflow-y: auto
  max-height: calc(100% - 200px)

@media (max-width: 768px)
  .timeline-panel
    padding: 0 1rem
</style>
