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

package org.olat.commons.coordinate.cluster.jms;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class JMSTest extends OlatTestCase {
	private static boolean isInitialized = false;
	private static Identity id1;

	private Event event;

	@Before
	public void setup() throws Exception {
		if (isInitialized == false) {
			id1 = JunitTestHelper.createAndPersistIdentityAsUser("jms" + UUID.randomUUID().toString());
			DBFactory.getInstance().closeSession();
			isInitialized = true;
		}
	}
	
	@After
	public void tearDown() throws Exception {
		DBFactory.getInstance().closeSession();
	}

	/**
	 * 
	 */
	@Test
	public void testSendReceive() {
		// enable test only if we have the cluster configuration enabled.
		// this test requires that an JMS Provider is running
		// (see file serviceconfig/org/olat/core/_spring/coreextconfig.xml)
		EventBus bus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		if (bus instanceof ClusterEventBus) {
			// send and wait some time until a message should arrive at the latest.
			OLATResourceable ores1 = OresHelper.createOLATResourceableInstance("hellojms", new Long(123));
			
			bus.registerFor(new GenericEventListener(){

				public void event(Event event) {
					System.out.println("event received!"+event);
					JMSTest.this.event = event;
				}}, id1, ores1);
			
			
			MultiUserEvent mue = new MultiUserEvent("amuecommand");
			bus.fireEventToListenersOf(mue, ores1);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			assertNotNull("after 2 secs, an answer from the jms should have arrived", event);
		}
		// else no tests to pass here
	}
}