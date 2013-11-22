package org.cytoscape.keggscape.internal.read.kgml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.keggscape.internal.generated.Entry;
import org.cytoscape.keggscape.internal.generated.Graphics;
import org.cytoscape.keggscape.internal.generated.Pathway;
import org.cytoscape.keggscape.internal.generated.Product;
import org.cytoscape.keggscape.internal.generated.Reaction;
import org.cytoscape.keggscape.internal.generated.Relation;
import org.cytoscape.keggscape.internal.generated.Substrate;

public class KGMLMapper {
	
	private final Pathway pathway;
	private final CyNetwork network;
	private final String pathwayName;
	private Map<CyNode, Double[]> positionMap;
	
	final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
	
	public KGMLMapper(final Pathway pathway, final CyNetwork network) {
		this.pathway = pathway;
		this.network = network;
		this.pathwayName = pathway.getName();
		this.positionMap = new HashMap<CyNode, Double[]>();
	}
	
	public void doMapping() {
		mapEntries();
		mapRelations();
		mapReactions();
	}
	
	private void mapEntries() {
		final List<Entry> entries = pathway.getEntry();
//		System.out.println(entries.size());
		for (final Entry entry : entries) {
			CyNode cyNode = network.addNode();
			network.getRow(cyNode).set(CyNetwork.NAME, entry.getId());
			nodeMap.put(entry.getId(), cyNode);
			
			final Double[] positionArray = new Double[4];
//			System.out.println(entry.getGraphics().get(0).getX());
			positionArray[0] = Double.valueOf(entry.getGraphics().get(0).getX());
			positionArray[1] = Double.valueOf(entry.getGraphics().get(0).getY());
			positionArray[2] = Double.valueOf(entry.getGraphics().get(0).getWidth());
			positionArray[3] = Double.valueOf(entry.getGraphics().get(0).getHeight());
			positionMap.put(cyNode, positionArray);
			
		}
	}
	
	private void mapRelations() {
		final List<Relation> relations = pathway.getRelation();
		System.out.println(relations.size());
		for (final Relation relation : relations) {
			final CyNode sourceNode = nodeMap.get(relation.getEntry1());
			final CyNode targetNode = nodeMap.get(relation.getEntry2());
			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
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
			}
			final List<Product> products = reaction.getProduct();
			for (final Product product : products) {
				final CyNode targetNode = nodeMap.get(product.getId());
				final CyEdge newEdge = network.addEdge(reactionNode, targetNode, true);
			}
		}
	}
	
	protected Map<CyNode, Double[]> getNodePosition() {
		return this.positionMap;
	}
		
}
