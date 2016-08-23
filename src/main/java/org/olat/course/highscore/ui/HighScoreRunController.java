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

import java.util.ArrayList;
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
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
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
	private boolean viewTable, viewHistogram, viewPodium, viewHighscore;
	private double[] allScores;
	
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
		//TODO initialize
		viewHighscore = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_HIGHSCORE);
		if (!viewHighscore)return;
		viewTable = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_LISTING);
		viewHistogram = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_HISTOGRAM);
		viewPodium = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_PODIUM);
		int bestOnly = (int) config.get(HighScoreEditController.CONFIG_KEY_BESTONLY);
		tableSize = bestOnly != 0 ? (int) config.get(HighScoreEditController.CONFIG_KEY_NUMUSER) : assessEntries.size();
		ownIdentity = ureq.getIdentity();
		initLists();
		
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
			scoreHistogramVC.contextPut("datas", BarSeries.datasToString(allScores));
			scoreHistogramVC.contextPut("cutValue", 
					ownIdIndices.size() > 0 ? allMembers.get(ownIdIndices.get(0)).getScore()	: "");
			
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
				for (HighScoreTableEntry te : allPodium.get(i)) {
					sb.append(te.getName());
					sb.append("</br>");
				}			
				mainVC.contextPut(localizer[i], allPodium.get(i).size() > 0 ? sb.toString() : "") ;
				mainVC.contextPut("score" + (i + 1), allPodium.get(i).size() > 0 ? 
						allPodium.get(i).get(0).getScore() : "");
				if (tableSize > i) {
					DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(),
							allPodium.get(i).get(0).getIdentity(), i == 0, true);
					Component compi = portrait.getInitialComponent();
					mainVC.put("portrait" + (i + 1), compi);
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
			allMembers.subList(tableSize, allMembers.size()).clear();

			tableDataModel = new FlexiTableDataModelImpl<HighScoreTableEntry>(
					new HighScoreFlexiTableModel(allMembers), tableColumnModel);
			FlexiTableElement topTenTable = uifactory.addTableElement(
					getWindowControl(), "table", tableDataModel, getTranslator(), formLayout);
			topTenTable.setNumOfRowsEnabled(false);
			topTenTable.setCustomizeColumns(false);
			topTenTable.setCssDelegate(new MarkedMemberCssDelegate(false));


			if (!ownIdMembers.isEmpty()) {
				tableDataModel2 = new FlexiTableDataModelImpl<HighScoreTableEntry>(
						new HighScoreFlexiTableModel(ownIdMembers),
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

	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}
	
	private class MarkedMemberCssDelegate extends DefaultFlexiTableCssDelegate {
		private boolean mark;
		public MarkedMemberCssDelegate(boolean mark) {
			this.mark = mark;
		}
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return ownIdIndices.get(0) < tableSize && pos == ownIdIndices.get(0) || mark ? "o_row_selected" : null;
		}
	}

	public boolean isViewHighscore() {
		return viewHighscore;
	}
	
	

}

