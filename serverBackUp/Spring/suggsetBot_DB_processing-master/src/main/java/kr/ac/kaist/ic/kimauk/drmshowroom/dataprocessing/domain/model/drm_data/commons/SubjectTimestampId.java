package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.commons;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject.Subject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Embeddable;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Embeddable
public class SubjectTimestampId implements Serializable{
    private Long timestamp;
    @OneToOne
    private Subject subject;

    SubjectTimestampId(){}

    public SubjectTimestampId(Subject subject, Long timestamp) {
        this.timestamp = timestamp;
        this.subject = subject;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Subject getSubject() {
        return subject;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || getClass() != o.getClass())
            return false;

        if (this == o)
            return true;

        SubjectTimestampId other = (SubjectTimestampId) o;
        return new EqualsBuilder().append(getTimestamp(), other.getTimestamp()).append(getSubject(), other.getSubject()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getSubject()).append(getTimestamp()).toHashCode();
    }
}
