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
 * <h3>Description:</h3>
 * 
 * 
 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "memoryStatisticsVO")
public class MemoryStatisticsVO {

	@XmlAttribute(name="usedMemory", required=true)
	private long usedMemory;
	@XmlAttribute(name="freeMemory", required=true)
	private long freeMemory;
	@XmlAttribute(name="totalMemory", required=true)
	private long totalMemory;

	@XmlAttribute(name="initHeap", required=true)
	private long initHeap;
	@XmlAttribute(name="usedHeap", required=true)
	private long usedHeap;
	@XmlAttribute(name="committedHeap", required=true)
	private long committedHeap;
	@XmlAttribute(name="maxHeap", required=true)
	private long maxHeap;
	
	@XmlAttribute(name="initNonHeap", required=true)
	private long initNonHeap;
	@XmlAttribute(name="usedNonHeap", required=true)
	private long usedNonHeap;
	@XmlAttribute(name="committedNonHeap", required=true)
	private long committedNonHeap;
	@XmlAttribute(name="maxNonHeap", required=true)
	private long maxNonHeap;
	
	@XmlAttribute(name="garbageCollectionTime", required=true)
	private long garbageCollectionTime;
	@XmlAttribute(name="garbageCollectionCount", required=true)
	private long garbageCollectionCount;
	
	
	public MemoryStatisticsVO() {
		//make JAXB happy
	}

	public long getUsedMemory() {
		return usedMemory;
	}

	public void setUsedMemory(long usedMemory) {
		this.usedMemory = usedMemory;
	}

	public long getFreeMemory() {
		return freeMemory;
	}

	public void setFreeMemory(long freeMemory) {
		this.freeMemory = freeMemory;
	}

	public long getTotalMemory() {
		return totalMemory;
	}

	public void setTotalMemory(long totalMemory) {
		this.totalMemory = totalMemory;
	}

	public long getInitHeap() {
		return initHeap;
	}

	public void setInitHeap(long initHeap) {
		this.initHeap = initHeap;
	}

	public long getUsedHeap() {
		return usedHeap;
	}

	public void setUsedHeap(long usedHeap) {
		this.usedHeap = usedHeap;
	}

	public long getCommittedHeap() {
		return committedHeap;
	}

	public void setCommittedHeap(long committedHeap) {
		this.committedHeap = committedHeap;
	}

	public long getMaxHeap() {
		return maxHeap;
	}

	public void setMaxHeap(long maxHeap) {
		this.maxHeap = maxHeap;
	}

	public long getInitNonHeap() {
		return initNonHeap;
	}

	public void setInitNonHeap(long initNonHeap) {
		this.initNonHeap = initNonHeap;
	}

	public long getUsedNonHeap() {
		return usedNonHeap;
	}

	public void setUsedNonHeap(long usedNonHeap) {
		this.usedNonHeap = usedNonHeap;
	}

	public long getCommittedNonHeap() {
		return committedNonHeap;
	}

	public void setCommittedNonHeap(long committedNonHeap) {
		this.committedNonHeap = committedNonHeap;
	}

	public long getMaxNonHeap() {
		return maxNonHeap;
	}

	public void setMaxNonHeap(long maxNonHeap) {
		this.maxNonHeap = maxNonHeap;
	}

	public long getGarbageCollectionTime() {
		return garbageCollectionTime;
	}

	public void setGarbageCollectionTime(long garbageCollectionTime) {
		this.garbageCollectionTime = garbageCollectionTime;
	}

	public long getGarbageCollectionCount() {
		return garbageCollectionCount;
	}

	public void setGarbageCollectionCount(long garbageCollectionCount) {
		this.garbageCollectionCount = garbageCollectionCount;
	}
}