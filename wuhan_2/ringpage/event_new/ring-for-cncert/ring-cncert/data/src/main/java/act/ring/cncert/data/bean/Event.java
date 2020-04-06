package act.ring.cncert.data.bean;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author HE, Tao
 *
 *         Modified by HE, Tao on 2017-11-03
 */
public class Event extends Element {

    private int sensitivity;
    private String chain;
    private float sim;
    private float novelty;
    private int chainlen;
    private String eventSpanDateString;
    private int e_type;
    private int eventtype2;
    private String participant;
    private boolean isEvent = true;

    public Event() {
        super();
        this.setDatatag("event");
    }

    public Event(String id) {
        super(id);
        this.setDatatag("event");
    }

    public static Event fromJson(JsonNode node) {
        ObjectNode value = (ObjectNode) node;
        String simids = "";
        Event item;
        if (value.has("simids")) {
            simids = value.get("simids").asText();
            value.remove("simids");
        }
        if (value.has("@timestamp")) {
            value.put("timestamp", value.get("@timestamp").asText().replaceAll("Z$", "+0000"));
        }
        if (value.has("time")) {
            value.put("time", value.get("time").asText().replaceAll("Z$", "+0000"));
        }
        if (value.has("desc")) {
            value.set("title", value.get("desc"));
        }
        item = new ObjectMapper().convertValue(value, Event.class);
        LinkedList<String> simidsList = new LinkedList<String>();
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

    public static Event fromMap(Map<String, Object> source) {
        return fromJson(new ObjectMapper().valueToTree(source));
    }

    public int getSensitivity() {
        return sensitivity;
    }

    public void setSensitivity(int sensitivity) {
        this.sensitivity = sensitivity;
    }

    public String getChain() {
        return this.chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public float getSim() {
        return this.sim;
    }

    public void setSim(float sim) {
        this.sim = sim;
    }

    public float getNovelty() {
        return this.novelty;
    }

    public void setNovelty(float novelty) {
        this.novelty = novelty;
    }

    public float getChainlen() {
        return this.chainlen;
    }

    public void setChainlen(int chainlen) {
        this.chainlen = chainlen;
    }

    public String getEventSpanDateString() {
        return eventSpanDateString;
    }

    public void setEventSpanDateString(String eventSpanDateString) {
        this.eventSpanDateString = eventSpanDateString;
    }

    public void setTime(Date time) {
        super.setTime(time);
        this.setEventSpanDateString(
            (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(this.getTime()));
    }

    public int getE_type() {
        return this.e_type;
    }

    public void setE_type(int e_type) {
        this.setType1(e_type);
        this.e_type = e_type;
    }

    public int getEventtype2() {
        return this.eventtype2;
    }

    public void setEventtype2(int eventtype2) {
        this.setType2(eventtype2);
        this.eventtype2 = eventtype2;
    }

    public String getParticipant() {
        return this.participant;
    }

    public void setParticipant(String participant) {
        this.participant = participant;
    }
}