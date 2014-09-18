package org.cytoscape.keggscape.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JList;

public class PathwaySelectorDialog extends JDialog {

	private static final long serialVersionUID = 4521458991696562839L;

	private JComboBox speciesBox;
	private JList pathwayList;

	public PathwaySelectorDialog() {
		super();
		this.setTitle("KEGG Pathways");
		this.setPreferredSize(new Dimension(400, 600));
		this.setLayout(new BorderLayout());

		init();
	}

	private final void init() {
		pathwayList = new JList();
		speciesBox = new JComboBox();

		getSpeciesNames();

		this.add(speciesBox, BorderLayout.NORTH);
		this.add(pathwayList, BorderLayout.CENTER);
		this.pack();
	}

	private final void getSpeciesNames() {
//		final Client client = ClientBuilder.newClient();
//		WebTarget target = client.target("http://rest.kegg.jp/").path("list/organism");
//		String list = target.request(MediaType.TEXT_PLAIN_TYPE).get(String.class);
//		
//		final String[] entries = list.split("\\n");
//
//		for(String sp:entries) {
//			speciesBox.addItem(sp);
//		}
	}
}