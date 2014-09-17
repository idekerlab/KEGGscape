package org.cytoscape.keggscape.internal.task;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class UpdateStyleTask extends AbstractTask {

	private final CyNetworkView view;
	private final VisualMappingManager vmm;
	private final VisualStyle originalStyle;

	public UpdateStyleTask(final CyNetworkView view, final VisualMappingManager vmm, final VisualStyle originalStyle) {
		this.originalStyle = originalStyle;
		this.vmm = vmm;
		this.view = view;

	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		final VisualStyle currentStyle = vmm.getVisualStyle(view);
		if (currentStyle.equals(originalStyle) == false) {
			vmm.setVisualStyle(originalStyle, view);
			originalStyle.apply(view);
			view.updateView();
		}
	}

}
