/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.manager;

import static org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembership;
import org.olat.basesecurity.GroupMembershipInheritance;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationDataDeletable;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.basesecurity.manager.GroupMembershipHistoryDAO;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.basesecurity.model.IdentityToRoleKey;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.InfoMessageManager;
import org.olat.commons.info.InfoMessageToCurriculumElement;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.NamedContainerImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.ims.lti13.LTI13Service;
import org.olat.modules.coach.manager.CoachingDAO;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumDataDeletable;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementAuditLog.Action;
import org.olat.modules.curriculum.CurriculumElementAuditLog.ActionTarget;
import org.olat.modules.curriculum.CurriculumElementFileType;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementMembershipEvent;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumLearningProgress;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.CurriculumStatus;
import org.olat.modules.curriculum.model.CurriculumCopySettings;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyElementSetting;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyOfferSetting;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.model.CurriculumElementImpl;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementKeyToRepositoryEntryKey;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.model.CurriculumElementNode;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementWebDAVInfos;
import org.olat.modules.curriculum.model.CurriculumElementWithParents;
import org.olat.modules.curriculum.model.CurriculumImpl;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.CurriculumMemberStats;
import org.olat.modules.curriculum.model.CurriculumRefImpl;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.model.RepositoryEntryInfos;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.site.CurriculumElementTreeRowComparator;
import org.olat.modules.curriculum.ui.CurriculumMailing;
import org.olat.modules.curriculum.ui.member.ConfirmationByEnum;
import org.olat.modules.curriculum.ui.member.ResourceToRoleKey;
import org.olat.modules.invitation.manager.InvitationDAO;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.manager.LectureBlockDAO;
import org.olat.modules.lecture.model.LectureBlockImpl;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.manager.RepositoryEntryMyCourseQueries;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.manager.RepositoryEntryToTaxonomyLevelDAO;
import org.olat.repository.manager.RepositoryTemplateRelationDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.OfferAccess;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;
import org.olat.resource.accesscontrol.model.SearchReservationParameters;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CurriculumServiceImpl implements CurriculumService, OrganisationDataDeletable, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(CurriculumServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private CoachingDAO coachingDao;
	@Autowired
	private CurriculumDAO curriculumDao;
	@Autowired
	private CurriculumStorage curriculumStorage;
	@Autowired
	private CurriculumMemberQueries memberQueries;
	@Autowired
	private InfoMessageManager infoMessageManager;
	@Autowired
	private RepositoryEntryMyCourseQueries myCourseQueries;
	@Autowired
	private GroupMembershipHistoryDAO groupMembershipHistoryDao;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private OrganisationDAO organisationDao;
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private MarkManager markManager;
	@Autowired
	private LectureBlockDAO lectureBlockDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private CurriculumElementDAO curriculumElementDao;
	@Autowired
	private CurriculumElementTypeDAO curriculumElementTypeDao;
	@Autowired
	private CurriculumElementAuditLogDAO curriculumElementAuditLogDao;
	@Autowired
	private CurriculumElementTypeToTypeDAO curriculumElementTypeToTypeDao;
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private RepositoryTemplateRelationDAO repositoryTemplateRelationDao;
	@Autowired
	private RepositoryEntryToTaxonomyLevelDAO repositoryEntryToTaxonomyLevelDao;
	@Autowired
	private CurriculumElementToTaxonomyLevelDAO curriculumElementToTaxonomyLevelDao;
	@Autowired
	private CurriculumRepositoryEntryRelationDAO curriculumRepositoryEntryRelationDao;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private BusinessGroupDAO businessGroupDAO;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;

	@Override
	public void afterPropertiesSet() throws Exception {
		List<CurriculumElementType> defaultTypes = curriculumElementTypeDao.loadByExternalId(DEFAULT_CURRICULUM_ELEMENT_TYPE);
		if(defaultTypes == null || defaultTypes.isEmpty()) {
			CurriculumElementType defaultType = createCurriculumElementType(DEFAULT_CURRICULUM_ELEMENT_TYPE,
					"Default type", "", DEFAULT_CURRICULUM_ELEMENT_TYPE);
			defaultType.setAllowedAsRootElement(true);
			defaultType.setMaxRepositoryEntryRelations(-1);
			defaultType.setSingleElement(false);
			defaultType.setManagedFlags(new CurriculumElementTypeManagedFlag[] {
					CurriculumElementTypeManagedFlag.identifier, CurriculumElementTypeManagedFlag.externalId,
					CurriculumElementTypeManagedFlag.allowAsRoot, CurriculumElementTypeManagedFlag.composite,
					CurriculumElementTypeManagedFlag.maxEntryRelations });
			updateCurriculumElementType(defaultType);
			log.info("Default curriculum element type created.");
		}
		dbInstance.commitAndCloseSession();
	}

	@Override
	public Curriculum createCurriculum(String identifier, String displayName, String description, boolean lecturesEnabled, Organisation organisation) {
		return curriculumDao.createAndPersist(identifier, displayName, description, lecturesEnabled, organisation);
	}

	@Override
	public Curriculum getCurriculum(CurriculumRef ref) {
		return curriculumDao.loadByKey(ref.getKey());
	}

	@Override
	public Curriculum updateCurriculum(Curriculum curriculum) {
		return curriculumDao.update(curriculum);
	}
	
	@Override
	public void deleteSoftlyCurriculum(CurriculumRef curriculumRef, Identity doer, boolean sendNotifications) {
		CurriculumImpl curriculum = (CurriculumImpl)getCurriculum(curriculumRef);
		if(curriculum != null) {
			for(CurriculumElement rootElement:curriculum.getRootElements()) {
				deleteSoftlyCurriculumElement(rootElement, doer, sendNotifications);
			}
			dbInstance.commitAndCloseSession();
			curriculum = (CurriculumImpl)getCurriculum(curriculumRef);
			curriculumDao.flagAsDelete(curriculum);
			dbInstance.commit();
		}
	}

	@Override
	public List<Curriculum> getCurriculums(Collection<? extends CurriculumRef> refs) {
		return curriculumDao.loadByKeys(refs);
	}
	
	@Override
	public List<CurriculumMember> getCurriculumMembers(SearchMemberParameters params) {
		return memberQueries.getCurriculumMembers(params);
	}

	@Override
	public List<Identity> getMembersIdentity(CurriculumRef curriculum, CurriculumRoles role) {
		return curriculumDao.getMembersIdentity(curriculum, role.name());
	}

	@Override
	public boolean hasRoleExpanded(CurriculumRef curriculum, IdentityRef identity, String... roles) {
		return curriculumDao.hasRoleExpanded(curriculum, identity, roles);
	}

	@Override
	public boolean isCurriculumOwner(IdentityRef identity) {
		return curriculumDao.hasCurriculumRole(identity, CurriculumRoles.curriculumowner.name());
	}

	@Override
	public boolean isCurriculumOrElementOwner(IdentityRef identity) {
		return curriculumDao.hasCurriculumRole(identity, CurriculumRoles.curriculumowner.name())
				|| curriculumElementDao.hasCurriculumElementRole(identity, CurriculumRoles.curriculumelementowner.name());
	}

	@Override
	public void addMember(Curriculum curriculum, Identity identity, CurriculumRoles role) {
		if(!groupDao.hasRole(curriculum.getGroup(), identity, role.name())) {
			groupDao.addMembershipOneWay(curriculum.getGroup(), identity, role.name(), GroupMembershipInheritance.none);
		}
	}

	@Override
	public void removeMember(Curriculum curriculum, IdentityRef member, CurriculumRoles role) {
		groupDao.removeMembership(curriculum.getGroup(), member, role.name());
	}

	@Override
	public void removeMember(Curriculum curriculum, IdentityRef member) {
		groupDao.removeMembership(curriculum.getGroup(), member);
	}

	@Override
	public List<CurriculumElementType> getCurriculumElementTypes() {
		return curriculumElementTypeDao.load();
	}

	@Override
	public CurriculumElementType getDefaultCurriculumElementType() {
		List<CurriculumElementType> defaultTypes = curriculumElementTypeDao.loadByExternalId(DEFAULT_CURRICULUM_ELEMENT_TYPE);
		return defaultTypes == null || defaultTypes.isEmpty() ? null : defaultTypes.get(0);
	}

	@Override
	public CurriculumElementType getCurriculumElementType(CurriculumElementTypeRef typeRef) {
		return curriculumElementTypeDao.loadByKey(typeRef.getKey());
	}
	
	@Override
	public CurriculumElementType getCurriculumElementType(CurriculumElementRef element) {
		return curriculumElementTypeDao.loadByCurriculumElement(element.getKey());
	}

	@Override
	public List<CurriculumElementType> getAllowedCurriculumElementType(CurriculumElement parentElement, CurriculumElement element) {
		List<CurriculumElementType> allowedTypes;
		if(parentElement == null) {
			allowedTypes = curriculumElementTypeDao.load().stream()
					.filter(CurriculumElementType::isAllowedAsRootElement)
					.collect(Collectors.toList());
		} else {
			allowedTypes = new ArrayList<>();
			List<CurriculumElement> parentLine = getCurriculumElementParentLine(parentElement);
			for(int i=parentLine.size(); i-->0; ) {
				CurriculumElement parent = parentLine.get(i);
				CurriculumElementType parentType = parent.getType();
				if(parentType != null) {
					Set<CurriculumElementTypeToType> typeToTypes = parentType.getAllowedSubTypes();
					for(CurriculumElementTypeToType typeToType:typeToTypes) {
						if(typeToType != null) {
							allowedTypes.add(typeToType.getAllowedSubType());
						}
					}
					break;
				}
			}
		}
		
		if(element != null && !allowedTypes.isEmpty()) {
			long numOfSubElements = curriculumElementDao.countChildren(element);
			long numOfEntryRelations = curriculumRepositoryEntryRelationDao.countRepositoryEntries(element);
			
			for(Iterator<CurriculumElementType> typeIterator=allowedTypes.iterator(); typeIterator.hasNext(); ) {
				CurriculumElementType type = typeIterator.next();
				if((type.isSingleElement() && numOfSubElements > 0)
						|| (type.getMaxRepositoryEntryRelations() >= 0 && numOfEntryRelations > type.getMaxRepositoryEntryRelations())) {
					typeIterator.remove();
				}
			}
		}
		return allowedTypes;
	}

	@Override
	public CurriculumElementType createCurriculumElementType(String identifier, String displayName,
			String description, String externalId) {
		return curriculumElementTypeDao.createCurriculumElementType(identifier, displayName, description, externalId);
	}
	
	@Override
	public CurriculumElementType updateCurriculumElementType(CurriculumElementType elementType) {
		return curriculumElementTypeDao.update(elementType);
	}

	@Override
	public CurriculumElementType updateCurriculumElementType(CurriculumElementType elementType, List<CurriculumElementType> allowedSubTypes) {
		curriculumElementTypeToTypeDao.setAllowedSubType(elementType, allowedSubTypes);
		return curriculumElementTypeDao.update(elementType);
	}
	
	@Override
	public void allowCurriculumElementSubType(CurriculumElementType parentType, CurriculumElementType allowedSubType) {
		curriculumElementTypeToTypeDao.addAllowedSubType(parentType, allowedSubType);
	}

	@Override
	public void disallowCurriculumElementSubType(CurriculumElementType parentType, CurriculumElementType disallowedSubType) {
		curriculumElementTypeToTypeDao.disallowedSubType(parentType, disallowedSubType);
	}

	@Override
	public CurriculumElementType cloneCurriculumElementType(CurriculumElementTypeRef elementType) {
		CurriculumElementType clonedType = curriculumElementTypeDao.cloneCurriculumElementType(elementType);
		List<CurriculumElementTypeToType> allowSubTypesToTypes = curriculumElementTypeToTypeDao.getAllowedSubTypes(elementType);
		if(!allowSubTypesToTypes.isEmpty()) {
			for(CurriculumElementTypeToType allowSubTypeToType:allowSubTypesToTypes) {
				curriculumElementTypeToTypeDao.addAllowedSubType(clonedType, allowSubTypeToType.getAllowedSubType());
			}
		}
		return clonedType;
	}

	@Override
	public boolean deleteCurriculumElementType(CurriculumElementTypeRef elementType) {
		if(curriculumElementTypeDao.hasElements(elementType)) {
			return false;
		}
		curriculumElementTypeToTypeDao.deleteAllowedSubTypes(elementType);
		curriculumElementTypeDao.deleteCurriculumElementType(elementType);
		return true;
	}
	
	@Override
	public List<Curriculum> getCurriculums(CurriculumSearchParameters params) {
		return curriculumDao.search(params);
	}

	@Override
	public CurriculumInfos getCurriculumWithInfos(CurriculumRef curriculum) {
		CurriculumSearchParameters params = new CurriculumSearchParameters();
		params.setCurriculums(List.of(curriculum));
		List<CurriculumInfos> infos = curriculumDao.searchWithInfos(params);
		return infos.size() == 1 ? infos.get(0) : null;
	}

	@Override
	public List<CurriculumInfos> getCurriculumsWithInfos(CurriculumSearchParameters params) {
		return curriculumDao.searchWithInfos(params);
	}
	
	@Override
	public List<Curriculum> getMyCurriculums(Identity identity) {
		return curriculumDao.getMyCurriculums(identity);
	}

	@Override
	public boolean hasCurriculums(IdentityRef identity) {
		return curriculumDao.hasMyCurriculums(identity);
	}

	@Override
	public List<CurriculumRef> getMyActiveCurriculumRefs(Identity identity) {
		List<Long> curriculumKeys = curriculumDao.getMyActiveCurriculumKeys(identity);
		return curriculumKeys.stream().distinct().map(CurriculumRefImpl::new).collect(Collectors.toList());
	}

	@Override
	public CurriculumElement createCurriculumElement(String identifier, String displayName,
			CurriculumElementStatus status, Date beginDate, Date endDate, CurriculumElementRef parentRef,
			CurriculumElementType elementType, CurriculumCalendars calendars, CurriculumLectures lectures,
			CurriculumLearningProgress learningProgress, Curriculum curriculum) {
		if (parentRef == null) {
			curriculum = getCurriculum(curriculum);
		}
		CurriculumElement element = curriculumElementDao.createCurriculumElement(identifier, displayName, status,
				beginDate, endDate, parentRef, elementType, calendars, lectures, learningProgress, curriculum);
		if(element.getParent() != null) {
			Group organisationGroup = element.getGroup();
			List<GroupMembership> memberships = groupDao.getMemberships(element.getParent().getGroup(),
					GroupMembershipInheritance.inherited, GroupMembershipInheritance.root);
			for(GroupMembership membership:memberships) {
				if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited
						|| membership.getInheritanceMode() == GroupMembershipInheritance.root) {
					groupDao.addMembershipOneWay(organisationGroup, membership.getIdentity(), membership.getRole(), GroupMembershipInheritance.inherited);
				}
			}
		}
		return element;
	}

	@Override
	public CurriculumElement copyCurriculumElement(Curriculum curriculum, CurriculumElement parentElement,
			CurriculumElement elementToClone, CurriculumCopySettings settings, Identity doer) {
		CurriculumElement copy = copyCurriculumElementRec(curriculum, parentElement, elementToClone, settings, doer, 0);
		dbInstance.commit();
		
		// Recalculate the numbering under this implementation / root element
		CurriculumElement rootElement = getImplementationOf(copy);
		numberRootCurriculumElement(rootElement);
		dbInstance.commitAndCloseSession();
		
		// Reload the numbered copy
		copy = curriculumElementDao.loadByKey(copy.getKey());
		return copy;
	}
	
	private CurriculumElement copyCurriculumElementRec(Curriculum curriculum, CurriculumElement parentElement,
			CurriculumElement elementToClone, CurriculumCopySettings settings, Identity doer, int depth) {
		
		CopyElementSetting elementSetting = settings.getCopyElementSetting(elementToClone);
		
		Date beginDate = elementSetting != null && elementSetting.hasDates() ? elementSetting.begin() : elementToClone.getBeginDate();
		Date endDate = elementSetting != null && elementSetting.hasDates() ? elementSetting.end() : elementToClone.getEndDate();
		
		String identifier = elementToClone.getIdentifier();
		if(elementSetting != null && StringHelper.containsNonWhitespace(elementSetting.identifier())) {
			identifier = elementSetting.identifier();
		}
		String displayName = elementToClone.getDisplayName();
		if(elementSetting != null && StringHelper.containsNonWhitespace(elementSetting.displayName())) {
			displayName = elementSetting.displayName();
		} else if(depth == 0) {
			displayName += " (Copy)";
		}
		
		CurriculumElement clone = curriculumElementDao.copyCurriculumElement(elementToClone,
				identifier, displayName, beginDate, endDate, parentElement, curriculum);
		copyCurriculumElemenFiles(elementToClone, clone, doer);
		if(settings.isCopyOwnersMemberships()) {
			copyCurriculumElementOwners(elementToClone, clone, CurriculumRoles.owner, doer, depth == 0);
			copyCurriculumElementOwners(elementToClone, clone, CurriculumRoles.curriculumelementowner, doer, depth == 0);
		}
		if(settings.isCopyMasterCoachesMemberships()) {
			copyCurriculumElementOwners(elementToClone, clone, CurriculumRoles.mastercoach, doer, depth == 0);
		}
		if(settings.isCopyCoachesMemberships()) {
			copyCurriculumElementOwners(elementToClone, clone, CurriculumRoles.coach, doer, depth == 0);
		}
		
		boolean hasTemplates = false;
		if(settings.getCopyResources() == CopyResources.relation
				|| settings.getCopyResources() == CopyResources.resource) {
			List<RepositoryEntry> templates = getRepositoryTemplates(elementToClone);
			for(RepositoryEntry template:templates) {
				addRepositoryTemplate(clone, template);
				hasTemplates |= true;
			}
		}
		
		// Tracks lecture blocks already copied
		Set<Long> lectureBlocksCloned = new HashSet<>();
		LectureService lectureService = CoreSpringFactory.getImpl(LectureService.class);
		List<LectureBlock> allLectureBlocks = lectureBlockDao.getLectureBlocks(elementToClone);
		
		if(settings.getCopyResources() == CopyResources.relation) {
			List<RepositoryEntry> entries = getRepositoryEntries(elementToClone);
			for(RepositoryEntry entry:entries) {
				boolean hasDefaultElement = repositoryEntryRelationDao.hasDefaultElement(entry);
				repositoryEntryRelationDao.createRelation(clone.getGroup(), entry, !hasDefaultElement);
				fireRepositoryEntryAddedEvent(clone, entry);
			}
		} else if(settings.getCopyResources() == CopyResources.resource) {
			if(hasTemplates) {
				for(LectureBlock blockToCopy:allLectureBlocks) {
					if(!lectureBlocksCloned.contains(blockToCopy.getKey())
							&& elementToClone.equals(blockToCopy.getCurriculumElement())
							&& blockToCopy.getEntry() != null) {
						
						Date start = settings.shiftDate(blockToCopy.getStartDate());
						Date end = settings.shiftDate(blockToCopy.getEndDate());
						String externalRef = settings.evaluateIdentifier(blockToCopy.getExternalRef());
						LectureBlock copiedBlock = lectureService.copyLectureBlock(blockToCopy, blockToCopy.getTitle(), externalRef, start, end, null, clone, true);
						lectureBlocksCloned.add(blockToCopy.getKey());
						lectureBlockDao.addGroupToLectureBlock(copiedBlock, clone.getGroup());
						if(settings.isCopyCoachesMemberships() && settings.isAddCoachesAsTeacher()) {
							copyLectureBlockTeachers(blockToCopy, copiedBlock);
						}
					}
				}
			} else {
				List<RepositoryEntry> entries = getRepositoryEntries(elementToClone);
				RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);// prevent service to service link at startup
				for(RepositoryEntry entry:entries) {
					if(repositoryService.canCopy(entry, doer)) {
						String externalRef = settings.evaluateIdentifier(entry.getExternalRef());
						RepositoryEntry entryCopy = repositoryService.copy(entry, doer, entry.getDisplayname(), externalRef);
						repositoryEntryRelationDao.createRelation(clone.getGroup(), entryCopy, true);// First relation of new course
						fireRepositoryEntryAddedEvent(clone, entryCopy);
						
						for(LectureBlock blockToCopy:allLectureBlocks) {
							if(!lectureBlocksCloned.contains(blockToCopy.getKey())
									&& elementToClone.equals(blockToCopy.getCurriculumElement())
									&& entry.equals(blockToCopy.getEntry())) {
								Date start = settings.shiftDate(blockToCopy.getStartDate());
								Date end = settings.shiftDate(blockToCopy.getEndDate());
								String blockExternalRef = settings.evaluateIdentifier(blockToCopy.getExternalRef());
								LectureBlock copiedBlock = lectureService.copyLectureBlock(blockToCopy, blockToCopy.getTitle(), blockExternalRef, start, end, entryCopy, clone, true);
								lectureBlocksCloned.add(blockToCopy.getKey());
								if(settings.isCopyCoachesMemberships() && settings.isAddCoachesAsTeacher()) {
									copyLectureBlockTeachers(blockToCopy, copiedBlock);
								}
							}
						}
						
						if(settings.isCopyTaxonomy()) {
							List<TaxonomyLevel> entryLevels = repositoryEntryToTaxonomyLevelDao.getTaxonomyLevels(entry);
							for(TaxonomyLevel entryLevel:entryLevels) {
								repositoryEntryToTaxonomyLevelDao.createRelation(entryCopy, entryLevel);
							}
						}
					}
				}
			}
		}
		
		if(settings.isCopyOffers()) {
			ACService acService = CoreSpringFactory.getImpl(ACService.class);
			List<OfferAndAccessInfos> offerAndAccessList = acService.findOfferAndAccessByResource(elementToClone.getResource(), true);
			for(OfferAndAccessInfos offerAndAccess:offerAndAccessList) {
				OfferAccess linkToCopy = offerAndAccess.offerAccess();
				CopyOfferSetting offerSetting = settings.getCopyOfferSetting(offerAndAccess.offer());
				if(offerSetting != null) {
					acService.copyOfferAccess(linkToCopy, offerSetting.validFrom(), offerSetting.validTo(),
							clone.getResource(), clone.getDisplayName());
				}
			}
		}

		if(settings.isCopyStandaloneEvents()) {
			for(LectureBlock blockToCopy:allLectureBlocks) {
				// Copy only standalone and don't clone twice the same lecture block
				if(!lectureBlocksCloned.contains(blockToCopy.getKey()) && blockToCopy.getEntry() == null) { 
					Date start = settings.shiftDate(blockToCopy.getStartDate());
					Date end = settings.shiftDate(blockToCopy.getEndDate());
					String externalRef = settings.evaluateIdentifier(blockToCopy.getExternalRef());
					LectureBlock copiedBlock = lectureService.copyLectureBlock(blockToCopy, blockToCopy.getTitle(), externalRef, start, end, null, clone, true);
					lectureBlocksCloned.add(blockToCopy.getKey());
					lectureBlockDao.addGroupToLectureBlock(copiedBlock, clone.getGroup());
					if(settings.isCopyCoachesMemberships() && settings.isAddCoachesAsTeacher()) {
						copyLectureBlockTeachers(blockToCopy, copiedBlock);
					}
				}
			}
		}
		
		if(settings.isCopyTaxonomy()) {
			List<TaxonomyLevel> taxonomyLevels = curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(elementToClone);
			for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
				curriculumElementToTaxonomyLevelDao.createRelation(clone, taxonomyLevel);	
			}
		}
		
		List<CurriculumElement> childrenToClone = getCurriculumElementsChildren(elementToClone);
		for(CurriculumElement childToClone:childrenToClone) {
			copyCurriculumElementRec(curriculum, clone, childToClone, settings, doer, depth);
		}
		return clone;
	}
	
	private void copyLectureBlockTeachers(LectureBlock blockToCopy, LectureBlock copiedBlock) {
		if(copiedBlock instanceof LectureBlockImpl blockImpl) {
			List<Identity> teachers = lectureBlockDao.getTeachers(List.of(blockToCopy));
			Group teacherGroup = blockImpl.getTeacherGroup();
			
			for(Identity teacher:teachers) {
				if(!groupDao.hasRole(teacherGroup, teacher, "teacher")) {
					groupDao.addMembershipOneWay(teacherGroup, teacher, "teacher");
				}
			}
		}
	}
	
	private void copyCurriculumElementOwners(CurriculumElement elementToClone, CurriculumElement clone, CurriculumRoles role, Identity actor, boolean root) {
		List<GroupMembership> memberships = groupDao.getMemberships(elementToClone.getGroup(), role.name(), true);
		GroupMembershipInheritance inheritanceMode = root ? GroupMembershipInheritance.root : GroupMembershipInheritance.inherited;
		for(GroupMembership membership:memberships) {
			addMember(clone, membership.getIdentity(), role, inheritanceMode, actor, null);
		}
	}
	
	private void copyCurriculumElemenFiles(CurriculumElement elementToClone, CurriculumElement clone, Identity doer) {
		for(CurriculumElementFileType type:CurriculumElementFileType.values()) {
			VFSLeaf file = getCurriculumElemenFile(elementToClone, type);
			if(file != null && file.exists() && file instanceof LocalFileImpl localFile) {
				storeCurriculumElemenFile(clone, type, localFile.getBasefile(), localFile.getBasefile().getName(), doer);
			}
		}
	}

	@Override
	public RepositoryEntry instantiateTemplate(RepositoryEntry template, CurriculumElement curriculumElement,
			String displayName, String externalRef, Date beginDate, Date endDate, Identity doer) {
		// prevent service to service link at startup
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		
		RepositoryEntry entry = repositoryService.copy(template, doer, displayName, externalRef);
		
		// Lifecycle
		RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
		if(beginDate != null || endDate != null) {
			if(lifecycle == null || !lifecycle.isPrivateCycle()) {
				String softKey = "lf_" + entry.getSoftkey();
				lifecycle = lifecycleDao.create(entry.getDisplayname(), softKey, true, beginDate, endDate);
			} else {
				lifecycle.setValidFrom(beginDate);
				lifecycle.setValidTo(endDate);
				lifecycle = lifecycleDao.updateLifecycle(lifecycle);
			}
		}
		
		RepositoryEntry instantiatedEntry = repositoryManager.setDescriptionAndName(entry, displayName, externalRef,
				entry.getAuthors(), entry.getDescription(), entry.getTeaser(), entry.getObjectives(), entry.getRequirements(),
				entry.getCredits(), entry.getMainLanguage(), entry.getLocation(), entry.getExpenditureOfWork(),
				lifecycle, null, null, entry.getEducationalType());
		instantiatedEntry = repositoryManager.setRuntimeType(instantiatedEntry, RepositoryEntryRuntimeType.curricular);

		boolean hasRepositoryEntries = hasRepositoryEntries(curriculumElement);
		boolean moveLectureBlocks = !hasRepositoryEntries;
		addRepositoryEntry(curriculumElement, instantiatedEntry, moveLectureBlocks);
		return instantiatedEntry;
	}

	@Override
	public CurriculumElement getCurriculumElement(CurriculumElementRef element) {
		return curriculumElementDao.loadByKey(element.getKey());
	}
	
	@Override
	public CurriculumElement getCurriculumElement(OLATResource resource) {
		return curriculumElementDao.loadElementByResource(resource);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(Collection<? extends CurriculumElementRef> elementRefs) {
		return curriculumElementDao.loadByKeys(elementRefs);
	}

	@Override
	public boolean deleteSoftlyCurriculumElement(CurriculumElementRef element, Identity doer, boolean sendNotifications) {
		if(element == null || element.getKey() == null) return true; // nothing to do

		boolean delete = true;
		List<CurriculumElement> children = curriculumElementDao.getChildren(element);
		for(CurriculumElement child:children) {
			delete &= deleteSoftlyCurriculumElement(child, doer, false);
		}
		
		// remove relations to taxonomy before reloading to clear the set
		curriculumElementToTaxonomyLevelDao.deleteRelation(element);
		dbInstance.commit();

		CurriculumElementImpl reloadedElement = (CurriculumElementImpl)curriculumElementDao.loadByKey(element.getKey());
		if(reloadedElement == null || reloadedElement.getElementStatus() == CurriculumElementStatus.deleted) {
			return false;
		}

		// remove relations to repository entries
		List<RepositoryEntry> entriesToClose = new ArrayList<>();
		List<RepositoryEntryToGroupRelation> relationsToRepo = repositoryEntryRelationDao.getCurriculumRelations(reloadedElement);
		for(RepositoryEntryToGroupRelation relationToRepo:relationsToRepo) {
			if(!relationToRepo.isDefaultGroup()) {// only paranoia
				RepositoryEntry entryToClose = removeRepositoryEntryRelation(reloadedElement, relationToRepo);
				if(entryToClose != null) {
					entriesToClose.add(entryToClose);
				}
			}
		}
		
		Map<String,CurriculumDataDeletable> deleteDelegates = CoreSpringFactory.getBeansOfType(CurriculumDataDeletable.class);
		for(CurriculumDataDeletable deleteDelegate:deleteDelegates.values()) {
			delete &= deleteDelegate.deleteCurriculumElementData(reloadedElement);
		}
		
		List<Identity> membersToNotify = sendNotifications
				? groupDao.getMembers(reloadedElement.getGroup(), CurriculumRoles.participant.name(), CurriculumRoles.coach.name(),
						CurriculumRoles.owner.name(), CurriculumRoles.mastercoach.name(), CurriculumRoles.curriculumelementowner.name())
				: List.of();
		
		groupDao.removeMemberships(reloadedElement.getGroup());
		
		//only flag as deleted
		reloadedElement.setParent(null);
		reloadedElement.setExternalId(null);
		reloadedElement.setMaterializedPathKeys(null);
		reloadedElement.setElementStatus(CurriculumElementStatus.deleted);
		curriculumElementDao.update(reloadedElement);
		
		dbInstance.commit();
		
		if(!entriesToClose.isEmpty()) {
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			for(RepositoryEntry entryToClose:entriesToClose) {
				repositoryService.closeRepositoryEntry(entryToClose, doer, sendNotifications);
			}
		}
		
		if(!membersToNotify.isEmpty()) {
			MailTemplate template = CurriculumMailing.getMembershipRemovedByAdminTemplate(reloadedElement.getCurriculum(), reloadedElement, doer);
			MailPackage mailing = new MailPackage(template, null, "[CurriculumElement:" + reloadedElement.getKey(), true);
			for(Identity member:membersToNotify) {
				CurriculumMailing.sendEmail(doer, member, reloadedElement.getCurriculum(), reloadedElement, mailing);
			}
		}

		return delete;
	}
	
	private RepositoryEntry removeRepositoryEntryRelation(CurriculumElement element, RepositoryEntryToGroupRelation relationToRepo) {
		boolean close = true;
		RepositoryEntry entry = relationToRepo.getEntry();
		if(entry.getRuntimeType() == RepositoryEntryRuntimeType.curricular) {
			List<RepositoryEntryToGroupRelation> entryRelations = repositoryEntryRelationDao.getCurriculumRelations(entry);
			for(RepositoryEntryToGroupRelation entryRelation:entryRelations) {
				if(!entryRelation.getGroup().equals(element.getGroup())) {
					close &= false;
				}
			}
		} else {
			close &= false;
		}
		repositoryEntryRelationDao.removeRelation(relationToRepo);
		return close ? entry : null;
	}

	@Override
	public CurriculumElement updateCurriculumElement(CurriculumElement element) {
		return curriculumElementDao.update(element);
	}
	
	@Override
	public CurriculumElement updateCurriculumElementStatus(Identity doer, CurriculumElementRef elementRef,
			CurriculumElementStatus newStatus, boolean updateChildren, MailPackage mailing) {
		
		CurriculumElement element = getCurriculumElement(elementRef);
		if (element.getElementStatus() == newStatus) {
			return element;
		}
		
		CurriculumElementStatus currentStatus = element.getElementStatus();
		((CurriculumElementImpl)element).setElementStatus(newStatus);
		element = updateCurriculumElement(element);
		auditLogChangeStatus(currentStatus, newStatus, element, doer);
		
		if (updateChildren) {
			getCurriculumElementsDescendants(element).stream()
				.filter(childElement -> !childElement.getElementStatus().isCancelledOrClosed())
				.filter(childElement -> childElement.getElementStatus() != newStatus)
				.forEach(childElement -> {
					CurriculumElementStatus currentChildStatus = childElement.getElementStatus();
					((CurriculumElementImpl)childElement).setElementStatus(newStatus);
					childElement = updateCurriculumElement(childElement);
					auditLogChangeStatus(currentChildStatus, newStatus, childElement, doer);
				});
		}
		
		dbInstance.commitAndCloseSession();
		
		if(mailing != null && mailing.isSendEmail()) {
			List<Identity> identities = getMembersIdentity(element, CurriculumRoles.participant);
			element = getCurriculumElement(element);
			sendStatusChangeNotificationsEmail(doer, identities, mailing, element);
		}
		
		return element;
	}
	
	private void auditLogChangeStatus(CurriculumElementStatus currentStatus, CurriculumElementStatus newStatus, CurriculumElement element, Identity doer) {
		String newStatusStr = newStatus == null ? null : newStatus.name();
		String currentStatusStr = currentStatus == null ? null : currentStatus.name();
		curriculumElementAuditLogDao.createAuditLog(Action.CHANGE_STATUS, ActionTarget.CURRICULUM_ELEMENT,
				currentStatusStr, newStatusStr, element.getCurriculum(), element, doer);
	}
	
	private void sendStatusChangeNotificationsEmail(Identity doer, List<Identity> identities, MailPackage mailing,
			CurriculumElement curriculumElement) {
		for (Identity identity : identities) {
			Curriculum curriculum = curriculumElement.getCurriculum();
			CurriculumMailing.sendEmail(doer, identity, curriculum, curriculumElement, mailing);
		}
	}
	
	@Override
	public CurriculumElement moveCurriculumElement(CurriculumElement elementToMove, CurriculumElement newParent,
			CurriculumElement siblingBefore, Curriculum targetCurriculum) {
		Curriculum reloadedTargetCurriculum = curriculumDao.loadByKey(targetCurriculum.getKey());
		CurriculumElement element = curriculumElementDao
				.move(elementToMove, newParent, siblingBefore, reloadedTargetCurriculum);
		
		// propagate inheritance of the new parent
		List<GroupMembership> membershipsToPropagate = new ArrayList<>();
		Map<IdentityToRoleKey,GroupMembership> identityRoleToNewParentMembership = new HashMap<>();
		if(newParent != null) {
			List<GroupMembership> memberships = groupDao.getMemberships(newParent.getGroup(),
					GroupMembershipInheritance.inherited, GroupMembershipInheritance.root);
			for(GroupMembership membership:memberships) {
				if(membership.getInheritanceMode() == GroupMembershipInheritance.inherited || membership.getInheritanceMode() == GroupMembershipInheritance.root) {
					membershipsToPropagate.add(membership);
					identityRoleToNewParentMembership.put(new IdentityToRoleKey(membership), membership);
				}
			}
		}
		
		// inherited of the moved element eventuel -> root
		List<GroupMembership> movedElementMemberships = groupDao.getMemberships(element.getGroup());
		for(GroupMembership movedElementMembership:movedElementMemberships) {
			if(movedElementMembership.getInheritanceMode() == GroupMembershipInheritance.inherited) {
				GroupMembership newParentMembership = identityRoleToNewParentMembership.get(new IdentityToRoleKey(movedElementMembership));
				if(newParentMembership == null || newParentMembership.getInheritanceMode() == GroupMembershipInheritance.none) {
					groupDao.updateInheritanceMode(movedElementMembership, GroupMembershipInheritance.root);
				}
			}
		}
		
		CurriculumElementNode treeToMove = curriculumElementDao.getDescendantTree(element);
		if(!membershipsToPropagate.isEmpty()) {
			propagateMembership(treeToMove, membershipsToPropagate);
		}
		
		// Flush all changes on the database
		dbInstance.commit();
		
		// Recalculate the numbering under this implementation / root element
		CurriculumElement rootElement = getImplementationOf(element);
		numberRootCurriculumElement(rootElement);
		dbInstance.commit();
		
		return element;
	}
	
	private void propagateMembership(CurriculumElementNode node, List<GroupMembership> membershipsToPropagate) {
		Group group = node.getElement().getGroup();
		List<GroupMembership> nodeMemberships = groupDao.getMemberships(group);
		Map<IdentityToRoleKey,GroupMembership> identityRoleToMembership = new HashMap<>();
		for(GroupMembership nodeMembership:nodeMemberships) {
			identityRoleToMembership.put(new IdentityToRoleKey(nodeMembership), nodeMembership);
		}

		for(GroupMembership membershipToPropagate:membershipsToPropagate) {
			GroupMembership nodeMembership = identityRoleToMembership.get(new IdentityToRoleKey(membershipToPropagate));
			if(nodeMembership == null) {
				groupDao.addMembershipOneWay(group, membershipToPropagate.getIdentity(), membershipToPropagate.getRole(), GroupMembershipInheritance.inherited);
			} else if(nodeMembership.getInheritanceMode() != GroupMembershipInheritance.inherited)  {
				groupDao.updateInheritanceMode(nodeMembership, GroupMembershipInheritance.inherited);
			}
		}

		List<CurriculumElementNode> children = node.getChildrenNode();
		if(children != null && !children.isEmpty()) {
			for(CurriculumElementNode child:children) {
				propagateMembership(child, membershipsToPropagate);
			}
		}
	}

	@Override
	public CurriculumElement moveCurriculumElement(CurriculumElement rootElement, Curriculum curriculum) {
		// root element move they entire memberships with, don't need to change them
		CurriculumElement element = curriculumElementDao.move(rootElement, curriculum);
		dbInstance.commit();
		return element;
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(CurriculumRef curriculum, CurriculumElementStatus[] status) {
		return curriculumElementDao.loadElements(curriculum, status);
	}

	@Override
	public List<CurriculumElementInfos> getCurriculumElementsWithInfos(CurriculumElementInfosSearchParams searchParams) {
		return curriculumElementDao.loadElementsWithInfos(searchParams);
	}

	@Override
	public Long getCuriculumElementCount(RepositoryEntryRef entry) {
		return curriculumElementDao.countElements(entry);
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry) {
		return curriculumElementDao.loadElements(entry);
	}

	@Override
	public boolean hasCurriculumElements(RepositoryEntryRef entry) {
		return curriculumElementDao.hasElements(entry);
	}

	@Override
	public List<CurriculumElementWithParents> getOrderedCurriculumElementsTree(RepositoryEntryRef entry) {
		List<CurriculumElement> elementsList = curriculumElementDao.loadElements(entry);
		Set<CurriculumElement> elements = new HashSet<>(elementsList);
		
		List<CurriculumElementWithParents> withParents = new ArrayList<>(elements.size() + 2);
		for(CurriculumElement element:elements) {
			int numOfSlashes = StringHelper.count(element.getMaterializedPathKeys(), '/');
			
			List<CurriculumElement> parentLine;
			if(numOfSlashes <= 2) {
				parentLine = List.of();
			} else if(numOfSlashes <= 3 && element.getParent() != null) {
				parentLine = List.of(element.getParent());
			} else {
				parentLine = curriculumElementDao.getParentLine(element);
			}
			withParents.add(new CurriculumElementWithParents(element, parentLine, 0));
		}
		
		try {
			Collections.sort(withParents, new CurriculumElementWithParentsComparator(Locale.GERMAN));
		} catch (Exception e) {
			log.error("", e);
		}

		return withParents;
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry, Identity identity,
			Collection<CurriculumRoles> roles) {
		return curriculumRepositoryEntryRelationDao.getCurriculumElements(entry, identity, roles);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementsChildren(CurriculumElementRef parentElement) {
		return curriculumElementDao.getChildren(parentElement);
	}
	
	@Override
	public boolean hasCurriculumElementChildren(CurriculumElementRef parentElement) {
		return curriculumElementDao.hasChildren(parentElement);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementsDescendants(CurriculumElement parentElement) {
		return curriculumElementDao.getDescendants(parentElement);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementsByCurriculums(Collection<? extends CurriculumRef> curriculumRefs) {
		return curriculumElementDao.loadElementsByCurriculums(curriculumRefs);
	}

	@Override
	public List<CurriculumElement> searchCurriculumElements(String externalId, String identifier, Long key) {
		return curriculumElementDao.searchElements(externalId, identifier, key);
	}

	@Override
	public List<CurriculumElementSearchInfos> searchCurriculumElements(CurriculumElementSearchParams params) {
		return curriculumElementDao.searchElements(params);
	}

	@Override
	public List<CurriculumElement> getCurriculumElementParentLine(CurriculumElement element) {
		return curriculumElementDao.getParentLine(element);
	}

	@Override
	public List<CurriculumMember> getCurriculumElementsMembers(SearchMemberParameters params) {
		return memberQueries.getCurriculumElementsMembers(params);
	}
	
	@Override
	public List<CurriculumMemberStats> getMembersWithStats(SearchMemberParameters params) {
		List<CurriculumMember> members = memberQueries.getCurriculumElementsMembers(params);
		
		Map<Identity, CurriculumMemberStats> rowMap = new HashMap<>();
		Map<Long, CurriculumMemberStats> rowLongMap = new HashMap<>();
		for(CurriculumMember member:members) {
			CurriculumMemberStats row = rowMap.computeIfAbsent(member.getIdentity(), CurriculumMemberStats::new);
			row.getMembership().setCurriculumElementRole(member.getRole());
			row.addFirstTime(member.getCreationDate());
			if(row.getMembership().isParticipant()) {
				rowLongMap.put(member.getIdentity().getKey(), row);
			}
		}
		
		if(!rowLongMap.isEmpty()) {
			List<CurriculumElement> elements = params.getCurriculumElements();
			coachingDao.getStudentsCompletionStatement(elements, rowLongMap);
		}
		
		return new ArrayList<>(rowMap.values());
	}

	@Override
	public List<Identity> getMembersIdentity(CurriculumElementRef element, CurriculumRoles role) {
		return curriculumElementDao.getMembersIdentity(element, role.name());
	}
	
	@Override
	public List<Long> getMemberKeys(List<CurriculumElementRef> elements, String... roles) {
		return curriculumElementDao.getMemberKeys(elements, roles);
	}
	
	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Curriculum curriculum, Identity identity) {
		if(identity == null) return new ArrayList<>();
		return curriculumElementDao.getMembershipInfos(Collections.singletonList(curriculum), null, identity);
	}
	
	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(List<CurriculumElement> elements, List<Identity> identities) {
		Identity[] idArr = identities.toArray(new Identity[identities.size()]);
		return curriculumElementDao.getMembershipInfos(null, elements, idArr);
	}

	@Override
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Collection<? extends CurriculumElementRef> elements, Identity... identities) {
		return curriculumElementDao.getMembershipInfos(null, elements, identities);
	}
	
	@Override
	public List<CurriculumElementMembershipHistory> getCurriculumElementMembershipsHistory(
			CurriculumElementMembershipHistorySearchParameters params) {
		return curriculumElementDao.getMembershipInfosAndHistory(params);
	}
	
	@Override
	public List<Identity> getMembersIdentity(List<Long> curriculumElementKeys, CurriculumRoles role) {
		if(role == null || curriculumElementKeys == null || curriculumElementKeys.isEmpty()) {
			return new ArrayList<>();
		}
		return curriculumElementDao.getMembersIdentity(curriculumElementKeys, role.name());
	}

	@Override
	public void acceptPendingParticipation(ResourceReservation reservation, Identity identity, Identity actor) {
		CurriculumRoles roles = ResourceToRoleKey.reservationToRole(reservation.getType());
		CurriculumElement curriculumElement = curriculumElementDao.loadElementByResource(reservation.getResource());
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(curriculumElement);
		
		List<CurriculumElementMembershipChange> changes = new ArrayList<>();
		CurriculumElementMembershipChange change = CurriculumElementMembershipChange.addMembership(actor, curriculumElement, !descendants.isEmpty(), roles);
		changes.add(change);
		
		for(CurriculumElement descendant:descendants) {
			CurriculumElementMembershipChange descendantChange = CurriculumElementMembershipChange.addMembership(actor, descendant, false, roles);
			changes.add(descendantChange);
		}
		
		updateCurriculumElementMemberships(actor, null, List.of(change), null);
	}

	@Override
	public void cancelPendingParticipation(ResourceReservation reservation, Identity identity, Identity actor, String adminNote) {
		CurriculumRoles roles = ResourceToRoleKey.reservationToRole(reservation.getType());
		CurriculumElement curriculumElement = curriculumElementDao.loadElementByResource(reservation.getResource());
		CurriculumElementMembershipChange change = CurriculumElementMembershipChange.valueOf(identity, curriculumElement);
		change.setNextStatus(roles, GroupMembershipStatus.cancel);
		if(StringHelper.containsNonWhitespace(adminNote)) {
			change.setAdminNote(roles, adminNote);
		}
		updateCurriculumElementMemberships(actor, null, List.of(change), null);
	}

	@Override
	public void updateCurriculumElementMemberships(Identity doer, Roles roles,
			List<CurriculumElementMembershipChange> changes, MailPackage mailing) {
		
		int count = 0;
		for(CurriculumElementMembershipChange change:changes) {
			updateCurriculumElementMembership(change, doer);
			if(++count % 100 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		dbInstance.commitAndCloseSession();
		
		if(mailing != null && mailing.isSendEmail()) {
			sendMembershipNotificationsEmail(doer, changes, mailing);
		}
	}
	
	private void sendMembershipNotificationsEmail(Identity doer, List<CurriculumElementMembershipChange> changes, MailPackage mailing) {
		Map<Identity,CurriculumElementMembershipChange> changesToNotifiy = new HashMap<>();
		for(CurriculumElementMembershipChange change:changes) {
			if(change.hasStatus(GroupMembershipStatus.reservation)
					|| change.hasStatus(GroupMembershipStatus.active)
					|| change.hasStatus(GroupMembershipStatus.removed)
					|| change.hasStatus(GroupMembershipStatus.declined)) {
				if(!changesToNotifiy.containsKey(change.getMember())) {
					changesToNotifiy.put(change.getMember(), change);
				} else {
					CurriculumElementMembershipChange currentChange = changesToNotifiy.get(change.getMember());
					if(change.numOfSegments() < currentChange.numOfSegments()) {
						changesToNotifiy.put(change.getMember(), change);
					}
				}
			}
		}
		
		for(CurriculumElementMembershipChange additionToNotifiy:changesToNotifiy.values()) {
			CurriculumElement curriculumElement = additionToNotifiy.getCurriculumElement();
			Curriculum curriculum = curriculumElement.getCurriculum();
			
			MailPackage mailingWithTemplate = mailing;
			if(mailingWithTemplate.getTemplate() == null) {
				MailTemplate template = CurriculumMailing.findBestMailTemplate(additionToNotifiy, doer);
				mailingWithTemplate = mailingWithTemplate.copyWithTemplate(template);
			}
			CurriculumMailing.sendEmail(doer, additionToNotifiy.getMember(), curriculum, curriculumElement, mailingWithTemplate);
		}
	}
	
	private void updateCurriculumElementMembership(CurriculumElementMembershipChange changes, Identity actor) {
		final CurriculumElement element = changes.getCurriculumElement();
		final List<CurriculumRoles> rolesToChange = changes.getRoles();
		final GroupMembershipInheritance inheritanceMode = changes.isApplyDescendants()
				? GroupMembershipInheritance.root
				: GroupMembershipInheritance.none;
		for(CurriculumRoles role:rolesToChange) {
			GroupMembershipStatus nextStatus = changes.getNextStatus(role);
			String adminNote = changes.getAdminNoteBy(role);
			updateCurriculumElementMembership(element, changes.getMember(), role, inheritanceMode, nextStatus,
					changes.getConfirmationBy(), changes.getConfirmUntil(),
					actor, adminNote);
		}
	}
	
	private void updateCurriculumElementMembership(CurriculumElement element,
			Identity member, CurriculumRoles role, GroupMembershipInheritance inheritanceMode,
			GroupMembershipStatus nextStatus, ConfirmationByEnum confirmationBy, Date confirmUntil,
			Identity actor, String adminNote) {
		
		if(nextStatus == GroupMembershipStatus.active) {
			removeMemberReservation(element, member, role, null, null, null);
			addMember(element, member, role, inheritanceMode, actor, adminNote);
		} else if(nextStatus == GroupMembershipStatus.reservation) {
			Boolean confirmBy = confirmationBy == ConfirmationByEnum.PARTICIPANT ? Boolean.TRUE : Boolean.FALSE;
			addMemberReservation(element, member, role, confirmUntil, confirmBy, actor, adminNote);
		} else if(nextStatus == GroupMembershipStatus.removed) {
			removeMember(element, member, role, nextStatus, actor, adminNote);
		} else if(nextStatus == GroupMembershipStatus.declined) {
			boolean removed = removeMemberReservation(element, member, role, nextStatus, actor, adminNote);
			removed |= removeMember(element, member, role, nextStatus, actor, adminNote);
			if(!removed) {
				groupMembershipHistoryDao.createMembershipHistory(element.getGroup(), member, role.name(),
						nextStatus, inheritanceMode == GroupMembershipInheritance.inherited,
						null, null, actor, adminNote);
			}
		} else if(nextStatus == GroupMembershipStatus.cancel
				|| nextStatus == GroupMembershipStatus.cancelWithFee) {
			removeMemberReservation(element, member, role, nextStatus, actor, adminNote);
			removeMember(element, member, role, nextStatus, actor, adminNote);
			groupMembershipHistoryDao.createMembershipHistory(element.getGroup(), member, role.name(),
					nextStatus, inheritanceMode == GroupMembershipInheritance.inherited,
					null, null, actor, adminNote);
		}
	}

	@Override
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role, Identity actor) {
		addMember(element, member, role, actor, null);
	}
	
	@Override
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role, Identity actor,
			String adminNote) {
		GroupMembershipInheritance inheritanceMode;
		if(CurriculumRoles.isInheritedByDefault(role)) {
			inheritanceMode = GroupMembershipInheritance.root;
		} else {
			inheritanceMode = GroupMembershipInheritance.none;
		}
		addMember(element, member, role, inheritanceMode, actor, adminNote);
		dbInstance.commit();
	}
	
	private void addMember(CurriculumElement element, Identity member, CurriculumRoles role, GroupMembershipInheritance inheritanceMode,
			Identity actor, String adminNote) {
		if(inheritanceMode == GroupMembershipInheritance.inherited) {
			throw new AssertException("Inherited are automatic");
		}
		
		List<CurriculumElementMembershipEvent> events = new ArrayList<>();
		Group elementGroup = element.getGroup();
		GroupMembership membership = groupDao.getMembership(elementGroup, member, role.name());
		if(membership == null) {
			groupDao.addMembershipOneWay(elementGroup, member, role.name(), inheritanceMode);
			groupMembershipHistoryDao.createMembershipHistory(elementGroup, member,
					role.name(), GroupMembershipStatus.active,
					inheritanceMode == GroupMembershipInheritance.inherited,
					null, null, actor, adminNote);
			if(element.getParent() == null) {
				OLATResourceable item = OresHelper.createOLATResourceableInstance(CurriculumElement.class, element.getKey());
				if(!markManager.isMarked(item, member, null)) {
					markManager.setMark(item, member, null, "[MyCoursesSite:0][CurriculumElement:" + element.getKey() + "]");
				}
			}
			events.add(CurriculumElementMembershipEvent.identityAdded(element, member, role));
		} else if(membership.getInheritanceMode() != inheritanceMode) {
			groupDao.updateInheritanceMode(membership, inheritanceMode);
		}
		
		if(inheritanceMode == GroupMembershipInheritance.root) {
			List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
			for(CurriculumElement descendant:descendants) {
				Group descendantGroup = descendant.getGroup();
				GroupMembership inheritedMembership = groupDao.getMembership(descendantGroup, member, role.name());
				if(inheritedMembership == null) {
					groupDao.addMembershipOneWay(descendantGroup, member, role.name(), GroupMembershipInheritance.inherited);
					groupMembershipHistoryDao.createMembershipHistory(descendantGroup, member,
							role.name(), GroupMembershipStatus.active, true, null, null, actor, adminNote);
					events.add(CurriculumElementMembershipEvent.identityAdded(element, member, role));
				} else if(inheritedMembership.getInheritanceMode() == GroupMembershipInheritance.none) {
					groupDao.updateInheritanceMode(inheritedMembership, GroupMembershipInheritance.inherited);
				}
			}
		}
		
		dbInstance.commitAndCloseSession();
		sendDeferredEvents(events);
	}

	@Override
	public void removeMember(CurriculumElement element, Identity member, Identity actor) {
		List<CurriculumElementMembershipEvent> events = new ArrayList<>();

		Group elementGroup = element.getGroup();
		List<GroupMembership> memberships = groupDao.getMemberships(elementGroup, member);
		groupDao.removeMembership(elementGroup, member);
		groupMembershipHistoryDao.createMembershipHistory(elementGroup, member,
				GroupRoles.owner.name(), GroupMembershipStatus.removed, false, null, null,
				actor, null);
		
		CurriculumElementNode elementNode = null;
		for(GroupMembership membership:memberships) {
			if(membership.getInheritanceMode() == GroupMembershipInheritance.none) {
				groupDao.removeMembership(membership);
				events.add(CurriculumElementMembershipEvent.identityRemoved(element, member, membership.getRole()));
			} else if(membership.getInheritanceMode() == GroupMembershipInheritance.root) {
				String role = membership.getRole();
				groupDao.removeMembership(membership);
				events.add(CurriculumElementMembershipEvent.identityRemoved(element, member, membership.getRole()));
				
				if(elementNode == null) {
					elementNode = curriculumElementDao.getDescendantTree(element);
				}
				for(CurriculumElementNode child:elementNode.getChildrenNode()) {
					removeInheritedMembership(child, member, role, GroupMembershipStatus.removed, actor, null, false, events);
				}
			}
		}
		dbInstance.commitAndCloseSession();
		sendDeferredEvents(events);
	}

	@Override
	public boolean removeMember(CurriculumElement element, Identity member, CurriculumRoles role,
			GroupMembershipStatus reason, Identity actor, String adminNote) {
		Group elementGroup = element.getGroup();
		List<CurriculumElementMembershipEvent> events = new ArrayList<>();
		GroupMembership membership = groupDao.getMembership(elementGroup, member, role.name());
		int removed = groupDao.removeMembership(elementGroup, member, role.name());
		if(removed > 0) {
			groupMembershipHistoryDao.createMembershipHistory(elementGroup, member,
					role.name(), reason, false, null, null,
					actor, adminNote);
			events.add(CurriculumElementMembershipEvent.identityRemoved(element, member, role));
		}
		
		if(membership != null && (membership.getInheritanceMode() == GroupMembershipInheritance.root
				|| membership.getInheritanceMode() == GroupMembershipInheritance.none)) {
			CurriculumElementNode elementNode = curriculumElementDao.getDescendantTree(element);
			for(CurriculumElementNode child:elementNode.getChildrenNode()) {
				removeInheritedMembership(child, member, role.name(), reason, actor, adminNote, true, events);
			}
		}
		dbInstance.commitAndCloseSession();
		sendDeferredEvents(events);
		return removed > 0;
	}
	
	/**
	 * The method will recursively delete the inherithed membership. If it
	 * found a mebership marked as "root" or "none". It will stop.
	 * 
	 * @param elementNode The organization node
	 * @param member The user to remove
	 * @param role The role
	 * @param events 
	 */
	private void removeInheritedMembership(CurriculumElementNode elementNode, Identity member,
			String role, GroupMembershipStatus reason, Identity actor, String adminNote,
			boolean force, List<CurriculumElementMembershipEvent> events) {
		Group group = elementNode.getElement().getGroup();
		GroupMembership membership = groupDao.getMembership(group, member, role);
		if(membership != null && (force || membership.getInheritanceMode() == GroupMembershipInheritance.inherited)) {
			groupDao.removeMembership(membership);
			events.add(CurriculumElementMembershipEvent.identityRemoved(elementNode.getElement(), member, membership.getRole()));
			
			if(reason == GroupMembershipStatus.cancel
					|| reason == GroupMembershipStatus.cancelWithFee
					|| reason == GroupMembershipStatus.declined
					|| reason == GroupMembershipStatus.removed) {
				groupMembershipHistoryDao.createMembershipHistory(group, member,
						role, reason, true, null, null,
						actor, adminNote);
			}
			
			for(CurriculumElementNode child:elementNode.getChildrenNode()) {
				removeInheritedMembership(child, member, role, reason,  actor, adminNote, force, events);
			}
		}
	}

	@Override
	public boolean removeMemberReservation(CurriculumElement element, Identity member, CurriculumRoles role,
			GroupMembershipStatus reason, Identity actor, String adminNote) {
		OLATResource resource = element.getResource();
		ResourceReservation reservation = reservationDao.loadReservation(member, resource);
		if(reservation != null) {
			Group group = element.getGroup();
			reservationDao.deleteReservation(reservation);
			if(reason != null) {
				groupMembershipHistoryDao.createMembershipHistory(group, member, role.name(),
					reason, false, null, null, actor, adminNote);
			}
			dbInstance.commit();
		}
		return reservation != null;
	}

	@Override
	public void removeMembers(CurriculumElement element, List<Identity> members, boolean overrideManaged, Identity actor) {
		List<CurriculumElementMembershipEvent> events = new ArrayList<>();
		if(!CurriculumElementManagedFlag.isManaged(element, CurriculumElementManagedFlag.members) || overrideManaged) {
			for(Identity member:members) {
				groupDao.removeMembership(element.getGroup(), member);
				events.add(CurriculumElementMembershipEvent.identityRemoved(element, member));
			}
		}
		dbInstance.commitAndCloseSession();
		sendDeferredEvents(events);
	}
	
	@Override
	public void addMemberReservation(CurriculumElement element, Identity member, CurriculumRoles role,
			Date expirationDate, Boolean confirmBy, Identity actor, String note) {
		OLATResource resource = element.getResource();
		ResourceReservation reservation = reservationDao.loadReservation(member, resource);
		if(reservation == null) {
			Date expiration = expirationDate == null
					? getDefaultReservationExpiration()
					: expirationDate;
			expiration = DateUtils.getEndOfDay(expiration);
			Group group = element.getGroup();
			reservationDao.createReservation(member, "curriculum_" + role, expiration, confirmBy, resource);
			groupMembershipHistoryDao.createMembershipHistory(group, member, role.name(),
					GroupMembershipStatus.reservation, false, null, null, actor, note);
			dbInstance.commit();
		}
	}

	@Override
	public Date getDefaultReservationExpiration() {
		return DateUtils.addMonth(DateUtils.getEndOfDay(new Date()), 6);
	}
	
	@Override
	public Map<Long, Long> getCurriculumElementKeyToNumParticipants(List<CurriculumElement> curriculumElements, boolean countReservations) {
		if (curriculumElements.isEmpty()) {
			return Map.of();
		}
		
		// Memberships
		Map<Long, Long> curriculumElementKeyToNumParticipants = new HashMap<>(curriculumElements.size());
		for (CurriculumElementMembership membership : getCurriculumElementMemberships(curriculumElements)) {
			if (membership.isParticipant()) {
				Long numParticipants = curriculumElementKeyToNumParticipants.getOrDefault(membership.getCurriculumElementKey(), Long.valueOf(0));
				numParticipants++;
				curriculumElementKeyToNumParticipants.put(membership.getCurriculumElementKey(), numParticipants);
			}
		}
		
		// Reservations
		if (countReservations) {
			List<OLATResource> resources = new ArrayList<>(curriculumElements.size());
			Map<Long, Long> resourceKeyToElementKey = new HashMap<>(curriculumElements.size());
			for (CurriculumElement curriculumElement : curriculumElements) {
				resources.add(curriculumElement.getResource());
				resourceKeyToElementKey.put(curriculumElement.getResource().getKey(), curriculumElement.getKey());
			}
			
			List<ResourceReservation> reservations = reservationDao.loadReservations(new SearchReservationParameters(resources));
			for (ResourceReservation reservation : reservations) {
				Long resourceKey = reservation.getResource().getKey();
				Long ceKey = resourceKeyToElementKey.get(resourceKey);
				if (ceKey != null) {
					Long numParticipants = curriculumElementKeyToNumParticipants.getOrDefault(ceKey, Long.valueOf(0));
					numParticipants++;
					curriculumElementKeyToNumParticipants.put(ceKey, numParticipants);
				}
			}
		}
		
		return curriculumElementKeyToNumParticipants;
	}
	
	@Override
	public boolean isMaxParticipantsReached(CurriculumElement element) {
		boolean maxParticipantsReached = false;
		if (element.getMaxParticipants() != null) {
			Long numParticipants = getCurriculumElementKeyToNumParticipants(List.of(element), true)
					.getOrDefault(element.getKey(), Long.valueOf(0));
			if (element.getMaxParticipants() <= numParticipants) {
				maxParticipantsReached = true;
			}
		}
		return maxParticipantsReached;
	}
	
	@Override
	public boolean hasRepositoryEntries(CurriculumElementRef element) {
		return curriculumRepositoryEntryRelationDao.hasRepositoryEntries(element);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element) {
		List<CurriculumElementRef> elements = Collections.singletonList(element);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, elements, RepositoryEntryStatusEnum.preparationToClosed(), false, null, null);
	}
	
	@Override
	public Map<Long, Set<RepositoryEntry>> getCurriculumElementKeyToRepositoryEntries(Collection<? extends CurriculumElementRef> elements) {
		return curriculumRepositoryEntryRelationDao.getCurriculumElementKeyToRepositoryEntries(elements);
	}
	
	@Override
	public List<RepositoryEntryInfos> getRepositoryEntriesWithInfos(CurriculumElementRef element) {
		return curriculumRepositoryEntryRelationDao.getRepositoryEntriesWithInfos(element);
	}

	@Override
	public List<RepositoryEntry> getRepositoryTemplates(CurriculumElementRef element) {
		return curriculumRepositoryEntryRelationDao.getRepositoryTemplates(element);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesWithDescendants(CurriculumElement element) {
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
		descendants.add(element);
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), false, null, null);
	}
	
	@Override
	public List<RepositoryEntry> getRepositoryEntriesOfParticipantWithDescendants(CurriculumElement element, Identity participant) {
		List<CurriculumElement> descendants = curriculumElementDao.getDescendants(element);
		descendants.add(element);
		List<String> roles = Arrays.asList(GroupRoles.participant.name());
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), false, participant, roles);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesWithLectures(CurriculumElement element, Identity identity, boolean withDescendants) {
		List<CurriculumElement> descendants;
		if(withDescendants) {
			descendants = curriculumElementDao.getDescendants(element);
		} else {
			descendants = new ArrayList<>();
		}	
		descendants.add(element);
		List<String> roles = Arrays.asList(OrganisationRoles.administrator.name(), OrganisationRoles.principal.name(),
				OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name(), CurriculumRoles.mastercoach.name());
		List<CurriculumElementRef> descendantRefs = new ArrayList<>(descendants);
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(null, descendantRefs, RepositoryEntryStatusEnum.preparationToClosed(), true, identity, roles);
	}

	@Override
	public List<RepositoryEntry> getRepositoryEntriesWithLectures(Curriculum curriculum, Identity identity) {
		List<String> roles = Arrays.asList(OrganisationRoles.administrator.name(), OrganisationRoles.principal.name(),
				OrganisationRoles.learnresourcemanager.name(), GroupRoles.owner.name(), CurriculumRoles.mastercoach.name());
		return curriculumRepositoryEntryRelationDao
				.getRepositoryEntries(curriculum, null, RepositoryEntryStatusEnum.preparationToClosed(), true, identity, roles);
	}
	
	@Override
	public CurriculumElement getDefaultCurriculumElement(RepositoryEntryRef entry) {
		return curriculumRepositoryEntryRelationDao.getDefaultCurriculumElement(entry);
	}

	@Override
	public AddRepositoryEntry addRepositoryEntry(CurriculumElement curriculumElement, RepositoryEntry entry, boolean moveLectureBlocks) {
		if(!hasRepositoryEntry(curriculumElement, entry)) {
			boolean moved = false;
			RepositoryEntry repoEntry = repositoryEntryDao.loadReferenceByKey(entry.getKey());
			boolean hasDefaultElement = repositoryEntryRelationDao.hasDefaultElement(repoEntry);
			repositoryEntryRelationDao.createRelation(curriculumElement.getGroup(), repoEntry, !hasDefaultElement);
			if(moveLectureBlocks) {
				moved = moveLectureBlocks(curriculumElement, entry);
			}
			fireRepositoryEntryAddedEvent(curriculumElement, entry);
			return new AddRepositoryEntry(true, moved);
		}
		return new AddRepositoryEntry(false, false);
	}
	
	@Override
	public boolean addRepositoryTemplate(CurriculumElement curriculumElement, RepositoryEntry template) {
		if(!hasRepositoryTemplate(curriculumElement, template)) {
			RepositoryEntry repoTemplate = repositoryEntryDao.loadReferenceByKey(template.getKey());
			repositoryTemplateRelationDao.createRelation(curriculumElement.getGroup(), repoTemplate);
			return true;
		}
		return false;
	}
	
	private boolean moveLectureBlocks(CurriculumElement curriculumElement, RepositoryEntry entry) {
		if(curriculumElement == null || entry == null) return false;
		
		boolean moved = false;
		List<LectureBlock> lectureBlocks = lectureBlockDao.getLectureBlocks(curriculumElement);
		for(LectureBlock lectureBlock:lectureBlocks) {
			((LectureBlockImpl)lectureBlock).setEntry(entry);
			lectureBlock = lectureBlockDao.update(lectureBlock);
			lectureBlockDao.addGroupToLectureBlock(lectureBlock, curriculumElement.getGroup());
			moved |= true;
		}
		return moved;
	}

	@Override
	public boolean hasRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry) {
		return repositoryEntryRelationDao.hasRelation(element.getGroup(), entry);
	}
	
	@Override
	public boolean hasRepositoryTemplate(CurriculumElement element, RepositoryEntryRef entry) {
		return repositoryTemplateRelationDao.hasRelation(element.getGroup(), entry);
	}

	@Override
	public void removeRepositoryEntry(RepositoryEntry entry) {
		List<CurriculumElement> elements = curriculumRepositoryEntryRelationDao.getCurriculumElements(entry);
		for(CurriculumElement element:elements) {
			internalRemoveRepositoryEntry(element, entry);
		}
	}

	@Override
	public RemovedRepositoryEntry removeRepositoryEntry(CurriculumElement element, RepositoryEntry entry) {
		// remove relation linked between infoMessages and curriculumElement
		List<InfoMessage> infoMessages = infoMessageManager.loadInfoMessageByResource(entry.getOlatResource(),
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, 0);
		for (InfoMessage infoMessage : infoMessages) {
			Set<InfoMessageToCurriculumElement> infoMessageToCurriculumElements = infoMessage.getCurriculumElements();
			if (infoMessageToCurriculumElements != null) {
				for (InfoMessageToCurriculumElement infoMessageToCurriculumElement : infoMessageToCurriculumElements) {
					if (infoMessageToCurriculumElement.getCurriculumElement().equals(element)) {
						infoMessageManager.deleteInfoMessageToCurriculumElement(infoMessageToCurriculumElement);
					}
				}
			}
		}
		int lectureBlocksMoved = internalRemoveRepositoryEntry(element, entry);
		return new RemovedRepositoryEntry(true, lectureBlocksMoved);
	}
	
	private int internalRemoveRepositoryEntry(CurriculumElement element, RepositoryEntry entry) {
		int lectureBlocksMoved = 0;
		List<LectureBlock> lectureBlocks = lectureBlockDao.getLectureBlocks(entry);
		for(LectureBlock lectureBlock:lectureBlocks) {
			if(lectureBlock.getCurriculumElement() == null) {
				// Do nothing
			} else if(element.equals(lectureBlock.getCurriculumElement())) {
				((LectureBlockImpl)lectureBlock).setEntry(null);
				lectureBlockDao.update(lectureBlock);
				lectureBlocksMoved++;
			}
		}
		repositoryEntryRelationDao.removeRelation(element.getGroup(), entry);
		return lectureBlocksMoved;
	}
	
	@Override
	public void removeRepositoryTemplate(CurriculumElement element, RepositoryEntry entry) {
		repositoryTemplateRelationDao.removeRelation(element.getGroup(), entry);
	}

	@Override
	public List<TaxonomyLevel> getTaxonomy(CurriculumElement element) {
		if(element == null || element.getKey() == null) return Collections.emptyList();
		return curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(element);
	}
	
	@Override
	public Map<Long, List<TaxonomyLevel>> getCurriculumElementKeyToTaxonomyLevels(List<? extends CurriculumElementRef> curriculumElements) {
		if (curriculumElements == null || curriculumElements.isEmpty()) return Map.of();
		return curriculumElementToTaxonomyLevelDao.getCurriculumElementKeyToTaxonomyLevels(curriculumElements);
	}
	
	@Override
	public void updateTaxonomyLevels(CurriculumElement element, Collection<TaxonomyLevel> addedLevels, Collection<TaxonomyLevel> removedLevels) {
		if (element == null || element.getKey() == null) return;
		
		List<TaxonomyLevel> taxonomyLevels = curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(element);
		
		if (addedLevels != null) {
			for (TaxonomyLevel level : addedLevels) {
				if (taxonomyLevels.contains(level)) {
					continue;
				}
				
				curriculumElementToTaxonomyLevelDao.createRelation(element, level);
			}
		}
		
		if (removedLevels != null) {
			for (TaxonomyLevel level : removedLevels) {
				if (!taxonomyLevels.contains(level)) {
					continue;
				}
				
				curriculumElementToTaxonomyLevelDao.deleteRelation(element, level);
			}
		}
	}

	@Override
	public List<CurriculumElement> getCurriculumElements(TaxonomyLevelRef level) {
		return curriculumElementToTaxonomyLevelDao.getCurriculumElements(level);
	}
	
	@Override
	public long countCurriculumElements(List<? extends TaxonomyLevelRef> taxonomyLevels) {
		return curriculumElementToTaxonomyLevelDao.countCurriculumElements(taxonomyLevels);
	}

	@Override
	public List<CurriculumElementWebDAVInfos> getCurriculumElementInfosForWebDAV(IdentityRef identity) {
		List<String> roles = new ArrayList<>();
		roles.add(GroupRoles.owner.name());
		roles.add(GroupRoles.coach.name());
		roles.add(GroupRoles.participant.name());
		return curriculumRepositoryEntryRelationDao.getCurriculumElementInfosForWebDAV(identity, roles);
	}

	@Override
	public List<CurriculumElementRepositoryEntryViews> getCurriculumElements(Identity identity, Roles roles, List<? extends CurriculumRef> curriculum, CurriculumElementStatus[] status) {
		return getCurriculumElements(identity, roles, curriculum, status,
				new RepositoryEntryRuntimeType[]{ RepositoryEntryRuntimeType.standalone }, false);
	}

	@Override
	public List<CurriculumElementRepositoryEntryViews> getCurriculumElements(Identity identity, Roles roles,
			List<? extends CurriculumRef> curriculums, CurriculumElementStatus[] status, 
			RepositoryEntryRuntimeType[] runtimeTypes, boolean participantsOnly) {
		if(curriculums == null || curriculums.isEmpty()) return Collections.emptyList();
		List<CurriculumElementMembership> memberships = curriculumElementDao.getMembershipInfos(curriculums, null, identity);
		Map<Long,CurriculumElementMembership> membershipMap = new HashMap<>();
		for(CurriculumElementMembership membership:memberships) {
			membershipMap.put(membership.getCurriculumElementKey(), membership);
		}
		
		Map<CurriculumElement, List<Long>> elementsMap = curriculumRepositoryEntryRelationDao
				.getCurriculumElementsWithRepositoryEntryKeys(curriculums, status);
		List<CurriculumElementRepositoryEntryViews> elements = new ArrayList<>(elementsMap.size());
		if(!elementsMap.isEmpty()) {
			SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(identity, roles);
			params.setCurriculums(curriculums);
			params.setOfferOrganisations(organisationDao.getOrganisationsWithParentLine(identity, List.of(OrganisationRoles.user.name())));
			params.setOfferValidAt(new Date());
			params.setRuntimeTypes(runtimeTypes);
			params.setParticipantsOnly(participantsOnly);
			
			List<RepositoryEntryMyView> views = myCourseQueries.searchViews(params, 0, -1);
			Map<Long, RepositoryEntryMyView> viewMap = new HashMap<>();
			for(RepositoryEntryMyView view:views) {
				viewMap.put(view.getKey(), view);
			}
			
			Map<Long,CurriculumElementRepositoryEntryViews> elementKeyMap = new HashMap<>();
			for(Map.Entry<CurriculumElement, List<Long>> elementEntry:elementsMap.entrySet()) {
				CurriculumElement element = elementEntry.getKey();
				List<RepositoryEntryMyView> elementViews = new ArrayList<>(elementEntry.getValue().size());
				Set<Long> deduplicatedEntryKeys = new HashSet<>(elementEntry.getValue());
				for(Long entryKey:deduplicatedEntryKeys) {
					RepositoryEntryMyView elementView = viewMap.get(entryKey);
					if(elementView != null) {
						elementViews.add(elementView);
					}
				}
				CurriculumElementMembership membership = membershipMap.get(element.getKey());
				CurriculumElementRepositoryEntryViews view = new CurriculumElementRepositoryEntryViews(element, elementViews, membership);
				elements.add(view);
				elementKeyMap.put(element.getKey(), view);
			}

			// calculate parents
			for(CurriculumElementRepositoryEntryViews element:elements) {
				CurriculumElement parent = element.getCurriculumElement().getParent();
				if(parent != null) {
					element.setParent(elementKeyMap.get(parent.getKey()));
				}
			}
			
			// propagate to the parents
			for(CurriculumElementRepositoryEntryViews element:elements) {
				if(element.isCurriculumMember()) {
					for(CurriculumElementRepositoryEntryViews parentRow=element.getParent(); parentRow != null; parentRow=parentRow.getParent()) {
						parentRow.setCurriculumMember(true);
					}
				}
			}
			
			// trim part of the tree without member flag
			for(Iterator<CurriculumElementRepositoryEntryViews> it=elements.iterator(); it.hasNext(); ) {
				if(!it.next().isCurriculumMember()) {
					it.remove();
				}
			}
		}
		
		return elements;
	}

	@Override
	public List<CurriculumElementKeyToRepositoryEntryKey> getRepositoryEntryKeyToCurriculumElementKeys(List<? extends CurriculumElementRef> curriculumElements) {
		return curriculumRepositoryEntryRelationDao.getRepositoryEntryKeyToCurriculumElementKeys(curriculumElements);
	}
	
	@Override
	public List<CurriculumElement> filterElementsWithoutManagerRole(List<CurriculumElement> elements, Roles roles) {
		if(elements == null || elements.isEmpty()) return elements;

		List<CurriculumElement> allowedToManaged = new ArrayList<>();
		List<OrganisationRef> uOrganisations = roles.getOrganisationsWithRoles(OrganisationRoles.curriculummanager, OrganisationRoles.administrator);
		if(!uOrganisations.isEmpty()) {
			for(CurriculumElement element:elements) {
				Organisation org = element.getCurriculum().getOrganisation();
				if(org != null) {
					for(OrganisationRef uOrganisation:uOrganisations) {
						if(uOrganisation.getKey().equals(org.getKey())) {
							allowedToManaged.add(element);
							break;
						}
					}
				}
			}
		}
		
		return allowedToManaged;
	}

	@Override
	public boolean deleteOrganisationData(Organisation organisation, Organisation replacementOrganisation) {
		CurriculumSearchParameters searchParams = new CurriculumSearchParameters();
		searchParams.setOrganisations(List.of(organisation));
		searchParams.setStatusList(List.of(CurriculumStatus.active, CurriculumStatus.deleted));
		List<Curriculum> curriculums = curriculumDao.search(searchParams);
		for(Curriculum curriculum:curriculums) {
			curriculum.setOrganisation(replacementOrganisation);
			curriculumDao.update(curriculum);
		}
		return true;
	}
	
	@Override
	public CurriculumElement getImplementationOf(CurriculumElement curriculumElement) {
		if(curriculumElement == null || curriculumElement.getParent() == null) {
			return curriculumElement;
		}
		List<CurriculumElement> parentLine = getCurriculumElementParentLine(curriculumElement);
		if(!parentLine.isEmpty()) {
			return parentLine.get(0);
		}
		return curriculumElement;
	}
	
	@Override
	public List<CurriculumElement> getImplementations(Curriculum curriculum, CurriculumElementStatus... status) {
		return curriculumElementDao.getImplementations(curriculum, status);
	}

	@Override
	public VFSContainer getMediaContainer(CurriculumElement curriculumElement) {
		VFSContainer mediaContainer = curriculumStorage.getMediaContainer(curriculumElement);
		mediaContainer = new NamedContainerImpl(curriculumElement.getDisplayName(), mediaContainer);
		return mediaContainer;
	}
	
	@Override
	public void storeCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type, File file, String filename, Identity savedBy) {
		curriculumStorage.storeCurriculumElementFile(element, type, savedBy, file, filename);
	}
	
	@Override
	public void deleteCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type) {
		curriculumStorage.deleteCurriculumElementFile(element, type);
	}
	
	@Override
	public VFSLeaf getCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type) {
		return curriculumStorage.getCurriculumElementFile(element, type);
	}
	
	@Override
	public boolean numberRootCurriculumElement(CurriculumElement rootElement) {
		if(rootElement.getParent() != null) {
			log.warn("Try to number a curriculum element which is not an implementation: {}", rootElement);
		}
		
		Locale locale = i18nModule.defaultLocale();
		
		List<NumberingCurriculumElement> elements = curriculumElementDao.getDescendants(rootElement).stream()
				.map(NumberingCurriculumElement::new)
				.collect(Collectors.toList());
		// Build parent line
		Map<Long,NumberingCurriculumElement> keyToElements = elements.stream()
				.collect(Collectors.toMap(NumberingCurriculumElement::getKey, el -> el, (u, v) -> u));
		for(NumberingCurriculumElement element:elements) {
			if(element.getParentKey() != null) {
				element.setParent(keyToElements.get(element.getParentKey()));
			}
		}
	
		// Sort the tree
		Collections.sort(elements, new CurriculumElementTreeRowComparator(locale));
		boolean changed = number(null, List.of(), 0, elements);
		dbInstance.commit();
		return changed;
	}
	
	private boolean number(NumberingCurriculumElement parent, List<Long> numbering, int start, List<NumberingCurriculumElement> elements) {
		int count = 1;
		boolean changed = false;
		for(int i=start; i<elements.size(); i++) {
			NumberingCurriculumElement element = elements.get(i);
			if(Objects.equals(parent, element.getParent())) {
				List<Long> values = new ArrayList<>(numbering);
				values.add(Long.valueOf(count++));
				String number = new Numbering(values).toString();
				if(!number.equals(element.getCurriculumElement().getNumberImpl())) {
					curriculumElementDao.updateNumber(element, number);
					changed |= true;
				}
				changed |= number(element, values, i+1, elements);
			}
		}
		return changed;
	}
	
	private void sendDeferredEvents(List<CurriculumElementMembershipEvent> events) {
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		for(CurriculumElementMembershipEvent event:events) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CurriculumElement.class, event.getCurriculumElementKey());
			eventBus.fireEventToListenersOf(event, ores);
		}
	}
	
	private void fireRepositoryEntryAddedEvent(CurriculumElementRef element, RepositoryEntryRef entry) {
		CurriculumElementRepositoryEntryEvent event = new CurriculumElementRepositoryEntryEvent(REPOSITORY_ENTRY_ADDED,
				element.getKey(), entry.getKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(CurriculumElement.class, event.getCurriculumElementKey());
		coordinator.getCoordinator().getEventBus().fireEventToListenersOf(event, ores);
	}

	@Override
	public List<BusinessGroup> deleteInternalGroupMembershipsAndInvitations(RepositoryEntry courseEntry) {
		List<BusinessGroup> businessGroups = getAllBusinessGroups(courseEntry);
		for (BusinessGroup businessGroup : businessGroups) {
			deleteGroupMembershipsAndInvitation(businessGroup);
		}
		return businessGroups;
	}
	
	private List<BusinessGroup> getAllBusinessGroups(RepositoryEntry courseEntry) {
		if (!RepositoryEntryRuntimeType.curricular.equals(courseEntry.getRuntimeType())) {
			return Collections.emptyList();
		}

		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		return businessGroupDAO
				.findBusinessGroups(params, courseEntry, 0, -1).stream()
				.filter(bg -> {
					if (LTI13Service.LTI_GROUP_TYPE.equals(bg.getTechnicalType())) {
						return false;
					}
					if (StringHelper.containsNonWhitespace(bg.getManagedFlagsString())) {
						return false;
					}
					List<Long> entryKeys = businessGroupRelationDao.getRepositoryEntryKeys(bg);
					if (entryKeys.size() != 1) {
						return false;
					}
					return true;
				})
				.toList();
	}

	private void deleteGroupMembershipsAndInvitation(BusinessGroup businessGroup) {
		groupDao.removeMemberships(businessGroup.getBaseGroup());
		groupMembershipHistoryDao.deleteMembershipHistory(businessGroup.getBaseGroup());
		
		// Also delete invitations related to the group
		invitationDao.deleteInvitation(businessGroup.getBaseGroup());
	}

	@Override
	public void deleteInternalGroups(List<BusinessGroup> internalGroups, Identity doer) {
		BusinessGroupLifecycleManager businessGroupLifecycleManager = CoreSpringFactory.getImpl(BusinessGroupLifecycleManager.class);
		for (BusinessGroup businessGroup : internalGroups) {
			businessGroupLifecycleManager.deleteBusinessGroup(businessGroup, doer, false);
		}
	}
}
