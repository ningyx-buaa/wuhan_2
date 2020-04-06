<template>
  <div class="page-wrapper">
    <div class="aside sub-aside">
      <div class="aside-nav r-panel">
        <h3 class="nav-title">
          事件详情
        </h3>
        <ul class="nav-list">
          <b-nav-item  :to="{name:'Event/Detail/Desc', params: {index: $route.params.index, eventId: $route.params.eventId}}">事件概览</b-nav-item>
          <b-nav-item  :to="{name:'Event/Detail/Trace', params: {index: $route.params.index, eventId: $route.params.eventId}}">事件溯源</b-nav-item>
          <b-nav-item  :to="{name:'Event/Detail/Related', params: {index: $route.params.index, eventId: $route.params.eventId}}">相关事件</b-nav-item>
          <b-nav-item  :to="{name:'Event/Detail/Evolution', params: {index: $route.params.index, eventId: $route.params.eventId}}">事件演化</b-nav-item>
          <b-nav-item  :to="{name:'Event/Detail/Trend', params: {index: $route.params.index, eventId: $route.params.eventId}}">事件趋势</b-nav-item>
          <!-- <b-nav-item :to="'/detail/' + $route.params.eventId + '/similar'">事件研判</b-nav-item> -->
        </ul>
      </div>
    </div>

    <div class="main-wrapper">
      <div class="container-fluid container-wrapper">
        <!--指数-->
        <!-- <div class="row index-row" v-if="!partition || partition === '/' || partition === '/desc'">
          <v-index-count-box
            name="影响力"
            v-bind:frontcolor="CommonColor[0]"
            value="88"
            update="14">
          </v-index-count-box>
          <v-index-count-box
            name="重要度"
            v-bind:frontcolor="CommonColor[1]"
            value="92"
            update="23">
          </v-index-count-box>
          <v-index-count-box
            name="敏感度"
            v-bind:frontcolor="CommonColor[2]"
            value="70"
            update="1">
          </v-index-count-box>
          <v-index-count-box
            name="风险度"
            v-bind:frontcolor="CommonColor[3]"
            value="68"
            update="-2">
          </v-index-count-box>
        </div> -->

        <!--内容-->
        <router-view :event="event"></router-view>
      </div>
    </div>
  </div>
</template>

<script type="text/ecmascript-6">

import IndexCountBox from 'components/event/IndexCountBox'
import Colors from 'components/Colors'

export default {
  data () {
    return {
      index: [],
      event: {},
      CommonColor: Colors.CommonColor,
    };
  },
  computed: {
    partition: function () {
      return _.last(_.split(this.$route.path, this.$route.params.eventId));
    },
  },
  watch: {
    '$route.params.eventId': function () {
      this.fetchEvent();
    },
  },
  created: function () {
    this.fetchEvent();
    // this.fetchIndex();
  },
  methods: {
    fetchEvent () {
      axios.get('/api/legacy/getEventById', {params: {
        eventId: this.$route.params.eventId,
      }}).then(response => {
        if (response.data) {
          this.event = response.data;
        } else { // fallback
          axios.get('/api/cache3/source/fetchValueFromIndex', {params: {
            index: this.$route.params.index,
            id: this.$route.params.eventId,
          }}).then(response => {
            let item = response.data;
            // do reverse translation.
            item.eventId = item.id;
            item.description = item.title;
            item.e_type = item.type0;
            item.eventType = item.type1;
            item.eventType2 = item.type2;
            item.from = item.user;
            item.eventSpanDateString = new Date(item.time).format('yyyy-MM-dd hh:mm:ss');
            item.delayed = true;
            this.event = item;
          })
        }
      });
    },
  },
  components: {
    'v-index-count-box': IndexCountBox,
  },
};
</script>

<style lang="sass" scoped>
@import "~assets/sass/mixin"

.real-body
  min-height: 100%
  height: 100%!important
.page-wrapper
  flex-direction: row
.main-wrapper
  height: 100%
.container-wrapper
  height: 100%
.aside
  width: 17rem
.aside-nav
  max-height: none
  .r-panel
    .nav-title
      padding-right: 2rem
      display: flex
      justify-content: space-between
      .fa-plus
        cursor: pointer
        color: #000000
        font-size: 1.8rem
        transition: .3s
        &:hover
          color: #00abff
</style>

<style lang="sass">
.container-wrapper
  height: 100%
  .main-row
    height: 100%
    .col-md-12
      height: 100%
</style>
