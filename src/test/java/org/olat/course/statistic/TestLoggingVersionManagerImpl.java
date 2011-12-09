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
*/
package org.olat.course.statistic;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.olat.test.OlatTestCase;

public class TestLoggingVersionManagerImpl extends OlatTestCase {

	private LoggingVersionManager loggingVersionManager_;
	
	
	
	@Before
	public void setUp() throws Exception {
		loggingVersionManager_ = new LoggingVersionManagerImpl();
	}
	
	@Test
	public void testIllegalValues() throws Exception {
		try{
			loggingVersionManager_.setLoggingVersionStartingNow(-1);
			fail("LoggingVersionManagerImpl.setLoggingVersionStartingNow didn't complain about version being -1");
		} catch(RuntimeException e) {
			// this is okay
		}
		try{
			loggingVersionManager_.setLoggingVersionStartingNow(0);
			fail("LoggingVersionManagerImpl.setLoggingVersionStartingNow didn't complain about version being 0");
		} catch(RuntimeException e) {
			// this is okay
		}
		try{
			loggingVersionManager_.setLoggingVersion(-1, -1);
			fail("LoggingVersionManagerImpl.setLoggingVersion didn't complain about version being -1");
		} catch(RuntimeException e) {
			// this is okay
		}
		try{
			loggingVersionManager_.setLoggingVersion(0, -1);
			fail("LoggingVersionManagerImpl.setLoggingVersion didn't complain about version being 0");
		} catch(RuntimeException e) {
			// this is okay
		}

		try{
			loggingVersionManager_.setLoggingVersion(1, -1);
			fail("LoggingVersionManagerImpl.setLoggingVersion didn't complain about startingTimeMillis being -1");
		} catch(RuntimeException e) {
			// this is okay
		}
	}
	
	@Test
	public void testSetAndGet() throws Exception {
		loggingVersionManager_.setLoggingVersion(1, 1);
		assertEquals("set and get version failed", 1, loggingVersionManager_.getStartingTimeForVersion(1));
		assertEquals("set and get version failed", 1, loggingVersionManager_.getStartingTimeForVersion(1));
		loggingVersionManager_.setLoggingVersion(1, 2);
		assertEquals("set and get version failed", 2, loggingVersionManager_.getStartingTimeForVersion(1));
		assertEquals("set and get version failed", 2, loggingVersionManager_.getStartingTimeForVersion(1));

		loggingVersionManager_.setLoggingVersion(11, 11);
		assertEquals("set and get version failed", 11, loggingVersionManager_.getStartingTimeForVersion(11));

		loggingVersionManager_.setLoggingVersion(17, 13);
		assertEquals("set and get version failed", 13, loggingVersionManager_.getStartingTimeForVersion(17));
	}
	
}
