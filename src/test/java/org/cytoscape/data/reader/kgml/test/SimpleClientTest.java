package org.cytoscape.data.reader.kgml.test;


import org.cytoscape.ding.NetworkViewTestSupport;
import org.cytoscape.keggscape.internal.wsclient.TogowsClient;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SimpleClientTest {
	
	private NetworkViewTestSupport support = new NetworkViewTestSupport();
	private final CyNetworkFactory networkFactory = support.getNetworkFactory();

	private CyNetwork network;
	
	@Before
	public void setUp() throws Exception {
	
		this.network = networkFactory.createNetwork();
	}

	@After
	public void tearDown() throws Exception {
	}

	
	@Test
	public void testClient() throws Exception {
		TogowsClient client = new TogowsClient();
		
		client.map("hsa00020", network);
		
	}
}
