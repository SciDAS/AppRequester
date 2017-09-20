package org.renci.resource;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import java.net.*;
import java.util.*;


/**
 * @author wenzhao
 * This class contains the functions to handle the REST invocation to the server's endpoint. 
 */
@Path("/api")
public class ResourceProcessor { 
    @Context
    private UriInfo uriInfo;
    @Context
    private Request request;
    
    /**
     * The default constructor
     */
    public ResourceProcessor(){}
    
    /**
     * This functions handles HTTP-get call. 
     * @return A hello string, to identify the successful setup of the server.
     */
    @GET
    @Produces( { MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
    public String getXML() {
    	return "Hello, how are you.\n";
    }
    
    /**
     * This function processes HTTP-post calls, to send a request to the meta-scheduler. 
     * @param info
     * @return
     * @throws URISyntaxException
     */
	@POST
	@Consumes( {MediaType.APPLICATION_JSON} )
	@Produces( {MediaType.APPLICATION_JSON} )
	public Response process( Object info ) throws URISyntaxException {
		Response res = Response.ok().entity("Processed Call\n").build(); 
		return res;
	}
}
