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
package org.olat.search.service.indexer;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IndexWriterHolder {
	private static final Logger log = Tracing.createLoggerFor(IndexWriterHolder.class);
	
	private Directory indexPath;
	private JmsIndexer indexer;
	
	private AtomicInteger counter = new AtomicInteger(0);
	private IndexWriter writerRef;

	public IndexWriterHolder(Directory indexPath, JmsIndexer indexer)
	throws IOException {
		this.indexPath = indexPath;
		this.indexer = indexer;
	}
	
	/**
	 * @return true if it has created the index
	 */
	public synchronized boolean ensureIndexExists() {
		boolean created = false;
		IndexWriter writer = null;
		try {
			if(!DirectoryReader.indexExists(indexPath)) {
				writer = getAndLock();
				created = true;
			}
		} catch (IOException e) {
			log.error("",  e);
		} finally {
			release(writer);
		}
		return created;
	}

	public synchronized IndexWriter getAndLock() throws IOException {
		if(writerRef == null) {
			long start = System.nanoTime();
			IndexWriter indexWriter = new IndexWriter(indexPath, indexer.newIndexWriterConfig());
			if(!DirectoryReader.indexExists(indexPath)) {
				indexWriter.commit();//make sure it exists
			}
			log.info("Opening writer takes (ms): " + CodeHelper.nanoToMilliTime(start));
			writerRef = indexWriter;
		}
		counter.incrementAndGet();
		return writerRef;
	}

	public synchronized void release(IndexWriter indexWriter) {
		if(indexWriter != null) {
			try {
				int used = counter.decrementAndGet();
				if(used == 0) {
					long start = System.nanoTime();
					indexWriter.commit();
					indexWriter.close();
					writerRef = null;
					log.info("Close writer takes (ms): " + CodeHelper.nanoToMilliTime(start));
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}
	
	public synchronized void close() {
		IndexWriter indexWriter = writerRef;
		if(indexWriter != null) {
			try {
				indexWriter.commit();
				indexWriter.close();
			} catch (IOException e) {
				log.error("", e);
			}
		}
	}
}
