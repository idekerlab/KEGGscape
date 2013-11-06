package org.cytoscape.keggscape.internal.read.kgml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.data.reader.kgml.generated.*;

public class KeggscapeNetworkReader extends AbstractCyNetworkReader {
	
	private static final String PACKAGE_NAME = "org.cytoscape.data.reader.kgml.generated";
	private Pathway pathway;
	private CyNetwork network = null;
	private final InputStream is;
	private KGMLMapper mapper;
	
	public KeggscapeNetworkReader(InputStream is, CyNetworkViewFactory cyNetworkViewFactory,
			CyNetworkFactory cyNetworkFactory, CyNetworkManager cyNetworkManager,
			CyRootNetworkManager cyRootNetworkManager) {
		super(is, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);
		
		if (is == null) {
			throw new NullPointerException("Input Stream cannot be null.");
		}
		
		this.is = is;
	}
	
	@Override
	public CyNetwork[] getNetworks() {
		
		CyNetwork[] result = new CyNetwork[1];
		result[0] = network;
		return result;
	}
	
	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = cyNetworkViewFactory.createNetworkView(network);

		// TODO Apply (X,Y) to the nodes
		
		return view;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		pathway = null;
		
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(PACKAGE_NAME, this.getClass().getClassLoader());
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			pathway = (Pathway) unmarshaller.unmarshal(is);
		} catch (Exception e) {
			e.printStackTrace();
			//throw new IOException("Could not unmarshall KGML file");
		} finally {
			if (is != null) {
				is.close();
			}
		}
		
		// TODO Auto-generated method stub
		mapper = new KGMLMapper(pathway);

	}

}
