package com.eislab.af.translator.spokes;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import opcua.client.UaClientConnector;

import com.eislab.af.translator.data.BaseContext;
import opcua.client.UaRequester;
import opcua.client.UaParser;
import opcua.client.UaClientConnector;

public class UaClient_spoke implements BaseSpokeConsumer {
	BaseSpoke nextSpoke;
	UaClientConnector internalClient;
	String providerAddress;								//address of target opcua server, defined in constructor
	String query;
	ArrayList<String> requestParameters = null;
	
	public UaClient_spoke(String ProviderAddress, String queryStr) {
		//this.providerAddress = "opc.tcp://MCOREII:4334/UA/MyLittleServer";
		
		this.providerAddress = addressString(ProviderAddress);
		this.query = queryString(ProviderAddress);
		
		try {
			this.internalClient = new UaClientConnector(this.providerAddress);
			//TODO: Other, more specific exceptions that are implemented in UaClientConnector: UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
		} catch(Throwable e){
			e.printStackTrace();
		}
	}

	@Override
	public void in(BaseContext context) {
	
		// TODO Auto-generated method stub
		this.requestParameters = new ArrayList<String>();
		
		String response = null;
		
		try {
			
			if(this.internalClient == null){
				response = "internalClient is null";
			} else if(this.internalClient.ClientUaParser == null) {
				response = "ClientUaParser is null";
			} else {
				//if there is an internal client created that has a UaParser - hand off the query string to the parser
				//response = this.internalClient.ClientUaRequester.ReadServiceRequest(ns_index, nodeId, attrId);
				response = this.internalClient.ClientUaParser.parseQuery(this.query);
				//response = this.query;
			}
			

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.print(response);
		
		if (response == null) {
			context.setContent("null response from UaClientConnector");
		} else {
			context.setContent(response);
		}
		
		//TODO: remove commented nextSpoke method below to restore integrated functionality
		//this.nextSpoke.in(context);
	}
	
	public String queryString(String addressQuery){
	      String delimeter = "\\?";
	      String[] temp = addressQuery.split(delimeter);
	      
	      return temp[1];
	}
	
	public String addressString(String addressQuery){
	      String delimeter = "\\?";
	      String[] temp = addressQuery.split(delimeter);
	      
	      return temp[0];
	}

	@Override
	public void setNextSpoke(Object nextSpoke) {
		// TODO Auto-generated method stub
		this.nextSpoke = (BaseSpoke) nextSpoke;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
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
