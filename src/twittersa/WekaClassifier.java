package twittersa;

/**
 * 
 */

/**
 * @author Munir geden
 *
 */
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayesMultinomial;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.lazy.IBk;
import weka.classifiers.functions.NeuralNetwork;
import weka.classifiers.functions.SMO;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;

import org.apache.commons.io.FileUtils;

public class WekaClassifier {

	public static void getClassifierResults(String trainingDataPath,
			String testDataPath, String algorithm, int numberOfInputs) {
		try {
			int ngramSize = Integer.parseInt(FileHandler.readConfigValue(Constants.NGRAM_SIZE_CONFIG, "1"));
			String resultsPath = FileHandler.readConfigValue(Constants.REPORTS_PATH_CONFIG)
					+ File.separator
					+ "weka-"
					+ algorithm
					+ Constants.UNDERSCORE
					+ "ngram"
					+ Constants.UNDERSCORE
					+ numberOfInputs
					+ Constants.UNDERSCORE
					+ Integer.toString(ngramSize) + ".log";

			File resultsFile = new File(resultsPath);
			FileUtils.deleteQuietly(resultsFile);

			// Training Instances
			DataSource trainingSource = new DataSource(trainingDataPath);
			Instances trainingData = trainingSource.getDataSet();
			if (trainingData.classIndex() == -1) {
				trainingData.setClassIndex(trainingData.numAttributes() - 1);
			}

			// Test Instances
			DataSource testSource = new DataSource(testDataPath);
			Instances testData = testSource.getDataSet();
			if (testData.classIndex() == -1) {
				testData.setClassIndex(testData.numAttributes() - 1);
			}

			Remove rm = new Remove();
			rm.setAttributeIndices("1"); // remove 1st attribute since it is id
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(rm);
			if (algorithm.equals("j48")) {
				J48 j48 = new J48();
				j48.setUnpruned(true);
				fc.setClassifier(j48);
			} else if (algorithm.equals("mnb")) {
				NaiveBayesMultinomial mnb = new NaiveBayesMultinomial();
				fc.setClassifier(mnb);
			} else if (algorithm.equals("nb")) {
				NaiveBayes nb = new NaiveBayes();
				fc.setClassifier(nb);
			} else if (algorithm.equals("knn")) {
				IBk ibk = new IBk();
				ibk.setKNN(Integer.parseInt(FileHandler.readConfigValue(Constants.KNN_CONFIG,"3")));
				fc.setClassifier(ibk);
			} else if (algorithm.equals("svm")) {
				SMO svm = new SMO();
				fc.setClassifier(svm);
			}
			else if (algorithm.equals("nn")) {
				NeuralNetwork nn = new NeuralNetwork();
				fc.setClassifier(nn);
			}
			// train and make predictions
			fc.buildClassifier(trainingData);

			// evaluate classifier and print some statistics
			// String[] evalOptions = new String[2];
			// evalOptions[0] = "-t";
			// evalOptions[1] = "/some/where/somefile.arff";

			Evaluation eval = new Evaluation(trainingData);
			eval.evaluateModel(fc, testData);
			System.out.println(eval.toSummaryString("\nResults\n======\n",
					false));
			FileUtils.write(resultsFile,
					eval.toSummaryString("\nResults\n======\n\n", false), true);
			for (int i = 0; i < testData.numInstances(); i++) {
				double pred = fc.classifyInstance(testData.instance(i));
				// System.out.print("Name: " +
				// testData.instance(i).stringValue(0));
				FileUtils.write(resultsFile, "Name: "
						+ testData.instance(i).stringValue(0), true);
				// System.out.print(", Actual: " +
				// testData.classAttribute().value((int)
				// testData.instance(i).classValue()));
				FileUtils.write(
						resultsFile,
						", Actual: "
								+ testData.classAttribute()
										.value((int) testData.instance(i)
												.classValue()), true);
				// System.out.println(", Predicted: " +
				// testData.classAttribute().value((int) pred));
				FileUtils.write(resultsFile, ", Predicted: "
						+ testData.classAttribute().value((int) pred) + "\n",
						true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
