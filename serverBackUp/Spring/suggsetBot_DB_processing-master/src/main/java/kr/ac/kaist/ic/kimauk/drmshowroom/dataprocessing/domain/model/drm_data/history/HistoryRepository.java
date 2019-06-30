package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.history;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface HistoryRepository extends CrudRepository<History, Long> {

    List<History> findAllByIdHistoryType(HistoryType historyType);
}
