package org.renci.resource;

import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;

import org.glassfish.jersey.client.ClientConfig;

import java.net.*;
import java.util.*;
import java.math.BigDecimal;

import javax.json.*;

import org.renci.coordinator.*;

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
    @Produces( { MediaType.TEXT_PLAIN })
    public String CheckStatus() {
    	return "Hello, how are you. The server is running right now. \n";
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
		//receive / parse a request
		Map<String,Object> map = (Map<String, Object>)info;
		String msgType = (String)map.get("type");
		
		if(  msgType!=null && msgType.equals("Offers") ){
			processOffersInfo(map);
			return Response.ok().entity("OK \n").build(); 
		}

		return processAppSubmission(map);
	}
	
	private void processOffersInfo(Map<String, Object> map){
		int count = map.size()-2; //skip the "type", "globalFrameworkId" fields
		if(count==0)
			return;
		
		String globalFKId = (String)map.get("globalFrameworkId");
		System.out.format( "\n========== Requester receives an offer collection for globalFrameworkId: %s \n", 
				globalFKId );
		for(int i=0; i<count; i++){
			Map<String, Object> _map = (Map<String, Object>)map.getOrDefault( String.valueOf(i), null ); 
			if(_map==null)
				break;
			System.out.format( "	%d.", i );
			for(String k : _map.keySet())
				System.out.format( "	%s: %s ", k, _map.get(k) ); 
			System.out.println("\n");
		}
		
		//System.out.println("Requester finished printing offers");
		Map<String, Object> offer2Exec = (Map<String, Object>)map.get( "0" ); 
		//System.out.println("offer2Exec: " + offer2Exec);
		JsonObject jsonObject = null;
		try{
		jsonObject = Json.createObjectBuilder()
				.add("id", map.get("globalFrameworkId")+"_"+System.currentTimeMillis())
				.add("cpus", (java.math.BigDecimal)offer2Exec.get("cpus"))
				.add("mem", (java.math.BigDecimal)offer2Exec.get("mem"))
				.add("container", Json.createObjectBuilder()
						.add("type", "DOCKER") 
						.add( "docker", Json.createObjectBuilder()
								.add("image", ResourceCoordinator.getInstance().getGlobalFKDockerImageMap().getOrDefault(globalFKId, "") )
								.add("network", "BRIDGE") ).build()
				).build();
		}catch(Exception e){
			System.out.println(e);
		}
		String marathonAddr = "http://" + offer2Exec.get("Marathon") + "/v2/apps";
		
		System.out.format( "\n========== Requester to send request %s  to Marathon %s, for framework %s \n", 
				jsonObject.toString(), marathonAddr, globalFKId );
		
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		WebTarget target = client.target( marathonAddr ); 
		Invocation.Builder invocationBuilder = target.request( "application/json" );
		Response response = invocationBuilder.post( Entity.entity( jsonObject, "application/json" ) );
		response.readEntity(String.class).toString();
		
	}
	
	private Response processAppSubmission(Map<String, Object> map){
		String requesterAddress=(String)map.get("requesterAddress");
		String coordinatorAddress=(String)map.get("coordinatorAddress");
		String name=(String)map.get("name");
		String resources=(String)map.get("resources");
		String dockerImage=(String)map.get("dockerImage"); 
		String globalFrameworkId=(String)map.get("globalFrameworkId");
		
		if(requesterAddress==null || coordinatorAddress==null || name==null || 
				resources==null || dockerImage==null || globalFrameworkId==null){
			
			return Response.serverError().entity("Invalid Input\n").build(); 
		}
		
		System.out.println("\n========== Requester receives a request: ");
		System.out.println("	requesterAddress: "+requesterAddress); 
		System.out.println("	coordinatorAddress: "+coordinatorAddress);
		System.out.println("	name: "+name);
		System.out.println("	resources: "+resources);
		System.out.println("	dockerImage: "+dockerImage);
		System.out.println("	globalFrameworkId: "+globalFrameworkId);
		ResourceCoordinator.getInstance().getGlobalFKDockerImageMap().put(globalFrameworkId, dockerImage);
		
		//forward the call to the specified MesosCoordinator
        ClientConfig config = new ClientConfig();
        Client client = ClientBuilder.newClient(config);
        WebTarget target = client.target( coordinatorAddress ); 
        Invocation.Builder invocationBuilder = target.request( "application/json" );
        Response response = invocationBuilder.post( Entity.entity( map, "application/json" ) );
        System.out.format("\n========== Requester has submitted framework ( globalFrameworkId: %s) to the MesosCoordinator (%s), status: %s \n", 
        		globalFrameworkId, coordinatorAddress, response.readEntity(String.class).toString() ).println();

		Response res = Response.ok().entity("OK\n").build(); 
		return res;
	}
}
