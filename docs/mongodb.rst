====================================
Mapping drug targets on KEGG pathway
====================================

Here we show a example of data integration.
We map drug targets(from Drugbank) on KEGG pathway.
To manage several tables, we use `MongoDB`_ and `PyMongo`_.

Importing all data into MongoDB
===============================

First we export node attribute table of *Alanine, aspartate and glutamate metabolism* as alanine_nodes.csv.

.. image:: https://raw.github.com/idekerlab/KEGGscape/develop/docs/images/table_export_from_menu.png
.. image:: https://raw.github.com/idekerlab/KEGGscape/develop/docs/images/export_table_pulldown.png
.. image:: https://raw.github.com/idekerlab/KEGGscape/develop/docs/images/export_table_csv.png

Next we download drug targets from `Drugbank`_ and id convert table with `KEGG REST API`_. ::

    wget http://www.drugbank.ca/system/downloads/current/all_target_ids_all.csv.zip
    unzip all_target_ids_all.csv.zip
    wget http://rest.kegg.jp/conv/eco/uniprot
    mv uniprot conv_eco_uniprot.tsv

Finally we import these tables into mongodb. ::

    mongoimport --db keggscape --collection alanine_nodes --headerline --type csv --file alanine_nodes.csv
    mongoimport --db keggscape --collection all_target_ids_all --headerline --type csv --file all_target_ids_all.csv
    mongoimport --db keggscape --collection conv_eco_uniprot -f uniprot_id,kegg_id --type tsv --file conv_eco_uniprot.tsv

.. _Drugbank: http://www.drugbank.ca
.. _MongoDB: http://www.mongodb.org/
.. _PyMongo: http://api.mongodb.org/python/current/
.. _KEGG REST API: http://www.kegg.jp/kegg/docs/keggapi.html

Merging tables with PyMongo
===========================

We integrate the three table(network nodes, drug targets table, id conversion table).
Here we append columns *drug target* and *drug* to Cytoscape's node table.

.. code-block:: python
