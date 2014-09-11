package org.cytoscape.keggscape.internal.read.kgml;

import java.net.URL;

import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ExpandPathwayTask extends AbstractTask {

	private final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory;
	private final URL url;
	private final String pathwayName;
	private final String pathwayID;
	
	public ExpandPathwayTask(final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory, URL url, String pathwayName, String mapID) {
		this.url = url;
		this.pathwayID = mapID;
		this.pathwayName = pathwayName;
		this.loadNetworkURLTaskFactory = loadNetworkURLTaskFactory;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Expanding Map Node");
		taskMonitor.setStatusMessage("Downloading Pathway (" + pathwayName + " " + pathwayID + ") from KEGG...");
		taskMonitor.setProgress(-1.0);
		
		TaskIterator it = loadNetworkURLTaskFactory.loadCyNetworks(url);
		this.insertTasksAfterCurrentTask(it);
		taskMonitor.setProgress(1.0d);
	}

}
