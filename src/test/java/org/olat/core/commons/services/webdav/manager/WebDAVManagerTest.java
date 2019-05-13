/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.webdav.manager;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * 
 * Initial date: 21.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebDAVManagerTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(WebDAVManagerTest.class);

	@Autowired
	private DB dbInstance;
	@Autowired
	private WebDAVManagerImpl webDAVManager;
	
	@Test
	public void handleBasicAuthentication() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("dav-user-" + UUID.randomUUID().toString());

		String credentialsClearText = id.getName() + ":" + JunitTestHelper.PWD;
		String credentials = StringHelper.encodeBase64(credentialsClearText);
		HttpServletRequest request = new MockHttpServletRequest();
		UserSession usess = webDAVManager.handleBasicAuthentication(credentials, request);
		Assert.assertNotNull(usess);
		dbInstance.commit();
	}
	
	@Test
	public void testSetIdentityAsActiv() {
		Identity id = JunitTestHelper.createAndPersistIdentityAsUser("dav-user-" + UUID.randomUUID().toString());
		String credentialsClearText = id.getName() + ":" + JunitTestHelper.PWD;
		String credentials = StringHelper.encodeBase64(credentialsClearText);
		
		final int maxLoop = 50; // => 4000 x 11ms => 44sec => finished in 50sec
		final int numThreads = 10;
		// Let two thread call UserDeletionManager.setIdentityAsActiv
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));

		CountDownLatch latch = new CountDownLatch(numThreads);
		ActivThread[] threads = new ActivThread[numThreads];
		for(int i=0; i<threads.length;i++) {
			threads[i] = new ActivThread(maxLoop, latch, exceptionHolder, webDAVManager, credentials);
		}

		for(int i=0; i<threads.length;i++) {
			threads[i].start();
		}
		
		try {
			latch.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			exceptionHolder.add(e);
		}
		
		// if not -> they are in deadlock and the db did not detect it
		for (Exception exception : exceptionHolder) {
			log.error("exception: ", exception);
		}
		assertTrue("Exceptions #" + exceptionHolder.size(), exceptionHolder.size() == 0);				
	}
	
	private static class ActivThread extends Thread {
		
		private final int maxLoop;
		private final CountDownLatch countDown;
		private final List<Exception> exceptionHolder;
		private final WebDAVManagerImpl webDAVManager;
		private final String credentials;
		
		public ActivThread(int maxLoop, CountDownLatch countDown, List<Exception> exceptionHolder, WebDAVManagerImpl webDAVManager, String credentials) {
			this.maxLoop = maxLoop;
			this.countDown = countDown;
			this.exceptionHolder = exceptionHolder;
			this.webDAVManager = webDAVManager;
			this.credentials = credentials;
		}
		
		@Override
		public void run() {
			try {
				sleep(10);
				for (int i=0; i<maxLoop; i++) {
					try {
						HttpServletRequest request = new MockHttpServletRequest();
						webDAVManager.handleBasicAuthentication(credentials, request);
						DBFactory.getInstance().commit();
					} catch (Exception e) {
						exceptionHolder.add(e);
					} finally {
						try {
							DBFactory.getInstance().commitAndCloseSession();
						} catch (Exception e) {
							// ignore
						}
					}
				}
			} catch (Exception e) {
				exceptionHolder.add(e);
			} finally {
				countDown.countDown();
			}
		}
	}
}