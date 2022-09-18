package br.uefs.larsid.extended.local_storage.listener;

import org.osgi.framework.ServiceReference;

import br.ufba.dcc.wiser.soft_iot.local_storage.MqttH2StorageController;
import br.ufba.dcc.wiser.soft_iot.local_storage.MqttPublisherController;

/**
 *
 * @author Allan Capistrano
 */
public class MappingDevicesListener {

    private boolean debugModeValue;
    private MqttPublisherController mqttPublisher;
    private MqttH2StorageController mqttH2Storage;

    public void onBind(ServiceReference ref) {
        printlnDebug("Bound service: " + ref); 
        
        // TODO: Rever
        this.mqttH2Storage.setFlag(true);
        
        
        this.mqttPublisher.sendFlowRequestBySensorDevice();
        this.mqttH2Storage.subscribeDevicesTopics();
    }

    public void onUnbind(ServiceReference ref) {
        printlnDebug("Unbound service: " + ref);

        // TODO: Rever
        this.mqttH2Storage.setFlag(true);
    }

    private void printlnDebug(String str) {
        if (this.debugModeValue) {
            System.out.println(str);
        }
    }

    public void setDebugModeValue(boolean debugModeValue) {
        this.debugModeValue = debugModeValue;
    }

    public void setMqttPublisher(MqttPublisherController mqttPublisher) {
        this.mqttPublisher = mqttPublisher;
    }

    public void setMqttH2Storage(MqttH2StorageController mqttH2Storage) {
      this.mqttH2Storage = mqttH2Storage;
    }
}
