/* 
 * Copyright 2014 UniBO (http://www.unibo.it/) 
 * 
 * This code is part of an Arrowhead System reference implementation.
 * You may use it freely within the scope of the Arrowhead project.
 * All other uses are prohibited.
 */
package eu.arrowhead.registry.data;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Stradivarius
 *
 */
@XmlRootElement
public class Property {
	
	private String name;
	private String value;
	
	public Property () {
	}
	
	public Property (String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
