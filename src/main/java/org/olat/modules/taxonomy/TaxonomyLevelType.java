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

import java.util.Set;

import org.olat.core.id.CreateInfo;

/**
 * 
 * Initial date: 22 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TaxonomyLevelType extends TaxonomyLevelTypeRef, CreateInfo {
	
	public String getIdentifier();
	
	public void setIdentifier(String identifier);
	
	public String getDisplayName();
	
	public void setDisplayName(String displayName);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public String getExternalId();
	
	public void setExternalId(String externalId);
	
	public String getManagedFlagsString();
	
	public TaxonomyLevelTypeManagedFlag[] getManagedFlags();
	
	public void setManagedFlags(TaxonomyLevelTypeManagedFlag[] flags);
	
	public String getCssClass();
	
	public void setCssClass(String cssClass);
	
	public boolean isVisible();
	
	public void setVisible(boolean visible);
	
	public boolean isDocumentsLibraryEnabled();

	public void setDocumentsLibraryEnabled(boolean documentsLibraryEnabled);
	
	public boolean isDocumentsLibraryManageCompetenceEnabled();

	public void setDocumentsLibraryManageCompetenceEnabled(boolean enable);

	public boolean isDocumentsLibraryTeachCompetenceReadEnabled();

	public void setDocumentsLibraryTeachCompetenceReadEnabled(boolean enable);

	public int getDocumentsLibraryTeachCompetenceReadParentLevels();

	public void setDocumentsLibraryTeachCompetenceReadParentLevels(int parentLevels);

	public boolean isDocumentsLibraryTeachCompetenceWriteEnabled();

	public void setDocumentsLibraryTeachCompetenceWriteEnabled(boolean enable);

	public boolean isDocumentsLibraryHaveCompetenceReadEnabled();

	public void setDocumentsLibraryHaveCompetenceReadEnabled(boolean enable);

	public boolean isDocumentsLibraryTargetCompetenceReadEnabled();

	public void setDocumentsLibraryTargetCompetenceReadEnabled(boolean enable);
	
	public Taxonomy getTaxonomy();
	
	public Set<TaxonomyLevelTypeToType> getAllowedTaxonomyLevelSubTypes();
	
	public boolean isAllowedAsCompetence();
	
	public void setAllowedAsCompetence(boolean allowedAsCompetence);

}
