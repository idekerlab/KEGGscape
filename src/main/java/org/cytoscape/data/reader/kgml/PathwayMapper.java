package org.cytoscape.data.reader.kgml;

import giny.view.Justification;
import giny.view.NodeView;
import giny.view.ObjectPosition;
import giny.view.Position;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.transform.Source;

import org.cytoscape.data.reader.kgml.generated.Entry;
import org.cytoscape.data.reader.kgml.generated.Graphics;
import org.cytoscape.data.reader.kgml.generated.Pathway;
import org.cytoscape.data.reader.kgml.generated.Product;
import org.cytoscape.data.reader.kgml.generated.Reaction;
import org.cytoscape.data.reader.kgml.generated.Relation;
import org.cytoscape.data.reader.kgml.generated.Substrate;
import org.cytoscape.data.reader.kgml.generated.Subtype;
import org.cytoscape.kegg.webservice.KEGGRestClient;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.data.Semantics;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.ArrowShape;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.LineStyle;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.PassThroughMapping;
import ding.view.ObjectPositionImpl;

public class PathwayMapper {

	private final Pathway pathway;
	private final String pathwayName;

	private int[] nodeIdx;
	private int[] edgeIdx;

	static final String KEGG_NAME = "KEGG.name";
	private static final String KEGG_NAME_LIST = "KEGG.name.list";
	private static final String KEGG_ENTRY = "KEGG.entry";
	private static final String KEGG_LABEL = "KEGG.label";
	private static final String KEGG_LABEL_LIST = "KEGG.label.list";
	private static final String KEGG_LABEL_LIST_FIRST = "KEGG.label.first";
	private static final String KEGG_RELATION = "KEGG.relation";
	private static final String KEGG_REACTION = "KEGG.reaction";
	private static final String KEGG_REACTION_LIST = "KEGG.reaction.list";
	private static final String KEGG_LINK = "KEGG.link";
	private static final String KEGG_TYPE = "KEGG.type";
	private static final String KEGG_COLOR = "KEGG.color";

	// Special cases: Global Map
	private static final String METABOLIC_PATHWAYS_ENTRY_ID = "01100";
	private static final String BIOSYNTHESIS_OF_SECONDARY_METABOLITES_ENTRY_ID = "01110";
	
	// This is a hack: special cases for global map
	private static final Map<String, String> MISSING_EDGES = new HashMap<String, String>();
	private static final Map<String, String> MISSING_EDGES_COLOR = new HashMap<String, String>();
	static  {
		MISSING_EDGES.put("C00091", "C00026");
		MISSING_EDGES_COLOR.put("C00091-C00026", "#8080F7");
	}

	private static final String REACTION_TYPE_REVERSIBLE = "reversible";
	private static final String REACTION_TYPE_IRREVERSIBLE = "irreversible";

	// Default Values for Visual Style
	private static final Color NODE_DEF_COLOR = Color.WHITE;
	private static final Color EDGE_DEF_COLOR = new Color(50, 50, 50);
	private static final Color NODE_LINE_DEF_COLOR = new Color(20, 20, 20);
	private static final Color NODE_LABEL_DEF_COLOR = new Color(30, 30, 30);

	private static final Color GENE_COLOR = new Color(153, 255, 153);
	private static final Color COMPOUND_COLOR = new Color(0xAAAAEE);
	private static final Color MAP_COLOR = new Color(0x00BFFF);

	private static final Color GLOBAL_DEF_COLOR = new Color(0xAAAAAA);

	private static final ObjectPosition NODE_DEF_LABEL_POSITION = new ObjectPositionImpl(Position.SOUTH_EAST,
			Position.NORTH_WEST, Justification.JUSTIFY_CENTER, 0.0, 0.0);
	private static final ObjectPosition COMPOUND_LABEL_POSITION = new ObjectPositionImpl(Position.SOUTH_EAST,
			Position.NORTH_WEST, Justification.JUSTIFY_CENTER, -30.0, 3.0);

