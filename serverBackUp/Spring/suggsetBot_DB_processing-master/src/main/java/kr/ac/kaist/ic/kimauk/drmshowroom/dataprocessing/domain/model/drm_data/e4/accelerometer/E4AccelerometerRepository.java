package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.e4.accelerometer;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.SubjectTimestampId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface E4AccelerometerRepository extends CrudRepository<E4Accelerometer, SubjectTimestampId> {
}
