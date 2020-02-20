package train_nn.mass_train_nn;

import java.net.InetAddress;
import edu.uw.bothell.css.dsl.MASS.Place;


public class ParamCombo extends Place {

	public static final int GET_HOSTNAME = 0;
	
	/**
	 * This constructor will be called upon instantiation by MASS
	 * The Object supplied MAY be the same object supplied when Places was
	 * created.
	 * @param obj
	 */
	public ParamCombo(Object obj) {}
	
	/**
	 * This method is called when "callAll" is invoked from the master node
	 */
	public Object callMethod(int method, Object o) {
		
		switch (method) {
		
		case GET_HOSTNAME:
			return findHostName(o);

		default:
			return new String("Unknown Method Number: " + method);
		
		}
	}
	
	/**
	 * Return a String identifying where this Place is actually located
	 * @param o
	 * @return The hostname (as a String) where this Place is located
	 */
	public Object findHostName(Object o){
		
		try{
			return (
			    (String) "ParamCombo is on "
			    + InetAddress.getLocalHost().getCanonicalHostName()
			    + " and corresponds to comboID = "
			    + Integer.toString(getIndex()[0])
			);
        }
		
		catch (Exception e) {
			return "Error : " + e.getLocalizedMessage() + e.getStackTrace();
		}
	}
}
