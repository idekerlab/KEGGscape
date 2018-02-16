package org.cytoscape.keggscape.internal.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.*;

import org.cytoscape.keggscape.internal.task.ImportKGMLTask;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.keggscape.internal.style.KGMLVisualStyleBuilder;
import org.cytoscape.keggscape.internal.rest.HeadlessTaskMonitor;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.model.CyNetwork;

import org.cytoscape.ci.model.CIError;
import org.cytoscape.ci.model.CIResponse;
import org.cytoscape.ci.CIResponseFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Api
@Path("/keggscape/v1")
public class KeggscapeResource {

	private static final Logger logger = LoggerFactory.getLogger(KeggscapeResource.class);

	private final CyNetworkViewFactory cyNetworkViewFactory;
  private final CyNetworkFactory cyNetworkFactory;
  private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;

	private final KGMLVisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;

	private final CyGroupFactory groupFactory;
	private final CIResponseFactory ciResponseFactory;

	private final TaskMonitor tm;
	private final CyNetworkViewManager cyNetworkViewManager;

	public KeggscapeResource(final CyNetworkViewFactory cyNetworkViewFactory,
      final CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager,
      final CyRootNetworkManager cyRootNetworkManager, final KGMLVisualStyleBuilder vsBuilder,
      final VisualMappingManager vmm, final CyGroupFactory groupFactory,
			final CIResponseFactory ciResponseFactory, final CyNetworkViewManager cyNetworkViewManager)
  {
        this.cyNetworkViewFactory = cyNetworkViewFactory;
        this.cyNetworkFactory = cyNetworkFactory;
        this.cyNetworkManager = cyNetworkManager;
        this.cyRootNetworkManager = cyRootNetworkManager;
        this.vsBuilder = vsBuilder;
        this.vmm = vmm;
        this.groupFactory = groupFactory;
				this.ciResponseFactory = ciResponseFactory;
				this.tm = new HeadlessTaskMonitor();
				this.cyNetworkViewManager = cyNetworkViewManager;
  }

	// public CyNetworkView loadKGML(final String collectionName, final String fileName) throws Exception
	// {
	// 		final URL foo = new URL("http://rest.kegg.jp/get/" + fileName + "/kgml");
	// 		final URLConnection yc = foo.openConnection();
	// 		final InputStream is = yc.getInputStream();
	// 		final CyNetworkReader reader = new KeggscapeNetworkReader(collectionName, is,
	// 				cyNetworkViewFactory, cyNetworkFactory,	cyNetworkManager, cyRootNetworkManager,
	// 				vsBuilder, vmm, groupFactory);
	// 		reader.run(tm);
	// 		is.close();
	// 		final CyNetwork[] networks = reader.getNetworks();
	// 		// assertNotNull(networks);
	// 		// assertEquals(1, networks.length);
	// 		final CyNetwork network = networks[0];
	// 		final CyNetworkView view = reader.buildCyNetworkView(network);
	// 		// assertNotNull(view);
	// 		return view;
	// }

	@ApiModel(
					value="Keggscape App Response",
					description="Kegg pathway import Results in CI Format",
					parent=CIResponse.class)
	public static class KeggscapeAppResponse extends CIResponse<KeggscapeImportResult>{

	}

	// final String collectionName = "KEGG Metabolic Pathways";
	// final String bar = "hsa00020";
	// final String humanTcaCycle = "src/test/resources/testData/kgml/metabolic/organisms/hsa/hsa00020.xml";

  // @Path("/pathway")
	// @POST
	// @Produces("application/json")
	// @Consumes("application/json")
	// @Path("{collectionName}/import/{pathwayID}")
	// @ApiOperation(value = "Import KEGG pathway with the KEGG pathway ID",
	// 							notes = "Import KEGG pathway with the KEGG pathway ID",
	// 							response = KeggscapeAppResponse.class)
	// @ApiResponses(value = {
	// 				@ApiResponse(code = 404, message = "failed to import KEGG pathway",
	// 					response = CIResponse.class), })

