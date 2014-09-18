package org.cytoscape.keggscape.internal.task;

import org.cytoscape.keggscape.internal.read.kgml.KeggConstants;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class OpenDetailsInBrowserTaskFactory extends AbstractNodeViewTaskFactory {

	private final OpenBrowser openBrowser;

	public OpenDetailsInBrowserTaskFactory(final OpenBrowser openBrowser) {
		super();
		this.openBrowser = openBrowser;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView netView) {
		// Create query
		final String link = netView.getModel().getRow(nodeView.getModel()).get(KeggConstants.KEGG_LINK, String.class);
		if (link == null) {
			throw new NullPointerException("Could not get KEGG link URL.");
		}

		return new TaskIterator(new OpenDetailsInBrowserTask(openBrowser, link));
	}
}
