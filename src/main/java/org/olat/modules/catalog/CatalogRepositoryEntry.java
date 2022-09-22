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
package org.olat.modules.catalog;

import java.util.List;
import java.util.Set;

import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CatalogRepositoryEntry {

	Long getKey();

	String getExternalId();

	String getExternalRef();

	String getDisplayname();

	String getDescription();

	String getTeaser();

	String getAuthors();
	
	String getMainLanguage();
	
	String getLocation();

	RepositoryEntryEducationalType getEducationalType();

	String getExpenditureOfWork();

	RepositoryEntryLifecycle getLifecycle();

	RepositoryEntryStatusEnum getStatus();

	boolean isPublicVisible();
	
	OLATResource getOlatResource();
	
	Set<TaxonomyLevel> getTaxonomyLevels();
	
	boolean isMember();
	
	boolean isOpenAccess();
	
	List<OLATResourceAccess> getResourceAccess();

}
