package br.ufba.dcc.wiser.fot.storage;


import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.datatypes.xsd.XSDDateTime;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.vocabulary.XSD;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;









import br.ufba.dcc.wiser.fot.storage.schema.FiestaIoT;
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
			long unixTime = System.currentTimeMillis() / 1000L;
			this.subscriber = new MqttClient(this.brokerUrl + ":"
					+ this.brokerPort, this.serverId+unixTime);
			this.subscriber.setCallback(this);
			this.subscriber.connect(connOpt);
			this.subscriber.subscribe(topic, 1);

		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void disconnect(){
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
					+ this.brokerPort, this.serverId+unixTime);
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
						model.write(System.out, "RDF/XML");
						updateTripleStore(model, "http://localhost:3030/IoTsensorDB/");
					}
				}).start();
			}
		} catch (org.json.JSONException e) {
			System.out.println(e.getMessage());
		}
	}
	
	private Model buildTriples(JSONObject json, Date dateTime){
		OntModel model = ModelFactory.createOntologyModel();
		//"{\"CODE\":\"POST\",\"HEADER\":{\"NAME\":\"ufbaino01\"},\"BODY\":{\"temperatureSensor\":\"27\"}}"
		String sensorName = json.getJSONObject("BODY").keys().next().toString();
		String sensorFullName = json.getJSONObject("HEADER").getString("NAME") + "_" + sensorName;
		
		long unixTime = System.currentTimeMillis() / 1000L;
		
		Individual observationValue = model.createIndividual(BASE_URI + "obsValue" + unixTime, SSN.ObservationValue);
		
		Literal valueLiteral = model.createTypedLiteral(json.getJSONObject("BODY").get(sensorName).toString(),XSDDatatype.XSDdouble);
		observationValue.addLiteral(FiestaIoT.hasDataValue, valueLiteral);
		
		Individual sensorOutput = model.createIndividual(BASE_URI + "sensorOutput" + unixTime, SSN.SensorOutput);
		sensorOutput.addProperty(SSN.hasValue, observationValue);
		
		
		
		Individual timeInterval = model.createIndividual(BASE_URI + "timeInterval" + unixTime, FiestaIoT.classTimeInterval);
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
		String dateFormated = sdf.format(dateTime); 
		Literal dateLiteral = model.createTypedLiteral(dateFormated, XSDDatatype.XSDdate);
		timeInterval.addLiteral(FiestaIoT.hasIntervalDate, dateLiteral);
		
		Individual observation = model.createIndividual(BASE_URI + "obs" + unixTime, SSN.Observation);
		observation.addProperty(SSN.observationSamplingTime, timeInterval);
		observation.addProperty(SSN.observationResult, sensorOutput);
		
		Resource sensor = model.createResource(SSN.NS + sensorFullName);
		sensor.addProperty(SSN.madeObservation, observation);
		 
		return model;
	}

	private void updateTripleStore(Model model, String tripleStoreURI) {
		
		DatasetAccessor accessor = DatasetAccessorFactory
				.createHTTP(tripleStoreURI);
		accessor.add(model);
		
		
		/*UpdateProcessor upp = UpdateExecutionFactory.createRemote(
				UpdateFactory.create(model.toString()),
				tripleStoreURI);
		upp.execute();*/
		// Query the collection, dump output
		/*QueryExecution qe = QueryExecutionFactory.sparqlService(
				"http://localhost:3030/IoTsensorDB/query",
				"SELECT * WHERE {?x ?r ?y}");
		ResultSet results = qe.execSelect();
		ResultSetFormatter.out(System.out, results);
		qe.close();*/

	}

}
