// Computation
import java.lang.Math;

// Data structures
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.StringTokenizer;

// I/O imports
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class NeuralNetwork {

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

        String trainingDataFile = "mnist_train.csv";
        ArrayList<TrainingImage> trainingData = readCSV(trainingDataFile);
        nnObj.train(trainingData, 3);

        ArrayList<Double> result = nnObj.run(trainingData.get(10).pixelVector);

        int expected = trainingData.get(10).labelVector.indexOf(
            Collections.max(trainingData.get(10).labelVector)
        );
        int actual = result.indexOf(Collections.max(result));
        System.out.println("expected = " + expected + ", actual = " + actual);

        double trainAccuracy = nnObj.evaluate(trainingData);
        System.out.println("Training accuracy: " + trainAccuracy);

        String testDataFile = "mnist_test.csv";
        ArrayList<TrainingImage> testData = readCSV(testDataFile);

        double testAccuracy = nnObj.evaluate(testData);
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
        System.out.println("Creating ANN");

        this.structure = structure;
        this.learningRate = learningRate;

        createWeightMatrices();
    }

    /**
    * Initialize the weight matrices with random values.
    */
    public void createWeightMatrices() {
        System.out.println("Initializing weight matrices");

        int numLayers = structure.size();

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
                    double weight = Math.random() - 0.5;
                    weightRow.add(weight);
                }
                weightMatrix.add(weightRow);
            }
            weightMatrices.add(weightMatrix);
        }

        System.out.println("number of matrices: " + weightMatrices.size());
        System.out.println("matrix sizes:");
        for (int i = 0; i < weightMatrices.size(); i++) {
            int mSize = weightMatrices.get(i).size();
            int nSize = weightMatrices.get(i).get(0).size();
            System.out.println(" * [" + mSize + ", " + nSize + "]");
        }

        this.weightMatrices = weightMatrices;
    }

    public void train(
            ArrayList<TrainingImage> trainingData,
            int iterations
            ) {
        System.out.println("Training ANN");
        long startTime = System.currentTimeMillis();

        for (int iteration = 0; iteration < iterations; iteration++) {
            for (
                    int imageIndex = 0;
                    imageIndex < trainingData.size();
                    imageIndex++) {
                TrainingImage imageData = trainingData.get(imageIndex);
                trainSingle(imageData.labelVector, imageData.pixelVector);
            }
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = (stopTime - startTime) / 1000; // convert to seconds
        System.out.println(
            "Training the NeuralNetwork took " + elapsedTime + " seconds"
        );
    }

    public void trainSingle(
            ArrayList<Double> labelVector, ArrayList<Double> pixelVector
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

    public void gradienctDescent(
            ArrayList<Double> labelVector, ArrayList<Double> pixelVector
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

        ArrayList<Double> deltaVector = new ArrayList<Double>();
        for (int digit = 0; digit < labelVector.size(); digit++) {
            double y = labelVector.get(digit);
            double yHat = actVectors.get(actVectors.size() - 1).get(digit);
            double z = zVectors.get(zVectors.size() - 1).get(digit);
            double detlaVal = costDerivative(y, yHat) * sigmoidDerivative(z);
            deltaVector.add(detlaVal);
        }

        ArrayList<ArrayList<ArrayList<Double>>> newWeightMatrices = (
            new ArrayList<ArrayList<ArrayList<Double>>>()
        );

        ArrayList<ArrayList<Double>> lastWeightMatrix = (
            new ArrayList<ArrayList<Double>>()
        );

        int outSize = structure.get(structure.size() - 1);
        int inSize = structure.get(structure.size() - 2);
        for (int outIndex = 0; outIndex < outSize; outIndex++) {
            ArrayList<Double> weightRow = new ArrayList<Double>();
            for (int inIndex = 0; inIndex < inSize; inIndex++) {
                double actVal = (
                    actVectors.get(actVectors.size() - 2).get(inIndex)
                );
                double deltaVal = deltaVector.get(outIndex);

                double newWeight = actVal * deltaVal;

                weightRow.add(newWeight);
            }
            lastWeightMatrix.add(weightRow);
        }
        newWeightMatrices.add(lastWeightMatrix);

        for (int layerIndex = 2; layerIndex < numLayers; layerIndex++) {
            ArrayList<Double> zVector = (
                zVectors.get(zVectors.size() - layerIndex)
            );
            ArrayList<ArrayList<Double>> weightMatrix = (
                weightMatrices.get(weightMatrices.size() - layerIndex + 1)
            );

            int numCols = weightMatrix.size();
            int numRows = weightMatrix.get(0).size();
            ArrayList<Double> newDeltaVector = new ArrayList<Double>();

            for (int row = 0; row < numRows; row++) {
                double newDeltaVal = 0;
                for (int col = 0; col < numCols; col++) {

                    double deltaVal = deltaVector.get(col);
                    double weightVal = weightMatrix.get(col).get(row);

                    newDeltaVal += deltaVal * weightVal;
                }
                double zVal = zVector.get(row);
                newDeltaVal *= sigmoid(zVal);
                newDeltaVector.add(newDeltaVal);
            }
            deltaVector.clear();
            deltaVector = newDeltaVector;

            ArrayList<Double> actVector = (
                actVectors.get(actVectors.size() - layerIndex - 1)
            );

            ArrayList<ArrayList<Double>> newWeightMatrix = (
                new ArrayList<ArrayList<Double>>()
            );

            outSize = structure.get(structure.size() - layerIndex);
            inSize = structure.get(structure.size() - layerIndex - 1);

            for (int outIndex = 0; outIndex < outSize; outIndex++) {
                ArrayList<Double> weightRow = new ArrayList<Double>();
                for (int inIndex = 0; inIndex < inSize; inIndex++) {
                    double actVal = actVector.get(inIndex);
                    double deltaVal = deltaVector.get(outIndex);
                    double newWeight = actVal * deltaVal;
                    weightRow.add(newWeight);
                }
                newWeightMatrix.add(weightRow);
            }
            newWeightMatrices.add(0, newWeightMatrix);
        }

        assert weightMatrices.size() == newWeightMatrices.size();
        for (int matrixIndex = 0; matrixIndex < numLayers - 1; matrixIndex++){
            assert (
                weightMatrices.get(matrixIndex).size()
                == newWeightMatrices.get(matrixIndex).size()
            );

            int numRows = weightMatrices.get(matrixIndex).size();
            int numCols = weightMatrices.get(matrixIndex).get(0).size();
            for (int row = 0; row < numRows; row++) {
                assert (
                    weightMatrices.get(matrixIndex).get(row).size()
                    == newWeightMatrices.get(matrixIndex).get(row).size()
                );
                for (int col = 0; col < numCols; col++) {
                    double newWeightVal = (
                        weightMatrices.get(matrixIndex).get(row).get(col)
                        - learningRate
                        * newWeightMatrices.get(matrixIndex).get(row).get(col)
                    );
                    weightMatrices.get(matrixIndex).get(row).set(
                        col, newWeightVal
                    );
                }
            }
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

    public static double costDerivative(double y, double yHat) {
        return yHat - y;
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
}