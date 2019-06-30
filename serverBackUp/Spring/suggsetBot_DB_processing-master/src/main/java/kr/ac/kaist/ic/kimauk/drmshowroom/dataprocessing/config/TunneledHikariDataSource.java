package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.config;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.InitializingBean;

final class TunneledHikariDataSource extends HikariDataSource implements InitializingBean {
    private static Session session;

    static Session getSession() {
        return session;
    }


    public int getPort(){
        if(System.getProperty("os.name").toUpperCase().contains("LINUX"))
            return 3306;
        else
            return 4321;
    }

    public void afterPropertiesSet() {
        if(System.getProperty("os.name").toUpperCase().contains("LINUX"))
            return;

        if(getSession() != null)
            return;
        JSch jsch = new JSch();
        try {
            session  = jsch.getSession("ubuntu", "suggestbot.kse.smoon.kr", 22);
            session.setPassword("ubuntu1!");
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            session.setPortForwardingL(4321, "localhost", 3306);
        } catch (JSchException e) {
            e.printStackTrace();
        }
    }
}
