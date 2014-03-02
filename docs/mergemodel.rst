=======================================================
Mapping genome scale metabolic model and KEGG pathway
=======================================================

Here we show the other example of data integration.
We map iAF1260(a genome-scale metabolic reconstruction for Escherichia
coli K-12 MG1655 that accounts for 1260 ORFs) on KEGG pathway.

Importing iAF1260 into MongoDB
==============================

You can download iAF1260 reaction table from ModelSEED_.

.. _ModelSEED: http://seed-viewer.theseed.org/seedviewer.cgi?page=ModelView

.. image:: https://raw.github.com/idekerlab/KEGGscape/develop/docs/images/download_seedmodel.png

and import this table into MongoDB, like this. ::

    mongoimport --db keggscape --collection iaf1260 --type tsv --headerline --file table.tsv

and export Galactose metabolism pathway from Cytoscape and import it
like this. ::

    mongoimport --db keggscape --collection galactose_node --headerline --type csv --file galactose_node.csv

This Python script append column which enzyme genes differ between
KEGG and iAF1260.

.. code-block:: python

    from sets import Set
    from pymongo import MongoClient
    
    client = MongoClient()
    db = client['keggscape']
    
    node_collection = db['galactose_node']
    model_collection = db['iaf1260']
    
    kegggene_table = node_collection.find({"KEGG_NODE_TYPE": "gene"})
    modelreaction_table = model_collection.find({"KEGG RID": {"$regex": "R[0-9]{5}"}})
    
    for kegggene in kegggene_table:
        kegggenes = kegggene['KEGG_ID'].split("\r")
        keggonly_genes = []
        modelonly_genes = []
    
        for keggreaction in kegggene['KEGG_NODE_REACTIONID'].split(" "):
            modelkeggreaction_table = model_collection.find({"KEGG RID": {"$regex": keggreaction.replace("rn:", "")}})
    
            if modelkeggreaction_table.count() > 0:
                for modelkeggreaction in modelkeggreaction_table:
                    modelgenes = modelkeggreaction['iAF1260\r'].strip().replace("<br>", "eco:").split(", ")
    
                    if Set(kegggenes) != Set(modelgenes):
                        keggonly = Set(kegggenes) - Set(modelgenes)
                        modelonly = Set(modelgenes) - Set(kegggenes)
                        if len(keggonly) > 0:
                            node_collection.update({"_id": kegggene["_id"]}, {"$push": {"keggonly": " ".join(keggonly)}})
                        else:
                            node_collection.update({"_id": kegggene["_id"]}, {"$push": {"modelonly": " ".join(modelonly)}})

And export galactose_node collection and reimport to Cytoscape. ::

    mongoexport --db keggscape --collection galactose_nodes --csv --fieldFile genediff_fields.txt --out new_galactose_nodes.csv

Here is the annotation difference between iAF1260 and KEGG

.. image:: https://raw.github.com/idekerlab/KEGGscape/develop/docs/images/kegg_model_genediff.png
