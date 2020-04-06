package act.ring.cncert.data.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

/**
 * Modified by HE, Tao on 2017-11-03
 */
public class News extends Element {

    public static final int FOREIGN_USERID_START = 60;

    private int userno;

    public News() {
        super();
        this.setDatatag("news");
    }

    public News(String id) {
        super(id);
        this.setDatatag("news");
    }

    public static News fromJson(JsonNode node) {
        ObjectNode value = (ObjectNode) node;
        String simids = "";
        News item;
        if (value.has("simids")) {
            simids = value.get("simids").asText();
            value.remove("simids");
        }
        value.put("timestamp", value.get("@timestamp").asText().replaceAll("Z$", "+0000"));
        item = new ObjectMapper().convertValue(value, News.class);
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

    public static News fromMap(Map<String, Object> source) {
        return fromJson(new ObjectMapper().valueToTree(source));
    }

    public int getUserno() {
        return userno;
    }

    public void setUserno(int userno) {
        this.userno = userno;
    }
}