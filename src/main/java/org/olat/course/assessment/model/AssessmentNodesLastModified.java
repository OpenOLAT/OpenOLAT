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
package org.olat.course.assessment.model;

import java.util.Date;

/**
 * 
 * Initial date: 14 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentNodesLastModified {
	
	private Date lastModified;
	private Date lastUserModified;
	private Date lastCoachModified;
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public void addLastModified(Date date) {
		if(date == null) return;
		
		if(lastModified == null) {
			lastModified = date;
		} else if(lastModified != null && lastModified.before(date)) {
			lastModified = date;
		}
	}
	
	public Date getLastUserModified() {
		return lastUserModified;
	}
	
	public void addLastUserModified(Date date) {
		if(date == null) return;
		
		if(lastUserModified == null) {
			lastUserModified = date;
		} else if(lastUserModified != null && lastUserModified.before(date)) {
			lastUserModified = date;
		}
	}
	
	public Date getLastCoachModified() {
		return lastCoachModified;
	}
	
	public void addLastCoachModified(Date date) {
		if(date == null) return;
		
		if(lastCoachModified == null) {
			lastCoachModified = date;
		} else if(lastCoachModified != null && lastCoachModified.before(date)) {
			lastCoachModified = date;
		}
	}
}
