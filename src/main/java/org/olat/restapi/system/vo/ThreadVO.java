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

import org.olat.admin.sysinfo.model.ThreadView;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  6 mai 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "threadVO")
public class ThreadVO implements Comparable<ThreadVO> {

	@XmlAttribute(name="id", required=true)
	private long id;
	@XmlAttribute(name="name", required=true)
	private String name;
	@XmlAttribute(name="cpuUsage")
	private float cpuUsage;
	@XmlAttribute(name="cpuUsagePercent")
	private String cpuUsagePercent;
	@XmlAttribute(name="cpuTime")
	private long cpuTime;
	
	public ThreadVO() {
		//make JAXB happy
	}
	
	public ThreadVO(Long id, String name, float cpuUsage) {
		this.id = id;
		this.name = name;
		this.cpuUsage = cpuUsage;
	}
	
	public ThreadVO(ThreadView view) {
		this(view.getId(), view.getName(), view.getCpuUsage());
		cpuUsagePercent = view.getCpuUsagePercent();
		cpuTime = view.getCpuTime();
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public float getCpuUsage() {
		return cpuUsage;
	}
	
	public void setCpuUsage(float cpuUsage) {
		this.cpuUsage = cpuUsage;
	}

	public String getCpuUsagePercent() {
		return cpuUsagePercent;
	}

	public void setCpuUsagePercent(String cpuUsagePercent) {
		this.cpuUsagePercent = cpuUsagePercent;
	}

	public long getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}

	@Override
	public int compareTo(ThreadVO o) {
		return name.compareToIgnoreCase(o.name);
	}

	@Override
	public int hashCode() {
		return (int)id;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ThreadVO) {
			ThreadVO thread = (ThreadVO)obj;
			return id == thread.id;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ThreadVO[id=").append(id).append(":name=").append(name).append(":cpu=").append(cpuUsagePercent).append("]");
		return sb.toString();
	}
}