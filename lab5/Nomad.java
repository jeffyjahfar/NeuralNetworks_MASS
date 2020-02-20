/*

 	MASS Java Software License
	© 2012-2015 University of Washington

	Permission is hereby granted, free of charge, to any person obtaining a copy
	of this software and associated documentation files (the "Software"), to deal
	in the Software without restriction, including without limitation the rights
	to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
	copies of the Software, and to permit persons to whom the Software is
	furnished to do so, subject to the following conditions:

	The above copyright notice and this permission notice shall be included in
	all copies or substantial portions of the Software.

	The following acknowledgment shall be used where appropriate in publications, presentations, etc.:      

	© 2012-2015 University of Washington. MASS was developed by Computing and Software Systems at University of 
	Washington Bothell.

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
	FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
	AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
	LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
	OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
	THE SOFTWARE.

*/

package edu.uwb.css534;

import java.net.InetAddress;

import edu.uw.bothell.css.dsl.MASS.Agent;

@SuppressWarnings("serial")
public class Nomad extends Agent {

	public static final int GET_HOSTNAME = 0;
	public static final int MIGRATE = 1;
	
	
	/**
	 * This constructor will be called upon instantiation by MASS
	 * The Object supplied MAY be the same object supplied when Places was created
	 * @param obj
	 */
	public Nomad(Object obj) { }

	/**
	 * This method is called when "callAll" is invoked from the master node
	 */
	public Object callMethod(int method, Object o) {
		
		switch (method) {
		
			case GET_HOSTNAME:
				return findHostName(o);
		
			case MIGRATE:
				return move(o);
			
		
		
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
			return (String) "Agent located at: " + InetAddress.getLocalHost().getCanonicalHostName() + " " + Integer.toString(getIndex()[0]) + ":" + Integer.toString(getIndex()[1]) + ":" + Integer.toString(getIndex()[2]);
        }
        
        catch(Exception e) {
        	return "Error : " + e.getLocalizedMessage() + e.getStackTrace();
        }
        
    }
	
	/**
	 * Move this Agent to the next position in the X-coordinate
	 * @param o
	 * @return
	 */
	public Object move(Object o) {
		
		int xModifier = this.getPlace().getIndex()[0];
        int yModifier = this.getPlace().getIndex()[1];
        int zModifier = this.getPlace().getIndex()[2];
        xModifier++;
	        
        migrate(xModifier, yModifier, zModifier);
        return o;

	}
	
}

