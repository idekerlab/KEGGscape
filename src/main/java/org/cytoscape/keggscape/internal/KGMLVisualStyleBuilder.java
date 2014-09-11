package org.cytoscape.keggscape.internal;

import java.awt.Color;
import java.awt.Paint;
import java.util.Set;

import org.cytoscape.keggscape.internal.read.kgml.KGMLMapper;
import org.cytoscape.view.presentation.property.ArrowShapeVisualProperty;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.LineTypeVisualProperty;
import org.cytoscape.view.presentation.property.NodeShapeVisualProperty;
import org.cytoscape.view.presentation.property.values.ArrowShape;
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
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_X, String.class, BasicVisualLexicon.NODE_X_LOCATION);
		final PassthroughMapping<String, Double> nodeyPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_Y, String.class, BasicVisualLexicon.NODE_Y_LOCATION);
		final PassthroughMapping<String, Double> nodewidthPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_WIDTH, String.class, BasicVisualLexicon.NODE_WIDTH);
		final PassthroughMapping<String, Double> nodeheightPassthrough = (PassthroughMapping<String, Double>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_HEIGHT, String.class, BasicVisualLexicon.NODE_HEIGHT);
		final PassthroughMapping<String, String> nodelabelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_LABEL_LIST_FIRST, String.class,
						BasicVisualLexicon.NODE_LABEL);
		final PassthroughMapping<String, Paint> nodelabelcolorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_LABEL_COLOR, String.class,
						BasicVisualLexicon.NODE_LABEL_COLOR);
		final PassthroughMapping<String, Paint> nodefillcolorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_FILL_COLOR, String.class,
						BasicVisualLexicon.NODE_FILL_COLOR);
		final PassthroughMapping<String, String> nodeTooltipPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_LABEL, String.class, BasicVisualLexicon.NODE_TOOLTIP);

		final PassthroughMapping<String, String> edgeLabelPassthrough = (PassthroughMapping<String, String>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_EDGE_LABEL, String.class,
						BasicVisualLexicon.EDGE_LABEL);

		defStyle.addVisualMappingFunction(nodexPassthrough);
		defStyle.addVisualMappingFunction(nodeyPassthrough);
		defStyle.addVisualMappingFunction(nodewidthPassthrough);
		defStyle.addVisualMappingFunction(nodeheightPassthrough);
		defStyle.addVisualMappingFunction(nodelabelPassthrough);
		defStyle.addVisualMappingFunction(nodeTooltipPassthrough);
		defStyle.addVisualMappingFunction(nodelabelcolorPassthrough);
		defStyle.addVisualMappingFunction(nodefillcolorPassthrough);
		
		defStyle.addVisualMappingFunction(edgeLabelPassthrough);

		final DiscreteMapping<String, LineType> edgelinetypeMapping = (DiscreteMapping<String, LineType>) discreteMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_EDGE_SUBTYPES, String.class,
						BasicVisualLexicon.EDGE_LINE_TYPE);
		edgelinetypeMapping.putMapValue("maplink", LineTypeVisualProperty.LONG_DASH);
		edgelinetypeMapping.putMapValue("state change", LineTypeVisualProperty.DOT);
		edgelinetypeMapping.putMapValue("binding/association", LineTypeVisualProperty.LONG_DASH);
		edgelinetypeMapping.putMapValue("indirect effect", LineTypeVisualProperty.LONG_DASH);
		
		final DiscreteMapping<String, ArrowShape> targetArrowShapeMapping = (DiscreteMapping<String, ArrowShape>) discreteMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_EDGE_SUBTYPES, String.class,
						BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE);
		targetArrowShapeMapping.putMapValue("activation", ArrowShapeVisualProperty.ARROW);
		targetArrowShapeMapping.putMapValue("inhibition", ArrowShapeVisualProperty.T);
		targetArrowShapeMapping.putMapValue("expression", ArrowShapeVisualProperty.ARROW);
		targetArrowShapeMapping.putMapValue("repression", ArrowShapeVisualProperty.T);
		targetArrowShapeMapping.putMapValue("indirect effect", ArrowShapeVisualProperty.HALF_TOP);
		
		final DiscreteMapping<String, NodeShape> nodetypeMapping = (DiscreteMapping<String, NodeShape>) discreteMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_TYPE, String.class, BasicVisualLexicon.NODE_SHAPE);
		nodetypeMapping.putMapValue("ortholog", NodeShapeVisualProperty.RECTANGLE);
		nodetypeMapping.putMapValue("gene", NodeShapeVisualProperty.RECTANGLE);
		nodetypeMapping.putMapValue("map", NodeShapeVisualProperty.ROUND_RECTANGLE);
		nodetypeMapping.putMapValue("compound", NodeShapeVisualProperty.ELLIPSE);
		nodetypeMapping.putMapValue("group", NodeShapeVisualProperty.ROUND_RECTANGLE);

		final DiscreteMapping<String, Double> nodeBorderMapping = (DiscreteMapping<String, Double>) discreteMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_NODE_TYPE, String.class,
						BasicVisualLexicon.NODE_BORDER_WIDTH);
		nodeBorderMapping.putMapValue("group", 5d);

		defStyle.addVisualMappingFunction(edgelinetypeMapping);
		defStyle.addVisualMappingFunction(targetArrowShapeMapping);
		
		defStyle.addVisualMappingFunction(nodetypeMapping);
		defStyle.addVisualMappingFunction(nodeBorderMapping);

		return defStyle;
	}

	private final void createDefaults(final VisualStyle style) {
		// Defaults for nodes
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE, 8);
		style.setDefaultValue(BasicVisualLexicon.NODE_LABEL_WIDTH, 80d);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_WIDTH, 1d);
		style.setDefaultValue(BasicVisualLexicon.NODE_TRANSPARENCY, 230);
		style.setDefaultValue(BasicVisualLexicon.NODE_BORDER_TRANSPARENCY, 255);

		// Defaults for Edges
		style.setDefaultValue(BasicVisualLexicon.EDGE_WIDTH, 1d);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_FONT_SIZE, 14);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY, 255);
		style.setDefaultValue(BasicVisualLexicon.EDGE_LABEL_COLOR, Color.RED);
		style.setDefaultValue(BasicVisualLexicon.EDGE_TARGET_ARROW_SHAPE, ArrowShapeVisualProperty.NONE);
		style.setDefaultValue(BasicVisualLexicon.EDGE_UNSELECTED_PAINT, Color.DARK_GRAY);
		style.setDefaultValue(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT, Color.DARK_GRAY);
		style.setDefaultValue(BasicVisualLexicon.EDGE_TRANSPARENCY, 200);
	}

	public VisualStyle getGlobalVisualStyle() {
		final VisualStyle originalStyle = this.getVisualStyle();
		originalStyle.setTitle(GLOBAL_VS_NAME);

		final PassthroughMapping<String, Paint> edgeColorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_EDGE_COLOR, String.class,
						BasicVisualLexicon.EDGE_UNSELECTED_PAINT);
		final PassthroughMapping<String, Paint> edgeStrokeColorPassthrough = (PassthroughMapping<String, Paint>) passthroughMappingFactory
				.createVisualMappingFunction(KGMLMapper.KEGG_EDGE_COLOR, String.class,
						BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
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