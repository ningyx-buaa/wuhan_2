package act.ring.cncert.restful.controller.taihai;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

import act.ring.cncert.data.bean.Taihai;
import act.ring.cncert.restful.service.Taihai.TaihaiService;


/**
 * Created by Machenike on 2017/12/13.
 */

@Controller
public class TaihaiController {

    private final static Logger LOG = LoggerFactory.getLogger(TaihaiController.class);
    private TaihaiService taihaiService = new TaihaiService();

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {"/api/taihai/hello"})
    public String hello() {
        return "hello!";
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/taihai/getTaihaiNews",
        "/api/cache1/taihai/getTaihaiNews"
    })

    public List<Taihai> getTaihaiNews(
        @RequestParam(value = "from", defaultValue = "0") long from,
        @RequestParam(value = "to", defaultValue = "0") long to,
        @RequestParam(value = "size", defaultValue = "100") int size) {
        Date fromDate, toDate;
        if (from != 0) {
            toDate = new Date(to * 1000);
            fromDate = new Date(from * 1000);
        } else {
            toDate = new Date();
            fromDate = DateUtils.addDays(toDate,-3);
        }
        return taihaiService.findTaihaiNews(fromDate, toDate, size);
    }
}
