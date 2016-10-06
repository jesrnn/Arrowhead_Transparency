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
import java.util.Arrays;
import java.util.UUID;

import org.opcfoundation.ua.application.Client;
import org.opcfoundation.ua.application.Session;
import org.opcfoundation.ua.application.SessionChannel;
import org.opcfoundation.ua.builtintypes.ServiceResult;
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.core.BrowseDescription;
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
import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;
import org.opcfoundation.ua.core.CallResponse;
//import sun.security.krb5.internal.crypto.Nonce;


/*
 * UaRequester takes HTTP parameters (from query or xml) to compose an
 * opcua service request that can be sent by the internal opcua client
 */
public class UaRequester {
	//General Parameters
	NodeId authenticationToken;							//Session authentication token
	UnsignedInteger returnDiagnosticsUInt;	//Return Diagnostics Setting
	UnsignedInteger timeoutHintUInt;			//Default Request TimeoutHint
	
	//Session Objects
	UaClientConnector ParentClientConnector;
	
	//Request-specific parameters
	java.lang.String mAuditEntryId;				//Default Audit Entry ID
	
	//Requester Variables
	RequestHeader latestRequestHeader;			//Latest Received Request Header
	int requestHeaderCounter = 0;
	UnsignedInteger currentRequestHandle;		//Current Request Handle
	
	//Default Service Call Parameters
	int maxAge = 0;
	
	//Default TimestampsToReturn
	String requestedTimeStamps;
	
	//Service Call Response
	String serviceCallResultString;				//Default Audit Entry ID
	
