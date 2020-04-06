/**
 * All provided echarts options.
 */
const ChartLib = {
  'line': {
    option: {
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
          left: '3%',
          right: '4%',
          bottom: '25%',
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
          data: ['2018年12月','2019年1月','2019年2月','2019年3月','2019年4月','2019年5月','2019年6月','2019年7月',
          '2019年8月','2019年9月','2019年10月','2019年11月']
      },
      yAxis: {
          type: 'value'
      },
      series: [
          {
              name: '朝鲜',
              type: 'line',
              stack: '总量',
              areaStyle: {normal: {}},
              data: [3755,3511,3605,11859,4145,4635,63858,5108,4354,6547,7774,3720]
          }
      ]
  }
  },
//   '折线图南海': {
//     option: {
//       title: {
//           text: ''
//       },
//       tooltip: {
//           trigger: 'axis'
//       },
//       legend: {
//           data: ['朝鲜','南海','台湾']
//       },
//       grid: {
//           left: '3%',
//           right: '4%',
//           bottom: '25%',
//           containLabel: true
//       },
//       toolbox: {
//           feature: {
//               saveAsImage: {}
//           }
//       },
//       xAxis: {
//           type: 'category',
//           boundaryGap: false,
//           data: Data.static.x
//       },
//       yAxis: {
//           type: 'value'
//       },
//       series: [
//           {
//               name: '南海',
//               type: 'line',
//               stack: '总量',
//               areaStyle: {normal: {}},
//               data: Data.static.y
//           },
//       ]
//   }
//   },
  '折线图台湾': {
    option: {
      title: {
          text: ''
      },
      tooltip: {
          trigger: 'axis',
      },
      legend: {
          data: ['朝鲜','南海','台湾']
      },
      grid: {
          left: '3%',
          right: '4%',
          bottom: '25%',
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
          data: ['2018年12月','2019年1月','2019年2月','2019年3月','2019年4月','2019年5月','2019年6月','2019年7月',
          '2019年8月','2019年9月','2019年10月','2019年11月']
      },
      yAxis: {
          type: 'value'
      },
      series: [
          {
              name: '台湾',
              type: 'line',
              stack: '总量',
              areaStyle: {normal: {}},
              data: [11487,36273,7931,6272,26807,12048,11896,101372,27142,6611,28080,7223]
          }
      ]
  }
  }
};
export {ChartLib};
