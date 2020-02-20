/**
* Trains Neural Networks for handwritten digit recognition using trying various
* combinations of parameters to determine and train the optimal Network.
*
* Neural Network Parameters:
* ==========================
* 1) Learning rate
* 2) Number of iterations
* 3) Number of hidden layers
* 4) Size of hidden layers (all are the same size for simplicity)
*
* TODO: maintain list of weight matrices, or save them, immediately to as a
* CSV -- ultimately we want the best trained weights to get saved.
*/

// **Standard imports:**
// =====================
// * I/O imports
import java.io.IOException;
// * Data structure imports:
import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;
// **Installed imports:**
// ======================
// * MPI imports
import mpi.MPI;
import mpi.MPIException;
import mpi.Status;


public class MPI_TrainNN {

    public static void main(String args[])
        throws ClassNotFoundException, IOException, MPIException
    {
        MPI.Init(args) ;

        int source;                            // Rank of sender
        int master = 0;                        // Rank of master node
        int tag = 50;                          // Tag for messages
        int myrank = MPI.COMM_WORLD.Rank();    // Rank ID of this process
        int numNodes = MPI.COMM_WORLD.Size();  // # of nodes in communication

        long startTime = System.currentTimeMillis();

        ParameterCombos paramCombos = new ParameterCombos();

        int stripeNumCombos = paramCombos.combos.size() / numNodes;

        SortedSet<ComboResult> localComboResultSet = (
            Collections.synchronizedSortedSet(new TreeSet<>())
        );

        String dataDir = System.getProperty("user.home") + "/data/";
        String trainingDataFile = dataDir + "mnist_train.csv";
        ArrayList<NeuralNetwork.TrainingImage> trainingData = (
            NeuralNetwork.readCSV(trainingDataFile)
        );

        String testDataFile = dataDir + "mnist_test.csv";
        ArrayList<NeuralNetwork.TrainingImage> testData = (
            NeuralNetwork.readCSV(testDataFile)
        );

        int localFirstComboID = myrank * stripeNumCombos;
        int localLastComboID = localFirstComboID + stripeNumCombos - 1;
        for (
            int comboIndex = localFirstComboID;
            comboIndex <= localLastComboID;
            comboIndex++
        ) {
            ParameterCombos.Combo combo = paramCombos.combos.get(comboIndex);
            ArrayList<Integer> structure = new ArrayList<>();
            structure.add(784);  // Add input layer
            for (
                int layerIndex = 1;
                layerIndex <= combo.numHiddenLayers;
                layerIndex++
            ) {
                // Add another hidden layer
                structure.add(combo.hiddenLayerSize);
            }
            structure.add(10);  // Add the output layer

            NeuralNetwork nnObj = new NeuralNetwork(
                structure,
                combo.learningRate
            );
            nnObj.train(trainingData, combo.numIterations);

            double testAccuracy = nnObj.evaluate(testData);

            ComboResult comboResultObj = new ComboResult(
                comboIndex,
                testAccuracy
            );
            localComboResultSet.add(comboResultObj);
        }

        ComboResult localBestComboResult = localComboResultSet.first();
        int[] localBestComboIndex = {localBestComboResult.comboIndex};
        double[] localBestAccuracy = {localBestComboResult.accuracy};

        int[] globalTopComboIndex = new int[numNodes];
        double[] globalTopAccuracy = new double[numNodes];

        MPI.COMM_WORLD.Gather(
            localBestComboIndex, 0, 1, MPI.INT, globalTopComboIndex, 0, 1,
            MPI.INT, 0
        );
        MPI.COMM_WORLD.Gather(
            localBestAccuracy, 0, 1, MPI.DOUBLE, globalTopAccuracy, 0, 1,
            MPI.DOUBLE, 0
        );

        int bestComboOwnerRank = -1;
        if (myrank == 0) {
            SortedSet<ComboResult> globalTopComboResultSet = (
                Collections.synchronizedSortedSet(new TreeSet<>())
            );
            for (int nodeIndex = 0; nodeIndex < numNodes; nodeIndex++) {
                ComboResult comboResultObj = new ComboResult(
                    globalTopComboIndex[nodeIndex],
                    globalTopAccuracy[nodeIndex]
                );
                globalTopComboResultSet.add(comboResultObj);
            }

            ComboResult bestComboResult = globalTopComboResultSet.first();

            long stopTime = System.currentTimeMillis();
            // Convert elapsed time to seconds
            long elapsedTime = (stopTime - startTime) / 1000;

            ParameterCombos.Combo paramCombo = paramCombos.combos.get(
                bestComboResult.comboIndex
            );

            double learningRate = paramCombo.learningRate;
            int numIterations = paramCombo.numIterations;
            int numHiddenLayers = paramCombo.numHiddenLayers;
            int hiddenLayerSize = paramCombo.hiddenLayerSize;

            System.out.println(
                "Elapsed time for finding top peforming Neural Networks = "
                + elapsedTime + " seconds.\n"
                + "The most accurate paramter combo was:\n"
                + " * Learning rate = " + learningRate + "\n"
                + " * Number of iterations = " + numIterations + "\n"
                + " * Number of hidden layers = " + numHiddenLayers + "\n"
                + " * Size of hidden layers = " + hiddenLayerSize + "\n"
                + "It had an accuracy of " + bestComboResult.accuracy * 100
                + "%."
            );

            bestComboOwnerRank = bestComboResult.comboIndex / stripeNumCombos;
        }
        int[] bestComboOwnerArr = {bestComboOwnerRank};
        MPI.COMM_WORLD.Bcast(bestComboOwnerArr, 0, 1, MPI.INT, master);
        bestComboOwnerRank = bestComboOwnerArr[0];

        String yourHostName = "";
        String myHostName = MPI.Get_processor_name();
        if (bestComboOwnerRank == 0 && myrank == 0) {
            yourHostName = myHostName;
        }
        else if (myrank == bestComboOwnerRank) {
            String best_NN_WeightsFile = "best_NN_weights.csv";
            //NeuralNetwork.saveWeights(best_NN_WeightsFile);

            char [] myHostMsg = myHostName.toCharArray();
            MPI.COMM_WORLD.Send(
                myHostMsg, 0, myHostMsg.length, MPI.CHAR, master, tag
            );
        }
        else if (myrank == 0){
            char [] yourHostMsg = new char [60] ;
            Status stat = MPI.COMM_WORLD.Recv(
                yourHostMsg, 0, 60, MPI.CHAR, bestComboOwnerRank, tag
            );
            yourHostName = new String(yourHostMsg);
        }

        if (myrank == 0) {
             System.out.println(
                "This was trained on process " + bestComboOwnerRank
                + ", which ran on " + yourHostName + ".\n"
                + "The trained weights will be saved on/by the corresponding "
                + "node."
            );
        }

        MPI.Finalize();
    }
}