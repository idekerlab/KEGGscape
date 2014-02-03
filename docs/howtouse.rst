===================================================
 How to import KEGG pathway xml(kgml) to Cytoscape
===================================================

First we show how to import KEGG pathway xml(kgml) to Cytoscape.

Downloading KEGG pathway xml(kgml)
==================================

You can download KEGG pathway xml(called kgml) without opening browser
(if you know the KEGG pathway entryID you want to import).

::

   wget http://rest.kegg.jp/get/eco00020/kgml -O eco00020.xml

eco00020.xml is TCA cycle of Escherichia coli K-12 MG1655.


Importing kgml to Cytoscape
===========================

You can import kgml to Cytoscape from menu bar

File -> Import -> Network -> File

and open eco00020.xml.

.. image:: https://raw.github.com/idekerlab/KEGGscape/develop/docs/images/import.png



