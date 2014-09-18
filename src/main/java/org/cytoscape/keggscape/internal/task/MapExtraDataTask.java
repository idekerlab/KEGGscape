package org.cytoscape.keggscape.internal.task;

import org.cytoscape.keggscape.internal.read.kgml.KeggConstants;
import org.cytoscape.keggscape.internal.wsclient.TogowsClient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkTask;
import org.cytoscape.work.TaskMonitor;

public class MapExtraDataTask extends AbstractNetworkTask {

	private final TogowsClient client;
	
	public MapExtraDataTask(CyNetwork network) {
		super(network);
		this.client = new TogowsClient();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final String id = network.getRow(network).get(KeggConstants.KEGG_PATHWAY_ID, String.class);
		
		final String pathID = id.split(":")[1];
		client.map(pathID, network);
	}

}
