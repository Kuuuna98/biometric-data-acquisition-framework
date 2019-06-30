package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class Subject implements Serializable {
    @Id
    private String phoneNumber;

    private boolean isReverseGuideDirection;

    Subject(){}

    public Subject(String phoneNumber, boolean isReverseGuideDirection) {
        this.phoneNumber = phoneNumber;
        this.isReverseGuideDirection = isReverseGuideDirection;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public boolean isReverseGuideDirection() {
        return isReverseGuideDirection;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || getClass() != o.getClass())
            return false;

        if (this == o)
            return true;

        Subject other = (Subject) o;
        return new EqualsBuilder().append(getPhoneNumber(), other.getPhoneNumber()).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getPhoneNumber()).toHashCode();
    }
}
