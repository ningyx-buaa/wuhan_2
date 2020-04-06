package act.ring.cncert.data.nlp.nlpir;

import com.sun.jna.Library;
import com.sun.jna.Native;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface CLibrarySentiment extends Library {

    CLibrarySentiment instance = CLibrarySentimentLoader.getlNlpirCLibrary();

    /**
     * 初始化
     * @param sInitDirPath data文件夹所在的目录的路径
     * @param encode 编码
     * @param sLicenseCode 授权码
     * @return
     */
    int ST_Init(String sInitDirPath, int encode, String sLicenseCode);

    /**
     * 对一篇文章的某一个对象进行情感分析
     * @param sTitle 文章标题，若不分析标题则传入空字符串
     * @param sContent 文章内容
     * @param sObject 分析对象
     * @return 分析结果
     */
    String ST_GetOneObjectResult(String sTitle, String sContent, String sObject);

    /**
     * 对一篇文章的多个对象进行情感分析
     * @param sTitle 文章标题，若不分析标题则传入空字符串
     * @param sContent 文章内容
     * @param sObjectRuleFile 存放分析对象等信息的配置文件的路径，具体格式参考data文件夹下的stConduct.xml
     * @return 分析结果
     */
    String ST_GetMultiObjectResult(String sTitle, String sContent, String sObjectRuleFile);

    /**
     * 获取文章的情感值
     * @param sContent 文章内容
     * @return 分析结果
     */
    String ST_GetSentencePoint(String sContent);

    /**
     * 退出
     */
    void ST_Exit();

    /**
     * 获取最后一条出错记录
     * @return
     */
    String ST_GetLastErrMsg();

    double ST_GetSentimentPoint(String sSentence);

    /**
     * 导入情感词典
     * @param sFilePath 词典的路径
     * @param bSaveDict 1:保存到词库，0：不保存到词库
     * @return 导入单词的个数
     */
    public int ST_ImportUserDict(String sFilePath, int bSaveDict);
    //

    /**
     * 删除单词
     * @param sWord 单词
     * @return 删除单词的个数
     */
    public int ST_DelUsrWord(String sWord);
}

class CLibrarySentimentLoader {

    private static final Logger LOG = LoggerFactory.getLogger("NLPIRLoader");
    static CLibrarySentiment instance;

    static {
        try {
            instance = (CLibrarySentiment) Native.loadLibrary("SentimentNew", CLibrarySentiment.class);
        } catch (Throwable e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
        if (instance == null) {
            try {
                instance = (CLibrarySentiment) Native.loadLibrary("lib/SentimentNew", CLibrarySentiment.class);
            } catch (Throwable e) {
                e.printStackTrace();
                LOG.error(e.getMessage());
            }
        }

        String nlpirdir = System.getProperty("nlpir.dir");
        if (nlpirdir == null || nlpirdir.isEmpty()) {
            // fallback to resources dir
            nlpirdir = NlpirCLibrary.class
                .getClassLoader()
                .getResource("nlpir")
                .getPath()
                .substring(1); // Remove the leading '/' in front of file path.
        }
        LOG.info("nlpir dir path: {}", nlpirdir);

        try {
            if (instance.ST_Init(nlpirdir, 1, "0") == 0) {
                String errormsg = instance.ST_GetLastErrMsg();
                System.out.println("Init fail \n" + errormsg);
            } else {
                System.out.println("Init succeed");
            }
            // int num = instance.ST_ImportUserDict(nlpirdir + "/sentilexicon.txt", 0);
            // System.out.printf("添加用户单词%d个\n", num);
        } catch (Throwable e) {
            // TODO: handle exception
            instance = null;
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
    }

    static CLibrarySentiment getlNlpirCLibrary() {
        return instance;
    }
}