package org.cytoscape.keggscape.internal.read.kgml;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.keggscape.internal.KGMLVisualStyleBuilder;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;

public class KeggscapeNetworkReaderFactory extends AbstractReaderFactory {
	
	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	private final KGMLVisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;
	
	public KeggscapeNetworkReaderFactory(final CyFileFilter filter, final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager, KGMLVisualStyleBuilder vsBuilder,
			VisualMappingManager vmm) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
		
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.vsBuilder = vsBuilder;
		this.vmm = vmm;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream is, String inputName) {
		// TODO Auto-generated method stub
		return new TaskIterator(new KeggscapeNetworkReader(is, cyNetworkViewFactory, cyNetworkFactory,
				cyNetworkManager, cyRootNetworkManager, vsBuilder, vmm));
	}

}
