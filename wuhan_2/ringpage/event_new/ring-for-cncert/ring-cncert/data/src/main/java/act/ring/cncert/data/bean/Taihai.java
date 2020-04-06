package act.ring.cncert.data.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by Machenike on 2017/12/13.
 */
public class Taihai extends News {

    private float TitlePoint;
    private float TextPoint;

    public Taihai() {
        super();
    }

    public Taihai(float TitlePoint, float TextPoint) {
        this.TitlePoint = TitlePoint;
        this.TextPoint = TextPoint;
    }

    public static Taihai fromJson(JsonNode node) {
        ObjectNode value = (ObjectNode) node;
        String simids = "";
        Taihai item;
        if (value.has("simids")) {
            simids = value.get("simids").asText();
            value.remove("simids");
        }
        value.put("timestamp", value.get("@timestamp").asText().replaceAll("Z$", "+0000"));
        item = new ObjectMapper().convertValue(value, Taihai.class);
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

    public static Taihai fromMap(Map<String, Object> source) {
        return fromJson(new ObjectMapper().valueToTree(source));
    }

    public float getTitlePoint() {
        return this.TitlePoint;
    }

    public void setTitlePoint(float TitlePoint) {
        this.TitlePoint = TitlePoint;
    }

    public float getTextPoint() {
        return this.TextPoint;
    }

    public void setTextPoint(float TextPoint) {
        this.TextPoint = TextPoint;
    }
}
