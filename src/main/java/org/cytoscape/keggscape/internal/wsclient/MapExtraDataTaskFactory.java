package org.cytoscape.keggscape.internal.wsclient;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

public class MapExtraDataTaskFactory extends AbstractNetworkTaskFactory {
	
	private final TogowsClient client;
	

	public MapExtraDataTaskFactory() {
		this.client = new TogowsClient();	
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new MapExtraDataTask(network));
	}

}
