# CSBFinderCore

-   [Overview](#overview)
-   [Prerequisites](#prerequisites)
-   [Running CSBFinderCore](#running)
-   [Input files formats](#input)
-   [Output files](#output)
-   [Sample input files](#sample)
-   [License](#license)
-   [Author](#author)


<a name='overview'>Overview</a>
--------
CSBFinderCore is a standalone software tool, executed using command line, for the discovery, ranking and clustering of local colinear syntenic blocks (CSBs) identified in large genomic datasets.

The input is a set of genomes and parameters **_k_** (number of allowed insertions) and **_q_** (the quorum parameter). The genomes are modeled as strings of homologous genes identifires, where genes belonging to the same homology group have identical IDs. A homology group is regarded as a 'gene' throughout the text. In our model, a CSB is a patterns of genes that appears as a substring of at least one of the input genomes, and has instances in at least **_q_** genomes, where each instance may vary from the CSB by at most **_k_** gene insertions. The genomes are mined to identify patterns that qualify as CSBs. The discovered CSBs are ranked according to a probabilistic score and clustered to families according to their gene content similarity.

<a name='prerequisites'>Prerequisites</a>
--------

[Java Runtime Environment (JRE)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
8 or higher.

<a name='running'>Running CSBFinderCore</a>
--------
- Download the [latest release](https://github.com/dinasv/CSBFinderCore/releases) of CSBFinderCore.jar
- You can use the link https://github.com/dinasv/CSBFinderCore/releases/download/v[VERSION]/CSBFinderCore.jar for direct download. For example, in linux:
    ```
    wget https://github.com/dinasv/CSBFinderCore/releases/download/v0.2.1/CSBFinderCore.jar
    ```
    
- In the terminal (linux) or cmd (windows) type:
    ``` 
    java -jar CSBFinderCore.jar [options]
    ```
    > Note: When executing CSBFinderCore on a large dataset, add the option -Xmx8g (8g or more, depending on your RAM size).
    For example:
    ``` 
    java -Xmx8g -jar CSBFinderCore.jar [options]
    ```
    

> [Sample input files](#sample) are provided below

### Options:
Mandatory:
- **-in**   
    Input file name with genome sequences, located in a directory named 'input'. See [Input files formats](#input) for more details.
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
      Default: 2147483647 (longest possible CSB in the dataset)
- **--datasetname, -ds**   
      Dataset name   
      Default: dataset1
- **--patterns, -p**   
      Input CSB patterns file name, located in a directory named 'input'. See [Input files formats](#input) for more details.
- **-cog-info**   
      Gene families information file name, located in a directory named 'input'. See [Input files formats](#input) for more details.
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
      
<a name='input'>Input files formats</a>
--------------

All input files must be located in a directory named 'input', in the same directory as the jar file. 

### Input file containing input genome sequences
- Mandatory input file
- Its name is indicated using the "-in" option
- Contains all input genomes modeled as strings, where each character is a homology group ID that contains the corresponding gene (for example, COG ID)
- FASTA file

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
> Genes that does not belong to any homology group, should be marked as 'X'

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

### Input file with functional information of homology group IDs 
- Optional input file
- Its name is indicated using the "-cog-info" option
- Information from this file will be printed in the catalog output file, as the functional description of CSB genes 

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
### Input file containing CSB patterns
- Optional input file
- Its name is indicated using the "--patterns" or "-p" option
- FASTA file
- If this option is used, CSBs are no longer extracted from the input sequences. It specifies specific CSB patterns which the user is interested to find in the input sequences

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
Two output files will be written to a directory named "output"

- **File 1: A Catalog of CSBs**: An excel file containing the discovered CSBs named "Catalog_[dataset name]\_ins[number of allowed insertions]\_q[quorum parameter].xlsx"   
    This file contains three sheets: 
    1. Catalog    
        - Each line describes a single CSB
            - ID: unique CSB ID
            - Length: number of characters in the CSB
            - Score: a probabllistic ranking score, higher score indicates higher significance
            - Instance count: number of input sequences with an insatnce
            - Instance_Ratio: number of input sequences with an insatnce divided by the number of input sequences
            - Exact_Instance_Count: number of input sequences with an instance that doesn't contain insertions
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
    - Each instance that is present in a replicon (e.g. chromosome/plasmid), begins from a specific index and can have different lengths, depending on the number of insertions allowed in the instance
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

Download the following zip file and extract its content to the same location as CSBFinderCore.jar:

> [Sample_input_files.zip](https://github.com/dinasv/CSBFinderCore/raw/master/input/Sample_input_files.zip)

The above zip file contains three files, located inside a folder named 'input':
- plasmid_genomes.fasta   
    _Plasmid dataset_ - 471 genomes with at least one plasmid, chromosomes were removed.
- chromosomal_genomes.fasta    
    _Chromosomal dataset_ - 1,485 genomes with at least one chromosome, plasmids were removed.
- cog_info.txt   
    Functional information of homology groups

**Sample execution of CSBFinderCore using the _Plasmid dataset_**
``` 
java -jar CSBFinderCore.jar -in plasmid_genomes.fasta -q 10 -ins 1 -ds plasmids -cog-info cog_info.txt
```
> On a laptop computer with Intel Core i7 processor and 8GB RAM, this execution should take a few seconds

**Sample execution of CSBFinderCore using the _Chromosomal dataset_**
``` 
java -Xmx8g -jar CSBFinderCore.jar -in chromosomal_genomes.fasta -q 50 -ins 1 -ds chromosomes -cog-info cog_info.txt
```
> On a laptop computer with Intel Core i7 processor and 8GB RAM, this execution should take less than 5 minutes


<a name='license'>License</a>
--------------

Licensed under the Apache License, Version 2.0. See more details in [LICENSE](https://github.com/dinasv/CSBFinderCore/blob/master/LICENSE) file

<a name='author'>Author</a>
--------------
Dina Svetlitsky

dina.svetlitsky@gmail.com
