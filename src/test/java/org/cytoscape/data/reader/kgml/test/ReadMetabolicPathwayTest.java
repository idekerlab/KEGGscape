// package org.cytoscape.data.reader.kgml.test;
//
// import static org.junit.Assert.assertEquals;
//
// import java.util.List;
//
// import org.cytoscape.model.CyEdge;
// import org.cytoscape.model.CyNetwork;
// import org.cytoscape.model.CyNode;
// import org.cytoscape.view.model.CyNetworkView;
// import org.junit.Test;
//
// public class ReadMetabolicPathwayTest extends AbstractKeggReaderTest {
//
// 	@Test
// 	public void testMetabolicPathway() throws Exception {
// 		final String collectionName = "KEGG Metabolic Pathways";
// 		final String humanTcaCycle = "src/test/resources/testData/kgml/metabolic/organisms/hsa/hsa00020.xml";
//
// 		final CyNetworkView humanTcaCycleView = loadKGML(collectionName, humanTcaCycle);
// 		final CyNetwork humanTcaCycleNetwork = humanTcaCycleView.getModel();
//
// 		final List<CyNode> nodes = humanTcaCycleNetwork.getNodeList();
// 		final List<CyEdge> edges = humanTcaCycleNetwork.getEdgeList();
//
// 		assertEquals(65, nodes.size());
// 		assertEquals(68, edges.size());
// 	}
// }
