/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.highscore.ui;
/**
 * Initial Date:  10.08.2016 <br>
 * @author fkiefer
 */
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.chart.StatisticsComponent;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.course.highscore.manager.HighScoreManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


public class HighScoreRunController extends FormBasicController{
	
	private FlexiTableDataModel<HighScoreTableEntry> tableDataModel, tableDataModel2;
	private List<HighScoreTableEntry> allMembers, ownIdMembers;
	private List<List<HighScoreTableEntry>> allPodium;
	private List<Integer> ownIdIndices;
	private int tableSize;
	private Identity ownIdentity;
	private boolean viewTable, viewHistogram, viewPodium, viewHighscore, anonymous;
	private double[] allScores;
	private Link[] links = new Link[3];
	private CloseableCalloutWindowController calloutCtr;

	
	@Autowired
	private HighScoreManager highScoreManager;
	@Autowired
	private UserManager userManager;


	public HighScoreRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode) {
		super(ureq, wControl, "highscore");
		
		List<AssessmentEntry>  assessEntries = userCourseEnv.getCourseEnvironment()
				.getAssessmentManager().getAssessmentEntries(courseNode);
		
		ModuleConfiguration config = courseNode.getModuleConfiguration();		
		//initialize ModuleConfig
		Date start = config.getBooleanEntry(HighScoreEditController.CONFIG_KEY_DATESTART) != null ? 
				(Date) config.get(HighScoreEditController.CONFIG_KEY_DATESTART) : null;
		if (start != null && start.getTime() > new Date().getTime())return;		
		
		ownIdentity = ureq.getIdentity();
		viewHighscore = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_HIGHSCORE);
		// do not display highscore if current user has not yet a score
		if (!highScoreManager.hasScore(assessEntries, ownIdentity))viewHighscore = false;
		// do not build form if high-score is not set
		if (!viewHighscore)return;
		
		viewTable = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_LISTING);
		viewHistogram = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_HISTOGRAM);
		viewPodium = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_PODIUM);
		anonymous = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_ANONYMIZE);
		int bestOnly = config.getBooleanEntry(HighScoreEditController.CONFIG_KEY_BESTONLY) != null ? 
				(int) config.get(HighScoreEditController.CONFIG_KEY_BESTONLY) : 0;
		tableSize = bestOnly != 0 ? (int) config.get(HighScoreEditController.CONFIG_KEY_NUMUSER) : assessEntries.size();
		initLists();		
		
		//compute ranking and order
		allScores = highScoreManager.sortRankByScore(assessEntries, allMembers, ownIdMembers,
				 allPodium, ownIdIndices, tableSize, ownIdentity, userManager);
		
		initForm(ureq);
	}
	

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		VelocityContainer mainVC = ((FormLayoutContainer) formLayout).getFormItemComponent();
		mainVC.put("loadd3js", new StatisticsComponent("d3loader"));

		if (viewHistogram) {
			VelocityContainer scoreHistogramVC = createVelocityContainer("histogram_score");
			//transfer all scores to velocity container as base data for histogram
			scoreHistogramVC.contextPut("datas", BarSeries.datasToString(allScores));
			//histogram marker for own position
			scoreHistogramVC.contextPut("cutValue", 
					ownIdIndices.size() > 0 ? allMembers.get(ownIdIndices.get(0)).getScore() : "");
			//find path for ownID image to display in histogram
			UserAvatarMapper mapper = new UserAvatarMapper(false);
			String mapperPath = registerMapper(ureq, mapper);
			String identityMapperPath = mapper.createPathFor(mapperPath, ownIdentity);
			scoreHistogramVC.contextPut("mapperUrl", identityMapperPath);

			mainVC.put("scoreHistogram", scoreHistogramVC);
		}
		if (viewPodium) {			
			String[] localizer = { "first", "second", "third" };
			for (int i = 0; i < localizer.length; i++) {
				StringBuilder sb = new StringBuilder();
				if (allPodium.get(i).size() > 2){
					//create link if podium has more than 2 entries per rank, entries can be displayed as anonymous
					sb.append(anonymous && !allPodium.get(i).get(0).getIdentity().equals(ownIdentity) ? 
							translate("highscore.anonymous") : allPodium.get(i).get(0).getName());

					links[i] = LinkFactory.createLink(null, "link" + (i + 1), "cmd",
							(allPodium.get(i).size() - 1) + " " + translate("highscore.further"), getTranslator(),
							mainVC, this, 16);

				} else {
					for (HighScoreTableEntry te : allPodium.get(i)) {
						sb.append(anonymous && !te.getIdentity().equals(ownIdentity) ? 
								translate("highscore.anonymous") : te.getName());
						sb.append("</br>");
					}							
				}
				mainVC.contextPut(localizer[i], allPodium.get(i).size() > 0 ? sb.toString() : translate("highscore.unavail"));
				mainVC.contextPut("score" + (i + 1), allPodium.get(i).size() > 0 ? 
						allPodium.get(i).get(0).getScore() : "");
				if (allPodium.get(i).size() > 0) {
					//decide whether or not to display id portrait
					Identity currentID = allPodium.get(i).get(0).getIdentity();
					boolean choosePortrait = !anonymous || ownIdentity.equals(currentID);
					DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(),
							currentID, i == 0, choosePortrait, !choosePortrait);
					Component portraitComponent = portrait.getInitialComponent();
					mainVC.put("portrait" + (i + 1), portraitComponent);
				}
			}
		}
		if (viewTable) {
			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			tableColumnModel.addFlexiColumnModel(
					new DefaultFlexiColumnModel("highscore.table.header1", HighScoreTableEntry.RANK));
			tableColumnModel.addFlexiColumnModel(
					new DefaultFlexiColumnModel("highscore.table.header2", HighScoreTableEntry.SCORE));
			tableColumnModel.addFlexiColumnModel(
					new DefaultFlexiColumnModel("highscore.table.header3", HighScoreTableEntry.NAME));
			
			//trim to tableSize
			if (tableSize < allMembers.size())allMembers.subList(tableSize, allMembers.size()).clear();

			tableDataModel = new FlexiTableDataModelImpl<HighScoreTableEntry>(
					new HighScoreFlexiTableModel(allMembers, anonymous, 
							translate("highscore.anonymous"),ownIdentity),
					tableColumnModel);
			FlexiTableElement topTenTable = uifactory.addTableElement(
					getWindowControl(), "table", tableDataModel, getTranslator(), formLayout);
			topTenTable.setNumOfRowsEnabled(false);
			topTenTable.setCustomizeColumns(false);
			topTenTable.setCssDelegate(new MarkedMemberCssDelegate(false));

			//establish a 2nd table if ownID position is greater than first table's size setting
			if (!ownIdMembers.isEmpty()) {
				tableDataModel2 = new FlexiTableDataModelImpl<HighScoreTableEntry>(
						new HighScoreFlexiTableModel(ownIdMembers, anonymous, 
								translate("highscore.anonymous"), ownIdentity),
						tableColumnModel);
				FlexiTableElement tableElement = uifactory.addTableElement(
						getWindowControl(), "table2", tableDataModel2, getTranslator(), formLayout);
				tableElement.setNumOfRowsEnabled(false);
				tableElement.setCustomizeColumns(false);
				tableElement.setCssDelegate(new MarkedMemberCssDelegate(true));
			}
		}

	}
	
	private void initLists(){
		ownIdIndices = new ArrayList<>();
		allMembers = new ArrayList<>();
		ownIdMembers = new ArrayList<>();
		allPodium = new ArrayList<>();
		allPodium.add(new ArrayList<>());
		allPodium.add(new ArrayList<>());
		allPodium.add(new ArrayList<>());
	}
	
	/**
	 * Builds the member list, which is displayed for each rank's link
	 * if the chosen rank has more than 2 entries
	 *
	 * @param persons the persons in i-th rank
	 * @param i the rank of the podium
	 */
	private void buildMemberList(List<String> persons, int i){
		for (HighScoreTableEntry te : allPodium.get(i)) {
			String person = anonymous && !te.getIdentity().equals(ownIdentity) ? 
					translate("highscore.anonymous") : te.getName();
			persons.add(person);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == links[0] || source == links[1] || source == links[2]) {
			List<String> persons = new ArrayList<>();
			Link link;
			if (source == links[0]){
				link = links[0];
				buildMemberList(persons,0);
			} else if (source == links[1]) {
				link = links[1];
				buildMemberList(persons,1);
			} else {
				link = links[2];
				buildMemberList(persons,2);
			}
			if (calloutCtr == null) {
				VelocityContainer podiumcalloutVC = createVelocityContainer("podiumcallout");
				podiumcalloutVC.contextPut("persons", persons);
				calloutCtr = new CloseableCalloutWindowController(ureq, getWindowControl(), podiumcalloutVC, link,
						"This is a title in a callout window", false, null);
				calloutCtr.activate();
				listenTo(calloutCtr);
			} else {
				removeAsListenerAndDispose(calloutCtr);
				calloutCtr = null;
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// only formInnerEvent()		
	}

	@Override
	protected void doDispose() {
		// only formInnerEvent()		
	}
	
	private class MarkedMemberCssDelegate extends DefaultFlexiTableCssDelegate {
		private boolean mark;
		public MarkedMemberCssDelegate(boolean mark) {
			this.mark = mark;
		}
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return ownIdIndices.size() > 0 && (ownIdIndices.get(0) < tableSize && pos == ownIdIndices.get(0)) || mark
					? "o_row_selected" : null;
		}
	}

	public boolean isViewHighscore() {
		return viewHighscore;
	}
	
	

}

