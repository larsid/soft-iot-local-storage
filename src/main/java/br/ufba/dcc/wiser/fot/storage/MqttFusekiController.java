package br.ufba.dcc.wiser.fot.storage;


import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import br.ufba.dcc.wiser.fot.storage.schema.SSN;

public class MqttFusekiController implements MqttCallback {

	private static String BASE_URI = "http://example.org/";
	public static String topic = "dev/#";

	private static final String UPDATE_TEMPLATE = "PREFIX dc: <http://purl.org/dc/elements/1.1/>"
			+ "INSERT DATA"
			+ "{ <http://example/%s>    dc:title    \"A new book\" ;"
			+ "                         dc:creator  \"A.N.Other\" ." + "}   ";

	private String brokerUrl;
	private String brokerPort;
	private String serverId;
	private String username;
	private String password;
	private MqttClient subscriber;

	public MqttFusekiController(String brokerUrl, String brokerPort,
			String serverId, String username, String password) {
		MqttConnectOptions connOpt = new MqttConnectOptions();

		this.brokerUrl = brokerUrl;
		this.brokerPort = brokerPort;
		this.serverId = serverId;
		this.username = username;
		this.password = password;

		try {
			if (!this.username.isEmpty())
				connOpt.setUserName(this.username);
			if (!this.password.isEmpty())
				connOpt.setPassword(this.password.toCharArray());

			this.subscriber = new MqttClient(this.brokerUrl + ":"
					+ this.brokerPort, this.serverId);
			this.subscriber.setCallback(this);
			this.subscriber.connect(connOpt);
			this.subscriber.subscribe(topic, 1);

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub

	}

	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub

	}

	public void messageArrived(String topic, MqttMessage message)
			throws Exception {		
		String messageContent = new String(message.getPayload());
		try {
			final JSONObject json = new JSONObject(messageContent);
			if (json.get("CODE").toString().contentEquals("POST")) {
				new Thread(new Runnable() {
					public void run() {
						Date date = new Date();
						Model model = buildTriples(json, date);
						model.write(System.out, "TURTLE");
					}
				}).start();
			}
		} catch (org.json.JSONException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private Model buildTriples(JSONObject json, Date dateTime){
		Model model = ModelFactory.createDefaultModel();
		//"{\"CODE\":\"POST\",\"HEADER\":{\"NAME\":\"Ufbaino01\"},\"BODY\":{\"temperatureSensor\":\"27\"}}"
		String sensorName = json.getJSONObject("BODY").keys().next().toString();
		String sensorFullName = json.getJSONObject("HEADER").getString("NAME") + "_" + sensorName;
		
		long unixTime = System.currentTimeMillis() / 1000L;
		
		Resource observationValue = model.createResource(BASE_URI + "obsValue" + unixTime, SSN.ObservationValue);
		Property hasDataValue = model.createProperty(SSN.NS+"hasDataValue");
		observationValue.addProperty(hasDataValue, json.getJSONObject("BODY").get(sensorName).toString());
		
		Resource sensorOutput = model.createResource(BASE_URI + "sensorOutput" + unixTime, SSN.SensorOutput);
		Property hasValue = model.createProperty(SSN.NS+"hasValue");
		sensorOutput.addProperty(hasValue, observationValue);
		
		Resource classTimeInterval = model.createResource("http://www.loa-cnr.it/ontologies/DUL.owl#TimeInterval");
		Property hasIntervalDate = model.createProperty("http://www.loa-cnr.it/ontologies/DUL.owl#hasIntervalDate");
		Resource timeInterval = model.createResource(BASE_URI + "timeInterval" + unixTime, classTimeInterval);
		timeInterval.addProperty(hasIntervalDate, (new Timestamp(dateTime.getTime())).toString());
		
		Resource observation = model.createResource(BASE_URI + "obs" + unixTime, SSN.Observation);
		Property observationSamplingTime = model.createProperty(SSN.NS+"observationSamplingTime");
		Property observationResult = model.createProperty(SSN.NS+"observationResult");
		observation.addProperty(observationSamplingTime, timeInterval);
		observation.addProperty(observationResult, sensorOutput);
		
		Resource sensor = model.createResource(SSN.NS + sensorFullName);
		Property madeObservation = model.createProperty(SSN.NS+"madeObservation");
		sensor.addProperty(madeObservation, observation);
		 
		return model;
	}

	private void testJena() {
		String id = UUID.randomUUID().toString();
		System.out.println(String.format("Adding %s", id));
		UpdateProcessor upp = UpdateExecutionFactory.createRemote(
				UpdateFactory.create(String.format(UPDATE_TEMPLATE, id)),
				"http://localhost:3030/IoTsensorDB/update");
		upp.execute();
		// Query the collection, dump output
		/*QueryExecution qe = QueryExecutionFactory.sparqlService(
				"http://localhost:3030/IoTsensorDB/query",
				"SELECT * WHERE {?x ?r ?y}");
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results);
		qe.close();*/

	}

}
