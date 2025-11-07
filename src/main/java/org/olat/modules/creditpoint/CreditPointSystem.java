/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.creditpoint;

import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;

/**
 * 
 * Initial date: 2 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface CreditPointSystem extends ModifiedInfo, CreateInfo {
	
	Long getKey();
	
	String getName();

	void setName(String name);

	String getLabel();

	void setLabel(String label);

	String getDescription();

	void setDescription(String description);
	
	/**
	 * @return Expiration in <strong>days</strong>
	 */
	Integer getDefaultExpiration();

	/**
	 * @param defaultExpiration The expiration specified in <strong>days</strong>
	 */
	void setDefaultExpiration(Integer defaultExpiration);
	
	CreditPointExpirationType getDefaultExpirationUnit();
	
	void setDefaultExpirationUnit(CreditPointExpirationType unit);
	
	CreditPointSystemStatus getStatus();

	void setStatus(CreditPointSystemStatus status);
	
	boolean isOrganisationsRestrictions();

	void setOrganisationsRestrictions(boolean organisationsRestrictions);
	
	boolean isRolesRestrictions();

	void setRolesRestrictions(boolean rolesRestrictions);
	
	Set<CreditPointSystemToOrganisation> getOrganisations();

}
