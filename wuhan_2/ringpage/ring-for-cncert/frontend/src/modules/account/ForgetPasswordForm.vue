<template>
    <div class="login-form" v-title data-title="Ring 找回密码">
      <form action="">
        <div class="form-logo">
          <img src="~assets/image/login-logo.png" alt="">
        </div>
        <div class="form-group">
          <input type="text" id="username" class="form-control required" placeholder="请输入用户名">
          <label for="username" class="form-label required"><i class="fa fa-user"></i></label>
        </div>
        <div class="form-group">
          <input type="password" id="userpws" class="form-control required" placeholder="请输入手机号">
          <label for="userpws" class="form-label required"><i class="fa fa-lock"></i></label>
        </div>
        <div class="form-group form-submit">
          <button type="submit" id="submit" class="submit-control">找&nbsp;回&nbsp;密&nbsp;码</button>
        </div>
        <p class="register"><router-link to="/">返回登录</router-link></p>
      </form>
    </div>
</template>
