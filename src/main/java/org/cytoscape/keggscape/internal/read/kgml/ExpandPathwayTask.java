package org.cytoscape.keggscape.internal.read.kgml;

import java.net.URL;

import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ExpandPathwayTask extends AbstractTask {

	private final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory;
	private final URL url;
	
	public ExpandPathwayTask(final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory, URL url) {
		this.url = url;
		this.loadNetworkURLTaskFactory = loadNetworkURLTaskFactory;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		TaskIterator it = loadNetworkURLTaskFactory.loadCyNetworks(url);
		this.insertTasksAfterCurrentTask(it);
	}

}
