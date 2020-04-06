package act.ring.cncert.data.topic;

import java.util.ArrayList;
import java.util.List;

import act.ring.cncert.data.nlp.nlpir.NlpirCLibrary;

/**
 * Imported from ring-event, 2017/11/20.
 *
 * Created by zhourui on 2017/11/18.
 */
public class CutWords {

    public static void main(String[] args) {
        //   String test = "王磊告诉李易峰，我要去吃饭，张鑫也走了，张三说好";
        //   String test="和弱势生跳舞，从头到尾都落拍。马英九：“请原谅我的手脚不协调，我真的尽力了……”（图片取自台媒）中国台湾网7月2日讯　据台湾东森新闻云报道，前台湾地区领导人马英九2日在facebook上PO出他前往宜兰探视弱势家庭孩子的照片，并贴上了与孩子们一同跳舞的影片。影片中看得出马跟不上节拍，还会同手同脚；不过网友都相当买账，直说虽然跳舞不协调，但“跳得很认真！跳得很可爱！”马英九表示，他今年在母亲节前夕，看到一则很感人的新闻：一个孝子靠打工帮母亲撑家计，还买了一台鸡蛋糕机器送给母亲圆梦。这位孝子罗弘杰，是罗东高商二年级的学生，也是今年台湾慈孝家庭楷模。他从媒体报道得知罗的故事后，非常感动，透过宜兰县宜萱妇幼关怀协会的安排，昨晚终于能当面为本人加油打气。马英九说，除了罗弘杰，宜萱妇幼关怀协会还辅导许多弱势家庭的孩子，他们不向命运低头，努力培养一技之长，希望透过自己的力量，翻转人生。看到孩子纯真无邪的笑容，心里的满足难以言喻。PO文里，马英九还贴上了一段影片，“感谢蔡秀姜老师的带动唱，让所有人渡过一个温馨难忘的夜晚”，只是马英九似乎一直跟不上节奏，连马办幕僚私下都说，马英九“从头到尾没有一个拍子跟上！没有！太强大了！”马英九也深知自己的问题，因此在facebook贴文末段还写上，“请原谅我的手脚不协调，我真的尽力了……”。就是这样一段手脚不协调的舞蹈，许多网友却十分买帐，看完影片后，都在下面留言“就是喜欢oppa，跳舞手脚不协调的样子”、“虽然不协调，但是很认真很可爱～”、“您好棒棒，手脚不协调无关紧要，爱心耐心开心是重点也是可贵的”。（中国台湾网 朱炼）";
        String test = "文章指出，年龄超过四十岁以上的朋友，应该有印象,蒋经国讲过的这句名言：“我是台湾人，我也是中国人。”呵呵是维护是和平呼呼";
        CutWords cut = new CutWords();
        List<String> str = new ArrayList<>();
        str = cut.Cutwords(test);
        System.out.println(str);
    }

    /*
    输入文本，NIPIR进行分词,选人名
    */
    public List<String> Cutwords(String content) {
        String splitbyNLPIR = "";
        List<String> words = new ArrayList<String>();
        splitbyNLPIR =
            NlpirCLibrary.instance.NLPIR_ParagraphProcess(content.replaceAll(" ", ""), 1);
        String keywords[] = splitbyNLPIR.split(" ");
        for (String keyword : keywords) {
            String kw[] = keyword.split("/");
            if (kw.length < 2 || kw[0].startsWith("@") || kw[0].length() > 6) {
                continue;
            }
            if (((kw[1].startsWith("nr")) && kw[1].length() <= 2) || kw[1].equals("n_new")) {
                if (kw[0].length() > 1 && !words.contains(kw[0])) {
                    words.add(kw[0]);
                }
            }
        }
        return words;
    }
}
