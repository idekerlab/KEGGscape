import requests
from sets import Set
import more_itertools

pathways = requests.get('http://rest.kegg.jp/list/pathway')
compounds = []

for line in pathways.content.split('\n'):
    mapid = line.split('\t')[0].replace('path:', '')
    cpds = requests.get('http://rest.kegg.jp/link/cpd/' + mapid)
    for cpd in cpds.content.split('\n'):
        if len(cpd.split('cpd:')) > 1:
            compounds.append(cpd.split('cpd:')[1])

cpdids = list(Set(compounds))
cpdids.sort()

output = open('compoundNames.txt', 'w')
for ids in more_itertools.chunked(cpdids, 100):
    names = requests.get('http://rest.kegg.jp/list/' + '+'.join(ids))
    for line in names.content.split("\n"):
        foo = line.split("\t")
        if len(foo) > 1:
            cpdid = foo[0]
            names = foo[1]
            sorted_names = sorted(names.split("; "), key=len)
            output.write(cpdid + "\t" + "; ".join(sorted_names) + "\n")
output.close()
