# CSBFinder

-   [Overview](#overview)
-   [Prerequisites](#prerequisites)
-   [Running CSBFinder](#running)
    - [Download](#download)
    - [User Interface](#ui)
    - [Command Line](#cmd)
-   [Input files formats](#input)
    -   [Input genomes file](#input_dataset)
    -   [Gene orthology group information file](#cog_info)
    -   [Patterns file](#in_patterns)
    -   [Taxonomy file](#in_taxa)
-   [Output files](#output)
-   [Sample input files](#sample)
-   [User interface features](#ui_features)
-   [License](#license)
-   [Author](#author)
-   [Credit](#credit)


<a name='overview'>Overview</a>
--------
CSBFinder is a standalone Desktop java application with a graphical user interface, 
that can also be executed via command line.

CSBFinder implements a novel methodology for the discovery and ranking of 
colinear syntenic blocks (**CSBs**) - groups of genes that are consistently located close to each other, 
in the same order, across a wide range of taxa.
CSBFinder incorporates efficient algorithms that identify CSBs in large genomic datasets. 
The discovered CSBs are ranked according to a probabilistic score and clustered to families according to their gene 
content similarity.

### March 27, 2019 update

CSBFinder-S is released for the discovery of cross-strand multi-operon CSBs

- In this version, the user can decide whether to segment the input genomes into directons (consecutive genes on the same 
strand) 

- A novel exact algorithm that uses match-point arithmetic is implemented. 
The time and space complexities of the algorithm are insensitive to the number of insertions and maximal CSB length. 
The new algorithm is faster than the algorithm given in published in (Svetlitsky et. al., 2018) for larger values of 
insertions. Additional advantages of the algorithm are its simplicity of implementation, and the fact that it is easily 
parallelizable, yielding further scalability.

CSBFinder-S provides several novel mechanisms to help the user sort, filter, and interpret the discovered CSBs. 
- A ranking score that takes into account the genomic distances between the genomes in which the corresponding CSBs 
appear. 
- The user can constrain the structural features of the desired CSBs (length, abundance, etc.), as well as to extract 
CSBs confined to specific functional semantic categories. 

- A taxonomic viewer of the genomes that contain instances of each CSB.

- Many other improvement in the user interface

### Workflow Description
The workflow of CSBFinder is given in the figure below.    

**(A)** The input to the workflow is a dataset of input genomes, where each genome is modeled as a sequence of gene 
identifiers; A gene identifier indicates the corresponding gene orthology group as well as the strand (+/-) in which 
the gene is encoded. 
Additional input consists of user-specified parameters **_k_** (number of allowed insertions) and and **_q_** 
(the quorum parameter). In our formulation, a CSB is a pattern that
appears as a substring of at least one of the input genomes, and has instances in at least **_q_** of the input
genomes, where each instance may vary from the CSB pattern by at most **_k_** gene insertions. 

**(B)** The genomes are mined to identify all patterns that qualify as CSBs according to the user-specified parameters. 

**(C)** All discovered CSBs are ranked according to a probabilistic score. 

**(D)** The CSBs are clustered to families according to their gene content similarity, and the rank of a family is
determined by the score of its highest scoring CSB.

![CSBFinder workflow](https://github.com/dinasv/CSBFinder/blob/master/images/workflow.png "Workflow")


### Citation
The following paper contains details regarding the previous version of CSBFinder that targeted the extraction of 
CSBs that correspond to operons. It contains details of the Suffix-Tree based algorithm for CSB extraction. 

If you used the tool as part of your research, please cite us:
Dina Svetlitsky, Tal Dagan, Vered Chalifa-Caspi, Michal Ziv-Ukelson, 
CSBFinder: discovery of colinear syntenic blocks across thousands of prokaryotic genomes, Bioinformatics, 
Volume 35, Issue 10, 15 May 2019, Pages 1634–1643,
 [https://doi.org/10.1093/bioinformatics/bty861](https://doi.org/10.1093/bioinformatics/bty861)

<a name='prerequisites'>Prerequisites</a>
--------

[Java Runtime Environment (JRE)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
8 or higher.

<a name='running'>Running CSBFinder</a>
--------
### <a name='download'>Download</a>
- Download the [latest release](https://github.com/dinasv/CSBFinder/releases) of CSBFinder installer.
- The available options are Windows 64 or 32 bit, Unix and MacOS

> CSBFinder has a user interface, but can be executed via the command line by executing the JAR file in the 
installation folder. 

### <a name='ui'>Running CSBFinder via User Interface</a>
Just double click on the CSBFinder executable file in the installation folder, or (if you checked these options 
during installation) from the Start Menu / Desktop.

> Note: If you are going to use a very large input dataset you might need to change the maximal memory that can be
used by CSBFinder. Go to the installation folder and edit the file "CSBFinder.vmoptions" using a Text Editor. Change
the Java option `-Xmx500m` to `-Xmx[maximal heap size]` depending of the available RAM.
 For example `-Xmx6g` sets the maximal JAVA heap size to 6GB.

 > It is recommended to use at least 6GB for a large dataset. You can specify a higher number, 
 depending on you RAM size.  
 
#### Importing input files

1. **Importing a file containing the input genomes**:
    1. Choose `File->Import->Genomes File`. If your dataset is large, this
make take a few minutes.
    > [Sample input files](#sample) are provided in the input directory in the installation folder

    2. The "Run" button should be enabled. Click on this button to set the parameters.
 
    3. A progressBar appears. Hover over the question mark icon next to each parameter for an explanation of each parameter. 
After setting the parameters, click on "Run". This can take a few minutes, depending on the size of the dataset and 
on the parameters specified. 

    4. After the process is done, the lower panel will contain all the discovered CSBs. 

2. **Importing a saved session file**:  
    If you have ran CSBFinder and saved a session file, you can load it by choosing `File->Import->Session File`
  
3. **Importing gene orthology group information**:  
Load it by choosing `File->Import->Orthology Information file`. This information will be displayed on the lower right 
 panel.

### <a name='cmd'>Running CSBFinder via Command Line </a>

> CSBFinder can be executed via the command line by executing the JAR file in the installation folder. 

- In the terminal (linux) or cmd (windows) type:
    ``` 
    java -jar CSBFinder-[version]-jar-with-dependencies.jar [options]
    ```
    > Note: If your input dataset is very large, add the argument -Xmx6g (6g might be enough, but you can specify a 
    higher number, depending on your RAM size).
    For example:
    ``` 
    java -Xmx6g -jar CSBFinder-[version]-jar-with-dependencies.jar [options]
    ```
    > Note: When running CSBFinder without command line arguments, the user interface will be launched.
    

> [Sample input files](#sample) are provided below

#### Options:
Mandatory:
- **-in** INPUT_DATASET_FILE_NAME    
    Input file relative or absolute path. 
    See [Input files formats](#input_dataset) for more details.
- **-q** QUORUM     
      The quorum parameter. Minimal number of input sequences that must contain a CSB instance.   
      Default: 1
      Min Value: 1
      Max Value: Total input sequences
      
Optional:     
- **-ins** INSERTIONS    
      Maximal number of insertions allowed in a CSB instance.
      Default: 0
- **-lmin** MIN_CSB_LENGTH     
      Minimal length (number of genes) of a CSB   
      Default: 2
      Min Value: 2
      Max Value: Maximal sequence length
- **-lmax** MAX_CSB_LENGTH    
      Maximal length (number of genes) of a CSB   
      Default: Maximal sequence length
      Min Value: 2
      Max Value: Maximal sequence length
- **--export-file** EXPORT_FILE_NAME ,**-e** EXPORT_FILE_NAME       
      EXPORT_FILE_NAME will be the prefix of the names of the *.xlsx/*.txt output files.   
      Default: dataset1
- **--patterns** PATTERNS_FILE_NAME, **-p** PATTERNS_FILE_NAME    
      A name of a file, located in a directory named 'input', in the same directory as CSBFinder.jar.   
      If this option is used, CSBs are no longer extracted from the input sequences.    
      The file should contain specific CSB patterns that the user is interested to find in the input sequences.      
      See [Input files formats](#in_patterns) for more details.    
- **-cog-info** COG_INFO_FILE_NAME     
      A name of a file, located in a directory named 'input', in the same directory as CSBFinder.jar.   
      This file should contain functional description of orthology groups.    
      See [Input files formats](#cog_info) for more details.        
- **--cross-strand**, **-cs**
      If this option is provided, cross-strand CSBs will be extracted.
- **-out** OUTPUT_FILE_TYPE    
      Output file type   
      Default: XLSX   
      Possible Values: [TXT, XLSX, SESSION]
- **-out-dir** OUT_DIR  
      Path to output directory
      Default: output
- **-alg** ALG_NAME   
      Algorithm to use for finding CSBs
      Default: SUFFIX_TREE
      Possible Values: [SUFFIX_TREE, MATCH_POINTS]
- **-keep-all-patterns**
      If this option is provided, keep all patterns, without removing sub-patterns with the same number of
      instances
- **--threshold** THRESHOLD, **-t** THRESHOLD   
      Threshold for family clustering      
      Default: 0.8    
      Min Value: 0   
      Max Value: 1    
- **-clust-by** CLUSTER_BY    
      Cluster CSBs to families by: 'score' or 'length'  
      Default: SCORE   
      Possible Values: [LENGTH, SCORE]
- **-clust-denominator** CLUST_DENOMINATOR    
      In the greedy CSB clustering to families, a CSB is added to an existing cluster if the 
      (intersection between the CSB and the Cluster genes/X) is above a threshold. Choose X.
      Default: MIN_SET   
      Possible Values: [MIN_SET, MAX_SET, UNION]   
- **-skip-cluster-step**  
      If this option is provided, skip the clustering to families step
- **-procs** NUM_OF_PROCS    
      Number of processes. 0 designates the maximal number of available
      processes
      Default: 1
- **-h**, **--help**     
      Show usage
      
<a name='input'>Input files formats</a>
--------------

### <a name='input_dataset'> Input file containing input genome sequences </a>

A text/fasta file containing all input genomes modeled as strings, where each character is an orthology group ID 
(for example, COG ID) that has been assigned to a corresponding gene 
- This is a mandatory input file
- The path to this file is provided in:
    - User Interface: Load this file by choosing `File->Import->Genomes File`
    - Command Line: "-in" option

This file should use the following format:
```
>[genome name] | [ replicon name (e.g. plasmid or chromosome id)]
[homology group ID] TAB [Strand (+ or -)] TAB [you can add additional information]
[homology group ID] TAB [Strand (+ or -)] TAB [you can add additional information] 
[homology group ID] TAB [Strand (+ or -)] TAB [you can add additional information] 
....
```

All replicons of the same genome should be consecutive, i.e.:
```
>genomeA|replicon1
....
>genomeA|replicon2
...
>genomeB|replicon1
...
```
> Genes that do not belong to any gene orthology group, should be marked as 'X'

#### Example:
```
>Agrobacterium_H13_3_uid63403|NC_015183
COG1806	+
COG0424	+
COG0169	+
COG0237	+
COG0847	+
COG1952	-
COG3030	-
COG4395	+
COG2821	+
....
>Agrobacterium_H13_3_uid63403|NC_015508
X	+
X	+
COG1487	-
X	-
X	-
X	-
COG1525	-
X	+
COG2253	-
COG5340	-
....
>Agrobacterium_radiobacter_K84_uid58269|NC_011983
COG1192	+
COG1475	+
X	+
X	+
COG0715	+
COG0600	+
....
```


#### Assigning genes to orthologous group identifiers

You can annotate genes by any orthologous group identifiers. The IDs can be numbers or symbols, the only restriction 
is that each orthology group will have a unique ID.

##### Examples
1. The [STRING](https://string-db.org/cgi/download.pl?sessionId=Dmc2Jkurdd3b) database contains 
[COG](https://www.ncbi.nlm.nih.gov/COG/) and [NOG](http://eggnogdb.embl.de/) annotations of many publicly 
available genomes
2. Newly sequenced genomes can be mapped to known orthology groups such as:
    - COGs using [CDD](https://www.ncbi.nlm.nih.gov/Structure/bwrpsb/bwrpsb.cgi)
    - NOGs using [eggNOG mapper](http://eggnogdb.embl.de/#/app/emapper)
3. A tool such as [Proteinortho](https://www.bioinf.uni-leipzig.de/Software/proteinortho/) detects orthologous genes within different species. 
4. The paper ["New Tools in Orthology Analysis: A Brief Review of Promising Perspectives" by Bruno T. L. Nichio et. al.](https://www.ncbi.nlm.nih.gov/pmc/articles/PMC5674930/)
reviews several current tools for gene orthology detection

### <a name='cog_info'>Input file with functional information of gene orthology group IDs </a>
- This is an optional input file
- The path to this file is provided in:
    - User Interface: `File->Import->Orthology Information file`
    - Command Line: "-cog-info" option
#### COG information input file
If you are using [COGs](https://www.ncbi.nlm.nih.gov/COG/) (Cluster of Orthologous Genes) as your 
gene orthology group identifiers, you can use the file _cog_info.txt_ provided in the input directory in the 
installation folder (also can be downloaded from [here](https://github.com/dinasv/CSBFinder/tree/master/input)).

The functional description of gene orthology groups will appear in the legend (User Interface) 
or in the output catalog file (when choosing "Export" in the User Interface, or when 
executing via Command Line).

You can also use a custom file of your own. See instructions below.

### Custom gene orthology group information input file

This file should use the following format:
```
COGID;COG description;[COG functional categries seperated by a comma (e.g. "E,H"); COG functional categry description 1; COG functional categry description 2;...;geneID] 
```
>The text inside the brackets [] is optional
#### Example
```
COG0318;Acyl-CoA synthetase (AMP-forming)/AMP-acid ligase II;I,Q;Lipid transport and metabolism;Secondary metabolites biosynthesis, transport and catabolism;CaiC;
COG0319;ssRNA-specific RNase YbeY, 16S rRNA maturation enzyme;J;Translation, ribosomal structure and biogenesis;YbeY;
COG0320;Lipoate synthase;H;Coenzyme transport and metabolism;LipA;
...
```
### <a name='in_patterns'>Input file containing CSB patterns </a>
If this file is provided, CSBs are no longer extracted from the input sequences. 
This file shohuld contain specific CSB patterns which the user is interested to find in the input sequences.

- This is an optional input text file
- The path to this file is provided in:
    - User Interface: In the progressBar opened after clicking on the "Run" button
    - Command Line: "--patterns" or "-p" option

This file should use the following format:
```
>[unique pattern ID, must be an integer]
[homology group IDs seperated by commas]
>[unique pattern ID, must be an integer]
[homology group IDs seperated by commas]
```

#### Example
```
>1
COG3736,COG3504,COG2948,COG0630
>564654
COG3736,COG3504,COG2948
....
```

> If you are running in "cross-strand" mode, you should add a strand (+/-) to each homology group ID
e.g. COG3736+,COG3504+,COG2948-,COG0630+

### <a name='in_taxa'>Input file containing Taxonomy information </a>
If this file is provided, taxonomic distribution of each CSB will be displayed in the user interface.

- This is an optional input text file
- User Interface: Load this file by choosing `File->Import->Taxonomy File`

This file should use the following format:
```
HEADER
genome-name(as provided in input genomes file),kingdom,phylum,class,genus,species
genome-name(as provided in input genomes file),kingdom,phylum,class,genus,species
...
```

> Missing data should be marked by "-"

#### Example
```
genome,kingdom,phylum,class,genus,species
Acaryochloris_marina_MBIC11017_uid58167,Bacteria,Cyanobacteria,-,Acaryochloris,Acaryochloris_marina
Acetobacter_pasteurianus_IFO_3283_01_uid59279,Bacteria,Proteobacteria,Alphaproteobacteria,Acetobacter,Acetobacter_pasteurianus
....
```



<a name='output'>Output files</a>
--------------
After clicking on the "Export" menu option in the User Interface, or after execution via Command Line: two output files will 
be written to the specified directory

- **File 1: A Catalog of CSBs**: An excel file (or txt file) containing the discovered CSBs named "[export file name].xlsx"   
    This file contains three sheets: 
    1. Catalog    
        - Each line describes a single CSB
            - ID: unique CSB ID
            - Length: number of characters in the CSB
            - Score: a probabilistic ranking score, higher score indicates higher significance
            - Instance count: number of input sequences with an instance
            - CSB: a sequence of genes
            - Main_Category: if functional category was provided in the -cog-info file, this column contains the functional category of                             the majority of CSB gene families
            - Family_ID: CSBs with similar gene content will belong to the same family, indicated by a postive integer
    2. Filtered CSBs
        - This sheet contains only the top scoring CSB from each family
    3. CSBs description
        - Information about gene family IDs of each CSB

- **File 2: CSB instances**: A FASTA file with the same name as the catalog file, only with the suffix "\_instances"
    
    - Each entry represents a CSB and all its instances in the input genomes 
    - Each entry is composed of a header (CSB ID and genes), followed by lines describing the instances
    - Each line describes the locations of CSB instances in a specific input genome
    - There can be more than one instance in each genome
    - Each instance that is present in a replicon (e.g. chromosome/plasmid), begins from a specific index and can have 
    different lengths, depending on the number of insertions allowed in the instance
    - The index of the first gene in a replicon is 0 
    
    This file has the following format:
    
    ```
    >[CSB ID] TAB [CSB genes]
     [genome name] TAB [replicon name]|[[instance start index (inclusive),instance end index (exclusive)]]
     [genome name] TAB [replicon name]|[[instance start index (inclusive),instance end index (exclusive)]]

     ...
     >[CSB ID] TAB	[CSB genes]
     [genome name] TAB [replicon name]|[[instance start index (inclusive),instance end index (exclusive)]]
     [genome name] TAB [replic     [genome name] TAB [replicon name]|[[instance start index (inclusive),
     instance end index (exclusive)]]
     ...
    ```
    #### Example
    ```
    >4539	COG1012 COG0665 
    Rhizobium_leguminosarum_bv__trifolii_WSM2304_uid58997	NC_011368|[829,831]	NC_011368|[832,834]
    Agrobacterium_vitis_S4_uid58249	NC_011981|[171,173]
    ```
<a name='sample'>Sample input files</a>   
--------------------------------------
Sample input files are located in the input directory of the installation folder.
You can also download the following zip file:
> [Sample_input_files.zip](https://github.com/dinasv/CSBFinder/raw/master/input/Sample_input_files.zip)

The above zip file contains four files, located inside a folder named 'input':
- plasmid_genomes.fasta   
    _Plasmid dataset_ - 471 prokaryotic genomes with at least one plasmid, chromosomes were removed.
- chromosomal_genomes.fasta    
    _Chromosomal dataset_ - 1,485 prokaryotic genomes with at least one chromosome, plasmids were removed.
    > Important: this is a huge dataset. See instructions below, how to run CSBFinder with a large dataset
- cog_info.txt   
    Functional information of gene orthology groups
- taxa_csbfinder.txt   
    Taxonomy information for the user interface

### Execution of CSBFinder on the Chromosomal Dataset of 1,485 prokaryotic genomes
  
#### User Interface

The file _chromosomal_genomes.fasta_ contains ~1,500 genomes, hence CSBFinder needs more heap memory.
When uploading a large dataset.

Go to the installation folder and edit the file "CSBFinder.vmoptions" using a Text Editor. Change
the Java option `-Xmx500m` to `-Xmx[maximal heap size]` depending of the available RAM. Changing to at least `-Xmx6g` 
is recommended (sets the maximal JAVA heap size to 6GB).

- Now execute CSBfinder and choose `File->Import->Genomes File`, it may take a few minutes to load the selected file.

- Click on the "Run" button, and a window will open.

- Set the parameters (e.g. Quorum 50, Insertions Allowed 1).

- Clicking on the "Run" button will start the computation of CSBs, this may take a few minutes.
 When the process is done, the results will be shown.

##### Output
You can export the resulting CSBs as a TXT or XLSX file.
You can also save the results in a session file, that can be opened using the user interface.

#### Command Line
In the installation directory, you will find a *.jar file, e.g., CSBFinder-S-0.6.1-jar-with-dependencies.jar
``` 
java -Xmx6g -jar CSBFinder-[version]-jar-with-dependencies.jar -in input/chromosomal_genomes.fasta -q 50 -ins 1 -e Chromosomes 
-cog-info input/cog_info.txt
```
##### Input parameters
- This line will execute CSBFinder.jar with maximal heap size (memory) of 6GB.   
- The input genomes files is _chromosomal_genomes.fasta_ located in the input directory.  
- The quorum parameter is set to 50 (i.e., each CSB must have instances in at least 50 input genomes).  
- The number of allowed insertions in a CSB instance is one.  
- The export file name is _"Chromosomes"_
- The gene orthology input file is _cog_info.txt_ located in the input directory

##### Output
The output files will be now located in the output directory

> On a laptop computer with Intel model i7 processor and 8GB RAM, this execution should take less than 5 minutes

### Execution of CSBFinder on the Plasmid Dataset of 471 microbial genomes

#### User Interface 
Upload the dataset by clicking on the "Load Input model.genomes" button.

##### Input Parameters
- Click on the "Run" button, and a window will open.

- Set the parameters (e.g. Quorum 10, Insertions Allowed 1).

- Clicking on the "Run" button will start the computation of CSBs, this may take a few minutes.
 When the process is done, the results will be shown.

##### Output
You can export the resulting CSBs as a TXT or XLSX file.
You can also save the results in a session file, that can be opened using the user interface.

#### Command Line
``` 
java -jar CSBFinder-[version]-jar-with-dependencies.jar -in input/plasmid_genomes.fasta -q 10 -ins 1 -e plasmids 
-cog-info input/cog_info.txt
```
##### Input parameters 
- The input genomes files is _plasmid_genomes.fasta_ located in the input directory.  
- The quorum parameter is set to 10 (i.e., each CSB must have instances in at least 10 input genomes).  
- The number of allowed insertions in a CSB instance is one.  
- The export file name is _"plasmids"_
- The gene orthology input file is _cog_info.txt_ located in the input directory

##### Output
The output files will be now located in the output directory
> On a laptop computer with Intel model i7 processor and 8GB RAM, this execution should take a few seconds

<a name='ui_features'>User interface features</a>   
--------------------------------------
- Save - saving a session file (*.csb extension). This will save the current session, all filtered-out CSBs will be lost.
- Double-clicking on a CSB gene, aligns all other CSBs/instances according to this gene
- Re-clustering to families after filtration
- Re-computing CSB scores with different paramaters

##### Properties files
The following files are present in the installation directory
1. config.properties:   
Include paths to Session file, Taxonomy file, and Orthology info file. 
These files will be loaded automatically when launching the program

2. CSBFinder.vmoptions:   
Increase the memory (RAM) used by the program, by changing MEM in -Xmx[MEM] (e.g., -Xmx6g)


<a name='license'>License</a>
--------------

Licensed under the Apache License, Version 2.0. See more details in [LICENSE](https://github.com/dinasv/CSBFinder/blob/master/LICENSE) file


<a name='author'>Author</a>
--------------
Dina Svetlitsky

dina.svetlitsky@gmail.com

<a name='credit'>Credit</a>
--------------------
Thanks to Alexander Lerman for help with the initial user interface

 This code modified a Generalized Suffix Tree code from https://github.com/abahgat/suffixtree, copyright 2012 Alessandro Bahgat Shehata, licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0"> Apache License, Version 2.0 </a> 
 
 The User Interface uses icons made by
 <a href="https://www.flaticon.com/authors/smashicons" title="Smashicons">Smashicons</a>,
   <a href="https://www.flaticon.com/authors/kiranshastry" title="Kiranshastry">Kiranshastry</a>, 
  <a href="https://www.flaticon.com/authors/dave-gandy" title="Dave Gandy">Dave Gandy</a>, 
  <a href="https://www.freepik.com/" title="Freepik">Freepik</a>,
  <a href="https://www.flaticon.com/authors/icon-works" title="Icon Works">Icon Works</a>
  from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a>, licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a>

CSBFinder uses install4j - a multi-platform installer builder   
[![install4j](https://www.ej-technologies.com/images/product_banners/install4j_medium.png "install4j")](https://www.ej-technologies.com/products/install4j/overview.html)