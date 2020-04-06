<template>
  <div class="page-wrapper" v-title data-title="综合推荐">
    <dl class="dl-group others">
      <dt>来&emsp;&emsp;源：</dt>
      <ul class="list-ul">
        <li class="list-btn" v-for="(name, source) in sources"
                            :key="source"
                            :class="{ active: selectedSource === source }"
                            @click="selectedSource = source">
          {{ name }}
        </li>
      </ul>
    </dl>
    <dl class="dl-group others" v-if="selectedSource === 'foreign'">
      <dt>关键词组：</dt>
      <ul class="list-ul">
        <li class="list-btn" v-for="(word, idx) in keywords"
                                 v-if="!_.isEmpty(_.trim(word))"
                                :key="idx">
          <span class="keyword">
            {{ word }}&nbsp;<span class="fa fa-times" @click="submitDeleteKeywords(word)"></span>
          </span>
        </li>
      </ul>
      <b-button class="add-btn button" v-b-model.modal-addwords><i class="fa fa-plus"></i>添加关键词</b-button>
    </dl>
    <b-modal id="modal-addwords" size="lg" title="添加关键词组"
             ok-title="确认"
             cancel-title="取消"
             @ok="submitNewKeywords">
      <div class="input-box">
        <label>关键词组：</label>
        <b-form-input v-model="newwords"
                      type="text"
                      placeholder="输入关键词组，多个词用空格分隔"></b-form-input>
      </div>
    </b-modal>
    <v-filter-box :search-input.sync="searchInput"></v-filter-box>
    <v-source-list :disp-datas="dispDatas"
                   :loading="loading"
                   :start-idx="(pageno - 1) * 64 + 1"
                   :fetch-simitems-cb="fetchSimForeignsById"></v-source-list>

    <b-pagination align="center" size="md" :limit="16"
                 :per-page="40"
                 :total-rows="10000"
                 :hide-goto-end-buttons="true"
                 :value="pageno"
                 @change="toPage">
    </b-pagination>
  </div>
</template>

<script type="text/ecmascript-6">

import Vue from 'vue'
import VBModal from 'bootstrap-vue/es/directives/modal/modal'
Vue.directive('b-model', VBModal);

import SourceList from 'components/list/SourceList'
import FilterBox from 'components/search/FilterBox'

export default {
  data () {
    return {
      dispDatas: [],
      currentPage: 2,
      totalRows: 160,
      searchInput: {
        dateStart: new Date(), // TODO truncate date to day unit.
        dateEnd: new Date(),
        includeText: false,
      },
      sources: {
        'all': '全部',
        'weibo': '微博',
        'wechat': '微信',
        'webnews': '新闻',
        'tieba': '贴吧',
        'foreign': '外文',
      },
      pageno: 1,
      loading: true,
      keywords: [],
      selectedSource: 'all',
      newwords: '',
      sourceCancel: undefined, // axios.CancelToken.source(),
    };
  },
  watch: {
    searchInput: function (input) {
      this.fetchWarnings();
    },
    selectedSource: function () {
      this.pageno = 1;
      this.fetchWarnings();
    },
    pageno: function () {
      this.fetchWarnings();
    },
  },
  created () {
    this.fetchKeywords();
    this.fetchWarnings();
  },
  methods: {
    fetchWarnings: function () {
      this.loading = true;
      this.dispDatas = [];

      if (this.sourceCancel) {
        this.sourceCancel.cancel('request out-of-date');
      }
      this.sourceCancel = axios.CancelToken.source(); // NOTICE: renew a cancel token for new request.

      axios.get('/api/cncert/v2/getDataWithSecu', {
        params: {
          size: 64,
          offset: (this.pageno - 1) * 64,
          source: this.selectedSource,
          from: this.searchInput.dateStart.format('yyyy-MM-dd'),
          to: this.$addDays(this.searchInput.dateEnd, 1).format('yyyy-MM-dd'),
          secu: 80,
          sorting: 'secu',
        },
        cancelToken: this.sourceCancel.token,
      }).then(response => {
        _.each(response.data, item => {
          if (item.user) {
            item.username = item.user;
          }
        });
        this.loading = false;
        this.dispDatas = _.orderBy(response.data, 'time', 'desc');
      });
    },
    fetchSimForeignsById: function (id, callback) {
      axios.get('/api/cache3/source/fetchSimForeignsById', {params: {
        id: id,
      }}).then(response => {
        callback(response.data);
      });
    },
    toPage: function (pageno) {
      this.pageno = pageno;
    },
    getTimeMargin (from, to) {
      let diff = (to.getTime() - from.getTime()) / 1000 / 3600 / 24;
      if (diff <= 1) {
        return '2';
      } else if (diff <= 7) {
        return '3';
      } else if (diff <= 30) {
        return '4';
      } else {
        return '5';
      }
    },
    fetchKeywords () {
      axios.get('/api/setting/getForeignWords', {params: {
        userid: 0,
      }}).then(response => {
        this.keywords = response.data.split(',');
      });
    },
    submitNewKeywords () {
      const params = new URLSearchParams();
      params.append('userid', 0);
      params.append('words', this.newwords);
      axios.post('/api/setting/addForeignWords', params.toString()).then(response => {
        this.keywords = response.data.words.split(',');
        this.fetchWarnings();
      });
    },
    submitDeleteKeywords (words) {
      const params = new URLSearchParams();
      params.append('userid', 0);
      params.append('words', words);
      axios.post('/api/setting/deleteForeignWords', params.toString()).then(response => {
        this.keywords = response.data.words.split(',');
        this.fetchWarnings();
      });
    },
  },
  components: {
    'v-source-list': SourceList,
    'v-filter-box': FilterBox,
  }
}
</script>

<style lang="sass" scoped>

@import "~assets/sass/mixin"
@import "~assets/sass/selectors"

.page-wrapper
  overflow: inherit
  height: auto
.add-btn
  color: #fff
  font-size: 1.6rem
  line-height: 1.6rem
  margin-left: 10px
  background-color: rgba(0, 171, 255, 1)
  cursor: pointer
  &:hover
    background-color: #00abff

.pagination
  margin-bottom: 1.5rem
  line-height: 1.5rem
  font-size: 1.5rem
  justify-content: center
@media (min-width: 1200px)
  .container
    width: 1200px
.page-wrapper
  width: 100%
  margin: 20px auto 0
  overflow: auto
  background-color: #fff
  padding: 20px 15px
  position: relative
  flex-direction: column
@media (min-width: 1200px)
  .page-wrapper
    width: 1200px
    min-height: 400px
@media (max-width: 768px)
  .page-wrapper
    margin-top: 0
.roll-warning-list-move
  transition: transform 1s

</style>


<style lang="sass" scoped>
.modal-content
  top: 100px
  height: 275px
  line-height: 1.6rem
  font-size: 1.6rem
  .modal-body
    .input-box
      height: 100%
      margin-top: 50px
      input
        width: calc(100% - 100px)
        display: inline-block
        line-height: 1.6rem
        font-size: 1.6rem
  .modal-footer
    button
      width: 100px
      line-height: 1.6rem
      font-size: 1.6rem
</style>
