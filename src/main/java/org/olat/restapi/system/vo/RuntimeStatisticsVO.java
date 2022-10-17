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

import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.Date;

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
@XmlRootElement(name = "runtimeVO")
public class RuntimeStatisticsVO {

	@XmlAttribute(name="systemLoadAverage", required=true)
	private double systemLoadAverage;
	@XmlAttribute(name="startTime", required=true)
	private Date startTime;
	@XmlAttribute(name="upTime", required=true)
	private long upTime;

	private ClasseStatisticsVO classes;
	private ThreadStatisticsVO threads;
	private MemoryStatisticsVO memory;
	
	public RuntimeStatisticsVO() {
		//make JAXB happy
	}
	
	public RuntimeStatisticsVO(OperatingSystemMXBean bean, RuntimeMXBean runtime) {
		systemLoadAverage = bean.getSystemLoadAverage();
		startTime = new Date(runtime.getStartTime());
		upTime = runtime.getUptime();
	}

	public double getSystemLoadAverage() {
		return systemLoadAverage;
	}

	public void setSystemLoadAverage(double systemLoadAverage) {
		this.systemLoadAverage = systemLoadAverage;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public long getUpTime() {
		return upTime;
	}

	public void setUpTime(long upTime) {
		this.upTime = upTime;
	}

	public ClasseStatisticsVO getClasses() {
		return classes;
	}

	public void setClasses(ClasseStatisticsVO classes) {
		this.classes = classes;
	}

	public ThreadStatisticsVO getThreads() {
		return threads;
	}

	public void setThreads(ThreadStatisticsVO threads) {
		this.threads = threads;
	}

	public MemoryStatisticsVO getMemory() {
		return memory;
	}

	public void setMemory(MemoryStatisticsVO memory) {
		this.memory = memory;
	}
	
	
}
