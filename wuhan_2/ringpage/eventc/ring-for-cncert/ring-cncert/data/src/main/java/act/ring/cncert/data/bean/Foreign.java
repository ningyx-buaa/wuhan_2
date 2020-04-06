package act.ring.cncert.data.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

/**
 * Foreign media: facebook, twitter,
 */
public class Foreign extends Element {

    private String origin_title;
    private String origin_text;
    private String detailwords;

    private int viewcnt;
    private int likecnt;
    private int dislikecnt;

    public Foreign() {
        super();
        this.setDatatag("foreign");
    }

    public Foreign(String id) {
        super(id);
        this.setDatatag("foreign");
    }

    public static Foreign fromJson(JsonNode node) {
        ObjectNode value = (ObjectNode) node;
        String simids = "";
        Foreign item;
        if (value.has("simids")) {
            simids = value.get("simids").asText();
            value.remove("simids");
        }
        value.put("timestamp", value.get("@timestamp").asText().replaceAll("Z$", "+0000"));
        item = new ObjectMapper().convertValue(value, Foreign.class);
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

    public static Foreign fromMap(Map<String, Object> source) {
        return fromJson(new ObjectMapper().valueToTree(source));
    }

    public String getOrigin_title() {
        return origin_title;
    }

    public void setOrigin_title(String origin_title) {
        this.origin_title = origin_title;
    }

    public String getOrigin_text() {
        return origin_text;
    }

    public void setOrigin_text(String origin_text) {
        this.origin_text = origin_text;
    }

    public String getDetailwords() {
        return detailwords;
    }

    public void setDetailwords(String detailwords) {
        this.detailwords = detailwords;
    }

    public int getViewcnt() {
        return viewcnt;
    }

    public void setViewcnt(int viewcnt) {
        this.viewcnt = viewcnt;
    }

    public int getLikecnt() {
        return likecnt;
    }

    public void setLikecnt(int likecnt) {
        this.likecnt = likecnt;
    }

    public int getDislikecnt() {
        return dislikecnt;
    }

    public void setDislikecnt(int dislikecnt) {
        this.dislikecnt = dislikecnt;
    }
}
