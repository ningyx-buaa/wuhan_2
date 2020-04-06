package act.ring.cncert.restful.controller.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

import act.ring.cncert.restful.repository.auth.User;
import act.ring.cncert.restful.service.auth.AccountService;


/**
 * 用户注册的Controller.
 *
 * @author calvin
 */
@Controller
@RequestMapping(value = "/api/auth/register")
public class RegisterController {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterController.class);

    @Autowired
    private AccountService accountService;

    @RequestMapping(method = RequestMethod.GET)
    public String registerForm() {
        return "redirect:/account/#/register";
    }

    @RequestMapping(method = RequestMethod.POST)
    public String register(@Valid User user, RedirectAttributes redirectAttributes) {
        if (accountService.findUserByLoginName(user.getLoginName()) != null) {
            return "redirect:/account/#/register";
        }
        accountService.registerUser(user);
        LOG.info("user {} register with nikename {} and email {}", user.getLoginName(),
                 user.getName(), user.getEmail());
        redirectAttributes.addFlashAttribute("username", user.getLoginName());
        return "redirect:/account/"; // 登陆
    }

    /**
     * Ajax请求校验loginName是否唯一。
     */
    @ResponseBody
    @RequestMapping(value = "checkLoginName", method = RequestMethod.POST)
    public String checkLoginName(@RequestParam("loginName") String loginName) {
        if (accountService.findUserByLoginName(loginName) == null) {
            return "true";
        } else {
            return "false";
        }
    }

    /**
     * Ajax请求校验邮箱是否已注册。
     */
    @ResponseBody
    @RequestMapping(value = "checkEmail", method = RequestMethod.POST)
    public String checkEmail(@RequestParam("email") String email) {
        if (accountService.findUserByEmail(email) == null) {
            return "true";
        } else {
            return "false";
        }
    }
}
