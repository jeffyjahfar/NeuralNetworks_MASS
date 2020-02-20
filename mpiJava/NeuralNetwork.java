// I/O imports
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

// Computation
import java.lang.Math;

// Data structures
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.StringTokenizer;


public class NeuralNetwork {

    private static final int TRAINING_BATCH_SIZE = 5000;

    ArrayList<Integer> structure;
    double learningRate;
    ArrayList<ArrayList<ArrayList<Double>>> weightMatrices;

    public static void main(String[] args) {
        ArrayList<Integer> structure = new ArrayList<>();
        structure.add(784);
        structure.add(80);
        structure.add(80);
        structure.add(10);
        double learningRate = 0.01;

        NeuralNetwork nnObj = new NeuralNetwork(structure, learningRate);

        String dataDir = System.getProperty("user.home") + "/data/";

        String trainingDataFile = dataDir + "mnist_train_subset.csv";
        ArrayList<TrainingImage> trainingData = readCSV(trainingDataFile);

        long startTime = System.currentTimeMillis();

        nnObj.train(trainingData, 5);

        long stopTime = System.currentTimeMillis();
        // Convert elapsed time to seconds
        long elapsedTime = (stopTime - startTime) / 1000;

        System.out.println("Training time: " + elapsedTime + " seconds.");

        ArrayList<Double> result = nnObj.run(trainingData.get(10).pixelVector);

        int expected = trainingData.get(10).labelVector.indexOf(
            Collections.max(trainingData.get(10).labelVector)
        );
        int actual = result.indexOf(Collections.max(result));
        System.out.println("expected = " + expected + ", actual = " + actual);

        double trainAccuracy = nnObj.evaluate(trainingData);
        System.out.println("Training accuracy: " + trainAccuracy);

        String testDataFile = dataDir + "mnist_test.csv";
        ArrayList<TrainingImage> testData = readCSV(testDataFile);

        double testAccuracy = nnObj.evaluate(testData);
        System.out.println("Testing accuracy: " + testAccuracy);

        // Should have the same accuracy:
        NeuralNetwork nnObj2 = new NeuralNetwork(structure, learningRate);
        nnObj2.train(trainingData, 5);
        testAccuracy = nnObj2.evaluate(testData);
        System.out.println("Testing accuracy: " + testAccuracy);
    }

    /**
    * Default constructor.
    */
    public NeuralNetwork() {}

    /**
    * The constructor of the ``NeuralNetwork`` class.
    * @param structure  A list of integers that represent of the structure of
    *     the network, i.e. the size of each layer, starting with the input
    *     layer, then the hidden layers, and the output layer.
    * @param learningRate  A floating point number that defines the rate at
    *     which the network "learns", i.e. adjusts it's weights.
    */
    public NeuralNetwork(ArrayList<Integer> structure, double learningRate) {
        this.structure = structure;
        this.learningRate = learningRate;

        createWeightMatrices();
    }

    /**
    * Initialize the weight matrices with random values.
    */
    public void createWeightMatrices() {
        int numLayers = structure.size();

        Random randomGenerator = new Random(1024);

        ArrayList<ArrayList<ArrayList<Double>>> weightMatrices = (
            new ArrayList<ArrayList<ArrayList<Double>>>()
        );

        // Randomize initial weights;
        for (
            int boundaryIndex = 0;
            boundaryIndex < numLayers - 1;
            boundaryIndex++
        ) {
            int neuronsIn = structure.get(boundaryIndex);
            int neuronsOut = structure.get(boundaryIndex + 1);

            ArrayList<ArrayList<Double>> weightMatrix = (
                new ArrayList<ArrayList<Double>>()
            );
            for (int row = 0; row < neuronsOut; row++) {
                ArrayList<Double> weightRow = new ArrayList<Double>();
                for (int col = 0; col < neuronsIn; col++) {
                    double weight = randomGenerator.nextDouble() - 0.5;
                    weightRow.add(weight);
                }
                weightMatrix.add(weightRow);
            }
            weightMatrices.add(weightMatrix);
        }

        String matrixSizesMsg = "matrix sizes:";
        for (int i = 0; i < weightMatrices.size(); i++) {
            int mSize = weightMatrices.get(i).size();
            int nSize = weightMatrices.get(i).get(0).size();
            matrixSizesMsg += ("[" + mSize + ", " + nSize + "]");
            if (i < weightMatrices.size() - 1) {
                matrixSizesMsg += ", ";
            }
        }

        this.weightMatrices = weightMatrices;
    }

