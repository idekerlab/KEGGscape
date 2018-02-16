// package org.cytoscape.data.reader.kgml.test;
//
// import static org.junit.Assert.*;
//
// import java.util.List;
//
// import org.cytoscape.model.CyEdge;
// import org.cytoscape.model.CyNetwork;
// import org.cytoscape.model.CyNode;
// import org.cytoscape.view.model.CyNetworkView;
// import org.junit.Test;
//
// public class DiseasePathwayTest extends AbstractKeggReaderTest {
//
// 	@Test
// 	public void testDiseasePathway() throws Exception {
// 		final String collectionName = "KEGG Metabolic Pathways";
// 		final String humanCancer = "src/test/resources/testData/kgml/non-metabolic/organisms/hsa/hsa05200.xml";
//
// 		final CyNetworkView humanCancerView = loadKGML(collectionName, humanCancer);
// 		final CyNetwork humanCancerNetwork = humanCancerView.getModel();
//
// 		final List<CyNode> nodes = humanCancerNetwork.getNodeList();
// 		final List<CyEdge> edges = humanCancerNetwork.getEdgeList();
//
// 		assertEquals(265, nodes.size());
// 		assertEquals(204, edges.size());
// 	}
//
// }
