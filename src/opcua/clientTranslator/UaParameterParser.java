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

import java.util.*;
//import java.util.ArrayList;

import org.opcfoundation.ua.core.*;

import opcua.clientTranslator.UaRequester;
import opcua.clientTranslator.UaServiceParser;

import org.opcfoundation.ua.builtintypes.*;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.common.ServiceFaultException;

import java.io.IOException;

public class UaParameterParser {
	//Translator modules
	UaServiceParser parser;
	UaRequester requester;
	
	//Generalized Parameters
	
	//NODE ID (NEW)
	NodeId gNodeId = null;			//Standard Node Id, used for most simple service calls
	NodeId tNodeId = null;			//Type Node Id, used to reference type nodes
	NodeId rNodeId = null;
	NodeId mNodeId = null;			//Method Node Id, used to make method calls
	NodeId sNodeId = null;			//Secondary Standard Node Id, used for service calls requiring a secondary target node (also used as requested new node id in addNodes)
	
	//OTHER
	UnsignedInteger attrId = null;
	String indexrange = null;
	Double pMaxAge = null;
	TimestampsToReturn pTimestampsToReturn = TimestampsToReturn.Neither;
	NodeClass nodeClass = null;
	QualifiedName qName = null;
	
	//DELETE NODES & REFERENCES
	Boolean deleteTargetReferences = false;
	Boolean deleteBidirectional = false;
	Boolean isForward = false;
	
	//TRANSLATE BROWSE PATHS TO NODE IDS
	Boolean isInverse = false;
	Boolean IncludeSubtypes = false;

		
	//Write Value Parameters
	Double writeValueDouble = null;
	
	//Multi-purpose Boolean (different meaning depending on which service is invoked)
	Boolean multiPurposeBoolean = false;
	
	public UaParameterParser(UaServiceParser aParser, UaRequester aRequester){
		parser = aParser;
		requester = aRequester;
	}

	public String WriteServiceRequest(ArrayList<String[]> paramsList){
		
		//define parameter strings
		parseParameters(paramsList);
		
		try {
			//WriteValue(NodeId NodeId, UnsignedInteger AttributeId, java.lang.String IndexRange, DataValue Value)
			
			//NodeId pNodeId = new NodeId(nsindex, name);
			NodeId pNodeId = this.gNodeId;
			
			Variant pVariant = new Variant(this.writeValueDouble);
			DataValue writeValue = new DataValue(pVariant);
			
			WriteValue NodesToWrite = new WriteValue(pNodeId, this.attrId, indexrange, writeValue);
			String response = this.requester.WriteServiceRequest(NodesToWrite);
			return response;
		} catch( NullPointerException npe) {
			return "service fault exception in UaServiceCallParser";			
		}	
	}
	
	public String QueryServiceRequest(ArrayList<String[]> paramsList){
		//define parameter strings
		parseParameters(paramsList);
		
		//ViewDescription
		NodeId viewNodeId = this.gNodeId;
		DateTime now = new DateTime();
		int intViewVersion = 1;
		UnsignedInteger uintViewVersion = new UnsignedInteger(intViewVersion);
		ViewDescription viewDescription = new ViewDescription(viewNodeId, now, uintViewVersion);

		//QueryDataDescription
		QueryDataDescription queryDataDescription = new QueryDataDescription();
		QueryDataDescription[] queryDataDescriptionArray = new QueryDataDescription[1];
		queryDataDescriptionArray[0] = queryDataDescription;
		
		NodeId nid = this.sNodeId;
		ExpandedNodeId enid = new ExpandedNodeId(nid);
		NodeTypeDescription nodeTypes = new NodeTypeDescription(enid, true, queryDataDescriptionArray);
		NodeTypeDescription[] nodeTypesArray = new NodeTypeDescription[1];
		nodeTypesArray[0] = nodeTypes;
		
		try {
			String response = this.requester.QueryServiceRequest(viewDescription, queryDataDescriptionArray, nodeTypesArray);
			return response;
		} catch( NullPointerException npe) {
			npe.printStackTrace();
			return "service fault exception in UaServiceCallParser";	
		}	
	}	
	
	public String BrowseServiceRequest(ArrayList<String[]> paramsList){
		
		//define parameter strings
		parseParameters(paramsList);
		
		try {
			int intReqMaxRefsPerNode = 1;
			UnsignedInteger mRequestedMaxReferencesPerNode = new UnsignedInteger(intReqMaxRefsPerNode);
			
			NodeId pNodeId = this.gNodeId;
			
			BrowseDirection pBrowseDirection = BrowseDirection.Both;
			NodeId pReferenceTypeId = this.tNodeId;
			java.lang.Boolean pIncludeSubtypes = true;
			int intNodeClassMask = 63;
			UnsignedInteger mNodeClassMask = new UnsignedInteger(intNodeClassMask);
			int intResultMask = 63;
			UnsignedInteger mResultMask = new UnsignedInteger(intResultMask);
			BrowseDescription mNodesToBrowse = new BrowseDescription(pNodeId, pBrowseDirection, pReferenceTypeId, pIncludeSubtypes, mNodeClassMask, mResultMask);
			
			String response = this.requester.BrowseServiceRequest(null, mRequestedMaxReferencesPerNode, mNodesToBrowse);
			return response;
		} catch( NullPointerException npe) {
			return "service fault exception in UaServiceCallParser";			
		}			
	}
	
