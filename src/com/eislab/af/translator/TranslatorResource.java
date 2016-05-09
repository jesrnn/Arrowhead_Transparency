package com.eislab.af.translator;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;


@Path("/")
@Singleton
public class TranslatorResource {

	public TranslatorResource() {}
	
	@Path("/translator")
	@GET
    public Response getServices() {
		Response response;
		
		response = Response.ok("<text>hello</text>").build();
		
		return response;
	
	}

}
