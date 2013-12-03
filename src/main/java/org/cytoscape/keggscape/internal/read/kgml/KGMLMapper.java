package org.cytoscape.keggscape.internal.read.kgml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.keggscape.internal.generated.Entry;
import org.cytoscape.keggscape.internal.generated.Pathway;
import org.cytoscape.keggscape.internal.generated.Product;
import org.cytoscape.keggscape.internal.generated.Reaction;
import org.cytoscape.keggscape.internal.generated.Relation;
import org.cytoscape.keggscape.internal.generated.Substrate;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

public class KGMLMapper {
	private static final String NAME_DELIMITER = ", ";
	private static final String ID_DELIMITER = " ";
	
	private final Pathway pathway;
	private final CyNetwork network;
	private final String pathwayName;
	
	private static final String KEGG_NODE_X = "KEGG_NODE_X";
	private static final String KEGG_NODE_Y = "KEGG_NODE_Y";
	private static final String KEGG_NODE_WIDTH = "KEGG_NODE_WIDTH";
	private static final String KEGG_NODE_HEIGHT = "KEGG_NODE_HEIGHT";
	private static final String KEGG_NODE_LABEL = "KEGG_NODE_LABEL";
	private static final String KEGG_NODE_LABEL_LIST = "KEGG_NODE_LABEL_LIST";
	private static final String KEGG_ID = "KEGG_ID";
	private static final String KEGG_NODE_LABEL_COLOR = "KEGG_NODE_LABEL_COLOR";
	private static final String KEGG_NODE_FILL_COLOR = "KEGG_NODE_FILL_COLOR";
	private static final String KEGG_NODE_REACTIONID = "KEGG_NODE_REACTIONID";
	
	private static final String KEGG_NODE_TYPE = "KEGG_NODE_TYPE";

	private static final String KEGG_RELATION_TYPE = "KEGG_RELATION_TYPE";
	private static final String KEGG_REACTION_TYPE = "KEGG_REACTION_TYPE";
	
	final String[] lightBlueMap = { "Other types of O-glycan biosynthesis",
			"Lipopolysaccharide biosynthesis",
			"Glycosaminoglycan biosynthesis - chondroitin sulfate / dermatan sulfate",
			"Glycosphingolipid biosynthesis - ganglio series",
			"Glycosphingolipid biosynthesis - globo series",
			"Glycosphingolipid biosynthesis - lacto and neolacto series",
			"Glycosylphosphatidylinositol(GPI)-anchor biosynthesis",
			"Glycosaminoglycan degradation",
			"Various types of N-glycan biosynthesis",
			"Glycosaminoglycan biosynthesis - keratan sulfate",
			"Mucin type O-Glycan biosynthesis",
			"N-Glycan biosynthesis",
			"Glycosaminoglycan biosynthesis - heparan sulfate / heparin",
			"Other glycan degradation"
	};
	final String[] lightBrownMap = { "Aminobenzoate degradation",
			"Atrazine degradation",
			"Benzoate degradation",
			"Bisphenol degradation",
			"Caprolactam degradation",
			"Chlorocyclohexane and chlorobenzene degradation",
			"DDT degradation",
			"Dioxin degradation",
			"Drug metabolism - cytochrome P450",
			"Drug metabolism - other enzymes",
			"Ethylbenzene degradation",
			"Fluorobenzoate degradation",
			"Metabolism of xenobiotics by cytochrome P450",
			"Naphthalene degradation",
			"Polycyclic aromatic hydrocarbon degradation",
			"Steroid degradation",
			"Styrene degradation",
			"Toluene degradation",
			"Xylene degradation"
	};

	final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
	
	public KGMLMapper(final Pathway pathway, final CyNetwork network) {
		this.pathway = pathway;
		this.network = network;

		this.pathwayName = pathway.getName();
		
	}
	
	public void doMapping() {
		createKeggNodeTable();
		createKeggEdgeTable();
		if (pathway.getNumber().equals("01100")) {
			mapGlobalEntries();
			mapGlobalReactions();
		} else {
            mapEntries();
            mapRelations();
            mapReactions();
		}
	}
	
	private void createKeggEdgeTable() {
		// TODO Auto-generated method stub
		network.getDefaultEdgeTable().createColumn(KEGG_RELATION_TYPE, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_REACTION_TYPE, String.class, true);
	}

	private void createKeggNodeTable() {
		network.getDefaultNodeTable().createColumn(KEGG_NODE_X, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_Y, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_WIDTH, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_HEIGHT, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_LABEL, String.class, true);
		network.getDefaultNodeTable().createListColumn(KEGG_NODE_LABEL_LIST, String.class, true);
		network.getDefaultNodeTable().createListColumn(KEGG_ID, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_LABEL_COLOR, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_FILL_COLOR, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_REACTIONID, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_TYPE, String.class, true);
	}


