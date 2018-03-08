package org.cytoscape.keggscape.internal.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "Required parameters for importing network(s) from KEGG.")
public class KeggImportParams {

	@ApiModelProperty(value = "ID of the KEGG pathway", example="eco00010", required=true)
	public String pathwayid;

	public KeggImportParams(String pathwayid) {
		this.pathwayid = pathwayid;
	}
}
