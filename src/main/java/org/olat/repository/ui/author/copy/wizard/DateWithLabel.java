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
package org.olat.repository.ui.author.copy.wizard;

import java.util.Date;

/**
 * Initial date: 31.01.2022<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class DateWithLabel {
	
	private Date date;
	private String label;
	private String courseNodeIdentifier;
	private boolean needsTranslation = true;
	
	public DateWithLabel(Date date, String label, String courseNodeIdentifier) {
		this.date = date;
		this.label = label;
		this.courseNodeIdentifier = courseNodeIdentifier;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getCourseNodeIdentifier() {
		return courseNodeIdentifier;
	}
	
	public void setCourseNodeIdentifier(String courseNodeIdentifier) {
		this.courseNodeIdentifier = courseNodeIdentifier;
	}
	
	public void setNeedsTranslation(boolean needsTranslation) {
		this.needsTranslation = needsTranslation;
	}
	
	public boolean needsTranslation() {
		return needsTranslation;
	}
	
}
