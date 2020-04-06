package act.ring.cncert.data.topic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import act.ring.cncert.data.bean.News;
import act.ring.cncert.data.bean.Opinion;
import act.ring.cncert.data.nlp.Classification;
import act.ring.cncert.data.nlp.cncnert.Cncert;
import act.ring.cncert.data.nlp.cncnert.HotDegree;
import act.ring.cncert.data.nlp.utils.BloomFilter;
import act.ring.cncert.data.nlp.utils.BloomFilterHelper;

/**
 * Created by lijun on 2017/11/20.
 */
public class ExtractOpinion {

    private static BloomFilter bloomFilter = new BloomFilter();
    private String Path = "TaiwanExperts/";
    private Classification classify;
    private Map<Integer, String> opinion_tag;

    public ExtractOpinion() {
        BloomFilterHelper BFH = new BloomFilterHelper();
        bloomFilter = BFH.BloomFilterHelper(0, Path);
        classify = new Classification();
        opinion_tag = new HashMap<>();
        opinion_tag.put(0, "台湾");
    }

    public static ArrayList<String> extractViewpiont1(String text, String expert)  //提取新闻观点（基于双引号）
    {
        int index = text.indexOf(expert);
        ArrayList<String> list = new ArrayList<String>();
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("“")) {
            String subStr = "";
            int len = text.length();
            int cha = len - index;
            if (cha < 40) {
                subStr = text.substring(index);
            } else {
                subStr = text.substring(index, index + 40);
            }
            Pattern p = Pattern.compile("“(.*?)”");
            Matcher m = p.matcher(subStr);
            while (m.find()) {
                list.add(m.group());
            }
        }
        return list;

    }

    public static String extractViewpiont2(String text, String expert)  //提取新闻观点（基于xxx认为）
    {
        String res = "";
        int index1 = text.indexOf("认为");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("认为")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont3(String text, String expert)  //提取新闻观点（基于xxx说）
    {
        String res = "";
        int index1 = text.indexOf("说");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("说")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont4(String text, String expert)  //提取新闻观点（基于xxx指出）
    {
        String res = "";
        int index1 = text.indexOf("指出");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("指出")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont5(String text, String expert)  //提取新闻观点（基于xxx谈论）
    {
        String res = "";
        int index1 = text.indexOf("谈论");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("谈论")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont6(String text, String expert)  //提取新闻观点（基于xxx发表）
    {
        String res = "";
        int index1 = text.indexOf("发表");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("发表")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont7(String text, String expert)  //提取新闻观点（基于xxx提出）
    {
        String res = "";
        int index1 = text.indexOf("提出");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("提出")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont8(String text, String expert)  //提取新闻观点（基于xxx回应）
    {
        String res = "";
        int index1 = text.indexOf("回应");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("回应")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont9(String text, String expert)  //提取新闻观点（基于xxx表示）
    {
        String res = "";
        int index1 = text.indexOf("表示");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("表示")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont10(String text, String expert)  //提取新闻观点（基于xxx发文）
    {
        String res = "";
        int index1 = text.indexOf("发文");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("发文")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont11(String text, String expert)  //提取新闻观点（基于xxx强调）
    {
        String res = "";
        int index1 = text.indexOf("强调");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("强调")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont12(String text, String expert)  //提取新闻观点（基于xxx提到）
    {
        String res = "";
        int index1 = text.indexOf("提到");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("提到")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont13(String text, String expert)  //提取新闻观点（基于xxx评论）
    {
        String res = "";
        int index1 = text.indexOf("评论");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("评论")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont14(String text, String expert)  //提取新闻观点（基于xxx提及）
    {
        String res = "";
        int index1 = text.indexOf("提及");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("提及")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static String extractViewpiont15(String text, String expert)  //提取新闻观点（基于xxx发布）
    {
        String res = "";
        int index1 = text.indexOf("发布");
        int index = text.indexOf(expert);
        int end = index + 20 < text.length() ? index + 20 : (text.length());
        String sub = text.substring(index, end);
        if (sub.contains("发布")) {
            if (index1 > -1) {
                String subStr1 = "";
                String subStr2 = "";
                subStr1 = text.substring(index1);
                int index2 = subStr1.indexOf("。");
                subStr2 = subStr1.substring(0 + 3, index2 + 1);
                res = subStr2;
            }
        }
        return res;
    }

    public static ArrayList<String> filtrate1(ArrayList<String> input)  //去除小于10个字的观点
    {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).length() > 10) {
                list.add(input.get(i));
            }
        }
        return list;
    }

    public static ArrayList<String> filtrate2(ArrayList<String> input)  //去重
    {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i).length() > 10) {
                if (list.contains(input.get(i)) == false) {
                    list.add(input.get(i));
                }
            }
        }
        return list;
    }

    /* public static void main(String args[]) {
         ExtractOpinion ext=new ExtractOpinion();
         //  String news="7月29日，蒋经国,据台湾气象部门预计，今年第9号台风最早于20时在花莲登陆";
         //  String news="和弱势生跳舞，从头到尾都落拍。马英九：“请原谅我的手脚不协调，我真的尽力了……”（图片取自台媒）中国台湾网7月2日讯　据台湾东森新闻云报道，前台湾地区领导人马英九2日在facebook上PO出他前往宜兰探视弱势家庭孩子的照片，并贴上了与孩子们一同跳舞的影片。影片中看得出马跟不上节拍，还会同手同脚；不过网友都相当买账，直说虽然跳舞不协调，但“跳得很认真！跳得很可爱！”马英九表示，他今年在母亲节前夕，看到一则很感人的新闻：一个孝子靠打工帮母亲撑家计，还买了一台鸡蛋糕机器送给母亲圆梦。这位孝子罗弘杰，是罗东高商二年级的学生，也是今年台湾慈孝家庭楷模。他从媒体报道得知罗的故事后，非常感动，透过宜兰县宜萱妇幼关怀协会的安排，昨晚终于能当面为本人加油打气。马英九说，除了罗弘杰，宜萱妇幼关怀协会还辅导许多弱势家庭的孩子，他们不向命运低头，努力培养一技之长，希望透过自己的力量，翻转人生。看到孩子纯真无邪的笑容，心里的满足难以言喻。PO文里，马英九还贴上了一段影片，“感谢蔡秀姜老师的带动唱，让所有人渡过一个温馨难忘的夜晚”，只是马英九似乎一直跟不上节奏，连马办幕僚私下都说，马英九“从头到尾没有一个拍子跟上！没有！太强大了！”马英九也深知自己的问题，因此在facebook贴文末段还写上，“请原谅我的手脚不协调，我真的尽力了……”。就是这样一段手脚不协调的舞蹈，许多网友却十分买帐，看完影片后，都在下面留言“就是喜欢oppa，跳舞手脚不协调的样子”、“虽然不协调，但是很认真很可爱～”、“您好棒棒，手脚不协调无关紧要，爱心耐心开心是重点也是可贵的”。（中国台湾网 朱炼）";
         String news="文章指出，年龄超过四十岁以上的朋友，应该有印象马晓光说：“我是台湾人，我也是中国人。”其实蒋经国这个金句，是有前后文的，是有时空背景的。文章说，当年，台湾的党外势力由于有美国、日本当靠山庇护，有恃无恐，在南北各地搞了几场聚众滋扰事件，撩拨挑弄台湾内部平抚多年的民粹暗潮，一时之间颇让蒋经国及国民党当局棘手不已。美国又不断对蒋经国当局施以政治压力，频频以停售武器裹胁威逼，强行干涉台湾内部事务，迫使当局不敢肆意弹压党外势力";
         String expert="";
         ArrayList<String> view=new ArrayList<>();
         Boolean flag=ext.exit(news);
         if(flag)
         {
             expert=ext.extractExperts(news);
             ArrayList<String> tmp=ext.extractViewpiont(news,expert);
             view=ext.filtrate(tmp);
             System.out.println(view.size());
             for(int i=0;i<view.size();i++)
             {
                 System.out.println(expert+"说："+view.get(i));
             }
         }
         //  if(flag==true)
         //   {
         //       view="今年第9号台风最早于20时在花莲登陆，并于次日凌晨至清晨出海";
         //   }
         //   System.out.println(view);
         //Match tmp=new Match();
         // System.out.println(tmp.bloomFilter.contains("党朝胜"));
     }*/
    public static void main(String args[]) {
        ExtractOpinion ext = new ExtractOpinion();
        String
            news =
            "文章指出，年龄超过四十岁以上的朋友，应该有印象马晓光说：“我是台湾人，我也是中国人。”其实蒋经国这个金句，是有前后文的，是有时空背景的。文章说，当年，台湾的党外势力由于有美国、日本当靠山庇护，有恃无恐，在南北各地搞了几场聚众滋扰事件，撩拨挑弄台湾内部平抚多年的民粹暗潮，一时之间颇让蒋经国及国民党当局棘手不已。美国又不断对蒋经国当局施以政治压力，频频以停售武器裹胁威逼，强行干涉台湾内部事务，迫使当局不敢肆意弹压党外势力";
        List<String> view = new ArrayList<>();
        view = ext.Extnews(news);
        System.out.println(view.size());
        for (int i = 0; i < view.size(); i++) {
            System.out.println(view.get(i));
        }
    }

    public Map<Integer, String> getOpinion_tag() {
        return this.opinion_tag;
    }

    /*
    datatag =0 表示taiwan问题
     */
    public Opinion ExtractOpinionBySource(News news, int datatag) {
        //如果不包含重点词汇，则不是相关文本，直接过滤
        String keywords = opinion_tag.get(datatag);
        if (!news.getText().contains(keywords)) {
            return null;
        }

        Cncert cncert = new Cncert();

        HotDegree hotDegree = new HotDegree();
        List<String> opinionString = Extnews(news.getText());

        if (opinionString.size() != 0) {
            String holder = opinionString.get(0);
            opinionString.remove(0);
            String opinion_string = String.join("_", opinionString);

 //         System.out.println(opinionString);

            Opinion opinion = new Opinion(news.getId());
            opinion.setDatatag(String.valueOf(datatag));
            opinion.setUser(news.getUser());
            opinion.setUserid(news.getUserid());
            opinion.setTitle(news.getTitle());
            opinion.setTime(news.getTime());//todo 格式可能有问题
            opinion.setUrl(news.getUrl());
            opinion.setHolder(holder);
            opinion.setOpinion(opinion_string);//注意在源文本中去掉下划线

            opinion.setEmotion(classify.getEmotionTypeHttp(opinion_string));
            opinion.setSensitive(cncert.getSensitivity(opinion_string));
            opinion.setHot(Math.toIntExact(hotDegree.getOpinionHot(opinion_string)));

            opinion.setImportance(news.getImportance());
            opinion.setRecommend(news.getRecommend());
            opinion.setRisk(news.getRecommend());

            return opinion;
        } else {
            return null;
        }
    }

    public List<String> Extnews(String news) {
        List<String> res = new ArrayList<>();
        String expert = "";
        ArrayList<String> view = new ArrayList<>();
        Boolean flag = exit(news);
        if (flag) {
            expert = extractExperts(news);
            ArrayList<String> tmp = extractViewpiont(news, expert);
            view = filtrate(tmp);
            if (view.size() != 0) {
                res.add(expert);//list第一个放expert
                res.addAll(view);
            }
//            for (int i = 0; i < view.size(); i++) {
//                res.add(expert + "说：" + view.get(i));
//            }
        }
        return res;
    }

    public boolean exit(String text) //提取人名，看其是否在专家词库中
    {
        Boolean flag = false;
        List<String> str = new ArrayList<>();
        CutWords cut = new CutWords();
        str = cut.Cutwords(text);
        for (int i = 0; i < str.size(); i++) {
            if (bloomFilter.contains(str.get(i))) {
                flag = true;
            }
        }
        return flag;
    }

    public String extractExperts(String text)  //提取人名，若在人名词库中，则显示该人名
    {
        String res = "";
        List<String> str = new ArrayList<>();
        CutWords cut = new CutWords();
        str = cut.Cutwords(text);
        for (int i = 0; i < str.size(); i++) {
            if (bloomFilter.contains(str.get(i))) {
                res = str.get(i);
            }
        }
        return res;
    }

    public ArrayList<String> extractViewpiont(String news, String expert)  //提取新闻观点
    {
        ArrayList<String> view = new ArrayList<String>();
        view = extractViewpiont1(news, expert);
        if (extractViewpiont2(news, expert) != "") {
            view.add("“" + extractViewpiont2(news, expert) + "”");
        }
        if (extractViewpiont3(news, expert) != "") {
            view.add("“" + extractViewpiont3(news, expert) + "”");
        }
        if (extractViewpiont4(news, expert) != "") {
            view.add("“" + extractViewpiont4(news, expert) + "”");
        }
        if (extractViewpiont5(news, expert) != "") {
            view.add("“" + extractViewpiont5(news, expert) + "”");
        }
        if (extractViewpiont6(news, expert) != "") {
            view.add("“" + extractViewpiont6(news, expert) + "”");
        }
        if (extractViewpiont7(news, expert) != "") {
            view.add("“" + extractViewpiont7(news, expert) + "”");
        }
        if (extractViewpiont8(news, expert) != "") {
            view.add("“" + extractViewpiont8(news, expert) + "”");
        }
        if (extractViewpiont9(news, expert) != "") {
            view.add("“" + extractViewpiont9(news, expert) + "”");
        }
        if (extractViewpiont10(news, expert) != "") {
            view.add("“" + extractViewpiont10(news, expert) + "”");
        }
        if (extractViewpiont11(news, expert) != "") {
            view.add("“" + extractViewpiont11(news, expert) + "”");
        }
        if (extractViewpiont12(news, expert) != "") {
            view.add("“" + extractViewpiont12(news, expert) + "”");
        }
        if (extractViewpiont13(news, expert) != "") {
            view.add("“" + extractViewpiont13(news, expert) + "”");
        }
        if (extractViewpiont14(news, expert) != "") {
            view.add("“" + extractViewpiont14(news, expert) + "”");
        }
        if (extractViewpiont15(news, expert) != "") {
            view.add("“" + extractViewpiont15(news, expert) + "”");
        }
        return view;
    }

    public ArrayList<String> filtrate(ArrayList<String> input)  //过滤
    {
        ArrayList<String> tmp = filtrate1(input);
        ArrayList<String> list = filtrate2(tmp);
        return list;
    }
}
