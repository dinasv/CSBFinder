# OGMFinder v 0.1

-   [Overview](#overview)
-   [Prerequisites](#prerequisites)
-   [Running OGMFinder](#running)
-   [Input files formats](#input)


<a name='overview'>Overview</a>
--------
OGMFinder was designed to find all ordered gene patterns (OGMs) in the input dataset. Given input genomes that are represented as gene
strings over an alphabet of gene family ids, an **_OGM_** is defined to be a substring of one of the genomes,
that has an instance in at least **_q_** genomes. An instance of an OGM is allowed to have at most **_k_** insertions in relation to 
that OGM. For example, if ABC is an OGM (each letter is a different gene family id) and k=2, ABC, AABC and ADBBC are instances of ABC, 
while ADGBBC is not. **_q_** and **_k_** are input parameteres specified by the user.

<a name='prerequisites'>Prerequisites</a>
--------

[Java Runtime Environment (JRE)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
8 or up.

<a name='running'>Running OGMFinder</a>
--------

OGMFinder can be executed on any operation system using JAVA.

In the terminal (linux) or cmd (windows) type:
``` 
java -jar OGMFinder.jar [options]
```
> Note: When executing OGMFinder on a large dataset, add the option -Xmx8g (8g or more, depending on your RAM size).
For example:
``` 
java -Xmx8g -jar OGMFinder.jar [options]
```

### Options:
Mandatory:
- **-in**   
    Input file name. See [Input files formats](#input) for more details.
- **-q**   
      Instance quorum with insertions. Minimal number of input sequences with a CSB instance.   
      Default: 1
      
Optional:     
- **-ins**   
      Maximal number of insertions allowed    
      Default: 0
- **-qexact**
      Instance quorum without insertions. Minimal number of input sequences with a CSB instance without insertions.   
      Default: 1
- **-lmin**   
      Minimal length of a CSB   
      Default: 2
- **-lmax**
      Maximal length of a CSB   
      Default: 2147483647   
- **--datasetname, -ds**   
      Dataset name   
      Default: dataset1
- ***--patterns, -p***   
      Input CSB patterns file name. See [Input files formats](#input) for more details.
- **-cog-info**   
      Gene families information file name. See [Input files formats](#input) for more details.
- **-bcount**   
      If true, count one instance per input sequence   
      Default: true
- **--threshold, -t**   
      Threshold for family clustering   
      Default: 0.8
- **-out**   
      Output file type   
      Default: XLSX   
      Possible Values: [TXT, XLSX]
- **-clust-by**   
      Cluster CSBs to families by: 'score' or 'length'  
      Default: SCORE   
      Possible Values: [LENGTH, SCORE]
- **--help**   
      Show usage

For example, running OGMFinder.jar with input file 'plasmids.txt' provided in [https://www.cs.bgu.ac.il/~negevcb/OGMFinder/OGMFinder/input/](https://www.cs.bgu.ac.il/~negevcb/OGMFinder/OGMFinder/input/), with **_q_**=50 and **_k_**=2:
``` 
java -jar OGMFinder.jar -i cog_words_plasmid -ins 2 -q2 50 -ds plasmid
```

<a name='input'>Input files formats</a>

