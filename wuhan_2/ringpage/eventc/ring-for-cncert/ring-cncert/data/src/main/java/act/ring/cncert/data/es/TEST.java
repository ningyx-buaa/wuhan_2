package act.ring.cncert.data.es;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import act.ring.cncert.data.bean.Opinion;


public class TEST {

    public static void main(String args[]) {
        Elasticsearch elasticsearch = new Elasticsearch(Opinion.class, "opinions");
        List<Map<String, Object>> res_list = new ArrayList<>();
        String dt = "0";
        res_list = elasticsearch.getInfo(dt, 100);
        System.out.println(res_list.size());
        for (int i = 0; i < res_list.size(); i++) {
            System.out.println(res_list.get(i));
        }
    }
}
