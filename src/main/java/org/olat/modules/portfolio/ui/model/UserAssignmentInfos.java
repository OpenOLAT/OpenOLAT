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
package org.olat.modules.portfolio.ui.model;

import java.io.File;
import java.util.List;

import org.olat.modules.ceditor.Assignment;

/**
 * 
 * Initial date: 20.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserAssignmentInfos {
	
	private final Assignment assignment;
	private final List<File> documents;
	
	public UserAssignmentInfos(Assignment assignment, List<File> documents) {
		this.assignment = assignment;
		this.documents = documents;
	}
	
	public Long getKey() {
		return assignment.getKey();
	}
	
	public String getType() {
		return assignment.getAssignmentType().name();
	}
	
	public String getSummary() {
		return assignment.getSummary();
	}
	
	public String getContent() {
		return assignment.getContent();
	}

	public List<File> getDocuments() {
		return documents;
	}
}
