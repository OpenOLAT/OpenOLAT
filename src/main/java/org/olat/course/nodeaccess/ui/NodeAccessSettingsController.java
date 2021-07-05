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
package org.olat.course.nodeaccess.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.modules.assessment.model.AssessmentObligation.mandatory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.LearningResourceLoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CompletionType;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.CourseConfigEvent.CourseConfigType;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodes.CollectingVisitor;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeAccessSettingsController extends FormBasicController {
	
	private FormLink migrateLink;
	private SingleSelection completionEvaluationeEl;

	private CloseableModalController cmc;
	private UnsupportedCourseNodesController unsupportedCourseNodesCtrl;
	private DurationConfirmationController durationConfirmationCtrl;
	
	private final boolean readOnly;
	private final RepositoryEntry courseEntry;
	private final CourseConfig courseConfig;

	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private LearningPathService learningPathService;
	
	public NodeAccessSettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, boolean readOnly) {
		super(ureq, wControl);
		this.courseEntry = courseEntry;
		this.readOnly = readOnly;
		ICourse course = CourseFactory.loadCourse(courseEntry);
		this.courseConfig = course.getCourseConfig();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("settings.title");
		setFormContextHelp("Learning path course");
		String nodeAccessTypeName = nodeAccessService.getNodeAccessTypeName(courseConfig.getNodeAccessType(),
				getLocale());
		uifactory.addStaticTextElement("settings.type", nodeAccessTypeName, formLayout);;
		
		if (!LearningPathNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType()) && !readOnly) {
			FormLayoutContainer migrationCont = FormLayoutContainer.createButtonLayout("migrationButtons", getTranslator());
			formLayout.add(migrationCont);
			migrateLink = uifactory.addFormLink("settings.convert", migrationCont, Link.BUTTON);
		}
		
		if (LearningPathNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType())) {
			SelectionValues completionKV = new SelectionValues();
			completionKV.add(entry(CompletionType.numberOfNodes.name(), translate("settings.completion.type.number.of.nodes")));
			completionKV.add(entry(CompletionType.duration.name(), translate("settings.completion.type.duration")));
			completionEvaluationeEl = uifactory.addRadiosVertical("settings.completion.type", formLayout,
					completionKV.keys(), completionKV.values());
			initCompletionTypeFromConfig();
			completionEvaluationeEl.addActionListener(FormEvent.ONCHANGE);
			completionEvaluationeEl.setEnabled(!readOnly);
		}
	}

	private void initCompletionTypeFromConfig() {
		String completionKey = courseConfig.getCompletionType().name();
		if (Arrays.asList(completionEvaluationeEl.getKeys()).contains(completionKey)) {
			completionEvaluationeEl.select(completionKey, true);
		}
		flc.setDirty(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == migrateLink) {
			doMigrate(ureq);
		} else if (source == completionEvaluationeEl) {
			doConfirmCompletionEvaluation(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == unsupportedCourseNodesCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if (source == durationConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				doSetCompletionTypeDuration(durationConfirmationCtrl.getDuration());
			} else if (Event.CANCELLED_EVENT.equals(event)) {
				initCompletionTypeFromConfig();
				CourseFactory.closeCourseEditSession(courseEntry.getOlatResource().getResourceableId(), false);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cmc.deactivate();
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(unsupportedCourseNodesCtrl);
		removeAsListenerAndDispose(durationConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		unsupportedCourseNodesCtrl = null;
		durationConfirmationCtrl = null;
		cmc = null;
	}
	
	private void doMigrate(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		List<CourseNode> unsupportedCourseNodes = learningPathService.getUnsupportedCourseNodes(course);
		if (!unsupportedCourseNodes.isEmpty()) {
			showUnsupportedMessage(ureq, unsupportedCourseNodes);
			return;
		}
		
		RepositoryEntry lpEntry = learningPathService.migrate(courseEntry, getIdentity());
		String bPath = "[RepositoryEntry:" + lpEntry.getKey() + "]";
		NewControllerFactory.getInstance().launch(bPath, ureq, getWindowControl());
	}

	private void showUnsupportedMessage(UserRequest ureq, List<CourseNode> unsupportedCourseNodes) {
		unsupportedCourseNodesCtrl = new UnsupportedCourseNodesController(ureq, getWindowControl(), unsupportedCourseNodes);
		listenTo(unsupportedCourseNodesCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				unsupportedCourseNodesCtrl.getInitialComponent(), true, translate("unsupported.course.nodes.title"));
		cmc.activate();
		listenTo(cmc);
	}

	private void doConfirmCompletionEvaluation(UserRequest ureq) {
		CompletionType completionType = completionEvaluationeEl.isOneSelected()
				? CompletionType.valueOf(completionEvaluationeEl.getSelectedKey())
				: CompletionType.numberOfNodes;
				
		boolean changedToDurationType = CompletionType.duration.equals(completionType)
				&& !CompletionType.duration.equals(courseConfig.getCompletionType());
		
		OLATResourceable courseOres = courseEntry.getOlatResource();
		if(CourseFactory.isCourseEditSessionOpen(courseOres.getResourceableId())) {
			showWarning("error.course.locked");
			initCompletionTypeFromConfig();
			return;
		}
		
		if (changedToDurationType) {
			doConfirmCompletionTypeDuration(ureq);
		} else {
			saveCompletionTypeAndCloseEditSession(completionType);
		}
	}

	private void doConfirmCompletionTypeDuration(UserRequest ureq) {
		durationConfirmationCtrl = new DurationConfirmationController(ureq, getWindowControl());
		listenTo(durationConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				durationConfirmationCtrl.getInitialComponent(), true, translate("settings.completion.type.confirmation.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doSetCompletionTypeDuration(Integer duration) {
		Long resourceId = courseEntry.getOlatResource().getResourceableId();
		if(CourseFactory.isCourseEditSessionOpen(resourceId)) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		ICourse course = CourseFactory.openCourseEditSession(resourceId);
		
		CollectingVisitor editorVisitor = CollectingVisitor.applying(new MissingDurationFunction());
		TreeNode editorRootNode = course.getEditorTreeModel().getRootNode();
		doInitDuration(editorVisitor, editorRootNode, duration);
		
		CollectingVisitor visitor = CollectingVisitor.testing(new MissingDurationPredicate());
		CourseNode runRootNode = course.getRunStructure().getRootNode();
		doInitDuration(visitor, runRootNode, duration);
		
		CourseFactory.saveCourse(courseEntry.getOlatResource().getResourceableId());
		CourseFactory.closeCourseEditSession(resourceId, false);
		
		saveCompletionTypeAndCloseEditSession(CompletionType.duration);
		
		EventBus eventBus = CoordinatorManager.getInstance().getCoordinator().getEventBus();
		CourseConfigEvent courseConfigEvent = new CourseConfigEvent(CourseConfigType.completionType, course.getResourceableId());
		eventBus.fireEventToListenersOf(courseConfigEvent, course);
	}

	private void doInitDuration(CollectingVisitor visitor, INode rootNode, Integer duration) {
		TreeVisitor tv = new TreeVisitor(visitor, rootNode, true);
		tv.visitAll();
		visitor.getCourseNodes().stream()
				.map(courseNode -> learningPathService.getConfigs(courseNode))
				.forEach(config -> config.setDuration(duration));
	}

	private void saveCompletionTypeAndCloseEditSession(CompletionType completionType) {
		Long resourceId = courseEntry.getOlatResource().getResourceableId();
		if(CourseFactory.isCourseEditSessionOpen(resourceId)) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		CourseFactory.openCourseEditSession(resourceId);
		boolean changed = !completionType.equals(courseConfig.getCompletionType());
		if (changed) {
			courseConfig.setCompletionType(completionType);
			logActivity(completionType);
		}
		CourseFactory.setCourseConfig(resourceId, courseConfig);
		CourseFactory.closeCourseEditSession(resourceId, false);
	}

	private void logActivity(CompletionType completionType) {
		ILoggingAction loggingAction = null;
		switch (completionType) {
		case numberOfNodes:
			loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COMPLETION_TYPE_NUMBEr_OF_NODES;
			break;
		case duration: 
			loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COMPLETION_TYPE_DURATION;
			break;
		case none:
			loggingAction = LearningResourceLoggingAction.REPOSITORY_ENTRY_PROPERTIES_COMPLETION_TYPE_NONE;
			break;
		}
		if (loggingAction == null) {
			throw new AssertException("No LoggingAction for CompletionType: " + completionType);
		}
		ThreadLocalUserActivityLogger.log(loggingAction, getClass());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		CourseFactory.closeCourseEditSession(courseEntry.getOlatResource().getResourceableId(), false);
	}
	
	private final class MissingDurationPredicate implements Predicate<CourseNode> {

		@Override
		public boolean test(CourseNode courseNode) {
			LearningPathConfigs configs = learningPathService.getConfigs(courseNode);
			return mandatory.equals(configs.getObligation()) && configs.getDuration() == null;
		}
	}
	
	private final class MissingDurationFunction implements Function<INode, CourseNode> {
		
		private Predicate<CourseNode> missingDuration = new MissingDurationPredicate();
		
		@Override
		public CourseNode apply(INode iNode) {
			if (iNode instanceof CourseEditorTreeNode) {
				CourseEditorTreeNode courseEditorTreeNode = (CourseEditorTreeNode) iNode;
				CourseNode courseNode = courseEditorTreeNode.getCourseNode();
				if (missingDuration.test(courseNode)) {
					return courseNode;
				}
			}
			return null;
		}
		
	}

}
