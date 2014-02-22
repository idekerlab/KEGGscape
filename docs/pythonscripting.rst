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
This script needs Python `requests`__  package.

__ http://docs.python-requests.org/en/latest/

You will get all eco KGMLs like this.

.. image:: https://raw.github.com/idekerlab/KEGGscape/develop/docs/images/get_all_eco_kgmls.png

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
To use Python from Cytoscape3, you need to download jython-standalone
from `here`__ , and move the jython-standalone like this.

__ http://www.jython.org/downloads.html

.. image:: http://wiki.cytoscape.org/Cytoscape_3/UserManual/Scripting?action=AttachFile&do=get&target=python.png

Now you can batch-import kgml files with Python.
Here we import all carbohydrate metabolism kgml files.
(Of course you can import all pathways, but it takes time and cys file
get so big.)

.. code-block:: bash
   
   mkdir carbohydrate
   mv eco00010.xml eco00020.xml eco00030.xml eco00040.xml eco00051.xml
   eco00052.xml eco00053.xml eco00500.xml eco00520.xml eco00562.xml
   eco00620.xml eco00630.xml eco00640.xml eco00650.xml eco00660.xml

next run cytoscape3, go in osgi shell and run following Python
script(save as load_kegg.py).

.. code-block:: python
   
   from java.io import File
    
   KEGG_DIR = "/ABS_PATH_TO/carbohydrate/"
   pathways = ["eco00010.xml", "eco00020.xml", "eco00030.xml", "eco00040.xml", "eco00051.xml", "eco00052.xml", "eco00053.xml", "eco00500.xml", "eco00520.xml", "eco00562.xml", "eco00620.xml", "eco00630.xml", "eco00640.xml", "eco00650.xml", "eco00660.xml"]
   
   loadNetworkTF = cyAppAdapter.get_LoadNetworkFileTaskFactory()
   taskManager = cyAppAdapter.getTaskManager()
    
   allTasks = None
   
   for pathway in pathways:
       kgmlpath = File(KEGG_DIR + pathway)
       print str(kgmlpath)
       itr = loadNetworkTF.createTaskIterator(kgmlpath)
       if allTasks is None:
           allTasks = itr
       else:
           allTasks.append(itr)
    
   taskManager.execute(allTasks)

Save this Python script as load_kegg.py.
To run load_kegg.py, type (from Cytoscape3 OSGi shell)

.. code-block:: bash

   cytoscape:script python /ABS_PATH_TO_SCRIPT/load_kegg.py

.. image:: https://raw.github.com/idekerlab/KEGGscape/develop/docs/images/batchimport.PNG