	public String TranslateBrowsePathsToNodeIds(ArrayList<String[]> paramsList){
		parseParameters(paramsList);

		try {
			//Starting Node
			//NodeId pNodeId = new NodeId(this.nsindex, this.name);
			NodeId pNodeId = this.gNodeId;
			
			//Reference Type Id Node Id
			NodeId pReferenceTypeId = this.tNodeId;
			
			//Reference Filter Parameters
			Boolean pIsInverse = this.isInverse;
			Boolean pIncludeSubtypes = this.IncludeSubtypes;
			
			//QualifiedName
			QualifiedName pTargetName = this.qName;
			
			RelativePathElement pRelativePathElement = new RelativePathElement(pReferenceTypeId, pIsInverse, pIncludeSubtypes, pTargetName);
			RelativePathElement[] pRelativePathElementArray = new RelativePathElement[1];
			pRelativePathElementArray[0] = pRelativePathElement;
			RelativePath pRelativePath = new RelativePath(pRelativePathElementArray);
			String response = this.requester.TranslateBrowsePathsToNodeIds(pNodeId, pRelativePath);
			return response;
		} catch( NullPointerException npe) {
			return "service fault exception in UaServiceCallParser";			
		}
	}
	
	public String ReadServiceRequest(ArrayList<String[]> paramsList){
		
		//Tier 2 parameters
		ReadValueId pNodesToRead = null;
		TimestampsToReturn pTimestampsToReturn = TimestampsToReturn.Both;
		
		//ReadValueId[] pReadValueId = null;
		
		//define parameter strings
		parseParameters(paramsList);
		
		//ReadValueId(NodeId NodeId, UnsignedInteger AttributeId, java.lang.String IndexRange, QualifiedName DataEncoding)
		
		//TimestampsToReturn.valueOf(this.RequestedTimeStamps)
		
		NodeId pNodeId = this.gNodeId;
		pNodesToRead = new ReadValueId(pNodeId, this.attrId, this.indexrange, QualifiedName.NULL);
		
		pNodesToRead = new ReadValueId(pNodeId, this.attrId, this.indexrange, QualifiedName.NULL);
		
		//NodeId pNodeId = new NodeId(1, "1020ffaa");
		//int i3 = 3;
		//UnsignedInteger ui3 = new UnsignedInteger(i3);
		//pNodesToRead = new ReadValueId(pNodeId, ui3, null, QualifiedName.NULL);
		String response = null;
		try {
			if(pMaxAge != null && pTimestampsToReturn != null && pNodesToRead != null){
				
				//NodeId aNodeId = new NodeId(1, "1020FFAA");
				//java.lang.String index = "0";
				//UnsignedInteger uInt = new UnsignedInteger("13");
				//ReadValueId aNodesToRead = new ReadValueId(aNodeId, uInt, index, QualifiedName.NULL);
				
				response = this.requester.ReadServiceRequest(this.pMaxAge, pTimestampsToReturn, pNodesToRead);
			} else {
				if(this.pMaxAge == null){
					return "bad max age";
				}
				if(pTimestampsToReturn == null){
					return "bad timstamps";
				}
				return "insufficient parameters";
			}
		} catch( ServiceFaultException sfe) {
			return "service fault exception in UaServiceCallParser";			
		} catch (ServiceResultException sre ){
			return "service result exception in UaServiceCallParser";
		} finally {
			
		}		
		//Execute Service Request Here at the bottom
		//this.sRequester.ReadServiceRequest("","","");
		//resultString = this.ParserServiceParser.ReadServiceRequest(params);
		return response;
	}

