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
package org.olat.modules.portfolio.model.export;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.modules.ceditor.ContentElementType;

/**
 * 
 * Initial date: 23 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SectionXML {

	private String title;
	private String description;

	private Date beginDate;
	private Date endDate;

	private List<AssignmentXML> assignments;
	
	public SectionXML() {
		//
	}
	
	public ContentElementType getType() {
		return ContentElementType.section;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getBeginDate() {
		return beginDate;
	}

	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public List<AssignmentXML> getAssignments() {
		if(assignments == null) {
			assignments = new ArrayList<>();
		}
		return assignments;
	}

	public void setAssignments(List<AssignmentXML> assignments) {
		this.assignments = assignments;
	}

}
