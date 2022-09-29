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
package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.practice.PracticeResource;
import org.olat.course.nodes.practice.PracticeService;
import org.olat.course.nodes.practice.model.PracticeItem;
import org.olat.course.nodes.practice.model.SearchPracticeItemParameters;
import org.olat.course.nodes.practice.ui.PracticeEditController;
import org.olat.course.nodes.practice.ui.PracticeParticipantController;
import org.olat.course.nodes.practice.ui.PracticeRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeCourseNode extends AbstractAccessableCourseNode implements SelfAssessableCourseNode {

	private static final String PACKAGE_PRACTICE = Util.getPackageName(PracticeParticipantController.class);
	
	public static final String TYPE = "practice";
	
	private static final int CURRENT_CONFIG_VERSION = 1;

	public PracticeCourseNode() {
		super(TYPE);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			return oneClickStatusCache[0];
		}

		List<StatusDescription> statusDescs = validateInternalConfiguration(null);
		if(statusDescs.isEmpty()) {
			statusDescs.add(StatusDescription.NOERROR);
		}
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache[0];
	}
	
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;//delete the cache
		
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_PRACTICE, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			//isConfigValidWithTranslator add first
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration(cev));
		if(sds.isEmpty()) {
			sds.add(StatusDescription.NOERROR);
		}
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}

	private List<StatusDescription> validateInternalConfiguration(CourseEditorEnv cev) {
		List<StatusDescription> sdList = new ArrayList<>(5);
		
		if(cev != null) {
			RepositoryEntry courseEntry = cev.getCourseGroupManager().getCourseEntry();
			NodeAccessType accessType = NodeAccessType.of(courseEntry.getTechnicalType());
			if(LearningPathNodeAccessProvider.TYPE.equals(accessType.getType())) {
				PracticeService practiceService = CoreSpringFactory.getImpl(PracticeService.class);
				LearningPathConfigs configs = CoreSpringFactory.getImpl(LearningPathService.class).getConfigs(this);
				FullyAssessedTrigger trigger = configs.getFullyAssessedTrigger();
				if(trigger == FullyAssessedTrigger.statusDone) {
					List<PracticeResource> resources = practiceService.getResources(courseEntry, getIdent());
					SearchPracticeItemParameters searchParams = SearchPracticeItemParameters.valueOf(null, courseEntry, this);
					Locale locale = CoreSpringFactory.getImpl(I18nManager.class).getCurrentThreadLocale();
					if(locale == null) {
						locale = I18nModule.getDefaultLocale();
					}
					List<PracticeItem> items = practiceService.generateItems(resources, searchParams, -1, locale);
	
					int questionsPerSerie = getModuleConfiguration().getIntegerSafe(PracticeEditController.CONFIG_KEY_QUESTIONS_PER_SERIE, 10);
					if(items.size() < questionsPerSerie) {
						addStatusWarningDescription("warning.practice.num.questions", PracticeEditController.PANE_TAB_CONFIGURATION, sdList);
					}
				}
			}
		}

		return sdList;
	}
	
	private void addStatusWarningDescription(String key, String pane, List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.WARNING, key, key, params, PACKAGE_PRACTICE);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		TabbableController childTabCntrllr = new PracticeEditController(ureq, wControl, course, this);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}
	
	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd,
			VisibilityFilter visibilityFilter) {
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(PracticeCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else {
			controller = new PracticeRunController(ureq, wControl, this, userCourseEnv);
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_practice_icon");
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		// 1) Delete all assessment test sessions (QTI 2.1)
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(AssessmentTestSessionDAO.class).deleteAllUserTestSessionsByCourse(courseEntry, getIdent());
		
		// Delete practice resources
		PracticeService practiceService = CoreSpringFactory.getImpl(PracticeService.class);
		List<PracticeResource> practiceResources = practiceService.getResources(courseEntry, getIdent());
		for(PracticeResource practiceResource:practiceResources) {
			practiceService.deleteResource(practiceResource);
		}
	}
	
	@Override
	public AssessmentEvaluation getAssessmentEvaluation(UserCourseEnvironment userCourseEnv) {
		return null;
	}

	@Override
	public void incrementUserAttempts(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.incrementNodeAttempts(this, mySelf, userCourseEnvironment, by);
	}
	
}
