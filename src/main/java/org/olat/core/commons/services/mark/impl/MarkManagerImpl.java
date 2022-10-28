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

package org.olat.core.commons.services.mark.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.commons.services.mark.MarkResourceStat;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

/**
 * Description:<br>
 * Implementation of the MarkManager
 * 
 * <P>
 * Initial Date:  9 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service
public class MarkManagerImpl implements MarkManager {
	
	@Autowired
	private DB dbInstance;

	@Override
	public List<Mark> getMarks(OLATResourceable ores, Identity identity, Collection<String> subPath) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select mrk from mark mrk where ")
		  .append("mrk.resId=:resId and mrk.resName=:resName and mrk.creator=:creator");
		if(subPath != null && !subPath.isEmpty()) {
			sb.append(" and mrk.resSubPath in (:resSubPaths)");
		}
		
		TypedQuery<Mark> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Mark.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setParameter("creator", identity);
		if(subPath != null && !subPath.isEmpty()) {
			query.setParameter("resSubPaths", subPath);
		}
		return query.getResultList();
	}
	
	@Override
	public Set<Long> getMarkResourceIds(Identity identity, String resourceTypeName, Collection<String> subPaths) {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct(mrk.resId) from mark mrk where ")
			.append("mrk.resName=:resName and mrk.creator=:creator");
		if(!subPaths.isEmpty()) {
			sb.append(" and mrk.resSubPath in (:resSubPaths)");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("resName", resourceTypeName)
				.setParameter("creator", identity);
		if(!subPaths.isEmpty()) {
			query.setParameter("resSubPaths", subPaths);
		}
		
		List<Long> results = query.getResultList();
		return new HashSet<>(results);
	}

	@Override
	public List<Mark> getMarks(Identity identity, Collection<String> resourceTypeName) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mrk from mark mrk where ")
			.append("mrk.creator=:creator ");
		if(resourceTypeName != null && !resourceTypeName.isEmpty()) {
			sb.append("and mrk.resName in (:resName)");
		}

		TypedQuery<Mark> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Mark.class)
				.setParameter("creator", identity);
		if(resourceTypeName != null && !resourceTypeName.isEmpty()) {
			query.setParameter("resName", resourceTypeName);
		}

		return query.getResultList();
	}

	@Override
	public List<Long> getMarksResourceId(Identity identity, String resourceTypeName) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mrk.resId from mark mrk")
		  .append(" where mrk.creator=:creator and mrk.resName =:resName");

		return dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("creator", identity)
				.setParameter("resName", resourceTypeName)
				.getResultList();
	}

	@Override
	public void filterMarks(Identity identity, String resourceTypeName, Collection<Long> resIds) {
		if(resIds == null || resIds.isEmpty()) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select mark.resId from ").append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resName=:resName and mark.creator=:creator");
		if(resIds.size() < 50) {//if there is too much resource to filter, retrieve all the mark
			sb.append(" and mark.resId in(:resIds)");
		}

		TypedQuery<Long> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("resName", resourceTypeName)
				.setParameter("creator", identity);
		if(resIds.size() < 50) {
			query.setParameter("resIds", resIds);
		}
		List<Long> markedResIds = query.getResultList();
		resIds.retainAll(markedResIds);
	}

	@Override
	public boolean isMarked(OLATResourceable ores, Identity identity, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mrk.key from mark mrk where ")
			.append("mrk.resId=:resId and mrk.resName=:resName and mrk.creator=:creator");
		if(resSubPath != null) {
			sb.append(" and mrk.resSubPath=:resSubPath");
		}
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setParameter("creator", identity);
		if(resSubPath != null) {
			query.setParameter("resSubPath", resSubPath);
		}

		List<Long> results = query
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return results != null && !results.isEmpty() && results.get(0) != null && results.get(0).longValue() > 0;
	}
	
	@Override
	public Mark setMark(OLATResourceable ores, Identity identity, String subPath, String businessPath) {
		MarkImpl mark = loadMark(ores, identity, subPath);
		if(mark == null) {
			mark = new MarkImpl();
			mark.setCreationDate(new Date());
			mark.setResName(ores.getResourceableTypeName());
			mark.setResId(ores.getResourceableId());
			mark.setResSubPath(subPath);
			mark.setBusinessPath(businessPath);
			mark.setCreator(identity);
			dbInstance.getCurrentEntityManager().persist(mark);
		}
		return mark;
	}
	

	@Override
	public void moveMarks(OLATResourceable ores, String oldSubPath, String newSubPath) {
		//can be a lot of marks to move around
		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(MarkImpl.class.getName()).append(" mark set mark.resSubPath=:newSubPath ")
		  .append("where mark.resId=:resId and mark.resName=:resName and mark.resSubPath=:oldSubPath");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setParameter("oldSubPath", oldSubPath)
				.setParameter("newSubPath", newSubPath)
				.setFlushMode(FlushModeType.AUTO)
				.executeUpdate();
	}

	@Override
	public void removeMark(OLATResourceable ores, Identity identity, String subPath) {
		MarkImpl mark = loadMark(ores, identity, subPath);
		if(mark != null) {
			dbInstance.deleteObject(mark);
		}
	}
	
	@Override
	public void removeMark(Mark mark) {
		removeMark(mark.getOLATResourceable(), mark.getCreator(), mark.getResSubPath());
	}

	private MarkImpl loadMark(OLATResourceable ores, Identity identity, String resSubPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mark from ").append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resId=:resId and mark.resName=:resName and mark.creator=:creator");
		if(resSubPath != null) {
			sb.append(" and mark.resSubPath=:resSubPath");
		}
		
		TypedQuery<MarkImpl> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MarkImpl.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setParameter("creator", identity);
		if(resSubPath != null) {
			query.setParameter("resSubPath", resSubPath);
		}

		List<MarkImpl> results = query.getResultList();
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	@Override
	public void deleteMarks(OLATResourceable ores) {
		String query = "delete from mark mrk where mrk.resId=:resId and mrk.resName=:resName";
		dbInstance.getCurrentEntityManager().createQuery(query)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setFlushMode(FlushModeType.AUTO)
				.executeUpdate();
	}

	/**
	 * Exact match
	 */
	@Override
	public void deleteMarks(OLATResourceable ores, String subPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resId=:resId and mark.resName=:resName");
		if(subPath == null) {
			sb.append(" and mark.resSubPath is null");
		} else {
			sb.append(" and mark.resSubPath=:resSubPath");
		}
		
		Query query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId());
		if(subPath != null) {
			query.setParameter("resSubPath", subPath);
		}
		query.setFlushMode(FlushModeType.AUTO).executeUpdate();
	}
	
	@Override
	public List<MarkResourceStat> getStats(OLATResourceable ores, List<String> subPaths, Identity identity) {
		if(subPaths == null || subPaths.isEmpty()) {
			//these stats are optimized
			if(identity == null) {
				return getStats(ores);
			} else {
				return getStats(ores, identity);
			}
		} else {
			StringBuilder sb = new StringBuilder();
			sb.append("select count(mrk.resSubPath), mrk.resSubPath from mark mrk where ")
				.append("mrk.resId=:resId and mrk.resName=:resName and mrk.resSubPath in (:resSubPath)");
			if(identity != null) {
				sb.append(" and mrk.creator=:creator");
			}
			sb.append(" group by mrk.resSubPath");
			
			TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
					.createQuery(sb.toString(), Object[].class)
					.setParameter("resName", ores.getResourceableTypeName())
					.setParameter("resId", ores.getResourceableId())
					.setParameter("resSubPath", subPaths);
			if(identity != null) {
				query.setParameter("creator", identity);
			}
			
			List<Object[]> rawStats = query.getResultList();
			List<MarkResourceStat> stats = new ArrayList<>(rawStats.size());
			for(Object[] rawStat:rawStats) {
				stats.add(new MarkResourceStat(ores,(String)rawStat[1],((Number)rawStat[0]).intValue()));
			}
			return stats;
		}
	}
	
	private List<MarkResourceStat> getStats(OLATResourceable ores) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(mark.resSubPath), mark.resSubPath from ")
			.append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resId=:resId and mark.resName=:resName")
			.append(" group by mark.resSubPath");
		
		List<Object[]> rawStats = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.getResultList();
		
		List<MarkResourceStat> stats = new ArrayList<>(rawStats.size());
		for(Object[] rawStat:rawStats) {
			stats.add(new MarkResourceStat(ores,(String)rawStat[1],((Number)rawStat[0]).intValue()));
		}
		return stats;
	}
	
	private List<MarkResourceStat> getStats(OLATResourceable ores, Identity identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mark.resSubPath from ")
			.append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resId=:resId and mark.resName=:resName")
			.append(" and mark.creator=:creator");
		
		List<String> markedSubPaths = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), String.class)
				.setParameter("resName", ores.getResourceableTypeName())
				.setParameter("resId", ores.getResourceableId())
				.setParameter("creator", identity)
				.getResultList();
		
		List<MarkResourceStat> stats = new ArrayList<>(markedSubPaths.size());
		for(String markedSubPath:markedSubPaths) {
			stats.add(new MarkResourceStat(ores,markedSubPath,1));
		}
		return stats;
	}
}