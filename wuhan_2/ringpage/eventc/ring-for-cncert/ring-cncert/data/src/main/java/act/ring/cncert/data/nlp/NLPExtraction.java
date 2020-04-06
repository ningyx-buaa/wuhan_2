package act.ring.cncert.data.nlp;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;

import act.ring.cncert.data.nlp.nlpir.CLibrarySentiment;
import act.ring.cncert.data.nlp.nlpir.NlpirCLibrary;

public class NLPExtraction {

    public NLPExtraction() {
    }

    public static void main(String[] args) {
        NLPExtraction nlpExtractor = new NLPExtraction();
        System.out.println(nlpExtractor.getKeyWords("桃江县政府：已有50名受肺结核疫情影响学生达到复学标准"));
        System.out.println(nlpExtractor.getSentiment("桃江县政府：已有50名受肺结核疫情影响学生达到复学标准"));
        System.out.println(nlpExtractor.getSentiment("新华社照片，路透，2016年9月6日 \n"
                                                     + "    （晚报）冲凉 \n"
                                                     + "    9月6日，在斯里兰卡科伦坡，一名男孩在凯勒尼河畔用河水冲洗身体消暑。 \n"
                                                     + "    新华社/路透"));
    }

    public String getKeyWords(String text) {
        return NlpirCLibrary.instance.NLPIR_GetKeyWords(text, 10, false);
    }

    public int getSentiment(String text) {
        try {
            String r = CLibrarySentiment.instance.ST_GetSentencePoint(text);
            Document dom = DocumentHelper.parseText(r);
            int emotion = (int) Double.parseDouble(dom.getRootElement().element("result").element("polarity").getTextTrim());
            if (emotion > 0) {
                return 1;
            } else if (emotion < 0) {
                return 2;
            } else {
                return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
