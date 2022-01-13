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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.IPUtils;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.model.AssessmentModeImpl;
import org.olat.course.assessment.model.AssessmentModeToAreaImpl;
import org.olat.course.assessment.model.AssessmentModeToCurriculumElementImpl;
import org.olat.course.assessment.model.AssessmentModeToGroupImpl;
import org.olat.course.assessment.model.SearchAssessmentModeParams;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.manager.CurriculumElementDAO;
import org.olat.modules.dcompensation.manager.DisadvantageCompensationDAO;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.manager.LectureBlockToGroupDAO;
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
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentModeManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BGAreaManager areaMgr;
	@Autowired
	private AssessmentModeDAO assessmentModeDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private BusinessGroupDAO businessGroupDAO;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private LectureBlockToGroupDAO lectureBlockToGroupDao;
	@Autowired
	private DisadvantageCompensationDAO disadvantageCompensationDao;
	@Autowired
	private AssessmentModeCoordinationServiceImpl assessmentModeCoordinationService;

	@Override
	public AssessmentMode createAssessmentMode(RepositoryEntry entry) {
		AssessmentModeImpl mode = new AssessmentModeImpl();
		mode.setCreationDate(new Date());
		mode.setLastModified(mode.getCreationDate());
		mode.setRepositoryEntry(entry);
		mode.setStatus(Status.none);
		mode.setManualBeginEnd(true);
		return mode;
	}
	
	@Override
	public AssessmentMode createAssessmentMode(LectureBlock lectureBlock,
			int leadTime, int followupTime, String ips, String sebKeys) {
		AssessmentModeImpl mode = new AssessmentModeImpl();
		mode.setCreationDate(new Date());
		mode.setLastModified(mode.getCreationDate());
		mode.setName(lectureBlock.getTitle());
		mode.setDescription(lectureBlock.getDescription());
		mode.setBegin(lectureBlock.getStartDate());
		mode.setEnd(lectureBlock.getEndDate());
		mode.setLeadTime(leadTime);
		mode.setFollowupTime(followupTime);
		boolean restricIps = StringHelper.containsNonWhitespace(ips);
		mode.setRestrictAccessIps(restricIps);
		if(restricIps) {
			mode.setIpList(ips);
		}
		boolean seb = StringHelper.containsNonWhitespace(sebKeys);
		mode.setSafeExamBrowser(seb);
		if(seb) {
			mode.setSafeExamBrowserKey(sebKeys);
		}
		mode.setRepositoryEntry(lectureBlock.getEntry());
		mode.setLectureBlock(lectureBlock);
		mode.setStatus(Status.none);
		mode.setManualBeginEnd(true);
		return mode;
	}

	@Override
	public AssessmentMode createAssessmentMode(AssessmentMode assessmentMode) {
		AssessmentModeImpl mode = new AssessmentModeImpl();
		mode.setCreationDate(new Date());
		mode.setLastModified(mode.getCreationDate());
		mode.setName(assessmentMode.getName());
		mode.setDescription(assessmentMode.getDescription());
		mode.setBegin(null);
		mode.setEnd(null);
		mode.setLeadTime(assessmentMode.getLeadTime());
		mode.setFollowupTime(assessmentMode.getFollowupTime());
		
		mode.setElementList(assessmentMode.getElementList());
		mode.setRestrictAccessElements(assessmentMode.isRestrictAccessElements());
		mode.setStartElement(assessmentMode.getStartElement());
		
		mode.setRestrictAccessIps(assessmentMode.isRestrictAccessIps());
		if(assessmentMode.isRestrictAccessIps()) {
			mode.setIpList(assessmentMode.getIpList());
		}
		
		mode.setSafeExamBrowser(assessmentMode.isSafeExamBrowser());
		if(assessmentMode.isSafeExamBrowser()) {
			mode.setSafeExamBrowserKey(assessmentMode.getSafeExamBrowserKey());
			mode.setSafeExamBrowserHint(assessmentMode.getSafeExamBrowserHint());
		}
		
		mode.setStatus(Status.none);
		mode.setManualBeginEnd(assessmentMode.isManualBeginEnd());
		mode.setTargetAudience(assessmentMode.getTargetAudience());
		mode.setRepositoryEntry(assessmentMode.getRepositoryEntry());
		mode.setApplySettingsForCoach(assessmentMode.isApplySettingsForCoach());
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
		Date endWithFollowupTime = evaluateFollowupTime(end, assessmentMode.getFollowupTime());
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
		log.info(Tracing.M_AUDIT, "Update assessment mode: {} ({}) from {} to {} manual: {}",
				reloadedMode.getName(), reloadedMode.getKey(), begin, end, reloadedMode.isManualBeginEnd());
		if(reloadedMode.isManualBeginEnd()) {
			reloadedMode = assessmentModeCoordinationService.syncManuallySetStatus(reloadedMode, forceStatus);
		} else {
			reloadedMode = assessmentModeCoordinationService.syncAutomicallySetStatus(reloadedMode);
		}
		return reloadedMode;
	}

	@Override
	public void syncAssessmentModeToLectureBlock(AssessmentMode assessmentMode) {
		LectureBlock lectureBlock = assessmentMode.getLectureBlock();
		RepositoryEntry entry = assessmentMode.getRepositoryEntry();
		assessmentMode.setName(lectureBlock.getTitle());
		assessmentMode.setBegin(lectureBlock.getStartDate());
		assessmentMode.setEnd(lectureBlock.getEndDate());
		
		List<Group> groups = lectureBlockToGroupDao.getGroups(lectureBlock);
		
		Group defGroup = repositoryEntryRelationDao.getDefaultGroup(entry);
		boolean hasCourse = groups.contains(defGroup);

		List<AssessmentModeToCurriculumElement> currentCurriculumElements = new ArrayList<>(assessmentMode.getCurriculumElements());
		for(Iterator<AssessmentModeToCurriculumElement> it=currentCurriculumElements.iterator(); it.hasNext(); ) {
			AssessmentModeToCurriculumElement rel = it.next();
			if(groups.contains(rel.getCurriculumElement().getGroup())) {
				groups.remove(rel.getCurriculumElement().getGroup());
			} else {
				it.remove();
			}
		}

		List<AssessmentModeToGroup> currentGroups = new ArrayList<>(assessmentMode.getGroups());
		for(Iterator<AssessmentModeToGroup> it=currentGroups.iterator(); it.hasNext(); ) {
			AssessmentModeToGroup rel = it.next();
			if(groups.contains(rel.getBusinessGroup().getBaseGroup())) {
				groups.remove(rel.getBusinessGroup().getBaseGroup());
			} else {
				it.remove();
			}
		}
		
		if(!groups.isEmpty()) {
			List<CurriculumElement> curriculumElements = curriculumElementDao.loadElements(entry);
			for(CurriculumElement curriculumElement:curriculumElements) {
				if(groups.contains(curriculumElement.getGroup())) {
					AssessmentModeToCurriculumElement rel = createAssessmentModeToCurriculumElement(assessmentMode, curriculumElement);
					currentCurriculumElements.add(rel);
				}
			}

			SearchBusinessGroupParams params = new SearchBusinessGroupParams();
			List<BusinessGroup> businessGroups = businessGroupDAO.findBusinessGroups(params, entry, 0, -1, BusinessGroupOrder.nameAsc);
			for(BusinessGroup businessGroup:businessGroups) {
				if(groups.contains(businessGroup.getBaseGroup())) {
					AssessmentModeToGroup rel = createAssessmentModeToGroup(assessmentMode, businessGroup);
					currentGroups.add(rel);
				}
			}
		}
		
		assessmentMode.getCurriculumElements().clear();
		if(!currentCurriculumElements.isEmpty()) {
			assessmentMode.getCurriculumElements().addAll(currentCurriculumElements);
		}
		assessmentMode.getGroups().clear();
		if(!currentGroups.isEmpty()) {
			assessmentMode.getGroups().addAll(currentGroups);
		}
		
		Target target;
		if(hasCourse && currentGroups.isEmpty() && currentCurriculumElements.isEmpty()) {
			target = Target.course;
		} else if(!hasCourse && !currentGroups.isEmpty() && currentCurriculumElements.isEmpty()) {
			target = Target.groups;
		} else if(!hasCourse && currentGroups.isEmpty() && !currentCurriculumElements.isEmpty()) {
			target = Target.curriculumEls;
		} else {
			target = Target.courseAndGroups;
		}
		assessmentMode.setTargetAudience(target);
	}

	@Override
	public void delete(AssessmentMode assessmentMode) {
		if(assessmentMode == null) return;// nothing to do
		log.info(Tracing.M_AUDIT, "Delete assessment mode: {} ({})", assessmentMode.getName(), assessmentMode.getKey());
		assessmentModeDao.delete(assessmentMode);
	}
	
	@Override
	public void delete(LectureBlock lectureBlock) {
		AssessmentMode mode = assessmentModeDao.getAssessmentModeByLecture(lectureBlock);
		if(mode != null) {
			delete(mode);
		}
	}
	
	@Override
	public AssessmentMode getAssessmentMode(LectureBlock lectureBlock) {
		return assessmentModeDao.getAssessmentModeByLecture(lectureBlock);
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
	public List<AssessmentMode> getPlannedAssessmentMode(RepositoryEntryRef entry, Date from, Date to) {
		return assessmentModeDao.getPlannedAssessmentMode(entry, from, to);
	}

	@Override
	public List<AssessmentMode> getAssessmentModeFor(IdentityRef identity) {
		List<AssessmentMode> currentModes = getAssessmentModes(new Date());
		List<AssessmentMode> myModes = null;
		if(!currentModes.isEmpty()) {
			//check permissions, groups, areas, course
			List<AssessmentMode> allMyModes = assessmentModeDao.loadAssessmentModeFor(identity, currentModes);

			myModes = new ArrayList<>(allMyModes.size());
			for(AssessmentMode mode:allMyModes) {
				if(assessmentModeCoordinationService.isDisadvantageCompensationExtensionTime(mode)) {
					if(isDisadvantagedUser(mode, identity)) {
						myModes.add(mode);
					}
				} else if(mode.getStatus() != Status.end) {
					myModes.add(mode);
				}
			}
		}
		return myModes == null ? Collections.<AssessmentMode>emptyList() : myModes;
	}
	
	@Override
	public boolean isDisadvantagedUser(AssessmentMode mode, IdentityRef identity) {
		return disadvantageCompensationDao
				.isActiveDisadvantagedUser(identity, mode.getRepositoryEntry(), mode.getElementAsList());
	}

	@Override
	public Set<Long> getAssessedIdentityKeys(AssessmentMode assessmentMode) {
		Target targetAudience = assessmentMode.getTargetAudience();
		RepositoryEntry re = assessmentMode.getRepositoryEntry();
		
		Set<Long> assessedKeys = new HashSet<>();
		if(targetAudience == Target.course || targetAudience == Target.courseAndGroups) {
			List<Long> courseMemberKeys = assessmentMode.isApplySettingsForCoach()
					? repositoryEntryRelationDao.getMemberKeys(re, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.coach.name(), GroupRoles.participant.name())
					: repositoryEntryRelationDao.getMemberKeys(re, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.participant.name());
			assessedKeys.addAll(courseMemberKeys);
		}
		
		// For courseAndGroups, the curriculums are retrieved by the relation type.
		if(targetAudience == Target.curriculumEls) {
			List<CurriculumElementRef> curriculumElements = new ArrayList<>();
			Set<AssessmentModeToCurriculumElement> modeTocurriculumElements  = assessmentMode.getCurriculumElements();
			for(AssessmentModeToCurriculumElement modeTocurriculumElement:modeTocurriculumElements) {
				curriculumElements.add(modeTocurriculumElement.getCurriculumElement());
			}
			
			List<Long> curriculumMemberKeys = assessmentMode.isApplySettingsForCoach()
					? curriculumElementDao.getMemberKeys(curriculumElements, GroupRoles.coach.name(), GroupRoles.participant.name())
					: curriculumElementDao.getMemberKeys(curriculumElements, GroupRoles.participant.name());
			assessedKeys.addAll(curriculumMemberKeys);
		}
		
		if(targetAudience == Target.groups || targetAudience == Target.courseAndGroups) {
			List<BusinessGroup> groups = new ArrayList<>();
			
			Set<AssessmentModeToArea> modeToAreas = assessmentMode.getAreas();
			if(!modeToAreas.isEmpty()) {
				List<BGArea> areas = new ArrayList<>(modeToAreas.size());
				for(AssessmentModeToArea modeToArea: modeToAreas) {
					areas.add(modeToArea.getArea());
				}
				
				List<BusinessGroup> groupsInAreas = areaMgr.findBusinessGroupsOfAreas(areas);	
				groups.addAll(groupsInAreas);
			}

			Set<AssessmentModeToGroup> modeToGroups = assessmentMode.getGroups();
			if(!modeToGroups.isEmpty()) {
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
	public boolean isInAssessmentMode(RepositoryEntryRef entry, String subIdent, IdentityRef identity) {
		return assessmentModeDao.isInAssessmentMode(entry, subIdent, identity);
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
	public void deleteAssessmentModeToGroup(AssessmentModeToGroup modeToGroup) {
		dbInstance.getCurrentEntityManager().remove(modeToGroup);
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
	public void deleteAssessmentModeToArea(AssessmentModeToArea modeToArea) {
		dbInstance.getCurrentEntityManager().remove(modeToArea);
	}

	@Override
	public AssessmentModeToCurriculumElement createAssessmentModeToCurriculumElement(AssessmentMode mode, CurriculumElement curriculumElement) {
		AssessmentModeToCurriculumElementImpl modeToElement = new AssessmentModeToCurriculumElementImpl();
		modeToElement.setAssessmentMode(mode);
		modeToElement.setCurriculumElement(curriculumElement);
		dbInstance.getCurrentEntityManager().persist(modeToElement);
		return modeToElement;
	}
	
	@Override
	public void deleteAssessmentModeToCurriculumElement(AssessmentModeToCurriculumElement modeToCurriculumElement) {
		dbInstance.getCurrentEntityManager().remove(modeToCurriculumElement);
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
					int indexMask = ipRange.indexOf('/');
					int indexPseudoRange = ipRange.indexOf('-');
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
				
				if(safeExamHash == null) {
					log.warn("Failed safeexambrowser request hash is null for URL: {} and key: {}", url, safeExamBrowserKey);
				} else {
					if(!safe) {
						log.warn("Failed safeexambrowser check: {} (Header) {} (Calculated) for URL: {}", safeExamHash, hash, url);
					}
					log.debug("safeexambrowser {} : {} (Header) {} (Calculated) for URL: {} and key: {}", (safeExamHash.equals(hash) ? "Success" : "Failed") , safeExamHash, hash, url, safeExamBrowserKey);
				}
			}
		} else {
			safe = true;
		}
		return safe;
	}
}