/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.NodeRightTypeBuilder;
import org.olat.course.nodes.topicbroker.TopicBrokerCourseNodeService;
import org.olat.course.nodes.topicbroker.ui.TBConfigController;
import org.olat.course.nodes.topicbroker.ui.TBConfigsController;
import org.olat.course.nodes.topicbroker.ui.TBEditController;
import org.olat.course.nodes.topicbroker.ui.TBRunCoachController;
import org.olat.course.nodes.topicbroker.ui.TBRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExportLinkEnum;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * 
 * Initial date: 27 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TopicBrokerCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = 2135898475307987572L;
	
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(TBRunController.class);
	private static final String FILENAME_CUSTOM_FIELD_DEFINITIONS = "customfielddefinitions.xml";
	
	public static final String TYPE = "topicbroker";
	public static final String ICON_CSS = "o_icon_topicbroker";
	
	private static final int CURRENT_VERSION = 1;
	public static final String CONFIG_KEY_ENROLLMENTS_PER_PARTICIPANT = "enrollments.per.participant";
	public static final String CONFIG_KEY_SELECTIONS_PER_PARTICIPANT = "selections.per.participant";
	public static final String CONFIG_KEY_PARTICIPANT_CAN_REDUCE_ENROLLMENTS = "participant.can.reduce.enrollments";
	public static final String CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW = "participant.can.withdraw";
	public static final String CONFIG_KEY_RELATIVE_DATES = "relative.dates";
	public static final String CONFIG_KEY_SELECTION_START = "selection.start";
	public static final String CONFIG_KEY_SELECTION_START_RELATIVE = "selection.start.relative";
	public static final String CONFIG_KEY_SELECTION_START_RELATIVE_TO = "selection.start.relative.to";
	public static final String CONFIG_KEY_SELECTION_DURATION = "selection.duration";
	public static final String CONFIG_KEY_SELECTION_END = "selection.end";
	public static final String CONFIG_KEY_ENROLLMENT_AUTO = "enrollment.auto";
	// End date only for participants.
	public static final String CONFIG_KEY_WITHDRAW_END = "withdraw.end";
	public static final String CONFIG_KEY_WITHDRAW_END_RELATIVE = "withdraw.end.relative";
	
	public static final NodeRightType EDIT_TOPIC = NodeRightTypeBuilder.ofIdentifier("editTopic")
			.setLabel(TBConfigsController.class, "config.rights.edit.topic")
			.addRole(NodeRightRole.coach, false)
			.build();
	public static final NodeRightType EDIT_SELECTIONS = NodeRightTypeBuilder.ofIdentifier("editSelections")
			.setLabel(TBConfigsController.class, "config.rights.edit.selections")
			.addRole(NodeRightRole.coach, false)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = List.of(EDIT_TOPIC, EDIT_SELECTIONS);

	public TopicBrokerCourseNode() {
		super(TYPE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		TBEditController childTabCtrl = new TBEditController(ureq, wControl, course, this);
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode,
				userCourseEnv, childTabCtrl);
		nodeEditCtr.addControllerListener(childTabCtrl);
		return nodeEditCtr;
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
			controller = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
		} else if (userCourseEnv.isParticipant() && userCourseEnv.isMemberParticipant()) {
			controller = new TBRunController(ureq, wControl, this, userCourseEnv);
		} else if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			controller = new TBRunCoachController(ureq, wControl, this, userCourseEnv);
		} else {
			Translator trans = Util.createPackageTranslator(TBRunController.class, ureq.getLocale());
			String title = trans.translate("error.not.member.title");
			String message = trans.translate("error.not.member.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		}
		
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, ICON_CSS);
		return new NodeRunConstructionResult(ctrl);
	}

	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}
		
		StatusDescription sd = StatusDescription.NOERROR;
		DueDateConfig dueDateConfig = getDueDateConfig(CONFIG_KEY_SELECTION_START);
		if (!DueDateConfig.isDueDate(dueDateConfig)) {
			String shortKey = "error.no.selection.period.short";
			String longKey = "error.no.selection.period.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(TBConfigController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			sd.setActivateableViewIdentifier(TBEditController.PANE_TAB_CONFIG);
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE,
				getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
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
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);
		
		ModuleConfiguration config = getModuleConfiguration();
		
		if (isNewNode) {
			config.setStringValue(CONFIG_KEY_ENROLLMENTS_PER_PARTICIPANT, "1");
			config.setStringValue(CONFIG_KEY_SELECTIONS_PER_PARTICIPANT, "3");
			config.setBooleanEntry(CONFIG_KEY_PARTICIPANT_CAN_REDUCE_ENROLLMENTS, false);
			config.setBooleanEntry(CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW, false);
			config.setBooleanEntry(CONFIG_KEY_ENROLLMENT_AUTO, true);
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			nodeRightService.initDefaults(config, NODE_RIGHT_TYPES);
		}
		// Configs are synchronized with TBBroker. If you add new configs, you probably
		// have to upgrade the TBBroker with an Upgrader because the publish process is
		// not run.
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	@Override
	public List<Entry<String, DueDateConfig>> getNodeSpecificDatesWithLabel() {
		return List.of(
				Map.entry("topic.broker.selection.period.start", getDueDateConfig(CONFIG_KEY_SELECTION_START)),
				Map.entry("topic.broker.selection.period.end", getDueDateConfig(CONFIG_KEY_SELECTION_END)),
				Map.entry("topic.broker.withdraw.period.end", getDueDateConfig(CONFIG_KEY_WITHDRAW_END))
			);
	}
	
	@Override
	public DueDateConfig getDueDateConfig(String key) {
		if (CONFIG_KEY_SELECTION_START.equals(key)) {
			return DueDateConfig.ofCourseNode(this, CONFIG_KEY_RELATIVE_DATES, CONFIG_KEY_SELECTION_START,
					CONFIG_KEY_SELECTION_START_RELATIVE, CONFIG_KEY_SELECTION_START_RELATIVE_TO);
		}
		if (CONFIG_KEY_SELECTION_END.equals(key)) {
			return DueDateConfig.absolute(getModuleConfiguration().getDateValue(CONFIG_KEY_SELECTION_END));
		}
		if (CONFIG_KEY_WITHDRAW_END.equals(key)) {
			return DueDateConfig.absolute(getModuleConfiguration().getDateValue(CONFIG_KEY_WITHDRAW_END));
		}
		return super.getDueDateConfig(key);
	}

	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		TopicBrokerCourseNodeService topicBrokerCourseNodeService = CoreSpringFactory.getImpl(TopicBrokerCourseNodeService.class);
		topicBrokerCourseNodeService.synchBroker(publisher, course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this);
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		TopicBrokerCourseNodeService topicBrokerCourseNodeService = CoreSpringFactory.getImpl(TopicBrokerCourseNodeService.class);
		topicBrokerCourseNodeService.deleteBroker(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this);
		super.cleanupOnDelete(course);
	}
	
	@Override
	public void exportNode(File fExportDirectory, ICourse course, RepositoryEntryImportExportLinkEnum withReferences) {
		TopicBrokerCourseNodeService topicBrokerCourseNodeService = CoreSpringFactory.getImpl(TopicBrokerCourseNodeService.class);
		String customFieldDefinitionsXml = topicBrokerCourseNodeService.getCustomFieldDefinitionExportXml(
				course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), this.getIdent());
		if (!StringHelper.containsNonWhitespace(customFieldDefinitionsXml)) {
			return;
		}
		
		File nodeDir = new File(fExportDirectory, getIdent());
		nodeDir.mkdirs();
		File definitionsFile = new File(nodeDir, FILENAME_CUSTOM_FIELD_DEFINITIONS);
		FileUtils.save(definitionsFile, customFieldDefinitionsXml, "utf-8");
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation,
			Locale locale, RepositoryEntryImportExportLinkEnum withReferences) {
		File nodeDir = new File(importDirectory, getIdent());
		if (!nodeDir.exists()) {
			return;
		}
		File definitionsFile = new File(nodeDir, FILENAME_CUSTOM_FIELD_DEFINITIONS);
		if (!definitionsFile.exists()) {
			return;
		}
		
		String customFieldDefinitionsXml = FileUtils.load(definitionsFile, "utf-8");
		postCopyImport(owner, course, this.getIdent(), customFieldDefinitionsXml);
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CourseNode copy = super.createInstanceForCopy(isNewTitle, course, author);
		postCopy(author, course, this.getIdent(), course, copy.getIdent());
		return copy;
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);
		// create custom fields only once
		if (Processing.editor == processType) {
			postCopy(envMapper.getAuthor(), sourceCourse, this.getIdent(), course, this.getIdent());
			
		}
		if (context != null) {
			ModuleConfiguration config = getModuleConfiguration();
			long dateDifference = context.getDateDifference(getIdent());
			
			Date selectionPeriodStart = config.getDateValue(CONFIG_KEY_SELECTION_START);
			if (selectionPeriodStart != null) {
				selectionPeriodStart.setTime(selectionPeriodStart.getTime() + dateDifference);
				config.setDateValue(CONFIG_KEY_SELECTION_START, selectionPeriodStart);
			}
			Date selectionPeriodEnd = config.getDateValue(CONFIG_KEY_SELECTION_END);
			if (selectionPeriodEnd != null) {
				selectionPeriodEnd.setTime(selectionPeriodEnd.getTime() + dateDifference);
				config.setDateValue(CONFIG_KEY_SELECTION_END, selectionPeriodEnd);
			}
			Date withdrawPeriodEnd = config.getDateValue(CONFIG_KEY_WITHDRAW_END);
			if (withdrawPeriodEnd != null) {
				withdrawPeriodEnd.setTime(withdrawPeriodEnd.getTime() + dateDifference);
				config.setDateValue(CONFIG_KEY_WITHDRAW_END, withdrawPeriodEnd);
			}
		}
	}
	
	@Override // Import course elements wizard
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		postCopy(envMapper.getAuthor(), sourceCourse, sourceCourseNode.getIdent(), course, this.getIdent());
	}

	private void postCopy(Identity author, ICourse sourceCourse, String sourceCourseNodeIdent, ICourse targetCourse, String targetCourseNodeIdent) {
		TopicBrokerCourseNodeService topicBrokerCourseNodeService = CoreSpringFactory.getImpl(TopicBrokerCourseNodeService.class);
		String customFieldDefinitionsXml = topicBrokerCourseNodeService.getCustomFieldDefinitionExportXml(
				sourceCourse.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), sourceCourseNodeIdent);
		if (!StringHelper.containsNonWhitespace(customFieldDefinitionsXml)) {
			return;
		}
		
		postCopyImport(author, targetCourse, targetCourseNodeIdent, customFieldDefinitionsXml);
	}

	private void postCopyImport(Identity doer, ICourse course, String courseNodeIdent, String customFieldDefinitionsXml) {
		TopicBrokerCourseNodeService topicBrokerCourseNodeService = CoreSpringFactory.getImpl(TopicBrokerCourseNodeService.class);
		topicBrokerCourseNodeService.createCustomFieldDefinitions(doer,
				course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNodeIdent,
				customFieldDefinitionsXml);
		
	}

}
