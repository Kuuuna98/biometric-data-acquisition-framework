package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.history;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class HistoryId implements Serializable {

    @Enumerated(EnumType.STRING)
    private HistoryType historyType;

    private String value;

    HistoryId(){}

    public HistoryId(HistoryType historyType, String value) {
        this.historyType = historyType;
        this.value = value;
    }

    public HistoryType getHistoryType() {
        return historyType;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass())
            return false;

        if (this == o) return true;

        HistoryId other = (HistoryId) o;
        return new EqualsBuilder().append(getHistoryType(), other.getHistoryType()).append(getValue(), other.getValue()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getHistoryType()).append(getValue()).toHashCode();
    }
}
