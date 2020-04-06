<template>
    <div class="login-form" v-title data-title="Ring 注册">
      <form action="/api/auth/register" method="POST" @submit.capture="validate">
        <div class="form-logo">
          <img src="~assets/image/login-logo.png" alt="">
        </div>
        <div class="form-group"
            :class="{'has-danger': $v.loginName.$error,
                     'has-success': $v.loginName.$dirty && !$v.loginName.$invalid}">
          <input type="text" v-model="loginName" name="loginName"
                 placeholder="请输入用户名"
                 @input="$v.loginName.$touch"
                 class="form-control required"
                :class="{'form-control-danger': $v.loginName.$error,
                         'form-control-success': $v.loginName.$dirty && !$v.loginName.$invalid}">
          <label for="" class="form-label required"><i class="fa fa-user"></i></label>
        </div>
        <div class="form-group"
            :class="{'has-danger': $v.username.$error,
                     'has-success': $v.username.$dirty && !$v.username.$invalid}">
          <input type="text" v-model="username" name="name"
                 placeholder="请输入昵称"
                 @input="$v.username.$touch"
                 class="form-control required"
                :class="{'form-control-danger': $v.username.$error,
                         'form-control-success': $v.username.$dirty && !$v.username.$invalid}">
          <label for="" class="form-label required"><i class="fa fa-user"></i></label>
        </div>
        <div class="form-group"
            :class="{'has-danger': $v.email.$error,
                     'has-success': $v.email.$dirty && !$v.email.$invalid}">
          <input type="email" v-model="email" name="email"
                 placeholder="请输入邮箱"
                 @input="$v.email.$touch"
                 class="form-control required"
                :class="{'form-control-danger': $v.email.$error,
                         'form-control-success': $v.email.$dirty && !$v.email.$invalid}">
          <label for="" class="form-label required"><i class="fa fa-envelope-square"></i></label>
        </div>
        <div class="form-group"
            :class="{'has-danger': $v.plainPassword.$error,
                     'has-success': $v.plainPassword.$dirty && !$v.plainPassword.$invalid}">
          <input type="password" v-model="plainPassword" name="plainPassword"
                 placeholder="请输入密码"
                 @input="$v.plainPassword.$touch"
                 class="form-control required"
                :class="{'form-control-danger': $v.plainPassword.$error,
                         'form-control-success': $v.plainPassword.$dirty && !$v.plainPassword.$invalid}">
          <label for="" class="form-label required"><i class="fa fa-lock"></i></label>
        </div>
        <div class="form-group"
            :class="{'has-danger': $v.confirmPassword.$error,
                     'has-success': $v.confirmPassword.$dirty && !$v.confirmPassword.$invalid}">
          <input type="password" v-model="confirmPassword" name="confirmPassword"
                 placeholder="请再次输入密码"
                 @input="$v.confirmPassword.$touch"
                 class="form-control required"
                :class="{'form-control-danger': $v.confirmPassword.$error,
                         'form-control-success': $v.confirmPassword.$dirty && !$v.confirmPassword.$invalid}">
          <label for="" class="form-label required"><i class="fa fa-lock"></i></label>
        </div>
        <p class="error">{{ error }}</p>
        <div class="form-group form-submit">
          <button type="submit" class="submit-control">注&nbsp;&nbsp;&nbsp;册</button>
        </div>
        <p class="register"><router-link to="/">已有账号？返回登录</router-link></p>
      </form>
    </div>
</template>

<script type="text/ecmascript-6">

import withParams from 'vuelidate/lib/withParams'
import { alphaNum, email, required, minLength, maxLength, sameAs } from 'vuelidate/lib/validators'
import URLSearchParams from 'url-search-params'

const unique = (field, api) => withParams({type: 'unique'}, value => {
  if (value.length === 0) {
    return false;
  }
  const params = new URLSearchParams();
  params.append(field, value);
  return new Promise((resolve, reject) => {
    axios.post(api, params.toString()).then(response => {
      resolve(response.data);
    });
  });
});

export default {
  data () {
    return {
      loginName: '',
      username: '',
      email: '',
      plainPassword: '',
      confirmPassword: '',
    };
  },
  computed: {
    error: function () {
      if (this.$v.loginName.$dirty && !this.$v.loginName.$pending) {
        if (!this.$v.loginName.required) {
          return '用户名不能为空';
        } else if (!this.$v.loginName.alphaNum) {
          return '用户名只能为字母和数字';
        } else if (!this.$v.loginName.minLength) {
          return '用户名至少' + this.$v.loginName.$params.minLength.min + '个字符';
        } else if (!this.$v.loginName.maxLength) {
          return '用户名最多' + this.$v.loginName.$params.maxLength.max + '个字符';
        } else if (!this.$v.loginName.unique) {
          return '用户名已被注册';
        }
      }
      if (this.$v.username.$dirty) {
        if (!this.$v.username.required) {
          return '昵称不能为空';
        } else if (!this.$v.username.maxLength) {
          return '昵称最多' + this.$v.username.$params.maxLength.max + '个字符';
        }
      }
      if (this.$v.email.$dirty && !this.$v.email.$pending) {
        if (!this.$v.email.required) {
          return '邮箱不能为空';
        } else if (!this.$v.email.email) {
          return '邮箱格式错误';
        } else if (!this.$v.email.unique) {
          return '此邮箱已被注册';
        }
      }
      if (this.$v.plainPassword.$dirty) {
        if (!this.$v.plainPassword.required) {
          return '密码不能为空';
        } else if (!this.$v.plainPassword.alphaNum) {
          return '密码只能包含数字和字母';
        } else if (!this.$v.plainPassword.minLength) {
          return '密码至少' + this.$v.plainPassword.$params.minLength.min + '个字符';
        } else if (!this.$v.plainPassword.maxLength) {
          return '密码最多' + this.$v.plainPassword.$params.maxLength.max + '个字符';
        }
      }
      if (this.$v.confirmPassword.$dirty) {
        if (!this.$v.confirmPassword.sameAsPlainPassword) {
          return '两次输入密码不一致';
        }
      }
      return '';
    },
  },
  validations: {
    loginName: {
      alphaNum,
      required,
      minLength: minLength(1),
      maxLength: maxLength(16),
      unique: unique('loginName', '/register/checkLoginName'),
    },
    username: {
      required,
      maxLength: maxLength(16),
    },
    email: {
      email,
      required,
      unique: unique('email', '/register/checkEmail'),
    },
    plainPassword: {
      required,
      alphaNum,
      minLength: minLength(4),
      maxLength: maxLength(16),
    },
    confirmPassword: {
      required,
      sameAsPlainPassword: sameAs('plainPassword'),
    },
  },
  methods: {
    validate: function (event) {
      if (!this.$v.$dirty || this.$v.$invalid) {
        event.preventDefault();
      }
    },
  },
}
</script>
