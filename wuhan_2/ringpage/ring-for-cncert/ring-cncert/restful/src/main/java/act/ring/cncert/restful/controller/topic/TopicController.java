package act.ring.cncert.restful.controller.topic;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import act.ring.cncert.data.bean.Opinion;
import act.ring.cncert.data.es.Elasticsearch;
import act.ring.cncert.restful.service.hot_topic.TopicService;

/**
 * Created by lijun on 2017/11/20.
 */
@Controller
public class TopicController {

    private final static Logger LOG = LoggerFactory.getLogger(TopicController.class);
    private static Elasticsearch elasticsearch = new Elasticsearch(Opinion.class, "opinions");
    private TopicService topicService = new TopicService();

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {"/api/topic/hello"})
    public String hello() {
        return "hello!";
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/topic/getOpinion",
        "/api/cache1/topic/getOpinion"
    })

    public Object getOpinion(
        @RequestParam(value = "datatag", defaultValue = "") String dt){
        List<Map<String, Object>> res_list = new ArrayList<>();
        res_list = elasticsearch.getInfo(dt, 100);
        return res_list;
    }
}
