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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;

/**
 * 
 * Description:<br>
 * Provide a cache for big PDF files.
 * 
 * <P>
 * Initial Date:  5 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PDFDataCache {
	
	private static final Logger log = Tracing.createLoggerFor(PDFDataCache.class);
	
	private File rootCache;
	private boolean useCache = true;

	private final List<Counter> invalidate = new ArrayList<>();
	private final Map<String,Long> positionToSizeMap = new HashMap<>();
	private final Map<String,Counter> cache = new HashMap<>();
	
	public void init() {
		if(rootCache == null) return;
		if(!rootCache.exists()) {
			rootCache.mkdirs();
		}
		File[] cachedFiles = rootCache.listFiles();
		if(cachedFiles == null || cachedFiles.length == 0) return;	
		for(File cachedFile:cachedFiles) {
			String cachedName = cachedFile.getName();
			int index = cachedName.indexOf('_');
			if(cachedName.endsWith(".zip") && index > 0) {
				String key = cachedName.substring(0, index);
				String time = cachedName.substring(index+1, cachedName.indexOf(".zip"));
				Date lastModified = new Date();
				lastModified.setTime(Long.parseLong(time));
				Counter counter = new Counter(key, lastModified);
				counter.getCacheFS().setCachedFile(cachedFile);
				synchronized(cache) {
					cache.put(key, counter);
					positionToSizeMap.put(key, Long.valueOf(cachedFile.length()));
				}
			}
		}
	}
	
	public boolean isUseCache() {
		return useCache;
	}

	public void setUseCache(boolean useCache) {
		this.useCache = useCache;
	}
	
	public File getRootCache() {
		return rootCache;
	}

	public void setRootCache(File rootCache) {
		this.rootCache = rootCache;
	}
	
	public Long getSize(String key) {
		return positionToSizeMap.get(key);
	}
	
	public boolean forceDelete(String key) {
		try {
			synchronized(cache) {
				Counter counter = cache.get(key);
				if(counter != null) {
					counter.invalidate();
				}
				cache.remove(key);
				positionToSizeMap.remove(key);
				if(counter != null) {
					CachedFS cachedFs = counter.getCacheFS();
					if(cachedFs != null) {
						cachedFs.setCachedFile(null);
					}
				}
			}
			
			File[] cachedFiles = rootCache.listFiles();
			if(cachedFiles != null && cachedFiles.length > 0) {
				for(File cachedFile:cachedFiles) {
					if(cachedFile.getName().startsWith(key)) {
						FileUtils.deleteFile(cachedFile);
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot force delete this: " + key, e);
		}
		return true;
	}

	public void invalidate(String key) {
		try {
			synchronized(cache) {
				Counter counter = cache.get(key);
				if(counter == null) {
					return;
				}
				synchronized(counter) {
					counter.invalidate();
					invalidate.add(counter);
				}
				cache.remove(key);
				positionToSizeMap.remove(key);
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	/**
	 * The method doesn't close the out
	 * @param key
	 * @param provider
	 * @param out
	 * @return
	 */
	public Long stream(String key, PDFDataProvider provider, OutputStream out) {
		Date lastModified = provider.getLastModified();
		
		//synchronized -> can stream
		Counter counter = getCounterAndIncrement(key, lastModified);
		Long size = null;
		try {
			if(useCache) {
				CachedFS cachedFs = counter.getCacheFS();
				size = cachedFs.stream(provider, out);
			} else {
				size = createZip(out, provider);
			}
		} catch(Exception e) {
			log.error("Cannot generate batch combined files for: " + key, e);
		} finally {
			synchronized(counter) {
				counter.decrement();
			}
		}
		if(size != null) {
			positionToSizeMap.put(key, size);
		}
		return size;
	}
	
	private void copyHighSpeed(File cachedFile, OutputStream out) {
		try (FileInputStream in = new FileInputStream(cachedFile)) {
			FileUtils.copy(in, out);
		} catch (IOException e) {
			log.error("Error copy", e);
		}
	}
	
	private Counter getCounterAndIncrement(String key, Date lastModified) {
		synchronized(cache) {
			Counter counter = cache.get(key);
			if(counter == null) {
				counter = new Counter(key, lastModified);
				cache.put(key, counter);
			} else if(counter.lastModified.before(lastModified)) {
				synchronized(counter) {
					counter.invalidate();
					invalidate.add(counter);
				}
				counter = new Counter(key, lastModified);
				cache.put(key, counter);
			}
			counter.increment();
			return counter;
		}
	}
	
	private File createCachedZip(File cachedZip, PDFDataProvider provider, OutputStream httpOut) {
		try {
			if(cachedZip.exists()) {
				Files.delete(cachedZip.toPath());
				cachedZip = new File(cachedZip.getAbsolutePath());
				if(!cachedZip.createNewFile()) {
					log.error("Cannot create chached zip: " + cachedZip);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
		
		try(OutputStream output = new FileOutputStream(cachedZip);
			DoubleOutputStream dStream = new DoubleOutputStream(output, httpOut)) {
			createZip(dStream, provider);
			output.flush();
			return cachedZip;
		} catch (Exception e) {
			log.error("Cannot generate zip for all applications combined file", e);
			return null;
		}
	}
	
	private Long createZip(OutputStream output, PDFDataProvider provider) {
		try {
			CounterOutputStream delOutput = new CounterOutputStream(output);
			NoCloseZipOutputStream zipOut = new NoCloseZipOutputStream(delOutput);
			provider.createBigData(zipOut);

			zipOut.flush();
			output.flush();
			zipOut.realClose();
			return Long.valueOf(delOutput.length);
		} catch (Exception e) {
			log.error("Cannot generate zip for all applications combined file", e);
			return null;
		}
	}
	
	private boolean useCache(Long sizeNeeded) {
		if(!useCache) return false;
		if(sizeNeeded == null) return true;
		
		if(!rootCache.exists()) {
			rootCache.mkdirs();
		}
		
		long usableSpace = rootCache.getUsableSpace();
		return (sizeNeeded * 1.5) < usableSpace;
	}
	
	public final class Counter  {
		
		private int count;
		private boolean valid = true;
		private final String key;
		private final Date lastModified;
		private CachedFS cachedFS;
		
		public Counter(String key, Date lastModified) {
			this.key = key;
			this.lastModified = lastModified;
			cachedFS = new CachedFS(key, lastModified);
		}
		
		public CachedFS getCacheFS() {
			return cachedFS;
		}
		
		public void increment() {
			count++;
		}
		
		public void decrement() {
			count--;
			if(count <= 0 && !valid) {
				cachedFS.delete();
			}
		}
		
		public boolean isValid() {
			return valid;
		}
		
		public void invalidate() {
			valid = false;
			if(count == 0) {
				cachedFS.delete();
			}
		}
		
		public boolean isEmpty() {
			return count == 0;
		}
		
		@Override
		public int hashCode() {
			return key.hashCode() + lastModified.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof Counter) {
				Counter cacheKey = (Counter)obj;
				return key.equals(cacheKey.key) && lastModified.equals(cacheKey.lastModified);
			}
			return false;
		}
	}
	
	private class CachedFS {
		private final String key;
		private final Date lastModified;
		private final Lock lock = new ReentrantLock();
		
		private File cachedFile;
		
		public CachedFS(String key, Date lastModified) {
			this.key = key;
			this.lastModified = lastModified;
		}
		
		public Long stream(PDFDataProvider provider, OutputStream out) {
			//block until decision	
			Long size = null;
			try {
				lock.lock();
				if(cachedFile == null || !cachedFile.exists()) {
					if(useCache(provider.sizeNeeded())) {
						File file = createCacheFile();
						//fill the cache
						cachedFile = createCachedZip(file, provider, out);			
						lock.unlock();
						size = Long.valueOf(cachedFile.length());
					} else {
						lock.unlock();
						size = createZip(out, provider);
					}
				} else {
					lock.unlock();
					copyHighSpeed(cachedFile, out);
					size = Long.valueOf(cachedFile.length());
				}
			} catch (Exception e) {
				log.error("Error while streaming", e);
			}
			return size;
		}
		
		public File createCacheFile() {
			if(!rootCache.exists()) {
				rootCache.mkdirs();
			}
			return new File(rootCache, key + "_" + lastModified.getTime() + ".zip");
		}
		
		protected void setCachedFile(File cachedFile) {
			this.cachedFile = cachedFile;
		}
		
		public void delete() {
			File file = createCacheFile();
			if(file != null) {
				FileUtils.deleteFile(file);
			}
		}
	}
	
	public final class DoubleOutputStream  extends OutputStream {
		private boolean open = true;
		private final OutputStream delegate1;
		private final OutputStream delegate2;
		
		public DoubleOutputStream(OutputStream delegate1, OutputStream delegate2) {
			this.delegate1 = delegate1;
			this.delegate2 = delegate2;
		}

		@Override
		public final void write(byte[] b, int off, int len) throws IOException {
			delegate1.write(b, off, len);
			
			if(open) {
				try {
					delegate2.write(b, off, len);
				} catch (Exception e) {
					open = false;
				}
			}
		}

		@Override
		public final void write(byte[] b) throws IOException {
			delegate1.write(b);
			if(open) {
				try {
					delegate2.write(b);
				} catch (Exception e) {
					open = false;
				}
			}
		}

		@Override
		public final void write(int b) throws IOException {
			delegate1.write(b);
			if(open) {
				try {
					delegate2.write(b);
				} catch (Exception e) {
					open = false;
				}
			}
		}
	}
	
	public final class CounterOutputStream  extends OutputStream {
		private final OutputStream delegate;
		private long length = 0;
		
		public CounterOutputStream(OutputStream delegate) {
			this.delegate = delegate;
		}

		@Override
		public final void write(int b) throws IOException {
			delegate.write(b);
			length++;
		}

		@Override
		public final void close() throws IOException {
			delegate.close();
		}

		@Override
		public final void write(byte[] b, int off, int len) throws IOException {
			delegate.write(b, off, len);
			length += len;
		}

		@Override
		public final void write(byte[] b) throws IOException {
			delegate.write(b);
			length += b.length;
		}
		
		public long size() {
			return length;
		}
	}
	
	private final class NoCloseZipOutputStream  extends ZipOutputStream {
		public NoCloseZipOutputStream(OutputStream delegate) {
			super(delegate);
		}

		@Override
		public final void close() throws IOException {
			//do nothing
		}
		
		public void realClose() throws IOException {
			super.close();
		}
	}
}
