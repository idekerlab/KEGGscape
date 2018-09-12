FROM selenium/standalone-chrome-debug

USER root
RUN apt-get update && apt-get install -y python3-dev git apt-transport-https libxml2-dev libxslt1.1 libjpeg62 libgstreamer1.0-0 libgstreamer-plugins-base1.0-0 && \
    apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E298A3A825C0D65DFD57CBB651716619E084DAB9 && \
    echo "deb https://cloud.r-project.org/bin/linux/ubuntu xenial-cran35/" >> /etc/apt/sources.list && \
    apt-get update && apt-get install -y r-base && \
    Rscript -e "source('http://bioconductor.org/biocLite.R'); biocLite(c('affy', 'limma', 'ecoliLeucine')); install.packages('knitr')" && \
    wget https://download1.rstudio.org/rstudio-xenial-1.1.456-amd64.deb && dpkg -i rstudio-xenial-1.1.456-amd64.deb && \
    ln -s /usr/bin/python3 /usr/bin/python && wget https://bootstrap.pypa.io/get-pip.py && \
    python3 get-pip.py && python3 -m pip install pandas requests py2cytoscape

USER seluser
RUN cd && wget http://chianti.ucsd.edu/cytoscape-3.6.1/cytoscape-3.6.1.tar.gz && tar xf cytoscape-3.6.1.tar.gz && \
    git clone git://github.com/idekerlab/KEGGscape
    
