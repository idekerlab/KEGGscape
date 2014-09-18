package org.cytoscape.keggscape.internal.task;

import java.io.IOException;
import java.net.URL;

import org.cytoscape.task.read.LoadNetworkURLTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ExpandPathwayTask extends AbstractTask {

	private final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory;
	private final URL url;
	private final String pathwayName;
	private final String pathwayID;

	private final CyNetworkView parentView;

	private final VisualMappingManager vmm;

	public ExpandPathwayTask(final LoadNetworkURLTaskFactory loadNetworkURLTaskFactory, URL url, String pathwayName,
			String mapID, final CyNetworkView parentView, VisualMappingManager vmm) {
		this.url = url;
		this.pathwayID = mapID;
		this.pathwayName = pathwayName;
		this.loadNetworkURLTaskFactory = loadNetworkURLTaskFactory;
		this.parentView = parentView;
		this.vmm = vmm;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Expanding Map Node");
		taskMonitor.setStatusMessage("Downloading Pathway (" + pathwayName + " " + pathwayID + ") from KEGG...");
		taskMonitor.setProgress(-1.0);

		try {
			final VisualStyle originalStyle = vmm.getVisualStyle(parentView);
			TaskIterator it = loadNetworkURLTaskFactory.loadCyNetworks(url);
			it.append(new UpdateStyleTask(parentView, vmm, originalStyle));
			this.insertTasksAfterCurrentTask(it);
			taskMonitor.setProgress(1.0d);
		} catch (Exception e) {
			if(e instanceof IllegalStateException) {
				throw new IOException("Could not expand pathway:\n" + "KEGG database does not have KGML file for pathway "
					+ pathwayID + ".", e);
			} else {
				throw e;
			}
		}
	}

}
