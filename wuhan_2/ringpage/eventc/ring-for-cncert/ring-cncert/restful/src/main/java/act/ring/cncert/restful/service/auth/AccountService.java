package act.ring.cncert.restful.service.auth;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import act.ring.cncert.restful.repository.auth.User;
import act.ring.cncert.restful.repository.auth.UserDao;
import act.ring.cncert.restful.service.ServiceException;
import act.ring.cncert.restful.service.auth.ShiroDbRealm.ShiroUser;

/**
 * 用户管理类.
 *
 * @author calvin
 */
// Spring Service Bean的标识.
@Component
@Transactional
@Service
public class AccountService {

    public static final String HASH_ALGORITHM = "SHA-1";
    public static final int HASH_INTERATIONS = 1024;
    private static final int SALT_SIZE = 8;
    private static Logger logger = LoggerFactory.getLogger(AccountService.class);
    @Autowired
    private UserDao userDao;

    public void deleteUser(Long id) {
        if (isSupervisor(id)) {
            logger.warn("操作员{}尝试删除超级管理员用户", getCurrentUserName());
            throw new ServiceException("不能删除超级管理员用户");
        }
        userDao.delete(id);
    }

    /**
     * 设定安全的密码，生成随机的salt并经过1024次 sha-1 hash
     */
    private void entryptPassword(User user) {
        byte[] salt = Utils.generateSalt(SALT_SIZE);
        user.setSalt(Utils.encodeHex(salt));

        byte[]
            hashPassword =
            Utils.sha1(user.getPlainPassword().getBytes(), salt, HASH_INTERATIONS);
        user.setPassword(Utils.encodeHex(hashPassword));
    }

    public User findUserByLoginName(String loginName) {
        return userDao.findFirstByLoginName(loginName);
    }

    public User findUserByEmail(String email) {
        return userDao.findFirstByEmail(email);
    }

    public List<User> getAllUser() {
        return (List<User>) userDao.findAll();
    }

    /**
     * 取出Shiro中的当前用户LoginName.
     */
    private String getCurrentUserName() {
        ShiroUser user = (ShiroUser) SecurityUtils.getSubject().getPrincipal();
        return user.loginName;
    }

    public User getUser(Long id) {
        return userDao.findOne(id);
    }

    /**
     * 判断是否超级管理员.
     */
    private boolean isSupervisor(Long id) {
        return id == 1;
    }

    public void registerUser(User user) {
        entryptPassword(user);
        user.setRoles("user");
        user.setRegisterDate(new Date());
        userDao.save(user);
    }

    public void updateUser(User user) {
        if (StringUtils.isNotBlank(user.getPlainPassword())) {
            entryptPassword(user);
        }
        userDao.save(user);
    }
}