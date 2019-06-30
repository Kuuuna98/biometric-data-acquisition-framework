package kr.ac.kaist.ic.kimauk.drmshowroom.dataprocessing.domain.service;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Service
public class CSVImporter {


    @Autowired
    @Qualifier("dataSource")
    private DataSource dataSource;

    private List<String> getPhoneNumbers() throws SQLException {
        QueryRunner run = new QueryRunner(dataSource);

        String PHONE_NUMBERS_SQL = "SELECT subject_phoneNumber\n" +
                "FROM drm_data._Survey \n" +
                "GROUP BY subject_phoneNumber\n" +
                "HAVING count(*) = 30";

        List<String> phoneNumbers = run.query(PHONE_NUMBERS_SQL, resultSet -> {
            List<String> result = new ArrayList<>();
            while (resultSet.next())
                result.add(resultSet.getString(1));
            return result;
        });
        return phoneNumbers;
    }


    private List<SQLResultData> getSQLResultDatas(String aql) throws SQLException {
        QueryRunner run = new QueryRunner(dataSource);
        return run.query(aql.toString(),
                resultSet -> {
                    List<SQLResultData> result = new ArrayList<>();
                    while (resultSet.next()) {
                        SQLResultDataBuilder builder = new SQLResultDataBuilder();
                        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
                        for (int i = 1; i <= resultSetMetaData.getColumnCount(); i++) {
                            switch (i) {
                                case 1:
                                    builder.PhoneNumber(resultSet.getString(i));
                                    break;
                                case 2:
                                    builder.ProductNo(resultSet.getInt(i));
                                    break;
                                default:
                                    builder.add(resultSetMetaData.getColumnLabel(i), resultSet.getString(i));
                                    break;
                            }
                        }
                        result.add(builder.Build());
                    }
                    return result;

                }
        );
    }

