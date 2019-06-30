package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.guide;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject.Subject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Embeddable;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Embeddable
public class GuideId implements Serializable {

    private int productNo;

    @OneToOne
    private Subject subject;

    private boolean isIntro;

    GuideId(){}

    public GuideId(Subject subject, int productNo, boolean isIntro) {
        this.productNo = productNo;
        this.subject = subject;
        this.isIntro = isIntro;
    }

    public int getProductNo() {
        return productNo;
    }

    public Subject getSubject() {
        return subject;
    }

    public boolean isIntro() {
        return isIntro;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass())
            return false;

        if (this == o) return true;

        GuideId other = (GuideId) o;

        return new EqualsBuilder()
                .append(getProductNo(), other.getProductNo())
                .append(getSubject(), other.getSubject())
                .append(isIntro(), other.isIntro())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getProductNo())
                .append(getSubject())
                .append(isIntro())
                .toHashCode();
    }
}
