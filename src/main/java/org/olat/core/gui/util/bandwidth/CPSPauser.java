/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.gui.util.bandwidth;

/**
 * inspired from http://jakarta.apache.org/jmeter
 * for the licence see: http://www.apache.org/licenses/LICENSE-2.0
 * 
 *
 */
/**
 * 
 * Generate appropriate pauses for a given CPS (characters per second)
 */
public class CPSPauser{
    private int CPS; // Characters per second to emulate
    
    // Conversions for milli and nano seconds
    private static final int MS_PER_SEC = 1000;
    private static final int NS_PER_SEC = 1000000000;
    private static final int NS_PER_MS  = NS_PER_SEC/MS_PER_SEC;
    
    /**
     * Create a pauser with the appropriate speed settings.
     * 
     * @param cps CPS to emulate
     */
    public CPSPauser(int cps){
    	CPS=cps;
    }
    
    /**
     * sets the character-per-second for the outputstream
     * @param cps -1 meaning no pause
     */
    public void setCPS(int cps) {
    	CPS = cps;
    }

    /**
     * Pause for an appropriate time according to the number of bytes being transferred.
     * 
     * @param bytes number of bytes being transferred
     */
    public void pause(int bytes){
    	if (CPS == -1) return;
    	long sleepMS = (bytes*MS_PER_SEC)/CPS;
    	int  sleepNS = ((bytes*MS_PER_SEC)/CPS) % NS_PER_MS;
        try {
            Thread.sleep(sleepMS,sleepNS);
        } catch (InterruptedException ignored) {
        	//
        }
    }

		/**
		 * @return
		 */
		public boolean isOff() {
			return CPS == -1;
		}
}