<template>
  <div class="page-wrapper" v-title data-title="事件分析">
    <v-filter-tab @update:filter="updateFilter"></v-filter-tab>
    <v-search-box :search-input.sync="searchInput"></v-search-box>
    <!-- <v-list :disp-datas="dispDatas"
            :fetch-simitems-cb="fetchSimNewsById"></v-list> -->
    <!-- <div class="con-box l-t-box">
        <Echarts theme="ring" :option="options.left_up.option" className="chart" ></Echarts>
    </div>
    <div class="con-box r-t-box">
      <Echarts theme="ring" :option="options.right_up.option" className="chart" ></Echarts>
    </div>
    <div class="con-box l-b-box">
      <Echarts theme="ring" :option="options.left_down.option" className="chart" ></Echarts>
    </div>
    <div class="con-box r-b-box" @click="goto">
      <div class="chart">
        <Echarts theme="ring" :option="options.right_down.option" className="chart" ></Echarts>
      </div>
    </div> -->
    <!-- <iframe> -->
      <table border="" cellspacing="" cellpadding="" style="width:1200px">
            <tr>
                <td style="width:500px; height: 300px">
                  <div id="left_up" ref="myCharts" style="width:500px; height: 300px"></div>
                </td>
                <td style="width:500px; height: 300px">
                  <div id="right_up" ref="myCharts" style="width:500px; height: 300px"></div>
                </td>
            </tr>
            <tr>
                <td style="width:500px; height: 300px">
                  <!-- <div id="left_down" ref="myCharts" style="width:500px; height: 300px"></div> -->
                  <table border="" > 
                    <tr>
                      <td style="width:250px; height: 40px">专家观点聚类</td>
                    </tr>
                    <tr>
                      <td style="width:50px; height: 40px">同类观点数量</td>
                      <td style="width:450px; height: 40px">中心观点</td>
                    </tr>
                    <tr>
                      <td style="width:30px; height: 40px">22</td>
                      <td style="width:470px; height: 40px">外交部：反对以航行自由名损害中国主权和安全</td>
                    </tr>
                    <tr>
                      <td style="width:30px; height: 40px">12</td>
                      <td style="width:470px; height: 40px">外交部：美方“横行自由才是南海局势紧张根源</td>
                    </tr>
                    <tr>
                      <td style="width:30px; height: 40px">17</td>
                      <td style="width:470px; height: 40px">美军接连两天在南海动 解放军警告驱离</td>
                    </tr>
                    <tr>
                      <td style="width:30px; height: 40px">26</td>
                      <td style="width:470px; height: 40px">挑衅？美国派军舰连续两天闯中国南岛礁 ，被我军海空兵力警告驱离</td>
                    </tr>
                  </table>
                </td>
                <td style="width:500px; height: 300px">
                  <table border="" > 
                    <tr>
                      <td style="width:250px; height: 40px">专家参与度统计</td>
                    </tr>
                    <tr>
                      <td style="width:250px; height: 40px">专家</td>
                      <td style="width:250px; height: 40px">热度</td>
                      <td style="width:250px; height: 40px">职位</td>
                    </tr>
                    <tr>
                      <td style="width:250px; height: 40px">戴旭</td>
                      <td style="width:250px; height: 40px">100</td>
                      <td style="width:250px; height: 40px">新闻媒体</td>
                    </tr>
                    <tr>
                      <td style="width:250px; height: 40px">华春莹</td>
                      <td style="width:250px; height: 40px">120</td>
                      <td style="width:250px; height: 40px">外交部</td>
                    </tr>
                    <tr>
                      <td style="width:250px; height: 40px">博尔顿</td>
                      <td style="width:250px; height: 40px">80</td>
                      <td style="width:250px; height: 40px">美国国家安全顾问</td>
                    </tr>
                  </table>
                </td>
            </tr>
      </table>
    <!-- </iframe> -->
    <!-- <b-pagination align="center" size="md" :limit="8"
                 :per-page="64"
                 :total-rows="totalRows"
                 v-model="pageno"> -->
    <!-- </b-pagination> -->
  </div>
</template>

<script type="text/ecmascript-6">

import Colors from 'components/Colors'
import List from 'components/list/List'
import SearchBox from 'components/search/SearchBox'
import FilterTab from 'components/filtertab/FilterTab'
// <<<<<<< HEAD
import {ChartLib} from './ChartLib.js'
import echarts from 'echarts'
// import Data from "../assets/data.json"
// =======
import Data from "../assets/data.json"
// >>>>>>> ce55a63e31a0b74836a5426d38bb75309f1fabb5

