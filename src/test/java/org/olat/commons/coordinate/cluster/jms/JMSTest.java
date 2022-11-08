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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
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
	
	private static final Logger log = Tracing.createLoggerFor(JMSTest.class);


	@Test
	public void testSendReceive() {
		// enable test only if we have the cluster configuration enabled.
		// this test requires that an JMS Provider is running
		// (see file serviceconfig/org/olat/core/_spring/coreextconfig.xml)
		EventBus bus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		if (bus instanceof ClusterEventBus) {
			Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("jms");
			
			// send and wait some time until a message should arrive at the latest.
			final OLATResourceable ores = OresHelper.createOLATResourceableInstance("hellojms", Long.valueOf(123));
			final CountDownLatch doneSignal = new CountDownLatch(1);
			
			bus.registerFor(new GenericEventListener() {
				@Override
				public void event(Event event) {
					log.info(Tracing.M_AUDIT, "Event received: " + event);
					doneSignal.countDown();
				}
			}, id, ores);
			
			MultiUserEvent mue = new MultiUserEvent("amuecommand");
			bus.fireEventToListenersOf(mue, ores);
			
			try {
				boolean interrupt = doneSignal.await(5, TimeUnit.SECONDS);
				assertTrue("Test takes too long (more than 5s)", interrupt);
			} catch (InterruptedException e) {
				fail("" + e.getMessage());
			}
		}
	}
}