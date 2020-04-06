package act.ring.cncert.restful.service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import act.ring.cncert.restful.repository.auth.ForeignSearch;
import act.ring.cncert.restful.repository.auth.ForeignSearchDao;

@Component
@Service
@Transactional
public class SettingService {

    @Autowired
    ForeignSearchDao foreignSearch;

    private static Logger logger = LoggerFactory.getLogger(SettingService.class);

    public boolean setForeignSearch(long userid, String words) {
        foreignSearch.save(new ForeignSearch(userid, words));
        return true;
    }

    public ForeignSearch getForeignSearch(long userid) {
        return foreignSearch.findForeignSearchByUserid(userid);
    }

    public ForeignSearch addForeignSearch(long userid, String words) {
        ForeignSearch record = getForeignSearch(userid);
        String allwords = words;
        ForeignSearch newrecord;
        if (record == null) {
            newrecord = new ForeignSearch(userid, allwords);
            logger.info("addForeignSearch: create new record, {}", newrecord);
        } else {
            if (words == null || words.isEmpty()) {
                allwords = record.getWords();
            } else {
                allwords = record.getWords() + words + ",";
            }
            newrecord = new ForeignSearch(record.getId(), userid, allwords);
            logger.info("addForeignSearch: update exists record, {}", newrecord);
        }
        foreignSearch.save(newrecord);
        return newrecord;
    }

    public ForeignSearch deleteForeignSearch(long userid, String words) {
        ForeignSearch record = getForeignSearch(userid);
        String allwords;
        ForeignSearch newrecord;
        if (words == null || words.isEmpty() || record == null) {
            return record;
        } else {
            allwords = record.getWords().replace(words + ",", "");
            newrecord = new ForeignSearch(record.getId(), userid, allwords);
            logger.info("deleteForeignSearch: update record: {}" + record);
            foreignSearch.save(newrecord);
            return newrecord;
        }
    }
}