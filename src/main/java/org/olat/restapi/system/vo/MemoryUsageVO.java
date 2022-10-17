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

import java.lang.management.MemoryUsage;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * 
 * <h3>Description:</h3>
 * A mapping class for the MemoryUsage
 * 
 * Initial Date:  21 jun. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "memoryUsageVO")
public class MemoryUsageVO {

	@XmlAttribute(name="init", required=true)
	private long init;
	@XmlAttribute(name="max", required=true)
	private long max;
	@XmlAttribute(name="committed", required=true)
	private long committed;
	@XmlAttribute(name="used", required=true)
	private long used;
	
	public MemoryUsageVO() {
		//make JAXB happy
	}

	public MemoryUsageVO(MemoryUsage usage) {
		init = usage.getInit();
		max = usage.getMax();
		committed = usage.getCommitted();
		used = usage.getUsed();
	}

	public long getInit() {
		return init;
	}

	public void setInit(long init) {
		this.init = init;
	}

	public long getMax() {
		return max;
	}

	public void setMax(long max) {
		this.max = max;
	}

	public long getCommitted() {
		return committed;
	}

	public void setCommitted(long committed) {
		this.committed = committed;
	}

	public long getUsed() {
		return used;
	}

	public void setUsed(long used) {
		this.used = used;
	}
}
