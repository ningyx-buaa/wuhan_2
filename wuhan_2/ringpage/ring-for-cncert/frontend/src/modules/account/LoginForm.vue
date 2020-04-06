<template>
    <div class="login-form" v-title data-title="Ring 登录">
      <form action="/api/auth/login" method="POST" @submit.capture="validate">
        <div class="form-logo">
          <img src="~assets/image/login-logo.png" alt="">
        </div>
        <div class="form-group"
            :class="{'has-danger': $v.loginname.$error,
                     'has-success': $v.loginname.$dirty && !$v.loginname.$invalid}">
          <input type="text" v-model="loginname" name="loginname"
                 placeholder="请输入用户名"
                 @input="$v.loginname.$touch"
                 class="form-control required"
                :class="{'form-control-danger': $v.loginname.$error,
                         'form-control-success': $v.loginname.$dirty && !$v.loginname.$invalid}">
          <label for="username" class="form-label required"><i class="fa fa-user"></i></label>
        </div>
        <div class="form-group"
            :class="{'has-danger': $v.password.$error,
                     'has-success': $v.password.$dirty && !$v.password.$invalid}">
          <input type="password" v-model="password" name="password"
                 placeholder="请输入密码"
                 @input="$v.password.$touch"
                 class="form-control required"
                :class="{'form-control-danger': $v.password.$error,
                         'form-control-success': $v.password.$dirty && !$v.password.$invalid}">
          <label for="userpws" class="form-label required"><i class="fa fa-lock"></i></label>
        </div>
        <div class="form-group form-forget">
          <input type="checkbox" name="rememberme" class="forget-control">
          <label for="forget">记住密码</label>
          <router-link to="/forget">忘记密码？</router-link>
        </div>
        <p class="error">{{ error }}</p>
        <div class="form-group form-submit">
          <button type="submit" id="submit" class="submit-control">登&nbsp;&nbsp;&nbsp;录</button>
        </div>
        <p class="register"><router-link to="/register">还没有账户？前往注册</router-link></p>
      </form>
    </div>
</template>

<script type="text/ecmascript-6">

import { alphaNum, required } from 'vuelidate/lib/validators'

export default {
  data () {
    return {
      loginname: '',
      password: '',
    };
  },
  computed: {
    error: function () {
      if (this.$v.loginname.$dirty) {
        if (!this.$v.loginname.required) {
          return '用户名不能为空';
        } else if (!this.$v.loginname.alphaNum) {
          return '用户名只能为字母和数字';
        }
      }
      if (this.$v.password.$dirty) {
        if (!this.$v.password.required) {
          return '密码不能为空';
        } else if (!this.$v.password.alphaNum) {
          return '密码只能包含数字和字母';
        }
      }
      return '';
    },
  },
  validations: {
    loginname: {
      alphaNum,
      required,
    },
    password: {
      required,
      alphaNum,
    },
  },
  methods: {
    validate: function (event) {
      if (!this.$v.$dirty || this.$v.$invalid) {
        event.preventDefault();
      }
    },
  },
};

</script>
