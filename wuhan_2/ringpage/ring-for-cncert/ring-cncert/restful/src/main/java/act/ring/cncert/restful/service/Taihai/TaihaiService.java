package act.ring.cncert.restful.service.Taihai;

import org.apache.commons.lang3.time.DateUtils;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import act.ring.cncert.data.bean.Taihai;
import act.ring.cncert.restful.service.source.SourceService;

/**
 * Created by Machenike on 2017/12/8.
 */

public class TaihaiService extends SourceService {

    private static String SearchWords[] = {"台湾", "台海", "九二共识", "台独", "两岸", "国台办"};

    //测试代码
    public static void main(String[] args) {
        SourceService sourceService = new SourceService();
        Date now = new Date();
        Date end = new Date();
        try {
            now = DateUtils.parseDate("2017/11/05 00:00:00", "yyyy/MM/dd HH:mm:ss");
            end = DateUtils.parseDate("2017/12/05 00:00:00", "yyyy/MM/dd HH:mm:ss");
        } catch (Exception e) {
            e.printStackTrace();
        }
        TaihaiService taihaiService = new TaihaiService();
//        System.out.println(taihaiService.findTaihaiNews(now,end,1000));
        PrintStream ps = null;
        try {
            ps = new PrintStream(new FileOutputStream("E:\\台湾问题.txt"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader
            reader =
            new BufferedReader(
                new InputStreamReader(
                    ClassLoader.getSystemResourceAsStream("Taihai/KeywordsPoint")));
        String content = "";
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                content += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        HashMap<String, Integer> kmap = new HashMap<>();
        List<Taihai> taihaiList = new ArrayList<>();
        try {
            JSONObject keywords = new JSONObject(content);
            System.out.println(keywords);
            for (String keyword : keywords.keySet()) {
                kmap.put(keyword, Integer.parseInt(keywords.get(keyword).toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (now.before(end)) {
            System.out.println(now);
            Date ago = DateUtils.addDays(now, -1);
            List<Taihai> Taiwan = new ArrayList<>();
            for (String keyword : SearchWords) {
                Taiwan.addAll(sourceService.findTaihaiNews(ago, now, keyword, 1000, false).v2());
            }
            for (Taihai tainews : Taiwan) {
                String Text = tainews.getText();
                String Title = tainews.getTitle();
                float TextPoint = 0;
                float TitlePoint = 0;
                for (String keyword : kmap.keySet()) {
                    if (Text != null) {
                        TextPoint += (Text.split(keyword).length - 1) * kmap.get(keyword);
                    }
                    TitlePoint += (Title.split(keyword).length - 1) * kmap.get(keyword);
                }
                TitlePoint = TitlePoint / Title.length();
                if (Text != null) {
                    TextPoint = TextPoint / Text.length();
                }
                Taihai taihai = tainews;
                taihai.setTitlePoint(TitlePoint);
                taihai.setTextPoint(TextPoint);
                taihaiList.add(taihai);
            }
            now = DateUtils.addDays(now, 1);
        }
        Collections.sort(taihaiList, new Comparator<Taihai>() {
            public int compare(Taihai arg0, Taihai arg1) {
                if (arg0.getTextPoint() != arg1.getTextPoint()) {
                    if (arg0.getTitlePoint() > arg1.getTitlePoint()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    if (arg0.getTitlePoint() != arg1.getTitlePoint()) {
                        if (arg0.getTitlePoint() > arg1.getTitlePoint()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        return 0;
                    }
                }
            }
        });
        for (Taihai news : taihaiList) {
            if (news.getTitlePoint() > 0.7 || news.getTextPoint() > 0.09) {
                ps.println(
                    "TitlePoint: " + news.getTitlePoint() + ",TextPoint: " + news.getTextPoint());
                System.out
                    .println("TitlePoint: " + news.getTitlePoint() + ",TextPoint: " + news
                        .getTextPoint());
                ps.println(news.getId());
                ps.println(news.getTitle());
                ps.println(news.getText());
                ps.println();
            }
        }
        ps.close();
    }

    public List<Taihai> findTaihaiNews(Date from, Date to, int size) {
        SourceService sourceService = new SourceService();
        BufferedReader
            reader =
            new BufferedReader(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream ("Taihai/KeywordsPoint")));
        String content = "";
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                content += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        HashMap<String, Integer> kmap = new HashMap<>();
        List<Taihai> taihaiList = new ArrayList<>();
        try {
            JSONObject keywords = new JSONObject(content);
            for (String keyword : keywords.keySet()) {
                kmap.put(keyword, Integer.parseInt(keywords.get(keyword).toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (from.before(to)) {
            Date ago = DateUtils.addDays(from, -1);
            List<Taihai> Taiwan = new ArrayList<>();
            for (String keyword : SearchWords) {
                Taiwan.addAll(sourceService.findTaihaiNews(ago, from, keyword, 1000, false).v2());
            }
            for (Taihai tainews : Taiwan) {
                String Text = tainews.getText();
                String Title = tainews.getTitle();
                float TextPoint = 0;
                float TitlePoint = 0;
                for (String keyword : kmap.keySet()) {
                    if (Text != null) {
                        TextPoint += (Text.split(keyword).length - 1) * kmap.get(keyword);
                    }
                    TitlePoint += (Title.split(keyword).length - 1) * kmap.get(keyword);
                }
                TitlePoint = TitlePoint / Title.length();
                if (Text != null) {
                    TextPoint = TextPoint / Text.length();
                }
                if (TitlePoint > 0.7 || TextPoint > 0.09) {
                    Taihai taihai = tainews;
                    taihai.setTitlePoint(TitlePoint);
                    taihai.setTextPoint(TextPoint);
                    taihaiList.add(taihai);
                }
            }
            Collections.sort(taihaiList, new Comparator<Taihai>() {
                public int compare(Taihai arg0, Taihai arg1) {
                    if (arg0.getTextPoint() != arg1.getTextPoint()) {
                        if (arg0.getTextPoint() > arg1.getTextPoint()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else {
                        if (arg0.getTitlePoint() != arg1.getTitlePoint()) {
                            if (arg0.getTitlePoint() > arg1.getTitlePoint()) {
                                return -1;
                            } else {
                                return 1;
                            }
                        } else {
                            return 0;
                        }
                    }
                }
            });
            from = DateUtils.addDays(from, 1);
        }
        if (size < taihaiList.size()) {
            return taihaiList.subList(0, size);
        } else {
            return taihaiList;
        }
    }
}
