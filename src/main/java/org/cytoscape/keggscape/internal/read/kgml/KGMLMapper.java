package org.cytoscape.keggscape.internal.read.kgml;

import static org.cytoscape.keggscape.internal.read.kgml.KEGGTags.COMPOUND;
import static org.cytoscape.keggscape.internal.read.kgml.KEGGTags.GROUP;
import static org.cytoscape.keggscape.internal.read.kgml.KEGGTags.MAP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.keggscape.internal.generated.Component;
import org.cytoscape.keggscape.internal.generated.Entry;
import org.cytoscape.keggscape.internal.generated.Graphics;
import org.cytoscape.keggscape.internal.generated.Pathway;
import org.cytoscape.keggscape.internal.generated.Product;
import org.cytoscape.keggscape.internal.generated.Reaction;
import org.cytoscape.keggscape.internal.generated.Relation;
import org.cytoscape.keggscape.internal.generated.Substrate;
import org.cytoscape.keggscape.internal.generated.Subtype;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CySubNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KGMLMapper {

	private static final Logger logger = LoggerFactory.getLogger(KGMLMapper.class);

	// Delimiters
	private static final String NAME_DELIMITER = ", ";
	private static final String ID_DELIMITER = " ";

	// Default values
	private static final String MAP_COLOR = "#89b9cE";
	private static final String TITLE_COLOR = "#f5f5f5";
	private static final String DEF_TEXT_COLOR = "#000000";
	private static final String DEF_FILL_COLOR = "#FFFFFF";

	private static final String EMPTY_COLOR_TAG = "none";

	// For building groups
	private final CyGroupFactory groupFactory;

	private static final Map<String, String> CPD2NAME = new HashMap<String, String>();
	private static final URL CPD_RESOURCE = KGMLMapper.class.getClassLoader().getResource("compoundNames.txt");

	static {
		try {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(CPD_RESOURCE.openStream()));
			String inputLine;
			while ((inputLine = reader.readLine()) != null) {
				String[] columns = inputLine.split("\t");
				String cid = columns[0];
				String cname = columns[1].split("; ")[0];
				CPD2NAME.put(cid, cname);
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Could not read compound name list.", e);
		}
	}

	// Convert relation type to KEGG style edge label.
	private static final Map<String, String> EDGE_TYPE_TO_LABEL = new HashMap<String, String>();

	static {
		EDGE_TYPE_TO_LABEL.put("dissociation", "+");
		EDGE_TYPE_TO_LABEL.put("missing interaction", "/");
		EDGE_TYPE_TO_LABEL.put("phosphorylation", "+p");
		EDGE_TYPE_TO_LABEL.put("dephosphorylation", "-p");
		EDGE_TYPE_TO_LABEL.put("glycosylation", "+g");
		EDGE_TYPE_TO_LABEL.put("ubiquitination", "+u");
		EDGE_TYPE_TO_LABEL.put("methylation", "+m");
		EDGE_TYPE_TO_LABEL.put("expression", "e");
	}

	protected static final Set<String> GLOBAL_MAP_ID = new HashSet<String>();
	static {
		GLOBAL_MAP_ID.add("01100");
		GLOBAL_MAP_ID.add("01110");
	}

	private final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
	private final List<String> maplinkIds = new ArrayList<String>();
	private final Set<String> relationNames = new HashSet<String>();
	private final Map<String, String> reactionColors = new HashMap<String, String>();
	private final Map<String, String> reactionBgColors = new HashMap<String, String>();

	private final Pathway pathway;
	private final CyNetwork network;

	public KGMLMapper(final Pathway pathway, final CyNetwork network, final CyGroupFactory groupFactory) {
		this.pathway = pathway;
		this.network = network;
		this.groupFactory = groupFactory;
	}

	public void doMapping() throws IOException {
		mapNetworkTable(pathway, network);

		// Test columns exists or not
		if (network.getDefaultNodeTable().getColumn(KeggConstants.KEGG_NODE_X) == null) {
			createKeggNodeTable();
			createKeggEdgeTable();
		}

		if (GLOBAL_MAP_ID.contains(pathway.getNumber())) {
			// This is a global pathway. Needs special handling.
			mapGlobalEntries();
			mapGlobalReactions();
		} else {
			mapEntries();
			mapReactions();
			mapRelations();

			// This should be called here since it requires both nodes and
			// edges.
			mapGroups();
			mapExtraRelations();
		}
	}

	private void createKeggEdgeTable() {
		network.getDefaultEdgeTable().createColumn(KeggConstants.KEGG_RELATION_TYPE, String.class, true);
		network.getDefaultEdgeTable().createColumn(KeggConstants.KEGG_REACTION_TYPE, String.class, true);
		network.getDefaultEdgeTable().createColumn(KeggConstants.KEGG_EDGE_COLOR, String.class, true);
		network.getDefaultEdgeTable().createColumn(KeggConstants.KEGG_EDGE_COLOR_BG, String.class, true);
		network.getDefaultEdgeTable().createListColumn(KeggConstants.KEGG_EDGE_SUBTYPES, String.class, true);
		network.getDefaultEdgeTable().createColumn(KeggConstants.KEGG_EDGE_LABEL, String.class, true);
	}

	private void createKeggNodeTable() {
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_X, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_Y, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_WIDTH, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_HEIGHT, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_LABEL, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_LABEL_LIST_FIRST, String.class, true);
		network.getDefaultNodeTable().createListColumn(KeggConstants.KEGG_NODE_LABEL_LIST, String.class, true);
		network.getDefaultNodeTable().createListColumn(KeggConstants.KEGG_ID, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_LABEL_COLOR, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_FILL_COLOR, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_REACTIONID, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_TYPE, String.class, true);
		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_NODE_SHAPE, String.class, true);

		network.getDefaultNodeTable().createColumn(KeggConstants.KEGG_LINK, String.class, true);
	}

	private final String getUniqueName(final Entry entry) {
		return pathway.getName() + ":" + entry.getId();
	}

	private final void basicNodeMapping(final CyRow row, final Entry entry) {
		// Add Pathway ID as prefix.
		row.set(CyNetwork.NAME, getUniqueName(entry));
		row.set(KeggConstants.KEGG_NODE_REACTIONID, entry.getReaction());
		row.set(KeggConstants.KEGG_NODE_TYPE, entry.getType());

		if (entry.getLink() != null) {
			row.set(KeggConstants.KEGG_LINK, entry.getLink());
		}

		mapIdList(entry.getName(), ID_DELIMITER, row, KeggConstants.KEGG_ID);

		final List<Graphics> graphicsList = entry.getGraphics();
		if (graphicsList == null || graphicsList.isEmpty()) {
			// Graphics is not available.
			return;
		}

		final Graphics graphics = graphicsList.get(0);
		if (graphics.getName() != null) {
			mapIdList(graphics.getName(), NAME_DELIMITER, row, KeggConstants.KEGG_NODE_LABEL_LIST);
		}

		final String type = graphics.getType();
		if (type.equals(KEGGTags.LINE.getTag())) {
			lineMapper(entry, row, graphics);
		} else {
			row.set(KeggConstants.KEGG_NODE_X, graphics.getX());
			row.set(KeggConstants.KEGG_NODE_Y, graphics.getY());
			row.set(KeggConstants.KEGG_NODE_WIDTH, graphics.getWidth());
			row.set(KeggConstants.KEGG_NODE_HEIGHT, graphics.getHeight());
			row.set(KeggConstants.KEGG_NODE_LABEL, graphics.getName());
			row.set(KeggConstants.KEGG_NODE_SHAPE, graphics.getType());
		}
	}

	/**
	 * Handle polyline node.
	 * 
	 * @param entry
	 * @param row
	 * @param g
	 */
	private final void lineMapper(final Entry entry, final CyRow row, final Graphics g) {
		final String coords = g.getCoords();

		final String lineColor = g.getFgcolor();
		final String reaction = entry.getReaction();
		if (lineColor != null && reaction != null) {
			reactionColors.put(reaction, lineColor);
		}

		final String[] parts = coords.split(",");

		final List<String[]> coordList = new ArrayList<String[]>();
		int partsLen = parts.length;
		for (int i = 0; i < partsLen; i = i + 2) {
			final String[] tuple = new String[2];
			tuple[0] = parts[i];
			tuple[1] = parts[i + 1];
			coordList.add(tuple);
		}

		String[] first = coordList.get(0);
		String[] last = coordList.get(coordList.size() - 1);

		int x1 = Integer.parseInt(first[0]);
		int y1 = Integer.parseInt(first[1]);
		int x2 = Integer.parseInt(last[0]);
		int y2 = Integer.parseInt(last[1]);
		Integer centerX = null;
		Integer centerY = null;
		if (x1 > x2) {
			centerX = (x1 - x2) / 2 + x2;
		} else {
			centerX = (x2 - x1) / 2 + x1;
		}
		if (y1 > y2) {
			centerY = (y1 - y2) / 2 + y2;
		} else {
			centerY = (y2 - y1) / 2 + y1;
		}

		row.set(KeggConstants.KEGG_NODE_X, centerX.toString());
		row.set(KeggConstants.KEGG_NODE_Y, centerY.toString());
		row.set(KeggConstants.KEGG_NODE_WIDTH, "1");
		row.set(KeggConstants.KEGG_NODE_HEIGHT, "1");
		row.set(KeggConstants.KEGG_NODE_LABEL, g.getName());
		row.set(KeggConstants.KEGG_NODE_SHAPE, "circle");

		// Use label color for nodes
		row.set(KeggConstants.KEGG_NODE_FILL_COLOR, g.getFgcolor());
	}

	/**
	 * Create group nodes.
	 */
	private final void mapGroups() {

		final List<Entry> entries = pathway.getEntry();
		for (final Entry entry : entries) {
			final String entryType = entry.getType();

			// Filter to group node only.
			if (!entryType.equals(GROUP.getTag())) {
				continue;
			}

			final List<Component> components = entry.getComponent();
			if (components.isEmpty()) {
				continue;
			}

			// Extract members
			final StringBuilder builder = new StringBuilder();
			final List<CyNode> nodes = new ArrayList<CyNode>();
			for (final Component component : components) {
				final String id = component.getId();
				final CyNode member = nodeMap.get(id);
				if (member != null) {
					nodes.add(member);
					// Add to label
					String label = network.getRow(member).get(KeggConstants.KEGG_NODE_LABEL_LIST_FIRST, String.class);
					builder.append(label + ", ");
				}
			}

			nodes.add(nodeMap.get(entry.getId()));

			final CyGroup group = groupFactory.createGroup(network, nodes, null, true);
			if (group == null) {
				continue;
			}
			final CyRow groupRow = ((CySubNetwork) network).getRootNetwork().getRow(group.getGroupNode(),
					CyRootNetwork.SHARED_ATTRS);

			String combinedLabel = builder.toString();
			combinedLabel = combinedLabel.substring(0, combinedLabel.length() - 2);

			groupRow.set(CyRootNetwork.SHARED_NAME, getUniqueName(entry));
			groupRow.set(KeggConstants.KEGG_NODE_LABEL_LIST_FIRST, combinedLabel);
			final Graphics graphics = entry.getGraphics().get(0);
			groupRow.set(KeggConstants.KEGG_NODE_X, graphics.getX());
			groupRow.set(KeggConstants.KEGG_NODE_Y, graphics.getY());
			groupRow.set(KeggConstants.KEGG_NODE_WIDTH, graphics.getWidth());
			groupRow.set(KeggConstants.KEGG_NODE_HEIGHT, graphics.getHeight());
			groupRow.set(KeggConstants.KEGG_NODE_LABEL, combinedLabel);
			groupRow.set(KeggConstants.KEGG_NODE_SHAPE, graphics.getType());
			groupRow.set(KeggConstants.KEGG_NODE_FILL_COLOR, graphics.getBgcolor());
			groupRow.set(KeggConstants.KEGG_NODE_TYPE, entryType);

		}
	}

	private void addPlaceholderNode(final Entry entry) {
		// Add placeholder node: This is purely for visualization.

		final Graphics graphics = entry.getGraphics().get(0);
		final CyNode cyNode = network.addNode();
		final CyRow row = network.getRow(cyNode);
		row.set(CyNetwork.NAME, "container");
		row.set(KeggConstants.KEGG_NODE_X, graphics.getX());
		row.set(KeggConstants.KEGG_NODE_Y, graphics.getY());
		row.set(KeggConstants.KEGG_NODE_WIDTH, ((Integer) (Integer.parseInt(graphics.getWidth()) + 2)).toString());
		row.set(KeggConstants.KEGG_NODE_HEIGHT, ((Integer) (Integer.parseInt(graphics.getHeight()) + 2)).toString());
		row.set(KeggConstants.KEGG_NODE_SHAPE, graphics.getType());
		row.set(KeggConstants.KEGG_NODE_FILL_COLOR, graphics.getBgcolor());
		row.set(KeggConstants.KEGG_NODE_TYPE, entry.getType());
		nodeMap.put(entry.getId(), cyNode);
	}

	/**
	 * Map nodes. This includes:
	 * <ul>
	 * <li>gene</li>
	 * <li>map</li>
	 * <li>compound</li>
	 * <li>group</li>
	 * </ul>
	 */
	private final void mapEntries() {
		final List<Entry> entries = pathway.getEntry();
		for (final Entry entry : entries) {
			final String entryType = entry.getType();
			if (entryType == null) {
				logger.warn("Missing Entry Type: " + entry.getId());
				continue;
			}

			// Ignore group for this round.
			if (entryType.equals(GROUP.getTag())) {
				addPlaceholderNode(entry);
				continue;
			}

			if (entryType.equals(MAP.getTag())) {
				maplinkIds.add(entry.getId());
			}

			final CyNode cyNode = network.addNode();
			final CyRow row = network.getRow(cyNode);

			// Map table data
			basicNodeMapping(row, entry);

			// Map Color information
			mapColor(entry, row);

			nodeMap.put(entry.getId(), cyNode);
		}
	}

	private void mapColor(final Entry entry, final CyRow row) {

		// Filter invalid entry
		final List<Graphics> graphicsList = entry.getGraphics();
		if (graphicsList == null || graphicsList.isEmpty()) {
			return;
		}

		// Get first entry ONLY.
		final Graphics graphics = graphicsList.get(0);

		// Set text color column.
		final String fgColor = graphics.getFgcolor();
		if (fgColor == null || fgColor.equals("none")) {
			row.set(KeggConstants.KEGG_NODE_LABEL_COLOR, DEF_TEXT_COLOR);
		} else {
			row.set(KeggConstants.KEGG_NODE_LABEL_COLOR, fgColor);
		}

		String fillColor = graphics.getBgcolor();
		if (fillColor == null || fillColor.equals(EMPTY_COLOR_TAG)) {
			fillColor = DEF_FILL_COLOR;
		}

		final String name = graphics.getName();
		if (name != null && name.startsWith("TITLE")) {
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, TITLE_COLOR);
		} else if (entry.getType().equals(MAP.getTag())) {
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, MAP_COLOR);
		} else if (entry.getType().equals(COMPOUND.getTag())) {
			row.set(KeggConstants.KEGG_NODE_LABEL_LIST_FIRST,
					CPD2NAME.get(row.get(KeggConstants.KEGG_ID, List.class).get(0)));
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, fillColor);
		} else {
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, fillColor);
		}
	}

	/**
	 * Map nodes in Global Map.
	 */
	private final void mapGlobalEntries() {
		final List<Entry> entries = pathway.getEntry();

		for (final Entry entry : entries) {
			if (entry.getType().equals("compound")) {
				final CyNode cyNode = network.addNode();
				final CyRow row = network.getRow(cyNode);
				basicNodeMapping(row, entry);

				final Graphics graphics = entry.getGraphics().get(0);
				if (entry.getType().equals(KEGGTags.MAP.getTag())) {
					updateGlobalMaps(row, entry, graphics);
				} else {
					mapGlobalMapColor(row, entry, graphics);
				}

				if (entry.getType().equals(KEGGTags.COMPOUND.getTag())) {
					row.set(KeggConstants.KEGG_NODE_LABEL_LIST_FIRST,
							CPD2NAME.get(row.get(KeggConstants.KEGG_ID, List.class).get(0)));
				}
				nodeMap.put(entry.getId(), cyNode);
				
				if (entry.getType().equals(KEGGTags.GENE.getTag())) {
					reactionColors.put(entry.getId(), graphics.getFgcolor());
					reactionBgColors.put(entry.getId(), graphics.getBgcolor());
				}
			}
		}
		//System.out.println(reactionColors);
	}

	private final void mapGlobalMapColor(final CyRow row, final Entry entry, final Graphics graphics) {
		String background = graphics.getBgcolor();
		String textColor = graphics.getFgcolor();

		if (textColor.equals(EMPTY_COLOR_TAG) == false) {
			row.set(KeggConstants.KEGG_NODE_LABEL_COLOR, textColor);
		}
		if (background.equals(EMPTY_COLOR_TAG) == false) {
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, background);
		}
	}

	// TODO: is special handling necessary?
	private final void updateGlobalMaps(final CyRow row, final Entry entry, final Graphics graphics) {
		if (Arrays.asList(KeggConstants.lightBlueMap).contains(graphics.getName())) {
			row.set(KeggConstants.KEGG_NODE_LABEL_COLOR, "#99CCFF");
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, "#FFFFFF");
		} else if (Arrays.asList(KeggConstants.lightBrownMap).contains(graphics.getName())) {
			row.set(KeggConstants.KEGG_NODE_LABEL_COLOR, "#DA8E82");
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, "#FFFFFF");
		} else if (Arrays.asList(KeggConstants.blueMap).contains(graphics.getName())) {
			row.set(KeggConstants.KEGG_NODE_LABEL_COLOR, "#8080F7");
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, "#FFFFFF");
		} else if (Arrays.asList(KeggConstants.blueMap).contains(graphics.getName())) {
			row.set(KeggConstants.KEGG_NODE_LABEL_COLOR, "#FFB3CC");
			row.set(KeggConstants.KEGG_NODE_FILL_COLOR, "#FFFFFF");
		} else {
			// Regular map
			mapGlobalMapColor(row, entry, graphics);
		}
	}

	private final void mapRelations() {
		final List<Relation> relations = pathway.getRelation();
		for (final Relation relation : relations) {
			final String relationType = relation.getType();

			if (relationType.equals("maplink")) {
				mapMaplinks(relation);
			} else if (relationType.equals("ECrel")) {
				mapECrel(relation);
			} else {
				final CyNode sourceNode = nodeMap.get(relation.getEntry1());
				final CyNode targetNode = nodeMap.get(relation.getEntry2());
				if (sourceNode == null || targetNode == null) {
					continue;
				} else if (network.getNode(sourceNode.getSUID()) == null
						|| network.getNode(targetNode.getSUID()) == null) {
					continue;
				}

				final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
				mapRelationTableData(newEdge, relation);
			}
		}
	}

	private final void mapECrel(final Relation ecrel) {
		final String gene1 = ecrel.getEntry1();
		final String gene2 = ecrel.getEntry2();
		final CyNode sourceNode = nodeMap.get(gene1);
		final CyNode targetNode = nodeMap.get(gene2);
		if (sourceNode == null || targetNode == null) {
			return;
		} else if (network.getNode(sourceNode.getSUID()) == null || network.getNode(targetNode.getSUID()) == null) {
			return;
		}
		
		final List<Subtype> subtypes = ecrel.getSubtype();
		for(final Subtype sub: subtypes) {
			final String name = sub.getName();
			if(name.equals(KEGGTags.COMPOUND.getTag())) {
				final CyNode compoundNode = nodeMap.get(sub.getValue());
				if (network.getNode(sourceNode.getSUID()) == null ) {
					continue;
				}
				
				final List<CyEdge> existingEdges1 = network.getConnectingEdgeList(sourceNode, compoundNode, CyEdge.Type.ANY);
				if(existingEdges1.isEmpty()) {
					final CyEdge newEdge1 = network.addEdge(sourceNode, compoundNode, true);
					mapRelationTableData(newEdge1, ecrel);
				}
				final List<CyEdge> existingEdges2 = network.getConnectingEdgeList(targetNode, compoundNode, CyEdge.Type.ANY);
				if(existingEdges2.isEmpty()) {
					final CyEdge newEdge2 = network.addEdge(compoundNode,targetNode, true);
					mapRelationTableData(newEdge2, ecrel);
				}
			}
		}
	}

	private final void mapExtraRelations() {
		final List<Relation> relations = pathway.getRelation();
		for (final Relation relation : relations) {
			if (relationNames.contains(getRelationName(relation.getEntry1(), relation.getEntry2(), relation.getType()))
					|| relation.getType().equals("maplink") || relation.getType().equals("ECrel")) {
				continue;
			}

			final CyNode sourceNode = nodeMap.get(relation.getEntry1());
			final CyNode targetNode = nodeMap.get(relation.getEntry2());
			if (sourceNode == null || targetNode == null) {
				continue;
			}
			final CyEdge newEdge = network.addEdge(sourceNode, targetNode, true);
			mapRelationTableData(newEdge, relation);
		}
	}

	private final String getRelationName(String entry1, String entry2, String type) {
		return entry1 + " (" + type + ") " + entry2;
	}

	private final void mapMaplinks(final Relation relation) {
		final List<String> subtypes = new ArrayList<String>();
		subtypes.add(relation.getType());

		for (Subtype subtype : relation.getSubtype()) {
			final CyNode cpdNode = nodeMap.get(subtype.getValue());
			if(cpdNode == null) {
				continue;
			}
			
			CyNode pathwayNode = null;
			if (maplinkIds.contains(relation.getEntry1())) {
				pathwayNode = nodeMap.get(relation.getEntry1());
			} else if (maplinkIds.contains(relation.getEntry2())) {
				pathwayNode = nodeMap.get(relation.getEntry2());
			}
			if (pathwayNode == null) {
				continue;
			}
			if (network.getNode(cpdNode.getSUID()) == null || network.getNode(pathwayNode.getSUID()) == null) {
				continue;
			}
			final List<CyEdge> existingEdges = network.getConnectingEdgeList(cpdNode, pathwayNode, CyEdge.Type.ANY);
			if (existingEdges.isEmpty()) {
				final CyEdge newEdge = network.addEdge(cpdNode, pathwayNode, false);
				network.getRow(newEdge).set(KeggConstants.KEGG_RELATION_TYPE, relation.getType());
				network.getRow(newEdge).set(CyEdge.INTERACTION, relation.getType());
				network.getRow(newEdge).set(KeggConstants.KEGG_EDGE_SUBTYPES, subtypes);
			}
		}
	}

	private void mapRelationTableData(final CyEdge edge, Relation relation) {
		final CyRow row = network.getRow(edge);
		final String type = relation.getType();
		row.set(KeggConstants.KEGG_RELATION_TYPE, type);
		row.set(CyEdge.INTERACTION, type);
		final String relationName = getRelationName(relation.getEntry1(), relation.getEntry2(), type);
		row.set(CyNetwork.NAME, relationName);
		mapSubtypes(relation, row);

		relationNames.add(relationName);
	}

	private final void mapSubtypes(Relation relation, final CyRow row) {
		final List<String> subtypes = new ArrayList<String>();
		final StringBuilder builder = new StringBuilder();
		for (Subtype subtype : relation.getSubtype()) {
			subtypes.add(subtype.getName());
			String label = subtype.getName();
			final String newLabel = EDGE_TYPE_TO_LABEL.get(label);
			if (newLabel != null) {
				builder.append(newLabel + " ");
			}
		}

		row.set(KeggConstants.KEGG_EDGE_SUBTYPES, subtypes);
		row.set(KeggConstants.KEGG_EDGE_LABEL, builder.toString());
	}

	/**
	 * Reaction consists of two edges.
	 */
	private void mapReactions() {
		final List<Reaction> reactions = pathway.getReaction();
		for (final Reaction reaction : reactions) {
			final CyNode reactionNode = nodeMap.get(reaction.getId());
			final List<String> subtypes = new ArrayList<String>();
			subtypes.add(reaction.getType());

			final List<Substrate> substrates = reaction.getSubstrate();
			for (final Substrate substrate : substrates) {
				final CyNode sourceNode = nodeMap.get(substrate.getId());
				final CyEdge newEdge = network.addEdge(sourceNode, reactionNode, true);
				mapReactionTable(network.getRow(newEdge), reaction, subtypes);
				;

			}
			final List<Product> products = reaction.getProduct();
			for (final Product product : products) {
				final CyNode targetNode = nodeMap.get(product.getId());
				final CyEdge newEdge = network.addEdge(reactionNode, targetNode, true);
				mapReactionTable(network.getRow(newEdge), reaction, subtypes);
				;
			}
		}
	}

	private final void mapReactionTable(final CyRow row, Reaction reaction, List<String> subtypes) {
		row.set(KeggConstants.KEGG_REACTION_TYPE, reaction.getType());
		row.set(CyEdge.INTERACTION, reaction.getType());
		row.set(CyNetwork.NAME, reaction.getName());
		row.set(KeggConstants.KEGG_EDGE_SUBTYPES, subtypes);
		final String color = reactionColors.get(reaction.getName());
		if (color != null) {
			row.set(KeggConstants.KEGG_EDGE_COLOR, color);
		}
	}

	private final void mapGlobalReactions() {
		final List<Reaction> reactions = pathway.getReaction();
		
		for (final Reaction reaction : reactions) {
			final List<Substrate> substrates = reaction.getSubstrate();
			final List<Product> products = reaction.getProduct();

			for (final Substrate substrate : substrates) {
				final CyNode substrateNode = nodeMap.get(substrate.getId());
				for (final Product product : products) {
					final CyNode productNode = nodeMap.get(product.getId());
					final CyEdge newEdge = network.addEdge(substrateNode, productNode, true);
					mapReactionEdgeData(newEdge, network.getRow(newEdge), reaction);
				}
			}
		}
	}

	private final void mapReactionEdgeData(final CyEdge edge, final CyRow row, Reaction reaction) {
		final CyNode source = edge.getSource();
		//System.out.println(reactionColors.get(reaction.getId()));
		row.set(KeggConstants.KEGG_EDGE_COLOR,	reactionColors.get(reaction.getId()));
		row.set(KeggConstants.KEGG_EDGE_COLOR_BG, reactionBgColors.get(reaction.getId()));
		row.set(KeggConstants.KEGG_REACTION_TYPE, reaction.getType());
		row.set(CyEdge.INTERACTION, reaction.getType());
		row.set(CyNetwork.NAME, reaction.getName());
	}

	private final void mapIdList(final String idListText, final String delimiter, final CyRow row,
			final String columnName) {
		final List<String> idList = new ArrayList<String>();
		final String[] ids = idListText.split(delimiter);
		for (String id : ids) {
			idList.add(id);
		}
		row.set(columnName, idList);
		if (ids.length != 0 && row.getTable().getColumn(columnName + "_FIRST") != null) {
			row.set(columnName + "_FIRST", ids[0]);
		}
	}

	private final void mapNetworkTable(final Pathway pathway, final CyNetwork network) {
		final String pathwayName = pathway.getName();
		final String linkToKegg = pathway.getLink();
		final String linkToImage = pathway.getImage();
		final String pathwayTitle = pathway.getTitle();
		final String organism = pathway.getOrg();

		final CyRow networkRow = network.getRow(network);

		// Make more usable name
		networkRow.set(CyNetwork.NAME, pathwayTitle + " [" + organism + pathway.getNumber() + "]");

		final CyTable netTable = network.getDefaultNetworkTable();
		if (netTable.getColumn(KeggConstants.KEGG_PATHWAY_ID) == null) {
			netTable.createColumn(KeggConstants.KEGG_PATHWAY_ID, String.class, true);
			netTable.createColumn(KeggConstants.KEGG_PATHWAY_IMAGE, String.class, true);
			netTable.createColumn(KeggConstants.KEGG_PATHWAY_LINK, String.class, true);
			netTable.createColumn(KeggConstants.KEGG_PATHWAY_ORG, String.class, true);
		}

		networkRow.set(KeggConstants.KEGG_PATHWAY_LINK, linkToKegg);
		networkRow.set(KeggConstants.KEGG_PATHWAY_IMAGE, linkToImage);
		networkRow.set(KeggConstants.KEGG_PATHWAY_ID, pathwayName);
		networkRow.set(KeggConstants.KEGG_PATHWAY_ORG, organism);
	}

	protected String getPathwayId() {
		return pathway.getNumber();
	}
}