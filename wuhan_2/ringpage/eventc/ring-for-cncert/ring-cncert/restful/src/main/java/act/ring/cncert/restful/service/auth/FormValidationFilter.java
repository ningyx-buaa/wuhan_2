package act.ring.cncert.restful.service.auth;

import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Created by hetao on 2017/6/22.
 */
public class FormValidationFilter extends FormAuthenticationFilter {

    protected boolean onAccessDenied(ServletRequest request,
                                     ServletResponse response, Object mappedValue)
        throws Exception {
        // 从session获取正确的验证码
        HttpSession session = ((HttpServletRequest) request).getSession();

        /* Comment out this section due to we don't have validate code currently.

        //页面输入的验证码
        String randomcode = request.getParameter("randomcode");

        //从session中取出验证码
        String validateCode = (String) session.getAttribute("validateCode");

        if (!randomcode.equals(validateCode)) {
            // randomCodeError表示验证码错误
            request.setAttribute("shiroLoginFailure", "randomCodeError");
            //拒绝访问，不再校验账号和密码
            return true;
        }
        */
        return super.onAccessDenied(request, response, mappedValue);
    }

    /**
     * Get real remote IP from proxy server.
     */
    @Override
    protected String getHost(ServletRequest request) {
        HttpServletRequest req = (HttpServletRequest) request;
        System.out.println(req.getHeaderNames());

        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }
}

