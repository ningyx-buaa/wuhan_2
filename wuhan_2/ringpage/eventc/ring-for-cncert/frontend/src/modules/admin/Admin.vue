<template>
  <div class="page-wrapper">
    <div class="aside sub-aside">
      <div class="aside-nav r-panel">
        <h3 class="nav-title">
          用户管理
        </h3>
        <ul class="nav-list">
        </ul>
      </div>

      <div class="aside-nav r-panel">
        <h3 class="nav-title">
          日志分析
        </h3>
        <ul class="nav-list">
          <b-nav-item to="/log/auth">登录统计</b-nav-item>
          <b-nav-item to="/log/trace">访问统计</b-nav-item>
        </ul>
      </div>

      <div class="aside-nav r-panel">
        <h3 class="nav-title">
          数据统计
        </h3>
        <ul class="nav-list">
          <b-nav-item to="/hist/event">事件统计</b-nav-item>
          <b-nav-item to="/hist/source">源数据统计</b-nav-item>
          <b-nav-item to="/hist/zhxg">天津数据统计</b-nav-item>
        </ul>
      </div>
    </div>

    <div class="main-wrapper">
      <div class="container-fluid container-wrapper">
        <!--内容-->
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<style lang="sass" scoped>
@import "~assets/sass/mixin"
.aside
  width: $nav_width

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
@import "~assets/sass/mixin"

.r-panel
  .monitor-view
    display: flex
    flex 1 1 0
    height: 50%
    width: 100%
    .real-chart
      display: flex
      flex 1 1 0
      height: 100%
      width: 100%
    .monitor-detail
      overflow-y: auto
      table
        table-layout: fixed
      .table-ipslocs
        th, td
          width: 33%
      .table-apis
        th, td
          &:first-child
            width: 66%
            max-width: 333px
            @include ellipsis
          &:last-child
            width: 33%


.panel-title
  > div
    margin-left: 2rem
  .date-select
    display: flex
    align-items: center
    > label
      color: #666
      font-size: 1.6rem
      margin-right: .4rem
      margin-bottom: 0
  .control
    display: flex
    align-items: center
    label
      margin: 0 0 0 0
    .command-btn
      background: #d8d8d8
      border: 1px solid #efefef
      border-radius: 3px
      padding: 0 1rem 0 1rem
</style>
