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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.license.License;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntryEducationalType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;

/**
 * 
 * Initial date: 24 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface CatalogEntry {
	
	Long getRepositoryEntryKey();

	Long getCurriculumElementKey();

	String getExternalId();

	String getExternalRef();

	String getDisplayname();

	String getDescription();

	String getTeaser();

	String getAuthors();
	
	String getMainLanguage();
	
	String getLocation();
	
	String getTechnicalType();

	RepositoryEntryEducationalType getEducationalType();

	String getExpenditureOfWork();

	String getLifecycleLabel();

	String getLifecycleSoftKey();
	
	Date getLifecycleStart();
	
	Date getLifecycleEnd();
	
	RepositoryEntryStatusEnum getRepositoryEntryStatus();
	
	CurriculumElementStatus getCurriculumElementStatus();
	
	Date getPublishedDate();

	boolean isPublicVisible();
	
	Long getCurriculumKey();
	
	Long getCurriculumElementTypeKey();
	
	String getCurriculumElementTypeName();
	
	OLATResource getOlatResource();
	
	Set<TaxonomyLevel> getTaxonomyLevels();
	
	boolean isHasCertificate();

	String getCreditPointAmount();
	
	boolean isMember();
	
	boolean isParticipant();

	boolean isReservationAvailable();
	
	boolean isOpenAccess();
	
	boolean isGuestAccess();
	
	Long getMaxParticipants();
	
	Long getNumParticipants();
	
	Integer getSortPriority();
	
	List<OLATResourceAccess> getResourceAccess();
	
	License getLicense();
	
	boolean isSingleCourseImplementation();
	
	Long getSingleCourseEntryKey();
	
	RepositoryEntryStatusEnum getSingleCourseEntryStatus();
	
	Integer getMyRating();
	
	Double getAverageRating();

	Long getNumOfRatings();

	Long getNumOfComments();
	
}
