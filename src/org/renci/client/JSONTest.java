package org.renci.client;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.renci.coordinator.ResourceCoordinator;

public class JSONTest {

	public static void main(String[] args) {
		JsonObject personObject = Json.createObjectBuilder()
				.add("id", "id123")
				.add("cpus", 1)
				.add("mem", 32)
				.add("container", Json.createObjectBuilder()
						.add("type", "DOCKER") 
						.add("docker", Json.createObjectBuilder()
								.add("image", "mycoy/repository:sleeper01")
								.add("network", "BRIDGE") ).build()
				).build();
		String marathonAddr = "http://129.114.111.199:9090" + "/v2/apps";
		
		ClientConfig config = new ClientConfig();
		Client client = ClientBuilder.newClient(config);
		WebTarget target = client.target( marathonAddr ); 
		Invocation.Builder invocationBuilder = target.request( "application/json" );
		Response response = invocationBuilder.post( Entity.entity( personObject, "application/json" ) );
		response.readEntity(String.class).toString();
	}

}
