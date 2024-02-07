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
package org.olat.course.duedate.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.LazyInitializationException;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.util.DateUtils;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.duedate.RelativeDueDateConfig;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DueDateServiceImpl implements DueDateService {
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryEntryLifecycleDAO repositoryEntryLifecycleDao;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;

	@Override
	public List<String> getCourseRelativeToDateTypes(RepositoryEntry courseEntry) {
		RepositoryEntryLifecycle lifecycle = getRepositoryEntryLifecycle(courseEntry);
		boolean courseStart = lifecycle != null && lifecycle.getValidFrom() != null;
		return courseStart
				? List.of(TYPE_COURSE_START, TYPE_COURSE_LAUNCH, TYPE_ENROLLMENT)
				: List.of(TYPE_COURSE_LAUNCH, TYPE_ENROLLMENT);
	}
	
	@Override
	public Map<Long, Date> getIdentityKeyToDueDate(DueDateConfig config, RepositoryEntry courseEntry, Collection<? extends IdentityRef> identities) {
		if (DueDateConfig.isAbsolute(config)) {
			Date dueDate = config.getAbsoluteDate();
			return identities.stream().collect(Collectors.toMap(IdentityRef::getKey, i -> dueDate));
		} else if (DueDateConfig.isRelative(config)) {
			return getIdentityKeyToRelativeDate(config, courseEntry, identities);
		}
		return Map.of();
	}

	@Override
	public Date getDueDate(DueDateConfig config, RepositoryEntry courseEntry, IdentityRef identity) {
		Date dueDate = null;
		if (DueDateConfig.isAbsolute(config)) {
			dueDate = config.getAbsoluteDate();
		} else if (DueDateConfig.isRelative(config)) {
			dueDate = getRelativeDate(config, courseEntry, identity);
		}
		return dueDate;
	}

	@Override
	public Date getRelativeDate(RelativeDueDateConfig config, RepositoryEntry courseEntry, IdentityRef identity) {
		return getIdentityKeyToRelativeDate(config, courseEntry, List.of(identity)).getOrDefault(identity.getKey(), null);
	}
	
	private Map<Long, Date> getIdentityKeyToRelativeDate(RelativeDueDateConfig config, RepositoryEntry courseEntry,
			Collection<? extends IdentityRef> identities) {
		if (TYPE_COURSE_START.equals(config.getRelativeToType())) {
			Date courseStart = addNumOfDays(getCourseStart(courseEntry), config.getNumOfDays());
			if(courseStart == null) {
				return Map.of();
			}
			return identities.stream().collect(Collectors.toMap(IdentityRef::getKey, i2 -> courseStart));
		} else if (TYPE_COURSE_LAUNCH.equals(config.getRelativeToType())) {
			Map<Long, Date> initialLaunchDates = userCourseInformationsManager.getInitialLaunchDates(courseEntry, identities);
			initialLaunchDates.entrySet().forEach(entry -> {
					entry.setValue(addNumOfDays(entry.getValue(), config.getNumOfDays()));
				});
			return initialLaunchDates;
		} else if (TYPE_ENROLLMENT.equals(config.getRelativeToType())) {
			Map<Long, Date> enrollmentDates = repositoryService.getEnrollmentDates(courseEntry, identities);
			enrollmentDates.entrySet().forEach(entry -> {
					entry.setValue(addNumOfDays(entry.getValue(), config.getNumOfDays()));
				});
			return enrollmentDates;
		}
		return Map.of();
	}

	@Override
	public Date getRelativeDate(RelativeDueDateConfig config, RepositoryEntry courseEntry, BusinessGroupRef businessGroup) {
		Date relativeDate = null;
		if (TYPE_COURSE_START.equals(config.getRelativeToType())) {
			relativeDate = getCourseStart(courseEntry);
		} else if (TYPE_COURSE_LAUNCH.equals(config.getRelativeToType())) {
			relativeDate =  userCourseInformationsManager.getInitialParticipantLaunchDate(courseEntry, businessGroup);
		} else if (TYPE_ENROLLMENT.equals(config.getRelativeToType())) {
			relativeDate =  businessGroupService.getFirstEnrollmentDate(businessGroup, GroupRoles.participant.name());
		}
		relativeDate = addNumOfDays(relativeDate, config.getNumOfDays());
		return relativeDate;
	}

	private Date getCourseStart(RepositoryEntry courseEntry) {
		RepositoryEntryLifecycle lifecycle = getRepositoryEntryLifecycle(courseEntry);
		if (lifecycle != null && lifecycle.getValidFrom() != null) {
			return lifecycle.getValidFrom();
		}
		return null;
	}
	
	/**
	 * This is a secure way to load the life cycle.
	 * 
	 * @param re The repository entry
	 * @return The repository entry life cycle
	 */
	private RepositoryEntryLifecycle getRepositoryEntryLifecycle(RepositoryEntry re) {
		try {
			RepositoryEntryLifecycle lifecycle = re.getLifecycle();
			if(lifecycle != null) {
				lifecycle.getValidTo();
			}
			return lifecycle;
		} catch (LazyInitializationException e) {
			return repositoryEntryLifecycleDao.loadByEntry(re);
		}
	}

	@Override
	public Date addNumOfDays(Date date, int numOfDays) {
		return DateUtils.addDays(date, numOfDays);
	}

}
