package act.ring.cncert.restful.controller.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * LoginController负责打开登录页面(GET请求)和登录出错页面(POST请求)，
 *
 * 真正登录的POST请求由Filter完成,
 *
 * @author calvin
 */
@Controller
@RequestMapping(value = "/api/auth/login")
public class LoginController {

    private static final Logger LOG = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping(method = RequestMethod.GET)
    public String login() {
        return "redirect:/account/";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String fail() {
        System.out.println("failed to login");
        return "redirect:/account/";
    }
}
