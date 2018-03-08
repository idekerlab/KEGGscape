package org.cytoscape.keggscape.internal.task;

import java.io.IOException;
import java.net.*;

import java.util.Collection;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.keggscape.internal.style.KGMLVisualStyleBuilder;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;

import org.cytoscape.keggscape.internal.read.kgml.KeggscapeNetworkReader;

public class ImportKGMLTask extends AbstractTask {

  private final CyNetworkViewFactory cyNetworkViewFactory;
  private final CyNetworkFactory cyNetworkFactory;
  private final CyNetworkManager cyNetworkManager;
	private final CyRootNetworkManager cyRootNetworkManager;
  private final CyGroupFactory groupFactory;

	private final KGMLVisualStyleBuilder vsBuilder;
	private final VisualMappingManager vmm;

	private final String pathwayID;
  // private final CyNetworkView parentView;
  private final CyNetworkViewManager cyNetworkViewManager;

  private Long suid = null;

  public ImportKGMLTask(final CyNetworkViewFactory cyNetworkViewFactory,
      final CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager,
      final CyRootNetworkManager cyRootNetworkManager, final KGMLVisualStyleBuilder vsBuilder,
      final VisualMappingManager vmm, final CyGroupFactory groupFactory, String pathwayID,
      final CyNetworkViewManager cyNetworkViewManager) {
          this.cyNetworkViewFactory = cyNetworkViewFactory;
          this.cyNetworkFactory = cyNetworkFactory;
          this.cyNetworkManager = cyNetworkManager;
          this.cyRootNetworkManager = cyRootNetworkManager;
          this.vsBuilder = vsBuilder;
          this.vmm = vmm;
          this.pathwayID = pathwayID;
          this.groupFactory = groupFactory;
          this.cyNetworkViewManager = cyNetworkViewManager;
    }

    public Long getSUID() {
      return suid;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
      taskMonitor.setTitle("Importing KEGG pathway");
      taskMonitor.setStatusMessage("Downloading Pathway (" + pathwayID + ") from KEGG...");
      taskMonitor.setProgress(-1.0);

      try {
        final URL foo = new URL("http://rest.kegg.jp/get/" + pathwayID + "/kgml");
        final URLConnection yc = foo.openConnection();
        final CyNetworkReader reader = new KeggscapeNetworkReader("foobar", yc.getInputStream(),
            cyNetworkViewFactory, cyNetworkFactory,	cyNetworkManager, cyRootNetworkManager,
            vsBuilder, vmm, groupFactory);
        // System.out.println("====before reader.run(taskMonitor)====");
        reader.run(taskMonitor);
        // System.out.println(reader.getNetworks().length);
        final CyNetwork[] networks = reader.getNetworks();
        final CyNetwork network = networks[0];

        // final CyNetworkView view = reader.buildCyNetworkView(network);
        cyNetworkManager.addNetwork(network);
        final CyNetworkView myView = cyNetworkViewFactory.createNetworkView(network);
        cyNetworkViewManager.addNetworkView(myView);
        // cyNetworkViewManager.addNetworkView(cyNetworkViewFactory.createNetworkView(network));

        for (final VisualStyle vs : vmm.getAllVisualStyles()){
          if(vs.getTitle() == "KEGG Style" || vs.getTitle() == "KEGG Global Map Style"){
            vs.apply(myView);
            break;
          }
        }

        System.out.println("====added network view with REST====");
        // System.out.println(networks.length);
        // System.out.println(network.getNodeCount());
        suid = network.getSUID();
      } catch (MalformedURLException male) {
          System.out.println("ERROR: WRONG URL " + male.toString());
      } catch (IOException ioe) {
          System.out.println("ERROR: WRONG FILE " + ioe.toString());
      } catch (Exception e) {
          System.out.println("ERROR: " + e.toString());
      }



    }
}
