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

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumAutomationService;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 6 mars 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CurriculumAutomationServiceImpl implements CurriculumAutomationService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private CurriculumService curriculumService;
	
	@Override
	public void instantiate() {
		Date now = DateUtils.getStartOfDay(new Date());
		List<CurriculumElement> elements = loadElementsInstantiate();
		for(CurriculumElement element:elements) {
			tryToInstantiate(element, now);
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void tryToInstantiate(CurriculumElement element, Date referenceDate) {
		Date beginDate = getBeginDate(element);
		if(beginDate == null) {
			return;
		}
		
		CurriculumElement implementation = curriculumService.getImplementationOf(element);
		Date date = implementation.getAutoInstantiation().getDateBefore(beginDate);
		if(date != null && date.compareTo(referenceDate) <= 0) {
			if(curriculumService.hasRepositoryEntries(element)) {
				return;// Has already courses
			}
			
			List<RepositoryEntry> templates = curriculumService.getRepositoryTemplates(element);
			for(RepositoryEntry template:templates) {
				String externalRef = buildDefaultIdentifier(element);
				curriculumService.instantiateTemplate(template, element, element.getDisplayName(), externalRef,
						element.getBeginDate(), element.getEndDate(), null);
			}
		}
	}
	
	private String buildDefaultIdentifier(CurriculumElement element) {
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(element.getIdentifier())) {
			sb.append(element.getIdentifier());
		}
		return sb.toString();
	}
	
	protected List<CurriculumElement> loadElementsInstantiate() {
		String query = """
			select curEl from curriculumelement as curEl
			where exists (select implEl.key from curriculumelement as implEl
			  where curEl.materializedPathKeys like implEl.materializedPathKeys || '%'
			  and implEl.parent.key is null and implEl.autoInstantiation.unit is not null
			  and implEl.status in (:implementationStatus)
			)
			and exists (select template.key from repotemplatetogroup as template
			  where curEl.group.key=template.group.key
			)
			and not exists (select course.key from repoentrytogroup as course
			  where curEl.group.key=course.group.key
			)""";
		
		List<String> implementationStatus = List.of(CurriculumElementStatus.confirmed.name());
		return dbInstance.getCurrentEntityManager().createQuery(query, CurriculumElement.class)
				.setParameter("implementationStatus", implementationStatus)
				.getResultList();
	}
	
	@Override
	public void accessForCoach() {
		Date now = DateUtils.getStartOfDay(new Date());
		List<CurriculumElement> elements = loadElementsToAccessForCoach();
		for(CurriculumElement element:elements) {
			tryAccessForCoach(element, now);
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void tryAccessForCoach(CurriculumElement element, Date referenceDate) {
		Date beginDate = getBeginDate(element);
		if(beginDate == null) {
			return;
		}
		
		CurriculumElement implementation = curriculumService.getImplementationOf(element);
		Date date = implementation.getAutoAccessForCoach().getDateBefore(beginDate);
		if(date != null && date.compareTo(referenceDate) <= 0) {
			List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
			for(RepositoryEntry entry:entries) {
				RepositoryEntryStatusEnum status = entry.getEntryStatus();
				if(status == RepositoryEntryStatusEnum.preparation || status == RepositoryEntryStatusEnum.review) {
					repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.coachpublished);
				}
			}
		}
	}
	
	protected List<CurriculumElement> loadElementsToAccessForCoach() {
		String query = """
			select curEl from curriculumelement as curEl
			where exists (select implEl.key from curriculumelement as implEl
			  where curEl.materializedPathKeys like implEl.materializedPathKeys || '%'
			  and implEl.parent.key is null and implEl.autoAccessForCoach.unit is not null
			  and implEl.status in (:implementationStatus)
			)
			and exists (select v.key from repositoryentry as v
			  inner join v.groups as relGroup
			  where relGroup.group.key = curEl.group.key and v.status in (:courseStatus)
			)""";
		
		List<String> courseStatus = List.of(RepositoryEntryStatusEnum.preparation.name(),
				RepositoryEntryStatusEnum.review.name());
		List<String> implementationStatus = List.of(CurriculumElementStatus.confirmed.name());
		return dbInstance.getCurrentEntityManager().createQuery(query, CurriculumElement.class)
				.setParameter("implementationStatus", implementationStatus)
				.setParameter("courseStatus", courseStatus)
				.getResultList();
	}
	
	@Override
	public void publish() {
		Date now = DateUtils.getStartOfDay(new Date());
		List<CurriculumElement> elements = loadElementsToPublish();
		for(CurriculumElement element:elements) {
			tryPublish(element, now);
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void tryPublish(CurriculumElement element, Date referenceDate) {
		Date beginDate = getBeginDate(element);
		if(beginDate == null) {
			return;
		}
		
		CurriculumElement implementation = curriculumService.getImplementationOf(element);
		Date date = implementation.getAutoPublished().getDateBefore(beginDate);
		if(date != null && date.compareTo(referenceDate) <= 0) {
			List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
			for(RepositoryEntry entry:entries) {
				RepositoryEntryStatusEnum status = entry.getEntryStatus();
				if(status == RepositoryEntryStatusEnum.preparation
						|| status == RepositoryEntryStatusEnum.review 
						|| status == RepositoryEntryStatusEnum.coachpublished) {
					repositoryManager.setStatus(entry, RepositoryEntryStatusEnum.published);
				}
			}
		}
	}
	
	protected List<CurriculumElement> loadElementsToPublish() {
		String query = """
			select curEl from curriculumelement as curEl
			where exists (select implEl.key from curriculumelement as implEl
			  where curEl.materializedPathKeys like implEl.materializedPathKeys || '%'
			  and implEl.parent.key is null and implEl.autoPublished.unit is not null
			  and implEl.status in (:implementationStatus)
			)
			and exists (select v.key from repositoryentry as v
			  inner join v.groups as relGroup
			  where relGroup.group.key = curEl.group.key and v.status in (:courseStatus)
			)""";
		
		List<String> courseStatus = List.of(RepositoryEntryStatusEnum.preparation.name(),
				RepositoryEntryStatusEnum.review.name(),
				RepositoryEntryStatusEnum.coachpublished.name());
		List<String> implementationStatus = List.of(CurriculumElementStatus.confirmed.name());
		return dbInstance.getCurrentEntityManager().createQuery(query, CurriculumElement.class)
				.setParameter("implementationStatus", implementationStatus)
				.setParameter("courseStatus", courseStatus)
				.getResultList();
	}
	
	@Override
	public void close() {
		Date now = DateUtils.getEndOfDay(new Date());
		List<CurriculumElement> elements = loadElementsToClose();
		for(CurriculumElement element:elements) {
			tryClose(element, now);
			dbInstance.commitAndCloseSession();
		}
		dbInstance.commitAndCloseSession();
	}
	
	private void tryClose(CurriculumElement element, Date referenceDate) {
		Date endDate = getEndDate(element);
		if(endDate == null) {
			return;
		}
		
		CurriculumElement implementation = curriculumService.getImplementationOf(element);
		Date date = implementation.getAutoClosed().getDateAfter(endDate);
		if(date != null) {
			date = DateUtils.getEndOfDay(date);
			if(date.compareTo(referenceDate) < 0) {
				List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(element);
				for(RepositoryEntry entry:entries) {
					RepositoryEntryStatusEnum status = entry.getEntryStatus();
					if(status == RepositoryEntryStatusEnum.preparation
							|| status == RepositoryEntryStatusEnum.review 
							|| status == RepositoryEntryStatusEnum.coachpublished
							|| status == RepositoryEntryStatusEnum.published) {
						repositoryService.closeRepositoryEntry(entry, null, true);
					}
				}
			}
		}
	}
	
	protected List<CurriculumElement> loadElementsToClose() {
		String query = """
			select curEl from curriculumelement as curEl
			where exists (select implEl.key from curriculumelement as implEl
			  where curEl.materializedPathKeys like implEl.materializedPathKeys || '%'
			  and implEl.parent.key is null and implEl.autoClosed.unit is not null
			  and implEl.status in (:implementationStatus)
			)
			and exists (select v.key from repositoryentry as v
			  inner join v.groups as relGroup
			  where relGroup.group.key = curEl.group.key and v.status in (:courseStatus)
			)""";
		
		List<String> courseStatus = List.of(RepositoryEntryStatusEnum.preparation.name(),
				RepositoryEntryStatusEnum.review.name(),
				RepositoryEntryStatusEnum.coachpublished.name(),
				RepositoryEntryStatusEnum.published.name());
		List<String> implementationStatus = List.of(CurriculumElementStatus.confirmed.name());
		return dbInstance.getCurrentEntityManager().createQuery(query, CurriculumElement.class)
				.setParameter("implementationStatus", implementationStatus)
				.setParameter("courseStatus", courseStatus)
				.getResultList();
	}
	
	@Override
	public Date getBeginDate(CurriculumElement element) {
		CurriculumElement beginElement = getBeginCurriculumElement(element);
		return beginElement == null ? null : beginElement.getBeginDate();
	}

	@Override
	public CurriculumElement getBeginCurriculumElement(CurriculumElement element) {
		if(element.getBeginDate() != null) {
			return element;
		}
		
		List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(element);
		for(int i=parentLine.size(); i-->0; ) {
			CurriculumElement parent = parentLine.get(i);
			if(parent.getBeginDate() != null) {
				return parent;
			}
		}
		return null;
	}

	@Override
	public Date getEndDate(CurriculumElement element) {
		CurriculumElement endElement = getEndCurriculumElement(element);
		return endElement == null ? null : endElement.getEndDate();
	}
	
	@Override
	public CurriculumElement getEndCurriculumElement(CurriculumElement element) {
		if(element.getEndDate() != null) {
			return element;
		}
		
		List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(element);
		for(int i=parentLine.size(); i-->0; ) {
			CurriculumElement parent = parentLine.get(i);
			if(parent.getEndDate() != null) {
				return parent;
			}
		}
		return null;
	}
}
