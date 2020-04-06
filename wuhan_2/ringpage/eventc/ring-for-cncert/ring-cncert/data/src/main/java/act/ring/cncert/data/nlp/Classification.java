package act.ring.cncert.data.nlp;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import act.ring.cncert.data.nlp.ansj.AnsjSeg;
import act.ring.cncert.data.nlp.nlpir.NlpirCLibrary;
import act.ring.cncert.data.nlp.utils.BloomFilter;
import act.ring.cncert.data.nlp.utils.BuildBloomFilter;
import act.ring.cncert.data.nlp.utils.SVMPredict;

public class Classification {

    private static final Logger LOG = LoggerFactory.getLogger(Classification.class);

    private static final int LONG_TEXT_LIMIT = 4500;
    private static final String SVM_MODEL_NAEM = "ECS-tra-use.model";

    private static AnsjSeg ansjSeg = new AnsjSeg();
    private static BloomFilter EventTypeWordFilter0;
    private static BloomFilter EventTypeWordFilter1;
    private static BloomFilter EventTypeWordFilter2;
    private static BloomFilter EventTypeWordFilter3;
    private static BloomFilter EventTypeWordFilter4;
    private static BloomFilter EventTypeWordFilter6;
    private static BloomFilter EventTypeWordFilter7;
    private static BloomFilter EventTypeWordFilter8;
    private static BloomFilter EventTypeWordFilter9;
    private static BloomFilter EventTypeWordFilter11;
    private static BloomFilter EventTypeWordFilter12;
    private static BloomFilter EventTypeWordFilter13;
    private static BloomFilter EventTypeWordFilter14;
    private static BloomFilter EventTypeWordFilter16;
    private static BloomFilter EventTypeWordFilter17;
    private static BloomFilter EventTypeWordFilter18;
    private static BloomFilter EventTypeWordFilter19;

    private static Set<String> chinaLocations;

    static {
        // read event type word files.
        BuildBloomFilter BBF1 = new BuildBloomFilter();
        EventTypeWordFilter0 = BBF1.BuildBloomFilter(0);
        EventTypeWordFilter1 = BBF1.BuildBloomFilter(1);
        EventTypeWordFilter2 = BBF1.BuildBloomFilter(2);
        EventTypeWordFilter3 = BBF1.BuildBloomFilter(3);
        EventTypeWordFilter4 = BBF1.BuildBloomFilter(4);
        EventTypeWordFilter6 = BBF1.BuildBloomFilter(6);
        EventTypeWordFilter7 = BBF1.BuildBloomFilter(7);
        EventTypeWordFilter8 = BBF1.BuildBloomFilter(8);
        EventTypeWordFilter9 = BBF1.BuildBloomFilter(9);
        EventTypeWordFilter11 = BBF1.BuildBloomFilter(11);
        EventTypeWordFilter12 = BBF1.BuildBloomFilter(12);
        EventTypeWordFilter13 = BBF1.BuildBloomFilter(13);
        EventTypeWordFilter14 = BBF1.BuildBloomFilter(14);
        EventTypeWordFilter16 = BBF1.BuildBloomFilter(16);
        EventTypeWordFilter17 = BBF1.BuildBloomFilter(17);
        EventTypeWordFilter18 = BBF1.BuildBloomFilter(18);
        EventTypeWordFilter19 = BBF1.BuildBloomFilter(19);

        // read chinese locations file.
        String txt = "";
        try {
            txt = IOUtils
                .toString(Classification.class.getClassLoader()
                              .getResourceAsStream("chinalocations.txt"));
        } catch (IOException e) {
            LOG.error("Can't read the resource file: chinalocations.txt");
            e.printStackTrace();
        }
        chinaLocations = new HashSet<>();
        for (String m : txt.split("\n")) {
            String[] ms = m.split(" ");
            if (ms.length == 2) {
                chinaLocations.add(ms[0]);
            }
        }
    }

    private SVMPredict svmpredict;
    private ObjectMapper objectMapper;
    private HttpTransport transport;
    private HttpRequestFactory requestFactory;

