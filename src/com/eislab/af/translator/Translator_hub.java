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

package com.eislab.af.translator;


import com.eislab.af.translator.spokes.BaseSpokeConsumer;
import com.eislab.af.translator.spokes.BaseSpokeProvider;
import com.eislab.af.translator.spokes.CoapClient_spoke;
import com.eislab.af.translator.spokes.CoapServer_spoke;
import com.eislab.af.translator.spokes.HttpClient_spoke;
import com.eislab.af.translator.spokes.HttpServer_spoke;
import com.eislab.af.translator.spokes.MqttClient_spoke;
import com.eislab.af.translator.spokes.MqttServer_spoke;
import com.eislab.af.translator.spokes.UaClient_spoke;
import com.eislab.af.translator.spokes.UaServer_spoke;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

interface Translator_hub_i {
	void writeToConsole(String msg);
}

public class Translator_hub {//implements Runnable {

	private int id = 0;	

	public static Properties properties;
	
	BaseSpokeProvider pSpoke;
	private String  pSpoke_ConsumerName = null;
	private String  pSpoke_ConsumerType = null;
	private String  pSpoke_ConsumerAddress = null;
	private String	pSpokeAddress = null;
	private String	pSpokeIp = null;
	private String	pSpokePort = null;
//	private String 	pSpokePath;		//translationPath;// 	= "*";

	BaseSpokeConsumer cSpoke;
	private String  cSpoke_ProviderName = null;
	private String  cSpoke_ProviderType = null;
	//private String  cSpoke_ProviderAddress = null;
	private String 	cSpoke_ProviderAddress;// 	= "coap://127.0.0.1:5692/";
	private String  cSpoke_ProviderPath;
	
	public Translator_hub(String StubConfiguration)	{
		this();
	}
	
	public Translator_hub() {
		loadProperties("translator.properties");
	}
	
	
	// spoke is responsible for translating from protocol specific domain to a generic domain
	// hub is used for chaining spokes in series.
	// the chain must start with a baseprovider and finish with a baseconsumer
	// each spoke in the chain has a generic interface
	
	public void closeProvider(String msg) {
		pSpoke.close();
	}
	
	public void closeConsumer(String msg) {
		cSpoke.close();
	}
	
