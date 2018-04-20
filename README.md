# OGMFinder v 0.1
==============================
-   [Overview](#overview)
-   [Prerequisites](#prerequisites)
-   [Running OGMFinder](#running)

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
java -jar OGMFinder.jar -i [-ins] [-q1] [-q2] [-l] [-bcount] [-ds] [-m] [-mem]
```
For example, running OGMFinder.jar with input file 'cog_words_plasmid.txt' provided in [https://www.cs.bgu.ac.il/~negevcb/OGMFinder/OGMFinder/input/](https://www.cs.bgu.ac.il/~negevcb/OGMFinder/OGMFinder/input/), with **_q_**=50 and **_k_**=2:
``` 
java -jar OGMFinder.jar -i cog_words_plasmid -ins 2 -q2 50 -ds plasmid
```

### Arguments
Mandatory:
- **-i, --input**			
  
   input file name containing a dataset of strings over an alphabet of ids, must be a *\*.txt* file located in *input* folder.
   If the file is large, it is recommended to use the option -Xmx8g (depending on RAM size)
   
Optional:
- **-ins, --insertion**
   
   maximal number of insertions (k), default 0
- **-q1, --quorum1**		
   
   minimal number of input strings the must contain an OGM as a substring, default 1
- **-q2, --quorum2**
   
   minimal number of input strings the must contain at least one instance of an OGM with up to *-ins* insertions, default 1
- **-l, --minlength**	
   
   minimal OGM length, default 2
- **-bcount, --boolean-count**
   
   A boolean indicating whether to at most one OGM instance in each input string (true), or by the total number of instances
   (could be more than one instance in each input string) (false), default true
- **-ds, --datasetname**
   
   dataset name, default *dataset1*
- **-m, --motifs**
   
   input file name containing putative OGMs, must be a *\*.txt* file located in *input* folder. If this file is provided, there will be no de-novo motif extraction. Only the given OGMs will be analyzed.
   
- **-mem**

   A memory saving option. If this option is selected, there will be no removal of redundant OGMs.
   
   ### Input files
   1. A mandatory input file containing all input sequences.
