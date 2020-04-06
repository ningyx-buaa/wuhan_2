package act.ring.cncert.data.nlp.cncnert;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * copy from ring-web
 * Created by Yan on 2017/6/29.
 */
public class SensitiveDegree {

    private static Logger LOG = LoggerFactory.getLogger(SensitiveDegree.class);

    private Set<String> sensitiveSet;
    private HashMap<Pattern, Double> sensitiveDict;

    public SensitiveDegree(String wordPath, String freqPath) {
        this.sensitiveSet = makeSensitiveSet(wordPath);
        this.sensitiveDict = makeSensitiveDict(freqPath);
    }

    public static void main(String[] args) {
        SensitiveDegree
            sd =
            new SensitiveDegree("sensitive/sensitive_words.txt",
                                "sensitive/sensitive_words_freq.txt");
        String
            text =
            "人民网北京6月28日电（刘洁妍 张瑞琦）今天上午，国台办发言人马晓光主持例行新闻发布会时表示，民进党只有放弃“台独”立场，才能在两岸关系上真正找到出路。 福建海峡卫视记者提问:台南市长赖清德在访问美国演讲时，宣称两岸和平的协议无助于和平，并表达自己的“台独”立场";
        int count = sd.sensitiveWordsCount(text);
        double freq = sd.sensitiveWordsFreq(text);
        System.out.printf("count: %d, freq: %f\n", count, freq);
    }

    private Set<String> makeSensitiveSet(String filePath) {
        Set<String> sensitiveWords = new HashSet<String>();
        try {
            String
                tempString =
                IOUtils
                    .toString(SensitiveDegree.class.getClassLoader().getResourceAsStream(filePath));
            for (String w : tempString.split("\\s+")) {
                sensitiveWords.add(w);
            }
        } catch (IOException e) {
            LOG.error("can't find resource file: " + filePath);
        }
        return sensitiveWords;
    }

    private HashMap<Pattern, Double> makeSensitiveDict(String filePath) {
        HashMap<Pattern, Double> sensitiveWords = new HashMap<>();
        try {
            String
                tempString =
                IOUtils
                    .toString(SensitiveDegree.class.getClassLoader().getResourceAsStream(filePath));
            for (String line : tempString.split("\\n")) {
                try {
                    String[] ss = line.split("\\s+");
                    sensitiveWords.put(Pattern.compile(ss[0]), Double.valueOf(ss[1]));
                } catch (Exception e) {
                    LOG.warn("meet invalid line: {}", line);
                }
            }
        } catch (IOException e) {
            LOG.error("can't find resource file: " + filePath);
        }
        return sensitiveWords;
    }

    public int sensitiveWordsCount(String text) {
        int totalWords = 0;
        for (String s : this.sensitiveSet) {
            String[] words = s.split(",");
            int min = -1;
            for (String word : words) {
                if (word.length() > 1) {
                    word = word.replaceAll("\\(", "\\\\(");
                    word = word.replaceAll("\\)", "\\\\)");
                    word = word.replaceAll("\\.", "\\\\.");
                    Pattern pattern = Pattern.compile(word);
                    Matcher matcher = pattern.matcher(text);
                    int count = 0;
                    while (matcher.find()) {
                        count++;
                    }
                    if (min == -1) {
                        min = count;
                    } else if (count < min) {
                        min = count;
                    }
                }
            }
            if (min > 0) {
                totalWords += min;
            }
        }
        return totalWords;
    }

    public double sensitiveWordsFreq(String text) {
        double sensitive = 0;
        for (Map.Entry<Pattern, Double> kv : sensitiveDict.entrySet()) {
            Matcher matcher = kv.getKey().matcher(text);
            int count = 0;
            while (matcher.find()) {
                count += 1;
            }
            sensitive += Math.log(Math.max(1, count)) * kv.getValue();
        }
        return sensitive;
    }
}
