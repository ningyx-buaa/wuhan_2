package act.ring.cncert.task.process;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.text.SimpleDateFormat;

import act.ring.cncert.data.bean.Element;
import act.ring.cncert.data.common.base.Maybe;
import act.ring.cncert.data.nlp.Classification;
import act.ring.cncert.data.nlp.NLPExtraction;
import act.ring.cncert.data.nlp.cncnert.Cncert;

public abstract class Processor<T> extends Thread {

    protected static final ThreadLocal<SimpleDateFormat> plainTime =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMddHHmmss"));
    protected static final ThreadLocal<SimpleDateFormat> fullTime =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    protected static final ThreadLocal<SimpleDateFormat> isoTime =
        ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZ"));

    protected static final String ZOOKEEPER_ADDR = "10.1.1.14:2181";
    protected static final String
        KAFKA_ADDR =
        "10.1.1.11:9092,10.1.1.11:9093,10.1.1.11:9094,10.1.1.12:9092,10.1.1.12:9093,10.1.1.12:9094,10.1.1.13:9092,10.1.1.13:9093,10.1.1.13:9094";
    protected static final String KAFKA_GROUP_ID = "ring-cncert-task";
    private static final String TRANSLATE_ADDR = "http://10.1.1.11:3001/translate";
    private ObjectMapper objectMapper;
    private GenericUrl removeDupEndpoint;
    private GenericUrl translateEndpoint;
    private HttpTransport transport;
    private HttpRequestFactory requestFactory;
    private Classification classify;
    private Cncert cncert;
    private NLPExtraction nlp = new NLPExtraction();

    public Processor(String removeDupAddr) {
        this.objectMapper = new ObjectMapper();
        this.removeDupEndpoint = new GenericUrl(removeDupAddr);
        this.translateEndpoint = new GenericUrl(TRANSLATE_ADDR);
        this.transport = new NetHttpTransport();
        this.requestFactory = transport.createRequestFactory();
        this.classify = new Classification();
        this.cncert = new Cncert();
    }

    public Maybe<String> duplicateWith(String id, String text) throws IOException {
        ObjectNode value = objectMapper.createObjectNode();
        value.put("id", id);
        value.put("text", text);
        ObjectNode valueNode = objectMapper.valueToTree(value);
        valueNode
            .putObject("message");  // patch an empty "message" field for logstash's json codec.
        ByteArrayContent
            content =
            ByteArrayContent.fromString("application/json", valueNode.toString());
        HttpRequest request = requestFactory.buildPostRequest(this.removeDupEndpoint, content);
        String responseText = request.execute().parseAsString();
        JsonNode result = objectMapper.readTree(responseText);
        if (result.get("dup").asBoolean()) {
            return new Maybe<>(result.get("dup_text_id").asText());
        } else {
            return new Maybe<>();
        }
    }

    protected String doTranslate(String text) {
        ObjectNode valueNode = objectMapper.createObjectNode();
        valueNode.put("text", text);
        ByteArrayContent
            content =
            ByteArrayContent.fromString("application/json", valueNode.toString());
        try {
            HttpRequest request = requestFactory.buildPostRequest(translateEndpoint, content);
            JsonNode result = objectMapper.readTree(request.execute().parseAsString());
            if (result.get("error").asInt() == 0) {
                return result.get("result").asText();
            } else {
                return text; // return original text.
            }
        } catch (IOException e) {
            return text; // return original text.
        }
    }

    protected String refineLoc(String loc) {
        if (classify.inChina(loc)) {
            return "中国" + " " + loc;
        } else {
            return loc;
        }
    }

    protected int[] doCncertReqAll(String tag, String title, String text) {
        return this.cncert.doReqAll(tag, title, text);
    }

    protected int doCncertReqSecu(String tag, String text) {
        return this.cncert.doReqSecu(tag, text);
    }

    protected void fillCncertIndex(Element item, String title, String text) {
        int[] r = this.cncert.doReqIRR(title, text);
        item.setImportance(r[0]);
        item.setRecommend(r[1]);
        item.setRisk(r[2]);
    }

    protected void fillCncertSecu(String tag, Element item, String title) {
        item.setSecu(this.doCncertReqSecu(tag, title));
    }

    private int understandEmotionResult(int r0, int r1) {
        final int bound0 = 50, bound1 = 50;
        if (r0 == bound0 || r1 == bound1) {
            return 0; // 中性
        }
        if (Math.abs(r0 - bound0) > Math.abs(r1 - bound1)) {
            if (r0 > bound0) {
                return 5; // 正面
            } else {
                return 6; // 负面
            }
        } else {
            if (r1 > bound1) {
                return 5; // 正面
            } else {
                return 6; // 负面
            }
        }
    }

    private int understandSentimentResult(String r) {
        if (r == null || r.isEmpty()) {
            return  0;
        } else {
            if (r.split(" ")[0].equals("POS")) {
                return 5; // 证明
            } else {
                return 6; // 负面
            }
        }
    }

    protected void refillEmotion(Element item, String title, String text) {
        if (item.getEmotion() != 0) {
            return;
        }
        int emo = nlp.getSentiment(title + text);
        if (emo != 0) {
            item.setEmotion(emo);
            return;
        }
        int[] r = cncert.doReqEmotion(title, text);
        item.setEmotion(this.understandEmotionResult(r[0], r[1]));
    }

    protected void refillSentiment(Element item, String title, String text) {
//        String r = cncert.doReqSentiment(title, text);
//        item.setEmotion(this.understandSentimentResult(r));

        // forward the invoke to `refillEmotion` since the `doReqSentiment` always timeout.
        //
        // update by @hetao, 2018-07-13.
        refillEmotion(item, title, text);
    }
}