package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.application.updatelog;

import kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.model.drm_data.log.Log;
import org.json.JSONObject;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class LogsCreatorCallable implements Callable<SQLiteLogs> {


    private static String DB_FILE_REGEX = "(sensors_data|audio_guide).db";
    private static String SQLITE_SQL = "SELECT * FROM log";
    private final File zipFile;
    private File dbFile = null;
    private String jsonString = null;

    public LogsCreatorCallable(File zipFile) {
        this.zipFile = zipFile;
    }

    @Override
    public SQLiteLogs call(){
        List<Log> result = null;
        if(initDBFileAndJsonString()){
            result = new ArrayList<>();
            String phoneNumber = getPhoneNumber(jsonString,"phone");
            result  = getLogs(dbFile, zipFile.getName(), phoneNumber);
            if(result == null){
                try {
                    File recoveredDbFile = File.createTempFile("recovered"+zipFile.getName(),"");
                    recoverDB(dbFile, recoveredDbFile);
                    result = getLogs(recoveredDbFile, zipFile.getName(), phoneNumber);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            dbFile.delete();
        }
        SQLiteLogs sqLiteLogs = new SQLiteLogs(zipFile, result);
        return sqLiteLogs;
    }

    private boolean initDBFileAndJsonString(){
        try {
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry;
            while((zipEntry = zis.getNextEntry()) != null){
                String fileName = zipEntry.getName();
                int len;
                if(fileName.matches(DB_FILE_REGEX)){
                    dbFile = File.createTempFile(fileName,"");
                    dbFile.deleteOnExit();
                    FileOutputStream fos = new FileOutputStream(dbFile);
                    while ((len = zis.read(buffer)) > 0)
                        fos.write(buffer, 0, len);
                    fos.close();
                }else if(fileName.matches("info.json")){
                    jsonString = "";
                    while (zis.read(buffer) > 0)
                        jsonString += new String(buffer);
                }
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException e) {
            e.printStackTrace();
            dbFile = null;
        }
        return dbFile != null;
    }

    //phoneNumber.matches("010[0-9]{8}"))

    public void recoverDB(File originalDB, File newDB) {
        try {
            String line;
            Process p = Runtime.getRuntime().exec(new String[]{
                    "/bin/sh",
                    "-c",
                    "sqlite3 "+originalDB+" .dump | sqlite3 "+ newDB});

            BufferedReader bri = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while ((line = bri.readLine()) != null)
                System.out.println(line);
            bri.close();
            while ((line = bre.readLine()) != null)
                System.out.println(line);
            bre.close();
            p.waitFor();
            System.out.println("Done.");
        }
        catch (Exception err) {
            err.printStackTrace();
        }
    }

    private List<Log> getLogs(File sqliteDB, String fileName, final String phoneNumber){
        List<Log> result = new ArrayList<>();
        Connection conn = null;
        try {
            String url = "jdbc:sqlite:"+sqliteDB.toString();
            conn = DriverManager.getConnection(url);
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(SQLITE_SQL);
            while (rs.next()) {
                long sqliteId = rs.getLong("_id");
                String type = rs.getString("type");
                String json = rs.getString("json");
                long timestamp = rs.getLong("reg");
                result.add(new Log(fileName, phoneNumber, sqliteId, type, json, timestamp));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            result = null;
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private String getPhoneNumber(String jsonString, String jsonTag){
        String result = null;
        if(jsonString != null && !jsonString.isEmpty()) {
            JSONObject jsonObject = new JSONObject(jsonString);
            result = jsonObject.getString(jsonTag);
            result = result.replaceAll("[^0-9]", "").trim();
            for(int i = result.length(); i< 8; i++)
                result += "?";
            result = "010"+result;
        }
        return result;
    }


}
