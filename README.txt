This README file is for Opinion Detection on Twitter Project as part of Information Retrieval and Data Mining Module at UCL
Submission date: 13/4/2015
Created by: Munir Geden, Olawole Oni, and Arwa Alamoudi

*************************************
Library folder and file requirements
*************************************
1. a "data" folder needs to be created where the dataset files and other generated data files should be placed
2. a "reports" folder needs to be created where the output results of the classifications would be placed.
3. "stanford_polarity_0_5.tsv" training dataset file should be placed in "data" folder created. Training dataset file can be downloaded from http://1drv.ms/1yohYkm
4. "stanford_validation_polarity.tsv" validation dataset file in that should be placed in "data" folder created. Validation dataset file can be downloaded from http://1drv.ms/1yohYkm
5. "tree-tagger" library with English tagset, for mac-unix version can be downloaded from http://1drv.ms/1yohYkm


*****************************************************************************
How to configure the applications?
*****************************************************************************
1. specify the "data" folder's path for the application by editing config.properties file or from the command line write: 
	-d DATA_FOLDER_ABSOLUTE_PATH
2. specify the "reports" folder's path for the application by editing config.properties file or from the command line write: 
	-r REPORT_FOLDER_ABSOLUTE_PATH
3. specify the training dataset file's path for the application by editing config.properties file or from the command write: 
	-t TRANING_FILE_ABSOLUTE_PATH
4. specify the validation dataset file's path for the application by editing config.properties file or from the command write: 
	-v VALIDATION_FILE_ABSOLUTE_PATH
You can play with the parameters from the configuration file(config.properties) based on your preference. 


******************************************************************************
How to collect tweets of user account by using TwitterAPI? (OPTIONAL)
******************************************************************************
1. specify TwitterAPI credentials in "twitter4j.properties" 

2. to collect tweets and print out them to a file from the command line enter: 
	-gt TWITTER_USER_NAME


******************************************************************************
How to take the small portion of the training dataset to try the application
with shorter processing time? (OPTIONAL BUT RECOMMENDED)
******************************************************************************
1. a training dataset can be reduced from the command line. i.e. to use 1/10 portion of a file: 
	-s FILE_ABSOLUTE_PATH -r 10


******************************************************************************
How to extract the distinctive ngrams and their class conditional 
probabilities from the training dataset?
******************************************************************************
1. to extract unigrams features from the command line enter: 
	-xn -n 1
	(ex:$jar TwitterSA.jar -cn -n 1)
	this will generate the following data files that we will be used for the following classification steps
	"conditional_probabilities_of_1-grams.tsv",
	"distinctive_1-grams_list_by_information_gain.tsv"
	"distinctive_1-grams_list_by_entropy.tsv"
	"distinctive_1-grams_list_by_salience.tsv"
	
2.	to extract bigrams features from the command line enter: 
	-xn -n 2
	this will generate the following data files that we will be used for the following classification steps
	"conditional_probabilities_of_2-grams.tsv",
	"distinctive_2-grams_list_by_information_gain.tsv"
	"distinctive_2-grams_list_by_entropy.tsv"
	"distinctive_2-grams_list_by_salience.tsv"
	
	
******************************************************************************
How to extract the postags class conditional probabilities from the training dataset?
******************************************************************************
1. to extract postags features from the command line enter: 
	-xp
	this will generate the following data files that we will be used for the following classification steps
	"conditional_probabilities_of_postags.tsv",


******************************************************************************
How to classify with custom Multinomial Naive Bayes classifier by using ngrams features?
******************************************************************************
1. to classify by using unigrams from the command line enter: 
	-cn -n1 UNIGRAM_CONDITINIONAL_PROBABILITIES_FILE_PATH 
	(ex:$jar TwitterSA.jar -cn -n1 /Users/***/Data/conditional_probabilities_of_1-grams.tsv)
	this will generate output results in the reports folder
	
