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

import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CurriculumElementType extends CurriculumElementTypeRef, CreateInfo, ModifiedInfo {
	
	public String getIdentifier();
	
	public void setIdentifier(String identifier);
	
	public String getDisplayName();
	
	public void setDisplayName(String displayName);
	
	public String getCssClass();
	
	public void setCssClass(String cssClass);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public String getExternalId();
	
	public void setExternalId(String externalId);

	/**
	 * @return true if calendars aggregation is available for curriculum elements of this type.
	 */
	public CurriculumCalendars getCalendars();
	
	public void setCalendars(CurriculumCalendars calendars);
	
	/**
	 * @return true if absence management aggregation is available for curriculum elements of this type.
	 */
	public CurriculumLectures getLectures();
	
	public void setLectures(CurriculumLectures lectures);
	
	public CurriculumElementTypeManagedFlag[] getManagedFlags();
	
	public void setManagedFlags(CurriculumElementTypeManagedFlag[] flags);
	
	public Set<CurriculumElementTypeToType> getAllowedSubTypes();

}
