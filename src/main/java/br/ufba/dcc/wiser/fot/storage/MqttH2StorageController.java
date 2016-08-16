package br.ufba.dcc.wiser.fot.storage;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.sql.DataSource;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import br.ufba.dcc.wiser.fot.storage.schema.FiestaIoT;
import br.ufba.dcc.wiser.fot.storage.schema.SSN;

public class MqttH2StorageController implements MqttCallback {
	public static String topic = "dev/#";

	private String brokerUrl;
	private String brokerPort;
	private String serverId;
	private String username;
	private String password;
	private String fusekiURI;
	private String baseURI;
	private MqttClient subscriber;
	private DataSource dataSource;
	private Connection dbConnection;

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
			this.subscriber.subscribe(topic, 1);

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}

		try {
			this.dbConnection = this.dataSource.getConnection();
			Statement stmt = this.dbConnection.createStatement();
			stmt.execute("drop table sensors_data");
			DatabaseMetaData dbMeta = this.dbConnection.getMetaData();
			System.out.println("Using datasource "
					+ dbMeta.getDatabaseProductName() + ", URL "
					+ dbMeta.getURL());
			stmt.execute("CREATE TABLE IF NOT EXISTS sensors_data(ID INT PRIMARY KEY, sensor_name VARCHAR(255),"
					+ " device_name VARCHAR(255), data_value VARCHAR(255), time TIMESTAMP)");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
			this.subscriber.subscribe(topic, 1);

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
				try {
					JSONObject json = new JSONObject(messageContent);
					if ((json.get("CODE").toString().contentEquals("POST"))
							&& json.getJSONObject("BODY") != null) {

						Date date = new Date();
						storeLocalData(json, date);
					}
				} catch (org.json.JSONException e) {
				}
			}
		}).start();
	}

	private synchronized void storeLocalData(JSONObject json, Date dateTime) {
		Model model2 = ModelFactory.createDefaultModel();
		OntModel model = ModelFactory.createOntologyModel();
		/*
		 * {"CODE":"POST","METHOD":"FLOW","HEADER":{"FLOW":{"publish":60000,"collect":10000},"NAME":"ufbaino01"},
		 * "BODY":{"temperatureSensor":["28","37","30","28","27","31"]}}
		 */
		String sensorName = json.getJSONObject("BODY").keys().next().toString();
		String deviceName = json.getJSONObject("HEADER").getString("NAME");
		int collectTime = json.getJSONObject("HEADER").getJSONObject("FLOW").getInt("collect");
		JSONArray sensorValues = json.getJSONObject("BODY").getJSONArray(sensorName);
		
		System.out.println(sensorValues);
		
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
}
