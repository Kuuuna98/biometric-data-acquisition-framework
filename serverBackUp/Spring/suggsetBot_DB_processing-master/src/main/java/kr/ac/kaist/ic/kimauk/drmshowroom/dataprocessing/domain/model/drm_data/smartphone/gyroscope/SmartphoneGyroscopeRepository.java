package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.smartphone.gyroscope;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.SubjectTimestampId;
import org.springframework.data.repository.CrudRepository;

public interface SmartphoneGyroscopeRepository extends CrudRepository<SmartphoneGyroscope, SubjectTimestampId> {
}
