package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.survey;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SurveyRepository extends CrudRepository<Survey, SurveyId> {
}
