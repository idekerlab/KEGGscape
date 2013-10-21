package org.cytoscape.keggscape.internal.read.kgml;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.view.model.CyNetworkViewFactory;
//import org.cytoscape.work.TaskIterator;

public abstract class AbstractReaderFactory implements InputStreamTaskFactory {
	
	private final CyFileFilter fileFilter;
	
	protected final CyNetworkFactory cyNetworkFactory;
	protected final CyNetworkViewFactory cyNetworkViewFactory;
	
	public AbstractReaderFactory(final CyFileFilter filter,
			final CyNetworkViewFactory cyNetworkViewFactory, final CyNetworkFactory cyNetworkFactory) {
		this.fileFilter = filter;
		this.cyNetworkFactory = cyNetworkFactory; 
		this.cyNetworkViewFactory = cyNetworkViewFactory;
	}

	@Override
	public CyFileFilter getFileFilter() {
		// TODO Auto-generated method stub
		return this.fileFilter;
	}

//	@Override
//	public TaskIterator createTaskIterator(InputStream arg0, String arg1) {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public boolean isReady(final InputStream is, final String inputName) {
		// TODO Auto-generated method stub
		return true;
	}

}
