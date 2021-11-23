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
import java.util.Set;

import org.olat.core.id.OLATResourceable;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 12.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryEntryMyView extends OLATResourceable {
	
	public Long getKey();
	
	public String getExternalId();
	
	public String getExternalRef();
	
	public String getDisplayname();
	
	public String getDescription();
	
	public Date getCreationDate();
	
	public String getAuthors();
	
	public String getLocation();
	
	public RepositoryEntryEducationalType getEducationalType();
	
	public String getExpenditureOfWork();
	
	public RepositoryEntryStatusEnum getEntryStatus();
	
	public boolean isAllUsers();
	
	public boolean isGuests();
	
	public boolean isBookable();
	
	public OLATResource getOlatResource();
	
	public RepositoryEntryLifecycle getLifecycle();
	
	/**
	 * @return The passed/failed status saved in the efficiency statement
	 */
	public Boolean getPassed();
	
	/**
	 * @return The score saved in the efficiency statement
	 */
	public Float getScore();
	
	/**
	 * @return The completion of the root assessment entry
	 */
	public Double getCompletion();
	
	/**
	 * @return True if the user as bookmarked this entry
	 */
	public boolean isMarked();
	
	/**
	 * @return The rating made by the user or null if the user has never rated the entry
	 */
	public Integer getMyRating();
	
	/**
	 * @return The average rating of this entry, or null if the entry was never rated
	 */
	public Double getAverageRating();
	
	public long getNumOfRatings();
	
	public long getNumOfComments();
	
	public long getLaunchCounter();
	
	/**
	 * @return True if some offers are currently available
	 */
	public boolean isValidOfferAvailable();
	
	public Set<TaxonomyLevel> getTaxonomyLevels();
}
