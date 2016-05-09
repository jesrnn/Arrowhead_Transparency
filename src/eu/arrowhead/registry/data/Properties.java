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
public class Properties {
	
	private List<Property> property;
	
	public Properties () {
		this.property = new ArrayList<Property>();
	}

	public Properties (List<Property> property) {
		this.property = property;
	}
	
	public List<Property> getProperty() {
		return property;
	}

	public void setProperty(List<Property> property) {
		this.property = property;
	}	
}
