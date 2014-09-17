package org.cytoscape.keggscape.internal.read.kgml;

public enum KEGGTags {
	
	COMPOUND("compound"), MAP("map"), GROUP("group"), GENE("gene");
	
	private final String tag;
	
	private KEGGTags(final String tag) {
		this.tag = tag;
	}
	
	public String getTag() {
		return tag;
	}
}
