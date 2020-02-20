import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaDoubleRDD;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.util.ArrayList;

public class SparkTrainOptimalCombo {

    public static void main(String[] args) {
        // start Sparks and read a given input file
        SparkConf conf = new SparkConf( ).setAppName( "Neural Networks with Spark" );
        JavaSparkContext jsc = new JavaSparkContext( conf );

        String trainingDataFile = "mnist_train.csv";
        ArrayList<NeuralNetwork.TrainingImage> trainingData = (
                NeuralNetwork.readCSV(trainingDataFile)
        );

        String testDataFile = "mnist_test.csv";
        ArrayList<NeuralNetwork.TrainingImage> testData = (
                NeuralNetwork.readCSV(testDataFile)
        );

        JavaRDD<NeuralNetwork.TrainingImage> testDataJavaRDD = jsc.parallelize(testData);

        // now start a timer
        long startTime = System.currentTimeMillis();

        ParameterCombos paramCombos = new ParameterCombos();

        /******* MAX ACCURACY PARAMETERS ********
        learningRate = 0.01
        numIterations = 3
        numHiddenLayers = 2
        hiddenLayerSize = 200
        */

        ParameterCombos.Combo combo = paramCombos.new Combo(0.01,3,2,200);


        ArrayList<Integer> structure = new ArrayList<>();
        structure.add(784);  // Add input layer
        for (
                int layerIndex = 1;
                layerIndex <= combo.numHiddenLayers;
                layerIndex++
        ) {
            structure.add(combo.hiddenLayerSize);  // Add another hidden layer
        }
        structure.add(10);  // Add the output layer

        NeuralNetwork nnObj = new NeuralNetwork(structure, combo.learningRate);
        nnObj.train(trainingData, combo.numIterations);
        JavaDoubleRDD testAccuracy = testDataJavaRDD.mapToDouble(trainingImage -> {
            ArrayList<NeuralNetwork.TrainingImage> list = new ArrayList<NeuralNetwork.TrainingImage>();
            list.add(trainingImage);
            return nnObj.evaluate(list);
        });

        Double sum = testAccuracy.sum();
        System.out.println("Number of Test entries: " + testData.size());
        System.out.println("Number of Correct Predictions: " + sum.toString() );

        long endtime = System.currentTimeMillis();
        System.out.println( "execution time = " + (endtime-startTime)/1000);

    }
}
