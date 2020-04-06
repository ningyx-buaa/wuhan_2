import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
  state: {
    user: {
      loginName: '',
      logined: false,
      userName: '',
      roles: '',
    },
  },
  mutations: {
    login (state, user) {
      state.user = user;
    },
  },
  actions: {
  },
  strict: process.env.NODE_ENV !== 'production',
});

export default store;
