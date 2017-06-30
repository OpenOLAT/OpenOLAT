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
package org.olat.modules.lecture.model;

/**
 * 
 * Initial date: 27 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AggregatedLectureBlocksStatistics {

	private long personalPlannedLectures;
	private long attendedLectures;
	private long authorizedAbsentLectures;
	private long absentLectures;
	private double rate;
	
	public AggregatedLectureBlocksStatistics(long personalPlannedLectures, long attendedLectures,
			long authorizedAbsentLectures, long absentLectures, double rate) {
		this.personalPlannedLectures = personalPlannedLectures;
		this.attendedLectures = attendedLectures;
		this.authorizedAbsentLectures = authorizedAbsentLectures;
		this.absentLectures = absentLectures;
		this.rate = rate;
	}
	
	public long getPersonalPlannedLectures() {
		return personalPlannedLectures;
	}
	
	public void setPersonalPlannedLectures(long personalPlannedLectures) {
		this.personalPlannedLectures = personalPlannedLectures;
	}
	
	public long getAttendedLectures() {
		return attendedLectures;
	}
	
	public void setAttendedLectures(long attendedLectures) {
		this.attendedLectures = attendedLectures;
	}
	
	public long getAuthorizedAbsentLectures() {
		return authorizedAbsentLectures;
	}
	
	public void setAuthorizedAbsentLectures(long authorizedAbsentLectures) {
		this.authorizedAbsentLectures = authorizedAbsentLectures;
	}
	
	public long getAbsentLectures() {
		return absentLectures;
	}
	
	public void setAbsentLectures(long absentLectures) {
		this.absentLectures = absentLectures;
	}
	
	public double getRate() {
		return rate;
	}
	
	public void setRate(double rate) {
		this.rate = rate;
	}
}