package act.ring.cncert.data.nlp.nlpir;

import com.sun.jna.Library;
import com.sun.jna.Native;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Imported from ring-event, 2017/11/08.
 */
public interface NlpirCLibrary extends Library {

    NlpirCLibrary instance = NlpirCLibraryLoader.getlNlpirCLibrary();

    public String NLPIR_WordFreqStat(String sText);

    public int NLPIR_AddUserWord(String sWord);

    public int NLPIR_DelUsrWord(String sWord);

    public void NLPIR_Exit();

    public String NLPIR_GetFileKeyWords(String sLine, int nMaxKeyLimit,
                                        boolean bWeightOut);

    public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit,
                                    boolean bWeightOut);

    public String NLPIR_GetLastErrorMsg();

    public String NLPIR_GetNewWords(String sLine, int nMaxKeyLimit,
                                    boolean bWeightOut);

    public int NLPIR_Init(String sDataPath, int encoding, String sLicenceCode);

    public String NLPIR_ParagraphProcess(String sSrc, int bPOSTagged);

    public int NLPIR_SaveTheUsrDic();

    // 导入用户自定义词典：自定义词典路径，bOverwrite=true表示替代当前的自定义词典，false表示添加到当前自定义词典后
    public int NLPIR_ImportUserDict(String sFilename, boolean bOverwrite);

}

class NlpirCLibraryLoader {

    private static final Logger LOG = LoggerFactory.getLogger("NLPIRLoader");
    static NlpirCLibrary instance;

    static {
        try {
            instance = (NlpirCLibrary) Native.loadLibrary("NLPIR", NlpirCLibrary.class);
        } catch (Throwable e) {
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
        if (instance == null) {
            try {
                instance = (NlpirCLibrary) Native.loadLibrary("lib/NLPIR", NlpirCLibrary.class);
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
            if (instance.NLPIR_Init(nlpirdir, 1, "0") == 0) {
                String nativeBytes = instance.NLPIR_GetLastErrorMsg();
                System.out.println("Init fail \n" + nativeBytes);
            } else {
                System.out.println("Init succeed");
            }
        } catch (Throwable e) {
            // TODO: handle exception
            instance = null;
            e.printStackTrace();
            LOG.error(e.getMessage());
        }
    }

    static NlpirCLibrary getlNlpirCLibrary() {
        return instance;
    }
}