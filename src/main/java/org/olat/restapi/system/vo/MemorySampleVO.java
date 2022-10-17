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


@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "memorySampleVO")
public class MemorySampleVO {

	@XmlAttribute(name="date", required=true)
	private Date date;
	
	private MemoryVO memory;
	private MemoryPoolVO[] memoryPools;
	
	public MemorySampleVO() {
		//make JAXB happy
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public MemoryVO getMemory() {
		return memory;
	}

	public void setMemory(MemoryVO memory) {
		this.memory = memory;
	}

	public MemoryPoolVO[] getMemoryPools() {
		return memoryPools;
	}

	public void setMemoryPools(MemoryPoolVO[] memoryPools) {
		this.memoryPools = memoryPools;
	}
}
