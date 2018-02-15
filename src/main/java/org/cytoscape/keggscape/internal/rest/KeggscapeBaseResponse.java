package org.cytoscape.keggscape.internal.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(description = "Response for import API call.")
public final class KeggscapeBaseResponse {
	@ApiModelProperty(value = "Cytoscape session-unique ID (SUID) of the new network imported from KEGG")
	public Long suid;

	public KeggscapeBaseResponse() {}

	public KeggscapeBaseResponse(final Long suid) {
		this.suid = suid;
	}
}
