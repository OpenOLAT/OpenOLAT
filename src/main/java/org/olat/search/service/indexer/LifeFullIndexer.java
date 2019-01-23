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
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LifeFullIndexer {
	
	public void addIndexer(LifeIndexer indexer);
	
	/**
	 * Start a full index
	 */
	public void fullIndex();
	
	public FullIndexerStatus getStatus();
	
	/**
	 * Ask to index the document with the specified key
	 * @param type
	 * @param key
	 */
	public void indexDocument(String type, Long key);
	
	/**
	 * Ask to index a batch of documents by their keys
	 * @param type
	 * @param keyList
	 */
	public void indexDocument(String type, List<Long> keyList);
	
	
	/**
	 * Delete a document
	 * @param type
	 * @param key
	 */
	public void deleteDocument(String type, Long key);
	
	/**
	 * Return the index writer, don't forget to release it.
	 * @return
	 * @throws IOException
	 */
	public IndexWriter getAndLockWriter() throws IOException;
	
	/**
	 * Release the writer, other nodes of a cluster can use it after that.
	 * @param writer
	 */
	public void releaseWriter(IndexWriter writer);
	
	/**
	 * Add a document to the index
	 * @param doc
	 */
	public void addDocuments(List<Document> doc);
	
	/**
	 * 
	 * @param doc
	 * @param writer
	 */
	public void addDocument(Document doc, IndexWriter writer);
	
	/**
	 * 
	 * @param resourceUrl
	 */
	public void deleteDocument(String resourceUrl);

}
