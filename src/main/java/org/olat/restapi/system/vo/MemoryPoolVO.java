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

import java.lang.management.MemoryPoolMXBean;
import java.util.Date;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * <h3>Description:</h3>
 * A mapping of the memory pool informations
 * <p>
 * Initial Date:  18 jun. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "memoryPoolVO")
public class MemoryPoolVO {

	@XmlAttribute(name="type", required=true)
	private int type;
	@XmlAttribute(name="name", required=true)
	private String name;
	@XmlAttribute(name="date", required=true)
	private Date date;
	
	private MemoryUsageVO usage;
	private MemoryUsageVO peakUsage;
	
	public MemoryPoolVO() {
		//make JAXB happy
	}
	
	public MemoryPoolVO(MemoryPoolMXBean bean) {
		type = bean.getType().ordinal();
		name = bean.getName();
		
		usage = new MemoryUsageVO(bean.getUsage());
		peakUsage = new MemoryUsageVO(bean.getPeakUsage());
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public MemoryUsageVO getUsage() {
		return usage;
	}

	public void setUsage(MemoryUsageVO usage) {
		this.usage = usage;
	}

	public MemoryUsageVO getPeakUsage() {
		return peakUsage;
	}

	public void setPeakUsage(MemoryUsageVO peakUsage) {
		this.peakUsage = peakUsage;
	}
}
