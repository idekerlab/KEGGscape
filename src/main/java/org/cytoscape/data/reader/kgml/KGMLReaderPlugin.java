package org.cytoscape.data.reader.kgml;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.cytoscape.data.reader.kgml.util.PathwayDragAndDropManager;
import org.cytoscape.data.reader.kgml.util.WelcomeDialog;
import org.cytoscape.kegg.browser.KEGGNetworkListener;

import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import cytoscape.data.ImportHandler;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.view.CytoscapeDesktop;

/**
 * KGML Reader Main class
 * 
 */
public class KGMLReaderPlugin extends CytoscapePlugin {

	// For now, Metabolic pathways only.
	private static final String KEGG_PATHWAY_WEB = "http://www.genome.jp/kegg-bin/get_htext?htext=br08901.keg&filedir=/files&extend=A1&open=A2#A2";
	
	// Global property to show/hide
	public static final String HIDE_WELCOME_SCREEN = "hideKeggWelcomeScreen";
	
	static Boolean importAnnotation = true;

	public KGMLReaderPlugin() {
		final ImportHandler importHandler = new ImportHandler();
		importHandler.addFilter(new KGMLFilter());

		// Add context menu listeners.
		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(CytoscapeDesktop.NETWORK_VIEW_CREATED,
				new KEGGNetworkListener());

		// Setup drop target
		PathwayDragAndDropManager.getManager().activateTarget();

		// Add menu to poen KEGG Pathway Web Site
		addMenu();
		
		String shouldHide = CytoscapeInit.getProperties().getProperty(HIDE_WELCOME_SCREEN);
		if(shouldHide == null || Boolean.parseBoolean(shouldHide) == false)
			WelcomeDialog.showDialog();
	}
	

	private void addMenu() {
		final JMenu menu = new JMenu("KEGG Pathway XML (KGML) File Reader Options");
		final JMenu optionMenu = new JMenu("Options");

		Cytoscape.getDesktop().getCyMenus().getMenuBar().getMenu("Plugins").add(menu);

		menu.add(new JMenuItem(new AbstractAction("Open KEGG Browser") {
			public void actionPerformed(ActionEvent e) {
				cytoscape.util.OpenBrowser.openURL(KEGG_PATHWAY_WEB);
			}
		}));

		// Sub menu for options
		final JCheckBoxMenuItem isAutomaticImportEnabled = new JCheckBoxMenuItem();
		isAutomaticImportEnabled.setSelected(true);
		isAutomaticImportEnabled.setAction(new AbstractAction("Automatic Annotation Import from TogoWS") {
			public void actionPerformed(ActionEvent e) {
				importAnnotation = isAutomaticImportEnabled.isSelected();

				System.out.println("## Auto annotation import is " + importAnnotation);
			}
		});
		optionMenu.add(isAutomaticImportEnabled);
		menu.add(optionMenu);
		
		// Add welcome Screen
		menu.add(new JSeparator());
		menu.add(new JMenuItem(new AbstractAction("Show Quick Guide") {
			public void actionPerformed(ActionEvent e) {
				WelcomeDialog.showDialog();
			}
		}));
	}
}
