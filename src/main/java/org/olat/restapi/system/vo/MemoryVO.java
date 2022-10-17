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

import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * <h3>Description:</h3>

 * Initial Date:  21 juin 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "memoryVO")
public class MemoryVO {

	@XmlAttribute(name="date", required=true)
	private Date date;

	@XmlAttribute(name="totalMem", required=true)
	private long totalMem;
	@XmlAttribute(name="totalUsed", required=true)
	private long totalUsed;
	@XmlAttribute(name="maxAvailable", required=true)
	private long maxAvailable;
	
	public MemoryVO() {
		//make JAXB happy
	}
	
	public MemoryVO(long totalMem, long totalUsed, long maxAvailable) {
		this.totalMem = totalMem;
		this.totalUsed = totalUsed;
		this.maxAvailable = maxAvailable;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getTotalMem() {
		return totalMem;
	}

	public void setTotalMem(long totalMem) {
		this.totalMem = totalMem;
	}

	public long getTotalUsed() {
		return totalUsed;
	}

	public void setTotalUsed(long totalUsed) {
		this.totalUsed = totalUsed;
	}

	public long getMaxAvailable() {
		return maxAvailable;
	}

	public void setMaxAvailable(long maxAvailable) {
		this.maxAvailable = maxAvailable;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Memory[totalUsed=").append(totalUsed).append(':')
			.append("totalMem=").append(totalMem).append(':')
			.append("maxAvailable").append(maxAvailable).append(']');
		return sb.toString();
	}

}
