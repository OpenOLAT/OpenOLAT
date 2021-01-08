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

import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.wizard.ReferencableEntryContext;

/**
 * 
 * Initial date: 10 Dec 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IQTESTCourseNodeDefaults implements CourseNodeTitleContext, ReferencableEntryContext {
	
	private String longTitle;
	private String shortTitle;
	private String objectives;
	private RepositoryEntry referencedEntry;
	private ModuleConfiguration moduleConfig;

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
	public String getObjectives() {
		return objectives;
	}

	@Override
	public void setObjectives(String objectives) {
		this.objectives = objectives;
	}

	@Override
	public RepositoryEntry getReferencedEntry() {
		return referencedEntry;
	}

	@Override
	public void setReferencedEntry(RepositoryEntry referencedEntry ) {
		this.referencedEntry = referencedEntry ;
	}

	public ModuleConfiguration getModuleConfig() {
		return moduleConfig;
	}

	public void setModuleConfig(ModuleConfiguration moduleConfig) {
		this.moduleConfig = moduleConfig;
	}

}
