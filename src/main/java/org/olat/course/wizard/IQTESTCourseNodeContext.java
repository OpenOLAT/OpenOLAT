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
package org.olat.course.wizard;

import java.util.Date;

import org.olat.course.nodes.CourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10 Dec 2020<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IQTESTCourseNodeContext implements IQTESTCourseNodeDefaults, AssessmentModeDefaults {

	private String longTitle;
	private String shortTitle;
	private String description;
	private RepositoryEntry referencedEntry;
	private CourseNode courseNode;
	private ModuleConfiguration moduleConfig;
	private boolean enabled = true;
	private String name;
	private Date begin;
	private Date end;
	private int leadTime;
	private int followUpTime;
	private boolean manualBeginEnd;

	@Override
	public String getLongTitle() {
		return longTitle;
	}

	@Override
	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	@Override
	public String getShortTitle() {
		return shortTitle;
	}

	@Override
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public RepositoryEntry getReferencedEntry() {
		return referencedEntry;
	}

	@Override
	public void setReferencedEntry(RepositoryEntry referencedEntry) {
		this.referencedEntry = referencedEntry;
	}

	public CourseNode getCourseNode() {
		return courseNode;
	}

	public void setCourseNode(CourseNode courseNode) {
		this.courseNode = courseNode;
	}

	@Override
	public ModuleConfiguration getModuleConfig() {
		return moduleConfig;
	}

	@Override
	public void setModuleConfig(ModuleConfiguration moduleConfig) {
		this.moduleConfig = moduleConfig;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Date getBegin() {
		return begin;
	}
	
	@Override
	public void setBegin(Date begin) {
		this.begin = begin;
	}
	
	@Override
	public Date getEnd() {
		return end;
	}
	
	@Override
	public void setEnd(Date end) {
		this.end = end;
	}
	
	@Override
	public int getLeadTime() {
		return leadTime;
	}

	@Override
	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}

	@Override
	public int getFollowUpTime() {
		return followUpTime;
	}

	@Override
	public void setFollowUpTime(int followUpTime) {
		this.followUpTime = followUpTime;
	}

	@Override
	public boolean isManualBeginEnd() {
		return manualBeginEnd;
	}

	@Override
	public void setManualBeginEnd(boolean manualBeginEnd) {
		this.manualBeginEnd = manualBeginEnd;
	}
	
}
