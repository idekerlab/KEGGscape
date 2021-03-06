package org.cytoscape.keggscape.internal.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.cytoscape.keggscape.internal.task.ImportKGMLTask;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.keggscape.internal.style.KGMLVisualStyleBuilder;
import org.cytoscape.keggscape.internal.rest.HeadlessTaskMonitor;
import org.cytoscape.keggscape.internal.KeggscapeDocumentation;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.ci.model.CIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiOperation;
import org.osgi.util.tracker.ServiceTracker;
import org.cytoscape.ci.CIErrorFactory;
import org.cytoscape.ci.CIExceptionFactory;
import org.cytoscape.ci.CIResponseFactory;

@Api(tags = {"Apps: KEGGscape"})
@Path("/keggscape/v1")
public class KeggscapeResource {

	private final CyNetworkViewFactory cyNetworkViewFactory;
	private final CyNetworkFactory cyNetworkFactory;
	private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;

	private final KGMLVisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;

	private final CyGroupFactory groupFactory;

	private final TaskMonitor tm;
	private final CyNetworkViewManager cyNetworkViewManager;


	private final ServiceTracker ciResponseFactoryTracker;

	private CIResponseFactory getCIResponseFactory() {
		return (CIResponseFactory) ciResponseFactoryTracker.getService();
	}
	//private final CIResponseFactory ciResponseFactory;

	private final ServiceTracker ciExceptionFactoryTracker;
	private CIExceptionFactory getCIExceptionFactory() {
		return (CIExceptionFactory) ciExceptionFactoryTracker.getService();
	}
	//private final CIExceptionFactory ciExceptionFactory;

	private final ServiceTracker ciErrorFactoryTracker;
	private CIErrorFactory getCIErrorFactory() {
		return (CIErrorFactory) ciErrorFactoryTracker.getService();
	}
	//private final CIErrorFactory ciErrorFactory;

	public KeggscapeResource(final CyNetworkViewFactory cyNetworkViewFactory, final CyNetworkFactory cyNetworkFactory,
			final CyNetworkManager cyNetworkManager, final CyRootNetworkManager cyRootNetworkManager,
			final KGMLVisualStyleBuilder vsBuilder, final VisualMappingManager vmm, final CyGroupFactory groupFactory,
			final ServiceTracker ciResponseFactoryTracker, final ServiceTracker ciExceptionFactoryTracker, final ServiceTracker ciErrorFactoryTracker,
			final CyNetworkViewManager cyNetworkViewManager) {
		this.cyNetworkViewFactory = cyNetworkViewFactory;
		this.cyNetworkFactory = cyNetworkFactory;
		this.cyNetworkManager = cyNetworkManager;
		this.cyRootNetworkManager = cyRootNetworkManager;
		this.vsBuilder = vsBuilder;
		this.vmm = vmm;
		this.groupFactory = groupFactory;
		this.tm = new HeadlessTaskMonitor();
		this.cyNetworkViewManager = cyNetworkViewManager;

		this.ciResponseFactoryTracker = ciResponseFactoryTracker;
		this.ciExceptionFactoryTracker = ciExceptionFactoryTracker;
		this.ciErrorFactoryTracker = ciErrorFactoryTracker;
	}

	@ApiModel(value = "Keggscape App Response", description = "Kegg pathway import Results in CI Format", parent = CIResponse.class)
	public static class KeggscapeAppResponse extends CIResponse<KeggscapeImportResult> {
	}

	@GET
	@Path("/{pathid}")
	@Produces(MediaType.APPLICATION_JSON)
	@ApiOperation(
		value = "Import a KEGG pathway and create the network view",
		notes = KeggscapeDocumentation.GENERIC_SWAGGER_NOTES)
	public void createNetworkFromKegg(@ApiParam(value = "KEGG pathway ID") @PathParam("pathid") final String pathid) {
		System.out.println(pathid);

		ImportKGMLTask importer;
		// if(params.pathwayid == null) {
		//  final String message = "Must provide a KEGG pathwayID to import a network";
		//  logger.error(message);
		// }
		try {
			importer = new ImportKGMLTask(cyNetworkViewFactory, cyNetworkFactory, cyNetworkManager,
					cyRootNetworkManager, vsBuilder, vmm, groupFactory, pathid, cyNetworkViewManager);
			// System.out.println("===before importer.run===");
			importer.run(tm);
		} catch (Exception e) {
			System.out.println("ERROR: " + e.toString());
		}

	}
}
