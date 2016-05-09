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
public class ServiceTypeList {
	
	private List<String> serviceType;
	
	public ServiceTypeList () {
		this.serviceType = new ArrayList<String>();
	}

	public ServiceTypeList (List<String> type) {
		this.serviceType = type;
	}
	
	public List<String> getType() {
		return serviceType;
	}

	public void setType(List<String> type) {
		this.serviceType = type;
	}

	@Override
	public String toString() {
		String output = "";
		for (String r : serviceType) {
			output += r + " ";
		}
		return output;
	}
	
}
