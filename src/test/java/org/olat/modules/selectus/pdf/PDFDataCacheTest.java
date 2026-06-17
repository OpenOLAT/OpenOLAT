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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.modules.selectus.pdf;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.test.OlatTestCase;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  5 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PDFDataCacheTest extends OlatTestCase {
	
	private static final Logger log = Tracing.createLoggerFor(PDFDataCacheTest.class);
	
	/**
	 * Test that the cache can cache something
	 */
	@Test
	public void fillCache() {
		final PDFDataCache cache = new PDFDataCache();
		cache.setUseCache(true);
		cache.setRootCache(getCleanCacheRoot());
		cache.init();
		
		String key = "test-1";
		
		Date creationDate1 = new Date();
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		TestDataProvider provider1 = new TestDataProvider(creationDate1);
		cache.stream(key, provider1, out1);
		Assert.assertTrue(provider1.isCreated());

		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		TestDataProvider provider2 = new TestDataProvider(creationDate1);
		cache.stream(key, provider2, out2);
		Assert.assertFalse(provider2.isCreated());
		
		Assert.assertNotNull(out1.toByteArray());
		Assert.assertNotNull(out2.toByteArray());
		Assert.assertTrue(out1.toByteArray().length > 100);
		Assert.assertTrue(out2.toByteArray().length > 100);
	}
	
	/*
	 * test the invalidation and that no zombie stay on the disk
	 */
	@Test
	public void invalidateCache() {
		final PDFDataCache cache = new PDFDataCache();
		cache.setUseCache(true);
		cache.setRootCache(getCleanCacheRoot());
		cache.init();
		
		String key = "test-2";
		
		Date creationDate1 = new Date();
		ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		TestDataProvider provider1 = new TestDataProvider(creationDate1);
		cache.stream(key, provider1, out1);
		Assert.assertTrue(provider1.isCreated());

		Date creationDate2 = new Date();
		ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		TestDataProvider provider2 = new TestDataProvider(creationDate2);
		cache.stream(key, provider2, out2);
		Assert.assertTrue(provider2.isCreated());
		
		int count = 0;
		for(File cacheFile:cache.getRootCache().listFiles()) {
			if(cacheFile.getName().startsWith(key)) {
				count++;
			}
		}
		Assert.assertEquals(1, count);
		
		Assert.assertNotNull(out1.toByteArray());
		Assert.assertNotNull(out2.toByteArray());
		Assert.assertTrue(out1.toByteArray().length > 100);
		Assert.assertTrue(out2.toByteArray().length > 100);
	}
	
	@Test
	public void testLocking() {
		final PDFDataCache cache = new PDFDataCache();
		cache.setUseCache(true);
		cache.setRootCache(getCleanCacheRoot());
		cache.init();
		
		final String key = "test-3";
		final Date creationDate = new Date();
		final ByteArrayOutputStream out1 = new ByteArrayOutputStream();
		final TestDataProvider longProvider = new LongTestDataProvider(creationDate);
		final CountDownLatch countDown = new CountDownLatch(2);
		
		Thread longThread = new Thread(new Runnable() {
			@Override
			public void run() {
				cache.stream(key, longProvider, out1);
				countDown.countDown();
			}
		});
		
		final ByteArrayOutputStream out2 = new ByteArrayOutputStream();
		final TestDataProvider shortProvider = new TestDataProvider(creationDate);
		Thread shortThread = new Thread(new Runnable() {
			@Override
			public void run() {
				cache.stream(key, shortProvider, out2);
				countDown.countDown();
			}
		});
		
		try {
			longThread.start();
			//to be sure that long thread is the first to hit the cache
			Thread.sleep(100);
			shortThread.start();
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		// sleep until t1 and t2 should have terminated/excepted
		try {
			countDown.await(120, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			log.error("", e);
		}
		
		Assert.assertNotNull("Output 1 is null", out1.toByteArray());
		Assert.assertNotNull("Output 2 is null", out2.toByteArray());
		Assert.assertTrue("Output 1 is too small", out1.toByteArray().length > 100);
		Assert.assertTrue("Output 2 is too small", out2.toByteArray().length > 100);
		Assert.assertFalse("Short has created the cached file",  shortProvider.isCreated());
		Assert.assertTrue("Long has not created the cached file",  longProvider.isCreated());
		
		int count = 0;
		for(File cacheFile:cache.getRootCache().listFiles()) {
			if(cacheFile.getName().startsWith(key)) {
				count++;
			}
		}
		Assert.assertEquals("Number of cached file not equals to 1", 1, count);
	}
	
	private File getCleanCacheRoot() {
		File rootCache = new File(new File(WebappHelper.getUserDataRoot(), "tmp"), "testbig");
		if(rootCache.exists()) {
			FileUtils.deleteDirsAndFiles(rootCache, true, false);
		} else {
			rootCache.mkdirs();
		}
		return rootCache;
	}

	private class LongTestDataProvider extends TestDataProvider {
		public LongTestDataProvider(Date lastModified) {
			super(lastModified);
		}

		@Override
		public void createBigData(ZipOutputStream zipOut) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				log.error("", e);
			}
			super.createBigData(zipOut);
		}
	}
	
	private class TestDataProvider implements PDFDataProvider {
		private final Date lastModified;
		private boolean created = false;
		
		public TestDataProvider(Date lastModified) {
			this.lastModified = lastModified;
		}

		public boolean isCreated() {
			return created;
		}

		@Override
		public Long sizeNeeded() {
			return Long.valueOf(20 * 1024 * 1024);
		}

		@Override
		public Date getLastModified() {
			return lastModified;
		}

		@Override
		public void createBigData(ZipOutputStream zipOut) {
			created = true;
			try {
				zipOut.putNextEntry(new ZipEntry("Hello"));
				for(int i=100000; i-->0; ) {
					zipOut.write((byte)54);
				}
				zipOut.closeEntry();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}
}
