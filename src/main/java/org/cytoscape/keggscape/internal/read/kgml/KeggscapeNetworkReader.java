package org.cytoscape.keggscape.internal.read.kgml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.keggscape.internal.KGMLVisualStyleBuilder;
import org.cytoscape.keggscape.internal.generated.Pathway;
import org.cytoscape.keggscape.internal.wsclient.MapExtraDataTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeggscapeNetworkReader extends AbstractCyNetworkReader {

	private static final Logger logger = LoggerFactory.getLogger(KeggscapeNetworkReader.class);

	private static final String PACKAGE_NAME = "org.cytoscape.keggscape.internal.generated";

	private Pathway pathway;
	private KGMLMapper mapper;

	private final InputStream is;
	private final String collectionName;

	private final VisualMappingManager vmm;
	private final KGMLVisualStyleBuilder vsBuilder;
	private final CyGroupFactory groupFactory;

	@Tunable(description = "Import pathway details from KEGG Database")
	public boolean importFull = false;

	@ProvidesTitle
	public String getTitle() {
		return "Import KEGG Pathway";
	}

	public KeggscapeNetworkReader(final String collectionName, InputStream is,
			CyNetworkViewFactory cyNetworkViewFactory, CyNetworkFactory cyNetworkFactory,
			CyNetworkManager cyNetworkManager, CyRootNetworkManager cyRootNetworkManager,
			final KGMLVisualStyleBuilder vsBuilder, final VisualMappingManager vmm, final CyGroupFactory groupFactory) {
		super(is, cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager, cyRootNetworkManager);

		if (is == null) {
			throw new NullPointerException("Input Stream cannot be null.");
		}

		this.is = is;
		this.collectionName = collectionName;
		this.vmm = vmm;
		this.vsBuilder = vsBuilder;
		this.groupFactory = groupFactory;
	}

	@Override
	public CyNetworkView buildCyNetworkView(CyNetwork network) {
		final CyNetworkView view = cyNetworkViewFactory.createNetworkView(network);

		// TODO Apply (X,Y) to the nodes
		return view;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if (taskMonitor != null) {
			taskMonitor.setTitle("Loading KEGG Pathway");
			taskMonitor.setStatusMessage("Loading KEGG Pathway file in KGML format...");
			taskMonitor.setProgress(-1.0);
		}
		pathway = null;

		if (collectionName != null) {
			ListSingleSelection<String> rootList = getRootNetworkList();
			if (rootList.getPossibleValues().contains(collectionName)) {
				// Collection already exists.
				rootList.setSelectedValue(collectionName);
			}
		}

		CyRootNetwork rootNetwork = getRootNetwork();
		final CyNetwork network;
		if (rootNetwork != null) {
			// Root network exists
			network = rootNetwork.addSubNetwork();
		} else {
			// Need to create new network with new root.
			network = (CySubNetwork) cyNetworkFactory.createNetwork();
		}

		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(PACKAGE_NAME, this.getClass().getClassLoader());
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			pathway = (Pathway) unmarshaller.unmarshal(is);
		} catch (Exception e) {
			logger.error("Could not ummarshall KGML.", e);
			throw new IOException("Could not unmarshall KGML file.", e);
		} finally {
			if (is != null) {
				is.close();
			}
		}

		this.networks = new CyNetwork[1];
		this.networks[0] = network;

		mapper = new KGMLMapper(pathway, network, groupFactory);
		mapper.doMapping();

		final String pathwayID = mapper.getPathwayId();

		VisualStyle keggStyle = null;
		String targetStyleName = KGMLVisualStyleBuilder.DEF_VS_NAME;

		// Special case: Global Map
		if (pathwayID.equals("01100") || pathwayID.equals("01110")) {
			targetStyleName = KGMLVisualStyleBuilder.GLOBAL_VS_NAME;
		}

		// Check Visual Style exists or not
		for (VisualStyle style : vmm.getAllVisualStyles()) {
			if (style.getTitle().equals(targetStyleName)) {
				keggStyle = style;
				break;
			}
		}
		if (keggStyle == null) {
			if (pathwayID.equals("01100") || pathwayID.equals("01110")) {
				keggStyle = vsBuilder.getGlobalVisualStyle();
			} else {
				keggStyle = vsBuilder.getVisualStyle();
			}
			vmm.addVisualStyle(keggStyle);
		}
		vmm.setCurrentVisualStyle(keggStyle);

		if (taskMonitor != null) {
			taskMonitor.setStatusMessage("KEGG Pathway successfully loaded.");
			taskMonitor.setProgress(1.0);
		}

		if (importFull) {
			insertTasksAfterCurrentTask(new MapExtraDataTask(network));
			if (taskMonitor != null) {
				taskMonitor.setStatusMessage("KEGG Loading more data from KEGG...");
				taskMonitor.setProgress(-1.0);
			}
		}
	}

}
