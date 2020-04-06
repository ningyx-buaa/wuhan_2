package act.ring.cncert.restful.controller.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import act.ring.cncert.restful.repository.auth.ForeignSearch;
import act.ring.cncert.restful.service.auth.SettingService;

@Controller
public class SettingController {

    @Autowired
    SettingService settingService;

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = "/api/setting/getForeignWords")
    public String getForeignWords(
        @RequestParam(value = "userid", defaultValue = "0") int userid) {
        ForeignSearch record = settingService.getForeignSearch(userid);
        if (record == null) {
            return "";
        } else {
            return record.getWords();
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/api/setting/setForeignWords")
    public boolean setForeignWords(
        @RequestParam(value = "userid", defaultValue = "0") int userid,
        @RequestParam(value = "words", defaultValue = "") String words) {
        return settingService.setForeignSearch(userid, words);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/api/setting/addForeignWords")
    public ForeignSearch addForeignWords(
        @RequestParam(value = "userid", defaultValue = "0") int userid,
        @RequestParam(value = "words", defaultValue = "") String words) {
        return settingService.addForeignSearch(userid, words);
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.POST, value = "/api/setting/deleteForeignWords")
    public ForeignSearch deleteForeignWords(
        @RequestParam(value = "userid", defaultValue = "0") int userid,
        @RequestParam(value = "words", defaultValue = "") String words) {
        return settingService.deleteForeignSearch(userid, words);
    }
}