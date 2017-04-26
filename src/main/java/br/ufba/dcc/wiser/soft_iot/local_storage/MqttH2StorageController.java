package br.ufba.dcc.wiser.soft_iot.local_storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.sql.DataSource;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import br.ufba.dcc.wiser.soft_iot.entities.Device;
import br.ufba.dcc.wiser.soft_iot.entities.Sensor;
import br.ufba.dcc.wiser.soft_iot.entities.SensorData;
import br.ufba.dcc.wiser.soft_iot.mapping_devices.Controller;
import br.ufba.dcc.wiser.soft_iot.tatu.TATUWrapper;


public class MqttH2StorageController implements MqttCallback {

	private String brokerUrl;
	private String brokerPort;
	private String serverId;
	private String username;
	private String password;
	private String fusekiURI;
	private String baseURI;
	private MqttClient subscriber;
	private DataSource dataSource;
	private String numOfHoursDataStored;
	private Controller fotDevices;
	private boolean debugModeValue;

	public void init() {
		MqttConnectOptions connOpt = new MqttConnectOptions();

		try {
			if (!this.username.isEmpty())
				connOpt.setUserName(this.username);
			if (!this.password.isEmpty())
				connOpt.setPassword(this.password.toCharArray());
			long unixTime = System.currentTimeMillis() / 1000L;
			this.subscriber = new MqttClient("tcp://" + this.brokerUrl + ":"
					+ this.brokerPort, this.serverId + unixTime);
			this.subscriber.setCallback(this);
			this.subscriber.connect(connOpt);
			
			subscribeDevicesTopics(fotDevices.getListDevices());

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

		try {
			Connection dbConnection = this.dataSource.getConnection();
			Statement stmt = dbConnection.createStatement();
			//stmt.execute("drop table sensors_data");
			DatabaseMetaData dbMeta = dbConnection.getMetaData();
			System.out.println("Using datasource "
					+ dbMeta.getDatabaseProductName() + ", URL "
					+ dbMeta.getURL());
			stmt.execute("CREATE TABLE IF NOT EXISTS sensors_data(ID BIGINT AUTO_INCREMENT PRIMARY KEY, sensor_id VARCHAR(255),"
					+ " device_id VARCHAR(255), data_value VARCHAR(255), start_datetime TIMESTAMP, end_datetime TIMESTAMP)");
			
			ResultSet rs = stmt.executeQuery("SELECT * FROM sensors_data");
            ResultSetMetaData meta = rs.getMetaData();
            while (rs.next()) {
                writeResult(rs, meta.getColumnCount());
            }
			rs = stmt.executeQuery("CALL DISK_SPACE_USED('sensors_data')");
			meta = rs.getMetaData();
            while (rs.next()) {
                writeResult(rs, meta.getColumnCount());
            }
            dbConnection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void subscribeDevicesTopics(List<Device> devices) throws MqttException{
		for(Device device : devices){
			this.subscriber.subscribe(TATUWrapper.topicBase + device.getId() + "/#", 1);
		}
	}
	
	private void writeResult(ResultSet rs, int columnCount) throws SQLException {
        for (int c = 1; c <= columnCount; c++) {
            System.out.print(rs.getString(c) + ", ");
        }
        System.out.println();
    }

	public void disconnect() {
		try {
			this.subscriber.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void connectionLost(Throwable arg0) {
		MqttConnectOptions connOpt = new MqttConnectOptions();
		try {
			if (!this.username.isEmpty())
				connOpt.setUserName(this.username);
			if (!this.password.isEmpty())
				connOpt.setPassword(this.password.toCharArray());
			long unixTime = System.currentTimeMillis() / 1000L;
			this.subscriber = new MqttClient(this.brokerUrl + ":"
					+ this.brokerPort, this.serverId + unixTime);
			this.subscriber.setCallback(this);
			this.subscriber.connect(connOpt);
			subscribeDevicesTopics(fotDevices.getListDevices());

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	public synchronized void messageArrived(String topic,
			final MqttMessage message) throws Exception {
		new Thread(new Runnable() {
			public void run() {
				String messageContent = new String(message.getPayload());
				if(TATUWrapper.isValidTATUAnswer(messageContent)){
					
					String deviceId = TATUWrapper.getDeviceIdByTATUAnswer(messageContent);
					Device device = fotDevices.getDeviceById(deviceId);
					
					String sensorId = TATUWrapper.getSensorIdByTATUAnswer(messageContent);
					Sensor sensor = device.getSensorbySensorId(sensorId);
					sensor.setDevice(device);
					Date date = new Date();
					List<SensorData> listSensorData = TATUWrapper.parseTATUAnswerToListSensorData(messageContent,sensor,date);
					storeSensorData(listSensorData);
				}
			}
		}).start();
	}
	
	private void storeSensorData(List<SensorData> listSensorData){
		try {
			Connection dbConn = this.dataSource.getConnection();
			Statement stmt = dbConn.createStatement();
			for(SensorData sensorData : listSensorData){
				String sensorId = sensorData.getSensor().getId();
				String deviceId = sensorData.getSensor().getDevice().getId();
				Timestamp startDateTime = new Timestamp(sensorData.getStartTime().getTime());
				Timestamp endDateTime = new Timestamp(sensorData.getEndTime().getTime());
				stmt.execute("INSERT INTO sensors_data (sensor_id, device_id, data_value, start_datetime, end_datetime) values "
						+ "('"+ sensorId + "', '" + deviceId +"', '" + sensorData.getValue() + "' ,'" + startDateTime
						+ "', '" + endDateTime + "')");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}	
	}
	
	public void cleanOldData(){
		System.out.println("clean old data...");
		
		Connection dbConn;
		try {
			dbConn = this.dataSource.getConnection();
			Statement stmt = dbConn.createStatement();
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.HOUR, (-1)* Integer.parseInt(this.numOfHoursDataStored));
			Date date = cal.getTime();
			String strDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
			stmt.execute("DELETE FROM sensors_data WHERE end_datetime <= '"+ strDate + "'" );
			dbConn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setBrokerUrl(String brokerUrl) {
		this.brokerUrl = brokerUrl;
	}

	public void setBrokerPort(String brokerPort) {
		this.brokerPort = brokerPort;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setFusekiURI(String fusekiURI) {
		this.fusekiURI = fusekiURI;
	}

	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	public String getFusekiURI() {
		return fusekiURI;
	}

	public String getBaseURI() {
		return baseURI;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void setnumOfHoursDataStored(String numOfHoursDataStored) {
		this.numOfHoursDataStored = numOfHoursDataStored;
	}

	public void setFotDevices(Controller fotDevices) {
		this.fotDevices = fotDevices;
	}
	
	public void setDebugModeValue(boolean debugModeValue) {
		this.debugModeValue = debugModeValue;
	}
	
}