	public UaRequester(UaClientConnector clientConnector) throws Throwable{
		ParentClientConnector = clientConnector;
		
		authenticationToken = ParentClientConnector.sessionAuthenticationToken;
		
		//Return Diagnostics
		int mInt2 = 0;
		returnDiagnosticsUInt = UnsignedInteger.getFromBits(mInt2);
		
		//TimeoutHint
		int mInt3 = 10000;
		timeoutHintUInt = UnsignedInteger.getFromBits(mInt3);
		
		//Default AuditEntryID
		mAuditEntryId = "ArrowheadUaRequester_RequestHeader_DefaultAuditEntryId";
		
		//Define Default TimestampsToReturn
		requestedTimeStamps = "Both";
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
	
	public RequestHeader composeDefaultRequestHeader(java.lang.String customAuditEntryId){
		//DateTime
		DateTime mTimeStamp1 = new DateTime();
		
		//Append RequestHeaderCounter
		this.requestHeaderCounter++;
		
		//Define a new requestHandle for this header
		currentRequestHandle = UnsignedInteger.getFromBits(this.requestHeaderCounter);
		
		//Define the actual request header (and update the LatestRequestHeader-variable)
		latestRequestHeader = new RequestHeader(authenticationToken, mTimeStamp1, currentRequestHandle, this.returnDiagnosticsUInt, customAuditEntryId, this.timeoutHintUInt, null);
		return latestRequestHeader;
	}
	
	public String ReadServiceRequest(java.lang.Double pMaxAge, TimestampsToReturn pTimestampsToReturn, ReadValueId pNodesToRead) throws ServiceResultException, ServiceFaultException {
		//----------------------------------------------
		//-----------READ NODE SERVICE CALL-------------
		//----------------------------------------------
		
		if(pMaxAge != null && pTimestampsToReturn != null && pNodesToRead != null){
			System.out.print("invalid maxage, timestamps, or nodesToRead OK!\n");
		} else {
			System.out.print("maxage, timestamps or nodesToRead are null!\n");
		}
		
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
		ReadResponse mReadResponse = ParentClientConnector.mySessionChannel.Read(pRequestHeader, pMaxAge, pTimestampsToReturn, pNodesToRead);
		
		return mReadResponse.toString();
	}	
	
	public String WriteServiceRequest(WriteValue NodesToWrite){
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

	public String BrowseServiceRequest(ViewDescription pView, UnsignedInteger pRequestedMaxReferencesPerNode, BrowseDescription pNodesToBrowse){
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
		try {
			if(pRequestedMaxReferencesPerNode != null && pNodesToBrowse != null){
				BrowseResponse mBrowseResponse = ParentClientConnector.mySessionChannel.Browse(pRequestHeader, pView, pRequestedMaxReferencesPerNode, pNodesToBrowse);
				
				return mBrowseResponse.toString();
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
	
	public String TranslateBrowsePathsToNodeIds(NodeId pNodeId, RelativePath pRelativePath){
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
		try {
			BrowsePath pBrowsePath = new BrowsePath();
			BrowsePath[] pBrowsePathArray = new BrowsePath[1];
			TranslateBrowsePathsToNodeIdsResponse mTranslateResponse = ParentClientConnector.mySessionChannel.TranslateBrowsePathsToNodeIds(pRequestHeader, pBrowsePathArray);
			return mTranslateResponse.toString();
		} catch( ServiceFaultException sfe) {
			System.out.print("\nServiceFaultException\n");
			return null;
		} catch (ServiceResultException sre ){
			System.out.print("\nServiceResultException\n");
			return null;
		}
	}
	
	public String QueryServiceRequest(ViewDescription viewDescription, QueryDataDescription[] queryDataDescriptionArray, NodeTypeDescription[] nodeTypesArray){
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
		try {

			//ContentFilterElement
/*			NodeId filterOperandNodeId = new NodeId(0,29);
			ExpandedNodeId enid = new ExpandedNodeId(filterOperandNodeId);
			ExtensionObject pFilterOperands = new ExtensionObject(enid);
			ExtensionObject[] pFilterOperandsArray = new ExtensionObject[1];
			pFilterOperandsArray[0] = pFilterOperands;
			FilterOperator pFilterOperator = FilterOperator.InView;
	*/		
			//ContentFilter
			/*ContentFilterElement pContentFilterElement = new ContentFilterElement(pFilterOperator, pFilterOperandsArray); 
			ContentFilter pContentFilter = new ContentFilter();*/
			
			//UInt MaxDataSetsToReturn
			int mdsr = 127;
			UnsignedInteger umdsr = new UnsignedInteger(mdsr);
			
			//MaxReferencesToReturn
			int mrtr = 1023;
			UnsignedInteger umrtr = new UnsignedInteger(mrtr);
			
			//QueryFirst Request
			QueryFirstResponse mQueryResponse = ParentClientConnector.mySessionChannel.QueryFirst(pRequestHeader, viewDescription, nodeTypesArray, null, umdsr, umrtr);
			return mQueryResponse.toString();

		} catch( ServiceFaultException sfe) {
			System.out.print("\nServiceFaultException\n");
			sfe.printStackTrace();
			return null;
		} catch (ServiceResultException sre ){
			System.out.print("\nServiceResultException\n");
			sre.printStackTrace();
			return null;
		}
	}
	
	public String AddNodesRequest(AddNodesItem aniArray){ //TODO: add array brackets
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
	
		try {
			
			//TODO: implement array instead of just an element for aniArray
			
			AddNodesResponse mAddNodesResponse = ParentClientConnector.mySessionChannel.AddNodes(pRequestHeader, aniArray);
			
		return mAddNodesResponse.toString();
		} catch( ServiceFaultException sfe) {
			System.out.print("\nServiceFaultException\n");
			sfe.printStackTrace();
			return null;
		} catch (ServiceResultException sre ){
			System.out.print("\nServiceResultException\n");
			sre.printStackTrace();
			return null;
		}
	}
	
	public String DeleteNodesRequest(NodeId pNodeId, Boolean pDeleteTargetReferences){
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
	
		try {
			
			DeleteNodesItem dni = new DeleteNodesItem(pNodeId, pDeleteTargetReferences);
			DeleteNodesItem[] dniArray = new DeleteNodesItem[1];
			dniArray[0] = dni;
			
			DeleteNodesResponse mDeleteNodesResponse = ParentClientConnector.mySessionChannel.DeleteNodes(pRequestHeader, dniArray);
			
		return mDeleteNodesResponse.toString();
		} catch( ServiceFaultException sfe) {
			System.out.print("\nServiceFaultException on DeleteNodes Request\n");
			sfe.printStackTrace();
			return null;
		} catch (ServiceResultException sre ){
			System.out.print("\nServiceResultException\n");
			return null;
		}
	}
	
	public String AddReferencesRequest(NodeId pSourceNodeId, NodeId pReferenceTypeId, Boolean pIsForward, String pTargetServerUri, ExpandedNodeId pTargetNodeId, NodeClass pTargetNodeClass){
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
		try {
			
			 AddReferencesItem ari = new AddReferencesItem(pSourceNodeId, pReferenceTypeId, pIsForward, pTargetServerUri, pTargetNodeId, pTargetNodeClass);
			 AddReferencesItem[] ariArray = new AddReferencesItem[1];
			 ariArray[0] = ari;
			 
			AddReferencesResponse mAddReferencesResponse =  ParentClientConnector.mySessionChannel.AddReferences(pRequestHeader, ariArray);
			
			
			return mAddReferencesResponse.toString();
		} catch( ServiceFaultException sfe) {
			System.out.print("\nServiceFaultException on DeleteNodes Request\n");
			sfe.printStackTrace();
			return null;
		} catch (ServiceResultException sre ){
			System.out.print("\nServiceResultException\n");
			return null;
		}
	}
	
	public String DeleteReferencesRequest(NodeId pSourceNodeId, NodeId pReferenceTypeId, java.lang.Boolean pIsForward, ExpandedNodeId pTargetNodeId, java.lang.Boolean pDeleteBidirectional){
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
		try {

			 DeleteReferencesItem dri = new DeleteReferencesItem(pSourceNodeId, pReferenceTypeId, pIsForward, pTargetNodeId, pDeleteBidirectional);
			 DeleteReferencesItem[] driArray = new DeleteReferencesItem[1];
			 driArray[0] = dri;
			 
			 DeleteReferencesResponse mDeleteReferencesResponse = ParentClientConnector.mySessionChannel.DeleteReferences(pRequestHeader, driArray);
			
			return mDeleteReferencesResponse.toString();
		} catch( ServiceFaultException sfe) {
			System.out.print("\nServiceFaultException on DeleteNodes Request\n");
			sfe.printStackTrace();
			return null;
		} catch (ServiceResultException sre ){
			System.out.print("\nServiceResultException\n");
			return null;
		}
	}	
	
	public String CallMethodRequest(CallMethodRequest pMethodsToCall){
		RequestHeader pRequestHeader = this.composeDefaultRequestHeader();
		try {
			CallResponse mCallMethodResponse = ParentClientConnector.mySessionChannel.Call(pRequestHeader, pMethodsToCall);
			return mCallMethodResponse.toString();
		} catch( ServiceFaultException sfe) {
			System.out.print("\nServiceFaultException on DeleteNodes Request\n");
			sfe.printStackTrace();
			return null;
		} catch (ServiceResultException sre ){
			System.out.print("\nServiceResultException\n");
			return null;
		}
	}
}
