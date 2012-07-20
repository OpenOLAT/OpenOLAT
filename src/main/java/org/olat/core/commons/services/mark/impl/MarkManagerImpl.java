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
import java.util.List;

import javax.persistence.TypedQuery;

import org.hibernate.FlushMode;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.commons.services.mark.MarkResourceStat;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * Implementation of the MarkManager
 * 
 * <P>
 * Initial Date:  9 mar. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
@Service
public class MarkManagerImpl extends MarkManager {
	
	@Autowired
	private DB dbInstance;
	
	/**
	 * [spring]
	 */
	private MarkManagerImpl() {
		INSTANCE = this;
	}
	
	

	@Override
	public List<Mark> getMarks(OLATResourceable ores, Identity identity, Collection<String> subPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mark from ").append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resId=:resId and mark.resName=:resName and mark.creator=:creator");
		if(!subPath.isEmpty()) {
			sb.append(" and mark.resSubPath in (:resSubPaths)");
		}
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resName", ores.getResourceableTypeName());
		query.setLong("resId", ores.getResourceableId());
		query.setEntity("creator", identity);
		if(!subPath.isEmpty()) {
			query.setParameterList("resSubPaths", subPath);
		}
		
		List<Mark> results = query.list();
		return results;
	}

	@Override
	public List<Mark> getMarks(Identity identity, String resourceTypeName, Collection<String> subPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mark from ").append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resName=:resName and mark.creator=:creator");
		if(!subPath.isEmpty()) {
			sb.append(" and mark.resSubPath in (:resSubPaths)");
		}
		
		TypedQuery<Mark> query = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Mark.class)
				.setParameter("resName", resourceTypeName)
				.setParameter("creator", identity);
		if(!subPath.isEmpty()) {
			query.setParameter("resSubPaths", subPath);
		}
		
		List<Mark> results = query.getResultList();
		return results;
	}

	@Override
	public void filterMarks(Identity identity, String resourceTypeName, Collection<Long> resIds) {
		if(resIds == null || resIds.isEmpty()) return;
		
		StringBuilder sb = new StringBuilder();
		sb.append("select mark.resId from ").append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resId in(:resIds) and mark.resName=:resName and mark.creator=:creator");

		List<Long> markedResIds = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Long.class)
				.setParameter("resName", resourceTypeName)
				.setParameter("resIds", resIds)
				.setParameter("creator", identity)
				.getResultList();

		resIds.retainAll(markedResIds);
	}

	@Override
	public boolean isMarked(OLATResourceable ores, Identity identity, String subPath) {
		return loadMark(ores, identity, subPath) != null;
	}
	
	@Override
	public Mark setMark(OLATResourceable ores, Identity identity, String subPath, String businessPath) {
		MarkImpl mark = loadMark(ores, identity, subPath);
		if(mark == null) {
			mark = new MarkImpl();
			mark.setResName(ores.getResourceableTypeName());
			mark.setResId(ores.getResourceableId());
			mark.setResSubPath(subPath);
			mark.setBusinessPath(businessPath);
			mark.setCreator(identity);
			DBFactory.getInstance().saveObject(mark);
		}
		return mark;
	}
	

	@Override
	public void moveMarks(OLATResourceable ores, String oldSubPath, String newSubPath) {
		//can be a lot of marks to move around
		StringBuilder sb = new StringBuilder();
		sb.append("update ").append(MarkImpl.class.getName()).append(" mark set mark.resSubPath=:newSubPath ")
		  .append("where mark.resId=:resId and mark.resName=:resName and mark.resSubPath=:oldSubPath");
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resName", ores.getResourceableTypeName());
		query.setLong("resId", ores.getResourceableId());
		query.setString("oldSubPath", oldSubPath);
		query.setString("newSubPath", newSubPath);
		query.executeUpdate(FlushMode.AUTO);
	}

	@Override
	public void removeMark(OLATResourceable ores, Identity identity, String subPath) {
		MarkImpl mark = loadMark(ores, identity, subPath);
		if(mark != null) {
			DBFactory.getInstance().deleteObject(mark);
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
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resName", ores.getResourceableTypeName());
		query.setLong("resId", ores.getResourceableId());
		query.setEntity("creator", identity);
		if(resSubPath != null) {
			query.setString("resSubPath", resSubPath);
		}

		List<MarkImpl> results = query.list();
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}
	
	@Override
	public void deleteMark(OLATResourceable ores) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resId=:resId and mark.resName=:resName");
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resName", ores.getResourceableTypeName());
		query.setLong("resId", ores.getResourceableId());
		query.executeUpdate(FlushMode.AUTO);
	}

	@Override
	public void deleteMark(OLATResourceable ores, String subPath) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from ").append(MarkImpl.class.getName()).append(" mark where ")
			.append("mark.resId=:resId and mark.resName=:resName and mark.resSubPath=:resSubPath");
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resName", ores.getResourceableTypeName());
		query.setLong("resId", ores.getResourceableId());
		query.setString("resSubPath", subPath);
		query.executeUpdate(FlushMode.AUTO);
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
			sb.append("select count(mark.resSubPath), mark.resSubPath from ").append(MarkImpl.class.getName()).append(" mark where ")
				.append("mark.resId=:resId and mark.resName=:resName and mark.resSubPath in (:resSubPath)");
			if(identity != null) {
				sb.append(" and mark.creator=:creator");
			}
			sb.append(" group by mark.resSubPath");
			
			DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
			query.setString("resName", ores.getResourceableTypeName());
			query.setLong("resId", ores.getResourceableId());
			query.setParameterList("resSubPath", subPaths);
			if(identity != null) {
				query.setEntity("creator", identity);
			}
			
			List<Object[]> rawStats = query.list();
			List<MarkResourceStat> stats = new ArrayList<MarkResourceStat>(rawStats.size());
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
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resName", ores.getResourceableTypeName());
		query.setLong("resId", ores.getResourceableId());

		List<Object[]> rawStats = query.list();
		List<MarkResourceStat> stats = new ArrayList<MarkResourceStat>(rawStats.size());
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
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setString("resName", ores.getResourceableTypeName());
		query.setLong("resId", ores.getResourceableId());
		query.setEntity("creator", identity);
		
		List<String> markedSubPaths = query.list();
		List<MarkResourceStat> stats = new ArrayList<MarkResourceStat>(markedSubPaths.size());
		for(String markedSubPath:markedSubPaths) {
			stats.add(new MarkResourceStat(ores,markedSubPath,1));
		}
		return stats;
	}
}