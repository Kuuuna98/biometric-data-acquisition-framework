package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.log;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class LogId implements Serializable, Comparable<LogId> {
    private long timestamp;

    private String json;

    private String type;

    LogId(){}

    public LogId(long timestamp, String json, String type) {
        this.timestamp = timestamp;
        this.json = json;
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getType() {
        return type;
    }

    public String getJson() {
        return json;
    }

    @Override
    public boolean equals(Object o) {
        if(this == null || this.getClass() != o.getClass())
            return false;

        if (this == o) return true;

        LogId other = (LogId) o;

        return new EqualsBuilder().append(getTimestamp(), other.getTimestamp()).append(getType(), other.getType()).append(getJson(), other.getJson()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getTimestamp()).append(getJson()).append(getType()).toHashCode();
    }

    @Override
    public int compareTo(LogId o) {
        return new CompareToBuilder().append(getTimestamp(), o.getTimestamp()).append(getType(), o.getType()).append(getType(), o.getType()).toComparison();
    }
}
