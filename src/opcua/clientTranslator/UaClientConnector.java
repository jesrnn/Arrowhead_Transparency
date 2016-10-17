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

import com.eislab.af.translator.data.BaseContext;
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
import org.opcfoundation.ua.builtintypes.DateTime;
import org.opcfoundation.ua.builtintypes.NodeId;
import org.opcfoundation.ua.builtintypes.QualifiedName;
import org.opcfoundation.ua.builtintypes.UnsignedInteger;
import org.opcfoundation.ua.common.ServiceResultException;
import org.opcfoundation.ua.core.ActivateSessionResponse;
import org.opcfoundation.ua.core.CloseSessionRequest;
import org.opcfoundation.ua.core.CloseSessionResponse;
import org.opcfoundation.ua.core.EndpointDescription;
import org.opcfoundation.ua.core.MessageSecurityMode;
import org.opcfoundation.ua.core.Node;
import org.opcfoundation.ua.core.ReadResponse;
import org.opcfoundation.ua.core.ReadValueId;
import org.opcfoundation.ua.core.RequestHeader;
import org.opcfoundation.ua.core.ResponseHeader;
import org.opcfoundation.ua.core.TimestampsToReturn;

import org.opcfoundation.ua.transport.SecureChannel;
import org.opcfoundation.ua.transport.security.SecurityPolicy;

import opcua.clientTranslator.*;
import opcua.clientTranslator.RequestAggregate.Method;

import org.opcfoundation.ua.transport.security.Cert;
import org.opcfoundation.ua.transport.security.KeyPair;
import org.opcfoundation.ua.transport.security.PrivKey;
//import sun.security.krb5.internal.crypto.Nonce;

public class UaClientConnector {
	//Keys & Certificates
	public Cert ClientCertificate;
	public PrivKey ClientPrivateKey;
	public KeyPair ClientApplicationInstanceCertificate;
	public Cert ServerCertificate;
	public PrivKey ServerPrivateKey;
	public KeyPair ServerApplicationInstanceCertificate;
	
	//Client, Channel & Session variables
	public Client myClient = null;
	public Session mySession = null;
	public SessionChannel mySessionChannel = null;
	private SecureChannel channel = null;
	NodeId sessionAuthenticationToken;
	
	//Request/Response
	public RequestHeader LatestRequestHeader = null;
	
	// INTERNAL, STATIC PARAMETERS
	public UnsignedInteger maxResponseMessageSize = null;
	public double requestedSessionTimeout;
	
	ArrayList<RequestAggregate> requests = null;
	
	public UaClientConnector(String serverUrl, String spokeQuery) throws Throwable,UnrecoverableKeyException,		//TODO: determine if/how the spokeQuery-string will be put to use
	CertificateException, NoSuchAlgorithmException, KeyStoreException,
	IOException {
		if (serverUrl != null)
		    try {
		    	//set max response message size
				maxResponseMessageSize = UnsignedInteger.getFromBits(1);
				
				//session timeout for this client
				requestedSessionTimeout = 100000;
		    	
				this.connectSecureChannel(serverUrl);
				
				//Create Session Channel
				this.mySession = this.myClient.createSession(this.channel, maxResponseMessageSize, requestedSessionTimeout, "mSession");
				
				//Create Session Channel
				this.mySessionChannel = this.mySession.createSessionChannel(this.channel, this.myClient);
				
				//Activate session channel and store call response in mActivateSessionResponse
				ActivateSessionResponse activateSessionResponse = this.mySessionChannel.activate();
				
				//Authentication Token for session
				sessionAuthenticationToken = this.mySession.getAuthenticationToken();
				
				//Return Diagnostics
				int mInt2 = 0;
				UnsignedInteger mUInt_returnDiagnostics = UnsignedInteger.getFromBits(mInt2);
				
				//AuditEntryId
				java.lang.String mAuditEntryId = "default-RequestHeader-AuditEntryID";

				//TimeoutHint
				int mInt3 = 10000;
				UnsignedInteger mUInt_timeoutHint = UnsignedInteger.getFromBits(mInt3);
				
				requests = new ArrayList<RequestAggregate>();
				
		    } finally {
			    
			    }
	}
	

	public void sendServiceRequest() throws Throwable,UnrecoverableKeyException,
	CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException {
		if (isConnectedSession())
			try{
				
			} finally {
				
			}
	}
	
	public void connectSecureChannel(String url) throws ServiceResultException {
		// Create Client
		myClient = Client.createClientApplication( this.ClientApplicationInstanceCertificate );
		// Get endpoint for desired security level
		EndpointDescription[] endpoints = myClient.discoverEndpoints( url );
		System.out.println("All endpoints: " + endpoints.length);
		// Filter out all but opc.tcp protocol endpoints
		endpoints = selectByProtocol(endpoints, "opc.tcp");
		System.out.println("opc.tcp endpoints: " + endpoints.length);
		// Filter out all but Signed & Encrypted endpoints
		endpoints = selectByMessageSecurityMode(endpoints, MessageSecurityMode.None);
		System.out.println("MessageSecurityMode endpoints: " + endpoints.length);
		endpoints = selectBySecurityPolicy(endpoints, SecurityPolicy.NONE);
		System.out.println("SecurityPolicy endpoints: " + endpoints.length);
		// Sort endpoints by security level. The lowest level at the beginning, the highest at the end of the array
		endpoints = sortBySecurityLevel(endpoints); 
		// Choose one endpoint (the most secure one)	
		EndpointDescription endpoint = endpoints[endpoints.length-1];
		// Create channel
		channel = myClient.createSecureChannel(endpoint);	
		//logger.open(0 /* TODO: secureChannelId */, configuration.getTestFilePath(), configuration.getRandomFilePath());
	}
	
	public void closeSession() throws ServiceResultException {
		//Delete subscriptions boolean ()
		java.lang.Boolean mBoolean = true;
		
		CloseSessionRequest mCloseSessionReq = new CloseSessionRequest(LatestRequestHeader, mBoolean);
		CloseSessionResponse mCloseSessionResponse = this.mySessionChannel.CloseSession(mCloseSessionReq);
				
		//Extract response header from response object
		ResponseHeader mCloseResponseHeader = mCloseSessionResponse.getResponseHeader();
	}
	
	public void closeClient() throws Throwable, ServiceResultException {
	
		if (isConnectedSession())
		    try {
		    	this.closeSession();
		    } finally {
		    	this.disconnectSecureChannel();
		    }
	}
	
	public String handleRequest(String path, String query, String content, String methodString){	
		Method method = Method.valueOf(methodString);
		RequestAggregate currentRequest = new RequestAggregate(this, method, path, query, content);
		this.requests.add(currentRequest);
		
		return currentRequest.getResponse();
	}
	
	/**
	 * @throws ServiceResultException
	 */
	private void disconnectSecureChannel() throws ServiceResultException {
		if (channel!=null) {
			channel.closeAsync();
			channel = null;
		}
	}

	private boolean isConnectedSession() {
		return (channel != null) && (mySession != null) && (channel.isOpen()) && (mySessionChannel != null);
	}
}