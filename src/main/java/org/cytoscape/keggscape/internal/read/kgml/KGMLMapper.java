package org.cytoscape.keggscape.internal.read.kgml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyEdge;
import org.cytoscape.keggscape.internal.generated.Entry;
import org.cytoscape.keggscape.internal.generated.Pathway;
import org.cytoscape.keggscape.internal.generated.Relation;

public class KGMLMapper {
	
	private final Pathway pathway;
	private final CyNetwork network;
	private final String pathwayName;
	
	final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
	
	public KGMLMapper(final Pathway pathway, final CyNetwork network) {
		this.pathway = pathway;
		this.network = network;
		this.pathwayName = pathway.getName();
	}
	
	public void doMapping() {
		mapEntries();
		mapRelations();
	}
	
	private void mapEntries() {
		final List<Entry> entries = pathway.getEntry();
		System.out.println(entries.size());
		for (final Entry entry : entries) {
			CyNode cyNode = network.addNode();
			network.getRow(cyNode).set(CyNetwork.NAME, entry.getId());
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
		} 
	}
}
