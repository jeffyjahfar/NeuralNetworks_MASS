import java.io.IOException;
import java.util.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.lang.*;
import java.util.ArrayList;
import java.util.Comparator;
import javafx.util.Pair;
import java.io.BufferedReader;
import java.io.FileReader;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;
import java.util.concurrent.TimeUnit;
import java.lang.Math; 
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.Mapper.Context;
import java.net.URI; 

public class MapRedNNN 
{
	// Output of mapper will be pair of (ComboIndex, Accuracy) 
	public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, IntWritable, DoubleWritable>
    {
		 private JobConf conf;
		    // Using the old API in the Mapper and Reducer. The JobConfigurable#configure has to be implemented in the Mapper and Reducer class.
		    // to read additional parameters to the mapper and reducers, besides the the inputs which they process
	        public void configure(JobConf job)
	        {
	            this.conf = job;
	        }
	        
	        // Each combo file is read and the neural network is created for the combintation and accuracy is calculated
	        /* Example of Combination file content is 
	         * Combo Index, Learning Rate, numIterations, numHiddenLayers, hiddenLayerSize
	         * 0,           0.01,          3,             2,               50
	         * */
	        public void map(LongWritable key, Text value, OutputCollector<IntWritable, DoubleWritable> output, Reporter reporter) throws IOException 
		    {
	         String line = value.toString();
	         String[] vals = line.split(",");
	         Combo combo = new Combo(Double.parseDouble(vals[1]),Integer.parseInt(vals[2]),Integer.parseInt(vals[3]),Integer.parseInt(vals[4]));
	         
	         Path path = new Path("/home/monali22_css534/MapRedNN/hun.csv");
	         ArrayList<NeuralNetwork.TrainingImage> trainingData = (
	             NeuralNetwork.readCSV(path.toString())
	         );

	         Path path2 = new Path("/home/monali22_css534/MapRedNN/ten.csv");
	         ArrayList<NeuralNetwork.TrainingImage> testData = (
	             NeuralNetwork.readCSV(path2.toString())
	         );
			 
	         // Creates a structure for the given combo
		     ArrayList<Integer> structure = new ArrayList<>();
		     structure.add(784);  // Add input layer
		     for (int layerIndex = 1;layerIndex <= combo.numHiddenLayers;layerIndex++) 
		     {
		     // Add another hidden layer
		     structure.add(combo.hiddenLayerSize);
		     }
		     structure.add(10);  // Add the output layer

		     NeuralNetwork nnObj = new NeuralNetwork(structure,combo.learningRate);
		     nnObj.train(trainingData, combo.numIterations);
		     double testAccuracy = nnObj.evaluate(testData);
		     output.collect(new IntWritable(Integer.parseInt(vals[0])) , new DoubleWritable(testAccuracy));
			 
		    }
		
    }
	
	// Output of reducer will be pair of (ComboIndex, Accuracy) written to a output file
	public static class Reduce extends MapReduceBase implements Reducer<IntWritable,DoubleWritable, IntWritable, DoubleWritable> 
    {
		 public void reduce(IntWritable key, Iterator<DoubleWritable> values, OutputCollector<IntWritable, DoubleWritable> output, Reporter reporter) throws IOException 
			{
			 output.collect(key, new  DoubleWritable(values.next().get()));  
			}
    }
	
	 public static void main(String[] args) throws Exception 
	    {
		 long startTime = System.currentTimeMillis();
		 
		 JobConf conf = new JobConf(MapRedNNN.class);
	     conf.setJobName("MapRedNNN");
	     
	     conf.setOutputKeyClass(IntWritable.class);
 	     conf.setOutputValueClass(DoubleWritable.class);
 	     
 	     conf.setMapperClass(Map.class);
 	     conf.setCombinerClass(Reduce.class);
 	     conf.setReducerClass(Reduce.class);
 	     
 	     conf.setInputFormat(TextInputFormat.class);
 	     conf.setOutputFormat(TextOutputFormat.class);
 	     
 	     String inpFile = args[0];
 	     
 	     FileInputFormat.setInputPaths(conf, new Path(args[0]));
 	     FileOutputFormat.setOutputPath(conf, new Path(args[1]));
 	   
 	     JobClient.runJob(conf);
 	     
 	     //Measure the endTime
         long endTime   = System.currentTimeMillis();
         // Calulate the total time
         long totalTime = endTime - startTime;
         //Convert in seconds
         long seconds = TimeUnit.MILLISECONDS.toSeconds(totalTime);
         System.out.println("Total time is "+ seconds + "seconds");
	    }
	
}