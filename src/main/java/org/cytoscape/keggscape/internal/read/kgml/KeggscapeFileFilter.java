package org.cytoscape.keggscape.internal.read.kgml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Set;

import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.util.StreamUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom file filter to return KEGG reader for all KGML files.
 * 
 */
public class KeggscapeFileFilter extends BasicCyFileFilter {

	private static final Logger logger = LoggerFactory.getLogger(KeggscapeFileFilter.class);

	private static final String KEGG_TAG = "www.kegg.jp/kegg";

	public KeggscapeFileFilter(Set<String> extensions, Set<String> contentTypes, String description,
			DataCategory category, StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	public KeggscapeFileFilter(String[] extensions, String[] contentTypes, String description, DataCategory category,
			StreamUtil streamUtil) {
		super(extensions, contentTypes, description, category, streamUtil);
	}

	@Override
	public boolean accepts(final InputStream stream, final DataCategory category) {
		final String header = getHeader(stream, 5);
		logger.debug("File header: " + header);

		if (header.contains(KEGG_TAG)) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean accepts(final URI uri, final DataCategory category) {
		try {
			return accepts(uri.toURL().openStream(), category);
		} catch (IOException e) {
			logger.error("Error while opening stream: " + uri, e);
			return false;
		}
	}
}