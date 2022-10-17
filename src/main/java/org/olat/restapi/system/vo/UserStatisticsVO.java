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
@XmlRootElement(name = "userStatisticsVO")
public class UserStatisticsVO {

	@XmlAttribute(name="totalUserCount", required=true)
	private long totalUserCount;
	@XmlAttribute(name="activeUserCount", required=false)
	private long activeUserCount;
	@XmlAttribute(name="activeUserCountLastDay", required=false)
	private long activeUserCountLastDay;
	@XmlAttribute(name="activeUserCountLastWeek", required=false)
	private long activeUserCountLastWeek;
	@XmlAttribute(name="activeUserCountLastMonth", required=false)
	private long activeUserCountLastMonth;
	@XmlAttribute(name="activeUserCountLast6Month", required=false)
	private long activeUserCountLast6Month;
	@XmlAttribute(name="externalUserCount", required=false)
	private long externalUserCount;
	@XmlAttribute(name="blockedUserCount", required=false)
	private long blockedUserCount;
	@XmlAttribute(name="deletedUserCount", required=false)
	private long deletedUserCount;
	@XmlAttribute(name="totalGroupCount", required=true)
	private long totalGroupCount;

	public long getTotalUserCount() {
		return totalUserCount;
	}
	
	public void setTotalUserCount(long totalUserCount) {
		this.totalUserCount = totalUserCount;
	}
	
	public long getActiveUserCount() {
		return activeUserCount;
	}
	
	public void setActiveUserCount(long activeUserCount) {
		this.activeUserCount = activeUserCount;
	}

	public long getActiveUserCountLastDay() {
		return activeUserCountLastDay;
	}

	public void setActiveUserCountLastDay(long activeUserCountLastDay) {
		this.activeUserCountLastDay = activeUserCountLastDay;
	}

	public long getActiveUserCountLastWeek() {
		return activeUserCountLastWeek;
	}

	public void setActiveUserCountLastWeek(long activeUserCountLastWeek) {
		this.activeUserCountLastWeek = activeUserCountLastWeek;
	}

	public long getActiveUserCountLastMonth() {
		return activeUserCountLastMonth;
	}

	public void setActiveUserCountLastMonth(long activeUserCountLastMonth) {
		this.activeUserCountLastMonth = activeUserCountLastMonth;
	}

	public long getActiveUserCountLast6Month() {
		return activeUserCountLast6Month;
	}

	public void setActiveUserCountLast6Month(long activeUserCountLast6Month) {
		this.activeUserCountLast6Month = activeUserCountLast6Month;
	}

	public long getExternalUserCount() {
		return externalUserCount;
	}

	public void setExternalUserCount(long externalUserCount) {
		this.externalUserCount = externalUserCount;
	}

	public long getBlockedUserCount() {
		return blockedUserCount;
	}

	public void setBlockedUserCount(long blockedUserCount) {
		this.blockedUserCount = blockedUserCount;
	}

	public long getDeletedUserCount() {
		return deletedUserCount;
	}

	public void setDeletedUserCount(long deletedUserCount) {
		this.deletedUserCount = deletedUserCount;
	}

	public long getTotalGroupCount() {
		return totalGroupCount;
	}
	
	public void setTotalGroupCount(long totalGroupCount) {
		this.totalGroupCount = totalGroupCount;
	}
	
	

}
