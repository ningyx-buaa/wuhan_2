package act.ring.cncert.data.nlp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Imported from ring-event, 2017/11/20.
 *
 * Created by lijun on 2017/5/22.
 */
public class BloomFilterHelper {

    private static String ext = ".txt";
    private String file = "";

    public static void main(String args[]) {
        BloomFilterHelper bfhelper = new BloomFilterHelper();
        String fpath = "TaiwanExperts/";
//        BloomFilter Event2TypeWordFilter[] = new BloomFilter[5];
//        for(int i=0;i<=0;i++){
//            Event2TypeWordFilter[i] = bfhelper.BloomFilterHelper(i, fpath);
//        }

//        System.out.println(Event2TypeWordFilter[0].contains("蒋经国"));
//        System.out.println(Event2TypeWordFilter[0].contains("陈水扁"));
    }

    public BloomFilter BloomFilterHelper(int num, String FilePath) {
        BloomFilter bf = new BloomFilter();
        file = FilePath + String.valueOf(num) + ext;
//        File CellFile = new File(file);
        BufferedReader reader1 = null;
        try {
//            reader1 = new BufferedReader(new FileReader(CellFile));
            reader1 =
                new BufferedReader(
                    new InputStreamReader(getClass().getClassLoader().getResourceAsStream(file)));
            String tempstring = "";
            while ((tempstring = reader1.readLine()) != null) {
                bf.add(tempstring);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader1 != null) {
                try {
                    reader1.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bf;
    }
}
