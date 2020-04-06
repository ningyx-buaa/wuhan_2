package act.ring.cncert.restful.service.hot_topic;

import act.ring.cncert.data.bean.Opinion;
import org.apache.commons.lang3.time.DateUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import act.ring.cncert.data.bean.News;

import act.ring.cncert.data.es.Elasticsearch;
import act.ring.cncert.data.topic.ExtractOpinion;
import act.ring.cncert.restful.service.source.SourceService;

/**
 * Created by lijun on 2017/11/19.
 */
public class TopicService {

    private Elasticsearch<News> esNews;
    private ExtractOpinion ext;
    private String ES_INDEX = "opinions";

    public TopicService() {
        ext = new ExtractOpinion();
        esNews = new Elasticsearch<>(News.class, "news-");
    }

    public static void main(String[] args) {
        TopicService topicService = new TopicService();
        Date now = new Date();
        Date start = DateUtils.addDays(now, -20);
        Date end = DateUtils.addDays(start, 10);
        int datatag = 0;
//        topicService.test();
        //     topicService.getTopicTexts(start, now, "台湾问题", 32, true);
        topicService.extractOpinion(start, now, "台湾问题", 1000, 0);
        //  topicService.extractOpinion(start, now, "台湾问题", 1000);
//        ExtractOpinion extractOpinion = new ExtractOpinion();
//        for(int tagKey :extractOpinion.getOpinion_tag().keySet()) {
//            topicService.extractOpinion(start, end,extractOpinion.getOpinion_tag().get(tagKey) , 1000, tagKey);
//        }
        System.out.println(topicService.TopicDemo().size());
    }

    public List<Opinion> extractOpinion(Date from, Date to, String kws, int size, int datatag) {
        List<News> newsList = getTopicTexts(from, to, kws, size, true);
        List<Opinion> opinions = getOpinionFromNews(newsList, datatag);
//        System.out.println(opinions.size());
        for (int i = 0; i < opinions.size(); i++) {
            Elasticsearch elasticsearch = new Elasticsearch(Opinion.class, ES_INDEX);
            try {
                elasticsearch.updateOpinion(ES_INDEX, opinions.get(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(opinions.toString());
        }
        return opinions;
    }

    public List<News> getTopicTexts(Date from, Date to, String kws, int size, boolean includeText) {
        SourceService sourceService = new SourceService();
        List<News> newsList = sourceService.findNews(from, to, kws, size, includeText).v2();
        System.out.println(newsList.size());
        return newsList;
    }

    public List<Opinion> getOpinionFromNews(List<News> newsList, int datatag) {
        List<Opinion> opinionList = new ArrayList<>();
        for (News news : newsList) {
            if (news.getText() == null) {
                continue;
            }
            Opinion opinion = ext.ExtractOpinionBySource(news, datatag);
            if (opinion != null) {
                opinionList.add(opinion);
            }
        }
        return opinionList;
    }

    public List<Opinion> TopicDemo() {
        BufferedReader reader = null;
        String patn = "TaiwanExperts/news_data.txt";
        reader =
            new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(patn)));
        String temp = "";
        Date now = new Date();
        List<News> newsList = new ArrayList<>();
        try {
            int id = 1;
            while ((temp = reader.readLine()) != null) {
                News news = new News(String.valueOf(id));
                news.setText(temp);
                Date time = DateUtils.addDays(now, -id);
                time = DateUtils.addMilliseconds(time, -id);
                time = DateUtils.addHours(time, -id);
                time = DateUtils.addMinutes(time, -id * 10);
                news.setTime(time);
                newsList.add(news);
                id++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<Opinion> opinions = getOpinionFromNews(newsList, 0);//datatag =0 表示台湾问题
//        for (Opinion opinion : opinions){
//            System.out.println(opinion.getOpinion() + opinion.getNewsID());
//        }
        return opinions;
    }
}
