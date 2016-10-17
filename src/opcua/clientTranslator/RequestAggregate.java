package opcua.clientTranslator;

import java.util.ArrayList;

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

public class RequestAggregate {
	public Method method;
	public String path;
	public String queryString;
	public String payload;
	
	private UaClientConnector parentClient;
	private NodeId targetNode;
	//private ArrayList<String[]> paramPairs;
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
				
				//Hardcoded attribute table
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
	
	private void GETaction() throws Throwable {
	 	UaRequest readRequest = new UaRequest(this.parentClient, ServiceType.Read, this.paramBank);
    	extendResponse(readRequest.getResponse());
    	
    	UaRequest browseRequest = new UaRequest(this.parentClient, ServiceType.Browse, this.paramBank);
    	extendResponse(browseRequest.getResponse());
	}
	
	private void PUTaction() throws Throwable {
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
		for(int i=1; i<paramsList.size(); i++){
			String[] paramArray =  paramsList.get(i);
			String paramType = paramArray[0];
			String paramArg = paramArray[1];
			
			switch (paramType){
            case "value":
            	this.paramBank.writeValueDouble = Double.parseDouble(paramArg);
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
		private ArrayList<UnsignedInteger> targetNodeAttributes;
		private Double writeValueDouble;
		
		public UaParamBank(){
			this.targetNodeAttributes = new ArrayList<UnsignedInteger>();
		}
		
		public void setTargetNode(int namespace, String name){
			this.targetNode = new NodeId(namespace, name);
		}
		
		public void addTargetAttribute(int targetAttributeInt){
			UnsignedInteger targetAttribute = UnsignedInteger.parseUnsignedInteger(String.valueOf(targetAttributeInt));
			this.targetNodeAttributes.add(targetAttribute);
		}
		
		public WriteValue[] getNodesToWrite(){
			NodeId nodeId = this.targetNode;
			Variant pVariant = new Variant(this.writeValueDouble);
			DataValue writeValue = new DataValue(pVariant);
			
			WriteValue[] nodesToWrite = new WriteValue[this.targetNodeAttributes.size()];
			
			for (int i=0; i<(this.targetNodeAttributes.size()-1); i++){
				nodesToWrite[i] = new WriteValue(nodeId, this.targetNodeAttributes.get(i), null, writeValue);
			}
			return nodesToWrite;
		}
		
		public BrowseDescription getNodesToBrowse(){
			int intReqMaxRefsPerNode = 0;
			UnsignedInteger mRequestedMaxReferencesPerNode = new UnsignedInteger(intReqMaxRefsPerNode);
			NodeId pNodeId = this.targetNode;
			BrowseDirection browseDirection = BrowseDirection.Both;
			
			NodeId referenceTypeId = null;//this.tNodeId;
			
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

	

	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	
}