package train_nn.mass_train_nn;

import java.net.InetAddress;
import java.util.ArrayList;

import edu.uw.bothell.css.dsl.MASS.Agent;
import edu.uw.bothell.css.dsl.MASS.MASS;
import edu.uw.bothell.css.dsl.MASS.logging.LogLevel;


@SuppressWarnings("serial")
public class Trainer extends Agent {

	public static final int GET_HOSTNAME = 0;
	public static final int TRAIN = 1;

    public ParameterCombos paramCombos;
    public ArrayList<NeuralNetwork.TrainingImage> trainingData;
    public ArrayList<NeuralNetwork.TrainingImage> testData;
	
	/**
	 * This constructor will be called upon instantiation by MASS
	 * The Object supplied MAY be the same object supplied when Places was
	 * created.
	 * @param obj
	 */
	public Trainer(Object args) {

        MASS.setLoggingLevel(LogLevel.DEBUG);
        paramCombos = new ParameterCombos();

        String dataDir = System.getProperty("user.home") + "/data/";
        String trainingDataFile = dataDir + "train_small.csv";
        trainingData = NeuralNetwork.readCSV(trainingDataFile);

        String testDataFile = dataDir + "test_small.csv";
        testData = NeuralNetwork.readCSV(testDataFile);
	}

	/**
	 * This method is called when "callAll" is invoked from the master node
	 */
	public Object callMethod(int method, Object o) {
		
		switch (method) {
			case GET_HOSTNAME:
				return findHostName(o);
			case TRAIN:
				return train(o);
		
		default:
			return new String("Unknown Method Number: " + method);
		
		}
	}
	
	/**
	 * Return a String identifying where this Agent is actually located
	 * @param o
	 * @return The hostname (as a String) where this Agent is located
	 */
	public Object findHostName(Object o){

		try{
			return (
			    (String) "Agent " + getAgentId() + " is on "
			    + InetAddress.getLocalHost().getCanonicalHostName()
			    + " and working on comboID = "
			    + Integer.toString(getIndex()[0])
			    + "."
			);
        }
        catch(Exception e) {
        	return "Error : " + e.getLocalizedMessage() + e.getStackTrace();
        }
    }
	
	/**
	 * Train the Neural Network with the current parameter-combo assigned to
	 * this trainer. Then evaluate the accuracy of the trained network,
	 * returning the result to the master.
	 * @param o
	 * @return
	 */
	public ComboResult train(Object o) {

		ParamCombo paramCombo = (ParamCombo) this.getPlace();
		int comboIndex = paramCombo.getIndex()[0];
        ParameterCombos.Combo combo = paramCombos.combos.get(comboIndex);

        ArrayList<Integer> structure = new ArrayList<Integer>();
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

        ComboResult result = new ComboResult(comboIndex, testAccuracy);

        Double accuracy = new Double(result.accuracy);

        MASS.getLogger().debug(
            "Training on Trainer[" + getAgentId() + "]"
            + ", comboIndex = " + comboIndex + ", accuracy = " + testAccuracy
        );
        //paramCombo.setVisted(true);
        //return accuracy;
        return result;
	}
}
