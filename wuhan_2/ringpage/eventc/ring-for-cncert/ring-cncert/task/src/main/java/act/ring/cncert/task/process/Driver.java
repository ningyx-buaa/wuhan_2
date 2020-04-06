package act.ring.cncert.task.process;

import java.util.LinkedList;
import java.util.List;

public class Driver {

    public static void main(String[] args) throws InterruptedException {
        List<Processor> ps = new LinkedList<>();
        for (int i = 0; i < 3; ++i) {
            ps.add(new SearchedDataProcessor());
        }
        for (int i = 0; i < 3; ++i) {
            ps.add(new TerrenProcessor());
        }
        for (int i = 0; i < 9; ++i) {
            ps.add(new NewsProcessor());
        }
        for (int i = 0; i < 9; ++i) {
            ps.add(new WeiboEventProcessor());
        }
        for (int i = 0; i < 3; ++i) {
            ps.add(new ForeignProcessor());
        }

        for (Processor proc: ps) {
            proc.start();
        }
        for (Processor proc: ps) {
            proc.join();
        }
    }
}
