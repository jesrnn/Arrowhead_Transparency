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


import org.eclipse.paho.client.mqttv3.*;

import com.eislab.af.translator.data.BaseContext;

public class MqttServer_spoke implements MqttCallback, BaseSpokeProvider  {

	private MqttClient mqttClient; 
	private String brokerURI;
	private String clientId;
//	private String topic;
	private BaseSpoke nextSpoke;
	
	
	private boolean mqtt_with_publish = false;
	
	
	public MqttServer_spoke(String brokerURI, String serviceProviderPath, int maxPublishDelay, boolean mqtt_with_publish){

		this.brokerURI = brokerURI;
		this.clientId = "test";
		
		try {
			mqttClient = new MqttClient(brokerURI, clientId);
			
			connect(2, "","".getBytes());//just an empty connect to open connection to broker
		} catch (MqttException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		this.mqtt_with_publish = mqtt_with_publish;
		if(mqtt_with_publish) {
			new Thread(new Worker(serviceProviderPath, maxPublishDelay)).start();;
		}
		
	}
		
	
	public void connect(int keepAliveIntervalInSeconds, String topic, byte[] payload) throws MqttException {
		
		
		MqttConnectOptions mqqtConnectOptions = new MqttConnectOptions();
		mqqtConnectOptions.setKeepAliveInterval(keepAliveIntervalInSeconds);//in seconds
		//mqqtConnectOptions.setWill(topic, payload, 0x01, false);
		mqttClient.connect(mqqtConnectOptions);
		System.out.println("Client " + clientId + "connection established to " + brokerURI);
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
//			this.publish (this.topic, message ) ;
//		} catch (Exception e) {
//			System.out.println("MQTT Spoke publish error: " + e.getMessage());
//		}
//	}
	
	public void publish ( String topic, String message ) throws MqttPersistenceException, MqttException {
		if (mqttClient.isConnected()) {
			MqttMessage mqttMessage = new MqttMessage();
			mqttMessage.setPayload(message.getBytes());
			mqttClient.publish(topic, mqttMessage);
			System.out.println("MQTT Spoke: Publish made: " + message);
		}
		
	}	
	
	@Override
	public void connectionLost(Throwable arg0) {
		// TODO Auto-generated method stub
		try {
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

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		// TODO Auto-generated method stub
		String payload = new String (arg1.getPayload(), "utf8");
		System.out.println("Should not recieve messages: " + payload);
		
		if(mqtt_with_publish) {

			System.out.println("sending message to next spoke");
			BaseContext context = new BaseContext();
			context.setContent(payload);
			context.setContentType("");
			context.setMethod("post");
			context.setPath(arg0);
			
			this.nextSpoke.in(context);
		}
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void in(BaseContext context) {
		if(mqtt_with_publish) {
			try {
				if (this.mqttClient.isConnected() != true) {
					MqttConnectOptions mqqtConnectOptions = new MqttConnectOptions();
					mqqtConnectOptions.setKeepAliveInterval(2);//in seconds
					//mqqtConnectOptions.setWill(topic, payload, 0x01, false);
					mqttClient.connect(mqqtConnectOptions);
				}
				publish ( context.getPath(), context.getContent() );
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			//TODO: should just be an ACK or a NACK, for now do nothing with it.
		}
		
	}

	@Override
	public void setNextSpoke(Object nextSpoke) {
		this.nextSpoke = (BaseSpoke) nextSpoke;
		
	}

	@Override
	public String getAddress() {
		// TODO Auto-generated method stub
		return null;
	}
	
	class Worker implements Runnable {
		
		String serviceProviderPath;
		int maxPublishDelay;
		
		public Worker(String serviceProviderPath, int maxPublishDelay) {
			this.serviceProviderPath = serviceProviderPath;
			this.maxPublishDelay = maxPublishDelay;
		}
		
		@Override
		public void run() {

			while(true) {
				try {	
					BaseContext context = new BaseContext();
					context.setContent("");
					context.setMethod("get");
					context.setPath(this.serviceProviderPath);
					context.setContentType("");
					
					nextSpoke.in(context);
				
					Thread.sleep(this.maxPublishDelay);//sleep for 1 second
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}				
			}
		}
	}
}
