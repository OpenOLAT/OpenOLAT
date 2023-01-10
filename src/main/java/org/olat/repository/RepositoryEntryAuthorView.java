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
package org.olat.repository;

import java.util.Date;

import org.olat.core.id.OLATResourceable;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryEntryAuthorView extends OLATResourceable, RepositoryEntryLight {
	
	public Date getCreationDate();
	
	public String getSoftkey();
	
	public String getExternalId();
	
	public String getExternalRef();
	
	public RepositoryEntryManagedFlag[] getManagedFlags();
	
	public boolean isCanIndexMetadata();
	
	public String getTechnicalType();

	public String getAuthor();
	
	public String getAuthors();
	
	public String getLocation();
	
	public RepositoryEntryEducationalType getEducationalType();
	
	public OLATResource getOlatResource();
	
	public RepositoryEntryLifecycle getLifecycle();
	
	public int getNumOfReferences();
	
	public int getNumOfCurriculumElements();
	
	public boolean isLectureEnabled();
	
	public boolean isRollCallEnabled();
	
	public Date getDeletionDate();
	
	public String getDeletedByFullName();
	
	/**
	 * @return True if the user has bookmarked this entry
	 */
	public boolean isMarked();
	
	/**
	 * @return The date of the last launch
	 */
	public Date getLastUsage();
	
	/**
	 * @return True if some offers are set
	 */
	public boolean isOfferAvailable();
}
