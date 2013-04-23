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
package org.olat.admin.sysinfo.model;


/**
 * 
 * Initial date: 19.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ThreadView implements Comparable<ThreadView> {

	private long id;
	private String name;
	private String groupName;
	private float cpuUsage;
	private String cpuUsagePercent;
	private long cpuTime;
	private long prevCpuTime = 0l;
	private int warningCounter = 0;
	private Thread.State state;
	
	
	public ThreadView() {
		//
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
	
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
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

	/**
	 * @return CPU time in nanoseconds
	 */
	public long getCpuTime() {
		return cpuTime;
	}

	public void setCpuTime(long cpuTime) {
		this.cpuTime = cpuTime;
	}

	public long getPrevCpuTime() {
		return prevCpuTime;
	}

	public void setPrevCpuTime(long prevCpuTime) {
		this.prevCpuTime = prevCpuTime;
	}

	public int getWarningCounter() {
		return warningCounter;
	}

	public void setWarningCounter(int warningCounter) {
		this.warningCounter = warningCounter;
	}

	public Thread.State getState() {
		return state;
	}

	public void setState(Thread.State state) {
		this.state = state;
	}

	@Override
	public int compareTo(ThreadView o) {
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
		if(obj instanceof ThreadView) {
			ThreadView thread = (ThreadView)obj;
			return id == thread.id;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ThreadInfo[id=").append(id).append(":name=").append(name).append(":cpu=").append(cpuUsagePercent).append("]");
		return sb.toString();
	}
}