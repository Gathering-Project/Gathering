package nbc_final.gathering.common.alarmconfig;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlarmMessageRepository extends MongoRepository<AlarmMessage, String> {
}
