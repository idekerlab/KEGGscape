package org.cytoscape.keggscape.internal;

import io.swagger.annotations.ApiModelProperty;

public class KeggscapeDocumentation {
	public static final String GENERIC_SWAGGER_NOTES = "KEGGscape will import the KEGG pathway (specified by {pathid}) into Cytoscape." + '\n' + '\n' 
	+ "For example, /keggscape/v1/eco00010 imports http://www.genome.jp/kegg-bin/show_pathway?eco00010 into Cytoscape." + '\n' + '\n'
	+ "You can try it by filling 'eco00010' in the following 'Value box' and clicking 'Try it out!' button." + '\n' + '\n';
}
