package act.ring.cncert.data.bean;

/**
 * Created by lijun on 2017/11/20.
 */
public class Opinion extends Element {

    private String holder;
    private String opinion;
    private int sensitive;
    private int importance;
    private int recommend;
    private int risk;
    private String chain;
    private float sim;
    private float novelty;
    private int chainlen;


    public Opinion(String id) {
        super(id);
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }


    public String getChain() {
        return this.chain;
    }
    public void setChain(String chain) {
        this.chain = chain;
    }

    public float getSim() {
        return this.sim;
    }

    public void setSim(float sim) {
        this.sim = sim;
    }

    public float getNovelty() {
        return this.novelty;
    }

    public void setNovelty(float novelty) {
        this.novelty = novelty;
    }

    public float getChainlen() {
        return this.chainlen;
    }

    public void setChainlen(int chainlen) {
        this.chainlen = chainlen;
    }

    public String getOpinion() {
        return opinion;
    }

    public void setOpinion(String opinion) {
        this.opinion = opinion;
    }

    public int getSensitive() {
        return sensitive;
    }

    public void setSensitive(int sensitive) {
        this.sensitive = sensitive;
    }

    public int getImportance() {
        return this.importance;
    }

    public void setImportance(int importance) {
        this.importance = importance;
    }

    public int getRecommend() {
        return recommend;
    }

    public void setRecommend(int recommend) {
        this.recommend = recommend;
    }

    public int getRisk() {
        return risk;
    }

    public void setRisk(int risk) {
        this.risk = risk;
    }

}
