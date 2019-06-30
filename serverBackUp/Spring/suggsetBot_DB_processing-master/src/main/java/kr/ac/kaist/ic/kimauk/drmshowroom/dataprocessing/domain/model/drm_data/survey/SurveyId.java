package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.survey;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.subject.Subject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Embeddable;
import javax.persistence.OneToOne;
import java.io.Serializable;

@Embeddable
public class SurveyId  implements Serializable {

    @OneToOne
    private Subject subject;

    private int productNo;

    SurveyId(){}

    public SurveyId(Subject subject, int productNo) {
        this.subject = subject;
        this.productNo = productNo;
    }

    public Subject getSubject() {
        return subject;
    }

    public int getProductNo() {
        return productNo;
    }

    @Override
    public boolean equals(Object o) {

        if(o == null || o.getClass() != getClass())
            return false;

        if (this == o) return true;

        SurveyId surveyId = (SurveyId) o;

        return new EqualsBuilder()
                .append(getProductNo(), surveyId.getProductNo())
                .append(getSubject(), surveyId.getSubject())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getSubject())
                .append(getProductNo())
                .toHashCode();
    }
}
