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

This Python script append column which enzyme genes differ between
KEGG and iAF1260.

.. code-block:: python

    from pymongo import MongoClient
