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
-   [Output files](#output)
-   [Sample input files](#sample)
-   [License](#license)
-   [Authors](#author)


<a name='overview'>Overview</a>
--------
CSBFinder is a standalone Desktop java application with a graphical user interface, 
that can also be executed via command line.

CSBFinder implements a novel methodology for the discovery, ranking, and taxonomic distribution analysis of 
colinear syntenic blocks (**CSBs**) - groups of genes that are consistently located close to each other, 
in the same order, across a wide range of taxa.
CSBFinder incorporates an efficient algorithm that identifies CSBs in large genomic datasets. 
The discovered CSBs are ranked according to a probabilistic score and clustered to families according to their gene 
content similarity.

### Workflow Description
The workflow of CSBFinder is given in the figure below.    

**(A)** The input to the workflow is a dataset of input genomes, where each genome is a sequence of gene orthology group
identifiers (genes belonging to the same orthology group have identical IDs). Each input genome is segmented to 'directons' - a directon is a maximal sequence of consecutive 
genes located on the same DNA strand. The gene order in each directon follows the order in which the genes are
transcribed in each genome (on either the forward or the reverse strand of the DNA).    

Additional input consists of user-specified parameters **_k_** (number of allowed insertions) and and **_q_** 
(the quorum parameter). In our formulation, a CSB is a pattern that
appears as a substring of at least one of the input genomes, and has instances in at least **_q_** of the input
genomes, where each instance may vary from the CSB pattern by at most **_k_** gene insertions. 
The workflow also accepts as input (optional) parameters specifying the minimal and maximal length of the sought CSBs.

**(B)** The genomes are mined to identify all patterns that qualify as CSBs according to the user-specified parameters
mentioned above}. 

**(C)** All discovered CSBs are ranked according to a probabilistic score. 

**(D)** The CSBs are clustered to families according to their gene content similarity, and the rank of a family is
determined by the score of its highest scoring CSB.

![CSBFinder workflow](https://github.com/dinasv/CSBFinder/blob/master/images/workflow.png "Workflow")

<a name='prerequisites'>Prerequisites</a>
--------

[Java Runtime Environment (JRE)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
8 or higher.

<a name='running'>Running CSBFinder</a>
--------
### <a name='download'>Download the JAR file</a>
- Download the [latest release](https://github.com/dinasv/CSBFinder/releases) of CSBFinder.jar
- You can use the link https://github.com/dinasv/CSBFinder/releases/download/v[VERSION]/CSBFinder.jar for direct download. For example, in linux:
    ```
    wget https://github.com/dinasv/CSBFinder/releases/download/v0.3.1/CSBFinder.jar
    ```
CSBFinder has a user interface, but can be executed via the command line as well. 

### <a name='ui'>Running CSBFinder via User Interface</a>
Just double click on the CSBFinder.jar file

> Note: If you are going to use a very large input dataset, execute CSBFinder via command line without arguments,
 and add the Java option -Xmx[maximum size of the heap]  
 For example:
 ``` 
     java -Xmx6g -jar CSBFinder.jar
 ```
 > It is recommended to use at least 6GB for a large dataset. You can specify a higher number, 
 depending on you RAM size.  
 
1. Upload your input genomes file, by clicking on "Load Input Genomes" in the top left. If your dataset is large, this
make take a few minutes.
    > [Sample input files](#sample) are provided below

2. The "Run" button should be enabled. Click on this button to set the parameters.
 
3. A dialog appears. Hover over the question mark icon next to each parameter, for an explanation of each parameter. 
After setting the parameters, click on "Run". This can take a few minutes, depending on the size of the dataset and 
on the parameters specified. 

4. After the process is done, the lower panel will contain all the discovered CSBs. There is also an option to view 
only top scoring CSBs (the highest scoring CSB from each family) by clicking on the appropriate tab.  
Selecting a CSB will show all its instances in the different genomes, in the top panel.  
If a gene orthology group information file was loaded, the panel in the lower right (legend) will show functional 
information regarding the genes of the selected CSB, and genes that appear as insertions in the instances of the 
selected CSB.  

The following screenshot shows an example of a selected CSB and its instances in the different genomes. The results 
were obtained by running CSBFinder on a dataset of ~1,500 prokaryotic genomes containing only chromosomes. The quorum 
was set to 50 and the number of allowed insertions is 3.

![CSBFinder results screenshot](https://github.com/dinasv/CSBFinder/blob/master/images/screenshot_results_example.PNG "CSBFinder Screenshot")


### <a name='cmd'>Running CSBFinder via Command Line </a>
- In the terminal (linux) or cmd (windows) type:
    ``` 
    java -jar CSBFinder.jar [options]
    ```
    > Note: If your input dataset is very large, add the argument -Xmx6g (6g might be enough, but you can specify a 
    higher number, depending on your RAM size).
    For example:
    ``` 
    java -Xmx6g -jar CSBFinder.jar [options]
    ```
    > Note: When running CSBFinder without command line arguments, the user interface will be launched.
    

> [Sample input files](#sample) are provided below

#### Options:
Mandatory:
- **-in** INPUT_DATASET_FILE_NAME    
    Input file name with genome sequences, located in a directory named 'input'. See [Input files formats](#input_dataset) for more details.
- **-q** QUORUM     
      The quorum parameter. Minimal number of input sequences that must contain a CSB instance.   
      Default: 1
      Min Value: 1
      Max Value: Total input sequences
      
Optional:     
- **-ins** INSERTIONS    
      Maximal number of insertions allowed in a CSB instance.
      Default: 0
- **-qexact** EXACT_QUORUM    
      Quorum without insertions. Minimal number of input sequences that must contain a CSB instance with no insertions. 
      Default: 1
      Min Value: 1
      Max Value: Total input sequences
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
- **--datasetname** DATASET_NAME ,**-ds** DATASET_NAME       
      DATASET_NAME will be reflected in the output file name.   
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
- **-mult_count**    
      If this option is provided, CSB count indicates the total number of instances (could be several instances in the same input sequence), rather than the number of input sequences with an instance.    
- **--threshold** THRESHOLD, **-t** THRESHOLD   
      Threshold for family clustering      
      Default: 0.8    
      Min Value: 0   
      Max Value: 1    
- **-out** OUTPUT_FILE_TYPE    
      Output file type   
      Default: XLSX   
      Possible Values: [TXT, XLSX]
- **-clust-by** CLUSTER_BY    
      Cluster CSBs to families by: 'score' or 'length'  
      Default: SCORE   
      Possible Values: [LENGTH, SCORE]
- , **-h**, **--help**     
      Show usage
      
<a name='input'>Input files formats</a>
--------------

When running using command line, all input files must be located in a directory named 'input', in the same directory 
as the jar file. 

### <a name='input_dataset'> Input file containing input genome sequences </a>

A text file containing all input genomes modeled as strings, where each character is an orthology group ID that was assigned to a corresponding gene (for example, COG ID)
- This is a mandatory input file
- The path to this file is provided in:
    - User Interface: Load this file using the "Load Input Genomes" button
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

You can annotate genes by any orthologous group identifiers. The IDs can be numbers or symbols, the only restriction it that
each orthology group will have a unique ID.

##### Examples
1. The [STRING](https://string-db.org/cgi/download.pl?sessionId=Dmc2Jkurdd3b) database contains 
[COG](https://www.ncbi.nlm.nih.gov/COG/) and [NOG](http://eggnogdb.embl.de/) annotations of many publicly 
available genomes
2. Newly sequenced genomes can be mapped to known orthology groups such as:
    - COGs using [CDD](https://www.ncbi.nlm.nih.gov/Structure/bwrpsb/bwrpsb.cgi)
    - NOGs using [eggNOG mapper](http://eggnogdb.embl.de/#/app/emapper)
3. A tool such as [Proteinortho](https://www.bioinf.uni-leipzig.de/Software/proteinortho/) detects orthologous genes within different species. 

### <a name='cog_info'>Input file with functional information of gene orthology group IDs </a>
- This is an optional input file
- The path to this file is provided in:
    - User Interface: In the dialog opened after clicking on the "Run" button
    - Command Line: "-cog-info" option
#### COG information input file
If you are using [COGs](https://www.ncbi.nlm.nih.gov/COG/) (Cluster of Orthologous Genes) as your 
gene orthology group identifiers, you can use the file _cog_info.txt_ provided in the 
[input](https://github.com/dinasv/CSBFinder/tree/master/input) directory.

The functional description of gene orthology groups will appear in the legend (User Interface) 
or in the output catalog file (when clicking on the "Save" button in the User Interface, or when 
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
    - User Interface: In the dialog opened after clicking on the "Run" button
    - Command Line: "--patterns" or "-p" option

This file should use the following format:
```
>[unique pattern ID, must be an integer]
[homology group IDs seperated by hyphens]
>[unique pattern ID, must be an integer]
[homology group IDs seperated by hyphens]
```

#### Example
```
>1
COG3736-COG3504-COG2948-COG0630
>564654
COG3736-COG3504-COG2948
....
```

<a name='output'>Output files</a>
--------------
After clicking on the "Save" button in the User Interface, or after execution via Command Line: two output files will be written to a directory named "output"

- **File 1: A Catalog of CSBs**: An excel file containing the discovered CSBs named "Catalog_[dataset name]\_ins[number of allowed insertions]\_q[quorum parameter].xlsx"   
    This file contains three sheets: 
    1. Catalog    
        - Each line describes a single CSB
            - ID: unique CSB ID
            - Length: number of characters in the CSB
            - Score: a probabilistic ranking score, higher score indicates higher significance
            - Instance count: number of input sequences with an instance
            - Instance_Ratio: number of input sequences with an instance divided by the number of input sequences
            - Exact_Instance_Count: number of input sequences with an instance that does'nt contain insertions
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
    - If a start index of an instance is *i*, an instance on the minus strand starts from index *i* and ends on index *i-(instance length)*
    - The index of the first gene in a replicon is 0 
    
    This file has the following format:
    
    ```
    >[CSB ID] TAB [CSB genes]
     [genome name] TAB [replicon name]|[[instance start index (inclusive),instance end index (exclusive)]]
     [genome name] TAB [replicon name]|[[instance start index (inclusive),instance end index (exclusive)]]

     ...
     >[CSB ID] TAB	[CSB genes]
     [genome name] TAB [replicon name]|[[instance start index (inclusive),instance end index (exclusive)]]
     [genome name] TAB [replic     [genome name] TAB [replicon name]|[[instance start index (inclusive),instance end index (exclusive)]]
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

Download the following zip file and extract its content to the same location as CSBFinder.jar:

> [Sample_input_files.zip](https://github.com/dinasv/CSBFinder/raw/master/input/Sample_input_files.zip)

The above zip file contains three files, located inside a folder named 'input':
- plasmid_genomes.fasta   
    _Plasmid dataset_ - 471 prokaryotic genomes with at least one plasmid, chromosomes were removed.
- chromosomal_genomes.fasta    
    _Chromosomal dataset_ - 1,485 prokaryotic genomes with at least one chromosome, plasmids were removed.
    > Important: this is a huge dataset. See instructions below, how to run CSBFinder with a large dataset
- cog_info.txt   
    Functional information of gene orthology groups

### Execution of CSBFinder on the Chromosomal Dataset of 1,485 prokaryotic genomes
  
#### User Interface

The file _chromosomal_genomes.fasta_ contains ~1,500 genomes, hence CSBFinder needs more heap memory.
When uploading a large dataset.
You should execute CSBFinder.jar via Command Line in the following way:
``` 
java -Xmx6g -jar CSBFinder.jar
```
This command will launch the User Interface with more available memory (6GB), you can specify a 
higher number depending on you RAM size.
It may take a few minuted to load the input file.

Upload the dataset by clicking on the "Load Input Genomes" button.

##### Input Parameters
Click on the "Run" button, and a dialog will open.

The screenshot below shows a typical parameter setting.
- The quorum parameter is set to 50 (i.e., each CSB must have instances in at least 50 input genomes).  
- The number of allowed insertions in a CSB instance is one.  
- The dataset name is _"Chromosomes"_
- The gene orthology input file is the provided _cog_info.txt_ 

![CSBFinder chromosomal dataset parames screenshot](https://github.com/dinasv/CSBFinder/blob/master/images/screenshot_chromosmoal_dataset_params.PNG "CSBFinder Chromosomal dataset parameters screenshot")

Clicking on the "Run" button will launch the computation of CSBs, this may take a few minutes.
 When the process is done, the results will be shown:

![CSBFinder chromosomal dataset parames screenshot](https://github.com/dinasv/CSBFinder/blob/master/images/screenshot_chromosmoal_dataset_results.PNG "CSBFinder Chromosomal dataset results screenshot")

##### Output
Click on the "Save" button an  choose the required output format. The resulting files will be saved in the output 
directory, in the same location as CSBFinder.jar

#### Command Line
``` 
java -Xmx6g -jar CSBFinder.jar -in chromosomal_genomes.fasta -q 50 -ins 1 -ds Chromosomes -cog-info cog_info.txt
```
##### Input parameters
- This line will execute CSBFinder.jar with maximal heap size (memory) of 6GB.   
- The input genomes files is _chromosomal_genomes.fasta_ located in the input directory.  
- The quorum parameter is set to 50 (i.e., each CSB must have instances in at least 50 input genomes).  
- The number of allowed insertions in a CSB instance is one.  
- The dataset name is _"Chromosomes"_
- The gene orthology input file is _cog_info.txt_ located in the input directory

##### Output
The output files will be now located in the output directory

> On a laptop computer with Intel Core i7 processor and 8GB RAM, this execution should take less than 5 minutes

### Execution of CSBFinder on the Plasmid Dataset of 471 prokaryotic genomes

#### User Interface 
Upload the dataset by clicking on the "Load Input Genomes" button.

##### Input Parameters
Click on the "Run" button, and a dialog will open.

The screenshot below shows a typical parameter setting.
- The quorum parameter is set to 10 (i.e., each CSB must have instances in at least 50 input genomes).  
- The number of allowed insertions in a CSB instance is one.  
- The dataset name is _"Plasmids"_
- The gene orthology input file is the provided _cog_info.txt_ 

![CSBFinder plasmid dataset parames screenshot](https://github.com/dinasv/CSBFinder/blob/master/images/screenshot_plasmid_dataset_params.png "CSBFinder Plasmid dataset parameters screenshot")

Clicking on the "Run" button will launch the computation of CSBs, this may take a few minutes.
 When the process is done, the results will be shown:

![CSBFinder plasmid dataset parames screenshot](https://github.com/dinasv/CSBFinder/blob/master/images/screenshot_plasmid_dataset_results.png "CSBFinder Plasmid dataset results screenshot")

##### Output
Click on the "Save" button an  choose the required output format. The resulting files will be saved in the output 
directory, in the same location as CSBFinder.jar

#### Command Line
``` 
java -jar CSBFinder.jar -in plasmid_genomes.fasta -q 10 -ins 1 -ds plasmids -cog-info cog_info.txt
```
##### Input parameters 
- The input genomes files is _plasmid_genomes.fasta_ located in the input directory.  
- The quorum parameter is set to 10 (i.e., each CSB must have instances in at least 10 input genomes).  
- The number of allowed insertions in a CSB instance is one.  
- The dataset name is _"plasmids"_
- The gene orthology input file is _cog_info.txt_ located in the input directory

##### Output
The output files will be now located in the output directory
> On a laptop computer with Intel Core i7 processor and 8GB RAM, this execution should take a few seconds



<a name='license'>License</a>
--------------

Licensed under the Apache License, Version 2.0. See more details in [LICENSE](https://github.com/dinasv/CSBFinder/blob/master/LICENSE) file

<a name='credit'>Credit</a>
--------------------
 This code modified a Generalized Suffix Tree code from https://github.com/abahgat/suffixtree, copyright 2012 Alessandro Bahgat Shehata, licensed under the <a href="http://www.apache.org/licenses/LICENSE-2.0"> Apache License, Version 2.0 </a> 
 
 The User Interface uses an icon made by <a href="https://www.flaticon.com/authors/roundicons" title="Roundicons">Roundicons</a> from <a href="https://www.flaticon.com/" title="Flaticon">www.flaticon.com</a>, licensed by <a href="http://creativecommons.org/licenses/by/3.0/" title="Creative Commons BY 3.0" target="_blank">CC 3.0 BY</a>

<a name='author'>Authors</a>
--------------
Dina Svetlitsky

dina.svetlitsky@gmail.com

User Interface:  
Alexander Lerman
