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
package org.olat.restapi.system.vo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "indexerStatisticsVO")
public class IndexerStatisticsVO {

	@XmlAttribute(name="indexedDocumentCount", required=false)
	private long indexedDocumentCount;
	@XmlAttribute(name="excludedDocumentCount", required=false)
	private long excludedDocumentCount;
	@XmlAttribute(name="documentQueueSize", required=false)
	private long documentQueueSize;
	@XmlAttribute(name="runningFolderIndexerCount", required=false)
	private long runningFolderIndexerCount;
	@XmlAttribute(name="availableFolderIndexerCount", required=false)
	private long availableFolderIndexerCount;
	@XmlAttribute(name="status", required=true)
	private String status;
	@XmlAttribute(name="indexSize", required=false)
	private long indexSize;
	@XmlAttribute(name="indexingTime", required=false)
	private long indexingTime;
	@XmlAttribute(name="fullIndexStartedAt", required=false)
	private String fullIndexStartedAt;
	@XmlAttribute(name="lastFullIndexTime", required=false)
	private String lastFullIndexTime;
	
	
	public IndexerStatisticsVO() {
		//
	}

	public long getIndexedDocumentCount() {
		return indexedDocumentCount;
	}

	public void setIndexedDocumentCount(long indexedDocumentCount) {
		this.indexedDocumentCount = indexedDocumentCount;
	}

	public long getExcludedDocumentCount() {
		return excludedDocumentCount;
	}

	public void setExcludedDocumentCount(long excludedDocumentCount) {
		this.excludedDocumentCount = excludedDocumentCount;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public long getDocumentQueueSize() {
		return documentQueueSize;
	}

	public void setDocumentQueueSize(long documentQueueSize) {
		this.documentQueueSize = documentQueueSize;
	}

	public long getRunningFolderIndexerCount() {
		return runningFolderIndexerCount;
	}

	public void setRunningFolderIndexerCount(long runningFolderIndexerCount) {
		this.runningFolderIndexerCount = runningFolderIndexerCount;
	}

	public long getAvailableFolderIndexerCount() {
		return availableFolderIndexerCount;
	}

	public void setAvailableFolderIndexerCount(long availableFolderIndexerCount) {
		this.availableFolderIndexerCount = availableFolderIndexerCount;
	}

	public long getIndexSize() {
		return indexSize;
	}

	public void setIndexSize(long indexSize) {
		this.indexSize = indexSize;
	}

	public long getIndexingTime() {
		return indexingTime;
	}

	public void setIndexingTime(long indexingTime) {
		this.indexingTime = indexingTime;
	}

	public String getFullIndexStartedAt() {
		return fullIndexStartedAt;
	}

	public void setFullIndexStartedAt(String fullIndexStartedAt) {
		this.fullIndexStartedAt = fullIndexStartedAt;
	}

	public String getLastFullIndexTime() {
		return lastFullIndexTime;
	}

	public void setLastFullIndexTime(String lastFullIndexTime) {
		this.lastFullIndexTime = lastFullIndexTime;
	}
}
