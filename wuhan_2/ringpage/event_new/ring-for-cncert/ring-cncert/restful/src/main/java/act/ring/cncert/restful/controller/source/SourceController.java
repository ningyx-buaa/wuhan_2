package act.ring.cncert.restful.controller.source;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.time.DateUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.elasticsearch.common.collect.Tuple;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TimeZone;

import javax.servlet.http.HttpServletRequest;

import act.ring.cncert.data.bean.Element;
import act.ring.cncert.data.bean.Foreign;
import act.ring.cncert.data.bean.News;
import act.ring.cncert.restful.service.source.SourceService;

@Controller
public class SourceController {

    private SourceService sourceService = new SourceService();

    private ThreadLocal<SimpleDateFormat> localDate = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return dateFormat;
        }
    };

    private ThreadLocal<SimpleDateFormat> localFullDate = new ThreadLocal<SimpleDateFormat>() {
        @Override
        public SimpleDateFormat initialValue() {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
            dateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
            return dateFormat;
        }
    };

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {"/api/event/hello"})
    public String hello() {
        return "hello!";
    }

    @RequiresRoles("cncert")
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {"/api/event/helloWithAuth"})
    public String helloWithAuth() {
        return "hello, cncert!";
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/source/findNews",
        "/api/cache1/source/findNews"
    })
    public List<News> findNews(
        @RequestParam(value = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
        @RequestParam(value = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
        @RequestParam(value = "kws", defaultValue = "") String kws,
        @RequestParam(value = "size", defaultValue = "64") int size,
        @RequestParam(value = "include_text", defaultValue = "false") boolean includeText) {
        return sourceService.findNews(from, DateUtils.addDays(to, 1), kws, size, includeText).v2(); // TODO pagination
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/source/findDatas",
        "/api/cncert/source/findDatas",
        "/api/cache1/source/findDatas"
    })
    public Page<Element> findDatas(
        @RequestParam(value = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
        @RequestParam(value = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
        @RequestParam(value = "kws", defaultValue = "") String kws,
        @RequestParam(value = "kws_kinds", defaultValue = "") String kws_kinds, // 按关键词设置的选题
        @RequestParam(value = "pageno", defaultValue = "1") int pageno,
        @RequestParam(value = "size", defaultValue = "64") int size,
        @RequestParam(value = "include_text", defaultValue = "false") boolean includeText,
        @RequestParam(value = "language", defaultValue = "全部") String language,
        @RequestParam(value = "location", defaultValue = "全部") String location,
        @RequestParam(value = "types0", defaultValue = "") String types0,
        @RequestParam(value = "types1", defaultValue = "") String types1,
        @RequestParam(value = "types2", defaultValue = "") String types2,
        @RequestParam(value = "sort", defaultValue = "") String sort,
        HttpMethod method, HttpServletRequest request) throws Exception {

        Tuple<Long, List<Element>> part1 = sourceService.findDatas(from, DateUtils.addDays(to, 1), kws, kws_kinds, size, (pageno - 1) * size, includeText,
                                            language, location, types0, types1, types2, sort);

        return new PageImpl<>(part1.v2(), new PageRequest(pageno, size, new Sort(
            Sort.Direction.DESC, StringUtils.hasText(sort) ? sort : "auto")), part1.v1());

        /* Now we don't need the follback, we query from es table directly.
           We keep this piece of code for backwards compability reason.

        List<JsonNode> part2 = new LinkedList<>();

        String q = request.getQueryString()
            .replaceAll("word=", "kws=")
            .replaceAll("types0=", "type=")
            .replaceAll("types1=", "s_type=")
            .replaceAll("types2=", "s_type2=")
            .replaceAll("location=", "loc=")
            .replaceAll("size=", "num=")
            + "&degrees=true";
        if (language.equals("外文")) {
            q += "&foreign=true";
        }
        URI uri = new URI("http", null, "10.1.1.35", 8082, "/api/topicWithTimeRange", q, null);
        ResponseEntity<ObjectNode[]> resultEventsEntity =
            restTemplate.exchange(uri, method, new HttpEntity<String>(""), ObjectNode[].class);
        for (ObjectNode event: resultEventsEntity.getBody()) {
            event.put("datatag", "event");
            event.put("type0", event.get("type"));
            event.put("type1", event.get("eventType"));
            event.put("type2", event.get("eventType2"));
            event.put("title", event.get("description"));
            event.put("timestamp", event.get("time"));
            part2.add(event);
        }

        List<Object> result = new ArrayList<>();
        result.addAll(part1);
        result.addAll(part2);

         */
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/source/findDatasWith",
        "/api/cache1/source/findDatasWith"
    })
    public List<Element> findDatasWith(
        @RequestParam(value = "from") @DateTimeFormat(pattern = "yyyy-MM-dd") Date from,
        @RequestParam(value = "to") @DateTimeFormat(pattern = "yyyy-MM-dd") Date to,
        @RequestParam(value = "kws", defaultValue = "") String kws,
        @RequestParam(value = "size", defaultValue = "64") int size,
        @RequestParam(value = "include_text", defaultValue = "false") boolean includeText,
        @RequestParam(value = "secu", defaultValue = "80") int secu) {
        return sourceService
            .findForeignDatasWith(from, DateUtils.addDays(to, 1), kws, size, includeText, secu).v2(); // TODO pagination
    }

    /**
     * @param id 多个id用逗号分隔
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/source/fetchSimNewsById",
        "/api/cache3/source/fetchSimNewsById"
    })
    public List<News> fetchSimNewsById(
        @RequestParam(value = "id", defaultValue = "") String id) {
        return sourceService.fetchSimNewsById(id);
    }

    /**
     * @param id 多个id用逗号分隔
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/source/fetchSimForeignsById",
        "/api/cache3/source/fetchSimForeignsById"
    })
    public List<Foreign> fetchSimForeignsById(
        @RequestParam(value = "id", defaultValue = "") String id) {
        return sourceService.fetchSimForeignsById(id);
    }

    /**
     * 趋势分析
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/source/fetchTrendById",
        "/api/cache3/source/fetchTrendById"
    })
    public SortedMap<String, Long> fetchTrendById(
        @RequestParam(value = "id", defaultValue = "") String id,
        @RequestParam(value = "text", defaultValue = "") String text,
        @RequestParam(value = "kind", defaultValue = "daily") String kind,
        @RequestParam(value = "time") @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ") Date time) {
        if (kind.equals("daily")) {
            return sourceService.computeTrendDaily(time, text);
        } else {
            return sourceService.computeTrendHourly(time, text);
        }
    }

    /**
     * 按照Id查询
     */
    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/source/fetchValueFromIndex",
        "/api/cache3/source/fetchValueFromIndex"
    })
    public Element fetchValueFromIndex(
        @RequestParam(value = "index") String index,
        @RequestParam(value = "id") String id) {
        assert StringUtils.hasText(index);
        assert StringUtils.hasText(id);
        return sourceService.fetchValueFromIndex(index, id);
    }

    @ResponseBody
    @RequestMapping(method =  RequestMethod.POST, value = {
        "/api/cncert/source/setFieldValueAs"
    })
    public boolean setFieldValueAs(
        @RequestParam(value = "esIndex") String esIndex,
        @RequestParam(value = "id") String id,
        @RequestParam(value = "field") String field,
        @RequestParam(value = "value") String value,
        @RequestParam(value = "type", defaultValue = "int") String type) {
        try {
            if (type.equals("int")) {
                return sourceService.setFieldValueAs(esIndex, id, field, Integer.valueOf(value));
            } else {
                return sourceService.setFieldValueAs(esIndex, id, field, value);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/getDataWithSecu",
        "/api/cncert/v2/getDataWithSecu"})
    public String getDataWithSecu(
        @RequestParam(value = "source", defaultValue = "weibo") String source,
        @RequestParam(value = "from" ) String from,
        @RequestParam(value = "to") String to,
        @RequestParam(value = "secu", defaultValue = "80") int secu,
        @RequestParam(value = "size", defaultValue = "64") int size,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "sorting", defaultValue = "time") String sorting,
        HttpMethod method, HttpServletRequest request)
        throws URISyntaxException, ParseException {

        // forward the real request to our ring server
        // see @EventController@ in ring-demo.
        RestTemplate restTemplate = new RestTemplate();

        if (source.equals("foreign")) {

            Date fromDate = localDate.get().parse(from);
            Date toDate = localDate.get().parse(to);

            // 对于外文，结果分两部分：外文新闻+twitter等

            // twitter
            List<Element> twitters = sourceService.findForeignDatasWith(fromDate, toDate, "", size, false, secu).v2(); // TODO pagination
            List<Element> twitterResult = new ArrayList<>();
            for (Element e: twitters) {
                if (e.getSecu() >= secu) { // post filter
                    twitterResult.add(e);
                }
            }

            // news
            String q = request.getQueryString().replaceAll("source=foreign", "source=webnews") + "&foreign=true";
            URI uri = new URI("http", null, "10.1.1.35", 8082, "/api/getDataWithSecu", q, null);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Referer", "http://ring.act.buaa.edu.cn/cc/");
            ResponseEntity<JsonNode[]> resultNewsEntity =
                restTemplate.exchange(uri, method, new HttpEntity<String>("", headers), JsonNode[].class);

            // merge result
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode result = objectMapper.createArrayNode();
            for (Element e: twitters) {
                result.add(objectMapper.valueToTree(e));
            }
            for (JsonNode news: resultNewsEntity.getBody()) {
                result.add(news);
            }

            // return result
            return result.toString();
        }
        else {
            // 对于微博、正常新闻、微信、贴吧，直接向bd35请求结果
            URI uri = new URI("http", null, "10.1.1.35", 8082, "/api/getDataWithSecu", request.getQueryString(), null);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Referer", "http://ring.act.buaa.edu.cn/cc/");
            return restTemplate.exchange(uri, method, new HttpEntity<String>("", headers), String.class).getBody();
        }
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/event/findEvent",
        "/api/cache1/event/findEvent"
    })
    public String findEvent(
        @RequestParam(value = "kws", defaultValue = "") String kws,
        @RequestParam(value = "page", defaultValue = "1") int pageno,
        @RequestParam(value = "size", defaultValue = "64") int size,
        @RequestParam(value = "sort", defaultValue = "time") String sort,
        HttpMethod method, HttpServletRequest request) throws Exception {

        String q = request.getQueryString()
            .replaceAll("size=", "page.size=")
            + "&wd=" + kws;
        URI uri = new URI("http", null, "10.1.1.35", 8082, "/api/esearch", q, null);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Referer", "http://ring.act.buaa.edu.cn/cc/");
        ResponseEntity<String> resultEventsEntity =
            restTemplate.exchange(uri, method, new HttpEntity<>("", headers), String.class);
        return resultEventsEntity.getBody();
    }

    @ResponseBody
    @RequestMapping(method = RequestMethod.GET, value = {
        "/api/getDataWithSecuRestful",
        "/api/cncert/v2/getDataWithSecuRestful"})
    public String getDataWithSecuRestful(
        @RequestParam(value = "source", defaultValue = "weibo") String source,
        @RequestParam(value = "from" ) String from,
        @RequestParam(value = "to") String to,
        @RequestParam(value = "secu", defaultValue = "80") int secu,
        @RequestParam(value = "size", defaultValue = "64") int size,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        HttpMethod method, HttpServletRequest request)
            throws URISyntaxException, ParseException {

        // forward the real request to our ring server
        // see @EventController@ in ring-demo.
        RestTemplate restTemplate = new RestTemplate();

        if (source.equals("foreign")) {

            Date fromDate = localFullDate.get().parse(from);
            Date toDate = localFullDate.get().parse(to);

            System.out.println(fromDate);
            System.out.println(toDate);

            // 对于外文，结果分两部分：外文新闻+twitter等

            System.out.println("time0: " + System.currentTimeMillis());
            // twitter
            List<Element> twitters = sourceService.findForeignDatasWith(fromDate, toDate, "", size, false, secu).v2(); // TODO pagination
            System.out.println(twitters);

            System.out.println("time1: " + System.currentTimeMillis());
            // news
            String q = request.getQueryString().replaceAll("source=foreign", "source=webnews") + "&foreign=true";
            URI uri = new URI("http", null, "10.1.1.35", 8082, "/api/getDataWithSecuRestful", q, null);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Referer", "http://ring.act.buaa.edu.cn/cc/");
            ResponseEntity<JsonNode[]> resultNewsEntity =
                restTemplate.exchange(uri, method, new HttpEntity<String>("", headers), JsonNode[].class);

            System.out.println("time3: " + System.currentTimeMillis());
            // merge result
            ObjectMapper objectMapper = new ObjectMapper();
            ArrayNode result = objectMapper.createArrayNode();
            for (Element e: twitters) {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("title", e.getTitle())
                    .put("url", e.getUrl())
                    .put("time", e.getTime().getTime())
                    .put("secu", e.getSecu())
                    .put("source", e.getUser());
                result.add(node);
            }
            for (JsonNode news: resultNewsEntity.getBody()) {
                result.add(news);
            }

            // return result
            return result.toString();
        }
        else {
            // 对于微博、正常新闻、微信、贴吧，直接向bd35请求结果
            URI uri = new URI("http", null, "10.1.1.35", 8082, "/api/getDataWithSecuRestful", request.getQueryString(), null);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Referer", "http://ring.act.buaa.edu.cn/cc/");
            return restTemplate.exchange(uri, method, new HttpEntity<String>("", headers), String.class).getBody();
        }
    }
}