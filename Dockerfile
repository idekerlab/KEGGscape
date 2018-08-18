FROM selenium/standalone-chrome-debug

USER seluser
RUN cd && wget http://chianti.ucsd.edu/cytoscape-3.6.1/cytoscape-3.6.1.tar.gz && tar xf cytoscape-3.6.1.tar.gz
