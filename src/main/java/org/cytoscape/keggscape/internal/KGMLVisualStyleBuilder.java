package org.cytoscape.keggscape.internal;

import java.awt.Color;
import java.awt.Paint;
import java.util.Set;

import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.LineType;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;

public class KGMLVisualStyleBuilder {
	
	// Default visual style name
	public static final String DEF_VS_NAME = "KEGG Style";
	public static final String GLOBAL_VS_NAME = "KEGG Global Map Style";
	
	private final VisualStyleFactory vsFactory;
	private final VisualMappingFunctionFactory discreteMappingFactory;
	private final VisualMappingFunctionFactory passthroughMappingFactory;
	
	private static final String KEGG_NODE_X = "KEGG_NODE_X";
	private static final String KEGG_NODE_Y = "KEGG_NODE_Y";
	private static final String KEGG_NODE_WIDTH = "KEGG_NODE_WIDTH";
	private static final String KEGG_NODE_HEIGHT = "KEGG_NODE_HEIGHT";
	private static final String KEGG_NODE_LABEL = "KEGG_NODE_LABEL";
	private static final String KEGG_NODE_LABEL_COLOR = "KEGG_NODE_LABEL_COLOR";
	private static final String KEGG_NODE_FILL_COLOR = "KEGG_NODE_FILL_COLOR";
	private static final String KEGG_NODE_LABEL_LIST_FIRST = "KEGG_NODE_LABEL_LIST_FIRST";
	private static final String KEGG_NODE_SHAPE = "KEGG_NODE_SHAPE";

	private static final String KEGG_RELATION_TYPE = "KEGG_RELATION_TYPE";
	private static final String KEGG_NODE_TYPE = "KEGG_NODE_TYPE";

	private static final String KEGG_EDGE_COLOR = "KEGG_EDGE_COLOR";

	public KGMLVisualStyleBuilder(final VisualStyleFactory vsFactory,
			final VisualMappingFunctionFactory discreteMappingFactory,
			final VisualMappingFunctionFactory passthroughMappingFactory) {
		this.vsFactory = vsFactory;
		this.discreteMappingFactory = discreteMappingFactory;
		this.passthroughMappingFactory = passthroughMappingFactory;
	} 
	
	public VisualStyle getVisualStyle() {
		final VisualStyle defStyle = vsFactory.createVisualStyle(DEF_VS_NAME);
		final Set<VisualPropertyDependency<?>> deps = defStyle.getAllVisualPropertyDependencies();
		
		// handle locked values
		for (VisualPropertyDependency<?> dep : deps) {
			if (dep.getIdString().equals("nodeSizeLocked")) {
				if (dep.isDependencyEnabled()) {
					dep.setDependency(false);
				}
			}
			if (dep.getIdString().equals("arrowColorMatchesEdge")) {
				dep.setDependency(true);
			}
		}
		
		createDefaults(defStyle);
		
		final PassthroughMapping<String, Double> nodexPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_NODE_X, String.class, BasicVisualLexicon.NODE_X_LOCATION);
		final PassthroughMapping<String, Double> nodeyPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_NODE_Y, String.class, BasicVisualLexicon.NODE_Y_LOCATION);
		final PassthroughMapping<String, Double> nodewidthPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_NODE_WIDTH, String.class, BasicVisualLexicon.NODE_WIDTH);
		final PassthroughMapping<String, Double> nodeheightPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_NODE_HEIGHT, String.class, BasicVisualLexicon.NODE_HEIGHT);
		final PassthroughMapping<String, String> nodelabelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_NODE_LABEL_LIST_FIRST, String.class, BasicVisualLexicon.NODE_LABEL);
		final PassthroughMapping<String, Paint> nodelabelcolorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_NODE_LABEL_COLOR, String.class, BasicVisualLexicon.NODE_LABEL_COLOR);
		final PassthroughMapping<String, Paint> nodefillcolorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_NODE_FILL_COLOR, String.class, BasicVisualLexicon.NODE_FILL_COLOR);
		final PassthroughMapping<String, String> nodeTooltipPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_NODE_LABEL, String.class, BasicVisualLexicon.NODE_TOOLTIP);
				
		
		defStyle.addVisualMappingFunction(nodexPassthrough);
		defStyle.addVisualMappingFunction(nodeyPassthrough);
		defStyle.addVisualMappingFunction(nodewidthPassthrough);
		defStyle.addVisualMappingFunction(nodeheightPassthrough);
		defStyle.addVisualMappingFunction(nodelabelPassthrough);
		defStyle.addVisualMappingFunction(nodeTooltipPassthrough);
		defStyle.addVisualMappingFunction(nodelabelcolorPassthrough);
		defStyle.addVisualMappingFunction(nodefillcolorPassthrough);
		

		final DiscreteMapping<String, LineType> edgelinetypeMapping = (DiscreteMapping<String, LineType>) discreteMappingFactory
				.createVisualMappingFunction(KEGG_RELATION_TYPE, String.class, BasicVisualLexicon.EDGE_LINE_TYPE);
		edgelinetypeMapping.putMapValue("maplink", LineTypeVisualProperty.LONG_DASH);
		
		final DiscreteMapping<String, NodeShape> nodetypeMapping = (DiscreteMapping<String, NodeShape>) discreteMappingFactory
				.createVisualMappingFunction(KEGG_NODE_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
		nodetypeMapping.putMapValue("ortholog", NodeShapeVisualProperty.RECTANGLE);
		nodetypeMapping.putMapValue("gene", NodeShapeVisualProperty.RECTANGLE);
		nodetypeMapping.putMapValue("map", NodeShapeVisualProperty.ROUND_RECTANGLE);
		nodetypeMapping.putMapValue("compound", NodeShapeVisualProperty.ELLIPSE);
		
		defStyle.addVisualMappingFunction(edgelinetypeMapping);
		defStyle.addVisualMappingFunction(nodetypeMapping);
		
		return defStyle;
	}

	private final void createDefaults(final VisualStyle style) {
		// Defaults for nodes
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 7);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 2d);
		style.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 200);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 220);
		
		// Defaults for Edges
		style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 1d);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 6);
		style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.ARROW);
		style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.GRAY);
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.GRAY);
	}

	public VisualStyle getGlobalVisualStyle() {
		final VisualStyle originalStyle = this.getVisualStyle();
		originalStyle.setTitle(GLOBAL_VS_NAME);

		final PassthroughMapping<String, Paint> edgeColorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_EDGE_COLOR, String.class, BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		final PassthroughMapping<String, Paint> edgeStrokeColorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KEGG_EDGE_COLOR, String.class, BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
		originalStyle.addVisualMappingFunction(edgeColorPassthrough);
		originalStyle.addVisualMappingFunction(edgeStrokeColorPassthrough);

		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 180);
		originalStyle.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 180);
		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 0d);
		originalStyle.setDefaultValue(BasicVisualLexicon.NODE_FILL_COLOR, new Color(204, 255, 255));
		originalStyle.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 5d);
		originalStyle.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
		
		return originalStyle;
	}
}