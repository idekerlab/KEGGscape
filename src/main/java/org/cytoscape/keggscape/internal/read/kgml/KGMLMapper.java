package org.cytoscape.keggscape.internal.read.kgml;

import java.util.List;

import org.cytoscape.keggscape.internal.generated.Entry;
import org.cytoscape.keggscape.internal.generated.Pathway;

public class KGMLMapper {
	
	private final Pathway pathway;
	private final String pathwayName;
	
	public KGMLMapper(final Pathway pathway) {
		this.pathway = pathway;
		this.pathwayName = pathway.getName();
	}
	
	public void doMapping() {
		mapNode();
	}
	
	private void mapNode() {
		final List<Entry> components = pathway.getEntry();
		System.out.println(components.size());
		
	}

}
