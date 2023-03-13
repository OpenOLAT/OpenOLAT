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
package org.olat.modules.project;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 9 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ProjMilestone extends ProjMilestoneRef, ModifiedInfo, CreateInfo {
	
	public static final String TYPE = "ProjectMilestone";
	
	public String getIdentifier();
	
	public void setIdentifier(String identifier);
	
	public ProjMilestoneStatus getStatus();
	
	public void setStatus(ProjMilestoneStatus status);
	
	public Date getDueDate();

	void setDueDate(Date dueDate);
	
	public String getSubject();

	void setSubject(String subject);
	
	public String getDescription();

	void setDescription(String description);
	
	public String getColor();

	void setColor(String color);
	
	public ProjArtefact getArtefact();

}
