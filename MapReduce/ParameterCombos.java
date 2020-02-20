import java.util.ArrayList;
import java.io.PrintWriter;  

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
        PrintWriter writer = null;
//        for (int i = 0; i < paramCombos.combos.size(); i++) 
//        {	
//        	try { 
//        		  
//        	writer = new PrintWriter("Combo"+i+".txt", "UTF-8");
//        	writer.println(i+","+paramCombos.combos.get(i).learningRate+","+paramCombos.combos.get(i).numIterations+","+paramCombos.combos.get(i).numHiddenLayers+","+paramCombos.combos.get(i).hiddenLayerSize);
////        	writer.println("learningRate="+paramCombos.combos.get(i).learningRate);
////        	writer.println("numIterations="+ paramCombos.combos.get(i).numIterations);
////        	writer.println("numHiddenLayers="+ paramCombos.combos.get(i).numHiddenLayers);
////        	writer.println("hiddenLayerSize="+ paramCombos.combos.get(i).hiddenLayerSize);
//        	writer.close();
//        	} 
//            catch (Exception e) { 
//                System.out.println(e); 
//            } 
//            System.out.println("learningRate "+paramCombos.combos.get(i).learningRate +" iterations "+ paramCombos.combos.get(i).numIterations +" hidden layers "+ paramCombos.combos.get(i).numHiddenLayers + "hiddenLayerSize "+ paramCombos.combos.get(i).hiddenLayerSize + " "); 
//        }   
//        FileOutputStream out = null;
//        try {
//            in = new FileInputStream("input.txt");
//            out = new FileOutputStream("output.txt");
//            
//            int c;
//            while ((c = in.read()) != -1) {
//               out.write(c);
//            }
//         }finally {
//            if (in != null) {
//               in.close();
//            }
//            if (out != null) {
//               out.close();
//            }
//         }
        System.out.println(paramCombos.combos.get(0).numIterations);
        System.out.println(paramCombos.combos.get(10).numIterations);
    }
}
