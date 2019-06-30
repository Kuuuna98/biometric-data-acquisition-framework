package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.survey;

import javax.persistence.*;
import java.io.Serializable;

@Entity
public class Survey{

    @EmbeddedId
    private SurveyId id;

    @Column(nullable = false)
    private int a1;
    @Column(nullable = false)
    private int a2;
    @Column(nullable = false)
    private int a3;
    @Column(nullable = false)
    private int a4;
    @Column(nullable = false)
    private int a5;
    @Column(nullable = false)
    private int a6;
    @Column(nullable = true)
    private int a7;
    @Column(nullable = false)
    private int a8;

    public Survey(){}

    public SurveyId getId() {
        return id;
    }

    public int getA1() {
        return a1;
    }

    public int getA2() {
        return a2;
    }

    public int getA3() {
        return a3;
    }

    public int getA4() {
        return a4;
    }

    public int getA5() {
        return a5;
    }

    public int getA6() {
        return a6;
    }

    public int getA7() {
        return a7;
    }

    public int getA8() {
        return a8;
    }

    @Override
    public boolean equals(Object o) {

        if(o == null || o.getClass() != getClass())
            return false;

        if (this == o) return true;

        Survey survey = (Survey) o;

        return getId().equals(survey.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}
