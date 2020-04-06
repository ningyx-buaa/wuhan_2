package act.ring.cncert.restful.service.auth;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.util.WebUtils;

import javax.servlet.ServletRequest;

/**
 * Created by hetao on 2017/6/22.
 */
public class WebSessionManager extends DefaultWebSessionManager {

    /**
     * Stores the Session's ID, usually as a Cookie, to associate with future requests.
     *
     * @param session the session that was just{@link #createSession created}.
     */
    @Override
    protected void onStart(Session session, SessionContext context) {
        super.onStart(session, context);
        ServletRequest request = WebUtils.getRequest(context);
        request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                             ShiroHttpServletRequest.COOKIE_SESSION_ID_SOURCE);
    }
}
