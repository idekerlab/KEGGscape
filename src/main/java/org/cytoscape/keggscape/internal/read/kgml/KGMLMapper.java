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
import org.cytoscape.group.CyGroupManager;
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
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
import org.cytoscape.view.presentation.property.values.LineType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KGMLMapper {

	private static final Logger logger = LoggerFactory.getLogger(KGMLMapper.class);

	private static final URL CPD_RESOURCE = KGMLMapper.class.getClassLoader().getResource("compoundNames.txt");

	private static final String NAME_DELIMITER = ", ";
	private static final String ID_DELIMITER = " ";

	// Default values
	private static final String MAP_COLOR = "#6999AE";
	private static final String TITLE_COLOR = "#32CCB6";
	private static final String DEF_TEXT_COLOR = "#000000";
	private static final String DEF_FILL_COLOR = "#FFFFFF";

	private final Pathway pathway;
	private String pathwayIdString = null;

	private final CyNetwork network;

	public static final String KEGG_PATHWAY_ID = "KEGG_PATHWAY_ID";
	public static final String KEGG_PATHWAY_IMAGE = "KEGG_PATHWAY_IMAGE";
	public static final String KEGG_PATHWAY_LINK = "KEGG_PATHWAY_LINK";

	public static final String KEGG_NODE_X = "KEGG_NODE_X";
	public static final String KEGG_NODE_Y = "KEGG_NODE_Y";
	public static final String KEGG_NODE_WIDTH = "KEGG_NODE_WIDTH";
	public static final String KEGG_NODE_HEIGHT = "KEGG_NODE_HEIGHT";
	public static final String KEGG_NODE_LABEL = "KEGG_NODE_LABEL";
	public static final String KEGG_NODE_LABEL_LIST_FIRST = "KEGG_NODE_LABEL_LIST_FIRST";
	public static final String KEGG_NODE_LABEL_LIST = "KEGG_NODE_LABEL_LIST";
	public static final String KEGG_ID = "KEGG_ID";
	public static final String KEGG_NODE_LABEL_COLOR = "KEGG_NODE_LABEL_COLOR";
	public static final String KEGG_NODE_FILL_COLOR = "KEGG_NODE_FILL_COLOR";
	public static final String KEGG_NODE_REACTIONID = "KEGG_NODE_REACTIONID";

	public static final String KEGG_NODE_TYPE = "KEGG_NODE_TYPE";
	public static final String KEGG_NODE_SHAPE = "KEGG_NODE_SHAPE";

	public static final String KEGG_RELATION_TYPE = "KEGG_RELATION_TYPE";
	public static final String KEGG_REACTION_TYPE = "KEGG_REACTION_TYPE";
	public static final String KEGG_EDGE_COLOR = "KEGG_EDGE_COLOR";
	public static final String KEGG_EDGE_SUBTYPES = "KEGG_EDGE_SUBTYPES";
	public static final String KEGG_EDGE_LABEL = "KEGG_EDGE_LABEL";

	public static final String KEGG_EXPRESSION = "KEGG_EXPRESSION";
	public static final String KEGG_INDIRECTEFFECT = "KEGG_INDIRECTEFFECT";
	public static final String KEGG_BINDINGASSOCIATION = "KEGG_BINDINGASSOCIATION";
	public static final String KEGG_ACTIVATION = "KEGG_ACTIVATION";
	public static final String KEGG_INHIBITION = "KEGG_INHIBITION";
	public static final String KEGG_PHOSPHORYLATION = "KEGG_PHOSPHORYLATION";
	public static final String KEGG_DEPHOSPHORYLATION = "KEGG_DEPHOSPHORYLATION";

	final String[] lightBlueMap = { "Other types of O-glycan biosynthesis", "Lipopolysaccharide biosynthesis",
			"Glycosaminoglycan biosynthesis - chondroitin sulfate / dermatan sulfate",
			"Glycosphingolipid biosynthesis - ganglio series", "Glycosphingolipid biosynthesis - globo series",
			"Glycosphingolipid biosynthesis - lacto and neolacto series",
			"Glycosylphosphatidylinositol(GPI)-anchor biosynthesis", "Glycosaminoglycan degradation",
			"Various types of N-glycan biosynthesis", "Glycosaminoglycan biosynthesis - keratan sulfate",
			"Mucin type O-Glycan biosynthesis", "N-Glycan biosynthesis",
			"Glycosaminoglycan biosynthesis - heparan sulfate / heparin", "Other glycan degradation" };
	final String[] lightBrownMap = { "Aminobenzoate degradation", "Atrazine degradation", "Benzoate degradation",
			"Bisphenol degradation", "Caprolactam degradation", "Chlorocyclohexane and chlorobenzene degradation",
			"DDT degradation", "Dioxin degradation", "Drug metabolism - cytochrome P450",
			"Drug metabolism - other enzymes", "Ethylbenzene degradation", "Fluorobenzoate degradation",
			"Metabolism of xenobiotics by cytochrome P450", "Naphthalene degradation",
			"Polycyclic aromatic hydrocarbon degradation", "Steroid degradation", "Styrene degradation",
			"Toluene degradation", "Xylene degradation" };
	final String[] blueMap = { "Amino sugar and nucleotide sugar metabolism", "Ascorbate and aldarate metabolism",
			"Pentose and glucuronate interconversions", "Glycolysis / Gluconeogenesis",
			"Inositol phosphate metabolism", "Propanoate metabolism", "Pyruvate metabolism",
			"Glyoxylate and dicarboxylate metabolism", "Citrate cycle (TCA cycle)", "Galactose metabolism",
			"C5-Branched dibasic acid metabolism", "Starch and sucrose metabolism", "Pentose phosphate pathway",
			"Fructose and mannose metabolism" };
	final String[] pinkMap = { "Vitamin B6 metabolism", "One carbon pool by folate", "Riboflavin metabolism",
			"Thiamine metabolism", "Folate biosynthesis", "Nicotinate and nicotinamide metabolism",
			"Porphyrin and chlorophyll metabolism", "Biotin metabolism",
			"Ubiquinone and other terpenoid-quinone biosynthesis", "Pantothenate and CoA biosynthesis" };

	private static Map<String, String> CPD2NAME = new HashMap<String, String>();
	
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
		}
	}
	
	private static final Map<String, String> EDGE_TYPE_TO_LABEL = new HashMap<String, String>();
	
	static {
		EDGE_TYPE_TO_LABEL.put("dissociation","+");
		EDGE_TYPE_TO_LABEL.put("missing interaction","/");
		EDGE_TYPE_TO_LABEL.put("phosphorylation", "+p");
		EDGE_TYPE_TO_LABEL.put("dephosphorylation",	"-p");
		EDGE_TYPE_TO_LABEL.put("glycosylation","+g");
		EDGE_TYPE_TO_LABEL.put("ubiquitination","+u");
		EDGE_TYPE_TO_LABEL.put("methylation","+m");
		EDGE_TYPE_TO_LABEL.put("expression", "e");
	}

	final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();
	final Map<String, String> cpdNameMap = new HashMap<String, String>();
	final List<String> groupnodeIds = new ArrayList<String>();
	final List<String> maplinkIds = new ArrayList<String>();

	private final CyGroupFactory groupFactory;

	public KGMLMapper(final Pathway pathway, final CyNetwork network, final CyGroupFactory groupFactory) {
		this.pathway = pathway;
		this.network = network;
		this.groupFactory = groupFactory;

		mapPathwayMetadata(pathway, network);
	}

	public void doMapping() throws IOException {
		// Test exists or not
		if (network.getDefaultNodeTable().getColumn(KEGG_NODE_X) == null) {
			createKeggNodeTable();
			createKeggEdgeTable();
		}
		if (pathway.getNumber().equals("01100") || pathway.getNumber().equals("01110")) {
			mapGlobalEntries();
			mapGlobalReactions();
		} else {
			mapEntries();
			mapRelations();
			mapReactions();

			// Add group information. This should be the last step since it
			// requires
			// both nodes and edges.
			mapGroups();
		}
	}

	private void createKeggEdgeTable() {
		network.getDefaultEdgeTable().createColumn(KEGG_RELATION_TYPE, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_REACTION_TYPE, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_EDGE_COLOR, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_PHOSPHORYLATION, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_DEPHOSPHORYLATION, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_INHIBITION, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_EXPRESSION, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_INDIRECTEFFECT, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_ACTIVATION, String.class, true);
		network.getDefaultEdgeTable().createColumn(KEGG_BINDINGASSOCIATION, String.class, true);
		
		network.getDefaultEdgeTable().createListColumn(KEGG_EDGE_SUBTYPES, String.class, false);
		network.getDefaultEdgeTable().createColumn(KEGG_EDGE_LABEL, String.class, false);
	}

	private void createKeggNodeTable() {
		network.getDefaultNodeTable().createColumn(KEGG_NODE_X, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_Y, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_WIDTH, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_HEIGHT, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_LABEL, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_LABEL_LIST_FIRST, String.class, true);
		network.getDefaultNodeTable().createListColumn(KEGG_NODE_LABEL_LIST, String.class, true);
		network.getDefaultNodeTable().createListColumn(KEGG_ID, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_LABEL_COLOR, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_FILL_COLOR, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_REACTIONID, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_TYPE, String.class, true);
		network.getDefaultNodeTable().createColumn(KEGG_NODE_SHAPE, String.class, true);
	}

	private final void basicNodeMapping(final CyRow row, final Entry entry) {
		// Add Pathway ID as prefix.
		row.set(CyNetwork.NAME, pathway.getName() + ":" + entry.getId());

		row.set(KEGG_NODE_REACTIONID, entry.getReaction());
		row.set(KEGG_NODE_TYPE, entry.getType());

		mapIdList(entry.getName(), ID_DELIMITER, row, KEGG_ID);

		final List<Graphics> graphicsList = entry.getGraphics();
		if (graphicsList == null || graphicsList.isEmpty()) {
			// Graphics is not available.
			return;
		}

		final Graphics graphics = graphicsList.get(0);
		if (graphics.getName() != null) {
			mapIdList(graphics.getName(), NAME_DELIMITER, row, KEGG_NODE_LABEL_LIST);
		}
		row.set(KEGG_NODE_X, graphics.getX());
		row.set(KEGG_NODE_Y, graphics.getY());
		row.set(KEGG_NODE_WIDTH, graphics.getWidth());
		row.set(KEGG_NODE_HEIGHT, graphics.getHeight());
		row.set(KEGG_NODE_LABEL, graphics.getName());
		row.set(KEGG_NODE_SHAPE, graphics.getType());
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
					String label = network.getRow(member).get(KEGG_NODE_LABEL_LIST_FIRST, String.class);
					builder.append(label + ", ");
				}
			}

			final CyGroup group = groupFactory.createGroup(network, nodes, null, true);
			if (group == null) {
				continue;
			}
			final CyRow groupRow = ((CySubNetwork) network).getRootNetwork().getRow(group.getGroupNode(),
					CyRootNetwork.SHARED_ATTRS);
			
			String combinedLabel = builder.toString();
			combinedLabel = combinedLabel.substring(0,  combinedLabel.length()-2);
			
			groupRow.set(CyRootNetwork.SHARED_NAME, combinedLabel);
			groupRow.set(KEGG_NODE_LABEL_LIST_FIRST, combinedLabel);
			final Graphics graphics = entry.getGraphics().get(0);
			groupRow.set(KEGG_NODE_X, graphics.getX());
			groupRow.set(KEGG_NODE_Y, graphics.getY());
			groupRow.set(KEGG_NODE_WIDTH, graphics.getWidth());
			groupRow.set(KEGG_NODE_HEIGHT, graphics.getHeight());
			groupRow.set(KEGG_NODE_LABEL, combinedLabel);
			groupRow.set(KEGG_NODE_SHAPE, graphics.getType());
			groupRow.set(KEGG_NODE_FILL_COLOR, graphics.getBgcolor());
			groupRow.set(KEGG_NODE_TYPE, entryType);
			nodeMap.put(entry.getId(), group.getGroupNode());
		}
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
			row.set(KEGG_NODE_LABEL_COLOR, DEF_TEXT_COLOR);
		} else {
			row.set(KEGG_NODE_LABEL_COLOR, fgColor);
		}

		String fillColor = graphics.getBgcolor();
		if (fillColor == null || fillColor.equals("none")) {
			fillColor = DEF_FILL_COLOR;
		}

		final String name = graphics.getName();
		if (name != null && name.startsWith("TITLE")) {
			row.set(KEGG_NODE_FILL_COLOR, TITLE_COLOR);
		} else if (entry.getType().equals(MAP.getTag())) {
			row.set(KEGG_NODE_FILL_COLOR, MAP_COLOR);
		} else if (entry.getType().equals(COMPOUND.getTag())) {
			row.set(KEGG_NODE_LABEL_LIST_FIRST, CPD2NAME.get(row.get(KEGG_ID, List.class).get(0)));
			row.set(KEGG_NODE_FILL_COLOR, fillColor);
		} else {
			row.set(KEGG_NODE_FILL_COLOR, fillColor);
		}
	}

	private void mapGlobalEntries() {
		final List<Entry> entries = pathway.getEntry();

		for (final Entry entry : entries) {
			final CyNode cyNode = network.addNode();
			final CyRow row = network.getRow(cyNode);
			basicNodeMapping(row, entry);

			if (entry.getType().equals("map")
					&& Arrays.asList(lightBlueMap).contains(entry.getGraphics().get(0).getName())) {
				row.set(KEGG_NODE_LABEL_COLOR, "#99CCFF");
				row.set(KEGG_NODE_FILL_COLOR, "#FFFFFF");
			} else if (entry.getType().equals("map")
					&& Arrays.asList(lightBrownMap).contains(entry.getGraphics().get(0).getName())) {
				row.set(KEGG_NODE_LABEL_COLOR, "#DA8E82");
				row.set(KEGG_NODE_FILL_COLOR, "#FFFFFF");
			} else if (entry.getType().equals("map")
					&& Arrays.asList(blueMap).contains(entry.getGraphics().get(0).getName())) {
				row.set(KEGG_NODE_LABEL_COLOR, "#8080F7");
				row.set(KEGG_NODE_FILL_COLOR, "#FFFFFF");
			} else if (entry.getType().equals("map")
					&& Arrays.asList(blueMap).contains(entry.getGraphics().get(0).getName())) {
				row.set(KEGG_NODE_LABEL_COLOR, "#FFB3CC");
				row.set(KEGG_NODE_FILL_COLOR, "#FFFFFF");
			} else {
				row.set(KEGG_NODE_LABEL_COLOR, entry.getGraphics().get(0).getFgcolor());
				row.set(KEGG_NODE_FILL_COLOR, entry.getGraphics().get(0).getBgcolor());
				if (entry.getType().equals("compound")) {
					row.set(KEGG_NODE_LABEL_LIST_FIRST, CPD2NAME.get(row.get(KEGG_ID, List.class).get(0)));
				}
			}
			nodeMap.put(entry.getId(), cyNode);
		}
	}

	private final void mapRelations() {
		final List<Relation> relations = pathway.getRelation();
		for (final Relation relation : relations) {
			final String relationType = relation.getType();
			if (relationType.equals("ECrel"))
				continue;

			if (relationType.equals("maplink")) {
				mapMaplinks(relation);
			} else {
				final CyNode sourceNode = nodeMap.get(relation.getEntry1());
				final CyNode targetNode = nodeMap.get(relation.getEntry2());
				if (sourceNode == null || targetNode == null) {
					System.out.println("MISSING!!!!!!!!!!!!!!!!!!!!!");
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
	
	
	private final void mapMaplinks(final Relation relation) {
		final List<String> subtypes = new ArrayList<String>();
		subtypes.add(relation.getType());
		
		for (Subtype subtype : relation.getSubtype()) {
			final CyNode cpdNode = nodeMap.get(subtype.getValue());
			CyNode pathwayNode = null;
			if (maplinkIds.contains(relation.getEntry1())) {
				pathwayNode = nodeMap.get(relation.getEntry1());
			} else if (maplinkIds.contains(relation.getEntry2())) {
				pathwayNode = nodeMap.get(relation.getEntry2());
			}
			if (pathwayNode == null) {
				continue;
			}
			final List<CyEdge> existingEdges = network.getConnectingEdgeList(cpdNode, pathwayNode, CyEdge.Type.ANY);
			if (existingEdges.isEmpty()) {
				final CyEdge newEdge = network.addEdge(cpdNode, pathwayNode, false);
				network.getRow(newEdge).set(KEGG_RELATION_TYPE, relation.getType());
				network.getRow(newEdge).set(CyEdge.INTERACTION, relation.getType());
				network.getRow(newEdge).set(KEGG_EDGE_SUBTYPES, subtypes);
			}
		}
	}
	
	private void mapRelationTableData(final CyEdge edge, Relation relation) {
		final CyRow row = network.getRow(edge);
		final String type = relation.getType();
		row.set(KEGG_RELATION_TYPE, type);
		row.set(CyEdge.INTERACTION, type);
		mapSubtypes(relation, row);
	}
	
	private final void mapSubtypes(Relation relation, final CyRow row) {
		final List<String> subtypes = new ArrayList<String>();
		final StringBuilder builder = new StringBuilder();
		for (Subtype subtype : relation.getSubtype()) {
			subtypes.add(subtype.getName());
			String label = subtype.getName();
			final String newLabel = EDGE_TYPE_TO_LABEL.get(label);
			if(newLabel != null) {
				builder.append(newLabel + " ");
			}
			
			if (subtype.getName().equals("inhibition")) {
				row.set(KEGG_INHIBITION, subtype.getValue());
			} else if (subtype.getName().equals("phosphorylation")) {
				row.set(KEGG_PHOSPHORYLATION, subtype.getValue());
			} else if (subtype.getName().equals("dephosphorylation")) {
				row.set(KEGG_DEPHOSPHORYLATION, subtype.getValue());
			} else if (subtype.getName().equals("indirect effect")) {
				row.set(KEGG_INDIRECTEFFECT, subtype.getValue());
			} else if (subtype.getName().equals("activation")) {
				row.set(KEGG_ACTIVATION, subtype.getValue());
			} else if (subtype.getName().equals("binding/association")) {
				row.set(KEGG_BINDINGASSOCIATION, subtype.getValue());
			} else if (subtype.getName().equals("expression")) {
				row.set(KEGG_EXPRESSION, subtype.getValue());
			}
		}
	
		row.set(KEGG_EDGE_SUBTYPES, subtypes);
		row.set(KEGG_EDGE_LABEL, builder.toString());
	}


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
				network.getRow(newEdge).set(KEGG_REACTION_TYPE, reaction.getType());
				network.getRow(newEdge).set(CyEdge.INTERACTION, reaction.getType());
				network.getRow(newEdge).set(CyNetwork.NAME, reaction.getName());
				network.getRow(newEdge).set(KEGG_EDGE_SUBTYPES, subtypes);
				
			}
			final List<Product> products = reaction.getProduct();
			for (final Product product : products) {
				final CyNode targetNode = nodeMap.get(product.getId());
				final CyEdge newEdge = network.addEdge(reactionNode, targetNode, true);
				network.getRow(newEdge).set(KEGG_REACTION_TYPE, reaction.getType());
				network.getRow(newEdge).set(CyEdge.INTERACTION, reaction.getType());
				network.getRow(newEdge).set(CyNetwork.NAME, reaction.getName());
			}
		}
	}

	private void mapGlobalReactions() {
		final List<Reaction> reactions = pathway.getReaction();
		for (Reaction reaction : reactions) {
			final List<Substrate> substrates = reaction.getSubstrate();
			final List<Product> products = reaction.getProduct();
			for (Substrate substrate : substrates) {
				final CyNode substrateNode = nodeMap.get(substrate.getId());
				for (Product product : products) {
					final CyNode productNode = nodeMap.get(product.getId());
					final CyEdge newEdge = network.addEdge(substrateNode, productNode, true);
					mapReactionEdgeData(newEdge);
				}
			}
		}
	}

	private final void mapReactionEdgeData(CyEdge edge) {
		final CyNode source = edge.getSource();
		network.getRow(edge).set(KEGG_EDGE_COLOR, network.getRow(source).get(KEGG_NODE_FILL_COLOR, String.class));
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

	private final void mapPathwayMetadata(final Pathway pathway, final CyNetwork network) {
		final String pathwayName = pathway.getName();
		final String linkToKegg = pathway.getLink();
		final String linkToImage = pathway.getImage();
		final String pathwayTitle = pathway.getTitle();
		this.pathwayIdString = pathway.getNumber();

		final CyRow networkRow = network.getRow(network);
		networkRow.set(CyNetwork.NAME, pathwayTitle);

		final CyTable netTable = network.getDefaultNetworkTable();
		if (netTable.getColumn(KEGG_PATHWAY_ID) == null) {
			netTable.createColumn(KEGG_PATHWAY_ID, String.class, true);
			netTable.createColumn(KEGG_PATHWAY_IMAGE, String.class, true);
			netTable.createColumn(KEGG_PATHWAY_LINK, String.class, true);
		}

		networkRow.set(KEGG_PATHWAY_LINK, linkToKegg);
		networkRow.set(KEGG_PATHWAY_IMAGE, linkToImage);
		networkRow.set(KEGG_PATHWAY_ID, pathwayName);
	}

	public String getPathwayId() {
		return pathwayIdString;
	}
}