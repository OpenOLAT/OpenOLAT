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
package org.olat.course.assessment.manager;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.group.BusinessGroupRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public interface UserCourseInformationsManager {
	
	static final Instant ACTIVITY_EPOCH = LocalDateTime.of(2026, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC);
	
	long getNewnessWeight();
	
	long getNewnessHalflifeInSeconds();

	/**
	 * The newness weight scaled to the same base as the stored activity
	 * (relative to ACTIVITY_EPOCH): N * 2 ^ ((now - EPOCH) / H)
	 *
	 * @param now The current date
	 * @return The scaled newness weight
	 */
	double getNewnessScale(Date now);

	double calculateActivity(int visit, Date now);
	
	/**
	 * Convert the stored activity (relative to ACTIVITY_EPOCH) to the
	 * effective activity at the specified date: activity / 2 ^ ((now - EPOCH) / H)
	 * 
	 * @param activity The stored activity
	 * @param now The current date
	 * @return The effective activity, a launch today is worth 1.0
	 */
	double getEffectiveActivity(double activity, Date now);
	
	/**
	 * Calculate the relevance of a course for a user. It's the effective activity
	 * plus a newness bonus which fades with the time since the membership and
	 * disappears after the user launched the course more than 2 times:<br>
	 * relevance = activity_eff + N * v(visit) * 2 ^ (-(now - memberSince) / Hn)
	 * <br>
	 * The value is on the effective base (not relative to ACTIVITY_EPOCH),
	 * the order is the same as the order of the relevance sort in the database.
	 * 
	 * @param activity The stored activity (relative to ACTIVITY_EPOCH)
	 * @param visit The number of launches of the user
	 * @param memberSince The earliest membership date of the user (can be null, no newness bonus)
	 * @param now The current date
	 * @return The relevance
	 */
	double calculateRelevance(double activity, int visit, Date memberSince, Date now);
	
	public List<UserCourseInformations> getUserCourseInformations(IdentityRef identity);
	
	public UserCourseInformations getUserCourseInformations(OLATResource resource, IdentityRef identity);

	public List<UserCourseInformations> getUserCourseInformations(IdentityRef identity, List<OLATResource> resources);
	
	public void updateUserCourseInformations(OLATResource courseResource, Identity identity);
	
	public void incrementUserCourseInformationsRun(OLATResource courseResource, Identity identity);
	
	public Date getInitialLaunchDate(OLATResource resource, IdentityRef identity);
	
	public Date getInitialLaunchDate(RepositoryEntryRef entry, IdentityRef identity);
	
	public Date getInitialParticipantLaunchDate(RepositoryEntryRef entry, BusinessGroupRef businessGroup);
	
	public Date getRecentLaunchDate(OLATResource resource, IdentityRef identity);
	
	/**
	 * Get all last launch date for all visitors.
	 * @param resource
	 * @return
	 */
	public Map<Long,Date> getRecentLaunchDates(OLATResource resource);
	
	public Map<Long,Date> getRecentLaunchDates(OLATResource resource, List<Identity> identities);
	
	/**
	 * Return the initial launch dates of a list of users.
	 * @param entry
	 * @param identities
	 * @return
	 */
	public Map<Long,Date> getInitialLaunchDates(RepositoryEntry entry, Collection<? extends IdentityRef> identities);
	
	/**
	 * Return all initial launch dates of a course.
	 * 
	 * @param courseResourceId
	 * @return
	 */
	public Map<Long,Date> getInitialLaunchDates(Long courseResourceId);
	
	public Map<Long,Date> getInitialLaunchDates(OLATResource resource);
	
	/**
	 * 
	 * @param resource The resource of the repository entry
	 * @param identities A list of users
	 * @return Map with the key of the user and its number of runs
	 */
	public Map<Long,Long> getCourseRuns(OLATResource resource, List<Identity> identities);
	
	public int deleteUserCourseInformations(RepositoryEntry entry);

}
