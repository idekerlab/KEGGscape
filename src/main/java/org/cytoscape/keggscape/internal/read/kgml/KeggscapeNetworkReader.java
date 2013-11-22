package org.cytoscape.keggscape.internal.read.kgml;

import java.io.InputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.keggscape.internal.generated.Pathway;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.TaskMonitor;

public class KeggscapeNetworkReader extends AbstractCyNetworkReader {
	
	private static final String PACKAGE_NAME = "org.cytoscape.keggscape.internal.generated";
	private Pathway pathway;
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
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = cyNetworkViewFactory.createNetworkView(network);

		// TODO Apply (X,Y) to the nodes
		
		final Map<CyNode, Double[]> positionMap = mapper.getNodePosition();		
		
		for (CyNode node : positionMap.keySet()) {
			final Double[] position = positionMap.get(node);
			view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, position[0]);
			view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, position[1]);
//			view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_WIDTH, position[2]);
			view.getNodeView(node).setLockedValue(BasicVisualLexicon.NODE_WIDTH, position[2]);
//			view.getNodeView(node).setVisualProperty(BasicVisualLexicon.NODE_HEIGHT, position[3]);
			view.getNodeView(node).setLockedValue(BasicVisualLexicon.NODE_HEIGHT, position[3]);
		}

		return view;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		pathway = null;
        final CyNetwork network = cyNetworkFactory.createNetwork();	
		
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
		
		this.networks = new CyNetwork[1];
		this.networks[0] = network;
		
		// TODO Auto-generated method stub
		mapper = new KGMLMapper(pathway, network);		
		mapper.doMapping();
	
	}

}
