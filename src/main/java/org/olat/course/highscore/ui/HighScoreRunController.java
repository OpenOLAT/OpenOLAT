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
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
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
import org.olat.core.util.prefs.Preferences;
import org.olat.course.highscore.manager.HighScoreManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;


public class HighScoreRunController extends FormBasicController{
	
	private static final String GUIPREF_KEY_HIGHSCORE = "highscore";
	
	private FlexiTableDataModel<HighScoreTableEntry> tableDataModel, tableDataModel2;
	private List<HighScoreTableEntry> allMembers, ownIdMembers;
	private List<List<HighScoreTableEntry>> allPodium;
	private List<Integer> ownIdIndices;
	private int tableSize;
	private Identity ownIdentity;
	private boolean viewTable, viewPosition, viewHistogram, viewPodium, viewHighscore, anonymous;
	private double[] allScores;
	private Link[] links = new Link[3];
	private CloseableCalloutWindowController calloutCtr;
	private Float lowerBorder, upperBorder;
	
	private String nodeID;
	
	@Autowired
	private HighScoreManager highScoreManager;
	@Autowired
	private UserManager userManager;
	
	/**
	 * Instantiates a new high score run controller. 
	 * Use this controller in combination with FormBasicController
	 */
	public HighScoreRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode, Form mainForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "highscore", mainForm);
		this.nodeID = courseNode.getIdent();
		
		setupContent(ureq, wControl, userCourseEnv, courseNode);
	}

	/**
	 * Instantiates a new high score run controller. 
	 * Use this controller in combination with BasicController and DefaultController
	 */
	public HighScoreRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode) {
		super(ureq, wControl, "highscore");
		this.nodeID = courseNode.getIdent();
		
		setupContent(ureq, wControl, userCourseEnv, courseNode);
	}
	
	private void setupContent(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNode courseNode) {
		
		ownIdentity = userCourseEnv.getIdentityEnvironment().getIdentity();
		AssessmentEntry ownEntry = userCourseEnv.getCourseEnvironment().getAssessmentManager().getAssessmentEntry(courseNode, ownIdentity);
		boolean adminORcoach = userCourseEnv.isAdmin() || userCourseEnv.isCoach();

		// guests will never see the highscore
		if (ureq.getUserSession().getRoles().isGuestOnly()){
			viewHighscore = false;
			return;			
		}
		
		// coaches or admin may see highscore, user only if already scored
		if (!adminORcoach && (ownEntry == null || (ownEntry != null && ownEntry.getScore() == null))) {
			viewHighscore = false;
			return;		
		}	
		
		List<AssessmentEntry>  assessEntries = userCourseEnv.getCourseEnvironment()
				.getAssessmentManager().getAssessmentEntries(courseNode);
		// display only if has content
		if (assessEntries.isEmpty()) {
			viewHighscore = false;
			return;		
		}
		
		//initialize ModuleConfiguration
		ModuleConfiguration config = courseNode.getModuleConfiguration();		

		viewHighscore = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_HIGHSCORE);
		// do not build form if high-score is not set
		if (!viewHighscore){
			return;			
		}
		Date start = config.getBooleanEntry(HighScoreEditController.CONFIG_KEY_DATESTART) != null ? 
				(Date) config.get(HighScoreEditController.CONFIG_KEY_DATESTART) : null;
		// display only if start time has been met		
		if (start != null && start.after(new Date())){
			viewHighscore = false;
			return;		
		}
		
		viewTable = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_LISTING);
		viewHistogram = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_HISTOGRAM);
		viewPosition = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_POSITION);
		viewPodium = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_PODIUM);
		anonymous = config.getBooleanSafe(HighScoreEditController.CONFIG_KEY_ANONYMIZE);
		int bestOnly = config.getBooleanEntry(HighScoreEditController.CONFIG_KEY_BESTONLY) != null ? 
				(int) config.get(HighScoreEditController.CONFIG_KEY_BESTONLY) : 0;
		tableSize = bestOnly != 0 ? (int) config.get(HighScoreEditController.CONFIG_KEY_NUMUSER) : assessEntries.size();
		initLists();
		// get borders 
		lowerBorder = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
		upperBorder = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		
		// compute ranking and order
		allScores = highScoreManager.sortRankByScore(assessEntries, allMembers, ownIdMembers,
				 allPodium, ownIdIndices, tableSize, ownIdentity, userManager);

		// init showConfig from user Prefs
		doLoadShowConfig(ureq);
		
		initForm(ureq);
	}
	

	/**
	 * loads GUI preferences 
	 */
	private void doLoadShowConfig(UserRequest ureq) {
		// add as listener to form layout for later dispatchinf of gui prefs changes
		this.flc.getFormItemComponent().addListener(this);
		// init showConfig from user prefs
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(HighScoreRunController.class, GUIPREF_KEY_HIGHSCORE + nodeID);
		if (showConfig  == null) {
			showConfig = Boolean.TRUE;
		}
		// expose initial value to velocity
		this.flc.contextPut("showConfig", Boolean.valueOf(showConfig));
	}
	
	private void doUpdateShowConfig(UserRequest ureq, boolean newValue) {
		// save new config in GUI prefs
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(HighScoreRunController.class, GUIPREF_KEY_HIGHSCORE + nodeID, Boolean.valueOf(newValue));
		}
	}
	

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		VelocityContainer mainVC = this.flc.getFormItemComponent();
		mainVC.put("loadd3js", new StatisticsComponent("d3loader"));
		
		if (viewHistogram) {
			VelocityContainer scoreHistogramVC = createVelocityContainer("histogram_score");
			//transfer all scores to velocity container as base data for histogram
			allScores = highScoreManager.processHistogramData(allScores, lowerBorder, upperBorder);
			scoreHistogramVC.contextPut("datas", BarSeries.datasToString(allScores));
			//histogram marker for own position
			scoreHistogramVC.contextPut("cutValue", ownIdIndices.size() > 0
					? highScoreManager.calculateHistogramCutvalue(allMembers.get(ownIdIndices.get(0)).getScore())
					: -1000);
			//classwidth to correctly display the histogram
			long classwidth = highScoreManager.getClasswidth();
			scoreHistogramVC.contextPut("step", classwidth);
			//find path for ownID image to display in histogram
			UserAvatarMapper mapper = new UserAvatarMapper(false);
			String mapperPath = registerMapper(ureq, mapper);
			String identityMapperPath = mapper.createPathFor(mapperPath, ownIdentity);
			scoreHistogramVC.contextPut("mapperUrl", identityMapperPath);

			mainVC.put("scoreHistogram", scoreHistogramVC);
		}
		if (viewPodium) {			
			String[] localizer = { "first", "second", "third" };
			// for clarity and space reasons do not show any portraits if one position has more than 6 persons 
			int maxPerson = 6;
			boolean showPortraits = !anonymous && allPodium.get(0).size() <= maxPerson 
					&& allPodium.get(1).size() <= maxPerson && allPodium.get(2).size() <= maxPerson;
			for (int i = 0; i < localizer.length; i++) {
				int sizePerPos = allPodium.get(i).size();
				StringBuilder sb = new StringBuilder();
				if (sizePerPos > 2){
					int reduce = 0;
					//create link if podium has more than 2 entries per rank, entries can be displayed as anonymous
					if (allPodium.get(i).get(0).getIdentity().equals(ownIdentity)) {
						sb.append(userManager.getUserDisplayName(ownIdentity));
						++reduce;
					}

					if (sizePerPos > 6 || anonymous) {
						mainVC.contextPut("further" + (i + 1), (sizePerPos - reduce) + " "
								+ (reduce == 1 ? translate("highscore.further") : translate("highscore.total")));
					} else {
						links[i] = LinkFactory.createLink(null, "link" + (i + 1), "cmd",
								(sizePerPos - reduce) + " "
										+ (reduce == 1 ? translate("highscore.further") : translate("highscore.total")),
								getTranslator(), mainVC, this, 16);
					}
				} else {
					for (HighScoreTableEntry te : allPodium.get(i)) {
						if (!anonymous || te.getIdentity().equals(ownIdentity)) {
							sb.append(userManager.getUserDisplayName(te.getIdentity()));
							sb.append("<br/>");
						}						
					}							
				}
				mainVC.contextPut(localizer[i], sizePerPos > 0 ? sb.toString() : translate("highscore.unavail"));
				mainVC.contextPut("score" + (i + 1), sizePerPos > 0 ? 
						allPodium.get(i).get(0).getScore() : null);
				if (sizePerPos > 0 && showPortraits) {
					//decide whether or not to display id portrait
					mainVC.contextPut("number"+ (i + 1), sizePerPos);
					for (int j = 0; j < sizePerPos; j++) {
						Identity currentID = allPodium.get(i).get(j).getIdentity();
						boolean choosePortrait = !anonymous || ownIdentity.equals(currentID);
						DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(),
								currentID, false, choosePortrait, !choosePortrait);
						Component portraitComponent = portrait.getInitialComponent();
						mainVC.put("portrait" + (i + 1) + "-" + (j + 1), portraitComponent);
					}
				} else {
					// if amount of people per rank is too large, own id portrait can still be displayed
					for (int j = 0; j < sizePerPos; j++) {
						Identity currentID = allPodium.get(i).get(j).getIdentity();
						if (ownIdentity.equals(currentID)) {
							DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(),
									currentID, true, true, false);
							mainVC.put("portrait" + (i + 1), portrait.getInitialComponent());
						}
					}
				}
			}
		}
		if (viewTable) {
			FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("highscore.table.header1", HighScoreTableEntry.RANK));
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("highscore.table.header2", HighScoreTableEntry.SCORE));
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("highscore.table.header3", HighScoreTableEntry.NAME));
			
			//trim to tableSize
			if (tableSize < allMembers.size()) {
				allMembers.subList(tableSize, allMembers.size()).clear();
			}

			tableDataModel = new FlexiTableDataModelImpl<HighScoreTableEntry>(new HighScoreFlexiTableModel(allMembers, anonymous, 
					translate("highscore.anonymous"),ownIdentity), tableColumnModel);
			FlexiTableElement topTenTable = uifactory.addTableElement(getWindowControl(), "table", tableDataModel, 
					getTranslator(), formLayout);
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
		if (viewPosition && ownIdIndices.size() > 0) {
			mainVC.contextPut("position", translate("highscore.position.info",
					new String[] { String.valueOf(highScoreManager.getOwnTableEntry().getRank()),
							String.valueOf(allScores.length - ownIdIndices.get(0))}));
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
		int counter = 0;
		for (HighScoreTableEntry te : allPodium.get(i)) {
			if (!te.getIdentity().equals(ownIdentity)) {
				String person = te.getName();
				persons.add(person);
			}
			if(counter++ == 6) {
				persons.add(translate("highscore.further"));
				break;
			}
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
		} else if (source == this.flc.getFormItemComponent()) {
			if("show".equals(event.getCommand())) {
				doUpdateShowConfig(ureq, true);
			} else if("hide".equals(event.getCommand())) {
				doUpdateShowConfig(ureq, false);
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

