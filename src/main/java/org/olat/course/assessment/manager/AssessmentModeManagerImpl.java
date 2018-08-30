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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
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
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
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
	private AssessmentModeDAO assessmentModeDao;
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
		assessmentModeDao.delete(assessmentMode);
	}

	@Override
	public AssessmentMode getAssessmentModeById(Long key) {
		return assessmentModeDao.getAssessmentModeById(key);
	}

	@Override
	public List<AssessmentMode> findAssessmentMode(SearchAssessmentModeParams params) {
		return assessmentModeDao.findAssessmentMode(params);
	}

	@Override
	public List<AssessmentMode> getAssessmentModeFor(RepositoryEntryRef entry) {
		return assessmentModeDao.getAssessmentModeFor(entry);
	}
	
	@Override
	public List<AssessmentMode> getPlannedAssessmentMode(RepositoryEntryRef entry, Date from) {
		return assessmentModeDao.getPlannedAssessmentMode(entry, from);
	}

	@Override
	public List<AssessmentMode> getAssessmentModeFor(IdentityRef identity) {
		List<AssessmentMode> currentModes = getAssessmentModes(new Date());
		List<AssessmentMode> myModes = null;
		if(currentModes.size() > 0) {
			//check permissions, groups, areas, course
			myModes = assessmentModeDao.loadAssessmentModeFor(identity, currentModes);
		}
		return myModes == null ? Collections.<AssessmentMode>emptyList() : myModes;
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
		return assessmentModeDao.getAssessmentModes(now);
	}

	@Override
	public boolean isInAssessmentMode(RepositoryEntryRef entry, Date date) {
		return assessmentModeDao.isInAssessmentMode(entry, date);
	}
	
	@Override
	public List<AssessmentMode> getCurrentAssessmentMode(RepositoryEntryRef entry, Date now) {
		return assessmentModeDao.getCurrentAssessmentMode(entry, now);
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
	public boolean isNodeInUse(RepositoryEntryRef entry, CourseNode node) {
		return assessmentModeDao.isNodeInUse(entry, node);
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

				if(!safe && url.endsWith("/")) {
					String strippedUrl = url.substring(0, url.length() - 1);
					String strippedHash = Encoder.sha256Exam(strippedUrl + safeExamBrowserKey);
					if(safeExamHash != null && safeExamHash.equals(strippedHash)) {
						safe = true;
					}
				}
				
				if(debug) {
					if(safeExamHash == null) {
						log.debug("Failed safeexambrowser request hash is null for URL: " + url + " and key: " + safeExamBrowserKey);
					} else {
						log.debug((safeExamHash.equals(hash) ? "Success" : "Failed") + " : " + safeExamHash +" (Header) " + hash + " (Calculated) for URL: " + url + " and key: " + safeExamBrowserKey);
					}
				}
			}
		} else {
			safe = true;
		}
		return safe;
	}
}