<template>
  <div class="page-wrapper" v-title data-title="综合推荐">
    <div class="row index-search-row">
      <div class="col-md index-search">
        <h3 class="title"><i class="fa fa-line-chart"></i>演化分析</h3>
        <div class="search-form-group">
          <input class="search-ipt" type="text" v-model="searchwd" placeholder="人名、企业名、品牌名或事件关键词" @keyup="inputDone">
          <button class="search-btn btn" type="submit" @click="doSearch">搜&nbsp;索</button>
        </div>
      </div>
    </div>
    <v-list :disp-datas="dispDatas" :isevent='true'></v-list>

    <b-pagination align="center" size="md" :limit="8"
                 :per-page="32"
                 :total-rows="totalRows"
                 v-model="pageno">
    </b-pagination>
  </div>
</template>

<script type="text/ecmascript-6">

import Colors from 'components/Colors'
import List from 'components/list/List'

export default {
  data () {
    return {
      Colors: Colors,
      dispDatas: [],
      pageno: 1,
      totalRows: 32,
      searchwd: '携程',
    };
  },
  watch: {
    pageno: function (pageno) {
      this.findEvent();
    },
  },
  created () {
    this.findEvent();
  },
  methods: {
    findEvent: function () {
      axios.get('/api/cache1/event/findEvent', {params: {
        kws: this.searchwd,
        size: 32,
        page: this.pageno,
        degrees: false,
        imgurl: false,
        nocache: true,
        sort: 'time',
      }}).then(response => {
        let xs = response.data.content;
        _.each(xs, item => {
          // do translation.
          item.id = item.eventId;
          item.title = item.description;
          item.risk = item.risk >= 0 ? Math.round(item.risk * 100) : item.risk;
          item.recommend = item.recommend >= 0 ? Math.round(item.recommend * 100) : item.recommend;
          item.type0 = item.e_type;
          item.type1 = item.eventType;
          item.type2 = item.eventType2;
          item.esIndex = 'events_v3';
        });
        this.dispDatas = xs;
        this.totalRows = response.data.totalElements;
      });
    },
    inputDone: function (event) {
      if (event.target === document.activeElement && event.keyCode === 13) {
        this.findEvent();
      }
    },
    doSearch: function () {
      this.findEvent();
    },
  },
  components: {
    'v-list': List
  }
}
</script>

<style lang="sass" scoped>
@import "~assets/sass/base"
@import "~assets/sass/mixin"

.page-wrapper
  overflow: inherit
  height: auto
// index-search
.index-search
  text-align: center
  height: 8rem
  margin-bottom: 2rem
  .title
    color: $color_deepen
    font-size: 2.8rem
    font-weight: bold
    margin-bottom: 1.5rem
    .fa
      margin-right: .4rem
  .sub-title
    font-size: 1.8rem
    margin-bottom: 2.5rem
  .search-form-group
    display: flex
    justify-content: center
    .search-ipt
      width: 65%
      margin-right: 1rem
      border-radius: 5px
      box-shadow: none
      font-size: 16px
      padding: 0 1rem
      outline: none
      @include trend_search

    .search-btn
      cursor: pointer
      font-size: 1.8rem
      font-weight: bold
      line-height: 1
      padding: 1rem 1.8rem
      border-radius: 5px
      color: $btn_c
      background-color: $btn_bgc
      border: none
      flex-grow: 0
      flex-shrink: 0
      &:hover,&:focus
        color: $btn_hover_c


.nav-title
  font-size: 1.8rem
  > a
    font-size: 1.8rem
    color: inherit
    cursor: pointer
    &:hover, &.active
      color: #00abff!important
  > span
    font-size: 1.8rem
    color: #666
    margin: 0 .3rem

</style>
