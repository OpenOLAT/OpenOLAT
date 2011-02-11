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
* <p>
*/ 

package org.olat.core.util.locks;


import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * @author Christian Guretzki
 */
@RunWith(JUnit4.class)
public class SynchManagerTest {

	private static Logger log = Logger.getLogger(SynchManagerTest.class.getName());

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Before public void setup() throws Exception {
	}

	/**
	 * Creates 3 OLAT-resourceable, two with same key, one with an other key.
	 * Compare the resulting synchronization objects.
	 * With the same key, the synch objects must be same objects.
	 * With different key, the synch objects must be different objects.
	 */
	@Test public void testGetSynchLockFor() throws Exception {
		//SynchManager synchManager = SynchManager.getInstance();
		// Create synch-object 1 for ores with key=1
		Long oresKey1 = new Long(1);
		OLATResourceable resource1 = OresHelper.createOLATResourceableInstance(this.getClass(),oresKey1);
		
		// cluster:: gen new test
		/*Object synchObject1 = synchManager.getSynchLockFor(resource1);
    // Create synch-object 2 for ores with key=2
		Long oresKey2 = new Long(2);
		OLATResourceable resource2 = OresHelper.createOLATResourceableInstance(this.getClass(),oresKey2);
		Object synchObject2 = synchManager.getSynchLockFor(resource2);
    // Create synch-object 3 for ores with key=1
		OLATResourceable resource1_1 = OresHelper.createOLATResourceableInstance(this.getClass(),oresKey1);
		Object synchObject1_1 = synchManager.getSynchLockFor(resource1_1);
		
		assertSame("Synch objects instances are not equals !",synchObject1,synchObject1_1);
		assertNotSame("Synch objects instances are not equals !",synchObject1,synchObject2);
		*/
	}

}