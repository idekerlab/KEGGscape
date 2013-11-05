package org.cytoscape.keggscape.internal.read.kgml;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskIterator;

public class KeggscapeNetworkReaderFactory extends AbstractReaderFactory {
	
	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;
	
	public KeggscapeNetworkReaderFactory(final CyFileFilter filter, final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager,
			final CyRootNetworkManager cyRootNetworkManager) {
		super(filter, cyNetworkViewFactory, cyNetworkFactory);
		
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
	}

	@Override
	public TaskIterator createTaskIterator(InputStream is, String inputName) {
		// TODO Auto-generated method stub
		return new TaskIterator(new KeggscapeNetworkReader(is, cyNetworkViewFactory, cyNetworkFactory,
				cyNetworkManager, cyRootNetworkManager));
	}

}
