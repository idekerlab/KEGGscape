package org.cytoscape.keggscape.internal;

import java.io.File;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory for Tasks that greet the user.
 */
public class KeggscapeTaskFactory implements TaskFactory {
	@Override
	public boolean isReady() {
		// This method lets the factory do its own sanity checks to verify
		// it's ready to create and run its tasks.
		return true;
	}

	@Override
	public TaskIterator createTaskIterator() {
		// Our factory creates a sequence of three tasks.
		return new TaskIterator(
			new HelloWorldTask(),
			new TunableGalleryTask()
		);
	}

	/**
	 * A basic Task implementation that says "hello" on the log console.
	 */
	public static class HelloWorldTask implements Task {
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// The setStatusMessage() method lets the user know what's
			// happening. In the Cy3 desktop application, this message
			// only gets shown if the task takes longer than half a
			// second to complete.
			taskMonitor.setStatusMessage("KEGGscape started...");

			Logger logger = LoggerFactory.getLogger(getClass());
			logger.info("Hello, world!");

			// The setProgress() method tells the TaskMonitor how close we
			// are to task completion, where 1.0 means we're done. As before,
			// the progress meter is only shown by the Cy3 desktop application
			// if the task takes longer than half a second.
			taskMonitor.setProgress(1.0);
		}

		@Override
		public void cancel() {
		}
	}

	/**
	 * Another parameterized Task implementation demonstrating other types
	 * of Tunable parameters.
	 */
	public static class TunableGalleryTask implements Task {
		@Tunable(description = "KGML to load",
			     params = "fileCategory=network;input=true")
		public File fileToLoad;
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			taskMonitor.setStatusMessage(String.format("Loading %s...",
				                                       fileToLoad.getPath()));
		}
		
		@Override
		public void cancel() {
		}
	}
}

