package org.cytoscape.keggscape.internal.wsclient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.cytoscape.keggscape.internal.read.kgml.KGMLMapper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.cytoscape.keggscape.internal.wsclient.TogowsTags.*;

public class TogowsClient {

	private static final String TOGOWS_URL = "http://togows.dbcls.jp/entry/pathway/";
	private static final String FORMAT_JSON = ".json";

	public void map(final String id, final CyNetwork network) throws Exception {
		if (id == null) {
			throw new NullPointerException("Pathway ID is null.");
		}

		final String url = TOGOWS_URL + id + FORMAT_JSON;
		final CloseableHttpClient httpclient = HttpClients.createDefault();
		String responseBody = null;
		try {
			HttpGet httpget = new HttpGet(url);
			System.out.println("Executing request " + httpget.getRequestLine());

			// Create a custom response handler
			ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
				public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
					int status = response.getStatusLine().getStatusCode();
					if (status >= 200 && status < 300) {
						HttpEntity entity = response.getEntity();
						return entity != null ? EntityUtils.toString(entity) : null;
					} else {
						throw new ClientProtocolException("Unexpected response status: " + status);
					}
				}
			};
			responseBody = httpclient.execute(httpget, responseHandler);
		} finally {
			httpclient.close();
		}

		// create an ObjectMapper instance.
		ObjectMapper mapper = new ObjectMapper();
		// use the ObjectMapper to read the json string and create a tree
		JsonNode rootNode = mapper.readTree(responseBody);
		mapEntries(rootNode.get(0), network);
	}

	private void mapEntries(final JsonNode rootNode, final CyNetwork network) {
		JsonNode id = rootNode.get("entry_id");
		JsonNode name = rootNode.get("name");
		JsonNode description = rootNode.get(DESCRIPTION);
		JsonNode classes = rootNode.get(CLASSES);
		JsonNode diseases = rootNode.get(DISEASES);
		JsonNode modules = rootNode.get(MODULES);
		JsonNode genes = rootNode.get(GENES);
		JsonNode compounds = rootNode.get("compounds");
		System.out.println("id: " + id.textValue());
		System.out.println("name: " + name.textValue());
		System.out.println("desc: " + description.textValue());

		final CyColumn descColumn = network.getDefaultNetworkTable().getColumn(DESCRIPTION);
		if(descColumn == null) {
			createColumns(network);
		}
		
		final CyRow networkRow = network.getRow(network);

		networkRow.set(DESCRIPTION, description.textValue());
		
		mapObjects(modules, MODULES, networkRow);
		mapObjects(diseases, DISEASES, networkRow);
		
		mapList(classes, CLASSES, networkRow);
		
		mapGenes(genes, network);
	} 
	
	private final void createColumns(final CyNetwork network) {
		network.getDefaultNetworkTable().createColumn(DESCRIPTION, String.class, false);
		network.getDefaultNodeTable().createColumn(KGMLMapper.KEGG_DEFINITION, String.class, false);

		network.getDefaultNetworkTable().createListColumn(MODULES, String.class, false);
		network.getDefaultNetworkTable().createListColumn(DISEASES, String.class, false);
		network.getDefaultNetworkTable().createListColumn(MODULES + "_id", String.class, false);
		network.getDefaultNetworkTable().createListColumn(DISEASES + "_id", String.class, false);
		
		network.getDefaultNetworkTable().createListColumn(CLASSES, String.class, false);
		

	}

	private final void mapGenes(final JsonNode genes, CyNetwork network) {
		final List<CyNode> nodes = network.getNodeList();
		for(final CyNode node: nodes) {
			final List<String> nameList = network.getRow(node).getList(KGMLMapper.KEGG_ID, String.class);
			JsonNode gene = null;
			for(final String name: nameList) {
				final String originalId = name.split(":")[1];
				gene = genes.get(originalId);
				if(gene != null) {
					break;
				}
			}
			
			if(gene == null) continue;
			
			final String geneText = gene.textValue();
			final String[] parts = geneText.split(";");
			network.getRow(node).set(KGMLMapper.KEGG_DEFINITION, parts[1]);
			// Replace label
			network.getRow(node).set(KGMLMapper.KEGG_NODE_LABEL_LIST_FIRST, parts[0]);
		}
	}
	
	
	private final void mapList(final JsonNode listNode, final String columnName, final CyRow row) {
		final List<String> list = new ArrayList<String>();
		for(JsonNode node: listNode) {
			list.add(node.textValue());
		}
		row.set(columnName, list);
	}
	
	
	private final void mapObjects(final JsonNode listNode, final String columnName, final CyRow row) {
		final List<String> list = new ArrayList<String>();
		final List<String> idList = new ArrayList<String>();
		
		final Iterator<String> fNames = listNode.fieldNames();
		while (fNames.hasNext()) {
			final String name = fNames.next();
//			System.out.println(name);
			list.add(name);
			idList.add(listNode.get(name).textValue());
		}
		row.set(columnName, list);
		row.set(columnName + "_id", idList);
	}

}
