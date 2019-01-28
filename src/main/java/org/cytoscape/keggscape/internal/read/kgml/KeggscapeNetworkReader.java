package org.cytoscape.keggscape.internal.read.kgml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.read.AbstractCyNetworkReader;
import org.cytoscape.keggscape.internal.generated.Pathway;
import org.cytoscape.keggscape.internal.style.KGMLVisualStyleBuilder;
import org.cytoscape.keggscape.internal.task.MapExtraDataTask;
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
import java.util.Scanner;

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
	
	private VisualStyle keggStyle = null;
	

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

		// String tmp = new Scanner(is).useDelimiter("\\Z").next();
        // System.out.println(tmp);
	}

	@Override
	public CyNetworkView buildCyNetworkView(final CyNetwork network) {
		return cyNetworkViewFactory.createNetworkView(network);
		// TODO Apply (X,Y) to the nodes
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

			// JAXBContext jc = JAXBContext.newInstance( "com.acme.foo" );
			// Unmarshaller u = jc.createUnmarshaller();
			// URL url = new URL( "http://beaker.east/nosferatu.xml" );
			// Object o = u.unmarshal( url );
			// System.out.println(o);

			final JAXBContext jaxbContext = JAXBContext.newInstance(PACKAGE_NAME, this.getClass().getClassLoader());
			// System.out.println(jaxbContext.toString());
			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			//unmarshaller.setProperty(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, "all");
			//unmarshaller.setProperty(javax.xml.XMLConstants.ACCESS_EXTERNAL_DTD, Boolean.TRUE);
			System.setProperty("javax.xml.accessExternalDTD", "all");
			
			// String tmp = new Scanner(is).useDelimiter("\\Z").next();
			// System.out.println(tmp);
			// System.out.println("netreader118");
			pathway = (Pathway) unmarshaller.unmarshal(is);

			// if (pathway == null) {
			// 	System.out.println("why this pathway is null?");
			// }
		} catch (Exception e) {
			System.out.println("In Exception===============");
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

		final String targetStyleName;
		if (KGMLMapper.GLOBAL_MAP_ID.contains(pathwayID)) {
			targetStyleName = KGMLVisualStyleBuilder.GLOBAL_VS_NAME;
		} else {
			targetStyleName = KGMLVisualStyleBuilder.DEF_VS_NAME;
		}

		// Check Visual Style exists or not
		for (final VisualStyle style : vmm.getAllVisualStyles()) {
			if (style.getTitle().equals(targetStyleName)) {
				keggStyle = style;
				break;
			}
		}
		
		if (keggStyle == null) {
			// Need to create custom style.
			if (KGMLMapper.GLOBAL_MAP_ID.contains(pathwayID)) {
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