export default {
  data () {
    return {
      Colors: Colors,
      dispDatas: [],
      totalRows: 64,
      pageno: 1,
      options: {
        left_up: { option: {xAxis: {
              type: 'category',
              data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
            },
            yAxis: {
              type: 'value'
            },
            series: [{
              data: [820, 932, 901, 934, 1290, 1330, 1320],
              type: 'line'
            }]}},
        right_up: { option: {}, update: () => { return; } },
        left_down: { option: {}, update: () => { return; } },
        right_down: { option: {}, update: () => { return; } },
      },
      searchInput: {
        kws: '',
        dateStart: new Date(), // TODO truncate date to day unit.
        dateEnd: new Date(),
        includeText: false,
      },
      filter: {},
    };
  },
  watch: {
    searchInput: function (input) {
      this.findDatas(this.filter);
    },
    pageno: function (pageno) {
      this.findDatas(this.filter);
    },
  },
  created () {
    this.findDatas();
    // var echarts = require('echarts');
  },
  mounted () {
    console.log("123456");
    // this.options.left_up.option = ChartLib.折线图朝鲜.option;
    var myChart = echarts.init(document.getElementById('left_up'));
    var left_up_option = {
      title: {
          text: ''
      },
      tooltip: {
          trigger: 'axis'
      },
      legend: {
          data: ['朝鲜','南海','台湾']
      },
      grid: {
          left: '1%',
          right: '1%',
          bottom: '2%',
          containLabel: true
      },
      toolbox: {
          feature: {
              saveAsImage: {}
          }
      },
      xAxis: {
          type: 'category',
          boundaryGap: false,
          data: ['2019年12月1日','2019年12月2日','2019年12月3日','2019年12月4日','2019年12月5日','2019年12月6日','2019年12月7日','2019年12月8日',
          '2019年12月9日','2019年12月10日','2019年12月11日','2019年12月12日']
      },
      yAxis: {
          type: 'value'
      },
      series: [
          {
              name: '南海',
              type: 'line',
              stack: '总量',
              areaStyle: {normal: {}},
              data: [3755,3511,3605,11859,4145,4635,63858,5108,4354,6547,7774,3720]
          },
      ]
  }
    myChart.setOption(left_up_option);
    // console.log(myChart);
    myChart = echarts.init(document.getElementById('right_up'));
    // var right_up_option = {
    //     title: {
    //         text: '各项指数分析',
    //         subtext: '数据来自网络'
    //     },
    //     tooltip: {
    //         trigger: 'axis',
    //         axisPointer: {
    //             type: 'shadow'
    //         }
    //     },
    //     legend: {
    //         data: ['2011年', '2012年']
    //     },
    //     grid: {
    //         left: '3%',
    //         right: '4%',
    //         bottom: '3%',
    //         containLabel: true
    //     },
    //     xAxis: {
    //         type: 'value',
    //         boundaryGap: [0, 0.01]
    //     },
    //     yAxis: {
    //         type: 'category',
    //         data: ['0.2-0.4', '0-0.2', '1.5以上', '0.8-1.5', '0.4-0.8', '活跃度']
    //     },
    //     series: [
    //         {
    //             name: '2011年',
    //             type: 'bar',
    //             data: [18203, 23489, 29034, 104970, 131744, 630230]
    //         },
    //         {
    //             name: '2012年',
    //             type: 'bar',
    //             data: [19325, 23438, 31000, 121594, 134141, 681807]
    //         }
    //     ]
    // };
    var ru_data = this.genData(50);
    var right_up_option = {
          title: {
              text: '美军两架B-1B轰炸机与日空自联演后飞越南海上空',
              left: 'center'
          },
          tooltip: {
              trigger: 'item',
              formatter: '{a} <br/>{b} : {c} ({d}%)'
          },
          legend: {
              // orient: 'vertical',
              // top: 'middle',
              bottom: 10,
              left: 'center',
              data: ["入侵行动", "国家立场", "防卫行动", "军演行动", "媒体评论", "访问行动"]
          },
          series: [
              {
                  type: 'pie',
                  radius: '65%',
                  center: ['50%', '50%'],
                  selectedMode: 'single',
                  data: [
                      {
                          value: 1548,
                          name: '外国发动入侵行动'
                      },
                      {value: 535, name: '国家所持有的立场'},
                      {value: 510, name: '南海采取相应防卫行动'},
                      {value: 634, name: '外国在我南海进行军演行动'},
                      {value: 735, name: '各家媒体进行评论报道'},
                      {value: 735, name: '国家元首决定进行访问行动'}
                  ],
                  emphasis: {
                      itemStyle: {
                          shadowBlur: 10,
                          shadowOffsetX: 0,
                          shadowColor: 'rgba(0, 0, 0, 0.5)'
                      }
                  }
              }
          ]
      };
    myChart.setOption(right_up_option);

    myChart = echarts.init(document.getElementById('left_down'));
    var left_down_option = {
        title: {
            text: '媒体观点'
        },
        tooltip: {
            trigger: 'axis',
            axisPointer: {            // 坐标轴指示器，坐标轴触发有效
                type: 'shadow'        // 默认为直线，可选为：'line' | 'shadow'
            },
            formatter: function (params) {
                var tar = params[0];
                return tar.seriesName;
            }
        },
        legend: {
            data: ['媒体观点']
        },
        grid: {
            left: '3%',
            right: '4%',
            bottom: '3%',
            containLabel: true
        },
        xAxis: {
            type: 'value',
            label: {
              normal: {
                show: false,
                position: "inside",
              }
            }
        },
        yAxis: {
            type: 'category',
            data: ["", "", "", "", ""],
            show: false,
            position: 'top'
            // label: {
            //   normal: {
            //     show: false,
            //     position: "inside",
            //   }
            // }
        },
        series: [
            {
                name: '日本最大军舰跟着美航母“巡航”南海',
                type: 'bar',
                stack: '总量',
                label: {
                    show: true,
                    position: 'inside'
                },
                data: [500,400,300,200,100]
            }
            // {
            //     name: '日本最大军舰跟着美航母“巡航”南海',
            //     type: 'bar',
            //     // stack: '总量',
            //     // label: {
            //     //     show: true,
            //     //     position: 'insideRight'
            //     // },
            //     data: [200]
            // },
            // {
            //     name: '美航母无事生非搅动南海借“航行自由”吓唬中国',
            //     type: 'bar',
            //     // stack: '总量',
            //     // label: {
            //     //     show: true,
            //     //     position: 'insideRight'
            //     // },
            //     data: [150]
            // },
            // {
            //     name: '东盟十国军方高层齐声：希望南海和平',
            //     type: 'bar',
            //     stack: '总量',
            //     label: {
            //         show: true,
            //         position: 'insideRight'
            //     },
            //     data: [100]
            // }
        ]
    };
    myChart.setOption(left_down_option);
  },
  beforeDestroy () {
  },
  methods: {
    genData (count) {
        var nameList = [
            '赵', '钱', '孙', '李', '周', '吴', '郑', '王', '冯', '陈', '褚', '卫', '蒋', '沈', '韩', '杨', '朱', '秦', '尤', '许', '何', '吕', '施', '张', '孔', '曹', '严', '华', '金', '魏', '陶', '姜', '戚', '谢', '邹', '喻', '柏', '水', '窦', '章', '云', '苏', '潘', '葛', '奚', '范', '彭', '郎', '鲁', '韦', '昌', '马', '苗', '凤', '花', '方', '俞', '任', '袁', '柳', '酆', '鲍', '史', '唐', '费', '廉', '岑', '薛', '雷', '贺', '倪', '汤', '滕', '殷', '罗', '毕', '郝', '邬', '安', '常', '乐', '于', '时', '傅', '皮', '卞', '齐', '康', '伍', '余', '元', '卜', '顾', '孟', '平', '黄', '和', '穆', '萧', '尹', '姚', '邵', '湛', '汪', '祁', '毛', '禹', '狄', '米', '贝', '明', '臧', '计', '伏', '成', '戴', '谈', '宋', '茅', '庞', '熊', '纪', '舒', '屈', '项', '祝', '董', '梁', '杜', '阮', '蓝', '闵', '席', '季', '麻', '强', '贾', '路', '娄', '危'
        ];
        var legendData = [];
        var seriesData = [];
        var selected = {};
        var tmpname = null;
        for (var i = 0; i < count; i++) {
            tmpname = Math.random() > 0.65
                ? makeWord(4, 1) + '·' + makeWord(3, 0)
                : makeWord(2, 1);
            legendData.push(tmpname);
            seriesData.push({
                name: tmpname,
                value: Math.round(Math.random() * 100000)
            });
            selected[tmpname] = i < 6;
        }

        return {
            legendData: legendData,
            seriesData: seriesData,
            selected: selected
        };

        function makeWord (max, min) {
            var nameLen = Math.ceil(Math.random() * max + min);
            var name = [];
            for (var i = 0; i < nameLen; i++) {
                name.push(nameList[Math.round(Math.random() * nameList.length - 1)]);
            }
            return name.join('');
        }
    },
    findDatas: function (filter = {
      selectedTypes: [],
      selectedLanguge: '全部',
      selectedLocation: '全部',
      selectedSecu: false,
      selectedWords: [],
    }) {
      // axios.get('/api/cncert/source/findDatas', {params: {
      //   from: this.searchInput.dateStart.format('yyyy-MM-dd'),
      //   to: this.searchInput.dateEnd.format('yyyy-MM-dd'),
      //   kws: this.searchInput.kws,
      //   kws_kinds: _.join(filter.selectedWords, ' '),
      //   include_text: this.searchInput.includeText,
      //   size: 64,
      //   pageno: this.pageno,
      //   sort: filter.selectedSecu ? 'risk' : '', // 如果选中“突发敏感”，搜索时按secu排序，否则按时间排序
      //   types0: _.join(filter.selectedLegacyTypes, ' '),
      //   types2: _.join(filter.selectedTypes, ' '),
      //   language: filter.selectedLanguge,
      //   location: filter.selectedLocation,
      // }}).then(response => {
      //   this.dispDatas = response.data.content;
      //   this.totalRows = response.data.totalElements;
      // });
      this.dispDatas = Data;
      console.log(this.dispDatas);
      this.totalRows = 64;
      console.log(this.totalRows);
    },
    fetchSimNewsById: function (id, callback) {
      axios.get('/api/cache3/source/fetchSimNewsById', {params: {
        id: id,
      }}).then(response => {
        callback(response.data);
      });
    },
    updateFilter: function (filter) {
      this.filter = filter;
      this.findDatas(filter);
    },
  },
  components: {
    'v-list': List,
    'v-search-box': SearchBox,
    'v-filter-tab': FilterTab,
  }
};
</script>

