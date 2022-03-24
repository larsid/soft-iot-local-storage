package br.uefs.larsid.extended.local_storage.listener;

import org.osgi.framework.ServiceReference;

import br.ufba.dcc.wiser.soft_iot.local_storage.MqttPublisherController;

/**
 *
 * @author Allan Capistrano
 */
public class MappingDevicesListener {

    private boolean debugModeValue;
    private MqttPublisherController mqttPublisher;

    public void onBind(ServiceReference ref) {
        printlnDebug("Bound service: " + ref);
        this.mqttPublisher.sendFlowRequestBySensorDevice();
    }

    public void onUnbind(ServiceReference ref) {
        printlnDebug("Unbound service: " + ref);
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
}
