package act.ring.cncert.data.nlp.cncnert;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.UrlEncodedContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.GenericData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.googlecode.jsonrpc4j.JsonRpcHttpClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HE, Tao on 2017/11/16.
 *
 * 此文件用于存放中心提供的接口相关的服务。
 */
public class Cncert {

    private static Logger LOG = LoggerFactory.getLogger(Cncert.class);

    private static SensitiveDegree sensitive;

    private static Map<String, String> headers;

    private ObjectMapper objectMapper = new ObjectMapper();
    private HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory();

    static {
        // sensitive
        sensitive =
            new SensitiveDegree("sensitive/sensitive_words.txt",
                                "sensitive/sensitive_words_freq.txt");
        headers = new HashMap<String, String>();
        headers.put("Rpc-Type", "shop");
    }

    public static void main(String[] args) {
        Cncert cncert = new Cncert();
        String text1 = "习近平对梁振英担任行政长官5年来的工作予以高度肯定";
        String text2 = "中堂中学砍学生事件，见了有关图片，一整晚都不舒服[悲伤]";
        String text3 = "实现祖国统一既需要反对和遏制任何台独分裂行为的硬实力，还包括一切藏独、疆独、非法宗教行为";

//        System.out.println(Arrays.toString(cncert.doReqEmotion(text1, text1)));
        System.out.println(cncert.doReqSentiment("", text2));
//        System.out.println(Arrays.toString(cncert.doReqAll("foreign", text1, text1)));
//        System.out.println(Arrays.toString(cncert.doReqAll(text1, text2)));
//        System.out.println(Arrays.toString(cncert.doReqAll("foreign", text1, text3)));
//
//        System.out.println(cncert.getSensitivity(text1));
//        System.out.println(cncert.getSensitivity(text2));
//        System.out.println(cncert.getSensitivity(text3));
    }

    private static String extendText(String text, int length) {
        while (!text.isEmpty() && text.length() < length) {
            text = text + " " + text;
        }
        return text;
    }