	private static final Font nodeLabelFont = new Font("SansSerif", 7, Font.PLAIN);

	public PathwayMapper(final Pathway pathway) {
		this.pathway = pathway;
		this.pathwayName = pathway.getName();
	}

	public void doMapping() {
		mapNode();
		final List<CyEdge> relationEdges = mapRelationEdge();
		final List<CyEdge> reactionEdges = mapReactionEdge();

		edgeIdx = new int[relationEdges.size() + reactionEdges.size()];
		int idx = 0;

		for (CyEdge edge : reactionEdges) {
			edgeIdx[idx] = edge.getRootGraphIndex();
			idx++;
		}

		for (CyEdge edge : relationEdges) {
			edgeIdx[idx] = edge.getRootGraphIndex();
			idx++;
		}
	}

	private final Map<String, Entry> entryMap = new HashMap<String, Entry>();
	private final Map<String, Entry> edgeEntryMap = new HashMap<String, Entry>();

	final Map<String, CyNode> nodeMap = new HashMap<String, CyNode>();

	final Map<String, CyNode> id2cpdMap = new HashMap<String, CyNode>();
	
	final Map<String, String> cpdName2cpdIDMap = new HashMap<String, String>();
	
	final Map<String, List<Entry>> cpdDataMap = new HashMap<String, List<Entry>>();
	final Map<CyNode, Entry> geneDataMap = new HashMap<CyNode, Entry>();
	private final Map<CyNode, String> entry2reaction = new HashMap<CyNode, String>();

	private void mapNode() {

		final String pathwayID = pathway.getName();
		final String pathway_entryID = pathway.getNumber();
		final List<Entry> components = pathway.getEntry();

		final CyAttributes nodeAttr = Cytoscape.getNodeAttributes();

		Pattern titleNodePattern = Pattern.compile("TITLE:.*");

		for (final Entry comp : components) {

			if (!comp.getType().equals("group")) {

				for (Graphics grap : comp.getGraphics()) {
					if (!titleNodePattern.matcher(grap.getName()).matches()) {
						if (!grap.getType().equals(KEGGShape.LINE.getTag())) {
							CyNode node = Cytoscape.getCyNode(pathwayID + "-" + comp.getId(), true);
							nodeAttr.setAttribute(node.getIdentifier(), KEGG_NAME, comp.getName());
							nodeAttr.setListAttribute(node.getIdentifier(), KEGG_NAME_LIST,
									java.util.Arrays.asList(comp.getName().split(" ")));
							if (comp.getLink() != null)
								nodeAttr.setAttribute(node.getIdentifier(), KEGG_LINK, comp.getLink());
							nodeAttr.setAttribute(node.getIdentifier(), KEGG_ENTRY, comp.getType());

							final String reaction = comp.getReaction();

							// Save reaction
							if (reaction != null) {
								entry2reaction.put(node, reaction);
								nodeAttr.setAttribute(node.getIdentifier(), KEGG_REACTION, reaction);
								nodeAttr.setListAttribute(node.getIdentifier(), KEGG_REACTION_LIST,
										java.util.Arrays.asList(reaction.split(" ")));
							}

							// final Graphics graphics = comp.getGraphics();
							if (grap != null && grap.getName() != null) {
								nodeAttr.setAttribute(node.getIdentifier(), KEGG_LABEL, grap.getName());
								final String[] labels = grap.getName().split(", ");
								final List<String> labelList = Arrays.asList(labels);
								nodeAttr.setListAttribute(node.getIdentifier(), KEGG_LABEL_LIST, labelList);
								nodeAttr.setAttribute(node.getIdentifier(), KEGG_LABEL_LIST_FIRST, labelList.get(0));
								if (pathway_entryID.equals(METABOLIC_PATHWAYS_ENTRY_ID)
										|| pathway_entryID.equals(BIOSYNTHESIS_OF_SECONDARY_METABOLITES_ENTRY_ID)) {
									nodeAttr.setAttribute(node.getIdentifier(), KEGG_COLOR, grap.getBgcolor());
								}
							}

							nodeMap.put(comp.getId(), node);
							entryMap.put(comp.getId(), comp);

							if (comp.getType().equals(KEGGEntryType.COMPOUND.getTag())) {
								id2cpdMap.put(comp.getId(), node);
								cpdName2cpdIDMap.put(comp.getName().split(":")[1], comp.getId());
								
								List<Entry> current = cpdDataMap.get(comp.getName());

								if (current != null) {
									current.add(comp);
								} else {
									current = new ArrayList<Entry>();
									current.add(comp);
								}
								cpdDataMap.put(comp.getName(), current);
							} else if (comp.getType().equals(KEGGEntryType.GENE.getTag())
									|| comp.getType().equals(KEGGEntryType.ORTHOLOG.getTag())) {
								geneDataMap.put(node, comp);
							}
						}
						// If the pathway is "global metabolism map", put the
						// entry
						// to entryMap even in "line" graphics.
						if (pathway_entryID.equals(METABOLIC_PATHWAYS_ENTRY_ID)
								|| pathway_entryID.equals(BIOSYNTHESIS_OF_SECONDARY_METABOLITES_ENTRY_ID)) {
							if (grap.getType().equals(KEGGShape.LINE.getTag())) {
								edgeEntryMap.put(comp.getId(), comp);
							}
						}
					}
				}

			}

		}

		nodeIdx = new int[nodeMap.values().size()];
		int idx = 0;
		for (CyNode node : nodeMap.values()) {
			nodeIdx[idx] = node.getRootGraphIndex();
			idx++;
		}
	}

