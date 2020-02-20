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

import java.util.Date;

import edu.uw.bothell.css.dsl.MASS.Agents;
import edu.uw.bothell.css.dsl.MASS.MASS;
import edu.uw.bothell.css.dsl.MASS.Places;
import edu.uw.bothell.css.dsl.MASS.logging.LogLevel;

public class SpaceTraversal2D {

	private static final String NODE_FILE = "nodes.xml";
	
	@SuppressWarnings("unused")		// some unused variables left behind for easy debugging
	public static void main( String[] args ) {

		// remember starting time
		long startTime = new Date().getTime();
		
		// init MASS library
		MASS.setNodeFilePath( NODE_FILE );
		MASS.setLoggingLevel( LogLevel.DEBUG );
		
		// start MASS
		MASS.getLogger().debug( "SpaceTraversal2D initializing MASS library..." );
		MASS.init();
		MASS.getLogger().debug( "MASS library initialized" );
		
		/* 
		 * Create all Places (having dimensions of x, y, and z)
		 * ( the total number of Place objects that will be created is: x * y * z )
		 */
		int x = 10;
		int y = 10;

		MASS.getLogger().debug( "SpaceTraversal2D creating Places..." );
		Places places = new Places( 1, Matrix.class.getName(), ( Object ) new Integer( 0 ), x, y );
		MASS.getLogger().debug( "Places created" );
		
		// instruct all places to return the hostnames of the machines on which they reside
		Object[] placeCallAllObjs = new Object[ x * y ];
		MASS.getLogger().debug( "SpaceTraversal2D sending callAll to Places..." );
		Object[] calledPlacesResults = ( Object[] ) places.callAll( Matrix.GET_HOSTNAME, placeCallAllObjs );
		MASS.getLogger().debug( "Places callAll operation complete" );
		
		// create Agents (number of Agents = x * y in this case), in Places
		MASS.getLogger().debug( "SpaceTraversal2D creating Agents..." );
		Agents agents = new Agents( 1, Nomad2D.class.getName(), null, places, x );
		MASS.getLogger().debug( "Agents created" );

		// instruct all Agents to return the hostnames of the machines on which they reside
		Object[] agentsCallAllObjs = new Object[ x ];
		MASS.getLogger().debug( "SpaceTraversal2D sending callAll to Agents..." );
		Object[] calledAgentsResults = ( Object[] ) agents.callAll( Nomad2D.GET_HOSTNAME, agentsCallAllObjs );
		MASS.getLogger().debug( "Agents callAll operation complete" );
		
		// move all Agents across the Z dimension to cover all Places
		for (int i = 0; i < y; i ++) {
			
			// tell Agents to move
			MASS.getLogger().debug( "SpaceTraversal2D instructs all Agents to migrate..." );
			agents.callAll(Nomad2D.MIGRATE);
			MASS.getLogger().debug( "Agent migration complete" );
			
			// sync all Agent status
			MASS.getLogger().debug( "SpaceTraversal2D sending manageAll to Agents..." );
			agents.manageAll();
			MASS.getLogger().debug( "Agents manageAll operation complete" );
			
			// find out where they live now
			MASS.getLogger().debug( "SpaceTraversal2D sending callAll to Agents..." );
			calledAgentsResults = ( Object[] ) agents.callAll( Nomad2D.GET_HOSTNAME, agentsCallAllObjs );
			MASS.getLogger().debug( "Agents callAll operation complete" );
			
		}
		
		// find out where all of the Agents wound up when all movements complete
		MASS.getLogger().debug( "SpaceTraversal2D sending callAll to Agents to get final landing spot..." );
		calledAgentsResults = ( Object[] ) agents.callAll(Nomad2D.GET_HOSTNAME, agentsCallAllObjs );
		MASS.getLogger().debug( "Agents callAll operation complete" );
		
		// orderly shutdown
		MASS.getLogger().debug( "SpaceTraversal2D instructs MASS library to finish operations..." );
		MASS.finish();
		MASS.getLogger().debug( "MASS library has stopped" );
		
		// calculate / display execution time
		long execTime = new Date().getTime() - startTime;
		System.out.println( "Execution time = " + execTime + " milliseconds" );
		
	 }
	 
}
