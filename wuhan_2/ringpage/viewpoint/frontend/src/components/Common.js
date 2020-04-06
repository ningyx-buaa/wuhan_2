import Vue from 'vue'
import Const from 'components/Const'

/* 更新子页面title的自定义指令 */

Vue.directive('title', {
  inserted: function (el, binding) {
    document.title = el.dataset.title;
  }
});

/* custom global filters */

// 类型序号转换为大类（旧的类型）
Vue.filter('toLegacyType', function (typeid) {
  return Const.LegacyType[typeid];
});

Vue.prototype.$toLegacyType = function (typeid) {
  return Const.LegacyType[typeid];
};

// 类型序号转换为大类（28类对应的大类）
Vue.filter('toMajorType', function (typeid) {
  return Const.MajorType[typeid];
});

Vue.prototype.$toMajorType = function (typeid) {
  return Const.MajorType[typeid];
};

// 类型序号转化为小类（28类）
Vue.filter('toMinorType', function (typeid) {
  return Const.MinorType[typeid];
});

Vue.prototype.$toMinorType = function (typeid) {
  return Const.MinorType[typeid];
};

// 类型序号转化为大类（128类）
Vue.filter('toBigType', function (typeid) {
  return _.findKey(Const.ManyType, x => _.has(x, typeid));
});

Vue.prototype.$toBigType = function (typeid) {
  return _.findKey(Const.ManyType, x => _.has(x, typeid));
};

// 类型序号转化为小类（128类）
Vue.filter('toManyType', function (typeid) {
  return _.find(Const.ManyType, typeid)[typeid];
});

Vue.prototype.$toManyType = function (typeid) {
  return _.find(Const.ManyType, typeid)[typeid];
};

// 按照参数中的给定格式，格式化时间
Vue.filter('formatTime', function (time, fmt) {
  return new Date(time.replace('+0000', 'Z')).format(fmt);
});

// 将 0,1,2,3 转换为情绪字符串
Vue.filter('toEmotion', function (emotion) {
  return Const.Emotion[emotion];
});

// 解析事件的源数据来源
Vue.filter('eventFrom', function (event) {
  if (event.from) {
    return event.from;
  }
  let from = '';
  if (event.weiboIds) {
    from += '微博、';
  }
  if (event.wechatIds) {
    from += '微信、';
  }
  if (event.webNewsIds || event.terrensIds) {
    from += '新闻、';
  }
  if (event.tiebaIds) {
    from += '贴吧、';
  }
  return from.slice(0, -1);
});

/* register custom functions to Vue's prototype */

// register lodash library
Vue.prototype._ = _;

// register other useful functions.

/**
 * Time flag to milliseconds.
 *
 * {text: '1小时', value: 0},
 * {text: '5小时', value: 1},
 * {text: '1天', value: 2},
 * {text: '1周', value: 3},
 * {text: '1月', value: 4},
 * {text: '全部', value: 5},
 */
Vue.prototype.$getTime = function (flag) {
  if (flag === undefined) {
    return new Date().getTime(); // return current time in milliseconds.
  }
  switch (flag) {
    case 0: return 3600 * 1000;
    case 1: return 3600 * 1000 * 5;
    case 2: return 3600 * 1000 * 24;
    case 3: return 3600 * 1000 * 24 * 7;
    case 4: return 3600 * 1000 * 24 * 30;
    default: return new Date().getTime() - new Date(0).getTime();
  }
};

/**
 * Translate weibo id to weibo url (userId + mid).
 *
 * For more detail, please visit: http://www.crazyant.net/647.html
 */
Vue.prototype.$getWeiboURL = function (userId, id) {
  if (!(/^\d+$/).test(userId) || !(/^\d+$/).test(id)) {
    // not a valid url.
    return '';
  }
  let str62keys = [
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
    'u', 'v', 'w', 'x', 'y', 'z',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J',
    'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T',
    'U', 'V', 'W', 'X', 'Y', 'Z'
  ];

  let int10to62 = (int10) => {
    let s62 = '';
    let r = 0;
    while (int10 !== 0) {
      r = int10 % 62;
      s62 = str62keys[r] + s62;
      int10 = Math.floor(int10 / 62);
    }
    return s62;
  };

  if (!userId || !id) {
    return 'http://weibo.com/'; // I can't calculate the weibo URL if no user id or no mid available.
  }

  if (typeof (id) !== 'string') {
    id = '' + id;
  }
  let url = '';
  for (var i = id.length - 7; i > -7; i = i - 7) {    // 从最后往前以7字节为一组读取mid
    let offset1 = i < 0 ? 0 : i;
    let offset2 = i + 7;
    let num = id.substring(offset1, offset2);
    num = int10to62(num);
    url = num + url;
  }
  return 'http://weibo.com/' + userId + '/' + url;
};

/* DateTime utilities. */

Vue.prototype.$addDays = function (time, ndays) {
  return new Date(time.getTime() + ndays * 24 * 3600 * 1000);
};
