package org.renci.coordinator;

import java.util.*;

public class ResourceCoordinator {
	private static ResourceCoordinator coordinator = null;
	private Map<String, String> globalFKDockerImageMap = null;
	
	/**
	 * The private constructor.
	 * It will create a monitoring thread.
	 */
	private ResourceCoordinator(){
		globalFKDockerImageMap = new HashMap<String, String>();
	}
	
	/**
	 * The only function to get the single instance of this class. 
	 * @return
	 */
	public static synchronized ResourceCoordinator getInstance(){
		if(coordinator==null)
			coordinator = new ResourceCoordinator();
		return coordinator;
	}

	public Map<String, String> getGlobalFKDockerImageMap() {
		return globalFKDockerImageMap;
	}
	
}
