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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.eislab.af.translator.data.TranslatorSetup;


@Path("/")
@Singleton
public class TranslatorService {

	Map<Integer, Translator_hub> hubs = new HashMap<Integer, Translator_hub>();
	
	public TranslatorService() { }	
	
	@Path("/translator")
	@GET
	@Produces(MediaType.APPLICATION_XML)
    public Response getTranslator() {
		Response response;
		
//		Translator_hub hub = new Translator_hub();
//		
//		
//		
//		hubs.put(1234,hub);
//		TranslatorSetup setup = new TranslatorSetup();
//		setup.setConsumerName("testconsumer");
//		setup.setProviderName("providerName");
//		response = Response.ok(setup).build();
		
		Map<Integer,String> hubResponse = new HashMap<Integer,String>();

		for (Entry<Integer, Translator_hub> entry : hubs.entrySet()) {
			hubResponse.put(entry.getKey(), entry.getValue().getPSpokeAddress());
		}
		
		response = Response.ok(hubResponse).build();
		
		return response;
	
	}
	
	@Path("/translator")
	@POST
	@Consumes(MediaType.APPLICATION_XML)
    public Response postTranslator(TranslatorSetup setup) {
		Response response;
		
		Translator_hub hub = new Translator_hub();
		System.out.println("postTranslator received: " + setup.getProviderName() + " ProviderSpoke: " + setup.getConsumerName());
		//cSpoke is the spoke connected to the service provider endpoint
		String cSpoke_ProviderName = 	setup.getProviderName().substring(0).toLowerCase();
		String cSpoke_ProviderType = 	setup.getProviderType().substring(0).toLowerCase();
		String cSpoke_ProviderAddress = setup.getProviderAddress().substring(0).toLowerCase();
		
		//pSpoke is the spoke connected to the service consumer endpoint
		String pSpoke_ConsumerName = 	setup.getConsumerName().substring(0).toLowerCase();
		String pSpoke_ConsumerType = 	setup.getConsumerType().substring(0).toLowerCase();
		String pSpoke_ConsumerAddress = setup.getConsumerAddress().substring(0).toLowerCase();
		
//		String serviceEndpoint = setup.getProviderAddress().substring(0);
//		String providerType = setup.getConsumerName().substring(0).toLowerCase();
//		String providerEndpoint = setup.getConsumerAddress().substring(0);
		
		//hub.setProviderSpokeType(pSpoke_ConsumerName);
		//hub.setConsumerSpokeType(cSpoke_ProviderName);
		hub.setPSpoke_ConsumerName(pSpoke_ConsumerName);
		hub.setCSpoke_ProviderName(cSpoke_ProviderName);
		
//		hub.setPSpokePath("/*");//hub.setTranslationPath("*");
		hub.setCSpoke_ProviderAddress(cSpoke_ProviderAddress);//hub.setServiceEndpoint("coap://127.0.0.1:5692/");
		
		int id = hub.getId();
		
		hub.online();
		
		hubs.put(id, hub);
		
		//response = Response.ok("<translatorId>newtranslator=" + id + "</translatorId><translatorAddress>"+ hub.getPSpokeAddress() +"</translatorAddress>").build();
		response = Response.ok("<translationendpoint><id>" + id + "</id><ip>"+ hub.getPSpokeIp() +"</ip><port>"+ hub.getPSpokePort() +"</port></translationendpoint>").build();
		
		return response;
	
	}
	
	
	@Path("/translator/{translatorid}")
	@GET
    public Response getTranslator( @PathParam("translatorid") int translatorid) {
		Response response;
		Translator_hub hub = null;
		
		hub = hubs.get(translatorid);
		
		if(hub != null) {	
			response = Response.ok("<translatorId>" + translatorid + "</translatorId><translatorAddress>"+ hub.getPSpokeAddress() +"</translatorAddress>").build();
		} else {
			response = Response.ok("<translatorId>" + translatorid + "</translatorId><error>" + "Error hub does not exist" + "</error>").build();
		}
		
		return response;
	
	}
	
}
