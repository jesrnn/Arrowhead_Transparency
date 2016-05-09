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

package com.eislab.af.core;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import eu.arrowhead.registry.data.Properties;
import eu.arrowhead.registry.data.Property;
import eu.arrowhead.registry.data.Service;
import eu.arrowhead.registry.data.ServiceList;

public class ArrowheadAccessorRest implements Runnable {
	//implemented as runnable incase in the future it is upgraded with Orchestration capability

	private String hostAddress = "";
	private final String serviceTypePath = "/servicediscovery/type/";
	private final String serviceNamePath = "/servicediscovery/service/";
	private final String servicePublishPath = "/servicediscovery/publish/";
	private final String serviceUnPublishPath = "/servicediscovery/unpublish/";

	public ArrowheadAccessorRest(String hostAddress) throws Exception {
		super();
		if(!hostAddress.isEmpty()) {
			this.hostAddress = hostAddress;
		} else {
			throw new Exception("hostAddress cannot be null or empty");
		}
		
	}
	
	public Service getServiceByName (String serviceName) throws Exception {

		if (serviceName.isEmpty()) {
			throw new Exception("serviceName cannot be null or empty");
		}

		String url = this.hostAddress + this.serviceNamePath + serviceName;
		
		return  ClientBuilder.newClient().target(url).request("application/xml").get().readEntity(Service.class);
	}
	
	public ServiceList getServiceListByType (String serviceType) throws Exception {

		if (serviceType.isEmpty()) {
			throw new Exception("serviceType cannot be null or empty");
		}

		String url = this.hostAddress + this.serviceTypePath + serviceType;
		
		return  ClientBuilder.newClient().target(url).request("application/xml").get().readEntity(ServiceList.class);
	}
	
	public void postPublishService (String name, String type, String host, String path) throws Exception {

		if (name.isEmpty() || type.isEmpty()) {
			throw new Exception("serviceType cannot be null or empty");
		}
		
		Property property = new Property("path", path);			
		List<Property> propertyList = new ArrayList<Property>();
		propertyList.add(property);
		
		Service newService = new Service();
		newService.setHost(host);
		newService.setName(name);
		newService.setType(type);			
		newService.setProperties(new Properties(propertyList));
		
		String url = this.hostAddress + this.servicePublishPath;
		
		Invocation request = ClientBuilder.newClient().target(url).request("application/xml").buildPost(Entity.xml(newService));
		request.submit();
	}
	
	public void postUnPublishService(String name) throws Exception {
		if (name.isEmpty()) {
			throw new Exception("name cannot be null or empty");
		}
		Service newService = new Service();
		newService.setName(name);
						
		String url = this.hostAddress + this.serviceUnPublishPath;
		System.out.println("url:" + url);
		
		Invocation request = ClientBuilder.newClient().target(url).request("application/xml").buildPost(Entity.xml(newService));
		request.submit();
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
