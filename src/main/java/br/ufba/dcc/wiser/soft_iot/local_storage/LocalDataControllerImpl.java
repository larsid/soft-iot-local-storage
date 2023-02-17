package br.ufba.dcc.wiser.soft_iot.local_storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.sql.DataSource;

import br.ufba.dcc.wiser.soft_iot.entities.Device;
import br.ufba.dcc.wiser.soft_iot.entities.Sensor;
import br.ufba.dcc.wiser.soft_iot.entities.SensorData;

public class LocalDataControllerImpl implements LocalDataController {

    private DataSource dataSource;
    private boolean debugModeValue;

    public void init() {
        Connection dbConnection;
        try {
            dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            DatabaseMetaData dbMeta = dbConnection.getMetaData();
            printlnDebug("Using datasource "
                    + dbMeta.getDatabaseProductName() + ", URL "
                    + dbMeta.getURL());

            stmt.execute("CREATE TABLE IF NOT EXISTS semantic_registered_last_time_sensors(sensor_id VARCHAR(255),"
                    + " device_id VARCHAR(255), last_time TIMESTAMP)");

            stmt.execute("CREATE TABLE IF NOT EXISTS aggregation_registered_last_time_sensors(sensor_id VARCHAR(255),"
                    + " device_id VARCHAR(255), last_time TIMESTAMP)");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public SensorData getLastSensorData(Device device, Sensor sensor) {
        SensorData sensorData = null;
        try {
            Connection dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            String query = "SELECT * FROM sensor_data WHERE device_id='" + device.getId() + "' AND "
                    + "sensor_id='" + sensor.getId() + "' ORDER BY start_datetime DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
                Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
                sensorData = new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate);
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sensorData;
    }

    public List<SensorData> getSensorData(Device device, Sensor sensor) {
        List<SensorData> sensorData = new ArrayList<SensorData>();
        try {
            Connection dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            String query = "SELECT * FROM sensor_data WHERE device_id='" + device.getId() + "' AND "
                    + "sensor_id='" + sensor.getId() + "' ORDER BY start_datetime";

            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
                Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
                sensorData.add(new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate));
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sensorData;
    }

    public List<SensorData> getSensorDataByDateTime(Device device, Sensor sensor, Date startDateTime, Date endDateTime) {
        List<SensorData> sensorData = new ArrayList<SensorData>();
        try {
            Connection dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            Timestamp startTimestamp = new java.sql.Timestamp(startDateTime.getTime());
            Timestamp endTimestamp = new java.sql.Timestamp(endDateTime.getTime());
            String query = "SELECT * FROM sensor_data WHERE device_id='" + device.getId() + "' AND "
                    + "sensor_id='" + sensor.getId() + "' AND start_datetime > '" + startTimestamp + "' AND "
                    + "end_datetime <= '" + endTimestamp + "' ORDER BY start_datetime ASC";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
                Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
                sensorData.add(new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate));
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sensorData;
    }

    public List<SensorData> getSensorDataByLastDateTime(Device device, Sensor sensor, Date lastDateTime) {
        List<SensorData> sensorData = new ArrayList<SensorData>();
        try {
            Connection dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            Timestamp lastTimestamp = new java.sql.Timestamp(lastDateTime.getTime());
            String query = "SELECT * FROM sensor_data WHERE device_id='" + device.getId() + "' AND "
                    + "sensor_id='" + sensor.getId() + "' AND "
                    + "start_datetime > '" + lastTimestamp + "' ORDER BY start_datetime ASC";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
                Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
                sensorData.add(new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate));
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sensorData;
    }

    public SensorData getLastAggregatedSensorData(Device device, Sensor sensor) {
        SensorData sensorData = null;
        try {
            Connection dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            int aggregationStatus = 1;

            String query = "SELECT * FROM sensor_data WHERE device_id='" + device.getId() + "' AND "
                    + "sensor_id='" + sensor.getId() + "' AND "
                    + "aggregation_status = '" + aggregationStatus
                    + "' ORDER BY start_datetime DESC LIMIT 1";
            ResultSet rs = stmt.executeQuery(query);

            if (rs.next()) {
                Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
                Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
                sensorData = new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate);
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sensorData;
    }

    public List<SensorData> getAggregatedSensorData(Device device, Sensor sensor) {
        List<SensorData> sensorData = new ArrayList<SensorData>();
        try {
            Connection dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            int aggregationStatus = 1;
            String query = "SELECT * FROM sensor_data WHERE device_id='" + device.getId() + "' AND "
                    + "sensor_id='" + sensor.getId() + "' AND "
                    + "aggregation_status = '" + aggregationStatus
                    + "' ORDER BY start_datetime ASC";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
                Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
                sensorData.add(new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate));
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sensorData;
    }

    public List<SensorData> getSensorDataByAggregationStatusAndDate(Device device, Sensor sensor, int aggregationStatus, Date lastDate) {
        List<SensorData> sensorData = new ArrayList<SensorData>();
        try {
            Connection dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            Timestamp lastTimestamp = new java.sql.Timestamp(lastDate.getTime());
            String query = "SELECT * FROM sensor_data WHERE device_id='" + device.getId() + "' AND "
                    + "sensor_id='" + sensor.getId() + "' AND "
                    + "aggregation_status = '" + aggregationStatus + "' AND start_datetime >'" + lastTimestamp
                    + "' ORDER BY start_datetime ASC";
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
                Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
                sensorData.add(new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate));
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sensorData;
    }

    public void insertSensorDataAggregated(List<SensorData> listSensorData, int aggregationStatus) {
        Connection dbConnection;
        try {
            dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            for (SensorData sensorData : listSensorData) {
                Timestamp startDateTime = new Timestamp(sensorData.getStartTime().getTime());
                Timestamp endDateTime = new Timestamp(sensorData.getEndTime().getTime());
                boolean result = stmt.execute("INSERT INTO sensor_data (sensor_id, device_id, data_value,"
                        + " start_datetime, end_datetime, aggregation_status) values ('" + sensorData.getSensor().getId() + "', '"
                        + sensorData.getDevice().getId() + "', '" + sensorData.getValue() + "' ,'"
                        + startDateTime + "', '" + endDateTime + "'," + aggregationStatus + ")");
                if (result) {
                    printlnDebug("cannot insert data:" + "('" + sensorData.getSensor().getId() + "', '" + sensorData.getDevice().getId() + "', '" + sensorData.getValue() + "' ,'" + startDateTime
                            + "', '" + endDateTime + "')");
                }
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createFirstLastSensorDataAggregated(Device device, Sensor sensor, Date lastDateTime) {
        Connection dbConnection;
        try {
            dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            Timestamp lastTimestamp = new java.sql.Timestamp(lastDateTime.getTime());
            stmt.execute("INSERT INTO aggregation_registered_last_time_sensors (sensor_id, device_id, last_time)"
                    + " VALUES('" + device.getId() + "', '" + sensor.getId() + "', '"
                    + lastTimestamp + "')");
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastSensorDataAggregated(Device device, Sensor sensor, Date lastDateTime) {
        Connection dbConnection;
        try {
            dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            Timestamp lastTimestamp = new java.sql.Timestamp(lastDateTime.getTime());
            stmt.execute("UPDATE aggregation_registered_last_time_sensors SET last_time='"
                    + lastTimestamp + "' WHERE sensor_id='" + sensor.getId() + "' AND "
                    + "device_id='" + device.getId() + "'");
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Date getLastDateOfAggregatedSensorData(Device device, Sensor sensor) {
        Connection dbConnection;
        try {
            dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM aggregation_registered_last_time_sensors"
                    + " WHERE sensor_id='" + sensor.getId() + "' AND "
                    + "device_id='" + device.getId() + "'");
            if (rs.next()) {
                return rs.getDate("last_time");
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void createFirstLastSensorDataEnriched(Device device, Sensor sensor, Date lastDateTime) {
        Connection dbConnection;
        try {
            dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            Timestamp lastTimestamp = new java.sql.Timestamp(lastDateTime.getTime());
            stmt.execute("INSERT INTO semantic_registered_last_time_sensors (sensor_id, device_id, last_time)"
                    + " VALUES('" + device.getId() + "', '" + sensor.getId() + "', '"
                    + lastTimestamp + "')");
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateLastSensorDataEnriched(Device device, Sensor sensor, Date lastDateTime) {
        Connection dbConnection;
        try {
            dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            Timestamp lastTimestamp = new java.sql.Timestamp(lastDateTime.getTime());
            stmt.execute("UPDATE semantic_registered_last_time_sensors SET last_time='"
                    + lastTimestamp + "' WHERE sensor_id='" + sensor.getId() + "' AND "
                    + "device_id='" + device.getId() + "'");
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Date getLastDateOfEnrichedSensorData(Device device, Sensor sensor) {
        Connection dbConnection;
        try {
            dbConnection = this.dataSource.getConnection();
            Statement stmt = dbConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM semantic_registered_last_time_sensors"
                    + " WHERE sensor_id='" + sensor.getId() + "' AND "
                    + "device_id='" + device.getId() + "'");
            if (rs.next()) {
                return rs.getDate("last_time");
            }
            dbConnection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void printlnDebug(String str) {
        Logger log = Logger.getLogger(LocalDataControllerImpl.class.getName());
      
        if (debugModeValue) {
            log.info(str);
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setDebugModeValue(boolean debugModeValue) {
        this.debugModeValue = debugModeValue;
    }
}