    private List<SQLResultData> getSQLResultDatas(String sensorTableName, String phoneNumber, String sql_select) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("SELECT Guide.subject_phoneNumber, Guide.productNo, \n").
                append(sql_select.replaceAll("TO_BE_REPLACE", sensorTableName))
                .append(String.format("\n" +
                                "FROM drm_data.Guide, %s\n" +
                                "\n" +
                                "WHERE \n" +
                                "Guide.subject_phoneNumber = %s.subject_phoneNumber\n" +
                                "and Guide.subject_phoneNumber = '%s'\n" +
                                "and Guide.startTime < timestamp and timestamp < Guide.endTime\n" +
                                "GROUP BY Guide.subject_phoneNumber, Guide.productNo",
                        sensorTableName, sensorTableName, phoneNumber));
        return getSQLResultDatas(stringBuilder.toString());
    }

    private List<SQLResultData> getSQLResultDatasForXYZ(String sensorTableName, String phoneNumber) throws SQLException {
        String sql = "min(sqrt(x*x+y*y+z*z)) as TO_BE_REPLACE_VEC_MIN, max(sqrt(x*x+y*y+z*z)) as TO_BE_REPLACE_VEC_MAX, avg(sqrt(x*x+y*y+z*z)) as TO_BE_REPLACE_VEC_ACG, std(sqrt(x*x+y*y+z*z)) as TO_BE_REPLACE_VEC_STD,\n" +
                "min(x) as TO_BE_REPLACE_X_MIN, max(x) as TO_BE_REPLACE_X_MAX, avg(x) as TO_BE_REPLACE_X_AVG, std(x) as TO_BE_REPLACE_X_STD,\n" +
                "min(y) as TO_BE_REPLACE_Y_MIN, max(y) as TO_BE_REPLACE_Y_MAX, avg(y) as TO_BE_REPLACE_Y_AVG, std(y) as TO_BE_REPLACE_Y_STD,\n" +
                "min(z) as TO_BE_REPLACE_Z_MIN, max(z) as TO_BE_REPLACE_Z_MAX, avg(z) as TO_BE_REPLACE_Z_AVG, std(z) as TO_BE_REPLACE_Z_STD\n";
        sql = "count(TO_BE_REPLACE.x), count(TO_BE_REPLACE.y), count(TO_BE_REPLACE.z)\n";
        return getSQLResultDatas(sensorTableName, phoneNumber, sql);
    }

    private List<SQLResultData> getSQLResultDatasForMultyXYZ(String sensorTableName, String phoneNumber) throws SQLException {
        String sql = "min(sqrt(accelerometer_x*accelerometer_x+accelerometer_y*accelerometer_y+accelerometer_z*accelerometer_z))  as TO_BE_REPLACE_ACC_VEC_MIN, \n" +
                " max(sqrt(accelerometer_x*accelerometer_x+accelerometer_y*accelerometer_y+accelerometer_z*accelerometer_z)) as TO_BE_REPLACE_ACC_VEC_MAX, \n" +
                " avg(sqrt(accelerometer_x*accelerometer_x+accelerometer_y*accelerometer_y+accelerometer_z*accelerometer_z)) as TO_BE_REPLACE_ACC_VEC_AVG, \n" +
                " std(sqrt(accelerometer_x*accelerometer_x+accelerometer_y*accelerometer_y+accelerometer_z*accelerometer_z)) as TO_BE_REPLACE_ACC_VEC_STD, \n" +
                " \n" +
                " min(accelerometer_x) as TO_BE_REPLACE_ACC_X_MIN,  max(accelerometer_x) as TO_BE_REPLACE_ACC_X_MAX, avg(accelerometer_x) as TO_BE_REPLACE_ACC_X_AVG,std(accelerometer_x) as TO_BE_REPLACE_ACC_X_STD,\n" +
                " min(accelerometer_y) as TO_BE_REPLACE_ACC_Y_MIN,  max(accelerometer_y) as TO_BE_REPLACE_ACC_Y_MAX, avg(accelerometer_y) as TO_BE_REPLACE_ACC_Y_AVG,std(accelerometer_y) as TO_BE_REPLACE_ACC_Y_STD,\n" +
                " min(accelerometer_z) as TO_BE_REPLACE_ACC_Z_MIN,  max(accelerometer_z) as TO_BE_REPLACE_ACC_Z_MAX, avg(accelerometer_z) as TO_BE_REPLACE_ACC_Z_AVG,std(accelerometer_z) as TO_BE_REPLACE_ACC_Z_STD,\n" +
                " \n" +
                " min(sqrt(gyroscope_x*gyroscope_x+gyroscope_y*gyroscope_y+gyroscope_z*gyroscope_z))  as TO_BE_REPLACE_GYRO_VEC_MIN, \n" +
                " max(sqrt(gyroscope_x*gyroscope_x+gyroscope_y*gyroscope_y+gyroscope_z*gyroscope_z)) as TO_BE_REPLACE_GYRO_VEC_MAX, \n" +
                " avg(sqrt(gyroscope_x*gyroscope_x+gyroscope_y*gyroscope_y+gyroscope_z*gyroscope_z)) as TO_BE_REPLACE_GYRO_VEC_AVG, \n" +
                " std(sqrt(gyroscope_x*gyroscope_x+gyroscope_y*gyroscope_y+gyroscope_z*gyroscope_z)) as TO_BE_REPLACE_GYRO_VEC_STD, \n" +
                " \n" +
                " min(gyroscope_x) as TO_BE_REPLACE_GYRO_X_MIN,  max(gyroscope_x) as TO_BE_REPLACE_GYRO_X_MAX, avg(gyroscope_x) as TO_BE_REPLACE_GYRO_X_AVG,std(gyroscope_x) as TO_BE_REPLACE_GYRO_X_STD,\n" +
                " min(gyroscope_y) as TO_BE_REPLACE_GYRO_Y_MIN,  max(gyroscope_y) as TO_BE_REPLACE_GYRO_Y_MAX, avg(gyroscope_y) as TO_BE_REPLACE_GYRO_Y_AVG,std(gyroscope_y) as TO_BE_REPLACE_GYRO_Y_STD,\n" +
                " min(gyroscope_z) as TO_BE_REPLACE_GYRO_Z_MIN,  max(gyroscope_z) as TO_BE_REPLACE_GYRO_Z_MAX, avg(gyroscope_z) as TO_BE_REPLACE_GYRO_Z_AVG,std(gyroscope_z) as TO_BE_REPLACE_GYRO_Z_STD,\n" +
                " \n" +
                " min(sqrt(magnitude_x*magnitude_x+magnitude_y*magnitude_y+magnitude_z*magnitude_z))  as TO_BE_REPLACE_MAG_VEC_MIN, \n" +
                " max(sqrt(magnitude_x*magnitude_x+magnitude_y*magnitude_y+magnitude_z*magnitude_z)) as TO_BE_REPLACE_MAG_VEC_MAX, \n" +
                " avg(sqrt(magnitude_x*magnitude_x+magnitude_y*magnitude_y+magnitude_z*magnitude_z)) as TO_BE_REPLACE_MAG_VEC_AVG, \n" +
                " std(sqrt(magnitude_x*magnitude_x+magnitude_y*magnitude_y+magnitude_z*magnitude_z)) as TO_BE_REPLACE_MAG_VEC_STD, \n" +
                " \n" +
                " min(magnitude_x) as TO_BE_REPLACE_MAG_X_MIN,  max(magnitude_x) as TO_BE_REPLACE_MAG_X_MAX, avg(magnitude_x) as TO_BE_REPLACE_MAG_X_AVG,std(magnitude_x) as TO_BE_REPLACE_MAG_X_STD,\n" +
                " min(magnitude_y) as TO_BE_REPLACE_MAG_Y_MIN,  max(magnitude_y) as TO_BE_REPLACE_MAG_Y_MAX, avg(magnitude_y) as TO_BE_REPLACE_MAG_Y_AVG,std(magnitude_y) as TO_BE_REPLACE_MAG_Y_STD,\n" +
                " min(magnitude_z) as TO_BE_REPLACE_MAG_Z_MIN,  max(magnitude_z) as TO_BE_REPLACE_MAG_Z_MAX, avg(magnitude_z) as TO_BE_REPLACE_MAG_Z_AVG,std(magnitude_z) as TO_BE_REPLACE_MAG_Z_STD\n";
        sql = "count(TO_BE_REPLACE.accelerometer_x), count(TO_BE_REPLACE.accelerometer_y), count(TO_BE_REPLACE.accelerometer_z),\n" +
                "count(TO_BE_REPLACE.gyroscope_x), count(TO_BE_REPLACE.gyroscope_y), count(TO_BE_REPLACE.gyroscope_z),\n" +
                "count(TO_BE_REPLACE.magnitude_x), count(TO_BE_REPLACE.magnitude_y), count(TO_BE_REPLACE.magnitude_z)\n";
        return getSQLResultDatas(sensorTableName, phoneNumber, sql);
    }

    private List<SQLResultData> getSQLResultDatasForSingle(String sensorTableName, String phoneNumber) throws SQLException {
        String sql = "min(value) as TO_BE_REPLACE_MIN, max(value) as TO_BE_REPLACE_MAX, avg(value) as TO_BE_REPLACE_AVG, std(value) as TO_BE_REPLACE_STD\n";
        sql = "count(TO_BE_REPLACE.value)\n";
        return getSQLResultDatas(sensorTableName, phoneNumber, sql);
    }

    private List<SQLResultData> getSQLResultDatasForSurvey(String phoneNumber) throws SQLException {
        String sql = "SELECT subject_phoneNumber, productNo, (a1+a2+a3+a4+a5+a6)/6 as USER_PREFERENCE\n" +
                "FROM _Survey\n" +
                "WHERE subject_phoneNumber = '%s'";
        return getSQLResultDatas(String.format(sql,phoneNumber));
    }

    public void run() throws SQLException, IOException {

        //String phoneNumber = "01024864821";
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("data.csv"));

        Set<String> valueNames = new HashSet<>();
        Map<String, Map<Integer, SQLResultData>> phoneNumberMap = new HashMap<>();
        List<String> phoneNumbers = getPhoneNumbers();
        for(String phoneNumber : phoneNumbers) {
            Map<Integer, SQLResultData> productNoAndSQLResultDataMap = new HashMap<>();
            getSQLResultDatasForXYZ("E4Accelerometer", phoneNumber).forEach(
                    sqlResultData -> {
                        if (productNoAndSQLResultDataMap.containsKey(sqlResultData.getProductNo()))
                            productNoAndSQLResultDataMap.get(sqlResultData.getProductNo()).merge(sqlResultData);
                        else productNoAndSQLResultDataMap.put(sqlResultData.getProductNo(), sqlResultData);
                    }
            );

            getSQLResultDatasForXYZ("SmartphoneAccelerometer", phoneNumber).forEach(
                    sqlResultData -> {
                        if (productNoAndSQLResultDataMap.containsKey(sqlResultData.getProductNo()))
                            productNoAndSQLResultDataMap.get(sqlResultData.getProductNo()).merge(sqlResultData);
                        else productNoAndSQLResultDataMap.put(sqlResultData.getProductNo(), sqlResultData);
                    }
            );
            getSQLResultDatasForXYZ("SmartphoneGyroscope", phoneNumber).forEach(
                    sqlResultData -> {
                        if (productNoAndSQLResultDataMap.containsKey(sqlResultData.getProductNo()))
                            productNoAndSQLResultDataMap.get(sqlResultData.getProductNo()).merge(sqlResultData);
                        else productNoAndSQLResultDataMap.put(sqlResultData.getProductNo(), sqlResultData);
                    }
            );
            getSQLResultDatasForSingle("E4BVP", phoneNumber).forEach(
                    sqlResultData -> {
                        if (productNoAndSQLResultDataMap.containsKey(sqlResultData.getProductNo()))
                            productNoAndSQLResultDataMap.get(sqlResultData.getProductNo()).merge(sqlResultData);
                        else productNoAndSQLResultDataMap.put(sqlResultData.getProductNo(), sqlResultData);
                    }
            );
            getSQLResultDatasForSingle("E4GSR", phoneNumber).forEach(
                    sqlResultData -> {
                        if (productNoAndSQLResultDataMap.containsKey(sqlResultData.getProductNo()))
                            productNoAndSQLResultDataMap.get(sqlResultData.getProductNo()).merge(sqlResultData);
                        else productNoAndSQLResultDataMap.put(sqlResultData.getProductNo(), sqlResultData);
                    }
            );
            getSQLResultDatasForSingle("E4SkinTemperature", phoneNumber).forEach(
                    sqlResultData -> {
                        if (productNoAndSQLResultDataMap.containsKey(sqlResultData.getProductNo()))
                            productNoAndSQLResultDataMap.get(sqlResultData.getProductNo()).merge(sqlResultData);
                        else productNoAndSQLResultDataMap.put(sqlResultData.getProductNo(), sqlResultData);
                    }
            );
            getSQLResultDatasForMultyXYZ("SensorTag", phoneNumber).forEach(
                    sqlResultData -> {
                        if (productNoAndSQLResultDataMap.containsKey(sqlResultData.getProductNo()))
                            productNoAndSQLResultDataMap.get(sqlResultData.getProductNo()).merge(sqlResultData);
                        else productNoAndSQLResultDataMap.put(sqlResultData.getProductNo(), sqlResultData);
                    }
            );
            getSQLResultDatasForSurvey(phoneNumber).forEach(
                    sqlResultData -> {
                        if (productNoAndSQLResultDataMap.containsKey(sqlResultData.getProductNo()))
                            productNoAndSQLResultDataMap.get(sqlResultData.getProductNo()).merge(sqlResultData);
                        else productNoAndSQLResultDataMap.put(sqlResultData.getProductNo(), sqlResultData);
                    }
            );
            productNoAndSQLResultDataMap.values().forEach(
                    sqlResultData -> valueNames.addAll(sqlResultData.getColumnNameAndResultMap().keySet())
            );
            phoneNumberMap.put(phoneNumber, productNoAndSQLResultDataMap);
        }
        for(String phoneNumber : phoneNumbers) {

            Map<Integer, SQLResultData> productNoAndSQLResultDataMap = phoneNumberMap.get(phoneNumber);

            bufferedWriter.write("phoneNumber");
            bufferedWriter.write(",");
            bufferedWriter.write("productNo");
            bufferedWriter.write(",");
            for (String valueName : valueNames) {
                bufferedWriter.write(valueName);
                bufferedWriter.write(",");
            }
            bufferedWriter.write("\n");
            for (int i = 1; i <= 30; i++) {
                bufferedWriter.write(phoneNumber);
                bufferedWriter.write(",");
                bufferedWriter.write(""+i);
                bufferedWriter.write(",");
                SQLResultData sqlResultData = productNoAndSQLResultDataMap.get(i);
                for (String valueName : valueNames) {
                    if (sqlResultData == null) {
                        bufferedWriter.write("NULL");
                    } else {
                        String value = sqlResultData.getColumnNameAndResultMap().get(valueName);
                        if (value == null)
                            bufferedWriter.write("NULL");
                        else
                            bufferedWriter.write(value);
                    }
                    bufferedWriter.write(",");
                }
                bufferedWriter.write("\n");
            }
        }
        bufferedWriter.flush();
        bufferedWriter.close();
        System.out.println("DONE");
        System.out.println("DONE");
        System.out.println("DONE");
        System.out.println("DONE");
        System.out.println("DONE");
        System.exit(0);
    }

}

