// for App.vue router-view
import Vue from 'vue'
import Router from 'vue-router'
import Home from 'modules/Home.vue'
import Event from 'modules/detail/Event.vue'
import EventDetail from 'modules/detail/Detail.vue'
import EventDesc from 'modules/detail/Desc.vue'
import EventTrace from 'modules/detail/Trace.vue'
import EventRelated from 'modules/detail/Related.vue'
import EventEvolution from 'modules/detail/Evolution.vue'
import EventTrend from 'modules/detail/Trend.vue'
import Warning from 'modules/warning/Warning.vue'
import Viewpoint from 'modules/viewpoint/Viewpoint.vue'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      // 首页路由
      path: '/',
      name: 'Home',
      component: Home
    },
    {
      path: '/event',
      component: Event,
      name: 'Event',
    },
    {
      path: '/event/detail/:index/:eventId',
      component: EventDetail,
      children: [
        {
          path: '',
          component: EventDesc,
          name: 'Event/Detail/Overall',
        },
        {
          path: 'desc',
          component: EventDesc,
          name: 'Event/Detail/Desc',
        },
        {
          path: 'trace',
          component: EventTrace,
          name: 'Event/Detail/Trace',
        },
        {
          path: 'related',
          component: EventRelated,
          name: 'Event/Detail/Related',
        },
        {
          path: 'evolution',
          component: EventEvolution,
          name: 'Event/Detail/Evolution',
        },
        {
          path: 'trend',
          component: EventTrend,
          name: 'Event/Detail/Trend',
        },
      ],
    },
    {
      // 源数据预警
      path: '/warning',
      name: 'Warning',
      component: Warning
    },
    {
      // 选题观点分析
      path: '/viewpoint',
      name: 'Viewpoint',
      component: Viewpoint
    },
  ]
});

// fix for IE 11
if (!!window.MSInputMethodContext && !!document.documentMode) {
  window.addEventListener('hashchange', () => {
    let target = window.location.hash;
    router.push(target.substring(1, target.length));
  });
}

export default router;
