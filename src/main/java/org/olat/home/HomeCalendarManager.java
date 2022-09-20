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
package org.olat.home;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.collaboration.CollaborationManager;
import org.olat.collaboration.CollaborationTools;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.PersonalCalendarManager;
import org.olat.commons.calendar.manager.ImportCalendarManager;
import org.olat.commons.calendar.model.CalendarFileInfos;
import org.olat.commons.calendar.model.CalendarKey;
import org.olat.commons.calendar.model.CalendarUserConfiguration;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.run.calendar.CourseLinkProviderController;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.resource.OLATResource;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class HomeCalendarManager implements PersonalCalendarManager, UserDataDeletable, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(HomeCalendarManager.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private ImportCalendarManager importCalendarManager;

	@Override
	public List<CalendarFileInfos> getListOfCalendarsFiles(Identity identity) {
		List<CalendarFileInfos> aggregatedFiles = new ArrayList<>();

		Map<CalendarKey,CalendarUserConfiguration> configMap = calendarManager.getCalendarUserConfigurationsMap(identity);
		
		//personal calendar
		CalendarKey personalCalendarKey = new CalendarKey(identity.getName(), CalendarManager.TYPE_USER);
		CalendarUserConfiguration personalCalendarConfig = configMap.get(personalCalendarKey);
		if(calendarModule.isEnablePersonalCalendar()
				&& (personalCalendarConfig == null || personalCalendarConfig.isInAggregatedFeed())) {
			File iCalFile = calendarManager.getCalendarICalFile(CalendarManager.TYPE_USER, identity.getName());
			if(iCalFile != null) {
				aggregatedFiles.add(new CalendarFileInfos(identity.getName(), CalendarManager.TYPE_USER, iCalFile));
			}
			
			//reload every hour
			List<CalendarFileInfos> importedCalendars = importCalendarManager.getImportedCalendarInfosForIdentity(identity, true);
			aggregatedFiles.addAll(importedCalendars);
		}

		//group calendars
		if(calendarModule.isEnableGroupCalendar()) {
			SearchBusinessGroupParams groupParams = new SearchBusinessGroupParams(identity, true, true);
			groupParams.addTools(CollaborationTools.TOOL_CALENDAR);
			List<BusinessGroup> groups = businessGroupService.findBusinessGroups(groupParams, null, 0, -1);
			Set<BusinessGroup> resourceSet = new HashSet<>();
			for(BusinessGroup group:groups) {
				if(resourceSet.contains(group)) {
					continue;
				} else {
					resourceSet.add(group);
				}
				
				String calendarId = group.getKey().toString();
				CalendarKey key = new CalendarKey(calendarId, CalendarManager.TYPE_GROUP);
				CalendarUserConfiguration calendarConfig = configMap.get(key);
				if(calendarConfig == null || calendarConfig.isInAggregatedFeed()) {
					File iCalFile = calendarManager.getCalendarICalFile(CalendarManager.TYPE_GROUP, calendarId);
					if(iCalFile != null) {
						aggregatedFiles.add(new CalendarFileInfos(calendarId, CalendarManager.TYPE_GROUP, iCalFile));
					}
				}
			}
		}
		
		if(calendarModule.isEnableCourseElementCalendar() || calendarModule.isEnableCourseToolCalendar()) {
			List<Object[]> resources =  getCourses(identity);
			Set<RepositoryEntry> resourceSet = new HashSet<>();
			for(Object[] resource:resources) {
				RepositoryEntry courseEntry = (RepositoryEntry)resource[0];
				if(resourceSet.contains(courseEntry)) {
					continue;
				} else {
					resourceSet.add(courseEntry);
				}
				
				String calendarId = courseEntry.getOlatResource().getResourceableId().toString();
				CalendarKey key = new CalendarKey(calendarId, CalendarManager.TYPE_COURSE);
				CalendarUserConfiguration calendarConfig = configMap.get(key);
				if(calendarConfig == null || calendarConfig.isInAggregatedFeed()) {
					File iCalFile = calendarManager.getCalendarICalFile(CalendarManager.TYPE_COURSE, calendarId);
					if(iCalFile != null) {
						aggregatedFiles.add(new CalendarFileInfos(calendarId, CalendarManager.TYPE_COURSE, iCalFile));
					}
				}
			}
		}
		
		return aggregatedFiles;
	}
	
	//
	
	@Override
	public List<KalendarRenderWrapper> getListOfCalendarWrappers(UserRequest ureq, WindowControl wControl) {
		if(!calendarModule.isEnabled()) {
			return new ArrayList<>();
		}
		
		Identity identity = ureq.getIdentity();
		
		List<KalendarRenderWrapper> calendars = new ArrayList<>();
		Map<CalendarKey,CalendarUserConfiguration> configMap = calendarManager
				.getCalendarUserConfigurationsMap(ureq.getIdentity());
		appendPersonalCalendar(identity, calendars, configMap);
		appendGroupCalendars(identity, calendars, configMap);
		appendCourseCalendars(ureq, wControl, calendars, configMap);
		
		//reload every hour
		List<KalendarRenderWrapper> importedCalendars = importCalendarManager.getImportedCalendarsForIdentity(identity, true);
		for(KalendarRenderWrapper importedCalendar:importedCalendars) {
			importedCalendar.setPrivateEventsVisible(true);
		}
		
		calendars.addAll(importedCalendars);
		return calendars;
	}
	
	private void appendPersonalCalendar(Identity identity, List<KalendarRenderWrapper> calendars,
			Map<CalendarKey,CalendarUserConfiguration> configMap) {
		// get the personal calendar
		if(calendarModule.isEnablePersonalCalendar()) {
			try {
				KalendarRenderWrapper calendarWrapper = calendarManager.getPersonalCalendar(identity);
				calendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
				calendarWrapper.setPrivateEventsVisible(true);
				CalendarUserConfiguration config = configMap.get(calendarWrapper.getCalendarKey());
				if (config != null) {
					calendarWrapper.setConfiguration(config);
				}
				calendars.add(calendarWrapper);
			} catch (Exception e) {
				log.error("Cannot read personal calendar of: " + identity, e);
			}
		}
	}
	
	private void appendGroupCalendars(Identity identity, List<KalendarRenderWrapper> calendars,
			Map<CalendarKey,CalendarUserConfiguration> configMap) {
		// get group calendars
		if(calendarModule.isEnableGroupCalendar()) {
			SearchBusinessGroupParams groupParams = new SearchBusinessGroupParams(identity, true, false);
			groupParams.addTools(CollaborationTools.TOOL_CALENDAR);
			List<BusinessGroup> ownerGroups = businessGroupService.findBusinessGroups(groupParams, null, 0, -1);
			addCalendars(ownerGroups, true, false, calendars, configMap);
			
			SearchBusinessGroupParams groupParams2 = new SearchBusinessGroupParams(identity, false, true);
			groupParams2.addTools(CollaborationTools.TOOL_CALENDAR);
			List<BusinessGroup> attendedGroups = businessGroupService.findBusinessGroups(groupParams2, null, 0, -1);
			attendedGroups.removeAll(ownerGroups);
			addCalendars(attendedGroups, false, true, calendars, configMap);
		}
	}

	private void appendCourseCalendars(UserRequest ureq, WindowControl wControl, List<KalendarRenderWrapper> calendars,
			Map<CalendarKey,CalendarUserConfiguration> configMap) {
		if(calendarModule.isEnableCourseElementCalendar() || calendarModule.isEnableCourseToolCalendar()) {
			
			// add course calendars
			List<Object[]> resources = getCourses(ureq.getIdentity());
			Set<OLATResource> editoredResources = getEditorGrants(ureq.getIdentity());
			
			Set<Long> duplicates = new HashSet<>();
			
			for (Object[] resource:resources) {
				RepositoryEntry courseEntry = (RepositoryEntry)resource[0];
				if(duplicates.contains(courseEntry.getKey())) {
					continue;
				}
				duplicates.add(courseEntry.getKey());
				
				String role = (String)resource[1];
				Long courseResourceableID = courseEntry.getOlatResource().getResourceableId();
				try {
					ICourse course = CourseFactory.loadCourse(courseEntry);
					if(CourseCalendars.isCourseCalendarEnabled(course)) {
						//calendar course aren't enabled per default but course node of type calendar are always possible
						// add course calendar
						KalendarRenderWrapper courseCalendarWrapper = calendarManager.getCourseCalendar(course);
						boolean isPrivileged = GroupRoles.owner.name().equals(role) || editoredResources.contains(courseEntry.getOlatResource());
						if (isPrivileged) {
							courseCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
						} else {
							courseCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
						}
						
						if(role != null && (GroupRoles.owner.name().equals(role) || GroupRoles.coach.name().equals(role) || GroupRoles.participant.name().equals(role))) {
							courseCalendarWrapper.setPrivateEventsVisible(true);
						}

						CalendarUserConfiguration config = configMap.get(courseCalendarWrapper.getCalendarKey());
						if (config != null) {
							courseCalendarWrapper.setConfiguration(config);
						}
						courseCalendarWrapper.setLinkProvider(new CourseLinkProviderController(course, Collections.singletonList(course), ureq, wControl));
						calendars.add(courseCalendarWrapper);
					}
				} catch (CorruptedCourseException e) {
					OLATResource olatResource = courseEntry.getOlatResource();
					log.error("Corrupted course: " + olatResource.getResourceableTypeName() + " :: " + courseResourceableID);
				} catch (Exception e) {
					OLATResource olatResource = courseEntry.getOlatResource();
					log.error("Cannor read calendar of course: " + olatResource.getResourceableTypeName() + " :: " + courseResourceableID);
				}
			}
		}
	}

	
	/**
	 * 
	 * @param identity
	 * @return List of array, first the repository entry, second the role
	 */
	private List<Object[]> getCourses(IdentityRef identity) {
		QueryBuilder sb = new QueryBuilder(2048);
		sb.append("select v, membership.role from repositoryentry  v ")
		  .append(" inner join fetch v.olatResource as resource ")
		  .append(" inner join v.groups as retogroup")
		  .append(" inner join retogroup.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where v.olatResource.resName='CourseModule' and membership.identity.key=:identityKey and")
		  .append(" (")
		  .append("   (v.status ").in(RepositoryEntryStatusEnum.reviewToClosed()).append(" and membership.role='").append(GroupRoles.owner.name()).append("')")
		  .append("   or")
		  .append("   (v.status ").in(RepositoryEntryStatusEnum.coachPublishedToClosed()).append(" and membership.role='").append(GroupRoles.coach.name()).append("')")
		  .append("   or")
		  .append("   (v.status ").in(RepositoryEntryStatusEnum.publishedAndClosed()).append(" and membership.role='").append(GroupRoles.participant.name()).append("')")
		  .append(" )");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Object[].class)
			.setParameter("identityKey", identity.getKey())
			.getResultList();
	}
	
	private Set<OLATResource> getEditorGrants(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select grant.resource from bgrant as grant")
		  .append(" inner join grant.group as baseGroup")
		  .append(" inner join baseGroup.members as membership")
		  .append(" where membership.identity.key=:identityKey and grant.permission='").append(CourseRights.RIGHT_COURSEEDITOR).append("' and membership.role=grant.role");
		List<OLATResource> resources = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OLATResource.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		return new HashSet<>(resources);
	}
	
	/**
	 * Append the calendars of a list of groups. The groups must have their calendar tool
	 * enabled, this routine doesn't check it.
	 * @param ureq
	 * @param groups
	 * @param isOwner
	 * @param calendars
	 */
	private void addCalendars(List<BusinessGroup> groups, boolean isOwner, boolean isParticipant,
			List<KalendarRenderWrapper> calendars, Map<CalendarKey,CalendarUserConfiguration> configMap) {
		
		Map<Long,Long> groupKeyToAccess = CoreSpringFactory.getImpl(CollaborationManager.class).lookupCalendarAccess(groups);
		for (BusinessGroup bGroup:groups) {
			try {
				KalendarRenderWrapper groupCalendarWrapper = calendarManager.getGroupCalendar(bGroup);
				groupCalendarWrapper.setPrivateEventsVisible(true);
				// set calendar access
				int iCalAccess = CollaborationTools.CALENDAR_ACCESS_OWNERS;
				Long lCalAccess = groupKeyToAccess.get(bGroup.getKey());
				if (lCalAccess != null) {
					iCalAccess = lCalAccess.intValue();
				}
				if (iCalAccess == CollaborationTools.CALENDAR_ACCESS_OWNERS && !isOwner) {
					groupCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_ONLY);
				} else {
					groupCalendarWrapper.setAccess(KalendarRenderWrapper.ACCESS_READ_WRITE);
				}
				CalendarUserConfiguration config = configMap.get(groupCalendarWrapper.getCalendarKey());
				if (config != null) {
					groupCalendarWrapper.setConfiguration(config);
				}
				if(isOwner || isParticipant) {
					groupCalendarWrapper.setPrivateEventsVisible(true);
				}
				calendars.add(groupCalendarWrapper);
			} catch (Exception e) {
				log.error("Cannot read calendar of group: " + bGroup, e);
			}
		}
	}
	
	@Override
	public String getExporterID() {
		return "calendars";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		File calendars = new File(archiveDirectory, "calendars");
		File iCalFile = calendarManager.getCalendarICalFile(CalendarManager.TYPE_USER, identity.getName());
		if(iCalFile != null && iCalFile.exists()) {
			FileUtils.copyFileToDir(iCalFile, calendars, false, "Archive calendar");
			manifest.appendFile("calendars/" + iCalFile.getName());
		}
		List<CalendarFileInfos> importedCalendars = importCalendarManager.getImportedCalendarInfosForIdentity(identity, false);
		for(CalendarFileInfos importedCalendar:importedCalendars) {
			File importedCalFile = importedCalendar.getCalendarFile();
			if(importedCalFile != null && importedCalFile.exists()) {
				FileUtils.copyFileToDir(importedCalFile, calendars, false, "Archive calendar");
				manifest.appendFile("calendars/" + importedCalFile.getName());
			}
		}
	}

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		File iCalFile = calendarManager.getCalendarICalFile(CalendarManager.TYPE_USER, identity.getName());
		deleteCalendarFile(iCalFile);

		List<CalendarFileInfos> importedCalendars = importCalendarManager.getImportedCalendarInfosForIdentity(identity, false);
		for(CalendarFileInfos importedCalendar:importedCalendars) {
			deleteCalendarFile(importedCalendar.getCalendarFile());
		}
	}
	
	private void deleteCalendarFile(File calendarFile) {
		if(calendarFile != null && calendarFile.exists()) {
			try {
				Files.deleteIfExists(calendarFile.toPath());
			} catch (IOException e) {
				log.error("Cannot delete calendar: {}", calendarFile);
			}
		}
	}
}
