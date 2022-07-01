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
package org.olat.modules.taxonomy;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TaxonomyLevel extends TaxonomyLevelRef, CreateInfo, ModifiedInfo, OLATResourceable {
	
	public String getIdentifier();
	
	public void setIdentifier(String identifier);
	
	public String getI18nSuffix();
	
	public String getExternalId();
	
	public void setExternalId(String externalId);
	
	public Integer getSortOrder();
	
	public void setSortOrder(Integer order);

	public String getManagedFlagsString();
	
	public TaxonomyLevelManagedFlag[] getManagedFlags();
	
	public void setManagedFlags(TaxonomyLevelManagedFlag[] flags);
	
	public String getMaterializedPathKeys();
	
	public String getMaterializedPathIdentifiers();
	
	public String getMaterializedPathIdentifiersWithoutSlash();
	
	public Taxonomy getTaxonomy();
	
	public TaxonomyLevel getParent();
	
	public TaxonomyLevelType getType();
	
	public void setType(TaxonomyLevelType type);

}
