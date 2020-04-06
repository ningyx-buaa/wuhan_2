package act.ring.cncert.data.nlp.cncnert;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.util.Date;

import act.ring.cncert.data.bean.News;
import act.ring.cncert.data.es.Elasticsearch;
import act.ring.cncert.data.nlp.NLPExtraction;

/**
 * Created by lijun on 2017/11/29.
 */
public class HotDegree {

    private static final String ES_INDEX_PREFIX = "news-";
    private static int LIFE_TIME = 24;
    private NLPExtraction nlpExtractor = new NLPExtraction();
    private Elasticsearch<News> elasticsearch;

    public HotDegree() {
        elasticsearch = new Elasticsearch<>(News.class, ES_INDEX_PREFIX);
    }

    public static void main(String[] args) {
        HotDegree hotDegree = new HotDegree();
        String opinion_string = "日本首相安倍晋三在APEC会中建议宋楚瑜转告蔡英文，可以举行“习蔡会”来解决两岸问题";
        System.out.println(hotDegree.getOpinionHot(opinion_string));
    }

    public Long getNewsHot(String kws, Date from, Date to, boolean includeText) {
        Long
            hot =
            elasticsearch
                .findItemCounts(from, to, kws, includeText, Elasticsearch.mkSortByScore(false));
        return hot;
    }

    public Long getOpinionHot(String opinion_string) {
        String corewords = nlpExtractor.getKeyWords(opinion_string);
        String kws = StringUtils.join(corewords.split("#"), " ");
        Date to = new Date();
        Date from = DateUtils.addMinutes(to, -60 * LIFE_TIME * 3);
        boolean includeText = true;
        return getNewsHot(kws, from, to, includeText);
    }
}
