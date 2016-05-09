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
public class Service {
	
	private String name;
	private String type;
	private String domain;
	private String host;
	private int port;
	private Properties properties;
	
	public Service () {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}


	
}
