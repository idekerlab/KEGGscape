=============================================
 Combination of Python scripts and KEGGscape
=============================================

Scripting language support is an experimental feature in Cytoscape 3

Cytoscape 3 supports scripting language.
Here we show a sample of Python + KEGGscape.

We import all Ecoli pathways to Cytoscape.

Importing all KEGG pathways of Escherichia coli K-12 MG1655
===========================================================

First we download all Ecoli pathways with the following Python script.
This script use Python `request`__  package.

__ http://docs.python-requests.org/en/latest/

.. code-block:: python
   
   import requests
   
   ORGANISM = "eco"
   
   pathways = requests.get('http://rest.kegg.jp/list/pathway/' + ORGANISM)
   for line in pathways.content.split('\n'):
       pathwayid = line.split('\t')[0].replace('path:', '')
       kgml = requests.get('http://rest.kegg.jp/get/' + pathwayid + '/kgml')
       f = open(pathwayid + '.xml', 'w')
       f.write(kgml.content)
       f.close

Next we show a sample to import a kgml from Python script.
