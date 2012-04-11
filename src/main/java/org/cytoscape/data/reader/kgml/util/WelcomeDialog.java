package org.cytoscape.data.reader.kgml.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.cytoscape.data.reader.kgml.KGMLReaderPlugin;

import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;

public final class WelcomeDialog extends JDialog {

	private static final long serialVersionUID = -4848738704932153111L;

	private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 14);
	private static final String DOC_PAGE_URL = "http://cytoscape.github.com/kgmlreader/";
	

	private static final String DESCRIPTION_TEXT = "<html><body>KGMLReader Plugin is a file reader for KEGG XML format (KGML).  " +
			"To import KGML file, select <br /><ul><li>File-->Import-->Network</li></ul>"
			+ "<p>For more information, please visit " +
			"<a href='"+ DOC_PAGE_URL + "'>KGMLReader Plugin web site</a></p></body></html>";

	private JLabel titleLabel;

	private JEditorPane description;

	private JButton closeButton;

	private JCheckBox doNotShowAgainCheckBox;

	public static final WelcomeDialog DIALOG = new WelcomeDialog();

	public static void showDialog() {
		DIALOG.setLocationRelativeTo(Cytoscape.getDesktop());
		DIALOG.setVisible(true);
	}

	private WelcomeDialog() {
		super(Cytoscape.getDesktop(), false);
		initComponents();
	}

	private void initComponents() {
		this.setTitle("Welcome to KGML Reader Plugin");

		titleLabel = new JLabel("Welcome to KEGG Pathway XML File Reader Plugin!");
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		titleLabel.setHorizontalTextPosition(SwingConstants.CENTER);

		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		doNotShowAgainCheckBox = new JCheckBox();
		doNotShowAgainCheckBox.setText("Do not show again");

		closeButton = new JButton();
		closeButton.setText("OK");
		closeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				CytoscapeInit.getProperties().setProperty(KGMLReaderPlugin.HIDE_WELCOME_SCREEN,
						Boolean.toString(doNotShowAgainCheckBox.isSelected()));
				setVisible(false);
			}
		});

		southPanel.add(doNotShowAgainCheckBox, BorderLayout.NORTH);
		southPanel.add(closeButton, BorderLayout.SOUTH);

		description = new JEditorPane();
		description.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		description.setBackground(Color.white);
		description.setPreferredSize(new Dimension(450, 200));
		description.setEditable(false);
		description.setContentType("text/html"); // javax.swing.text.html.HTMLEditorKit
		description.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
		description.addHyperlinkListener(new HyperlinkListener() {
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED)
					cytoscape.util.OpenBrowser.openURL(e.getURL().toString());
			}
		});
		description.setText(DESCRIPTION_TEXT);

		this.setLayout(new BorderLayout());
		this.add(titleLabel, BorderLayout.NORTH);
		this.add(description, BorderLayout.CENTER);
		this.add(southPanel, BorderLayout.SOUTH);
		pack();
	}
}
