import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;

import java.io.Serializable;
import java.util.*;

public class SparkTrain {

    public static class AccuracyComparator implements Serializable, Comparator<Tuple2<ParameterCombos.Combo, Double> >{
        @Override
        public int compare(Tuple2<ParameterCombos.Combo, Double> o1, Tuple2<ParameterCombos.Combo, Double> o2) {
            return Double.compare(o1._2(), o2._2());
        }
    }

    public static void main(String[] args) {
        // start Sparks and read a given input file
        String inputFile = "resources/mnist_train.csv";
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

        JavaRDD<NeuralNetwork.TrainingImage> trainingImageJavaRDD = jsc.parallelize(trainingData);
        JavaRDD<NeuralNetwork.TrainingImage> testDataJavaRDD = jsc.parallelize(testData);

        // now start a timer
        long startTime = System.currentTimeMillis();

        ParameterCombos paramCombos = new ParameterCombos();

        //Creating the RDD for parameterCombos
        JavaRDD<ParameterCombos.Combo> parameterCombosRDD = jsc.parallelize(paramCombos.combos);

        //Lambda function for parallel training and testing on different parameter combination
        JavaPairRDD<ParameterCombos.Combo, Double> accuracyRDD = parameterCombosRDD.mapToPair(combo -> {
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
            double testAccuracy = nnObj.evaluate(testData);
            Tuple2<ParameterCombos.Combo, Double> tuple = new Tuple2<>(combo, testAccuracy);
            return tuple;
        });

        //Collating results to find the combination that gave maximum accuracy
        AccuracyComparator accuracyComparator = new AccuracyComparator();
        Tuple2<ParameterCombos.Combo, Double> max = accuracyRDD.max(accuracyComparator);
        System.out.println( "Maximum Accuracy obtained = " + max._2.toString() );
        max._1.print();

        long endtime = System.currentTimeMillis();
        System.out.println( "execution time = " + (endtime-startTime));

    }
}
