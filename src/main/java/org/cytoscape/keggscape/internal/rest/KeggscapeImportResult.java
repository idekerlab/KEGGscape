package org.cytoscape.keggscape.internal.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

// @ApiModel(description = "Response for import API call.")
public class KeggscapeImportResult {
	@ApiModelProperty(value = "Cytoscape session-unique ID (SUID) of the new network imported from KEGG", required=true)
	public Long suid;
}
