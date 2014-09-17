package org.cytoscape.data.reader.kgml.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.io.FileInputStream;
import java.io.InputStream;

import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.keggscape.internal.KGMLVisualStyleBuilder;
import org.cytoscape.keggscape.internal.read.kgml.KeggscapeNetworkReader;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.junit.After;
import org.junit.Before;

public abstract class AbstractKeggReaderTest {

	// Actual instances
	private NetworkViewTestSupport support = new NetworkViewTestSupport();
	private final CyNetworkFactory networkFactory = support.getNetworkFactory();
	private final CyNetworkViewFactory viewFactory = support.getNetworkViewFactory();
	private final CyRootNetworkManager rootNetworkManager = mock(CyRootNetworkManager.class);
	private final CyNetworkManager networkManager = support.getNetworkManager();

	// Mocks
	private VisualMappingManager vmm;
	private KGMLVisualStyleBuilder builder;
	private TaskMonitor tm;
	private CyGroupFactory groupFactory;

	@Before
	public void setUp() throws Exception {
		this.tm = mock(TaskMonitor.class);
		this.vmm = mock(VisualMappingManager.class);
		this.builder = mock(KGMLVisualStyleBuilder.class);
		this.groupFactory = mock(CyGroupFactory.class);
	}

	@After
	public void tearDown() throws Exception {
	}

	protected final CyNetworkView loadKGML(final String collectionName, final String fileName) throws Exception {
		final InputStream is = new FileInputStream(fileName);
		final CyNetworkReader reader = new KeggscapeNetworkReader(collectionName, is, viewFactory, networkFactory,
				networkManager, rootNetworkManager, builder, vmm, groupFactory);
		reader.run(tm);
		is.close();
		final CyNetwork[] networks = reader.getNetworks();
		assertNotNull(networks);
		assertEquals(1, networks.length);
		final CyNetwork network = networks[0];
		final CyNetworkView view = reader.buildCyNetworkView(network);
		assertNotNull(view);
		return view;
	}
}