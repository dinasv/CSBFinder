# CSBFinder

-   [Overview](#overview)
-   [Prerequisites](#prerequisites)
-   [Running CSBFinder](#running)
-   [Input files formats](#input)
-   [Output files](#output)


<a name='overview'>Overview</a>
--------
CSBFinder is a standalone software tool, executed using command line, for the discovery, ranking and clustering of colinear synteny blocks (CSBs) identified in large genomic datasets.

The input is a set of genomes and parameters **_k_** (number of allowed insertions) and **_q_** (the quorum parameter). The genomes are modeled as strings of homologous gene family IDs, where genes belonging to the same homology family have identical IDs. The genomes are mined to identify substrings that qualify as CSBs: patterns of gene-family IDs that appear as an exact substring of at least one of the input genomes, and have instances in at least **_q_** genomes, where each instance may vary from the CSB pattern by at most **_k_** gene insertions. The discovered CSBs are ranked according to a probabilistic score and clustered to families according to their gene content similarity.

<a name='prerequisites'>Prerequisites</a>
--------

[Java Runtime Environment (JRE)](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
8 or up.

<a name='running'>Running CSBFinder</a>
--------

In the terminal (linux) or cmd (windows) type:
``` 
java -jar CSBFinder.jar [options]
```
> Note: When executing CSBFinder on a large dataset, add the option -Xmx8g (8g or more, depending on your RAM size).
For example:
``` 
java -Xmx8g -jar CSBFinder.jar [options]
```

### Options:
Mandatory:
- **-in**   
    Input file name, located in a directory named 'input'. See [Input files formats](#input) for more details.
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
- Contains all input genomes modeled as strings, where each character is a gene family ID (for example, COG ID)
- FASTA file

This file should use the following format:
```
>[genome name] | [ replicon name (e.g. plasmid or chromosome id)]
[gene family ID] TAB [Strand (+ or -)] TAB [you can add additional information]
[gene family ID] TAB [Strand (+ or -)] TAB [you can add additional information] 
[gene family ID] TAB [Strand (+ or -)] TAB [you can add additional information] 
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
> Genes that are not annotated by a gene family ID, should be marked as 'X'

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

### Input file containing gene family ID information
- Optional input file
- Its name is indicated using the "-cog-info" option
- Contains information regarding gene family IDs, this information will be printed in the output files anc an help decipher the function of a CSB 

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
[gene family IDs seperated by hyphens]
>[unique pattern ID, must be an integer]
[gene family IDs seperated by hyphens]
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

- **A Catalog of CSBs**: An excel file containing the discovered CSBs named "Catalog_[dataset name]\_ins[number of allowed insertions]\_q[quorum parameter].xlsx"   
    This file contains three sheets: 
    1. Catalog    
        - Each line describes a single CSB
            - ID: unique CSB ID
            - Length: number of characters in the CSB
            - Score: a probabllistic ranking score, higher score indicates higher significance
            - Instance count: number of input sequences with an insatnce
            - Instance_Ratio: number of input sequences with an insatnce divided by the number of input sequences
            - Exact_Instance_Count: number of input sequences with an instance that doesn't contain insertions
            - CSB: a sequence of gene family IDs 
            - Main_Category: if functional category was provided in the -cog-info file, this column contains the functional category of                             the majority of CSB gene families
            - Family_ID: CSBs with similar gene content will belong to the same family, indicated by a postive integer
    2. Filtered CSBs
        - This sheet contains only the top scoring CSB from each family
    3. CSBs description
        - Information about gene family IDs of each CSB

- **Information of CSB instances**: A text FASTA file in the same name as the catalog file, only with the suffix "\_instances"

    This file has the following format:
    
    ```
    >[CSB ID]	[CSB gene family IDs]
     [genome name] TAB [replicon name]_[start index]_length_[instance length]
     [genome name] TAB [replicon name]_[start index]_length_[instance length]
     ...
     >[CSB ID]	[CSB gene family IDs]
     [genome name] TAB [replicon name]_[start index]_length_[instance length]
     [genome name] TAB [replicon name]_[start index]_length_[instance length]
     ...
    ```
    #### Example
    ```
    >20880	COG4264-COG3486-
    Halomicrobium_mukohataei_DSM_12286_uid59107	NC_013201_97_length_3
    Sinorhizobium_medicae_WSM419_uid58549	NC_009620_521_length_3	NC_009620_672_length_3	NC_009621_768_length_3	NC_009620_639_length_3
    Klebsiella_pneumoniae_NTUH_K2044_uid59073	NC_006625_257_length_2
    Cronobacter_turicensis_z3032_uid40821	NC_013283_105_length_2
    ```
<a name='sample'>Sample input files</a>   
--------------------------------------

Download the following file and extract it to a directory named "input" in the same location of CSBFinder.jar: [sample_input.zip](https://github.com/dinasv/CSBFinder/raw/master/input/sample_input.zip)

It contains two datasets:
 - Chromosomal dataset - 1,485 genomes with at least one chromosome, plasmids were removed.
 - Plasmid dataset - 471 genomes with at least one plasmid, chromosomes were removed.
 
It also contains cog_info.txt file with gene family information

**Sample execution of CSBFinder using the _Plasmid dataset_**
``` 
java -jar CSBFinder.jar -in plasmid_genomes.fasta -q 10 -ins 1 -ds plasmids -cog-info cog_info.txt
```
**Sample execution of CSBFinder using the _Chromosomal dataset_**
``` 
java -Xmx8g -jar CSBFinder.jar -in chromosomal_genomes.fasta -q 50 -ins 1 -ds chromosomes -cog-info cog_info.txt
```


