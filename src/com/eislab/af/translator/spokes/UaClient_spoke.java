package com.eislab.af.translator.spokes;

//DEMO VERSION

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import com.eislab.af.translator.data.BaseContext;

import opcua.clientTranslator.UaClientConnector;
import opcua.clientTranslator.UaRequest;

public class UaClient_spoke implements BaseSpokeConsumer {
	BaseSpoke nextSpoke;
	UaClientConnector internalClient;
	String providerAddress;								//address of target opcua server, defined in constructor
	String spokeQuery;
	ArrayList<String> requestParameters = null;
	
	public UaClient_spoke(String ProviderAddress) {
		this.providerAddress = pathString(ProviderAddress);
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
			//response = handleRequest(context); //TODO: fix a proper output
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response = this.handleRequest(context);
		context.setContent("RESPONSE:\n" + response);
		this.nextSpoke.in(context);
	}
	
	public String queryString(String addressQuery){
	      String delimeter = "\\?";
	      String[] temp = addressQuery.split(delimeter);
	      return temp[1];
	}
	
	public String pathString(String addressQuery){
	      String delimeter = "\\?";
	      String[] temp = addressQuery.split(delimeter);
	      return temp[0];
	}
	
	/*public String concatenateContextAndResponse(BaseContext context, String response){
		String localResponse = "REQUEST:\nContentType: " + context.getContentType() + "\nContent: " + context.getContent() + "\nKey: " + context.getKey() + "\nMethod: " + context.getMethod() + "\nPath: " + context.getPath() + "\n\nRESPONSE: " + response;
		return localResponse;
	}*/
	
	public String handleRequest(BaseContext context){
		String response;
		
		String query = queryString(context.getPath());
		String path = pathString(context.getPath());
		String content = context.getContent();
		String method = context.getMethod();
		response = this.internalClient.handleRequest(path, query, content, method);
		
		return response;
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
