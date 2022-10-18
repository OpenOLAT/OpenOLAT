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
package org.olat.repository.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.LockModeType;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.commentAndRating.UserCommentsDelegate;
import org.olat.core.commons.services.commentAndRating.UserRatingsDelegate;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.commons.services.commentAndRating.manager.UserRatingsDAO;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.model.RepositoryEntryStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryStatisticsDAO implements UserRatingsDelegate, UserCommentsDelegate {

	@Autowired
	private DB dbInstance;
	@Autowired
	private UserRatingsDAO userRatingsDao;
	@Autowired
	private UserCommentsDAO userCommentsDao;
	
	@PostConstruct
	public void init() {
		userRatingsDao.addDegelate(this);
		userCommentsDao.addDelegate(this);
	}
	
	/**
	 * Increment the launch counter.
	 * @param re
	 */
	public void incrementLaunchCounter(RepositoryEntry re) {
		String updateQuery = "update repoentrystats set launchCounter=launchCounter+1, lastUsage=:now where key=:statsKey";
		int updated = dbInstance.getCurrentEntityManager().createQuery(updateQuery)
			.setParameter("statsKey", re.getStatistics().getKey())
			.setParameter("now", new Date())
			.executeUpdate();
		if(updated > 0) {
			dbInstance.commit();//big performance improvement
		}
	}

	/**
	 * Increment the download counter.
	 * @param re
	 */
	public void incrementDownloadCounter(RepositoryEntry re) {
		String updateQuery = "update repoentrystats set downloadCounter=downloadCounter+1, lastUsage=:now where key=:statsKey";
		int updated = dbInstance.getCurrentEntityManager().createQuery(updateQuery)
			.setParameter("statsKey", re.getStatistics().getKey())
			.setParameter("now", new Date())
			.executeUpdate();
		if(updated > 0) {
			dbInstance.commit();//big performance improvement
		}
	}

	/**
	 * Set last-usage date to now for the specified repository entry
	 * with a granularity of 1 minute.
	 * @param 
	 */
	public void setLastUsageNowFor(RepositoryEntry re) {
		if (re == null) return;
		
		Date newUsage = new Date();
		if(re.getStatistics().getLastUsage().getTime() + 60000 < newUsage.getTime()) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.MINUTE, -1);
			Date limit = cal.getTime();

			String updateQuery = "update repoentrystats set lastUsage=:now where key=:statsKey and lastUsage<:limit";
			int updated = dbInstance.getCurrentEntityManager().createQuery(updateQuery)
				.setParameter("statsKey", re.getStatistics().getKey())
				.setParameter("now", newUsage)
				.setParameter("limit", limit)
				.executeUpdate();
			if(updated > 0) {
				dbInstance.commit();//big performance improvement
			}
		}
	}
	
	protected RepositoryEntryStatistics loadStatistics(OLATResourceable repositoryEntryRes) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v.statistics from ").append(RepositoryEntry.class.getName()).append(" as v")
		  .append(" where v.key=:key");
		
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryStatistics.class)
				.setParameter("key", repositoryEntryRes.getResourceableId())
				.getSingleResult();
	}
	
	private RepositoryEntryStatistics loadStatisticsForUpdate(OLATResourceable repositoryEntryRes) {
		if(repositoryEntryRes instanceof RepositoryEntry) {
			RepositoryEntry re = (RepositoryEntry)repositoryEntryRes;
			dbInstance.getCurrentEntityManager().detach(re);
			dbInstance.getCurrentEntityManager().detach(re.getStatistics());
		}
		
		StringBuilder sb = new StringBuilder(256);
		sb.append("select stats from ").append(RepositoryEntryStatistics.class.getName()).append(" as stats")
		  .append(" where stats.key in (select v.statistics.key from ").append(RepositoryEntry.class.getName()).append(" as v where v.key=:key)");
		
		List<RepositoryEntryStatistics> statistics = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), RepositoryEntryStatistics.class)
				.setParameter("key", repositoryEntryRes.getResourceableId())
				.setLockMode(LockModeType.PESSIMISTIC_WRITE)
				.getResultList();
		return statistics == null || statistics.isEmpty() ? null : statistics.get(0);
	}

	@Override
	public boolean accept(OLATResourceable ores, String resSubPath) {
		if("RepositoryEntry".equals(ores.getResourceableTypeName()) && resSubPath == null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean update(OLATResourceable ores, String resSubPath, double newAverageRating, long numOfRatings) {
		RepositoryEntryStatistics statistics = loadStatisticsForUpdate(ores);
		if(statistics != null) {
			statistics.setRating(newAverageRating);
			statistics.setNumOfRatings(numOfRatings);
			statistics.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(statistics);
			return true;
		}
		return false;
	}

	@Override
	public boolean update(OLATResourceable ores, String resSubPath, int numOfComments) {
		RepositoryEntryStatistics statistics = loadStatisticsForUpdate(ores);
		if(statistics != null) {
			statistics.setNumOfComments(numOfComments);
			statistics.setLastModified(new Date());
			dbInstance.getCurrentEntityManager().merge(statistics);
			return true;
		}
		return false;
	}
}