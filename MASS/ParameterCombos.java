package train_nn.mass_train_nn;

import java.util.ArrayList;


public class ParameterCombos {
    /**
    * Neural Network Parameters:
    * --------------------------
    * 1) Learning rate
    * 2) Number of iterations
    * 3) Number of hidden layers
    * 4) Size of hidden layers (all are the same size for simplicity)
    */
    public class Combo {
        double learningRate;
        int numIterations;
        int numHiddenLayers;
        int hiddenLayerSize;

        public Combo (
                double learningRate,
                int numIterations,
                int numHiddenLayers,
                int hiddenLayerSize
                ) {
            this.learningRate = learningRate;
            this.numIterations = numIterations;
            this.numHiddenLayers = numHiddenLayers;
            this.hiddenLayerSize = hiddenLayerSize;
        }
    }

    ArrayList<Combo> combos;

    public ParameterCombos() {
        this(
        // Learning Rate related params:
        0.01,  // learningRateMin
        0.5,   // learningRateMax
        0.1,   // learningRateStep
        // Number of iterations related params:
        3,     // numIterationsMin
        3,    // numIterationsMax
        1,     //numIterationStep
        // Number of hidden layer related params:
        2,     // numHiddenLayersMin
        6,     //numHiddenLayersMax
        2,     // numHiddenLayersStep
        // Hidden layer
        50,    // hiddenLayerSizeMin
        200,   // hiddenLayerSizeMax
        50     // hiddenLayerSizeStep
        );
    }

    public ParameterCombos(
        // Learning Rate related params:
        double learningRateMin,
        double learningRateMax,
        double learningRateStep,
        // Number of iterations related params:
        int numIterationsMin,
        int numIterationsMax,
        int numIterationStep,
        // Number of hidden layer related params:
        int numHiddenLayersMin,
        int numHiddenLayersMax,
        int numHiddenLayersStep,
        // Hidden layer
        int hiddenLayerSizeMin,
        int hiddenLayerSizeMax,
        int hiddenLayerSizeStep
    ) {
        combos = new ArrayList<Combo>();

        ArrayList<Integer> numIterationOptions = new ArrayList<Integer>();
        for (
            double learningRate = learningRateMin;
            learningRate <= learningRateMax;
            learningRate += learningRateStep
        ) {
            for (
                int numIterations = numIterationsMin;
                numIterations <= numIterationsMax;
                numIterations += numIterationStep
            ) {
                for (
                    int numHiddenLayers = numHiddenLayersMin;
                    numHiddenLayers <= numIterationsMax;
                    numHiddenLayers += numHiddenLayersStep
                ) {
                    for (
                        int hiddenLayerSize = hiddenLayerSizeMin;
                        hiddenLayerSize <= hiddenLayerSizeMax;
                        hiddenLayerSize += hiddenLayerSizeStep
                    ) {
                        //
                        Combo combo = new Combo(
                            learningRate,
                            numIterations,
                            numHiddenLayers,
                            hiddenLayerSize
                        );
                        combos.add(combo);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        ParameterCombos paramCombos = new ParameterCombos();
        System.out.println(paramCombos.combos.size());
        System.out.println(paramCombos.combos.get(0).numIterations);
        System.out.println(paramCombos.combos.get(10).numIterations);
    }
}
