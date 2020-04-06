package act.ring.cncert.restful.service.source;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.search.sort.SortBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import act.ring.cncert.data.bean.Element;
import act.ring.cncert.data.bean.Event;
import act.ring.cncert.data.bean.Foreign;
import act.ring.cncert.data.bean.News;
import act.ring.cncert.data.bean.Taihai;
import act.ring.cncert.data.es.Elasticsearch;
import act.ring.cncert.data.nlp.NLPExtraction;

@Service
public class SourceService {

    private NLPExtraction nlpExtractor;

    private Elasticsearch<News> esNews;
    private Elasticsearch<Element> esElements;
    private Elasticsearch<Taihai> esTaihaiNews;
    private Elasticsearch<Foreign> esForeigns;

    public SourceService() {
        nlpExtractor = new NLPExtraction();
        esNews = new Elasticsearch<>(News.class, "news-");
        esElements = new Elasticsearch<>(Element.class, "news-", "weibo_events-");
        esTaihaiNews = new Elasticsearch<>(Taihai.class, "news-");
        esForeigns = new Elasticsearch<>(Foreign.class, "foreigns-");
    }

    public static void main(String[] args) {
        SourceService sourceService = new SourceService();
        Date now = new Date();
        Date ago = DateUtils.addDays(now, -1);

        System.out.println(System.currentTimeMillis());
        // System.out.println(sourceService.findForeignDatasWith(ago, now, "", 32, true, 55));
        System.out.println(sourceService.fetchValueFromIndex("weibo_events-2018.05.06", "201805060840-3"));
        System.out.println(System.currentTimeMillis());

    }

    public Tuple<Long, List<News>> findNews(Date from, Date to, String kws, int size, boolean includeText) {
        return esNews
            .findItems(from, to, kws, size, 0, includeText, Elasticsearch.mkSortByScore(false));
    }

    public Tuple<Long, List<Taihai>> findTaihaiNews(Date from, Date to, String kws, int size,
                                       boolean includeText) {
        return esTaihaiNews
            .findItems(from, to, kws, size, 0, includeText, Elasticsearch.mkSortByScore(false));
    }

    public Tuple<Long, List<Element>> findDatas(Date from, Date to, String kws, String kws_kinds, int size, int offset, boolean includeText,
                                   String language, String location, String types0, String types1, String types2,
                                   String sort) {
        SortBuilder sorter;
        if (sort == null || sort.isEmpty()) {
            sorter = Elasticsearch.mkSortByScore(false);
        } else {
            sorter = Elasticsearch.mkSortByField(sort, false);
        }
        Tuple<Long, List<Element>> result = esElements.findItems(from, to, kws, kws_kinds, size, offset, language, location,
                                                         types0, types1, types2, includeText, sorter);
        return result;
    }

    public Tuple<Long, List<Element>> findForeignDatasWith(Date from, Date to, String kws, int size,
                                              boolean includeText,
                                              int secu) {
        long resultCnt = 0;
        List<Element> result = new ArrayList<>();

        Tuple<Long, List<Foreign>> r1 = esForeigns.findItemsWith(from, to, kws, null, size, 0, includeText,
                                               Elasticsearch.mkSortByScore(false), secu);
        resultCnt += r1.v1();
        result.addAll(r1.v2());

        return new Tuple<>(resultCnt, result);
    }

    public List<News> fetchSimNewsById(String id) {
        News item = esNews.fetchItem(id);
        System.out.println(item.getSimids());
        return esNews.fetchItems(item.getSimids());
    }

    public List<Foreign> fetchSimForeignsById(String id) {
        Foreign item = esForeigns.fetchItem(id);
        System.out.println(item.getSimids());
        return esForeigns.fetchItems(item.getSimids());
    }

    public Element fetchValueFromIndex(String index, String id) {
        return esElements.fetchItem(id);
    }

    public SortedMap<String, Long> computeTrendDaily(Date time, String text) {
        Date from = DateUtils.addDays(time, -4), to = DateUtils.addDays(time, 4);
        String words = nlpExtractor.getKeyWords(text);
        if (words.isEmpty()) {
            words = text;
        }
        String kws = StringUtils.join(words.split("#"), " ");
        return esElements.dateHistDay(from, to, kws);
    }

    public SortedMap<String, Long> computeTrendHourly(Date time, String text) {
        Date from = DateUtils.addHours(time, -24), to = DateUtils.addHours(time, 12);
        String words = nlpExtractor.getKeyWords(text);
        if (words.isEmpty()) {
            words = text;
        }
        String kws = StringUtils.join(words.split("#"), " ");
        SortedMap<String, Long> r = esElements.dateHistHour(from, to, kws);
        // accumulate
        List<String> dates = new ArrayList<>();
        List<Long> values = new ArrayList<>();
        dates.addAll(r.keySet());
        values.addAll(r.values());
        SortedMap<String, Long> result = new TreeMap<>();
        for (int i = 12; i < dates.size(); ++i) {
            Long s = values.subList(i - 12, i).stream().mapToLong(x -> x.longValue()).sum();
            result.put(dates.get(i), s);
        }
        return result;
    }

    public boolean setFieldValueAs(String esIndex, String id, String field, Object value)
            throws IOException {
        return esNews.updateItem(esIndex, id, field, value);
    }
}
