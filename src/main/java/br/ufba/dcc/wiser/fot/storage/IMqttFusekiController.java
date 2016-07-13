/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufba.dcc.wiser.fot.storage;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author absinto
 */
public interface IMqttFusekiController extends MqttCallback {

    void connectionLost(Throwable arg0);

    void deliveryComplete(IMqttDeliveryToken arg0);

    void disconnect();
    
    void init();

    void messageArrived(String topic, MqttMessage message) throws Exception;
    
}