	public void online() {
		

		try {

			System.out.println("go online: ConsumerSpoke: " + cSpoke_ProviderName + " ProviderSpoke: " + pSpoke_ConsumerName);
			
			if ( pSpoke_ConsumerName.contains("coap") ) {
				pSpoke = new CoapServer_spoke(properties.getProperty("translator.interface.ipaddress"));
			} else if (pSpoke_ConsumerName.contains("http")) {
				//HttpServer_spoke httpserver = new HttpServer_spoke(translationPort, translationPath);
				pSpoke = new HttpServer_spoke(properties.getProperty("translator.interface.ipaddress"), "/*");
			}  else if (pSpoke_ConsumerName.contains("ua")) {
				pSpoke = new UaServer_spoke(properties.getProperty("translator.interface.ipaddress"), "/*");
			} else if (pSpoke_ConsumerName.contains("mqtt")) {
				String brokerUri = "tcp://localhost:1883";//TODO: get these values from ORCHESTRaTOR OR METADATA
				int maxPublishDelay = 1000; //1 second
				boolean MqttServer_spoke = true;
				//home/garden/fountain
				String serviceProviderPath = "/home/garden/fountain";
				pSpoke = new MqttServer_spoke(brokerUri, serviceProviderPath, maxPublishDelay, MqttServer_spoke);
			} else {
			
				System.exit(1);
			}
			
			if(cSpoke_ProviderName.contains("coap")) {
				cSpoke = new CoapClient_spoke(cSpoke_ProviderAddress);
			} else if(cSpoke_ProviderName.contains("http")) {
				cSpoke = new HttpClient_spoke(cSpoke_ProviderAddress);
			} else if(cSpoke_ProviderName.contains("mqtt")) {
//				String brokerUri = "tcp://localhost:1883";//TODO: get these values from ORCHESTRaTOR OR METADATA
				cSpoke = new MqttClient_spoke(cSpoke_ProviderAddress);
			} else if(cSpoke_ProviderName.contains("ua")) {
				cSpoke = new UaClient_spoke(cSpoke_ProviderAddress);
			} else {
				System.exit(1);
			}			
			
			// link the spoke connections 
			pSpoke.setNextSpoke(cSpoke);
			cSpoke.setNextSpoke(pSpoke);

			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					System.out.println("===END===");
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public int getId() {
		if (id == 0) {
			id = (int) (Math.random() * 100000);
		}
		return id;
	}


	public String getPSpoke_ConsumerName() {
		return pSpoke_ConsumerName;
	}

	public void setPSpoke_ConsumerName(String pSpoke_ConsumerName) {
		this.pSpoke_ConsumerName = pSpoke_ConsumerName;
	}

	public String getCSpoke_ProviderName() {
		return cSpoke_ProviderName;
	}

	public void setCSpoke_ProviderName(String cSpoke_ProviderName) {
		this.cSpoke_ProviderName = cSpoke_ProviderName;
	}
	
	//TODO must be composed as url = scheme://ip:port/path or scheme://ip:port/topic or XMPP??? or OPC-UA??? 
	public String getPSpokeAddress() {
		if (pSpokeAddress == null) {
			String address = this.pSpoke.getAddress();
			this.setPSpokeAddress(address);
		}
		return pSpokeAddress;
	}	
	private void setPSpokeAddress(String pSpokeAddress) {
		this.pSpokeAddress = pSpokeAddress;
	}
	// 
	public String getPSpokeIp() {
		if (pSpokeIp == null) {
			String temp = this.pSpoke.getAddress();
			temp = temp.substring(temp.indexOf("//") + 2);
			if(temp.startsWith("[")) {
				temp = temp.substring(0, temp.indexOf("]") + 1);
			} else {
				temp = temp.substring(0, temp.indexOf(":"));
			}
			String pSpokeIp = temp;
			this.setPSpokeIp(pSpokeIp);
		}
		return pSpokeIp;
	}	
	private void setPSpokeIp(String pSpokeIp) {
		this.pSpokeIp = pSpokeIp;
	}
	// 
	public String getPSpokePort() {
		if (pSpokePort == null) {
			String temp = this.pSpoke.getAddress();
			temp = temp.substring(temp.indexOf("//") + 2);
			if(temp.startsWith("[")) {
				temp = temp.substring(temp.indexOf("]") + 2);
				temp = temp.substring(0, temp.indexOf("/"));
			} else {
				temp = temp.substring(temp.indexOf(":") + 1);
			}
			
			String pSpokePort = temp;
			
			this.setPSpokePort(pSpokePort);
		}
		return pSpokePort;
	}
	
	private void setPSpokePort(String pSpokePort) {
		this.pSpokePort = pSpokePort;
	}

//	public String getPSpokePath() {
//		return pSpokePath;
//	}
//
//	public void setPSpokePath(String pSpokePath) {
//		this.pSpokePath = pSpokePath;
//	}

	public String getCSpoke_ProviderAddress() {
		return cSpoke_ProviderAddress;
	}

	public void setCSpoke_ProviderAddress(String cSpoke_ProviderAddress) {
		this.cSpoke_ProviderAddress = cSpoke_ProviderAddress;
	}
	
	/** 
	 * Reads the properties from the file .properties
	 *
	 */
	private static boolean loadProperties(String propertiesFileName) {
		boolean result = false;
		/* Setting up Globals */
		String fileName = propertiesFileName;
		
		/* Read the input properties file and set the properties */
		try {
			Properties props = new Properties();
			props.load(new FileInputStream(fileName));
			properties = props;
			result = true;
		} catch (IOException e) {
			//LOG.severe("Failed to read property file " + fileName + ". Reason: " + e.getMessage());
			System.out.println("Failed to read property file " + fileName + ". Reason: " + e.getMessage());
			System.exit(-1);
		}
		return result;
	}
	
}
