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

import static org.olat.core.commons.persistence.PersistenceHelper.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.IPUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.model.AssessmentModeImpl;
import org.olat.course.assessment.model.AssessmentModeToAreaImpl;
import org.olat.course.assessment.model.AssessmentModeToGroupImpl;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupImpl;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.area.BGtoAreaRelationImpl;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("assessmentModeManager")
public class AssessmentModeManagerImpl implements AssessmentModeManager {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentModeManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGAreaManager areaMgr;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private AssessmentModeCoordinationServiceImpl assessmentModeCoordinationService;

	@Override
	public AssessmentMode createAssessmentMode(RepositoryEntry entry) {
		AssessmentModeImpl mode = new AssessmentModeImpl();
		mode.setCreationDate(new Date());
		mode.setLastModified(new Date());
		mode.setRepositoryEntry(entry);
		mode.setStatus(Status.none);
		mode.setManualBeginEnd(false);
		return mode;
	}
	
	protected Date evaluateLeadTime(Date begin, int leadtime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(begin);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if(leadtime > 0) {
			cal.add(Calendar.MINUTE, -leadtime);
		}
		return cal.getTime();
	}
	
	protected Date evaluateFollowupTime(Date end, int followupTime) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(end);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if(followupTime > 0) {
			cal.add(Calendar.MINUTE, followupTime);
		}
		return cal.getTime();
	}
	
	@Override
	public AssessmentMode persist(AssessmentMode assessmentMode) {
		assessmentMode.setLastModified(new Date());
		
		//update begin with lead time
		Date begin = assessmentMode.getBegin();
		Date beginWithLeadTime = evaluateLeadTime(begin, assessmentMode.getLeadTime());
		((AssessmentModeImpl)assessmentMode).setBeginWithLeadTime(beginWithLeadTime);
		Date end = assessmentMode.getEnd();
		Date endWithFollowupTime = this.evaluateFollowupTime(end, assessmentMode.getFollowupTime());
		((AssessmentModeImpl)assessmentMode).setEndWithFollowupTime(endWithFollowupTime);

		dbInstance.getCurrentEntityManager().persist(assessmentMode);
		dbInstance.commit();
		return assessmentMode;
	}

	@Override
	public AssessmentMode merge(AssessmentMode assessmentMode, boolean forceStatus) {
		assessmentMode.setLastModified(new Date());
		
		//update begin with lead time
		Date begin = assessmentMode.getBegin();
		Date beginWithLeadTime = evaluateLeadTime(begin, assessmentMode.getLeadTime());
		((AssessmentModeImpl)assessmentMode).setBeginWithLeadTime(beginWithLeadTime);
		
		Date end = assessmentMode.getEnd();
		Date endWithFollowupTime = evaluateFollowupTime(end, assessmentMode.getFollowupTime());
		((AssessmentModeImpl)assessmentMode).setEndWithFollowupTime(endWithFollowupTime);

		AssessmentMode reloadedMode;
		if(assessmentMode.getKey() == null) {
			dbInstance.getCurrentEntityManager().persist(assessmentMode);
			reloadedMode = assessmentMode;
		} else {
			reloadedMode = dbInstance.getCurrentEntityManager()
					.merge(assessmentMode);
		}
		dbInstance.commit();
		if(reloadedMode.isManualBeginEnd()) {
			reloadedMode = assessmentModeCoordinationService.syncManuallySetStatus(reloadedMode, forceStatus);
		} else {
			reloadedMode = assessmentModeCoordinationService.syncAutomicallySetStatus(reloadedMode);
		}
		return reloadedMode;
	}

	@Override
	public void delete(AssessmentMode assessmentMode) {
		AssessmentModeImpl refMode = dbInstance.getCurrentEntityManager()
				.getReference(AssessmentModeImpl.class, assessmentMode.getKey());
		dbInstance.getCurrentEntityManager().remove(refMode);
	}

	@Override
	public AssessmentMode getAssessmentModeById(Long key) {
		List<AssessmentMode> modes = dbInstance.getCurrentEntityManager()
			.createNamedQuery("assessmentModeById", AssessmentMode.class)
			.setParameter("modeKey", key)
			.getResultList();
		
		return modes == null || modes.isEmpty() ? null : modes.get(0);
	}

	@Override
	public List<AssessmentMode> findAssessmentMode(SearchAssessmentModeParams params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mode from courseassessmentmode mode")
		  .append(" inner join fetch mode.repositoryEntry v")
		  .append(" inner join fetch v.olatResource res");
		
		boolean where = false;
		
		Date date = params.getDate();
		if(date != null) {
			where = appendAnd(sb, where);
			sb.append(":date between mode.beginWithLeadTime and mode.endWithFollowupTime");
		}
		
		String name = params.getName();
		if(StringHelper.containsNonWhitespace(name)) {
			name = PersistenceHelper.makeFuzzyQueryString(name);
			where = appendAnd(sb, where);
			sb.append("(");
			appendFuzzyLike(sb, "v.displayname", "name", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "mode.name", "name", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		Long id = null;
		String refs = null;
		String fuzzyRefs = null;
		if(StringHelper.containsNonWhitespace(params.getIdAndRefs())) {
			refs = params.getIdAndRefs();
			fuzzyRefs = PersistenceHelper.makeFuzzyQueryString(refs);
			where = appendAnd(sb, where);
			sb.append(" (v.externalId=:ref or ");
			PersistenceHelper.appendFuzzyLike(sb, "v.externalRef", "fuzzyRefs", dbInstance.getDbVendor());
			sb.append(" or v.softkey=:ref");
			if(StringHelper.isLong(refs)) {
				try {
					id = Long.parseLong(refs);
					sb.append(" or v.key=:vKey or res.resId=:vKey");
				} catch (NumberFormatException e) {
					//
				}
			}
			sb.append(")");	
		}
		
		sb.append(" order by mode.beginWithLeadTime desc ");

		TypedQuery<AssessmentMode> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class);
		if(StringHelper.containsNonWhitespace(params.getName())) {
			query.setParameter("name", name);
		}
		if(id != null) {
			query.setParameter("vKey", id);
		}
		if(refs != null) {
			query.setParameter("ref", refs);
		}
		if(fuzzyRefs != null) {
			query.setParameter("fuzzyRefs", fuzzyRefs);
		}
		if(date != null) {
			query.setParameter("date", date, TemporalType.TIMESTAMP);
		}
		return query.getResultList();
	}

	@Override
	public List<AssessmentMode> getAssessmentModeFor(RepositoryEntryRef entry) {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("assessmentModeByRepoEntry", AssessmentMode.class)
				.setParameter("entryKey", entry.getKey())
				.getResultList();
	}

	@Override
	public List<AssessmentMode> getAssessmentModeFor(IdentityRef identity) {
		List<AssessmentMode> currentModes = getAssessmentModes(new Date());
		List<AssessmentMode> myModes = null;
		if(currentModes.size() > 0) {
			//check permissions, groups, areas, course
			myModes = loadAssessmentModeFor(identity, currentModes);
		}
		return myModes == null ? Collections.<AssessmentMode>emptyList() : myModes;
	}
	
	private List<AssessmentMode> loadAssessmentModeFor(IdentityRef identity, List<AssessmentMode> currentModes) {
		StringBuilder sb = new StringBuilder(1500);
		sb.append("select mode from courseassessmentmode mode ")
		  .append(" inner join fetch mode.repositoryEntry entry")
		  .append(" left join mode.groups as modeToGroup")
		  .append(" left join mode.areas as modeToArea")
		  .append(" where mode.key in (:modeKeys)")
		  .append("  and ((mode.targetAudienceString in ('").append(AssessmentMode.Target.courseAndGroups.name()).append("','").append(AssessmentMode.Target.groups.name()).append("')")
		  .append("   and (exists (select businessGroup from ").append(BusinessGroupImpl.class.getName()).append(" as businessGroup, bgroupmember as membership")
		  .append("     where modeToGroup.businessGroup=businessGroup and membership.group=businessGroup.baseGroup and membership.identity.key=:identityKey")
		  .append("     and (membership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and membership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("   ) or exists (select areaToGroup from ").append(BGtoAreaRelationImpl.class.getName()).append(" as areaToGroup,").append(BusinessGroupImpl.class.getName()).append(" as businessGroupArea, bgroupmember as membership")
		  .append("     where modeToArea.area=areaToGroup.groupArea and areaToGroup.businessGroup=businessGroupArea and membership.group=businessGroupArea.baseGroup and membership.identity.key=:identityKey")
		  .append("     and (membership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and membership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("  ))) or (mode.targetAudienceString in ('").append(AssessmentMode.Target.courseAndGroups.name()).append("','").append(AssessmentMode.Target.course.name()).append("')")
		  .append("   and exists (select rel from repoentrytogroup as rel,  bgroupmember as membership ")
		  .append("     where mode.repositoryEntry=rel.entry and membership.group=rel.group and rel.defaultGroup=true and membership.identity.key=:identityKey")
		  .append("     and (membership.role='").append(GroupRoles.participant.name()).append("' or ")
		  .append("       (mode.applySettingsForCoach=true and membership.role='").append(GroupRoles.coach.name()).append("'))")
		  .append("  ))")
		  .append(" )");

		List<Long> modeKeys = new ArrayList<>(currentModes.size());
		for(AssessmentMode mode:currentModes) {
			modeKeys.add(mode.getKey());
		}
		List<AssessmentMode> modeList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class)
				.setParameter("identityKey", identity.getKey())
				.setParameter("modeKeys", modeKeys)
				.getResultList();
		//quicker than distinct
		return new ArrayList<AssessmentMode>(new HashSet<AssessmentMode>(modeList));
	}

	@Override
	public Set<Long> getAssessedIdentityKeys(AssessmentMode assessmentMode) {
		Target targetAudience = assessmentMode.getTargetAudience();
		RepositoryEntry re = assessmentMode.getRepositoryEntry();
		
		Set<Long> assessedKeys = new HashSet<>();
		if(targetAudience == Target.course || targetAudience == Target.courseAndGroups) {
			List<Long> courseMemberKeys = assessmentMode.isApplySettingsForCoach()
					? repositoryEntryRelationDao.getMemberKeys(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name(), GroupRoles.participant.name())
					: repositoryEntryRelationDao.getMemberKeys(re, RepositoryEntryRelationType.defaultGroup, GroupRoles.participant.name());
			assessedKeys.addAll(courseMemberKeys);
		}
		if(targetAudience == Target.groups || targetAudience == Target.courseAndGroups) {
			List<BusinessGroup> groups = new ArrayList<>();
			
			Set<AssessmentModeToArea> modeToAreas = assessmentMode.getAreas();
			if(modeToAreas.size() > 0) {
				List<BGArea> areas = new ArrayList<>(modeToAreas.size());
				for(AssessmentModeToArea modeToArea: modeToAreas) {
					areas.add(modeToArea.getArea());
				}
				
				List<BusinessGroup> groupsInAreas = areaMgr.findBusinessGroupsOfAreas(areas);	
				groups.addAll(groupsInAreas);
			}

			Set<AssessmentModeToGroup> modeToGroups = assessmentMode.getGroups();
			if(modeToGroups.size() > 0) {
				for(AssessmentModeToGroup modeToGroup: modeToGroups) {
					groups.add(modeToGroup.getBusinessGroup());
				}	
			}

			List<Long> groupMemberKeys = assessmentMode.isApplySettingsForCoach()
					? businessGroupRelationDao.getMemberKeys(groups, GroupRoles.coach.name(), GroupRoles.participant.name())
					: businessGroupRelationDao.getMemberKeys(groups, GroupRoles.participant.name());
			assessedKeys.addAll(groupMemberKeys);	
		}
		
		return assessedKeys;
	}

	@Override
	public List<AssessmentMode> getAssessmentModes(Date now) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		
		StringBuilder sb = new StringBuilder();
		sb.append("select mode from courseassessmentmode mode where ")
		  .append(" (mode.beginWithLeadTime<=:now and mode.endWithFollowupTime>=:now and mode.manualBeginEnd=false	)")
		  .append(" or mode.statusString in ('").append(Status.leadtime.name()).append("','")
		  .append(Status.assessment.name()).append("','").append(Status.followup.name()).append("')");

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), AssessmentMode.class)
				.setParameter("now", now)
				.getResultList();
	}

	@Override
	public AssessmentModeToGroup createAssessmentModeToGroup(AssessmentMode mode, BusinessGroup group) {
		AssessmentModeToGroupImpl modeToGroup = new AssessmentModeToGroupImpl();
		modeToGroup.setAssessmentMode(mode);
		modeToGroup.setBusinessGroup(group);
		dbInstance.getCurrentEntityManager().persist(modeToGroup);
		return modeToGroup;
	}

	@Override
	public AssessmentModeToArea createAssessmentModeToArea(AssessmentMode mode, BGArea area) {
		AssessmentModeToAreaImpl modeToArea = new AssessmentModeToAreaImpl();
		modeToArea.setAssessmentMode(mode);
		modeToArea.setArea(area);
		dbInstance.getCurrentEntityManager().persist(modeToArea);
		return modeToArea;
	}
	
	@Override
	public boolean isIpAllowed(String ipList, String address) {
		boolean allOk = false;
		if(!StringHelper.containsNonWhitespace(ipList)) {
			allOk |= true;
		} else {
			for(StringTokenizer tokenizer = new StringTokenizer(ipList, "\n\r", false); tokenizer.hasMoreTokens(); ) {
				String ipRange = tokenizer.nextToken();
				if(StringHelper.containsNonWhitespace(ipRange)) {
					int indexMask = ipRange.indexOf("/");
					int indexPseudoRange = ipRange.indexOf("-");
					if(indexMask > 0) {
						allOk |= IPUtils.isValidRange(ipRange, address);
					} else if(indexPseudoRange > 0) {
						String begin = ipRange.substring(0, indexPseudoRange).trim();
						String end = ipRange.substring(indexPseudoRange + 1).trim();
						allOk |= IPUtils.isValidRange(begin, end, address);
					} else {
						allOk |= ipRange.equals(address);
					}
				}
			}
		}
		return allOk;
	}

	@Override
	public boolean isSafelyAllowed(HttpServletRequest request, String safeExamBrowserKeys) {
		boolean safe = false;
		boolean debug = log.isDebug();
		if(StringHelper.containsNonWhitespace(safeExamBrowserKeys)) {
			String safeExamHash = request.getHeader("x-safeexambrowser-requesthash");
			String url = request.getRequestURL().toString();
			for(StringTokenizer tokenizer = new StringTokenizer(safeExamBrowserKeys); tokenizer.hasMoreTokens() && !safe; ) {
				String safeExamBrowserKey = tokenizer.nextToken();
				String hash = Encoder.sha256Exam(url + safeExamBrowserKey);
				if(safeExamHash != null && safeExamHash.equals(hash)) {
					safe = true;
				}
				if(debug) {
					log.debug((safeExamHash.equals(hash) ? "Success" : "Failed") + " : " + safeExamHash +" (Header) " + hash + " (Calculated)");
				}
			}
		} else {
			safe = true;
		}
		return safe;
	}
}