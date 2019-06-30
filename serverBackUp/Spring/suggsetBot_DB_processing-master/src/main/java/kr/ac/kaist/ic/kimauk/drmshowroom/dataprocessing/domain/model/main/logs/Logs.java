package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.main.logs;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "logs")
public class Logs implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fileID")
    private long fileID;

    @Column(name = "logID")
    private long logID;

    @Column
    private String type;

    @Column
    private String json;

    @Column
    private long reg;

    public Logs() {

    }
}