    public Classification() {

        String svmdir = System.getProperty("svm.dir");
        String svmmodelpath;
        if (svmdir == null || svmdir.isEmpty()) {
            // fallback to resources dir
            svmmodelpath = getClass().getClassLoader().getResource(SVM_MODEL_NAEM).getPath();
        } else {
            svmmodelpath = svmdir + File.separator + SVM_MODEL_NAEM;
        }
        LOG.info("use svm model path: {}", svmmodelpath);
        this.svmpredict = new SVMPredict(svmmodelpath);

        this.objectMapper = new ObjectMapper();
        this.transport = new NetHttpTransport();
        this.requestFactory = transport.createRequestFactory();
    }

    public static void main(String[] args) throws Exception {
        Classification classify = new Classification();

        String test = "安徽妹子夜遇持械劫匪淡定继续嗑瓜子】近日，安徽萧县一加油站两劫匪用疑似手 " +
                      "枪顶住值班员小刘头部，此时值班员小李正淡定嗑瓜子。劫匪要钱时，小李拍拍手，将财物交出。" +
                      "歹徒走后，小李仍拿起瓜子磕了几粒，数秒后才反应过来，吓得坐到了椅子上。 http://t.cn/zQxzyx8";

        System.out.println(classify.getTextType(test)); // 6
        System.out.println(classify.getTextType1(test));
        System.out.println(classify.getTextType2(test));
        System.out.println(classify.getEmotionTypeHttp(test));

        String[] words2 = new String[]{"门", "艺人", "对抗", "老板", "说", "综艺", "XSUGAR", "糖果", "手机"};
        System.out.println(classify.process(Arrays.asList(words2))); // 3

        System.out.println(classify.inChina("海淀区")); // true
        System.out.println(classify.inChina("老挝")); // false
    }

    private String stripText(String text) {
        return text.replaceAll("([a-zA-z]+://[\\w\\./:-?\\&]+)", " ") // url
            .replaceAll("\\[.*?\\]", " ") // 表情符
            .replaceAll("//@.*?:", " ") // 转发
            .replaceAll("//@.*?$", " ") // 转发
            .replaceAll("@.*? ", " ") // @用户
            .replaceAll("@.*?$", " ")
            .replaceAll("@", " ")
            .replaceAll("\n+", " ")
            .replaceAll("\\s+", " ");
    }