	// @POST
	// @Produces("application/json")
	// @Consumes("application/json")
	// @Path("/{pathwayid}")
	// @ApiOperation(value = "Import network from KEGG", notes = "Import network from KEGG", response = KeggscapeAppResponse.class)
	// @ApiResponses(value = {
	// 		@ApiResponse(code = 404, message = "Network does not exist", response = KeggscapeAppResponse.class), })
	@GET
  @Produces(MediaType.APPLICATION_JSON)
	public void createNetworkFromKegg(
					 // @ApiParam(value = "Properties required to import network from NDEx.", required = true) KeggImportParams params){
					 ){
						 // System.out.println("---------------------hogepiyomoge==========================");
						 ImportKGMLTask importer;
						 // if(params.pathwayid == null) {
							//  final String message = "Must provide a KEGG pathwayID to import a network";
							//  logger.error(message);
						 // }
						 try{
						 	importer = new ImportKGMLTask(cyNetworkViewFactory, cyNetworkFactory,
								cyNetworkManager, cyRootNetworkManager, vsBuilder, vmm,
								groupFactory, "hsa01100", cyNetworkViewManager);
							// System.out.println("===before importer.run===");
							importer.run(tm);
						} catch (Exception e) {
			          System.out.println("ERROR: " + e.toString());
			      }


						// try {
						// 	return ciResponseFactory.getCIResponse(new Object());
						// } catch (InstantiationException | IllegalAccessException e) {
						// 	final String message = "Could not create wrapped CI JSON. Error: " + e.getMessage();
						// 	logger.error(message);
						//
						// }


					}
				}

// 	public void getloadKGML(
// 					@ApiParam(value = "Collection name") @PathParam("collectionName") String collectionName,
// 					@ApiParam(value = "KEGG pathway ID") @PathParam("pathwayID") String pathwayID) {
// 			System.out.println("Importing KEGG pathway via REST");
//
// 			try{
// 			final URL foo = new URL("http://rest.kegg.jp/get/" + pathwayID + "/kgml");
// 			final URLConnection yc = foo.openConnection();
// 			// final InputStream is = yc.getInputStream();
//
// 			final CyNetworkReader reader = new KeggscapeNetworkReader(collectionName, yc.getInputStream(),
// 					cyNetworkViewFactory, cyNetworkFactory,	cyNetworkManager, cyRootNetworkManager,
// 					vsBuilder, vmm, groupFactory);
// 			reader.run(tm);
//
// 			final CyNetwork[] networks =  reader.getNetworks();
// 			// assertNotNull(networks);
// 			// assertEquals(1, networks.length);
// 			final CyNetwork network = networks[0];
// 			final CyNetworkView view = reader.buildCyNetworkView(network);
// 			// assertNotNull(view);
// 			// return view;
//
// 		} catch (MalformedURLException male) {
// 				System.out.println("ERROR: WRONG URL " + male.toString());
// 		} catch (IOException ioe) {
// 				System.out.println("ERROR: WRONG FILE " + ioe.toString());
// 		} catch (Exception e) {
// 				System.out.println("ERROR: " + e.toString());
// 		}
//
// 			// CyNetworkView keggpathwayView = loadKGML("KEGG Metabolic Pathways", pathwayID);
// 			// return keggpathwayView;
//
// 		  // KeggscapeAppResponse response = new KeggscapeAppResponse(keggpathwayView.getSUID());
// 			// return ciResponseFactory.getCIResponse(response, KeggscapeAppResponse.class);
//
// 			// return Response.status(keggpathwayView.getModel().getNodeCount() > 0 ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
// 			// 				.type(MediaType.APPLICATION_JSON)
// 			// 				.entity()
// 	}
// 	// public int loadKGML("KEGG Metabolic Pathways", "hsa00020");
//
// }