	public String AddNodesServiceRequest(ArrayList<String[]> paramsList){
		parseParameters(paramsList);
		
		NodeId pNodeId = this.gNodeId;
		ExpandedNodeId pENodeId = new ExpandedNodeId(pNodeId);					//Parent Node Id
		
		NodeId rNodeId = this.rNodeId;											//Reference Type Node Id (for the reference between parent node and new node)
		
		ExpandedNodeId expandedNewNodeId = new ExpandedNodeId(this.sNodeId);	//Requested Node Id of the added node
		
		QualifiedName BrowseName = this.qName;
		
		NodeClass nc = this.nodeClass;
		
		ExpandedNodeId etdNodeId = new ExpandedNodeId(this.tNodeId);
				
		AddNodesItem ani = new AddNodesItem(pENodeId, rNodeId, expandedNewNodeId, BrowseName, nc, null, etdNodeId);
		AddNodesItem[] aniArray = new AddNodesItem[1];
		aniArray[0] = ani;
		
		//String response = this.sRequester.AddNodesRequest(aniArray);
		String response = this.requester.AddNodesRequest(ani);
		
		return response;
	}
	
	public String DeleteNodesServiceRequest(ArrayList<String[]> paramsList){
		parseParameters(paramsList);
		
		NodeId pNodeId = this.gNodeId;
		Boolean pDeleteTargetReferences = this.deleteTargetReferences;
		String response = this.requester.DeleteNodesRequest(pNodeId, this.multiPurposeBoolean);
		
		return response;
	}
	
	public String AddReferencesServiceRequest(ArrayList<String[]> paramsList){
		parseParameters(paramsList);
		
		NodeId pSourceNodeId = this.gNodeId;
		NodeId pReferenceTypeId = this.tNodeId;
		Boolean pIsForward = this.isForward;
		//String pTargetServerUri
		
		ExpandedNodeId pExpandedTargetNodeId = new ExpandedNodeId(this.sNodeId);
		
		this.nodeClass = NodeClass.Variable;
		
		if(this.sNodeId != null){
			String response = this.requester.AddReferencesRequest(pSourceNodeId, pReferenceTypeId, pIsForward, null, pExpandedTargetNodeId, this.nodeClass);
			return response;
		} else {
			return "null node id";
		}
	}

	public String DeleteReferencesServiceRequest(ArrayList<String[]> paramsList){
		parseParameters(paramsList);
		
		NodeId pSourceNodeId = this.tNodeId;
		NodeId pReferenceTypeId = this.tNodeId;
		
		//String pTargetServerUri
		
		NodeId pTargetNodeId = this.sNodeId;
		ExpandedNodeId pExpandedTargetNodeId = new ExpandedNodeId(pTargetNodeId);
		
		if(pTargetNodeId != null){
			String response = this.requester.DeleteReferencesRequest(pSourceNodeId, pReferenceTypeId, this.isForward, pExpandedTargetNodeId, this.deleteBidirectional);
			return response;
		} else {
			return "null node id";
		}
	}
	
	public String CallMethodServiceRequest(ArrayList<String[]> paramsList){
		parseParameters(paramsList);
		
		NodeId ObjectId = this.gNodeId;
		NodeId MethodId = this.mNodeId;
		//Variant[] InputArguments = null;
		
		CallMethodRequest pMethodsToCall = new CallMethodRequest(ObjectId, MethodId, null);//Variant[] InputArguments);
		String response = this.requester.CallMethodRequest(pMethodsToCall);
		return response;
	}
	
