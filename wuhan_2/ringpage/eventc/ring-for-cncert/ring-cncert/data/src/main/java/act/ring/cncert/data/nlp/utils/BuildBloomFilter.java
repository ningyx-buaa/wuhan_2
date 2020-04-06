package act.ring.cncert.data.nlp.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Imported from ring-event, 2017/11/08.
 *
 * Created by ASUS on 2015/11/14.
 */
public class BuildBloomFilter {

    public BloomFilter BuildBloomFilter(int TypeNum) {
        //建立Eventword bloomfilter 1-10
        BloomFilter EventTypeWordFilter1 = new BloomFilter();
        String filepath = null;
        if (TypeNum == 0) {
            filepath = "InputFiles/0.txt";
        }
        if (TypeNum == 1) {
            filepath = "InputFiles/1.txt";
        }
        if (TypeNum == 2) {
            filepath = "InputFiles/2.txt";
        }
        if (TypeNum == 3) {
            filepath = "InputFiles/3.txt";
        }
        if (TypeNum == 4) {
            filepath = "InputFiles/4.txt";
        }
        if (TypeNum == 5) {
            filepath = "InputFiles/5.txt";
        }
        if (TypeNum == 6) {
            filepath = "InputFiles/6.txt";
        }
        if (TypeNum == 7) {
            filepath = "InputFiles/7.txt";
        }
        if (TypeNum == 8) {
            filepath = "InputFiles/8.txt";
        }
        if (TypeNum == 9) {
            filepath = "InputFiles/9.txt";
        }
        if (TypeNum == 10) {
            filepath = "InputFiles/10.txt";
        }
        if (TypeNum == 11) {
            filepath = "InputFiles/11.txt";
        }
        if (TypeNum == 12) {
            filepath = "InputFiles/12.txt";
        }
        if (TypeNum == 13) {
            filepath = "InputFiles/13.txt";
        }
        if (TypeNum == 14) {
            filepath = "InputFiles/14.txt";
        }
        if (TypeNum == 16) {
            filepath = "InputFiles/16.txt";
        }
        if (TypeNum == 17) {
            filepath = "InputFiles/17.txt";
        }
        if (TypeNum == 18) {
            filepath = "InputFiles/18.txt";
        }
        if (TypeNum == 19) {
            filepath = "InputFiles/19.txt";
        }

        getClass().getResourceAsStream(filepath);

        BufferedReader reader1 = null;

        try {
            //以行为单位读取微博样本文件内容，一次读一整行：
            reader1 =
                new BufferedReader(new InputStreamReader(
                    getClass().getClassLoader().getResourceAsStream(filepath)));
            String tempString = null;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader1.readLine()) != null) {
                //System.out.println(tempString);
                EventTypeWordFilter1.add(tempString);
            }
            reader1.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader1 != null) {
                try {
                    reader1.close();
                } catch (IOException e1) {
                }
            }
        }
        return EventTypeWordFilter1;
    }
}
