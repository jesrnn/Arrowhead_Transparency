/**
 * Copyright (c) <2016> <jesrnn>
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

package opcua.clientTranslator;

import java.util.ArrayList;
import java.util.Iterator;
import java.net.InetAddress;
import java.io.IOException;

import org.opcfoundation.ua.builtintypes.*;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.WriteValue;

import opcua.clientTranslator.*;
import opcua.clientTranslator.UaRequest;
import opcua.clientTranslator.UaRequest.ServiceType;

import org.json.simple.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.json.simple.parser.JSONParser;

public class RequestAggregate {
	public Method method;
	public String path;
	public String queryString;
	public String payload;
	
	private UaClientConnector parentClient;
	private NodeId targetNode;
	private ArrayList<UaRequest> uarequests;
	
	private UaParamBank paramBank;
	private String response;
	
	public enum Method {
		get, put, post, delete
	}
	
	public RequestAggregate(UaClientConnector client, Method method, String path, String queryString, String payload){
		this.method = method;
		this.path = path;
		this.queryString = queryString;
		this.payload = payload;
		this.parentClient = client;
		this.paramBank = new UaParamBank();

		NodeId targetNode;
		
		try {
			parsePayload(payload);
			parsePath();
			parseQueryString();
			parseMethod();
			
			
		} catch(Throwable tbl){
			tbl.printStackTrace();
		}

	}
	
	private void parsePath(){
		String[] pathComponents = this.path.split("/");
		
		int attribute = 0;
		String name = null;
		int namespace = 0;
		
		if(pathComponents.length >= 3){
			if(this.method == Method.get){
				name = pathComponents[(pathComponents.length-1)];
				namespace = Integer.parseInt(pathComponents[(pathComponents.length-2)]);
				
				//Hardcoded attribute table, since no specifications on formatting is yet imposed
				this.paramBank.addTargetAttribute(1);
				this.paramBank.addTargetAttribute(2);
				this.paramBank.addTargetAttribute(3);
				this.paramBank.addTargetAttribute(4);
				this.paramBank.addTargetAttribute(5);
				this.paramBank.addTargetAttribute(8);
				this.paramBank.addTargetAttribute(9);
				this.paramBank.addTargetAttribute(10);
				this.paramBank.addTargetAttribute(13);
				this.paramBank.addTargetAttribute(14);
				
			}else if(this.method == Method.put && pathComponents.length >= 4){
				attribute = Integer.parseInt(pathComponents[(pathComponents.length-1)]);
				name = pathComponents[(pathComponents.length-2)];
				namespace = Integer.parseInt(pathComponents[(pathComponents.length-3)]);
				
				this.paramBank.addTargetAttribute(attribute);
			}
			this.paramBank.setTargetNode(namespace, name);
		}
	}
	
	private void parseQueryString(){
		if(this.queryString != null){
	    	String[] params = this.queryString.split("\\;");
	    	
			ArrayList<String[]> paramsList = new ArrayList<String[]>();
			
			for(int i=0; i<params.length ; i++){
				String[] paramPair = new String[2];
				paramPair = params[i].split("\\=");
				
				if(paramPair.length == 2){
					paramsList.add(paramPair);
				} else {
					System.out.print("Invalid Parameter Pair: " + params[i] + "\n");
				}
			}
			// this.paramPairs = paramsList;
			
			processQueryParams(paramsList);
		} else {
			System.out.print("NULL QUERY STRING!\n");
		}
	}
	
	private void parseMethod() throws Throwable{
		switch (this.method) {
		case get:
			GETaction();
            break;
		
        case put:
        	PUTaction();
            break;
            
        default: 
            break;
    	}
	}
	
	private void parsePayload(String payload) throws Throwable{
		String name;
		Double writeValue = 0.0;
		int attributeIndexInt = 0;
		
		JSONParser parser = new JSONParser();
		System.out.println("PAYLOAD: \n" + payload);
		try {
	    	  Object obj3 = parser.parse(payload);
	    	  JSONArray array = (JSONArray) obj3;
	    	  
	    	  for(int i=0; i <= (array.size()-1);i++){
	    		  JSONObject arrayElement= (JSONObject) array.get(i);
	    		  JSONObject node = (JSONObject) arrayElement.get("node");
	    		  
	    		  String nameTemp = (String) node.get("name");
	    		  name = nameTemp;
	    		  
	    		  long attributeIndex = (long) node.get("atr");
	    		  Long attributeIndexLong = new Long (attributeIndex);
	    		  attributeIndexInt = attributeIndexLong.intValue();
	    		  
	    		  long value = (long) node.get("val");
	    		  Long valueLong = new Long (value);
	    		  writeValue = valueLong.doubleValue();
	    		  
	    		  PayloadNode payloadNode = new PayloadNode(name, attributeIndexInt, writeValue);
	    		  this.paramBank.payloadNodes.add(payloadNode);
	    		  System.out.println("ADDED PAYLOAD NODE IN PARAMBANK!");
	    		  System.out.println("name: " + name);
	    		  System.out.println("atr: " + attributeIndexInt);
	    		  System.out.println("value: " + writeValue);
	    	  }
	    	  
	  	} catch (ParseException e) {
	  		e.printStackTrace();
	  	} finally {
	  		
	  	}
	}
	
	private void GETaction() throws Throwable {
	 	UaRequest readRequest = new UaRequest(this.parentClient, ServiceType.Read, this.paramBank);
    	extendResponse(readRequest.getResponse());
    	
    	UaRequest browseRequest = new UaRequest(this.parentClient, ServiceType.Browse, this.paramBank);
    	extendResponse(browseRequest.getResponse());
	}
	
	private void PUTaction() throws Throwable {
		NodeId refTypeId = new NodeId(0, 47);
    	UaRequest browseRequest = new UaRequest(this.parentClient, ServiceType.Browse, this.paramBank, refTypeId);
    	this.paramBank.defineBrowsedNodeResultNodes(browseRequest.getBrowseResultNodes());
    	
		UaRequest writeRequest = new UaRequest(this.parentClient, ServiceType.Write, this.paramBank);
    	extendResponse(writeRequest.getResponse());
	}
	
	private void extendResponse(String newResponse){
		this.response = this.response + "\n" + newResponse;
	}
	
	public String getResponse(){
		return this.response;
	}
	
	public void processQueryParams(ArrayList<String[]> paramsList){
		for(int i=0; i<paramsList.size(); i++){
			String[] paramArray =  paramsList.get(i);
			String paramType = paramArray[0];
			String paramArg = paramArray[1];
			
			switch (paramType){
            case "value":
            	this.paramBank.writeValueDouble = Double.valueOf(paramArg);
                break;
                
            default: 
            	//default action
            	//TODO: handle unknown parameters 
                break;
        	}
		}
	}
	
	public class UaParamBank {
		private NodeId targetNode;
		private WriteValue[] predefinedNodesToWrite;
		private NodeId referenceType;
		private ArrayList<UnsignedInteger> targetNodeAttributes;
		private Double writeValueDouble;
		
		private ArrayList<PayloadNode> payloadNodes;
		private ExpandedNodeId[] BrowsedNodeResultNodes = null;
		
		public UaParamBank(){
			this.targetNodeAttributes = new ArrayList<UnsignedInteger>();
			this.payloadNodes = new ArrayList<PayloadNode>();
		}
		
		public void setTargetNode(int namespace, String name){
			if(isIntegerFormat(name)){
				this.targetNode = new NodeId(namespace, Integer.parseInt(name));
			} else {
				this.targetNode = new NodeId(namespace, name);
			}
		}
		
		public boolean isIntegerFormat(String name){
			boolean returnValue = false;
			try{
				Integer.parseInt(name);
				returnValue = true;
			}catch(NumberFormatException nfe){
				returnValue = false;
			}catch(NullPointerException npe){
				returnValue = false;
			}
			return returnValue;
		}
		
		public void addTargetAttribute(int targetAttributeInt){
			UnsignedInteger targetAttribute = UnsignedInteger.parseUnsignedInteger(String.valueOf(targetAttributeInt));
			this.targetNodeAttributes.add(targetAttribute);
		}
		
		public void setBrowseReferenceTypeFilterNodeId(NodeId referenceType){
			this.referenceType = referenceType;
		}
		
		public void defineBrowsedNodeResultNodes(ExpandedNodeId[] nodes){
			this.BrowsedNodeResultNodes = nodes;
		}
			
		public WriteValue[] getNodesToWrite(){
			NodeId nodeId = this.targetNode;
			Variant variant = new Variant(this.writeValueDouble);
			DataValue writeValue = new DataValue(variant);
			WriteValue[] nodesToWrite;
			ArrayList<WriteValue> writeNodeMatches = new ArrayList<WriteValue>();
			
			int nsindex = 5; //TODO: remove hardcoded namespace index, restore nsindex below!
			
			System.out.println("number of payload nodes: " + payloadNodes.size());
			if(this.payloadNodes.size() > 0){ //&& this.BrowsedNodeResultNodes.length > 0){
				
				for (int p=0; p<(payloadNodes.size()); p++){
					
					NodeId localNodeId = new NodeId(nsindex, payloadNodes.get(p).name); //TODO: remove this iteration of localNodeId and restore below!
					
					/*BEGINNING OF TEMPORARY BLOCK*/
					UnsignedInteger attributeUInt = new UnsignedInteger(payloadNodes.get(p).attribute);
					Variant variant2 = new Variant(payloadNodes.get(p).value);
					DataValue writeValue2 = new DataValue(variant2);
					
					WriteValue wVal = new WriteValue(localNodeId, attributeUInt, null, writeValue2);
					writeNodeMatches.add(wVal);
					/*END OF TEMPORARY BLOCK*/
					
					for (int k=0; k<(this.BrowsedNodeResultNodes.length); k++){
						
						//int nsindex = this.BrowsedNodeResultNodes[k].getNamespaceIndex();
						
						
						
						//NodeId localNodeId = new NodeId(nsindex, payloadNodes.get(p).name);
						ExpandedNodeId localExpandedNodeId = new ExpandedNodeId(localNodeId);
						
						//System.out.println("name: " + payloadNodes.get(p).name);
						//System.out.println("attribute: " + payloadNodes.get(p).attribute);
						//System.out.println("value: " + payloadNodes.get(p).value);
						
						/*
						if(localExpandedNodeId.getValue() == this.BrowsedNodeResultNodes[k].getValue()){
							
							System.out.println("NODE MATCH!");
							
							UnsignedInteger attributeUInt = new UnsignedInteger(payloadNodes.get(p).attribute);
							Variant variant2 = new Variant(payloadNodes.get(p).value);
							DataValue writeValue2 = new DataValue(variant2);
							
							WriteValue wVal = new WriteValue(localNodeId, attributeUInt, null, writeValue2);
							writeNodeMatches.add(wVal);
						} else {
							System.out.println("NO NODE MATCH!");
						}
						*/
						
						
						
					}
				}
				
				nodesToWrite = new WriteValue[writeNodeMatches.size()];
				for (int i=0; i<writeNodeMatches.size(); i++){
					nodesToWrite[i] = writeNodeMatches.get(i);
				}
				
			} else {
				nodesToWrite = new WriteValue[this.targetNodeAttributes.size()];
				
				for (int i=0; i<(this.targetNodeAttributes.size()); i++){
					nodesToWrite[i] = new WriteValue(nodeId, this.targetNodeAttributes.get(i), null, writeValue);
				}
			}
			
			

			return nodesToWrite;
		}
		
		public BrowseDescription getNodesToBrowse(){
			int intReqMaxRefsPerNode = 0;
			UnsignedInteger mRequestedMaxReferencesPerNode = new UnsignedInteger(intReqMaxRefsPerNode);
			NodeId pNodeId = this.targetNode;
			BrowseDirection browseDirection = BrowseDirection.Both;
			NodeId referenceTypeId = null;
			
			if(this.referenceType != null){
				referenceTypeId = this.referenceType;
			}
			
			java.lang.Boolean includeSubtypes = true;
			int intNodeClassMask = 63;
			UnsignedInteger nodeClassMask = new UnsignedInteger(intNodeClassMask);
			int intResultMask = 63;
			UnsignedInteger resultMask = new UnsignedInteger(intResultMask);
			BrowseDescription nodesToBrowse = new BrowseDescription(pNodeId, browseDirection, referenceTypeId, includeSubtypes, nodeClassMask, resultMask);
			
			return nodesToBrowse;
		}
		
		public ReadValueId[] getNodesToRead(){
			TimestampsToReturn pTimestampsToReturn = TimestampsToReturn.Both;
			NodeId nodeId = this.targetNode;
			
			ReadValueId[] nodesToRead = new ReadValueId[this.targetNodeAttributes.size()];
					
			for (int i=0; i<(this.targetNodeAttributes.size()-1); i++){
				nodesToRead[i] = new ReadValueId(nodeId, this.targetNodeAttributes.get(i), null, QualifiedName.NULL);
			}
			
			return nodesToRead;
		}
		
		
	}

	public class PayloadNode{
		public String name;
		public int attribute;
		public Double value;
		
		public PayloadNode(String name, int atr, Double val){
			this.name = name;
			this.attribute = atr;
			this.value = val;
		}
	}

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}
