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
package org.olat.modules.curriculum.ui.lectures;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureRateWarning;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.IdentityRateWarning;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.model.LectureStatisticsSearchParameters;
import org.olat.modules.lecture.ui.coach.LecturesSearchFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Report absences of the curriculum element and its children.
 * List of participants -> select a participant -> list of courses -> select a course -> list of lectures
 * 
 * Initial date: 13 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementLecturesController extends BasicController {
	
	private static final String PROPS_IDENTIFIER = LecturesSearchFormController.PROPS_IDENTIFIER;
	
	private LecturesListController lecturesListCtlr;

	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumService curriculumService;
	
	/**
	 * 
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param breadcrumbPanel A breadcrumb panel (mandatory)
	 * @param element The curriculum element
	 * @param withDescendants Show the lectures of the specified curriculum and all its descendants (or not)
	 * @param secCallback The security callback
	 */
	public CurriculumElementLecturesController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel breadcrumbPanel,
			Curriculum curriculum, CurriculumElement element, boolean withDescendants, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));

		Roles roles = ureq.getUserSession().getRoles();
		boolean adminProps = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(PROPS_IDENTIFIER, adminProps);

		VelocityContainer mainVC = createVelocityContainer("curriculum_lectures");
		
		boolean all = secCallback.canViewAllLectures()
				|| (lectureModule.isOwnerCanViewAllCoursesInCurriculum() && secCallback.canEditCurriculumElement(element));
		
		Identity checkByIdentity = all ? null : getIdentity();
		List<RepositoryEntry> entries;
		if(element != null) {
			entries = curriculumService
					.getRepositoryEntriesWithLectures(element, checkByIdentity, withDescendants);
		} else {
			entries = curriculumService
					.getRepositoryEntriesWithLectures(curriculum, checkByIdentity);
		}
		
		LectureStatisticsSearchParameters params = new LectureStatisticsSearchParameters();
		params.setEntries(entries);
		List<LectureBlockIdentityStatistics> rawStatistics = lectureService
				.getLecturesStatistics(params, userPropertyHandlers, getIdentity());
		List<LectureBlockIdentityStatistics> aggregatedStatistics = lectureService.groupByIdentity(rawStatistics);
		calculateWarningRates(rawStatistics, aggregatedStatistics);
		
		List<RepositoryEntryRef> filterByEntry = new ArrayList<>(entries);

		if(filterByEntry.isEmpty()) {
			mainVC.contextPut("hasLectures", Boolean.FALSE);
		} else {
			lecturesListCtlr = new LecturesListController(ureq, getWindowControl(), breadcrumbPanel,
					aggregatedStatistics, filterByEntry, curriculum, element, userPropertyHandlers, PROPS_IDENTIFIER);
			listenTo(lecturesListCtlr);
			mainVC.contextPut("hasLectures", Boolean.TRUE);
			mainVC.put("lectures", lecturesListCtlr.getInitialComponent());
		}
		
		if(curriculum != null) {
			mainVC.contextPut("curriculumName", curriculum.getDisplayName());
			mainVC.contextPut("curriculumIdentifier", curriculum.getIdentifier());
		}
		
		if(element != null) {
			mainVC.contextPut("elementName", element.getDisplayName());
			mainVC.contextPut("elementIdentifier", element.getIdentifier());
			Formatter formatter = Formatter.getInstance(getLocale());
			if(element.getBeginDate() != null) {
				mainVC.contextPut("elementBegin", formatter.formatDate(element.getBeginDate()));
			}
			if(element.getEndDate() != null) {
				mainVC.contextPut("elementEnd", formatter.formatDate(element.getEndDate()));
			}
			
			List<CurriculumElement> parentLine = curriculumService.getCurriculumElementParentLine(element);
			parentLine.remove(element);
			mainVC.contextPut("parentLine", parentLine);
		}

		putInitialPanel(mainVC);
	}
	
	private void calculateWarningRates(List<LectureBlockIdentityStatistics> rawStatistics, List<LectureBlockIdentityStatistics> aggregatedStatistics) {
		List<IdentityRateWarning> warnings = lectureService.groupRateWarning(rawStatistics);
		if(warnings.isEmpty()) return;

		Map<Long,IdentityRateWarning> warningMap = warnings.stream()
				.collect(Collectors.toMap(IdentityRateWarning::getIdentityKey, w -> w, (u, v) -> u));
		for(LectureBlockIdentityStatistics aggregatedStatistic:aggregatedStatistics) {
			IdentityRateWarning warning = warningMap.get(aggregatedStatistic.getIdentityKey());
			if(warning != null && (warning.getWarning() == LectureRateWarning.warning || warning.getWarning() == LectureRateWarning.error)) {
				aggregatedStatistic.setExplicitWarning(warning.getWarning());
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
