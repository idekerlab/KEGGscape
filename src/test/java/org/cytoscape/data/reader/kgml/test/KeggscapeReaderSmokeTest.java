package org.cytoscape.data.reader.kgml.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.junit.Test;

/**
 * Simple smoke test to ensure that a KGML file can be read and a network/view
 * are created without errors.
 */
public class KeggscapeReaderSmokeTest extends AbstractKeggReaderTest {

	@Test
	public void testLoadHumanTcaCycleKgml() throws Exception {
		final String collectionName = "KEGG Metabolic Pathways";
		final String humanTcaCycle = "src/test/resources/testData/kgml/metabolic/organisms/hsa/hsa00020.xml";

		final CyNetworkView view = loadKGML(collectionName, humanTcaCycle);
		assertNotNull(view);

		final CyNetwork network = view.getModel();
		assertNotNull(network);
		assertTrue("Network should contain at least one node", !network.getNodeList().isEmpty());
	}
}

