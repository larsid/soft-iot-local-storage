package br.ufba.dcc.wiser.soft_iot.local_storage;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import br.ufba.dcc.wiser.soft_iot.entities.Device;
import br.ufba.dcc.wiser.soft_iot.entities.Sensor;
import br.ufba.dcc.wiser.soft_iot.entities.SensorData;


public class LocalDataControllerImpl implements LocalDataController{
	
	private DataSource dataSource;
	private boolean debugModeValue;
	
	
	public SensorData getLastSensorData(Device device, Sensor sensor){
		SensorData sensorData = null;
		try {
			Connection dbConnection = this.dataSource.getConnection();
			Statement stmt = dbConnection.createStatement();
			String query = "SELECT * FROM sensors_data WHERE device_id='" + device.getId() + "' AND " +
					   "sensor_id='" + sensor.getId() + "' ORDER BY start_datetime DESC LIMIT 1";
			ResultSet rs = stmt.executeQuery(query);

			if (rs.next()) {
				Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
				Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
				sensorData = new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate);
			}
			dbConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sensorData;
	}
	
	public List<SensorData> getSensorDataByDateTime(Device device, Sensor sensor, Date startDateTime, Date endDateTime){
		List<SensorData> sensorData = new ArrayList<SensorData>();
		try {
			Connection dbConnection = this.dataSource.getConnection();
			Statement stmt = dbConnection.createStatement();
			Timestamp startTimestamp = new java.sql.Timestamp(startDateTime.getTime());
			Timestamp endTimestamp = new java.sql.Timestamp(endDateTime.getTime());
			String query = "SELECT * FROM sensors_data WHERE device_id='" + device.getId() + "' AND " +
					   "sensor_id='" + sensor.getId() + "' AND start_datetime > '" + startTimestamp + "' AND " +
					   "end_datetime <= '" + endTimestamp + "' ORDER BY start_datetime ASC";
			ResultSet rs = stmt.executeQuery(query);
			while(rs.next()) {
				Date startDate = new Date(rs.getTimestamp("start_datetime").getTime());
				Date endDate = new Date(rs.getTimestamp("end_datetime").getTime());
				sensorData.add(new SensorData(device, sensor, rs.getString("data_value"), startDate, endDate));
			}
			dbConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sensorData;
	}

	
	
	private void printlnDebug(String str){
		if (debugModeValue)
			System.out.println(str);
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setDebugModeValue(boolean debugModeValue) {
		this.debugModeValue = debugModeValue;
	}
}
