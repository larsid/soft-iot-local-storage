package br.ufba.dcc.wiser.soft_iot.local_storage;

import java.util.List;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttPersistenceException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.osgi.service.blueprint.container.ServiceUnavailableException;

import br.ufba.dcc.wiser.soft_iot.entities.Device;
import br.ufba.dcc.wiser.soft_iot.entities.Sensor;
import br.ufba.dcc.wiser.soft_iot.mapping_devices.Controller;
import br.ufba.dcc.wiser.soft_iot.tatu.TATUWrapper;

public class MqttPublisherController {

	private String brokerUrl;
	private String brokerPort;
	private String serverId;
	private String username;
	private String password;
	private int defaultCollectionTime;
	private int defaultPublishingTime;
	private MqttClient publisher;
	private Controller fotDevices;
	private boolean debugModeValue;

	public MqttPublisherController() {
	}

	public void init() {
		MqttConnectOptions connOpt = new MqttConnectOptions();
		if (!this.username.isEmpty())
			connOpt.setUserName(this.username);
		if (!this.password.isEmpty())
			connOpt.setPassword(this.password.toCharArray());
		try {
			long unixTime = System.currentTimeMillis() / 1000L;
			publisher = new MqttClient("tcp://" + this.brokerUrl + ":"
					+ this.brokerPort, this.serverId + "_pub" + unixTime);
			publisher.connect(connOpt);
			printlnDebug("Sending FLOW messages:");
			sendFlowRequestBySensorDevice();
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendFlowRequestBySensorDevice(){
		try{
			List<Device> devices = fotDevices.getListDevices();
			for(Device device : devices){
				List<Sensor> sensors = device.getSensors();
				for(Sensor sensor : sensors){
					String flowRequest;
					if(sensor.getCollection_time() <= 0){
						flowRequest = TATUWrapper.getTATUFlow(sensor.getId(), defaultCollectionTime, defaultPublishingTime);
					}else{
						flowRequest = TATUWrapper.getTATUFlow(sensor.getId(), sensor.getCollection_time(), sensor.getPublishing_time());
					}
					printlnDebug("[topic: " + device.getId() +"] " + flowRequest);
					publishTATUMessage(flowRequest, device.getId());
				}
				
			}
		}catch (ServiceUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	private void publishTATUMessage(String msg, String topicName){
		MqttMessage mqttMsg = new MqttMessage();
		mqttMsg.setPayload(msg.getBytes());
		String topic = TATUWrapper.topicBase + topicName;
		try {
			publisher.publish(topic, mqttMsg);
		} catch (MqttPersistenceException e) {
			e.printStackTrace();
		} catch (MqttException e) {
			e.printStackTrace();
		}
		
	}
	
	public void destroy() {
		try {
			this.publisher.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void printlnDebug(String str){
		if (debugModeValue)
			System.out.println(str);
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

	public Controller getFotDevices() {
		return fotDevices;
	}

	public void setFotDevices(Controller fotDevices) {
		this.fotDevices = fotDevices;
	}

	public void setDefaultCollectionTime(int defaultCollectionTime) {
		this.defaultCollectionTime = defaultCollectionTime;
	}

	public void setDefaultPublishingTime(int defaultPublishingTime) {
		this.defaultPublishingTime = defaultPublishingTime;
	}

	public void setDebugModeValue(boolean debugModeValue) {
		this.debugModeValue = debugModeValue;
	}
	
	

}
