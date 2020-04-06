<template>
  <div class="login-body"  v-title data-title="Ring 用户登录">
    <div class="login-rotate">
      <div class="circle circle-5">
        <img src="~assets/image/circle-5.png" alt="">
      </div>
      <div class="circle circle-4">
        <img src="~assets/image/circle-4.png" alt="">
      </div>
      <div class="circle circle-3">
        <img src="~assets/image/circle-3.png" alt="">
      </div>
      <div class="circle circle-2">
        <img src="~assets/image/circle-2.png" alt="">
      </div>
      <div class="circle circle-1">
        <img src="~assets/image/circle-1.png" alt="">
      </div>
    </div>
    <router-view></router-view>
  </div>
</template>

<script type="text/ecmascript-6">
import Vue from 'vue'
import Vuelidate from 'vuelidate'
Vue.use(Vuelidate)

export default {}
</script>

<style lang="sass">

.login-body
  flex: 1 1 100%
  width: 100%
  top: 0
  bottom: 0
  position: relative
  background: #000522 url("~assets/image/login-bg.png") no-repeat center
  background-size: 100% 100%
.login-form
  position: absolute
  right: 20%
  transform: translate(0, -60%)
  top: 50%
  width: 380px
  height: auto
  padding: 2rem 2rem 2.5rem
  background: #fff
  border-radius: 5px
  .form-logo
    font-size: 2.5rem
    font-weight: bold
    text-align: center
    margin-bottom: 2rem
  .form-group
    position: relative
    margin-bottom: 2rem
    > label.form-label
      position: absolute
      z-index: 10
      margin-bottom: 0
      left: .8rem
      top: .9rem
      font-size: 1.8rem
      color: #666
      transition: .3s
    > input.form-control
      height: 4rem
      padding-left: 2.4rem
      position: relative
      outline: none
      font-size: 1.5rem
      color: #000
    > input.form-control:focus
      ~ label.form-label
        color: #00abff
  .form-forget
    position: relative
    margin-bottom: 3rem
    > input
      position: relative
      left: 0
      top: 2px
    > label
      cursor: pointer
      margin-bottom: 0
    > a
      position: absolute
      right: 0
      top: 0
  .form-submit
    .submit-control
      width: 100%
      height: 3.4rem
      border: none
      outline: none
      font-size: 1.8rem
      color: #fff
      background-color: #00abff
      cursor: pointer
      transition: .3s
      &:hover, &:focus
        background-color: #2283d0
  .register
    text-align: center
  .error
    text-align: center
    color: red
    height: 21px
.login-rotate
  position: absolute
  width: 434px
  height: 434px
  left: 18%
  top: 50%
  transform: translate(0, -60%)
  .circle
    position: absolute
    img
      width: 100%
  .circle-1
    animation: rotate_1 4s infinite linear
  .circle-2
    animation: rotate_2 6s 1s infinite linear
  .circle-3
    animation: rotate_3 10s 2s infinite linear
  .circle-4
    animation: rotate_4 12s 3s infinite linear
  .circle-5
    animation: rotate_5 15s 4s infinite linear
@media (min-width: 1200px) and (max-width: 1440px)
  .login-rotate
    width: 360px
    height: 360px
    left: 15%
    transform: translate(0, -55%)
  .login-form
    right: 15%
    top: 50%
    width: 340px
    transform: translate(0, -50%)
    padding: 1rem 1.5rem 1rem
    .form-group
      > label.form-label
        font-size: 2.2rem
@media (min-width: 768px) and (max-width: 1200px)
  .login-rotate
    width: 300px
    height: 300px
    left: 15%
    transform: translate(0, -50%)
  .login-form
    right: 15%
    top: 50%
    width: 320px
    transform: translate(0, -50%)
    padding: 1rem 1.5rem 1rem

@media (max-width: 768px)
  .login-rotate
    display: none
  .login-form
    left: 50%
    top: 50%
    width: 85%
    transform: translate(-50%, -50%)
    padding: .5rem 1rem
    .form-logo
      margin-bottom: 1rem
    .form-group
      margin-bottom: 1.5rem
      > label.form-label
        top: .48rem
      > input.form-control
        height: 3rem
@keyframes rotate_1
  0%
    transform: rotate(0deg)
  100%
    transform: rotate(360deg)
@keyframes rotate_2
  0%
    transform: rotate(360deg)
  100%
    transform: rotate(0deg)
@keyframes rotate_3
  0%
    transform: rotate(0deg)
  100%
    transform: rotate(360deg)
@keyframes rotate_4
  0%
    transform: rotate(360deg)
  100%
    transform: rotate(0deg)
@keyframes rotate_5
  0%
    transform: rotate(0deg)
  100%
    transform: rotate(360deg)


</style>
