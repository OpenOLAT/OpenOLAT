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
package org.olat.core.commons.services.text;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextServiceTest extends OlatTestCase {

	private static final OLog log = Tracing.createLoggerFor(TextServiceTest.class);
	
	@Autowired
	private TextService textService;
	
	@Test
	public void detectLocale_french() {
		Locale locale = textService.detectLocale("Bonjour, je parle français.");
		Assert.assertNotNull(locale);
		Assert.assertEquals("fr", locale.getLanguage());
	}
	
	@Test
	public void detectLocale_english() {
		Locale locale = textService.detectLocale("Hi, I speak British english.");
		Assert.assertNotNull(locale);
		Assert.assertEquals("en", locale.getLanguage());
	}
	
	@Test
	public void concurrentDetection() throws InterruptedException {
		int numOfThreads = 10;
		CountDownLatch latch = new CountDownLatch(numOfThreads);
		NGramThread[] threads = new NGramThread[numOfThreads];
		for(int i=numOfThreads; i-->0; ) {
			threads[i] = new NGramThread(textService, latch);
		}
		
		for(int i=numOfThreads; i-->0; ) {
			threads[i].start();
		}

		latch.await(120, TimeUnit.SECONDS);
		
		for(int i=numOfThreads; i-->0; ) {
			if(threads[i].getException() != null) {
				log.error("", threads[i].getException());
			}
			Assert.assertNull(threads[i].getException());
		}
	}

	private static class NGramThread extends Thread {

		private final TextService service;
		private final CountDownLatch latch;
		
		private Exception exception;
		
		public NGramThread(TextService service, CountDownLatch latch) {
			this.service = service;
			this.latch = latch;
		}
		
		public Exception getException() {
			return exception;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(100);
				for(int i=0; i<2500; i++) {
					Locale locale = service.detectLocale("Bonjour, je parle français.");
					Assert.assertNotNull(locale);
					
				}
			} catch (Exception e) {
				exception = e;
			} finally {
				latch.countDown();
			}
		}
	}

}
