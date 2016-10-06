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

import java.util.ArrayList;

import opcua.clientTranslator.UaParameterParser;
import opcua.clientTranslator.UaRequester;

/*
 * UaParser parses query-string into parameters for-, and returns output from UaRequester
 */

public class UaServiceParser {
	//Translator Modules
	UaRequester uaRequester;
	UaClientConnector uaClientConnector;
	
	UaParameterParser uaServiceCallParser;
	
	//Service Call Parameters
	ArrayList<String[]> params = new ArrayList<String[]>();
	
	public UaServiceParser(UaRequester ParentUaRequester, UaClientConnector ParentClientConnector) throws Throwable{
		this.uaRequester = ParentUaRequester;
		this.uaClientConnector = ParentClientConnector;
		this.uaServiceCallParser = new UaParameterParser(this, ParentUaRequester);
	}
	
	public String parseQuery(String query){
		String resultString;
		
		if (query == null) {
			resultString = "NULL QUERY STRING";
        } else {      	
        	//Define query parameter array
        	String[] params = query.split("\\;");
        	
        	//Define parameter arrayList (to be passed on)
    		ArrayList<String[]> paramsList = new ArrayList<String[]>();
    		for(int i=0; i<params.length ; i++){
    			String[] paramPair = new String[2];
    			paramPair = params[i].split("\\=");
    			
    			if(paramPair.length == 2){
    				paramsList.add(paramPair);
    			} else {
    				System.out.print("Invalid Parameter Pair: " + params[i] + "\n");
    			}
    		}
        	
        	String requestTypeParam = params[0];		//The first parameter pair must always be the definition of the type of service call. TODO: make more flexible
        	switch (requestTypeParam) {
            case "uarequest=read":
            	resultString = this.uaServiceCallParser.ReadServiceRequest(paramsList);
                break;
                
            case "uarequest=write":
            	resultString = this.uaServiceCallParser.WriteServiceRequest(paramsList);
                break;
                
            case "uarequest=browse":
            	resultString = this.uaServiceCallParser.BrowseServiceRequest(paramsList);
                break;
                
            case "uarequest=translatebrowsepathstonodeids":
            	resultString = this.uaServiceCallParser.TranslateBrowsePathsToNodeIds(paramsList);
                break;
                
            case "uarequest=callmethod":
            	resultString = this.uaServiceCallParser.CallMethodServiceRequest(paramsList);
                break;
                
            case "uarequest=queryfirst":
            	resultString = this.uaServiceCallParser.QueryServiceRequest(paramsList);
                break;
                
            case "uarequest=addnodes":
            	resultString = this.uaServiceCallParser.AddNodesServiceRequest(paramsList);
                break;
                
            case "uarequest=deletenodes":
            	resultString = this.uaServiceCallParser.DeleteNodesServiceRequest(paramsList);
                break;
                
            case "uarequest=addreferences":
            	resultString = this.uaServiceCallParser.AddReferencesServiceRequest(paramsList);
                break;
                
            case "uarequest=deletereferences":
            	resultString = this.uaServiceCallParser.AddReferencesServiceRequest(paramsList);
                break;
                
            default: 
            	resultString = "INVALID QUERY STRING";
                break;
        	}
        }
		return resultString;
	}
}
