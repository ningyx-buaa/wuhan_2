package act.ring.cncert.restful.controller.auth;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import act.ring.cncert.restful.repository.auth.User;
import act.ring.cncert.restful.service.auth.AccountService;
import act.ring.cncert.restful.service.auth.ShiroDbRealm.ShiroUser;

/**
 * Created by HE, Tao on 2017/6/23.
 */
@Controller
public class AccountController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @ResponseBody
    @RequestMapping(value = "/api/auth/isLogined", method = RequestMethod.POST)
    public String isLogined(@RequestParam(value = "path", required = true) String path) {
        Subject subject = SecurityUtils.getSubject();
        JSONObject result = new JSONObject();
        ShiroUser shiroUser = (ShiroUser) subject.getPrincipal();
        if (shiroUser != null) {
            User user = accountService.findUserByLoginName(shiroUser.loginName);
            Session session = subject.getSession();
            result.put("logined", true);
            result.put("loginName", user.getLoginName());
            result.put("userName", user.getName());
            result.put("roles", user.getRoles());
            LOG.info("user {} [with roles: {}] visit {}", user.getLoginName(), user.getRoles(),
                     path);
        } else {
            result.put("logined", false);
        }
        return result.toString();
    }
}
