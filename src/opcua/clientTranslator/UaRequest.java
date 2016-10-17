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

import static org.opcfoundation.ua.utils.EndpointUtil.selectByMessageSecurityMode;
import static org.opcfoundation.ua.utils.EndpointUtil.selectByProtocol;
import static org.opcfoundation.ua.utils.EndpointUtil.selectBySecurityPolicy;
import static org.opcfoundation.ua.utils.EndpointUtil.sortBySecurityLevel;

import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.Session;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.ServiceResult;
import org.opcfoundation.ua.builtintypes.DataValue;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.builtintypes.UnsignedShort;
import org.opcfoundation.ua.builtintypes.Variant;
import org.opcfoundation.ua.core.BrowseDescription;
import org.opcfoundation.ua.core.BrowseDirection;
import org.opcfoundation.ua.core.ViewDescription;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.common.ServiceFaultException;
import org.opcfoundation.ua.core.ActivateSessionResponse;
import org.opcfoundation.ua.core.BrowseResponse;
import org.opcfoundation.ua.core.TranslateBrowsePathsToNodeIdsResponse;
import org.opcfoundation.ua.core.CloseSessionRequest;
import org.opcfoundation.ua.core.CloseSessionResponse;
import org.opcfoundation.ua.core.QueryFirstResponse;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.Node;
import org.opcfoundation.ua.core.NodeClass;
import org.opcfoundation.ua.core.NodeTypeDescription;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadRequest;
import org.opcfoundation.ua.core.WriteResponse;
import org.opcfoundation.ua.core.WriteRequest;
import org.opcfoundation.ua.core.ViewDescription;
import org.opcfoundation.ua.core.NodeTypeDescription;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.core.FilterOperator;
import org.opcfoundation.ua.builtintypes.ExpandedNodeId;
import org.opcfoundation.ua.core.QueryDataDescription;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.core.DeleteNodesItem;
import org.opcfoundation.ua.core.BrowsePath;
import org.opcfoundation.ua.core.RelativePath;
import org.opcfoundation.ua.core.RelativePathElement;
import org.opcfoundation.ua.core.TranslateBrowsePathsToNodeIdsRequest;

import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.RequestHeader;
import org.opcfoundation.ua.core.ResponseHeader;
import org.opcfoundation.ua.core.TimestampsToReturn;
import org.opcfoundation.ua.core.WriteValue;
import org.opcfoundation.ua.core.ContentFilter;
import org.opcfoundation.ua.core.ContentFilterElement;
import org.opcfoundation.ua.core.DeleteNodesResponse;
import org.opcfoundation.ua.builtintypes.ExtensionObject;
import org.opcfoundation.ua.core.FilterOperator;
import org.opcfoundation.ua.core.AddNodesResponse;
import org.opcfoundation.ua.core.AddNodesItem;
import org.opcfoundation.ua.core.AddReferencesItem;
import org.opcfoundation.ua.core.AddReferencesResponse;
import org.opcfoundation.ua.core.DeleteReferencesResponse;
import org.opcfoundation.ua.core.DeleteReferencesItem;
import org.opcfoundation.ua.core.CallMethodRequest;

import org.opcfoundation.ua.transport.SecureChannel;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

import opcua.clientTranslator.RequestAggregate;
import opcua.clientTranslator.RequestAggregate.UaParamBank;

import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;
import org.opcfoundation.ua.core.CallResponse;
//import sun.security.krb5.internal.crypto.Nonce;


public class UaRequest {
	ServiceType serviceType;
	UaClientConnector ParentClientConnector;
	UaParamBank parambank;
	
	//General Parameters
	NodeId authenticationToken;					//Session authentication token
	UnsignedInteger returnDiagnosticsUInt;		//Return Diagnostics Setting
	UnsignedInteger timeoutHintUInt;			//Default Request TimeoutHint
	//Requester Variables
	RequestHeader latestRequestHeader;			//Latest Received Request Header
	int requestHeaderCounter = 0;
	UnsignedInteger currentRequestHandle;		//Current Request Handle
	//Request-specific parameters
	java.lang.String mAuditEntryId;				//Default Audit Entry ID
	//Default Service Call Parameters
	double maxAge = 0.0;
	//Default TimestampsToReturn
	String requestedTimeStamps;
	
