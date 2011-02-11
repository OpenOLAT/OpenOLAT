package org.olat.instantMessaging;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;
import org.olat.test.MockServletContextWebContextLoader;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Instant Messaging integration JUnit Tests, relying on a working IM OpenFire Jabberserver 
 */
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = {
		"classpath:org/olat/instantMessaging/_spring/instantMessagingTestContext.xml" })
public class IMUnitTestWithoutOLAT extends AbstractJUnit4SpringContextTests {

	@Test
	public void testNormal() {
		/**
		 * Precondition
		 */
		IMConfig config = (IMConfig) applicationContext.getBean("org.olat.im.IMConfig");
		assertNotNull(config);
		// only run IM tests if enabled
		assumeTrue(config.isEnabled());

		/**
		 * test
		 */
		InstantMessaging im = (InstantMessaging) applicationContext.getBean("org.olat.instantMessaging.InstantMessaging");
		assertNotNull(im);
		assertTrue(im.getConfig().isEnabled());
		
		String username = "unittest";
		String password = "test";
		String fullname = "test test";
		String email = "@test.ch";
		String groupId = "testgroup-1234556";
		String groupname = "testgroupABC";
		
		groupId = im.getNameHelper().getGroupnameForOlatInstance(groupId);

		// test api functions that do not need OLAT runtime
		String tmpUsermaster = username + 0;
		for (int j = 0; j < 4; j++) {
			String tmpUsername = username + j;
			assertFalse(im.hasAccount(tmpUsername));
			assertTrue(im.createAccount(tmpUsername, password, fullname, username + j + email));
			assertTrue(im.hasAccount(tmpUsername));
			assertTrue(im.addUserToFriendsRoster(tmpUsermaster, groupId, groupname, tmpUsername));
		}
		assertTrue(im.renameRosterGroup(groupId, groupname + "CDEF"));
		assertTrue(im.removeUserFromFriendsRoster(groupId, tmpUsermaster));
		assertTrue(im.removeUserFromFriendsRoster(groupId, username + 1));
		assertTrue(im.deleteRosterGroup(groupId));
		for (int j = 0; j < 4; j++) {
			String tmpUsername = username + j;
			assertTrue(im.deleteAccount(tmpUsername));
		}	
	}

}