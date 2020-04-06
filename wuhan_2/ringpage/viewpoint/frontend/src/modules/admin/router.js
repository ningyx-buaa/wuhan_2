// for App.vue router-view
import Vue from 'vue'
import Router from 'vue-router'
import Admin from 'modules/admin/Admin.vue'

import URLSearchParams from 'url-search-params'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      // 管理员视角
      path: '/',
      name: 'Admin',
      component: Admin,
    },
  ]
});

const baseURL = '/admin';

router.beforeEach((to, from, next) => {
  const params = new URLSearchParams();
  params.append('path', baseURL + to.fullPath);
  axios.post('/api/auth/isLogined', params.toString()).then(response => {
    if (response.data.logined && response.data.roles.indexOf('admin') !== -1) {
      router.app.$store.commit('login', response.data);
      next();
    } else {
      window.location.replace('/account/');
    }
  }).catch(response => {
    window.location.replace('/account/');
  });
});

export default router;
