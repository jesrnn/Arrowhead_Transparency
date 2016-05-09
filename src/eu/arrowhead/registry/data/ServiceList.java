/* 
 * Copyright 2014 UniBO (http://www.unibo.it/) 
 * 
 * This code is part of an Arrowhead System reference implementation.
 * You may use it freely within the scope of the Arrowhead project.
 * All other uses are prohibited.
 */
package eu.arrowhead.registry.data;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Stradivarius
 *
 */
@XmlRootElement
public class ServiceList {
	
	private List<Service> service;
	
	public ServiceList () {
		this.service = new ArrayList<Service>();
	}

	public ServiceList (List<Service> service) {
		this.service = service;
	}
	
	public List<Service> getService() {
		return service;
	}

	public void setService(List<Service> service) {
		this.service = service;
	}

//	@Override
//	public String toString() {
//		String output = "";
//		for (String r : service) {
//			output += r + " ";
//		}
//		return output;
//	}
	
}
