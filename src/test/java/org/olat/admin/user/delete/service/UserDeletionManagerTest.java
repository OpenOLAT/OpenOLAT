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

package org.olat.admin.user.delete.service;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;

/**
 * Description: <br>
 * 
 * @author Christian Guretzki
 */
public class UserDeletionManagerTest extends OlatTestCase {
	private static boolean isInitialized = false;
	private static Identity ident;

	@Before
	public void setUp() throws Exception {
		if (isInitialized == false) {
			ident = JunitTestHelper.createAndPersistIdentityAsUser("anIdentity");
			DBFactory.getInstance().closeSession();
			isInitialized = true;
		}
	}

	@Test
	public void testSetIdentityAsActiv() {
		final int maxLoop = 2000; // => 4000 x 11ms => 44sec => finished in 50sec
		// Let two thread call UserDeletionManager.setIdentityAsActiv
		final List<Exception> exceptionHolder = Collections.synchronizedList(new ArrayList<Exception>(1));

		CountDownLatch latch = new CountDownLatch(4);
		ActivThread[] threads = new ActivThread[4];
		for(int i=0; i<threads.length;i++) {
			threads[i] = new ActivThread(maxLoop, latch, exceptionHolder);
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
			System.err.println("exception: "+exception.getMessage());
			exception.printStackTrace();
		}
		assertTrue("Exceptions #" + exceptionHolder.size(), exceptionHolder.size() == 0);				
	}
	
	private static class ActivThread extends Thread {
		
		private final int maxLoop;
		private final CountDownLatch countDown;
		private final List<Exception> exceptionHolder;
		
		public ActivThread(int maxLoop, CountDownLatch countDown, List<Exception> exceptionHolder) {
			this.maxLoop = maxLoop;
			this.countDown = countDown;
			this.exceptionHolder = exceptionHolder;
		}
		
		public void run() {
			try {
				sleep(10);
				for (int i=0; i<maxLoop; i++) {
					try {
						UserDeletionManager.getInstance().setIdentityAsActiv(ident);
					} catch (Exception e) {
						exceptionHolder.add(e);
					} finally {
						try {
							DBFactory.getInstance().closeSession();
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