<style lang="sass" >
@import "~assets/sass/mixin"
.con-box
  position: absolute
  width: 37%
  height: 45%
  overflow: scroll
  padding: .7rem 1rem .8rem
  // background-image: url("~assets/images/box-bg.png")
  background-size: 100% 100%
  z-index: 1000
  cursor: pointer
  &.l-t-box
    left: 1.5rem
    top: 1.2rem
  &.r-t-box
    right: 1.5rem
    top: 1.2rem
  &.l-b-box
    left: 1.5rem
    bottom: 1.2rem
  &.r-b-box
    right: 1.5rem
    bottom: 1.2rem
    .chart
      bottom: 100rem
      width: 100%
      height: 80%
.real-body
  min-height: 100%
  height: 100%
  display: flex
  flex-direction: column
  .r-header
    flex: 0 0 auto
  .page-wrapper
    flex: 1 0 auto
  .r-footer
    flex: 0 0 auto
</style>

<style lang="sass" scoped>
.page-wrapper
  overflow: inherit
  height: auto
.pagination
  margin-bottom: 1.5rem
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
  // position: relative
  flex-direction: column
// @media (min-width: 1200px)
//   .page-wrapper
//     width: 1200px
//     min-height: 525px
// @media (max-width: 768px)
//   .page-wrapper
//     margin-top: 0
// .roll-warning-list-move
//   transition: transform 1s
</style>

<style type="text/css">
	table{
		border-collapse:collapse;
		table-layout:fixed;
		border-radius:5px;
		overflow:hidden;
		margin: 0px auto;
		border:0px solid #010811 ;
		background-color: white;
		color: #01060f;
		}
	 table td,th{
		/* padding: 5px;
		width: 33%; */
		text-align: center;
		border:1px solid #01070f ;
		vertical-align:middle;
		/* font-size: 15px; */
		width: 120%;
	}

	 .table-color-green{
	 	background-color: green;

	 }

	 .table-color-grey{

	 	background-color:  #696969;
	 }

	  .table-color-black{

	 	background-color: black;
	 }

	 .jt-up-color{
	   color:red;
	 }

	 .parent-position{
	 	position: relative;
	 }

	 .child-position{
	 	    position: absolute;
		    right: 0;
		    bottom: 0;
	 }

	 .main-font{
	 	    font-size: 23px;
	 }
</style>
