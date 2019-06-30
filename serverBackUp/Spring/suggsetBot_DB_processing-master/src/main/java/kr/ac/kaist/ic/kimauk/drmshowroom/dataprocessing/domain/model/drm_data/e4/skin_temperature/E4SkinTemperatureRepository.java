package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.e4.skin_temperature;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons.SubjectTimestampId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface E4SkinTemperatureRepository extends CrudRepository<E4SkinTemperature, SubjectTimestampId> {
}