	private List<CyEdge> mapRelationEdge() {

		final List<Relation> relations = pathway.getRelation();
		final List<CyEdge> edges = new ArrayList<CyEdge>();

		final CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();

		for (Relation rel : relations) {

			final String type = rel.getType();

			if (rel.getType().equals(KEGGRelationType.MAPLINK.getTag())) {
				final List<Subtype> subs = rel.getSubtype();

				if (entryMap.containsKey(rel.getEntry1()) && entryMap.containsKey(rel.getEntry2())) {

					if (entryMap.get(rel.getEntry1()).getType().equals(KEGGEntryType.MAP.getTag())) {
						CyNode maplinkNode = nodeMap.get(rel.getEntry1());

						for (Subtype sub : subs) {
							CyNode cpdNode = nodeMap.get(sub.getValue());

							// System.out.println(maplinkNode.getIdentifier());
							// System.out.println(cpdNode.getIdentifier() +
							// "\n\n");

							CyEdge edge2 = Cytoscape.getCyEdge(maplinkNode, cpdNode, Semantics.INTERACTION, type, true,
									true);
							edges.add(edge2);
							edgeAttr.setAttribute(edge2.getIdentifier(), KEGG_RELATION, type);
						}
					} else {
						CyNode maplinkNode = nodeMap.get(rel.getEntry2());

						for (Subtype sub : subs) {
							CyNode cpdNode = nodeMap.get(sub.getValue());

							// System.out.println(maplinkNode.getIdentifier());
							// System.out.println(cpdNode.getIdentifier() +
							// "\n\n");

							CyEdge edge2 = Cytoscape.getCyEdge(maplinkNode, cpdNode, Semantics.INTERACTION, type, true,
									true);
							edges.add(edge2);
							edgeAttr.setAttribute(edge2.getIdentifier(), KEGG_RELATION, type);
						}
					}

				}

			}
		}

		return edges;

	}
	