	private void parseParameters(ArrayList<String[]> paramsList2){
		for(int i=1; i<paramsList2.size(); i++){
			String[] paramArray =  paramsList2.get(i);
			String paramType = paramArray[0];
			String paramArg = paramArray[1];
			
			//For Node ID split
			String[] nString = null;
			
			switch (paramType){
            case "nodeidstring":
            	nString = paramArg.split("\\:");
            	this.gNodeId = new NodeId(Integer.parseInt(nString[0]), nString[1]);
                break;
                
            case "nodeidint":
            	nString = paramArg.split("\\:");
            	this.gNodeId = new NodeId(Integer.parseInt(nString[0]), Integer.parseInt(nString[1]));
                break;
                
            case "sourcenodeidstring":
            	nString = paramArg.split("\\:");
            	this.sNodeId = new NodeId(Integer.parseInt(nString[0]), nString[1]);
                break;
                
            case "sourcenodeidint":
            	nString = paramArg.split("\\:");
            	this.sNodeId = new NodeId(Integer.parseInt(nString[0]), Integer.parseInt(nString[1]));
                break;
                
            case "methodnodeidstring":
            	nString = paramArg.split("\\:");
            	this.mNodeId = new NodeId(Integer.parseInt(nString[0]), nString[1]);
                break;
                
            case "methodnodeidint":
            	nString = paramArg.split("\\:");
            	this.mNodeId = new NodeId(Integer.parseInt(nString[0]), Integer.parseInt(nString[1]));
                break;
                
            case "attributeid":
            	this.attrId = new UnsignedInteger(paramArg);
            	System.out.print("attr id set to: " + this.attrId + "\n");
                break;
                
            case "typenodeidstring":
            	nString = paramArg.split("\\:");
            	this.tNodeId = new NodeId(Integer.parseInt(nString[0]), nString[1]);
                break;
                
            case "typenodeidint":
            	nString = paramArg.split("\\:");
            	this.tNodeId = new NodeId(Integer.parseInt(nString[0]), Integer.parseInt(nString[1]));
                break;
                
            case "refnodeidstring":
            	nString = paramArg.split("\\:");
            	this.rNodeId = new NodeId(Integer.parseInt(nString[0]), nString[1]);
                break;
                
            case "refnodeidint":
            	nString = paramArg.split("\\:");
            	this.rNodeId = new NodeId(Integer.parseInt(nString[0]), Integer.parseInt(nString[1]));
                break;
                
            case "qualifiedname":
            	nString = paramArg.split("\\:");
            	this.qName = new QualifiedName(Integer.parseInt(nString[0]), nString[1]);	
                break;
            	
            case "isinverse":
            	this.isInverse = Boolean.parseBoolean(paramArg);
            	break;
            	
            case "includesubtypes":
            	this.IncludeSubtypes = Boolean.parseBoolean(paramArg);
            	break;
                
            case "indexrange":
            	indexrange = paramArg;
            	System.out.print("indexrange set to: " + this.indexrange + "\n");
                break;
                
            case "timestamps":
                this.pTimestampsToReturn = assertTimestampEnum(paramArg);
                break;
                
            case "maxage":
            	this.pMaxAge = this.toDouble(paramArg);
            	System.out.print("maxage set to: " + this.pMaxAge + "\n");
                break;
                
            case "writevalue":
            	this.writeValueDouble = this.toDouble(paramArg);
                break;
                
            case "deletetargetrefs":
            	this.deleteTargetReferences = Boolean.valueOf(paramArg);
                break;
                
            case "deletebidirectional":
            	this.deleteBidirectional = Boolean.valueOf(paramArg);
                break;
                
            case "isforward":
            	this.isForward = Boolean.valueOf(paramArg);
                break;
                  
            case "nodeclass":
            	try{
            		this.nodeClass = NodeClass.valueOf(paramArg);
            	} catch (java.lang.IllegalArgumentException iae){
            		System.out.print("Illegal Argument Exception on parsing NodeClass string:\n");
            		iae.printStackTrace();
            	} catch (java.lang.NullPointerException npe){
            		System.out.print("Null Pointer Exception on parsing NodeClass string\n");
            		npe.printStackTrace();
            	}
                break;
                
                
            default: 
            	//default action
            	//TODO: handle unknown parameters 
                break;
        	}
		}
	}
	
	public TimestampsToReturn assertTimestampEnum(String tsParam){
		TimestampsToReturn tsReturn = null;
		
		switch (tsParam){
		
		case "both":
        	tsReturn = TimestampsToReturn.Both;
            break;
            
		case "neither":
        	tsReturn = TimestampsToReturn.Neither;
            break;
            
		case "server":
        	tsReturn = TimestampsToReturn.Server;
            break;
            
		case "source":
        	tsReturn = TimestampsToReturn.Source;
            break;
            
        default: 
        	//default action
        	//TODO: handle unknown parameters 
            break;
    	}
		return tsReturn;
	}
	
	public Double toDouble(String input){
		Double paramDouble;
		try {
			paramDouble = Double.parseDouble(input);
		} catch (NumberFormatException nfe) {
			return null;
		} catch (NullPointerException npe) {
			return null;
		}
		return paramDouble;
	}
	
	public int toInt(String input1){
		try {
			int input1Int = Integer.parseInt(input1);
			return input1Int;
		} catch (NumberFormatException nfe) {
			return 0;
		} catch (NullPointerException npe) {
			return 0;
		}
	}
	
	public UnsignedInteger toUInt(String input1){
		try {
			UnsignedInteger input1UInt = UnsignedInteger.parseUnsignedInteger(input1);
			return input1UInt;
		} catch (NumberFormatException nfe) {
			return UnsignedInteger.ZERO;
		} catch (NullPointerException npe) {
			return UnsignedInteger.ZERO;
		} catch (IllegalArgumentException iae) {
			return UnsignedInteger.ZERO;
		}
	}
	
	public java.lang.Boolean toBoolean(String input1){
		try {
			java.lang.Boolean pBoolean = Boolean.valueOf(input1);
			return pBoolean;
		} catch (NullPointerException npe){
			return false;
		}
	}
	
	public QualifiedName toQualifiedName(String namespaceInt, String name){
		UnsignedShort uShort = null;
		QualifiedName qName = null;
		
		try {
			uShort = UnsignedShort.valueOf(Integer.parseInt(namespaceInt));
			qName = new QualifiedName(uShort, name);
		} catch (NumberFormatException nfe) {
			return null;
		} catch (NullPointerException npe) {
			return null;
		}
			return qName;
	}
}
