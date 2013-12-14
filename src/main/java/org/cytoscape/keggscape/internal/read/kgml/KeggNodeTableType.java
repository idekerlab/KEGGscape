package org.cytoscape.keggscape.internal.read.kgml;

public enum KeggNodeTableType {
	X("KEGG_NODE_X"), Y("KEGG_NODE_Y"), WIDTH("KEGG_NODE_WIDTH"), HEIGHT("KEGG_NODE_HEIGHT"),
	LABEL("KEGG_NODE_LABEL"), LABEL_COLOR("KEGG_NODE_LABEL_COLOR"), FILL_COLOR("KEGG_NODE_FILL_COLOR"),
	LABEL_LIST_FIRST("KEGG_NODE_LABEL_LIST_FIRST"), SHAPE("KEGG_NODE_SHAPE"), TYPE("KEGG_NODE_TYPE");
	
	private String tag;
	
	private KeggNodeTableType(final String tag) {
		this.tag = tag;
	}
	
	public String getTag() {
		return this.tag;
	}
	
	public static KeggNodeTableType getType(final String tag) {
        for(KeggNodeTableType entry: KeggNodeTableType.values()) {
            if(entry.getTag().equals(tag))
                    return entry;
        }
    
        return null;
	}

}
