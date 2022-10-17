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

import java.lang.management.ThreadMXBean;

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
@XmlRootElement(name = "threadsVO")
public class ThreadsVO {
	
	@XmlAttribute(name="threadCount", required=true)
	private int threadCount;
	@XmlAttribute(name="daemonCount", required=true)
	private int daemonCount;
	
	public ThreadsVO() {
		//make JAXB happy
	}
	
	public ThreadsVO(ThreadMXBean bean) {
		threadCount = bean.getThreadCount();
		daemonCount = bean.getDaemonThreadCount();
	}

	public int getThreadCount() {
		return threadCount;
	}

	public void setThreadCount(int threadCount) {
		this.threadCount = threadCount;
	}

	public int getDaemonCount() {
		return daemonCount;
	}

	public void setDaemonCount(int daemonCount) {
		this.daemonCount = daemonCount;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Threads[count=").append(getThreadCount()).append(':')
			.append("daemons=").append(getDaemonCount()).append(']');
		return sb.toString();
	}
}