    /**
     * 分类：9类
     */
    public int getTextType(String content) {
        int weiboType = 0;
        ArrayList<String> words = new ArrayList<>();

        content = this.stripText(content);

        String nativeByte = "";
        try {
            nativeByte =
                NlpirCLibrary.instance.NLPIR_ParagraphProcess(content.replaceAll(" ", ""), 1);
        } catch (Throwable e) {
            nativeByte = ansjSeg.ansjSeg(content.replaceAll(" ", ""), 1);
        }
        String keywords[] = nativeByte.split(" ");
        for (String keyword : keywords) {
            String T[] = keyword.split("/");
            if (T.length < 2 || T[0].startsWith("@")) {
                continue;
            }
            if (((T[1].startsWith("n") || T[1].startsWith("v")) && T[1].length() <= 2) || T[1]
                .equals("n_new")) {
                if (words.contains(T[0])) {
                    continue;
                }
                if (T[1].startsWith("n") && (T[0].equals("霾") || T[0].equals("贿") || T[0]
                    .equals("党"))) {
                    words.add(T[0]);
                } else if (T[0].length() >= 1) {
                    words.add(T[0]);
                }
            }
            if (words.size() >= 30) {
                break;
            }
        }

        if (words.isEmpty() || words.size() < 8) {
            return 0;
        }
        try {
            weiboType = process(words);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return weiboType;
    }

    private String getTextTypeReq(String endpoint, String text) {
        text = this.stripText(text);
        if (text.length() > LONG_TEXT_LIMIT) {
            text = text.substring(0, LONG_TEXT_LIMIT);
        }
        String splitWords = NlpirCLibrary.instance.NLPIR_ParagraphProcess(text, 0);
        String[] words = splitWords.split("\\s+");
        splitWords = "";
        for (String w : words) {
            if (w.length() < 2) {
                continue;
            }
            if (Pattern.matches("[\\u4e00-\\u9fa5]+", w.substring(0, 1)) && !w.contains("-")) {
                splitWords += w + " ";
            }
        }
        ByteArrayContent content =
            ByteArrayContent.fromString("text/plain", "data=" + splitWords);
        try {
            HttpRequest request =
                requestFactory.buildPostRequest(new GenericUrl(endpoint), content);
            return request.execute().parseAsString().trim();
        } catch (IOException e) {
            e.printStackTrace();
            return "0";
        }
    }

    public int getTextType1(String text) {
        String responseText = this.getTextTypeReq("http://bd33:8799", text);
        return Integer.parseInt(responseText);
    }

    public int getTextType2(String text) {
        String responseText = this.getTextTypeReq("http://bd33:8796", text);
        return Integer.parseInt(responseText.split(":")[1]);
    }

    public int getEmotionTypeHttp(String text) {
        ArrayNode value = objectMapper.createArrayNode();
        value.add(this.stripText(text));
        ByteArrayContent content =
            ByteArrayContent.fromString("application/json", value.toString());
        try {
            HttpRequest request =
                requestFactory.buildPostRequest(new GenericUrl("http://10.1.1.35:8766"), content);
            List<Integer> r = objectMapper.readValue(
                request.execute().parseAsString().trim(),
                objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
            return r.get(0);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public boolean inChina(String loc) {
        for (String area : chinaLocations) {
            if (area.contains(loc) || loc.contains(area)) {
                return true;
            }
        }
        return false;
    }

    private int process(List<String> words) throws IOException {

        int characteristics[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        float classificationvector[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int weiboeventtype = 9;
        for (int i = 0; i <= 10; i++) {
            characteristics[i] = 0;
            classificationvector[i] = 0;
        }
        int sum = 0;
        int weight = 10;
        for (String word : words) {
            if (word.equals("")) {
                continue;
            }
            if (EventTypeWordFilter0.contains(word)) {
                return 0;
            }
            if (EventTypeWordFilter11.contains(word)) {
                characteristics[1] += weight;
                sum += weight;
            }
            if (EventTypeWordFilter12.contains(word)) {
                characteristics[2] += weight;
                sum += weight;
            }
            if (EventTypeWordFilter13.contains(word)) {
                characteristics[3] += weight;
                sum += weight;
            }
            if (EventTypeWordFilter14.contains(word)) {
                characteristics[4] += weight;
                sum += weight;
            }
            if (EventTypeWordFilter16.contains(word)) {
                characteristics[6] += weight;
                sum += weight;
            }
            if (EventTypeWordFilter17.contains(word)) {
                characteristics[7] += weight;
                sum += weight;
            }
            if (EventTypeWordFilter18.contains(word)) {
                characteristics[8] += weight;
                sum += weight;
            }
            if (EventTypeWordFilter19.contains(word)) {
                characteristics[9] += weight;
                sum += weight;
            }
            if (EventTypeWordFilter1.contains(word)) {
                characteristics[1] += 1;
                sum += 1;
            }
            if (EventTypeWordFilter2.contains(word)) {
                characteristics[2] += 1;
                sum += 1;
            }
            if (EventTypeWordFilter3.contains(word)) {
                characteristics[3] += 1;
                sum += 1;
            }
            if (EventTypeWordFilter4.contains(word)) {
                characteristics[4] += 1;
                sum += 1;
            }
            if (EventTypeWordFilter6.contains(word)) {
                characteristics[6] += 1;
                sum += 1;
            }
            if (EventTypeWordFilter7.contains(word)) {
                characteristics[7] += 1;
                sum += 1;
            }
            if (EventTypeWordFilter8.contains(word)) {
                characteristics[8] += 1;
                sum += 1;
            }
            if (EventTypeWordFilter9.contains(word)) {
                characteristics[9] += 1;
                sum += 1;
            }
        }
        if (sum == 0) {
            return 0;
        }
        for (int i = 1; i <= 10; i++) {
            classificationvector[i] = (float) characteristics[i] / sum;
        }

        String input = weiboeventtype + "  " +
                       "1:" + classificationvector[1] + " " +
                       "2:" + classificationvector[2] + " " +
                       "3:" + classificationvector[3] + " " +
                       "4:" + classificationvector[4] + " " +
                       "5:" + classificationvector[5] + " " +
                       "6:" + classificationvector[6] + " " +
                       "7:" + classificationvector[7] + " " +
                       "8:" + classificationvector[8] + " " +
                       "9:" + classificationvector[9] + " " + "\n";

        return (int) svmpredict.predict(input, true);
    }
}
