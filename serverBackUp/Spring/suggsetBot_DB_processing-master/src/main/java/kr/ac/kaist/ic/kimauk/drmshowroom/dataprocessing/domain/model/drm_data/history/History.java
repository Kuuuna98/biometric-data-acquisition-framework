package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.history;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(indexes = {
        @Index(name = "index_historyType",  columnList="historyType", unique = false)
})
public class History implements Serializable {
    @EmbeddedId
    private HistoryId id;

    History(){}

    public History(HistoryType historyType, String value) {
        this.id = new HistoryId(historyType, value);
    }

    public HistoryId getId() {
        return id;
    }

    public HistoryType getHistoryType() {
        return getId().getHistoryType();
    }

    public String getValue() {
        return getId().getValue();
    }

    @Override
    public boolean equals(Object o) {
        if( o == null || o.getClass() != getClass())
            return false;
        if (this == o) return true;
        History history = (History) o;
        return history.getId().equals(getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
