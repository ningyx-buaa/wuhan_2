// for App.vue router-view
import Vue from 'vue'
import Router from 'vue-router'
import Login from 'modules/account/Login'
import LoginForm from 'modules/account/LoginForm'
import RegisterForm from 'modules/account/RegisterForm'
import ForgetPasswordForm from 'modules/account/ForgetPasswordForm'

import URLSearchParams from 'url-search-params'

Vue.use(Router)

const router = new Router({
  routes: [
    {
      // 账户首页路由
      path: '/',
      component: Login,
      children: [
        {
          path: '/',
          component: LoginForm
        },
        {
          path: '/login',
          component: LoginForm
        },
        {
          path: '/register',
          component: RegisterForm
        },
        {
          path: '/forget',
          component: ForgetPasswordForm
        },
      ]
    },
  ]
});

const whiteList = ['/', '/login', '/register', '/forget'];

const baseURL = '/account';

router.beforeEach((to, from, next) => {
  const params = new URLSearchParams();
  params.append('path', baseURL + to.fullPath);
  axios.post('/api/auth/isLogined', params.toString()).then(response => {
    if (response.data.logined) {
      router.app.$store.commit('login', response.data);
      if (whiteList.indexOf(to.path) !== -1) {
        window.location.replace('/');
      } else {
        next();
      }
    } else {
      if (whiteList.indexOf(to.path) !== -1) {
        next();
      } else {
        window.location.replace('/account/');
      }
    }
  });
});

export default router;
