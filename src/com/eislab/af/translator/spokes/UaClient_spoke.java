package com.eislab.af.translator.spokes;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import com.eislab.af.translator.data.BaseContext;

import opcua.clientTranslator.UaClientConnector;
import opcua.clientTranslator.UaRequester;
import opcua.clientTranslator.UaServiceParser;

public class UaClient_spoke implements BaseSpokeConsumer {
	BaseSpoke nextSpoke;
	UaClientConnector internalClient;
	String providerAddress;								//address of target opcua server, defined in constructor
	String spokeQuery;
	ArrayList<String> requestParameters = null;
	
	public UaClient_spoke(String ProviderAddress) {
		this.providerAddress = addressString(ProviderAddress);
		this.spokeQuery = queryString(ProviderAddress);
		
		try {
			this.internalClient = new UaClientConnector(this.providerAddress, this.spokeQuery);
			//TODO: Other, more specific exceptions that are implemented in UaClientConnector: UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException
		} catch(Throwable e){
			e.printStackTrace();
		}
	}

	@Override
	public void in(BaseContext context) {
		
		this.requestParameters = new ArrayList<String>();
		
		String response = null;
		
		try {
			if(this.internalClient == null){
				response = "cannot access internal client";
			} else if(this.internalClient.ClientUaParser == null) {
				response = "cannot access parser";
			} else {
				response = handleRequest(queryString(context.getPath()));
			}

		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.print(response);

		if (response == null) {
			context.setContent("null response from client connector");
		} else {
			String finalResponse = concatenateContextAndResponse(context, response);
			context.setContent(finalResponse);
		}
		this.nextSpoke.in(context);
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
	
	public String concatenateContextAndResponse(BaseContext context, String response){
		String localResponse = "REQUEST:\nContentType: " + context.getContentType() + "\nContent: " + context.getContent() + "\nKey: " + context.getKey() + "\nMethod: " + context.getMethod() + "\nPath: " + context.getPath() + "\n\nRESPONSE: " + response;
		return localResponse;
	}
	
	public String handleRequest(String requestQuery){
		return this.internalClient.ClientUaParser.parseQuery(requestQuery);
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
