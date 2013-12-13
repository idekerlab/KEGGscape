package org.cytoscape.keggscape.internal.ui;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;

public class ShowPathwaySelectorAction extends AbstractCyAction {
	
	private static final long serialVersionUID = 7186087068626505675L;

	private PathwaySelectorDialog dialog;
	
	public ShowPathwaySelectorAction() {
		super("Import KEGG Pathways...");
		setPreferredMenu("File.Import.Network");
		setEnabled(true);
		setMenuGravity(100.0f);
	}

	
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(dialog == null) {
			dialog = new PathwaySelectorDialog();
		}
		
		dialog.setVisible(true);
	}
}
