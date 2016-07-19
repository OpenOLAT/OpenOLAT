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

import org.olat.core.util.StringHelper;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 19.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseTemplateRow {
	
	private RepositoryEntry courseEntry;
	private PortfolioCourseNode courseNode;
	private RepositoryEntry templateEntry;
	
	public CourseTemplateRow(RepositoryEntry courseEntry,
			PortfolioCourseNode courseNode,
			RepositoryEntry templateEntry) {
		this.courseEntry = courseEntry;
		this.courseNode = courseNode;
		this.templateEntry = templateEntry;
	}
	
	public String getCourseTitle() {
		return courseEntry.getDisplayname();
	}
	
	public RepositoryEntry getCourseEntry() {
		return courseEntry;
	}
	
	public PortfolioCourseNode getCourseNode() {
		return courseNode;
	}
	
	public String getCourseNodeTitle() {
		String title = courseNode.getShortTitle();
		if(!StringHelper.containsNonWhitespace(title)) {
			title = courseNode.getLongTitle();
		}
		return title;
	}
	
	public RepositoryEntry getTemplateEntry() {
		return templateEntry;
	}

}
