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