    private static String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) {
            return ""; // vacancy test.
        }
        for (int i = 0; i < in.length(); i++) {
            current =
                in.charAt(
                    i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF))) {
                out.append(current);
            }
        }
        return out.toString();
    }

    /**
     * @return [重要度(importance)、推荐度(recommend)、风险度(risk)、gfw、secu]
     */
    public int[] doReqAll(String tag, String title, String text) {
        text = stripNonValidXMLCharacters(text);
        String title_ext = stripNonValidXMLCharacters(extendText(title, 100));
        String text_ext = stripNonValidXMLCharacters(extendText(title + text, 100));

        JsonRpcHttpClient rpcHttpClient;

        int importance = -1, recommend = -1, risk = -1, gfw = -1, secu = -1;

        try {
            rpcHttpClient = new JsonRpcHttpClient(new URL("http://202.108.211.41:8080"));
            rpcHttpClient.setHeaders(headers);
            // 计算文本的重要度/推荐度
            double[] ir = rpcHttpClient.invoke("refText", new Object[]{text_ext}, double[].class);
            importance = new Double(ir[0] * 100).intValue();
            recommend = new Double(ir[1] * 100).intValue();
            // 计算文本的风险度
            double
                risktmp =
                rpcHttpClient.invoke("refTextRsk", new Object[]{text_ext}, double.class);
            risk = new Double(risktmp * 100).intValue();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        try {
            if (tag.equals("foreign")) {
                rpcHttpClient = new JsonRpcHttpClient(new URL("http://202.108.211.41:6050"));
            } else {
                rpcHttpClient = new JsonRpcHttpClient(new URL("http://202.108.211.41:6030"));
            }
            rpcHttpClient.setHeaders(headers);
            // 计算GWF（是否涉及关防信息）
            gfw = rpcHttpClient.invoke("getGfwRnn", new Object[]{text}, int.class);
            // 识别是否为涉安全信息（>55分）
            int score1 = rpcHttpClient.invoke("getSecuRnn", new Object[]{text}, int.class);
            // 识别是否为敏感信息（>55分）
            int score2 = rpcHttpClient.invoke("getMingRnn", new Object[]{text}, int.class);
            secu = Math.max(score1, score2);
        } catch (Throwable e) {
            LOG.error("doReqIRR timeout: {}...", title);
            e.printStackTrace();
        }
        return new int[]{importance, recommend, risk, gfw, secu};
    }

    /**
     * @return [重要度(importance)、推荐度(recommend)、风险度(risk)]
     */
    public int[] doReqIRR(String title, String text) {
        text = stripNonValidXMLCharacters(text);
        String text_ext = stripNonValidXMLCharacters(extendText(title + text, 100));

        JsonRpcHttpClient rpcHttpClient;

        int importance = -1, recommend = -1, risk = -1;

        try {
            rpcHttpClient = new JsonRpcHttpClient(new URL("http://202.108.211.41:8080"));
            rpcHttpClient.setHeaders(headers);
            // 计算文本的重要度/推荐度
            double[] ir = rpcHttpClient.invoke("refText", new Object[]{text_ext}, double[].class);
            importance = new Double(ir[0] * 100).intValue();
            recommend = new Double(ir[1] * 100).intValue();
            // 计算文本的风险度
            double
                risktmp =
                rpcHttpClient.invoke("refTextRsk", new Object[]{text_ext}, double.class);
            risk = new Double(risktmp * 100).intValue();
        } catch (Throwable e) {
            LOG.error("doReqIRR timeout: {}...", title);
            e.printStackTrace();
        }
        return new int[]{importance, recommend, risk};
    }

    /**
     * @return secu
     */
    public int doReqSecu(String tag, String text) {
        text = stripNonValidXMLCharacters(text);
        try {
            // 计算GWF（是否涉及关防信息）
            JsonRpcHttpClient rpcHttpClient;
            if (tag.equals("foreign")) {
                rpcHttpClient = new JsonRpcHttpClient(new URL("http://202.108.211.41:6050"));
            } else {
                rpcHttpClient = new JsonRpcHttpClient(new URL("http://202.108.211.41:6030"));
            }
            rpcHttpClient.setHeaders(headers);
            // 识别是否为涉安全信息（>55分）
            int score1 = rpcHttpClient.invoke("getSecuRnn", new Object[]{text}, int.class);
            // 识别是否为敏感信息（>55分）
            int score2 = rpcHttpClient.invoke("getMingRnn", new Object[]{text}, int.class);
            return Math.max(score1, score2);
        } catch (Throwable e) {
            LOG.error("doReqSecu timeout: {}...", text.length() >= 10 ? text.substring(0, 10) : text);
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * @return emotion: [bow, rnn]
     */
    public int[] doReqEmotion(String title, String text) {
        text = stripNonValidXMLCharacters(text);
        GenericData value = new GenericData();
        value.put("content", title + text);
        UrlEncodedContent content = new UrlEncodedContent(value);
        try {
            HttpRequest request =
                requestFactory.buildPostRequest(new GenericUrl("http://47.93.46.171/demo/webview/keyquery_extractEntitys"), content);
            JsonNode r = objectMapper.readTree(
                request.execute().parseAsString().trim());
            boolean succ = r.get("success").asBoolean(false);
            if (succ) {
                String[] rtext = r.get("entitys").asText().split("<br><br>");
                // sample: <br>情感倾向（BoW）: 负面 （31, 0-49负面, 50中性, 51-100正面）<br><br>情感倾向（RNN）: 正面 （90, 0-40负面, 41-60中性, 61-100正面）<br><br><br>突发信息（WRD）: ....
                int e1 = Integer.parseInt(rtext[0].split(": ")[1].split("（")[1].split(",")[0]);
                int e2 = Integer.parseInt(rtext[1].split(": ")[1].split("（")[1].split(",")[0]);
                return new int[]{e1, e2};
            }
            return new int[]{0, 0};
        } catch (IOException e) {
            e.printStackTrace();
            return new int[]{0, 0};
        }
    }

    public String doReqSentiment(String title, String text) {
        text = stripNonValidXMLCharacters(text);
        try {
            // 计算GWF（是否涉及关防信息）
            JsonRpcHttpClient rpcHttpClient;
            rpcHttpClient = new JsonRpcHttpClient(new URL("http://202.108.211.41:6030"));
            rpcHttpClient.setHeaders(headers);
            // 识别是否为涉安全信息（>55分）
            return rpcHttpClient.invoke("getSentiment", new Object[]{title + text}, String.class);
        } catch (Throwable e) {
            LOG.error("doReqSentiment timeout: {}...", text.length() >= 10 ? text.substring(0, 10) : text);
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 计算敏感度，返回：敏感词的Count，整数，转换为double表示。
     */
    public int getSensitivity(String text) {
        return (int) sensitive.sensitiveWordsFreq(text) * 100;
    }
}
