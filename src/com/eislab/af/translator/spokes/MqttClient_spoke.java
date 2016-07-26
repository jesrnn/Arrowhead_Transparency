/**
 * Copyright (c) <2016> <hasder>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 	
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 * 
*/

package com.eislab.af.translator.spokes;


import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.*;

import com.eislab.af.translator.data.BaseContext;

public class MqttClient_spoke implements MqttCallback, BaseSpokeConsumer  {

	private MqttClient mqttClient; 
	private String serverURI, clientId, topic;

	BaseSpoke nextSpoke;
	
	public MqttClient_spoke( String brokerUri ){

		this.serverURI = "tcp://localhost:1883";
		this.clientId = "translator_tempid";
		this.topic = "";
		
		try {
			this.mqttClient = new MqttClient(serverURI,clientId);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void connect(int keepAliveIntervalInSeconds, String topic, byte[] payload) throws MqttException {
		
		mqttClient = new MqttClient(serverURI, clientId);
		MqttConnectOptions mqqtConnectOptions = new MqttConnectOptions();
		mqqtConnectOptions.setKeepAliveInterval(keepAliveIntervalInSeconds);//in seconds
		
		mqttClient.connect(mqqtConnectOptions);
		System.out.println("Client " + clientId + "connection established to " + serverURI);
	}
	
	public void forceDisconnect() throws MqttException {
		if (mqttClient != null && mqttClient.isConnected()) {
			mqttClient.disconnectForcibly();
			mqttClient.close();
		}
	}
	
	public void subscribe(String topic) throws MqttException {
		mqttClient.subscribe(topic);
		mqttClient.setCallback(this);
		System.out.println("Subscription made to " + topic);
	}
	
//	@Override
//	public void response ( String message ) {
//		try {
//		this.publish ( message ) ;
//		} catch (Exception e) {
//			System.out.println("MQTT Spoke publish error: " + e.getMessage());
//		}
//	}
	
	public void publish ( String message ) throws MqttPersistenceException, MqttException {
		if (mqttClient.isConnected()) {
			MqttMessage mqttMessage = new MqttMessage();
			mqttMessage.setPayload(message.getBytes());
			mqttClient.publish(this.topic, mqttMessage);
			System.out.println("MQTT Spoke: Publish made: " + message);
		}
		
	}	
	
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		try {
			System.out.println("MQTT Spoke connection is lost to broker ");
			
			mqttClient.disconnect();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void deliveryComplete(IMqttDeliveryToken arg0) {
		// TODO Auto-generated method stub
		
	}
	
	HashMap<String, String> cache = new HashMap<String, String>();
	
	@Override
	public void messageArrived(String topic, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		String payload = new String (arg1.getPayload(), "utf8");
		System.out.println("Message Arrived: " + payload);
		
		cache.put(topic, payload);
		
//		this.hub.update(payload);
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	ArrayList<String> subscriptions = new ArrayList<String>();
	
	@Override
	public void in(BaseContext context) {
		
		if(context.getMethod().equals("get")) {
			String topic = context.getPath();
			try {
				if (!mqttClient.isConnected()) {
					connect(3, "", "".getBytes());//TODO: fix this. Put proper parameters
				}
				if (mqttClient.isConnected()) {
					
					if (subscriptions.contains(topic)) {
						
						//already subscribed so just return the last cached message
						context.setContent(cache.get(topic));
						if (context.getContent().equals(null)) {
							subscriptions.remove(topic);
							System.out.println("MQTT Client Spoke already subscribed to " + topic + " But payload is empty ");	
						} else {
							System.out.println("MQTT Client Spoke already subscribed to " + topic);	
						}
						activity++;
						this.nextSpoke.in(context);
					} else {
						subscribe(topic);
						subscriptions.add(topic);

						System.out.println("MQTT Client Spoke subscribed to " + topic);	
					}
										
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else { //"post", "put", "delete"
			
			try {
				if (mqttClient.isConnected()) {
					MqttMessage mqttMessage = new MqttMessage();
					mqttMessage.setPayload(context.getContent().getBytes());
					
					mqttClient.publish(context.getPath(), mqttMessage);
					System.out.println("MQTT Spoke: Publish made: " + context.getContent());
					context.setContent("ACK");
					activity++;
					this.nextSpoke.in(context);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

	@Override
	public void setNextSpoke(Object nextSpoke) {
		this.nextSpoke = (BaseSpoke) nextSpoke;
	}
	
	public int activity = 0;
	
	@Override
	public int getLastActivity() {
		// TODO Auto-generated method stub
		return activity;
	}

	@Override
	public void clearActivity() {
		// TODO Auto-generated method stub
		activity = 0;
	}

}
