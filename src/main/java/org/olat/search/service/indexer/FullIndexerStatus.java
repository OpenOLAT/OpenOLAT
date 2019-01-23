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

package org.olat.search.service.indexer;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.search.service.document.file.FileDocumentFactory;

/**
 * Status of full indexer.
 * @author Christian Guretzki
 */
public class FullIndexerStatus {

	public static final String STATUS_STOPPED  = "stopped";
	public static final String STATUS_RUNNING  = "running";
	public static final String STATUS_FINISHED = "finished";
	public static final String STATUS_DISABLED = "disabled";

	private long fullIndexStartedAt = 0;
	private long lastFullIndexTime = 0;
	private long indexingTime;
	private String status = STATUS_STOPPED;
	
	private int documentCount = 0;
	private int indexSize = 0;
	private int indexPerMinute = 0;
	private int[] partDocumentCounters;
	private int documentQueueSize;
	private int numberRunningFolderIndexer = 0;
	private int numberAvailableFolderIndexer = 0;
	
	/** Hashtable with document-type-names as key and Integer-object as counters. */
	private Map<String,Integer> documentCounters;
	/** Hashtable with file-type-names as key and Integer-object as counters. */
	private Map<String,Integer> fileTypeCounters;
		
	public FullIndexerStatus(int numberOfPartDocumentCounters) {
		partDocumentCounters = new int[numberOfPartDocumentCounters];
		documentCounters = new Hashtable<>();
		fileTypeCounters = new Hashtable<>();
	}
	

	/**
	 * @return Returns the indexingTime.
	 */
	public long getIndexingTime() {
		return indexingTime;
	}

	/**
	 * @return Returns the lastFullIndexTime.
	 */
	public String getLastFullIndexDateString() {
		if (lastFullIndexTime > 0) {
			return new Date(lastFullIndexTime).toString();
		} else {
			// not finished yet
			return "-";
		}
	}
	
	public long getLastFullIndexTime() {
		return lastFullIndexTime;
	}
	
	public void setLastFullIndexTime(long time) {
		lastFullIndexTime = time;
	}

	/**
	 * @return Returns the status.
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @return Returns the fullIndexStartedAt.
	 */
	public String getFullIndexStartedAt() {
		if (fullIndexStartedAt != 0) {
			return new Date(fullIndexStartedAt).toString();
		} else {
			// not finished yet
			return "-";
		}
	}


	/**
	 * @param status The status to set.
	 */
	public void setStatus(String status) {
		this.status = status;
	}

  /**
   * Indexing started. Set starting time, set status to 'running', 
   * reset full-index document-counter and all document-counters. 
   */
	public void indexingStarted() {
		fullIndexStartedAt = System.currentTimeMillis();
		setStatus(STATUS_RUNNING);
		setDocumentCount(0);//Reset FullIndex-DocumentCounter
		resetAllDocumentCounters();
	}

  /**
   * Reset all part document-counters, reset documentCounters and fileTypeCounters.
   */
	private void resetAllDocumentCounters() {
		for (int i = 0; i < partDocumentCounters.length; i++) {
			partDocumentCounters[i] = 0;
		}
		documentCounters = new Hashtable<>();
		fileTypeCounters = new Hashtable<>();
		CoreSpringFactory.getImpl(FileDocumentFactory.class).resetExcludedFileSizeCount();
	}

  /**
   * Indexing finished. Set end time, calculate duration and set status to 'finished'. 
   */
	public void indexingFinished() {
		lastFullIndexTime = System.currentTimeMillis();
		indexingTime = lastFullIndexTime - fullIndexStartedAt;
		setStatus(STATUS_FINISHED);
	}


	public int getDocumentCount() {
		return documentCount;
	}


	public void setDocumentCount(int documentCount) {
		this.documentCount = documentCount;
	}
	
	public void incrementDocumentCount() {
		documentCount++;
	}

	public void setIndexSize(int indexSize) {
		this.indexSize  = indexSize;
	}

	public int getIndexSize() {
		return indexSize;
	}

	public void setIndexPerMinute(int indexPerMinute) {
		this.indexPerMinute = indexPerMinute;
	}
	
	/**
	 * 
	 * @return Return number of indexed elements in the las minute.
	 */
	public int getIndexPerMinute() {
		return indexPerMinute;
	}

	/**
	 * @return Returns the runningFolderIndexer.
	 */
	public int getNumberRunningFolderIndexer() {
		return numberRunningFolderIndexer;
	}
	
	public void setNumberRunningFolderIndexer(int numberRunningFolderIndexer) {
		this.numberRunningFolderIndexer = numberRunningFolderIndexer;
	}

	/**
	 * @return Returns the availableFolderIndexer.
	 */
	public int getNumberAvailableFolderIndexer() {
		return numberAvailableFolderIndexer;
	}
	
	public void setNumberAvailableFolderIndexer(int numberAvailableFolderIndexer) {
		this.numberAvailableFolderIndexer = numberAvailableFolderIndexer;
	}


	public void incrementPartDocumentCount(int id) {
		partDocumentCounters[id]++;
	}
	
	public String getPartDocumentCounters() {
		StringBuilder buf = new StringBuilder();
		for (int i = 0; i < partDocumentCounters.length; i++) {
			if (buf.length() != 0) {
				buf.append(",");
			}
			buf.append(partDocumentCounters[i]);
		}
		return buf.toString();
	}


	public void addDocumentCount(int docCount) {
		documentCount += docCount;
	}


	public void setPartDocumentCount(int docCount, int id) {
		partDocumentCounters[id] = docCount;
	}

	/**
	 * Set document queue size. The queue is used in multi-threaded mode.
	 */
	public void setDocumentQueueSize(int documentQueueSize) {
		this.documentQueueSize = documentQueueSize;
	}
	
	/**
	 * @return  Return document queue size. The queue is used in multi-threaded mode.
	 */
	public int getDocumentQueueSize() {
		return this.documentQueueSize;
	}

	/**
	 * Set new document-type counters Hashtable.
	 * @param fileTypeCounters  New Hashtable with document-type-names as key and Integer-object as counters.
	 */
	public void setDocumentCounters(Map<String,Integer> documentCounters) {
		this.documentCounters = documentCounters;
	}

	/**
	 * @return  Return HTML formatted text with document-type names and counter-values.
	 */
	public String getDocumentCounters() {
		StringBuilder buf = new StringBuilder(1024);
		for (Map.Entry<String, Integer> documentType : documentCounters.entrySet()) {
			Integer counterValue = documentType.getValue();
			buf.append(documentType.getKey());
			buf.append("=");
			buf.append(counterValue.toString());
			buf.append("<br />");
		}
		return buf.toString();
	}

	/**
	 * Set new file-type counters Hashtable.
	 * @param fileTypeCounters  New Hashtable with file-type-names as key and Integer-object as counters.
	 */
	public void setFileTypeCounters(Map<String,Integer> fileTypeCounters) {
		this.fileTypeCounters = fileTypeCounters;
	}

	/**
	 * @return  Return HTML formatted text with file-type names and counter-values.
	 */
	public String getFileTypeCounters() {
		StringBuilder buf = new StringBuilder(1024);
		for (Map.Entry<String, Integer> fileType : fileTypeCounters.entrySet()) {
			Integer counterValue = fileType.getValue();
			buf.append(fileType.getKey());
			buf.append("=");
			buf.append(counterValue.toString());
			buf.append("<br />");
		}
		return buf.toString();
	}

	public int getExcludedDocumentCount() {
		return CoreSpringFactory.getImpl(FileDocumentFactory.class).getExcludedFileSizeCount();
	}

}
