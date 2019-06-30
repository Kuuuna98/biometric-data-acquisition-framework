package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.log;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.log.Log;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Service
public class LogSQLRepository {

    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    public long saveAll(final List<Log> logs){
        if(logs == null || logs.isEmpty())
            return -1;
        long result=0;
        StringBuilder build;
        try {
            QueryRunner run = new QueryRunner(dataSource);
            final String prefixSQL = "INSERT IGNORE INTO Log (timestamp, json, type, fileName, phoneNumber, sqliteId) VALUES ";
            final String valueSQLTemplate = "(%d, '%s','%s','%s','%s','%s')";
            build = new StringBuilder(prefixSQL);

            boolean forceToUpdate;
            for(int i=0; i< logs.size();){
                Log log = logs.get(i);
                String nextValues = String.format(valueSQLTemplate,
                        log.getId().getTimestamp(),
                        log.getId().getJson(),
                        log.getId().getType(),
                        log.getFileName(),
                        log.getPhoneNumber(),
                        log.getSqliteId());
                if((build.length()+ nextValues.length())*2 < Math.pow(2, 24) ){
                    build.append(nextValues).append(",");
                    forceToUpdate = false;
                    i++;
                }
                else
                    forceToUpdate = true;
                if(forceToUpdate || i <= logs.size()){
                    build.deleteCharAt(build.length()-1).append(";");
                    result += run.update(build.toString());
                    build = new StringBuilder(prefixSQL);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    private List<String> findAllDistinctType(){
        List<String> result = null;
        try {
            final String sql = "SELECT distinct type FROM Log";
            QueryRunner run = new QueryRunner(dataSource);
            result = run.query(sql, resultSet -> {
                List<String> result1 = new ArrayList<>();
                while(resultSet.next())
                    result1.add(resultSet.getString(0));
                return result1;
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