	private String finalResponse;
	
	public UaRequest(UaClientConnector clientConnector, ServiceType serviceType, UaParamBank parambank) throws Throwable{
		ParentClientConnector = clientConnector;
		this.serviceType = serviceType;
		this.parambank = parambank;
		executeRequest();
	}
	
	public RequestHeader composeDefaultRequestHeader(){
		//DateTime
		DateTime timeStamp = new DateTime();
		
		//Append RequestHeaderCounter
		this.requestHeaderCounter++; //TODO: implement random UUID request handle?
		
		//Define a new requestHandle (and update the LatestRequestHeader-variable)
		currentRequestHandle = UnsignedInteger.getFromBits(this.requestHeaderCounter);
				
		latestRequestHeader = new RequestHeader(authenticationToken, timeStamp, currentRequestHandle, this.returnDiagnosticsUInt, this.mAuditEntryId, this.timeoutHintUInt, null);
		return latestRequestHeader;
	}
	
	public String ReadServiceRequest(java.lang.Double pMaxAge, TimestampsToReturn timestampsToReturn, ReadValueId[] nodesToRead) throws ServiceResultException, ServiceFaultException {
		RequestHeader requestHeader = this.composeDefaultRequestHeader();
		ReadResponse readResponse = ParentClientConnector.mySessionChannel.Read(requestHeader, pMaxAge, timestampsToReturn, nodesToRead);
		
		return readResponse.toString();
	}
	
	public String BrowseServiceRequest(ViewDescription view, UnsignedInteger requestedMaxReferencesPerNode, BrowseDescription nodesToBrowse){
		RequestHeader requestHeader = this.composeDefaultRequestHeader();
		try {
			if(requestedMaxReferencesPerNode != null && nodesToBrowse != null){
				BrowseResponse browseResponse = ParentClientConnector.mySessionChannel.Browse(requestHeader, view, requestedMaxReferencesPerNode, nodesToBrowse);
				
				return browseResponse.toString();
			}else{
				return "One or more critical browse parameter(s) is null";
			}
		} catch( ServiceFaultException sfe) {
			System.out.print("\nServiceFaultException\n");
			return null;
		} catch (ServiceResultException sre ){
			System.out.print("\nServiceResultException\n");
			return null;
		}
	}
	
	public String WriteServiceRequest(WriteValue[] NodesToWrite){
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
		try {
			WriteResponse writeResponse = ParentClientConnector.mySessionChannel.Write(pRequestHeader, NodesToWrite);

			return writeResponse.toString();
		} catch( ServiceFaultException sfe) {
			return null;
		} catch (ServiceResultException sre ){
			return null;
		}
	}
	
	public enum ServiceType {
		None, Read, Browse, Write
	}
	
	
	private void executeRequest(){
		try{
			
			//.BrowseServiceRequest(paramsList);
			//.ReadServiceRequest(paramsList);
			//.WriteServiceRequest(paramsList);
			
			switch(this.serviceType){
			
			case Read:
				this.finalResponse = ReadServiceRequest(this.maxAge, TimestampsToReturn.Both, this.parambank.getNodesToRead());
	            break;
	            
			case Write:
				this.finalResponse = WriteServiceRequest(this.parambank.getNodesToWrite());
	            break;
	            
			case Browse:
				this.finalResponse = BrowseServiceRequest(null, UnsignedInteger.ZERO, this.parambank.getNodesToBrowse());
	            break;
	            
	        default: 
	            break;
	    	}
		} catch (ServiceFaultException sfe){
			sfe.printStackTrace();
		} catch (ServiceResultException sre){
			sre.printStackTrace();
		}
	}
	
	public String getResponse(){
		return this.finalResponse;
	}
}