	private List<CyEdge> fetchReaction(final String reactionIDs) {
		final List<CyEdge> edges = new ArrayList<CyEdge>();
		
		String[] reactionIDArray = reactionIDs.split(" ");
		if(reactionIDArray.length == 0)
			return edges;
		
		// Use only the first ID:
		String reactionID = reactionIDArray[0].split(":")[1];
		try {
			final Map<List<String>, List<String>> response = KEGGRestClient.getCleint().getReactions(reactionID);
			final List<String> left = response.keySet().iterator().next();
			final List<String> right = response.values().iterator().next();
			
			for(String compoundID: left) {
				System.out.println("### Compound ID = " + compoundID);
				String nodeID = cpdName2cpdIDMap.get(compoundID);
				if(nodeID == null)
					continue;
				
				final CyNode node = id2cpdMap.get(nodeID);
				if (node != null) {
					for (String compoundIDRight : right) {
						String targetNodeID = cpdName2cpdIDMap.get(compoundIDRight);
						
						if(targetNodeID == null)
							continue;
						final CyNode target = id2cpdMap.get(targetNodeID);
						CyEdge edge = Cytoscape.getCyEdge(node, target, Semantics.INTERACTION, "cc", true);
						
						edges.add(edge);
						System.out.println("### Compound ID R = " + compoundIDRight);
					}
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return edges;
	}

	private List<CyEdge> mapReactionEdge() {

		final String pathway_entryID = pathway.getNumber();
		final List<Reaction> reactions = pathway.getReaction();
		final List<CyEdge> edges = new ArrayList<CyEdge>();

		CyAttributes edgeAttr = Cytoscape.getEdgeAttributes();

		// Special case: global map
		if (pathway_entryID.equals(METABOLIC_PATHWAYS_ENTRY_ID)
				|| pathway_entryID.equals(BIOSYNTHESIS_OF_SECONDARY_METABOLITES_ENTRY_ID)) {
			
			// Check missing edge
			for (String key : MISSING_EDGES.keySet()) {
				String source = cpdName2cpdIDMap.get(key);
				String target = cpdName2cpdIDMap.get(MISSING_EDGES.get(key));
				if (source != null && target != null) {
					final CyEdge edge = Cytoscape.getCyEdge(id2cpdMap.get(source), id2cpdMap.get(target), Semantics.INTERACTION, "cc", true);
					final String colorKeyString = key + "-" + MISSING_EDGES.get(key);
					edgeAttr.setAttribute(edge.getIdentifier(), KEGG_COLOR, MISSING_EDGES_COLOR.get(colorKeyString));
					edges.add(edge);
				}
			}
			
			for (Reaction rea : reactions) {
				final List<Product> products = rea.getProduct();
				final List<Substrate> substrates = rea.getSubstrate();
				
				if(products.size()==0 && substrates.size()==0)
					continue;
				
				Entry rea_entry = edgeEntryMap.get(rea.getId());
				for (Graphics grap : rea_entry.getGraphics()) {
					for (Substrate sub : rea.getSubstrate()) {
						CyNode subNode = nodeMap.get(sub.getId());
						for (Product pro : rea.getProduct()) {
							CyNode proNode = nodeMap.get(pro.getId());
							CyEdge edge = Cytoscape.getCyEdge(subNode, proNode, Semantics.INTERACTION, "cc", true);
							edges.add(edge);
							edgeAttr.setAttribute(edge.getIdentifier(), KEGG_NAME, rea_entry.getName());
							edgeAttr.setAttribute(edge.getIdentifier(), KEGG_REACTION, rea_entry.getReaction());
							edgeAttr.setAttribute(edge.getIdentifier(), KEGG_TYPE, rea_entry.getType());
							if (rea_entry.getLink() != null) {
								edgeAttr.setAttribute(edge.getIdentifier(), KEGG_LINK, rea_entry.getLink());
							}
							edgeAttr.setAttribute(edge.getIdentifier(), KEGG_COLOR, grap.getFgcolor());
						}
					}
				}
			}
		} else {
			for (Reaction rea : reactions) {
				CyNode reaNode = nodeMap.get(rea.getId());
				// System.out.println(rea.getId());
				// System.out.println(reaNode.getIdentifier());

				if (rea.getType().equals(REACTION_TYPE_IRREVERSIBLE)) {
					for (Substrate sub : rea.getSubstrate()) {
						CyNode subNode = nodeMap.get(sub.getId());
						CyEdge edge = Cytoscape.getCyEdge(subNode, reaNode, Semantics.INTERACTION, "cr", true, true);
						edges.add(edge);
						edgeAttr.setAttribute(edge.getIdentifier(), KEGG_NAME, rea.getName());
						edgeAttr.setAttribute(edge.getIdentifier(), KEGG_REACTION, rea.getType());
					}
					for (Product pro : rea.getProduct()) {
						CyNode proNode = nodeMap.get(pro.getId());
						CyEdge edge = Cytoscape.getCyEdge(reaNode, proNode, Semantics.INTERACTION, "rc", true, true);
						edges.add(edge);
						edgeAttr.setAttribute(edge.getIdentifier(), KEGG_NAME, rea.getName());
						edgeAttr.setAttribute(edge.getIdentifier(), KEGG_REACTION, rea.getType());
					}

				} else if (rea.getType().equals(REACTION_TYPE_REVERSIBLE)) {
					for (Substrate sub : rea.getSubstrate()) {
						// System.out.println(sub.getId());
						CyNode subNode = nodeMap.get(sub.getId());
						// System.out.println(subNode.getIdentifier());

						CyEdge proEdge = Cytoscape.getCyEdge(reaNode, subNode, Semantics.INTERACTION, "rc", true, true);
						edges.add(proEdge);
						edgeAttr.setAttribute(proEdge.getIdentifier(), KEGG_NAME, rea.getName());
						edgeAttr.setAttribute(proEdge.getIdentifier(), KEGG_REACTION, rea.getType());
					}
					for (Product pro : rea.getProduct()) {
						CyNode proNode = nodeMap.get(pro.getId());

						CyEdge proEdge = Cytoscape.getCyEdge(reaNode, proNode, Semantics.INTERACTION, "rc", true, true);
						edges.add(proEdge);
						edgeAttr.setAttribute(proEdge.getIdentifier(), KEGG_NAME, rea.getName());
						edgeAttr.setAttribute(proEdge.getIdentifier(), KEGG_REACTION, rea.getType());
					}
				}
			}
		}
		return edges;
	}

	protected void updateView(final CyNetwork network) {

		final String vsName = "KEGG: " + network.getTitle() + " (" + pathwayName + ")";
		final CyNetworkView view = Cytoscape.getNetworkView(network.getIdentifier());
		final CyAttributes nodeAttr = Cytoscape.getNodeAttributes();

		if (Cytoscape.getVisualMappingManager().getCalculatorCatalog().getVisualStyleNames().contains(vsName)) {
			// Visual Style already exists.
			// Apply it and exit.

			final VisualStyle targetStyle = Cytoscape.getVisualMappingManager().getCalculatorCatalog()
					.getVisualStyle(vsName);
			Cytoscape.getVisualMappingManager().setVisualStyle(targetStyle);
			view.setVisualStyle(targetStyle.getName());

			Cytoscape.getVisualMappingManager().setNetworkView(view);
			view.redrawGraph(false, true);
			return;
		}

		final VisualStyle defStyle = new VisualStyle(vsName);
		final String pathway_entryID = pathway.getNumber();

		NodeAppearanceCalculator nac = defStyle.getNodeAppearanceCalculator();
		EdgeAppearanceCalculator eac = defStyle.getEdgeAppearanceCalculator();
		GlobalAppearanceCalculator gac = defStyle.getGlobalAppearanceCalculator();
		gac.setDefaultBackgroundColor(Color.white);

		// Node Label Mapping
		final PassThroughMapping m = new PassThroughMapping("", KEGG_LABEL_LIST_FIRST);
		final Calculator nodeLabelMappingCalc = new BasicCalculator(vsName + "-" + "NodeLabelMapping", m,
				VisualPropertyType.NODE_LABEL);
		nac.setCalculator(nodeLabelMappingCalc);

		nac.setNodeSizeLocked(false);

		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FILL_COLOR, NODE_DEF_COLOR);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_SHAPE, NodeShape.ROUND_RECT);

		nac.getDefaultAppearance().set(VisualPropertyType.NODE_BORDER_COLOR, NODE_LINE_DEF_COLOR);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_LINE_WIDTH, 0);

		nac.getDefaultAppearance().set(VisualPropertyType.NODE_LABEL_COLOR, NODE_LABEL_DEF_COLOR);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FONT_FACE, nodeLabelFont);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_FONT_SIZE, 6);
		nac.getDefaultAppearance().set(VisualPropertyType.NODE_LABEL_WIDTH, 15);

		// Default Edge appr
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_TGTARROW_SHAPE, ArrowShape.DELTA);
		eac.getDefaultAppearance().set(VisualPropertyType.EDGE_TGTARROW_OPACITY, 50);
		final DiscreteMapping edgeLineStyle = new DiscreteMapping(LineStyle.SOLID, KEGG_RELATION,
				ObjectMapping.EDGE_MAPPING);
		final Calculator edgeLineStyleCalc = new BasicCalculator(vsName + "-" + "EdgeLineStyleMapping", edgeLineStyle,
				VisualPropertyType.EDGE_LINE_STYLE);
		edgeLineStyle.putMapValue(KEGGRelationType.MAPLINK.getTag(), LineStyle.LONG_DASH);
		eac.setCalculator(edgeLineStyleCalc);

		final DiscreteMapping edgeTgtarrowShape = new DiscreteMapping(ArrowShape.DELTA, Semantics.INTERACTION,
				ObjectMapping.EDGE_MAPPING);
		final Calculator edgeTgtarrowShapeCalc = new BasicCalculator(vsName + "-" + "EdgeTgtarrowStyleMapping",
				edgeTgtarrowShape, VisualPropertyType.EDGE_TGTARROW_SHAPE);

		edgeTgtarrowShape.putMapValue("cr", ArrowShape.NONE);
		edgeTgtarrowShape.putMapValue("maplink", ArrowShape.NONE);
		if (pathway_entryID.equals(METABOLIC_PATHWAYS_ENTRY_ID)
				|| pathway_entryID.equals(BIOSYNTHESIS_OF_SECONDARY_METABOLITES_ENTRY_ID)) {
			edgeTgtarrowShape.putMapValue("cc", ArrowShape.NONE);
		}

		eac.setCalculator(edgeTgtarrowShapeCalc);

		// Node Shape Mapping
		final DiscreteMapping nodeShape = new DiscreteMapping(NodeShape.RECT, KEGG_ENTRY, ObjectMapping.NODE_MAPPING);
		final Calculator nodeShapeCalc = new BasicCalculator(vsName + "-" + "NodeShapeMapping", nodeShape,
				VisualPropertyType.NODE_SHAPE);
		nodeShape.putMapValue(KEGGEntryType.MAP.getTag(), NodeShape.ROUND_RECT);
		nodeShape.putMapValue(KEGGEntryType.GENE.getTag(), NodeShape.RECT);
		nodeShape.putMapValue(KEGGEntryType.ORTHOLOG.getTag(), NodeShape.RECT);
		nodeShape.putMapValue(KEGGEntryType.COMPOUND.getTag(), NodeShape.ELLIPSE);
		nac.setCalculator(nodeShapeCalc);

		// Node Border Mapping
		final DiscreteMapping nodeLineWidth = new DiscreteMapping(1, KEGG_ENTRY, ObjectMapping.NODE_MAPPING);
		final Calculator nodeLineWidthCalc = new BasicCalculator(vsName + "-" + "NodeBorderWidthMapping",
				nodeLineWidth, VisualPropertyType.NODE_LINE_WIDTH);
		nodeLineWidth.putMapValue(KEGGEntryType.MAP.getTag(), 0);
		nodeLineWidth.putMapValue(KEGGEntryType.GENE.getTag(), 2);
		nodeLineWidth.putMapValue(KEGGEntryType.ORTHOLOG.getTag(), 2);
		nodeLineWidth.putMapValue(KEGGEntryType.COMPOUND.getTag(), 0);
		nac.setCalculator(nodeLineWidthCalc);

		// Node Opacity Mapping
		final DiscreteMapping nodeOpacity = new DiscreteMapping(255, KEGG_ENTRY, ObjectMapping.NODE_MAPPING);
		final Calculator nodeOpacityCalc = new BasicCalculator(vsName + "-" + "NodeOpacityMapping", nodeOpacity,
				VisualPropertyType.NODE_OPACITY);
		nodeOpacity.putMapValue(KEGGEntryType.MAP.getTag(), 60);
		nodeOpacity.putMapValue(KEGGEntryType.COMPOUND.getTag(), 200);
		nac.setCalculator(nodeOpacityCalc);

		// Special Case: Global Map (Metabolic Pathway)
		if (pathway_entryID.equals(METABOLIC_PATHWAYS_ENTRY_ID)
				|| pathway_entryID.equals(BIOSYNTHESIS_OF_SECONDARY_METABOLITES_ENTRY_ID)) {

			// Set edge opacity
			eac.getDefaultAppearance().set(VisualPropertyType.EDGE_OPACITY, 220);
			nac.getDefaultAppearance().set(VisualPropertyType.NODE_OPACITY, 90);

			final DiscreteMapping nodeColorMap = new DiscreteMapping(NODE_DEF_COLOR, KEGG_COLOR,
					ObjectMapping.NODE_MAPPING);
			final Calculator nodeColorCalc = new BasicCalculator(vsName + "-" + "NodeColorMapping", nodeColorMap,
					VisualPropertyType.NODE_FILL_COLOR);

			final DiscreteMapping edgeColorMap = new DiscreteMapping(EDGE_DEF_COLOR, KEGG_COLOR,
					ObjectMapping.EDGE_MAPPING);
			final Calculator edgeColorCalc = new BasicCalculator(vsName + "-" + "EdgeColorMapping", edgeColorMap,
					VisualPropertyType.EDGE_COLOR);

			for (String key : nodeMap.keySet()) {
				for (Graphics nodeGraphics : entryMap.get(key).getGraphics()) {
					if (!nodeGraphics.getBgcolor().equals("none")) {
						Color c = Color.decode(nodeGraphics.getBgcolor());
						nodeColorMap.putMapValue(nodeGraphics.getBgcolor(), c);
						edgeColorMap.putMapValue(nodeGraphics.getBgcolor(), c);
					}
				}
			}

			nodeColorMap.putMapValue("none", GLOBAL_DEF_COLOR);

			final DiscreteMapping edgeWidthMap = new DiscreteMapping(3, Semantics.INTERACTION,
					ObjectMapping.EDGE_MAPPING);
			final Calculator edgeWidthCalc = new BasicCalculator(vsName + "-" + "EdgeWidthMapping", edgeWidthMap,
					VisualPropertyType.EDGE_LINE_WIDTH);
			edgeWidthMap.putMapValue("cc", 7);

			nac.setCalculator(nodeColorCalc);
			eac.setCalculator(edgeColorCalc);
			eac.setCalculator(edgeWidthCalc);

		} else {
			final DiscreteMapping nodeColorMap = new DiscreteMapping(NODE_DEF_COLOR, KEGG_ENTRY,
					ObjectMapping.NODE_MAPPING);
			final Calculator nodeColorCalc = new BasicCalculator(vsName + "-" + "NodeColorMapping", nodeColorMap,
					VisualPropertyType.NODE_FILL_COLOR);
			nodeColorMap.putMapValue(KEGGEntryType.GENE.getTag(), GENE_COLOR);
			nodeColorMap.putMapValue(KEGGEntryType.COMPOUND.getTag(), COMPOUND_COLOR);
			nodeColorMap.putMapValue(KEGGEntryType.MAP.getTag(), MAP_COLOR);

			final DiscreteMapping nodeLabelPositionMap = new DiscreteMapping(NODE_DEF_LABEL_POSITION, KEGG_ENTRY,
					ObjectMapping.NODE_MAPPING);
			final Calculator nodeLabelPositionCalc = new BasicCalculator(vsName + "-" + "NodeLabelPositionMapping",
					nodeLabelPositionMap, VisualPropertyType.NODE_LABEL_POSITION);
			nodeLabelPositionMap.putMapValue(KEGGEntryType.COMPOUND.getTag(), COMPOUND_LABEL_POSITION);

			nac.setCalculator(nodeColorCalc);
			nac.setCalculator(nodeLabelPositionCalc);
		}

		final DiscreteMapping nodeWidth = new DiscreteMapping(30, "ID", ObjectMapping.NODE_MAPPING);
		final Calculator nodeWidthCalc = new BasicCalculator(vsName + "-" + "NodeWidthMapping", nodeWidth,
				VisualPropertyType.NODE_WIDTH);
		final DiscreteMapping nodeHeight = new DiscreteMapping(30, "ID", ObjectMapping.NODE_MAPPING);
		final Calculator nodeHeightCalc = new BasicCalculator(vsName + "-" + "NodeHeightMapping", nodeHeight,
				VisualPropertyType.NODE_HEIGHT);

		nac.setCalculator(nodeHeightCalc);
		nac.setCalculator(nodeWidthCalc);

		for (String key : nodeMap.keySet()) {

			for (Graphics nodeGraphics : entryMap.get(key).getGraphics()) {
				if (KEGGShape.getShape(nodeGraphics.getType()) != -1) {
					final String nodeID = nodeMap.get(key).getIdentifier();

					// System.out.println(nodeID);
					// System.out.println(key);
					// System.out.println(nodeMap.size());
					// System.out.println(nodeMap.get(key).getIdentifier());
					// System.out.println(nodeMap.get(key).toString());
					// System.out.println(view.toString());

					final NodeView nv = view.getNodeView(nodeMap.get(key));
					if (nv == null)
						continue;

					nv.setXPosition(Double.parseDouble(nodeGraphics.getX()));
					nv.setYPosition(Double.parseDouble(nodeGraphics.getY()));

					final double w = Double.parseDouble(nodeGraphics.getWidth());
					nodeAttr.setAttribute(nodeID, "KEGG.nodeWidth", w);

					nodeWidth.putMapValue(nodeID, w);

					final double h = Double.parseDouble(nodeGraphics.getHeight());
					nodeAttr.setAttribute(nodeID, "KEGG.nodeHeight", h);

					nodeHeight.putMapValue(nodeID, h);

					nv.setShape(KEGGShape.getShape(nodeGraphics.getType()));
				}
			}
		}

		Cytoscape.getVisualMappingManager().getCalculatorCatalog().addVisualStyle(defStyle);
		Cytoscape.getVisualMappingManager().setVisualStyle(defStyle);
		view.setVisualStyle(defStyle.getName());

		Cytoscape.getVisualMappingManager().setNetworkView(view);
		view.redrawGraph(false, true);

	}

	public int[] getNodeIdx() {
		return nodeIdx;
	}

	public int[] getEdgeIdx() {
		return edgeIdx;
	}

}
