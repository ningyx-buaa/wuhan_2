<template>
  <div id="app">
    <v-header></v-header>
    <!--通过vue-router控制主内容区域变换-->
    <router-view></router-view>
  </div>
</template>

<script type="text/ecmascript-6">

import Vue from 'vue'
import Header from 'components/header/Header.vue'

// Require custom echarts theme.
import 'components/charts/theme/Ring.js'

/* Globally bus for Non Parent-Child Communication */
window.bus = new Vue();

export default {
  name: 'app',
  components: {
    'v-header': Header
  },
}
</script>

<style lang="sass">
@import "../../assets/sass/common"
@import "../../assets/sass/base"
@import "../../assets/sass/mixin"
</style>