	private void mapEntries() {
		final List<Entry> entries = pathway.getEntry();
//		System.out.println(entries.size());
		
		for (final Entry entry : entries) {
			CyNode cyNode = network.addNode();
			network.getRow(cyNode).set(CyNetwork.NAME, entry.getId());
			network.getRow(cyNode).set(KEGG_NODE_REACTIONID, entry.getReaction());
			mapIdList(entry.getName(), ID_DELIMITER, network.getRow(cyNode), KEGG_ID);
			mapIdList(entry.getGraphics().get(0).getName(), NAME_DELIMITER, network.getRow(cyNode), KEGG_NODE_LABEL_LIST);

			network.getRow(cyNode).set(KEGG_NODE_X, entry.getGraphics().get(0).getX());
			network.getRow(cyNode).set(KEGG_NODE_Y, entry.getGraphics().get(0).getY());
			network.getRow(cyNode).set(KEGG_NODE_WIDTH, entry.getGraphics().get(0).getWidth());
			network.getRow(cyNode).set(KEGG_NODE_HEIGHT, entry.getGraphics().get(0).getHeight());
			network.getRow(cyNode).set(KEGG_NODE_LABEL, entry.getGraphics().get(0).getName());
			network.getRow(cyNode).set(KEGG_NODE_LABEL_COLOR, entry.getGraphics().get(0).getFgcolor());
			network.getRow(cyNode).set(KEGG_NODE_FILL_COLOR, entry.getGraphics().get(0).getBgcolor());
			
			network.getRow(cyNode).set(KEGG_NODE_TYPE, entry.getGraphics().get(0).getType());
			nodeMap.put(entry.getId(), cyNode);
		}
	}
	
	private void mapGlobalEntries() {
		final List<Entry> entries = pathway.getEntry();
		
		for (final Entry entry : entries) {
			CyNode cyNode = network.addNode();
			network.getRow(cyNode).set(CyNetwork.NAME, entry.getId());

			network.getRow(cyNode).set(KEGG_NODE_X, entry.getGraphics().get(0).getX());
			network.getRow(cyNode).set(KEGG_NODE_Y, entry.getGraphics().get(0).getY());
			network.getRow(cyNode).set(KEGG_NODE_WIDTH, entry.getGraphics().get(0).getWidth());
			network.getRow(cyNode).set(KEGG_NODE_HEIGHT, entry.getGraphics().get(0).getHeight());
			network.getRow(cyNode).set(KEGG_NODE_LABEL, entry.getGraphics().get(0).getName());
			
			if (entry.getType().equals("map") && Arrays.asList(lightBlueMap).contains(entry.getGraphics().get(0).getName())) {
                network.getRow(cyNode).set(KEGG_NODE_LABEL_COLOR, "#99CCFF");
                network.getRow(cyNode).set(KEGG_NODE_FILL_COLOR, "#FFFFFF");
			} else if (entry.getType().equals("map") && Arrays.asList(lightBrownMap).contains(entry.getGraphics().get(0).getName())) {
                network.getRow(cyNode).set(KEGG_NODE_LABEL_COLOR, "#DA8E82");
                network.getRow(cyNode).set(KEGG_NODE_FILL_COLOR, "#FFFFFF");				
			} else {
                network.getRow(cyNode).set(KEGG_NODE_LABEL_COLOR, entry.getGraphics().get(0).getFgcolor());
                network.getRow(cyNode).set(KEGG_NODE_FILL_COLOR, entry.getGraphics().get(0).getBgcolor());
			}
			
			network.getRow(cyNode).set(KEGG_NODE_TYPE, entry.getGraphics().get(0).getType());
			nodeMap.put(entry.getId(), cyNode);			
		}
	}
	
	private void mapRelations() {
		final List<Relation> relations = pathway.getRelation();
		System.out.println(relations.size());
		for (final Relation relation : relations) {
			final CyNode sourceNode = nodeMap.get(relation.getEntry1());
			final CyNode targetNode = nodeMap.get(relation.getEntry2());
			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
			network.getRow(newEdge).set(KEGG_RELATION_TYPE, relation.getType());
		} 
	}
	
	private void mapReactions() {
		final List<Reaction> reactions = pathway.getReaction();
		System.out.println(reactions.size());
		for (Reaction reaction : reactions) {
			final CyNode reactionNode = nodeMap.get(reaction.getId());
			final List<Substrate> substrates = reaction.getSubstrate();
			for (final Substrate substrate : substrates) {
				final CyNode sourceNode = nodeMap.get(substrate.getId());
				final CyEdge newEdge = network.addEdge(sourceNode, reactionNode, true);
				network.getRow(newEdge).set(KEGG_REACTION_TYPE, reaction.getType());
			}
			final List<Product> products = reaction.getProduct();
			for (final Product product : products) {
				final CyNode targetNode = nodeMap.get(product.getId());
				final CyEdge newEdge = network.addEdge(reactionNode, targetNode, true);
				network.getRow(newEdge).set(KEGG_REACTION_TYPE, reaction.getType());
			}
		}
	}
	
	private void mapGlobalReactions() {
		final List<Reaction> reactions = pathway.getReaction();
		for (Reaction reaction : reactions) {
			final List<Substrate> substrates = reaction.getSubstrate();
			final List<Product> products = reaction.getProduct();
			for (Substrate substrate : substrates) {
				final CyNode substrateNode = nodeMap.get(substrate.getId());
				for (Product product : products) {
					final CyNode productNode = nodeMap.get(product.getId());
					final CyEdge newEdge = network.addEdge(substrateNode, productNode, true);
				}
			}
		}
	}

	
	private final void mapIdList(final String idListText, final String delimiter, final CyRow row, final String columnName) {
		final List<String> idList = new ArrayList<String>();
		final String[] ids = idListText.split(delimiter);
		for(String id: ids) {
			idList.add(id);
		}
		row.set(columnName, idList);
	}
}
