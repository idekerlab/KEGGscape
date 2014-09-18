package org.cytoscape.keggscape.internal.read.kgml;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;

public abstract class AbstractReaderFactory extends AbstractInputStreamTaskFactory {

	protected final CyNetworkFactory cyNetworkFactory;
	protected final CyNetworkViewFactory cyNetworkViewFactory;

	public AbstractReaderFactory(final CyFileFilter filter, final CyNetworkViewFactory cyNetworkViewFactory,
			final CyNetworkFactory cyNetworkFactory) {
		super(filter);
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkViewFactory = cyNetworkViewFactory;
	}
}
