package org.cytoscape.keggscape.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The BundleActivator for HelloWorld. When the enclosing bundle is started and
 * stopped, the start() and stop() methods are called, respectively.
 */
public class Activator implements BundleActivator {
	/**
	 * This is the {@code start} method, which sets up your app. The
	 * {@code BundleContext} object allows you to communicate with the OSGi
	 * environment. You use {@code BundleContext} to import services or ask OSGi
	 * about the status of some service.
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		// We use a logger from SLF4J to handle our output. In Cy3
		// the log messages is accessible through the menu Help ->
		// Developer's Log Console...
		Logger logger = LoggerFactory.getLogger(Activator.class);
		logger.warn("Hello, world!");
		// This is a test
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Logger logger = LoggerFactory.getLogger(Activator.class);
		logger.warn("I'm Kozo Nishida Goodbye!");
		// This is another test
	}
}
