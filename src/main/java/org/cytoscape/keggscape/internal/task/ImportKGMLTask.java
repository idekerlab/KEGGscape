package org.cytoscape.keggscape.internal.task;

import java.io.IOException;
import java.net.*;

import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.io.read.CyNetworkReader;
import org.cytoscape.keggscape.internal.style.KGMLVisualStyleBuilder;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
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

  private Long suid = null;

  public ImportKGMLTask(final CyNetworkViewFactory cyNetworkViewFactory,
      final CyNetworkFactory cyNetworkFactory, final CyNetworkManager cyNetworkManager,
      final CyRootNetworkManager cyRootNetworkManager, final KGMLVisualStyleBuilder vsBuilder,
      final VisualMappingManager vmm, final CyGroupFactory groupFactory, String pathwayID) {
          this.cyNetworkViewFactory = cyNetworkViewFactory;
          this.cyNetworkFactory = cyNetworkFactory;
          this.cyNetworkManager = cyNetworkManager;
          this.cyRootNetworkManager = cyRootNetworkManager;
          this.vsBuilder = vsBuilder;
          this.vmm = vmm;
          this.pathwayID = pathwayID;
          this.groupFactory = groupFactory;
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
        reader.run(taskMonitor);
        final CyNetwork[] networks = reader.getNetworks();
        final CyNetwork network = networks[0];
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
