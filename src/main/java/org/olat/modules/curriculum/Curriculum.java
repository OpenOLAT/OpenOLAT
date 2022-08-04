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
package org.olat.modules.curriculum;

import org.olat.basesecurity.Group;
import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Curriculum extends CreateInfo, ModifiedInfo, CurriculumRef {
	
	public String getIdentifier();
	
	public void setIdentifier(String identifier);
	
	public String getDisplayName();
	
	public void setDisplayName(String displayName);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public String getStatus();
	
	public void setStatus(String status);
	
	public String getDegree();
	
	public void setDegree(String degree);
	
	public String getExternalId();
	
	public void setExternalId(String externalId);
	
	public boolean isLecturesEnabled();
	
	public void setLecturesEnabled(boolean enabled);
	
	public Organisation getOrganisation();
	
	public void setOrganisation(Organisation organisation);
	
	public CurriculumManagedFlag[] getManagedFlags();
	
	public void setManagedFlags(CurriculumManagedFlag[] flags);
	
	public Group getGroup();

}
