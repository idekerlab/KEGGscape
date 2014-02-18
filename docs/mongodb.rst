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

    from pymongo import MongoClient
    
    client = MongoClient()
    db = client['keggscape']
    
    node_collection = db['alanine_nodes']
    drug_collection = db['all_target_ids_all']
    conv_collection = db['conv_eco_uniprot']
    
    gene_table = node_collection.find({"KEGG_NODE_TYPE": "gene"})
    
    for genes in gene_table:
        locuses = genes["KEGG_ID"].split("\r") #newline character depends on your OS, I exported cytoscape table on Mac
        for locus in locuses:
            ids = conv_collection.find_one({"kegg_id": locus})
            drug = drug_collection.find_one({"UniProt ID": ids["uniprot_id"].replace("up:", "")})
            if drug != None:
                node_collection.update({"_id": genes["_id"]}, {"$push": {"drug_ids": drug["Drug IDs"], "target_id": drug["ID"], "target": locus}})

Next we create fields.txt to export the new node table. ::

    SUID
    KEGG_ID
    KEGG_NODE_FILL_COLOR
    KEGG_NODE_HEIGHT
    KEGG_NODE_LABEL
    KEGG_NODE_LABEL_COLOR
    KEGG_NODE_LABEL_LIST
    KEGG_NODE_LABEL_LIST_FIRST
    KEGG_NODE_REACTIONID
    KEGG_NODE_SHAPE
    KEGG_NODE_TYPE
    KEGG_NODE_WIDTH
    KEGG_NODE_X
    KEGG_NODE_Y
    name
    selected
    shared name
    drug_ids
    target_id
    target
    
and export node table as csv. ::

    mongoexport --db keggscape --collection alanine_nodes --csv --fieldFile fields.txt --out new_node_table.csv

