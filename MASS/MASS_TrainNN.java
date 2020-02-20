package train_nn.mass_train_nn;

import java.util.Date;
import java.util.ArrayList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Collections;

import edu.uw.bothell.css.dsl.MASS.Agents;
import edu.uw.bothell.css.dsl.MASS.MASS;
import edu.uw.bothell.css.dsl.MASS.Places;
import edu.uw.bothell.css.dsl.MASS.logging.LogLevel;


public class MASS_TrainNN {

    private static final String NODE_FILE = "nodes.xml";

    // some unused variables left behind for easy debugging
    @SuppressWarnings("unused")
    public static void main( String[] args ) {
        // init MASS library
        MASS.setNodeFilePath(NODE_FILE);
        MASS.setLoggingLevel(LogLevel.DEBUG);

        // start MASS
        MASS.getLogger().debug( "MASS_TrainNN initializing MASS library..." );
        MASS.init();
        MASS.getLogger().debug( "MASS library initialized" );

        long startTime = new Date().getTime();

        // Create all Places: a place for each param-combo
        MASS.getLogger().debug( "MASS_TrainNN creating ParamCombo Places..." );
        ParameterCombos paramCombos = new ParameterCombos();
        int numCombos = paramCombos.combos.size();

        Places paramComboPlaces = new Places(
            1,
            ParamCombo.class.getName(),
            null,
            numCombos
        );
        MASS.getLogger().debug("Places created");

        // instruct all Places to return the hostnames of the machines on which
        // they reside.
        Object[] paramComboHosts = (Object[]) paramComboPlaces.callAll(
            ParamCombo.GET_HOSTNAME,
            (new Object[numCombos])
        );

        MASS.getLogger().debug("MASS_TrainNN creating Trainer Agents...");
        Agents trainers = new Agents(
            1,
            Trainer.class.getName(),
            null,
            paramComboPlaces,
            numCombos
        );
        MASS.getLogger().debug("Agents created");

        // instruct all Agents to return the hostnames of the machines on which
        // they reside.
        Object[] trainerHosts = (Object[]) trainers.callAll(
            Trainer.GET_HOSTNAME,
            (new Object[numCombos])
        );

        Object[] trainResults = (Object[]) trainers.callAll(
            Trainer.TRAIN,
            (new Object[numCombos])
        );
        trainers.manageAll();

        SortedSet<ComboResult> comboResultSet = (
            Collections.synchronizedSortedSet(new TreeSet<ComboResult>())
        );
        for (int i = 0; i < numCombos; i++) {
            ComboResult result = (ComboResult) trainResults[i];
            comboResultSet.add(result);
        }
        ComboResult bestComboResultSet = comboResultSet.first();

        // orderly shutdown
        MASS.getLogger().debug(
            "MASS_TrainNN instructs MASS library to finish operations..."
        );
        MASS.finish();
        MASS.getLogger().debug("MASS library has stopped");

        // calculate / display execution time
        long elapsedTime = (new Date().getTime() - startTime) / 1000;

        ParameterCombos.Combo paramCombo = paramCombos.combos.get(
            bestComboResultSet.comboIndex
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
            + "It had an accuracy of " + bestComboResultSet.accuracy * 100
            + "%."
        );
    }
}
