package act.ring.cncert.data.es;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.update.UpdateRequestBuilder;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.index.engine.VersionConflictEngineException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import act.ring.cncert.data.bean.Element;
import act.ring.cncert.data.bean.News;
import act.ring.cncert.data.bean.Opinion;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.existsQuery;
import static org.elasticsearch.index.query.QueryBuilders.idsQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.rangeQuery;
import static org.elasticsearch.index.query.QueryBuilders.simpleQueryStringQuery;
import static org.elasticsearch.index.query.QueryBuilders.termQuery;
import static org.elasticsearch.index.query.QueryBuilders.termsQuery;
import static org.elasticsearch.search.aggregations.AggregationBuilders.dateHistogram;

public class Elasticsearch<T extends Element> {

    public static final ThreadLocal<SimpleDateFormat>
        shortDateFormat =
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy.MM.dd");
            }
        };
    public static final ThreadLocal<SimpleDateFormat>
        utcDateFormat =
        new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ");
            }
        };
    private final static Logger LOG = LoggerFactory.getLogger(Elasticsearch.class);
    private static final int ES_PORT = 9300;
    private static final String ES_HOST = "10.1.1.11,10.1.1.12,10.1.1.13,10.1.1.14,10.1.1.15,10.1.1.16,10.1.1.17,10.1.1.18,10.1.1.19";
    // private static final String ES_HOST = "10.1.1.11";
    private static TransportClient client;

    static {
        Settings settings = Settings.settingsBuilder()
            .put("client.transport.sniff", true)
            .put("cluster.name", "elasticsearch").build();
        client = TransportClient.builder().settings(settings).build();
        for (String host : ES_HOST.split(",")) {
            LOG.info("connect to es node {}:{} ...", host, ES_PORT);
            try {
                client.addTransportAddress(
                    new InetSocketTransportAddress(InetAddress.getByName(host), ES_PORT));
            } catch (UnknownHostException e) {
                LOG.error(e.getMessage());
            }
        }
    }

    private Class<T> classType;
    private String[] indexPrefixes;

    public Elasticsearch(Class<T> classType, String... indexPrefixes) {
        this.classType = classType;
        this.indexPrefixes = indexPrefixes;
    }

    public static SortBuilder mkSortByScore(boolean asc) {
        if (asc) {
            return SortBuilders.scoreSort().order(SortOrder.ASC);
        } else {
            return SortBuilders.scoreSort().order(SortOrder.DESC);
        }
    }

    public static SortBuilder mkSortByField(String field, boolean asc) {
        if (asc) {
            return SortBuilders.fieldSort(field).order(SortOrder.ASC);
        } else {
            return SortBuilders.fieldSort(field).order(SortOrder.DESC);
        }
    }

    /**
     * @return isCreated
     */

    public static List<Map<String, Object>> getInfo(String dt, int size) {
        //  public static List<Map<String, Object>>  getInfo(Date from,Date to,int size){
        String tableName = "opinions";
        // QueryBuilder dateFilter = QueryBuilders.rangeQuery("time").from(from).to(to);//time range
        // QueryBuilder filter = boolQuery().must(dateFilter);
        //  QueryBuilder termFilter = QueryBuilders.termQuery("holder",hd);
        //   QueryBuilder filter = boolQuery().must(termFilter);
        QueryBuilder termFilter = QueryBuilders.termQuery("datatag", dt);
        QueryBuilder filter = boolQuery().must(termFilter);
        QueryBuilder qb = QueryBuilders.matchAllQuery();
        QueryBuilder query = boolQuery().must(qb).must(filter);
        SearchRequestBuilder srb = client.prepareSearch(tableName)
            .setQuery(query)
            .setSize(size)
            .addSort(SortBuilders.fieldSort("time").order(SortOrder.DESC));
        SearchResponse response = srb.execute().actionGet();
        List<Map<String, Object>> res_list = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            res_list.add(hit.getSource());
        }
        return res_list;
    }

    private String[] genIndexNames(Date from, Date to) {
        List<String> indices = new ArrayList<>();
        Date today = new Date();
        for (; from.compareTo(to) <= 0 && from.compareTo(today) <= 0; from = DateUtils.addDays(from, 1)) {
            for (String prefix: indexPrefixes) {
                indices.add(prefix + shortDateFormat.get().format(from));
            }
        }
        return indices.toArray(new String[0]);
    }

    private T newInstance(Class<T> classType) {
        try {
            return classType.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            return null;
        }
    }

    public T fetchItem(String id) {
        List<T> elements = fetchItems(Arrays.asList(id));
        if (elements.isEmpty()) {
            return null;
        } else {
            return elements.get(0);
        }
    }

    public T fetchItem(String index, String id) {
        List<T> elements = fetchItems(new String[]{index}, Arrays.asList(id));
        if (elements.isEmpty()) {
            return null;
        } else {
            return elements.get(0);
        }
    }

    public List<T> fetchItems(Collection<String> ids) {
        Date now = new Date();
        String[] indices = genIndexNames(DateUtils.addDays(now, -7), now);
        return fetchItems(indices, ids);
    }

    public List<T> fetchItems(String[] indices, Collection<String> ids) {
        SearchResponse response = client.prepareSearch(indices)
            .setIndicesOptions(IndicesOptions.fromOptions(true, true, true, false))
            .setTypes("message")
            .setQuery(idsQuery().ids(ids))
            .execute()
            .actionGet();
        return parseSearchResponse(response).v2();
    }

    public boolean deleteItem(String index, String id) {
        boolean found = client.prepareDelete(index, "message", id)
            .get()
            .isFound();
        return found;
    }

    /**
     * @return isCreated
     */
    public boolean updateOpinion(String index, Opinion opinion) throws IOException {
        LOG.info("elasticsearch update: id {}, {} => {}", opinion.getId(), opinion.getOpinion(),
                 opinion.getTime());
        XContentBuilder updateValue = jsonBuilder()
            .startObject()
            .field("user", opinion.getUser())//source
            .field("userid", opinion.getUserid())//source_id
            .field("title", opinion.getTitle())
            .field("hot", opinion.getHot())
            .field("emotion", opinion.getEmotion())
            .field("url", opinion.getUrl())
            .field("sensitive", opinion.getSensitive())
            .field("importance",opinion.getImportance())
            .field("recommend",opinion.getRecommend())
            .field("risk",opinion.getRisk())
            .field("opinion", opinion.getOpinion())
            .field("holder", opinion.getHolder())
            .field("time", opinion.getTime())
            .field("datatag", opinion.getDatatag())
            .field("@timestamp", new Date())
            .endObject();

        try {
            UpdateResponse response = client.prepareUpdate(index, "message", opinion.getId())
                .setDoc(updateValue)
                .setDocAsUpsert(true)
                .execute()
                .actionGet();
            return response.isCreated();
        } catch (VersionConflictEngineException e) {
            LOG.error("updateOpinion: ElasticSearch version conflict exception {} ",
                      e.getMessage());
            return false;
        }
    }

    /**
     * @return isCreated
     */
    public boolean updateItem(String index, String id, String field, Object value) throws
                                                                                   IOException {
        LOG.info("elasticsearch update: id {}, {} => {}", id, field, value);
        XContentBuilder updateValue = jsonBuilder()
            .startObject()
            .field(field, value)
            .field("@timestamp", new Date())
            .endObject();
        try {
            UpdateResponse updateResponse = client.prepareUpdate(index, "message", id)
                .setDoc(updateValue)
                .get();
            return updateResponse.isCreated();
        } catch (VersionConflictEngineException e) {
            LOG.error("updateItem: ElasticSearch version conflict exception {} ",
                      e.getMessage());
            return false;
        }
    }

    /**
     * TODO refactor the @timestamp change during update new date.
     *
     * @return isCreated
     */
    public <E> boolean updateItemWithListValue(String index, String id, String field, Date timestamp, List<E> value)
        throws
        IOException {
        LOG.info("elasticsearch update: id {}, {} => {}", id, field, value);
        XContentBuilder updateValue = jsonBuilder()
            .startObject()
            .field(field, StringUtils.join(value, ","))
            .field("@timestamp", new Date())
            .endObject();
        try {
            UpdateResponse updateResponse = client.prepareUpdate(index, "message", id)
                .setDoc(updateValue)
                .get();
            return updateResponse.isCreated();
        } catch (VersionConflictEngineException e) {
            LOG.error("updateItemWithListValue: ElasticSearch version conflict exception {} ",
                      e.getMessage());
            return false;
        }
    }

    private Tuple<Long, List<T>> parseSearchResponse(SearchResponse response) {
        List<T> elements = new ArrayList<>();

        try {
            Method fromMap = classType.getMethod("fromMap", Map.class);
            for (SearchHit hit : response.getHits().getHits()) {
                T item = (T) fromMap.invoke(null, hit.getSource());
                item.setId(hit.getId());
                item.setEsIndex(hit.getIndex());
                item.setScore(hit.getScore());
                elements.add(item);
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }

        return new Tuple<>(response.getHits().getTotalHits(), elements);
    }

    /**
     *
     */
    public Long findItemCounts(Date from, Date to, String kws, boolean includeText,
                               SortBuilder sort) {
        SearchResponse
            response =
            genericFindItems(from, to, kws, null, 0, 0, "全部", "全部", "", "", "", includeText, sort);
        return response.getHits().getTotalHits();
    }

    public Tuple<Long, List<T>> findItems(Date from, Date to, int size, int offset, SortBuilder sort) {
        SearchResponse
            response =
            genericFindItems(from, to, null, null, size, offset, "全部", "全部", "", "", "", false, sort);
        return parseSearchResponse(response);
    }

    public Tuple<Long, List<T>> findItems(Date from, Date to, String kws, int size, int offset, boolean includeText,
                                   SortBuilder sort) {
        SearchResponse
            response =
            genericFindItems(from, to, kws, null, size, offset, "全部", "全部", "", "", "", includeText, sort);
        return parseSearchResponse(response);
    }

    public Tuple<Long, List<T>> findItems(Date from, Date to, String kws, String kws_kinds, int size, int offset, String language,
                             String location,
                             String types0, String types1, String types2, boolean includeText,
                             SortBuilder sort) {
        SearchResponse
            response =
            genericFindItems(from, to, kws, kws_kinds, size, offset, language, location, types0, types1, types2, includeText,
                             sort);
        return parseSearchResponse(response);
    }

    public Tuple<Long, List<T>> findItemsWith(Date from, Date to, String kws, String kws_kinds, int size, int offset, boolean includeText,
                                 SortBuilder sort, int secu) {
        SearchResponse
            response =
            genericFindItems(from, to, kws, kws_kinds, size, offset, "全部", "全部", "", "", "", includeText, sort, secu);
        return parseSearchResponse(response);
    }

    public SortedMap<String, Long> dateHist(Date from, Date to, String kws,
                                            DateHistogramInterval interval) {
        QueryBuilder query = boolQuery()
            .must(rangeQuery("@timestamp").gte(from).lte(to))
            .must(queryStringQuery(kws));

        String[] indices = genIndexNames(from, to);
        System.out.println(Arrays.toString(indices) + ": " + kws);

        AggregationBuilder agg = dateHistogram("dailytrend")
            .field("@timestamp")
            .interval(interval)
            .timeZone("+08:00");

        SearchResponse response = client.prepareSearch(indices)
            .setIndicesOptions(IndicesOptions.fromOptions(true, true, true, false))
            .setQuery(query)
            .setSize(0)
            .setMinScore(0.1f)
            .addAggregation(agg)
            .execute().actionGet();
        Histogram hist = response.getAggregations().get("dailytrend");
        SortedMap<String, Long> result = new TreeMap<>();
        for (Histogram.Bucket bucket : hist.getBuckets()) {
            result.put(bucket.getKeyAsString(), bucket.getDocCount());
        }
        return result;
    }

    public SortedMap<String, Long> dateHistDay(Date from, Date to, String kws) {
        return this.dateHist(from, to, kws, DateHistogramInterval.DAY);
    }

    public SortedMap<String, Long> dateHistHour(Date from, Date to, String kws) {
        return this.dateHist(from, to, kws, DateHistogramInterval.HOUR);
    }

    private SearchResponse genericFindItems(Date from, Date to, String kws, String kws_kinds,
                                            int size, int offset,
                                            String language, String loc,
                                            String types0,
                                            String types1,
                                            String types2,
                                            boolean includeText,
                                            SortBuilder sort) {
        return this
            .genericFindItems(from, to, kws, kws_kinds, size, offset, language, loc, types0, types1, types2, includeText, sort,
                              -1);
    }

    private SearchResponse genericFindItems(Date from, Date to, String kws, String kws_kinds,
                                            int size, int offset,
                                            String language, String loc,
                                            String types0,
                                            String types1,
                                            String types2,
                                            boolean includeText,
                                            SortBuilder sort, int secu) {

        String[] indices = genIndexNames(from, to);

        QueryBuilder timeRange = rangeQuery("time").gte(from).lte(to);
        QueryBuilder kwsQuery = matchAllQuery();
        if (kws != null && !kws.isEmpty()) {
            if (includeText) {
                kwsQuery = simpleQueryStringQuery(kws);
            } else {
                // kwsQuery = termsQuery("title", Arrays.asList(kws.split(" ")));
                kwsQuery = simpleQueryStringQuery(kws.replaceAll("\\s+", " | ")).field("title");
            }
        }
        QueryBuilder secuQuery = matchAllQuery();
        if (secu > 0) {
            secuQuery = rangeQuery("secu").gte(secu);
        }

        BoolQueryBuilder query = boolQuery()
            .must(kwsQuery)
            .mustNot(existsQuery("parentid"))
            .must(genLangQuery(language))
            .must(genLocQuery(loc))
            .must(secuQuery);

        BoolQueryBuilder filter = boolQuery()
            .must(timeRange);

        float minscore = 0.4f;

        if (types0 != null && !types0.isEmpty()) {
            filter.must(genTypesQuery("type0", types0));
        }
        if (types1 != null && !types1.isEmpty()) {
            filter.must(genTypesQuery("type1", types1));
        }
        if (types2 != null && !types2.isEmpty()) {
            if (kws_kinds != null && !kws_kinds.isEmpty()) {
                filter.must(boolQuery()
                                .should(genTypesQuery("type2", types2))
                                .should(matchQuery("title", kws_kinds).operator(MatchQueryBuilder.Operator.OR))
                                .minimumShouldMatch("0<1"));
            } else {
                filter.must(genTypesQuery("type2", types2));
            }
        } else {
            if (kws_kinds != null && !kws_kinds.isEmpty()) {
                // here we use query to improve result, and don't use title
                query.must(matchQuery("title", kws_kinds).operator(MatchQueryBuilder.Operator.OR));
                // more words, more minimal score
                minscore = Math.max(0.1f, minscore / (kws_kinds.split("\\s+").length / 5.0f + 1.0f));
            }
        }
        System.out.println(query);

        SearchResponse response = client.prepareSearch(indices)
            .setIndicesOptions(IndicesOptions.fromOptions(true, true, true, false))
            .setTypes("message")
            .setQuery(query)
            .setPostFilter(filter)
            .setMinScore(minscore)
            .setSize(size)
            .setFrom(offset)
            .addSort(sort)
            .execute().actionGet();
        return response;
    }

    private QueryBuilder genLangQuery(String lang) {
        if (lang == null || lang.isEmpty()) {
            return matchAllQuery();
        }
        switch (lang) {
            case "中文":
                switch (indexPrefixes[0]) { // the major
                    case "news-":
                        return rangeQuery("userno").lt(News.FOREIGN_USERID_START);
                    case "foreigns-":
                        return boolQuery().mustNot(matchAllQuery());
                    case "weibo_events":
                        return matchAllQuery();
                    default:
                        return matchAllQuery();
                }
            case "外文":
                switch (indexPrefixes[0]) { // the major
                    case "news-":
                        return rangeQuery("userno").gte(News.FOREIGN_USERID_START);
                    case "foreigns-":
                        return matchAllQuery();
                    case "weibo_events":
                        return boolQuery().mustNot(matchAllQuery());
                    default:
                        return matchAllQuery();
                }
            default:
                return matchAllQuery();
        }
    }

    private QueryBuilder genLocQuery(String loc) {
        if (loc == null || loc.isEmpty()) {
            return matchAllQuery();
        }
        switch (loc) {
            case "国内":
                return termQuery("location", "中国");
            case "海外":
                return boolQuery().mustNot(termQuery("location", "中国"))
                    .mustNot(termQuery("location", "其他"));
            default:
                return matchAllQuery();
        }
    }

    // 当一个TermsQuery中候选的Term太多时无法正常查询
    private static int TERMS_GROUP_SIZE = 5;

    private QueryBuilder genTypesQuery(String field, String types) {
        if (types == null || types.isEmpty()) {
            return matchAllQuery();
        }
        return termsQuery(field, types.split("\\s+"));
    }
}