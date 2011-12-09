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
package org.olat.test.util.selenium;

import com.thoughtworks.selenium.Selenium;
import junit.framework.Assert;

/**
 * @author Lavinia Dumitrescu
 *
 */
public class SeleniumHelper extends Assert {

	/**	 
	 * If timeout exceeded fails.
	 * @param selenium
	 * @param text
	 * @return Returns true if text was present. 
	 * @throws Exception 
	 */
	public static boolean waitUntilTextPresent(Selenium selenium, String text, int timeoutSec) throws Exception {
		for (int second = 0;; second++) {
			if (second >= timeoutSec) fail("timeout");
			try { 
				if (selenium.isTextPresent(text)) 
					return true; 
			} catch (Exception e) {}
			Thread.sleep(1000);
		}		
	}
	
	/**
	 * Checks if text shows up in the first timeoutSec.
	 * @param selenium
	 * @param text
	 * @param timeoutSec
	 * @return Return true if the test was present, else false.
	 */
	public static boolean isTextPresent(Selenium selenium, String text, int timeoutSec) throws Exception {
		for (int second = 0; second <= timeoutSec; second++) {			
			try { 
				if (selenium.isTextPresent(text)) 
					return true; 
			} catch (Exception e) {}
			Thread.sleep(1000);
		}	
		return false;
	}
	
	public static void safeWait(long milliseconds){
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