final class SQLResultData {
    final String phoneNumber;
    final int productNo;

    final Map<String, String> columnNameAndResultMap;

    public SQLResultData(String phoneNumber, int productNo, Map<String, String> columnNameAndResultMap) {
        this.phoneNumber = phoneNumber;
        this.productNo = productNo;
        this.columnNameAndResultMap = columnNameAndResultMap;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public int getProductNo() {
        return productNo;
    }

    public Map<String, String> getColumnNameAndResultMap() {
        return columnNameAndResultMap;
    }

    public SQLResultData merge(SQLResultData sqlResultData) {
        if (!equals(sqlResultData))
            return null;

        for (Map.Entry<String, String> entry : sqlResultData.getColumnNameAndResultMap().entrySet())
            columnNameAndResultMap.put(entry.getKey(), entry.getValue());

        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == null || getClass() != o.getClass()) return false;

        if (this == o) return true;

        SQLResultData that = (SQLResultData) o;

        return new EqualsBuilder()
                .append(getProductNo(), that.getProductNo())
                .append(getPhoneNumber(), that.getPhoneNumber())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getPhoneNumber())
                .append(getProductNo())
                .toHashCode();
    }
}


final class SQLResultDataBuilder {
    private final Map<String, String> columnNameAndResultMap = new HashMap<>();
    private String phoneNumber = null;
    private int productNo = -1;

    public SQLResultDataBuilder PhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    public SQLResultDataBuilder add(String columnName, String result) {
        columnNameAndResultMap.put(columnName, result);
        return this;
    }

    public SQLResultDataBuilder ProductNo(int productNo) {
        this.productNo = productNo;
        return this;
    }

    public SQLResultData Build() {
        Validate.notNull(phoneNumber);
        Validate.isTrue(productNo != -1);
        Validate.notEmpty(columnNameAndResultMap);
        return new SQLResultData(phoneNumber, productNo, columnNameAndResultMap);
    }
}
