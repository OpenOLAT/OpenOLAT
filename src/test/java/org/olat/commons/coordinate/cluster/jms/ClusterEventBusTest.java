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

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.wildfly.common.Assert;

/**
 * Description: <br>
 * 
 * @author Felix Jost
 */
public class ClusterEventBusTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(ClusterEventBusTest.class);


	@Test
	public void testSendReceive() {
		// enable test only if we have the cluster configuration enabled.
		// this test requires that an JMS Provider is running
		// (see file serviceconfig/org/olat/core/_spring/coreextconfig.xml)
		EventBus bus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		Assert.assertTrue(bus instanceof ClusterEventBus);
		
		Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("jms");
		
		// send and wait some time until a message should arrive at the latest.
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance("hellojms", Long.valueOf(123));
		final CountDownLatch standardSignal = new CountDownLatch(1);
		final CountDownLatch assessmentModeSignal = new CountDownLatch(6);
		final CountDownLatch clusterInfoSignal = new CountDownLatch(1);
		
		bus.registerFor(new GenericEventListener() {
			@Override
			public void event(Event event) {
				log.info("Event received: {}", event);
				if("amuecommand".equals(event.getCommand())) {
					standardSignal.countDown();
				} else if(event instanceof AssessmentModeNotificationEvent) {
					assessmentModeSignal.countDown();
				} else if(event instanceof ClusterInfoEvent) {
					clusterInfoSignal.countDown();
				}
			}
		}, id, ores);
		
		MultiUserEvent mue = new MultiUserEvent("amuecommand");
		bus.fireEventToListenersOf(mue, ores);
		
		sendAssessmentModeEvent(AssessmentModeNotificationEvent.BEFORE, bus, ores);
		sendAssessmentModeEvent(AssessmentModeNotificationEvent.LEADTIME, bus, ores);
		sendAssessmentModeEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, bus, ores);
		sendAssessmentModeEvent(AssessmentModeNotificationEvent.STOP_WARNING, bus, ores);
		sendAssessmentModeEvent(AssessmentModeNotificationEvent.STOP_ASSESSMENT, bus, ores);
		sendAssessmentModeEvent(AssessmentModeNotificationEvent.END, bus, ores);
		
		ClusterInfoEvent clusterInfos = new ClusterInfoEvent(null, null);
		bus.fireEventToListenersOf(clusterInfos, ores);
		
		try {
			boolean interruptAssessmentMode = assessmentModeSignal.await(15, TimeUnit.SECONDS);
			assertTrue("Test of assessment mode consumer takes too long (more than 15s)", interruptAssessmentMode);
			boolean interruptStandard = standardSignal.await(15, TimeUnit.SECONDS);
			assertTrue("Test of standard consumer takes too long (more than 15s)", interruptStandard);
			boolean interruptClusterInfo = clusterInfoSignal.await(15, TimeUnit.SECONDS);
			assertTrue("Test of culster-infos consumer takes too long (more than 15s)", interruptClusterInfo);
		} catch (InterruptedException e) {
			fail("" + e.getMessage());
		}
	}
	
	private void sendAssessmentModeEvent(String command, EventBus bus, OLATResourceable ores) {
		AssessmentModeNotificationEvent assessmentModeEvent = new AssessmentModeNotificationEvent(command,
				null, new HashSet<>(), new HashMap<>());
		bus.fireEventToListenersOf(assessmentModeEvent, ores);
	}
}