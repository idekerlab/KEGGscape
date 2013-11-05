package org.cytoscape.keggscape.internal.read.kgml;

import java.util.List;

import org.cytoscape.data.reader.kgml.generated.*;

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
