package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.smartphone.accelerometer;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.SubjectTimestampId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SmartphoneAccelerometerRepository extends CrudRepository<SmartphoneAccelerometer, SubjectTimestampId> {
}
