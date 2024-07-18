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
package org.olat.course.nodes.topicbroker.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.TopicBrokerCourseNode;
import org.olat.course.nodes.topicbroker.TBCourseNodeSecurityCallbackFactory;
import org.olat.course.nodes.topicbroker.TopicBrokerCourseNodeGroupRestrictionCandidates;
import org.olat.course.nodes.topicbroker.TopicBrokerCourseNodeParticipantCandidates;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TBGroupRestrictionCandidates;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.modules.topicbroker.ui.TBCustomFieldDefinitionListController;
import org.olat.modules.topicbroker.ui.TBTopicListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBEditController extends ActivateableTabbableDefaultController {
	
	public static final String PANE_TAB_CONFIG = "pane.tab.config";
	private static final String PANE_TAB_CUSTOM_FIELDS = "pane.tab.custom.fields";
	private static final String PANE_TAB_TOPICS = "pane.tab.topics";
	private final static String[] paneKeys = { PANE_TAB_CONFIG, PANE_TAB_CUSTOM_FIELDS, PANE_TAB_TOPICS };
	
	private TabbedPane tabPane;
	private TBConfigsController configCtrl;
	private TBCustomFieldDefinitionListController customFieldDefinitionsCtrl;
	private TBTopicListController topicsCtrl;
	
	@Autowired
	private TopicBrokerService topicBrokerService;
	
	public TBEditController(UserRequest ureq, WindowControl wControl, ICourse course, TopicBrokerCourseNode courseNode) {
		super(ureq, wControl);
		
		CourseGroupManager courseGroupManager = course.getCourseEnvironment().getCourseGroupManager();
		configCtrl = new TBConfigsController(ureq, wControl, courseGroupManager, courseNode);
		listenTo(configCtrl);
		
		TBBroker broker = topicBrokerService.getOrCreateBroker(getIdentity(),
				course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), courseNode.getIdent());
		customFieldDefinitionsCtrl = new TBCustomFieldDefinitionListController(ureq, getWindowControl(), broker, translate("config.custom.fields.info"));
		listenTo(customFieldDefinitionsCtrl);
		
		TopicBrokerCourseNodeParticipantCandidates participantCandidates = new TopicBrokerCourseNodeParticipantCandidates(
				getIdentity(), courseGroupManager.getCourseEntry(), true);
		TBGroupRestrictionCandidates groupRestrictionCandidates = new TopicBrokerCourseNodeGroupRestrictionCandidates(
				courseGroupManager.getCourseEntry());
		topicsCtrl = new TBTopicListEditController(ureq, wControl, broker,
				TBCourseNodeSecurityCallbackFactory.ADMIN_SEC_CALLBACK, participantCandidates,
				groupRestrictionCandidates);
		listenTo(topicsCtrl);
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return tabPane;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == configCtrl) {
			fireEvent(ureq, event);
		}
	}
	
	@Override
	public void addTabs(TabbedPane tabbedPane) {
		tabPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_CONFIG), configCtrl.getInitialComponent());
		tabbedPane.addTab(translate(PANE_TAB_CUSTOM_FIELDS), customFieldDefinitionsCtrl.getInitialComponent());
		tabbedPane.addTab(translate(PANE_TAB_TOPICS), topicsCtrl.getInitialComponent());
	}

}