2. to classify by using bigrams from the command line enter: 
	-cn -n2 BIGRAM_CONDITINIONAL_PROBABILITIES_FILE_PATH 
	(ex:$jar TwitterSA.jar -cn -n2 /Users/***/Data/conditional_probabilities_of_2-grams.tsv)
	this will generate output results in the reports folder
		
3. to classify by combining unigram and bigrams together from the command line enter: 
	-cn -n1 UNIGRAM_CONDITINIONAL_PROBABILITIES_FILE_PATH -n2 BIGRAM_CONDITINIONAL_PROBABILITIES_FILE_PATH 
	(ex:$jar TwitterSA.jar -cn -n1 /Users/***/Data/conditional_probabilities_of_1-grams.tsv -n2 /Users/***/Data/conditional_probabilities_of_2-grams.tsv)
	this will generate output results in the reports folder

		
******************************************************************************
How to classify with custom Multinomial Naive Bayes classifier by just using postags?
******************************************************************************
1. to classify by using just postag from the command line enter: 
	-cp -p POSTAG_CONDITINIONAL_PROBABILITIES_FILE_PATH 
	(ex:$jar TwitterSA.jar -cp -p /Users/***/Data/conditional_probabilities_of_postags.tsv)
	this will generate output results in the reports folder
	
	
******************************************************************************
How to classify with custom Multinomial Naive Bayes classifier by combining postags and ngrams?
******************************************************************************
1. to classify by using unigrams and postags features together from the command line enter: 
	-cnp -p POSTAG_CONDITINIONAL_PROBABILITIES_FILE_PATH -n1 UNIGRAM_CONDITINIONAL_PROBABILITIES_FILE_PATH -l LAMBDA_VALUE
	(ex:$jar TwitterSA.jar -cnp -p /Users/***/Data/conditional_probabilities_of_postags.tsv  -n1 /Users/***/Data/conditional_probabilities_of_1-grams.tsv -l 0.45)
	this will generate output results in the reports folder

2. to classify by using bigrams and postags features together from the command line enter: 
	-cnp -p POSTAG_CONDITINIONAL_PROBABILITIES_FILE_PATH -n2 BIGRAM_CONDITINIONAL_PROBABILITIES_FILE_PATH -l LAMBDA_VALUE
	(ex:$jar TwitterSA.jar -cnp -p /Users/***/Data/conditional_probabilities_of_postags.tsv  -n2 /Users/***/Data/conditional_probabilities_of_2-grams.tsv -l 0.45)
	this will generate output results in the reports folder


******************************************************************************
How to generate Weka data files for Weka Classifiers?
******************************************************************************
1. to generate Weka data *.arff files of the most distinctive unigrams (default #of input size=1000) from the command line enter: 
	-gw -n 1 -d DISTINCTIVE_UNIGRAMS_LIST_FILE_PATH
	(ex:$jar TwitterSA.jar -gw -n 1 -d /Users/***/Data/distinctive_1-grams_list_by_information_gain.tsv)
	this will generate the following training and validation data files that we will be used for the following Weka classifications
	"train_ngram_1000_1.arff"
	"test_ngram_1000_1.arff"

******************************************************************************
How to classify wih Weka classifiers?
******************************************************************************
1. to classify Weka for the given algorithm with the given weka training and validation *.arff files
   (mnb:multinomial naive bayes, 
    nb:naive bayes,
    knn:k-nearest neighbours(k value configurable from configuration.properties), 
    j48:decision trees, 
    svm:support vector machines) from the command line enter: 
	-cw ALGORITHM -wt TRANING_ARFF_FILE_PATH -wv VALIDATION_ARFF_FILE_PATH
	(ex:$jar TwitterSA.jar -cw mnb -wt /Users/***/Data/train_ngram_1000_1.arff -wv /Users/***/Data/test_ngram_1000_1.arff)
	this will generate output results in the reports folder

***************************************
Access to source code
***************************************
Link to project's repository:
https://bitbucket.org/msgeden/twittersa

For clonning the repository:
git clone https://bitbucket.org/msgeden/twittersa.git
