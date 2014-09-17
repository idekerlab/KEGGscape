package org.cytoscape.keggscape.internal.wsclient;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

public class MapExtraDataTaskFactory extends AbstractNetworkTaskFactory {
	
	@Override
	public TaskIterator createTaskIterator(CyNetwork network) {
		return new TaskIterator(new MapExtraDataTask(network));
	}

}