    public void train(
        ArrayList<TrainingImage> trainingData,
        int iterations
    ) {
        long startTime = System.currentTimeMillis();

        Random randomGenerator = new Random(1024);

        ArrayList<TrainingImage> trainingDataCopy = (
            new ArrayList<TrainingImage>(trainingData)
        );
        Collections.shuffle(trainingDataCopy, randomGenerator);

        for (int iteration = 0; iteration < iterations; iteration++) {

            int fullSetSize = trainingDataCopy.size();
            int[] batchIndices = trainingBatchIndices(iteration, fullSetSize);
            ArrayList<TrainingImage> trainBatchData = (
                new ArrayList<TrainingImage>(
                    trainingDataCopy.subList(
                        batchIndices[0],
                        batchIndices[1]
                    )
                )
            );

            for (
                int imageIndex = 0;
                imageIndex < trainBatchData.size();
                imageIndex++
            ) {
                TrainingImage imageData = trainBatchData.get(imageIndex);
                trainSingle(imageData.labelVector, imageData.pixelVector);
            }
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = (stopTime - startTime) / 1000; // convert to seconds
    }

    public void trainSingle(
        ArrayList<Double> labelVector,
        ArrayList<Double> pixelVector
    ) {
        int numLayers = structure.size();
        ArrayList<ArrayList<Double>> actVectors = (
            new ArrayList<ArrayList<Double>>()
        );
        actVectors.add(pixelVector);

        ArrayList<ArrayList<Double>> zVectors = (
            new ArrayList<ArrayList<Double>>()
        );

        for (int layerIndex = 1; layerIndex < numLayers; layerIndex++) {
            ArrayList<Double> actVector = actVectors.get(layerIndex - 1);
            ArrayList<ArrayList<Double>> weightMatrix = (
                weightMatrices.get(layerIndex - 1)
            );

            ArrayList<Double> zVector = new ArrayList<Double>();
            ArrayList<Double> outVector = new ArrayList<Double>();

            int neuronsOut = weightMatrix.size();
            int neuronsIn = weightMatrix.get(0).size();

            for (int outIndex = 0; outIndex < neuronsOut; outIndex++) {
                double zVal = 0;
                for (int inIndex = 0; inIndex < neuronsIn; inIndex++) {
                    zVal += (
                        weightMatrix.get(outIndex).get(inIndex)
                        * actVector.get(inIndex)
                    );
                }
                zVector.add(zVal);

                double outVal = sigmoid(zVal);
                outVector.add(outVal);
            }

            zVectors.add(zVector);
            actVectors.add(outVector);
        }

        int layerIndex = numLayers - 1;
        ArrayList<Double> errorVector = new ArrayList<Double>();
        for (int row = 0; row < labelVector.size(); row++) {
            double errorVal = (
                labelVector.get(row)
                - actVectors.get(actVectors.size() - 1).get(row)
            );
            errorVector.add(errorVal);
        }

        while (layerIndex > 0) {
            ArrayList<Double> outVector = actVectors.get(layerIndex);
            ArrayList<Double> inVector = actVectors.get(layerIndex - 1);

            ArrayList<Double> deltaVector = new ArrayList<Double>();
            for (int row = 0; row < outVector.size(); row++) {
                double deltaVal = (
                    errorVector.get(row)
                    * outVector.get(row)
                    * (1 - outVector.get(row))
                );
                deltaVector.add(deltaVal);
            }

            int matrixIndex = layerIndex - 1;
            for (int row = 0; row < deltaVector.size(); row++) {
                ArrayList<Double> deltaRow = new ArrayList<Double>();
                for (int col = 0; col < inVector.size(); col++) {
                    double deltaVal = (
                        inVector.get(col)
                        * deltaVector.get(row)
                    );
                    weightMatrices.get(matrixIndex).get(row).set(col, (
                        weightMatrices.get(matrixIndex).get(row).get(col)
                        + deltaVal * learningRate
                    ));
                }
            }


            int numRows = weightMatrices.get(matrixIndex).get(0).size();
            int numCols = weightMatrices.get(matrixIndex).size();
            ArrayList<Double> newErrorVector = new ArrayList<Double>();
            for (int row = 0; row < numRows; row++) {
                double errorVal = 0;
                for (int col = 0; col < numCols; col++) {
                    errorVal += (
                        weightMatrices.get(matrixIndex).get(col).get(row)
                        * errorVector.get(col)
                    );
                }
                newErrorVector.add(errorVal);
            }
            errorVector = newErrorVector;

            layerIndex--;
        }
    }

    public double evaluate(ArrayList<TrainingImage> trainingData) {
        double numCorrect = 0;
        double numWrong = 0;
        for (int i = 0; i < trainingData.size(); i++) {
            ArrayList<Double> result = run(trainingData.get(i).pixelVector);
            double resultVal = result.indexOf(Collections.max(result));
            double expectedVal = (
                trainingData.get(i).labelVector.indexOf(
                    Collections.max(trainingData.get(i).labelVector))
            );

            if (resultVal == expectedVal) {
                numCorrect += 1;
            }
            else {
                numWrong += 1;
            }
        }
        return numCorrect / (numCorrect + numWrong);
    }

    public ArrayList<Double> run(ArrayList<Double> imageData) {
        int numLayers = structure.size();

        ArrayList<Double> inVector = imageData;
        for (int layerIndex = 1; layerIndex < numLayers; layerIndex++) {
            ArrayList<ArrayList<Double>> weightMatrix = (
                weightMatrices.get(layerIndex - 1)
            );

            ArrayList<Double> zVector = new ArrayList<Double>();
            ArrayList<Double> outVector = new ArrayList<Double>();

            int neuronsOut = weightMatrix.size();
            int neuronsIn = weightMatrix.get(0).size();

            for (int outIndex = 0; outIndex < neuronsOut; outIndex++) {
                double zVal = 0;
                for (int inIndex = 0; inIndex < neuronsIn; inIndex++) {
                    zVal += (
                        weightMatrix.get(outIndex).get(inIndex)
                        * inVector.get(inIndex)
                    );
                }
                zVector.add(zVal);

                double outVal = sigmoid(zVal);
                outVector.add(outVal);
            }
            inVector = outVector;
        }
        return inVector;
    }

    public static double sigmoid(double z) {
        return 1 / (1 + Math.exp(-z));
    }

    public static double sigmoidDerivative(double z) {
        return sigmoid(z)*(1-sigmoid(z));
    }

    public static double costDerivative(
        double y,
        double yHat
    ) {
        return yHat - y;
    }

    public static int[] trainingBatchIndices(int iteration, int fullSetSize) {
        int batchEndIndex = (
            iteration * TRAINING_BATCH_SIZE
            + TRAINING_BATCH_SIZE
        );
        if (iteration * TRAINING_BATCH_SIZE >= fullSetSize) {
            batchEndIndex = TRAINING_BATCH_SIZE;
        }
        else if (batchEndIndex > fullSetSize) {
            batchEndIndex = fullSetSize;
        }

        int batchStartIndex = (
            batchEndIndex - TRAINING_BATCH_SIZE
        );

        int[] batchIndices = {batchStartIndex, batchEndIndex};

        return batchIndices;
    }

    public static ArrayList<TrainingImage> readCSV(String filename) {
        BufferedReader readObj = null;
        String line = "";
        String cvsSplitBy = ",";

        ArrayList<TrainingImage> trainingData = new ArrayList<TrainingImage>();

        try {
            readObj = new BufferedReader(new FileReader(filename));
            while ((line = readObj.readLine()) != null) {
                // use comma as separator
                String[] imageData = line.split(cvsSplitBy);
                int label = Integer.parseInt(imageData[0]);
                ArrayList<Double> labelVector = new ArrayList<Double>(
                    Collections.nCopies(10, 0.01)
                );
                labelVector.set(label, 0.99);

                ArrayList<Double> pixelVector = new ArrayList<Double>();
                double scaleFactor = 0.99 / 255;
                for (int i = 1; i < imageData.length; i++) {
                    double pixel = (
                        Double.parseDouble(imageData[i]) * scaleFactor + 0.1
                    );
                    pixelVector.add(pixel);
                }

                TrainingImage trainingImage = (
                    new NeuralNetwork().new TrainingImage()
                );
                trainingImage.labelVector = labelVector;
                trainingImage.pixelVector = pixelVector;

                trainingData.add(trainingImage);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (readObj != null) {
                try {
                    readObj.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return trainingData;
    }

    public class TrainingImage {
        ArrayList<Double> labelVector;
        ArrayList<Double> pixelVector;

        TrainingImage() {}
    }

    /**
    * Saves weights of trained NeuralNetwork to a CSV at the five filepath.
    */
    public void saveWeights(
        String filepath,
        ArrayList<ArrayList<ArrayList<Double>>> weightMatrices
    ) {
        ;
    }
}