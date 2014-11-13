# KEGGscape
__Comprehensive KEGG Database Support App for Cytoscape 3.x Series__

Formerly known as [KGMLReader](https://github.com/idekerlab/kgmlreader/tree/2.x)

![](http://cl.ly/XbCF/kegg_human_global2.png)


![](http://cl.ly/XbMZ/keggscape.png)


## Introduction
[Cytoscape](http://www.cytoscape.org/) is a de-facto standard bioinformatics software platform for integrating, analyzing, and visualizing biological network data.  __*KEGGscape*__ is a [Cytoscape App](http://apps.cytoscape.org/) to provide comprehensive support for KEGG Pathway database and other resources available in KEGG.  KEGGScape uses only basic data structures in Cytoscape core, and users can use powerful Cytoscape core features to visualize additional (experimental) data sets on top of KEGG pathways. 

### About KEGG

![](http://cl.ly/XbS7/kegg_sugar1.png)

[KEGG Pathway database](http://www.genome.jp/kegg/pathway.html) is one of the most 
comprehensive databases of human-curated biological pathways.  [KGML (KEGG Markup Language)](http://www.kegg.jp/kegg/xml/) is 
the file format to represent KEGG Pathway data files in XML.  KEGGScape uses those KGML files as its primary source of pathway data.


## Goals
* Full support for [KEGG Pathway](http://www.genome.jp/kegg/pathway.html), including signaling pathways.
* On-demand access to [KEGG RESTful API](http://www.kegg.jp/kegg/rest/keggapi.html) to add extra annotations for the pathways.
* Annotation by [KEGG MEDICUS database](http://www.kegg.jp/kegg/rest/keggapi2.html)
* Merging multiple pathways in a same network view

## Design
(Available soon...)


## Release Notes
### 0.5.x (9/4/2013)
Basic support for KEGG metabolic pathways.


### 0.7.0 (9/17/2014)
This is a __major__ version up and has lots of new features!

![](http://cl.ly/Xbq9/kegg_biosysthesis.png)


#### Full support for KEGG Pathway Database

![](http://cl.ly/XbiV/kegg_human2.png)

![](http://cl.ly/Xbha/kegg_cell_cycle.png)


Now KEGGScape supports all pathway data available in KGML format, including:

* Metabolic Pathways
* Global Map for Metabolic Pathways
* Overview Maps
* Signaling Pathways
* Organismal Systems
* Human Disease Pathways


#### Better Visual Styles
We have tweaked custom _Visual Style_ to present as mush information as possible out of the original KGML files.  Directionality and type of reactions/relations are mapped to human understandable visual properties.

![](http://cl.ly/XbmY/kegg_human_cancer.png)


#### Maplink Expansion from Context Menu
KEGG pathways have _maplink_ nodes which point to other pathways.  Cytoscape can automatically import those pathways by selecting a context menu:

![](http://cl.ly/Xbri/kegg_expand.png) 

- Select a maplink node and right-click to expand!

![](http://cl.ly/XbAd/kegg_mapk.png)

- Actual pathway data will be fetched dynamically from KEGG pathway database.

#### View Details in Web Browser
If you need complete information about compounds, genes, or pathways, you can view details in web browser by selecting right-click menu item.

![](http://cl.ly/XbwG/details1.png)

![](http://cl.ly/Xast/details2.png)


#### Network Colleciton Support
From this version, KEGGScape supports network collection.  You can organize your pathways based on the biological context.

![](http://cl.ly/Xbca/kegg_organized.png)


#### Import Extra Annotations from KEGG
By checking options in import window, you can add extra information from KEGG:

* KEGG Modules
* Diseases
* Pathway classes

#### Group Support
Group information in KGML is now mapped to Cytoscape's CyGroup.  This means intrnally, grouped nodes are handled as CyGroup, instead of set of nodes.

- Default representation (set of individdual nodes):

![](http://cl.ly/XbO6/group1.png)

- Collapsed to a single group node:

![](http://cl.ly/Xbt6/group2.png)

#### Bug Fixes
Lots of bugs are fixed.  Some of the missed edges in the last version are now correctly mapped in the view.

----
## Future Plan
#### TODO
* Advanced example workflow written with [cy-rest](https://github.com/keiono/cy-rest/wiki) and IPython Notebook
* Automatic import from list of pathway IDs
* Import additional annotation on-demand:
	* KEGG Modules
	* Drug targets
	* etc.
* Expanding pathways in existing network view


## Credits

### Publication (Please cite this for project's sustainability!)

* [KEGGscape: a Cytoscape app for pathway data integration](http://www.ncbi.nlm.nih.gov/pmc/articles/PMC4141640/). Nishida K, Ono K, Kanaya S, Takahashi K. F1000Res. 2014 Jul 1;3:144. doi: 10.12688/f1000research.4524.1. eCollection 2014.

### Design & Development
* Kozo Nishida (Riken, Japan)
* Keiichiro Ono (Cytoscape Consortium / UC, San Diego, USA)

### Questions?
Please send them to [cytoscape-helpdesk](https://groups.google.com/forum/#!forum/cytoscape-helpdesk).
