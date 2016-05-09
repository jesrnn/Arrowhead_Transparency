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

import java.util.List;

import com.eislab.af.core.ArrowheadAccessorRest;

import eu.arrowhead.registry.data.Service;
import eu.arrowhead.registry.data.ServiceList;

public class ArrowheadAccessorRestTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		try {
			ArrowheadAccessorRest accessor = new ArrowheadAccessorRest("http://127.0.0.1:8045");
	//		
			{
				ServiceList services = accessor.getServiceListByType("_printer-s-ws-https._tcp");
				List<Service> serviceList = services.getService();
				for (Service service : serviceList) {
					System.out.println(service.getHost() + service.getName() + service.getType());
				}
			}
			
			{
				Service service = accessor.getServiceByName("anotherprinterservice");
				System.out.println(service.getHost() + service.getName() + service.getType());
			}
			
			{
				accessor.postPublishService("FirstPrinterService", "_printer-s-ws-https._tcp", "192.168.3.100", "/hello/arrowhead" );
			}
			
			{
				Service service = accessor.getServiceByName("FirstPrinterService");
				System.out.println(service.getHost() + service.getName() + service.getType());
			}
			
			{
				accessor.postUnPublishService("FirstPrinterService");
			}
			
			{
				Service service = accessor.getServiceByName("FirstPrinterService");
				System.out.println(service.getHost() + service.getName() + service.getType());
			}
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		
	}

}
