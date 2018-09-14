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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureRollCallStatus;

/**
 * 
 * Initial date: 14 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureReportRow {
	
	private final Long lectureBlockKey;
	private final String lectureBlockTitle;
	private final Date startDate;
	private final Date endDate;
	private final LectureRollCallStatus rollCallStatus;
	private String externalRef;
	
	private final List<Identity> teachers = new ArrayList<>(2);
	private final List<Identity> owners = new ArrayList<>(2);
	
	public LectureReportRow(LectureBlock lectureBlock) {
		lectureBlockKey = lectureBlock.getKey();
		startDate = lectureBlock.getStartDate();
		endDate = lectureBlock.getEndDate();
		lectureBlockTitle = lectureBlock.getTitle();
		rollCallStatus = lectureBlock.getRollCallStatus();
	}
	
	public Long getKey() {
		return lectureBlockKey;
	}
	
	public String getTitle() {
		return lectureBlockTitle;
	}

	public Date getStartDate() {
		return startDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public LectureRollCallStatus getRollCallStatus() {
		return rollCallStatus;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public void setExternalRef(String externalRef) {
		this.externalRef = externalRef;
	}

	public List<Identity> getTeachers() {
		return teachers;
	}

	public List<Identity> getOwners() {
		return owners;
	}
}
