package act.ring.cncert.data.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author HE, Tao
 *
 *         Modified by HE, Tao on 2017-11-03
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Element implements Comparable<Element> {

    private String id;
    private String datatag;

    private String user;
    private String userid;
    private String title;
    private String text;
    private String location;
    private int emotion;
    private int hot;
    private int importance;
    private int recommend;
    private int risk;
    private int gfw;
    private int secu;
    private int type0; // Ring的旧的分类体系
    private int type1; // 新的CNN的分类，大类
    private int type2; // 新的CNN的分类，小类
    private String url;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    private Date time;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ")
    private Date timestamp; // last modified time
    private String corewords;
    private List<String> simids;
    private String parentid;

    private String esIndex; // index name in ES.
    private float score;    // score from ES.

    public Element() {
    }

    public Element(String id) {
        this.id = id;
    }

    public static Element fromJson(JsonNode node) {
        ObjectNode value = (ObjectNode) node;
        String simids = "";
        Element item;
        if (value.has("simids")) {
            simids = value.get("simids").asText();
            value.remove("simids");
        }
        value.put("timestamp", value.get("@timestamp").asText().replaceAll("Z$", "+0000"));
        item = new ObjectMapper().convertValue(value, Element.class);
        LinkedList<String> simidsList = new LinkedList<>();
        if (!simids.isEmpty()) {
            for (String s : Arrays.asList(simids.split(","))) {
                if (!s.isEmpty()) {
                    simidsList.add(s);
                }
            }
        }
        item.setSimids(simidsList);
        return item;
    }

    public static Element fromMap(Map<String, Object> source) {
        return fromJson(new ObjectMapper().valueToTree(source));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDatatag() {
        return datatag;
    }

    public void setDatatag(String datatag) {
        this.datatag = datatag;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getEmotion() {
        return emotion;
    }

    public void setEmotion(int emotion) {
        this.emotion = emotion;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public int getImportance() {
        return this.importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public int getRecommend() {
        return recommend;
    }

    public void setRecommend(int recommend) {
        this.recommend = recommend;
    }

    public int getRisk() {
        return risk;
    }

    public void setRisk(int risk) {
        this.risk = risk;
    }

    public int getGfw() {
        return gfw;
    }

    public void setGfw(int gfw) {
        this.gfw = gfw;
    }

    public int getSecu() {
        return secu;
    }

    public void setSecu(int secu) {
        this.secu = secu;
    }

    public int getType0() {
        return type0;
    }

    public void setType0(int type0) {
        this.type0 = type0;
    }

    public int getType1() {
        return type1;
    }

    public void setType1(int type1) {
        this.type1 = type1;
    }

    public int getType2() {
        return type2;
    }

    public void setType2(int type2) {
        this.type2 = type2;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getCorewords() {
        return corewords;
    }

    public void setCorewords(String corewords) {
        this.corewords = corewords;
    }

    public List<String> getSimids() {
        return simids;
    }

    public void setSimids(List<String> simids) {
        this.simids = simids;
    }

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }

    public String getEsIndex() {
        return esIndex;
    }

    public void setEsIndex(String esIndex) {
        this.esIndex = esIndex;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object obj) {
        return this.id == ((Element) obj).id;
    }

    @Override
    public int compareTo(Element o) {
        return this.id.compareTo(o.id);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}