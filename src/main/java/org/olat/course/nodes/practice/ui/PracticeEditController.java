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
package org.olat.course.nodes.practice.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.course.ICourse;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.PracticeCourseNode;

/**
 * 
 * Initial date: 5 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PracticeEditController extends ActivateableTabbableDefaultController {

	public static final String CONFIG_KEY_NUM_LEVELS = "numberOfLevels";
	public static final String CONFIG_KEY_QUESTIONS_PER_SERIE = "questionsPerSerie";
	public static final String CONFIG_KEY_SERIE_PER_CHALLENGE = "seriesPerChallenge";
	/**
	 * Num. of successful chalenges to complete the course element
	 */
	public static final String CONFIG_KEY_NUM_CHALLENGES_FOR_COMPLETION = "numberOfChallengesForCompletion";
	/**
	 * Rules to filter the list of question
	 */
	public static final String CONFIG_KEY_FILTER_RULES = "filterRules";
	/**
	 * List of taxonomy levels to filter
	 */
	public static final String CONFIG_KEY_FILTER_TAXONOMY_LEVELS = "filterTaxonomyLevels";
	
	public static final String CONFIG_KEY_RANK_LIST = "rankListOfParticipants";
	
	
	private TabbedPane myTabbedPane;
	private final String[] paneKeys;
	
	private PracticeConfigurationController configurationCtrl;
	
	public PracticeEditController(UserRequest ureq, WindowControl wControl, ICourse course, PracticeCourseNode courseNode) {
		super(ureq, wControl);
		paneKeys = new String[]{ "pane.tab.config.practice" };
		
		configurationCtrl = new PracticeConfigurationController(ureq, wControl, course, courseNode);
		listenTo(configurationCtrl);
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate("pane.tab.config.practice"), "o_sel_repo_entry", configurationCtrl.getInitialComponent());
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(configurationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		}
	}
}
