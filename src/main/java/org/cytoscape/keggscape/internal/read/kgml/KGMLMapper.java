package org.cytoscape.keggscape.internal.read.kgml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.keggscape.internal.generated.Entry;
import org.cytoscape.keggscape.internal.generated.Pathway;

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
		mapNode();
	}
	
	private void mapNode() {
		final List<Entry> components = pathway.getEntry();
		System.out.println(components.size());
		for (final Entry comp : components) {
			CyNode cyNode = network.addNode();
			network.getRow(cyNode).set(CyNetwork.NAME, comp.getId());
			nodeMap.put(comp.getId(), cyNode);
		}
	}
}
