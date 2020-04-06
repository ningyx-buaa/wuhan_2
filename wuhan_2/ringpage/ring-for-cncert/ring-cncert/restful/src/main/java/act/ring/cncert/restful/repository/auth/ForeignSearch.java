package act.ring.cncert.restful.repository.auth;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "foreign_words")
public class ForeignSearch extends IdEntity {

    private long userid;
    private String words;

    public ForeignSearch() {
    }

    public ForeignSearch(long userid, String words) {
        this.userid = userid;
        this.words = words;
    }

    public ForeignSearch(long id, long userid, String words) {
        super(id);
        this.userid = userid;
        this.words = words;
    }

    public long getUserid() {
        return userid;
    }

    public void setUserid(long userid) {
        this.userid = userid;
    }

    public String getWords() {
        return words;
    }

    public void setWords(String words) {
        this.words = words;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
