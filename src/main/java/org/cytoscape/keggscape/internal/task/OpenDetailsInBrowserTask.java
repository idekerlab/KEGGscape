package org.cytoscape.keggscape.internal.task;

import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class OpenDetailsInBrowserTask extends AbstractTask {

	private final OpenBrowser openBrowser;
	private final String url;

	public OpenDetailsInBrowserTask(OpenBrowser openBrowser, String url) {
		this.openBrowser = openBrowser;
		this.url = url;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		openBrowser.openURL(url);
	}

}
