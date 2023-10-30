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
package org.olat.modules.project.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.model.TitleItem;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateRange;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjActivity;
import org.olat.modules.project.ProjActivitySearchParams;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactSearchParams;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ui.ProjTimelineActivityRowsFactory;
import org.olat.modules.project.ui.ProjTimelineActivityRowsFactory.ActivityKey;
import org.olat.modules.project.ui.ProjTimelineActivityRowsFactory.ActivityRowData;
import org.olat.modules.project.ui.ProjTimelineRow;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 2 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjectNotificationsHandler implements NotificationsHandler {
	
	private static final Logger log = Tracing.createLoggerFor(ProjectNotificationsHandler.class);
	
	@Autowired
	private ProjectService projectService;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private UserManager userManager;
	
	@Override
	public String getType() {
		return ProjProject.TYPE;
	}

	@Override
	public String getDisplayName(Publisher publisher) {
		ProjProject project = projectService.getProject(() -> publisher.getResId());
		return project != null? project.getTitle(): null;
	}

	@Override
	public String getIconCss() {
		return CSSHelper.getIconCssClassFor("o_icon_proj_project");
	}

	@Override
	public String getAdditionalDescriptionI18nKey(Locale locale) {
		return null;
	}

	@Override
	public String createTitleInfo(Subscriber subscriber, Locale locale) {
		String displayName = getDisplayName(subscriber.getPublisher());
		return StringHelper.containsNonWhitespace(displayName)
				? Util.createPackageTranslator(ProjectUIFactory.class, locale).translate("notifications.title", displayName)
				: "";
	}

	@Override
	public SubscriptionInfo createSubscriptionInfo(Subscriber subscriber, Locale locale, Date compareDate) {
		Publisher p = subscriber.getPublisher();
		Date latestNews = p.getLatestNewsDate();
	
		SubscriptionInfo si = null;
		try {
			if (notificationsManager.isPublisherValid(p) && compareDate.before(latestNews)) {
				ProjProject project = projectService.getProject(() -> p.getResId());
				if (project != null) {
					Translator translator = Util.createPackageTranslator(ProjectUIFactory.class, locale);
					Formatter formatter = Formatter.getInstance(locale);
					ProjTimelineActivityRowsFactory activityRowsFactory = new ProjTimelineActivityRowsFactory(translator, formatter, userManager);
					List<ProjTimelineRow> rows = loadActivites(project, new DateRange(compareDate, new Date()),
							activityRowsFactory, ProjTimelineActivityRowsFactory::keyWithoutDate);
					if (!rows.isEmpty()) {
						rows.sort((r1, r2) -> r2.getDate().compareTo(r1.getDate()));
						List<SubscriptionListItem> items = rows.stream().map(row -> toItem(translator, row)).toList();
						String title = translator.translate(ProjectUIFactory.templateSuffix("notifications.info.title", project), project.getTitle());
						TitleItem titleItem = new TitleItem(title, "o_icon_proj_project");
						si = new SubscriptionInfo(subscriber.getKey(), p.getType(), titleItem, items);
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot create project notifications for subscriber: {}", subscriber.getKey(), e);
		}
		if (si == null) {
			si = notificationsManager.getNoSubscriptionInfo();
		}
		return si;
	}
	
	private SubscriptionListItem toItem(Translator translator, ProjTimelineRow row) {
		String desc = translator.translate("notifications.info.desc", row.getDoerDisplyName(), row.getMessage());
		String url = null;
		String businessPath = null;
		if (row.getProject() != null) {
			String artefactType = row.getArtefact() != null? row.getArtefact().getType(): null;
			Long artefactKey =  row.getBusinessPathKey();
			ProjectBCFactory bcFactory = ProjectBCFactory.createFactory(row.getProject());
			url = bcFactory.getArtefactUrl(row.getProject(), artefactType, artefactKey);
			businessPath = bcFactory.getBusinessPath(row.getProject(), artefactType, artefactKey);
		}
		return new SubscriptionListItem(desc, url, businessPath, row.getDate(), row.getIconCssClass());
	}
	
	public List<ProjTimelineRow> loadActivites(ProjProject project, DateRange dateRange,
			ProjTimelineActivityRowsFactory activityRowsFactory, Function<ProjActivity, ActivityKey> keyGenerator) {
		ProjActivitySearchParams searchParams = new ProjActivitySearchParams();
		searchParams.setProject(project);
		searchParams.setActions(ProjActivity.TIMELINE_ACTIONS);
		searchParams.setCreatedDateRanges(List.of(dateRange));
		
		List<ProjActivity> activities = projectService.getActivities(searchParams, 0, -1);
		Set<ProjArtefact> artefacts = activities.stream()
				.map(ProjActivity::getArtefact)
				.filter(Objects::nonNull)
				.collect(Collectors.toSet());
		
		ProjArtefactSearchParams artefactSearchParams = new ProjArtefactSearchParams();
		artefactSearchParams.setProject(project);
		artefactSearchParams.setArtefacts(artefacts);
		ProjArtefactItems artefactItems = projectService.getArtefactItems(artefactSearchParams);
		
		Map<Long, Set<Long>> artefactKeyToIdentityKeys = projectService.getArtefactKeyToIdentityKeys(artefacts);
		
		List<ActivityRowData> activityRowDatas = activities
				.stream()
				.collect(Collectors.groupingBy(activity -> keyGenerator.apply(activity)))
				.values()
				.stream()
				.map(activityRowsFactory::createActivityRowData)
				.toList();
		
		List<ProjTimelineRow> rows = new ArrayList<>(activityRowDatas.size());
		for (ActivityRowData activityRowData : activityRowDatas) {
			// ureq == null is ok, because it is only used to create the user portraits
			activityRowsFactory.addActivityRows(null, rows, activityRowData, artefactItems, artefactKeyToIdentityKeys);
		}
		return rows;
	}
	
}
