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
package org.olat.modules.assessment.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.spacesaver.ExpandableController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.AssessedBusinessGroup;
import org.olat.course.assessment.model.AssessedCurriculumElement;
import org.olat.course.assessment.model.AssessmentStatistics;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.event.BusinessGroupEvent;
import org.olat.course.assessment.ui.tool.event.CurriculumElementEvent;
import org.olat.course.assessment.ui.tool.event.SelectionEvents;
import org.olat.modules.assessment.model.AssessmentMembersStatistics;
import org.olat.modules.assessment.ui.AssessmentStatsController.LaunchTableModel.LaunchCols;
import org.olat.modules.assessment.ui.AssessmentStatsController.PassedTableModel.GroupCols;
import org.olat.modules.assessment.ui.component.PassedChart;
import org.olat.modules.assessment.ui.component.PassedChart.PassedPercent;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 19 Jan 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentStatsController extends FormBasicController implements ExpandableController {
	
	private static final String CMD_MEMBERS = "members";
	private static final String CMD_NON_MEMBERS = "non.members";
	private static final String CMD_GROUP = "group";
	private static final String CMD_CURRICULUM_ELEMENT = "curEle";
	
	private FormLink assessedIdentitiesLink;
	private FormLink groupsLink;
	private FormLink curriculumElementLink;
	private FormLink passedLink;
	private FormLink failedLink;
	private FormLink undefinedLink;
	private PassedChart passedChart;
	private LaunchTableModel launchTableModel;
	private FlexiTableElement launchTableEl;
	private PassedTableModel groupTableModel;
	private FlexiTableElement groupTableEl;
	private PassedTableModel curriculumElementTableModel;
	private FlexiTableElement curriculumElementTableEl;

	private final AssessmentToolSecurityCallback assessmentCallback;
	private final SearchAssessedIdentityParams params;
	
	private AssessmentStatistics statistics;
	private AssessmentMembersStatistics memberStatistics;
	private List<AssessedBusinessGroup> businessGroupStatistics;
	private List<AssessedCurriculumElement> curriculumElementStatistics;
	
	@Autowired
	private AssessmentToolManager assessmentToolManager;

	public AssessmentStatsController(UserRequest ureq, WindowControl wControl,
			AssessmentToolSecurityCallback assessmentCallback, SearchAssessedIdentityParams params) {
		super(ureq, wControl, "stats");
		this.assessmentCallback = assessmentCallback;
		this.params = params;
		
		initForm(ureq);
		reload();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		assessedIdentitiesLink = uifactory.addFormLink("assessed.identities", "assessed.identities", null, null, formLayout, Link.NONTRANSLATED);
		assessedIdentitiesLink.setElementCssClass("o_sel_assessment_tool_assessed_users");
		assessedIdentitiesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_user");
		
		groupsLink = uifactory.addFormLink("groups", "groups", null, null, formLayout, Link.NONTRANSLATED);
		groupsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		
		curriculumElementLink = uifactory.addFormLink("curriculum.elements", "curriculum.elements", null, null, formLayout, Link.NONTRANSLATED);
		curriculumElementLink.setIconLeftCSS("o_icon o_icon-fw o_icon_curriculum_element");
		
		passedLink = uifactory.addFormLink("num.passed", "num.passed", null, null, formLayout, Link.NONTRANSLATED);
		passedLink.setIconLeftCSS("o_icon o_icon-fw o_icon_passed o_passed");
		
		failedLink = uifactory.addFormLink("num.failed", "num.failed", null, null, formLayout, Link.NONTRANSLATED);
		failedLink.setIconLeftCSS("o_icon o_icon-fw o_icon_failed o_failed");
		
		undefinedLink = uifactory.addFormLink("num.undefined", "num.undefined", null, null, formLayout, Link.NONTRANSLATED);
		undefinedLink.setIconLeftCSS("o_icon o_icon-fw o_icon_passed_undefined o_noinfo");
		
		passedChart = new PassedChart("passed.chart");
		passedChart.setDomReplacementWrapperRequired(false);
		flc.put("passed.chart", passedChart);
		
		
		// Participants
		FlexiTableColumnModel launchColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		launchColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LaunchCols.assessedIdentities));
		launchColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LaunchCols.launches, new LaunchesCellRenderer()));
		
		launchTableModel = new LaunchTableModel(launchColumnsModel);
		launchTableEl = uifactory.addTableElement(getWindowControl(), "launches", launchTableModel, 20, false, getTranslator(), formLayout);
		launchTableEl.setNumOfRowsEnabled(false);
		launchTableEl.setExportEnabled(false);
		launchTableEl.setCustomizeColumns(false);
		
		
		// Groups
		FlexiTableColumnModel groupColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		groupColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.group));
		groupColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.members));
		groupColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.passed, new PassedStatsRenderer()));
		
		groupTableModel = new PassedTableModel(groupColumnsModel);
		groupTableEl = uifactory.addTableElement(getWindowControl(), "groupList", groupTableModel, 20, false, getTranslator(), formLayout);
		groupTableEl.setElementCssClass("o_group_list");
		groupTableEl.setNumOfRowsEnabled(false);
		groupTableEl.setExportEnabled(false);
		groupTableEl.setCustomizeColumns(false);
		
		
		// Curriculum elements
		FlexiTableColumnModel curriculumElementColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		curriculumElementColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.curriculumElement));
		curriculumElementColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.members));
		curriculumElementColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(GroupCols.passed, new PassedStatsRenderer()));
		
		curriculumElementTableModel = new PassedTableModel(curriculumElementColumnsModel);
		curriculumElementTableEl = uifactory.addTableElement(getWindowControl(), "curriculumElementList", curriculumElementTableModel, 20, false, getTranslator(), formLayout);
		curriculumElementTableEl.setElementCssClass("o_group_list");
		curriculumElementTableEl.setNumOfRowsEnabled(false);
		curriculumElementTableEl.setExportEnabled(false);
		curriculumElementTableEl.setCustomizeColumns(false);
	}
	
	public void reload() {
		statistics = assessmentToolManager.getStatistics(getIdentity(), params);
		memberStatistics = assessmentToolManager.getNumberOfParticipants(getIdentity(), params);
		businessGroupStatistics = assessmentToolManager.getBusinessGroupStatistics(getIdentity(), params);
		curriculumElementStatistics = assessmentToolManager.getCurriculumElementStatistics(getIdentity(), params);
		
		updateUI();
		updateLaunchModel();
		updateBusinessGroupModel();
		updateCurriculumElementModel();
	}

	private void updateUI() {
		int numOfParticipants = memberStatistics.getNumOfParticipants();
		int numOfOtherUsers = memberStatistics.getNumOfOtherUsers();
		String[] args = new String[]{ Integer.toString(numOfParticipants), Integer.toString(numOfOtherUsers) };
		String assessedIdentitiesText = numOfOtherUsers > 0
				? translate("assessment.tool.num.assessed.participants.others", args)
				: translate("assessment.tool.num.assessed.participants", args);
		assessedIdentitiesLink.setI18nKey(assessedIdentitiesText);
		
		if (!businessGroupStatistics.isEmpty()) {
			String groupsLinkText = translate("assessment.tool.num.groups", Integer.toString(businessGroupStatistics.size()));
			groupsLink.setI18nKey(groupsLinkText);
			groupsLink.setVisible(true);
		} else {
			groupsLink.setVisible(false);
		}
		
		if (!curriculumElementStatistics.isEmpty()) {
			String groupsLinkText = translate("assessment.tool.num.curriculum.elements", Integer.toString(curriculumElementStatistics.size()));
			curriculumElementLink.setI18nKey(groupsLinkText);
			curriculumElementLink.setVisible(true);
		} else {
			curriculumElementLink.setVisible(false);
		}
		
		int numOfPassedPercent = statistics.getCountTotal() > 0? Math.round(100 * statistics.getCountPassed() / statistics.getCountTotal()) : 0;
		String numOfPassedText = translate("assessment.tool.num.passed", Integer.toString(statistics.getCountPassed()), Integer.toString(numOfPassedPercent));
		passedLink.setI18nKey(numOfPassedText);
		
		int numOfFailedPercent = statistics.getCountTotal() > 0? Math.round(100 * statistics.getCountFailed() / statistics.getCountTotal()) : 0;
		String numOfFailedText = translate("assessment.tool.num.failed", Integer.toString(statistics.getCountFailed()), Integer.toString(numOfFailedPercent));
		failedLink.setI18nKey(numOfFailedText);
		
		int numOfUndefinedPercent = statistics.getCountTotal() > 0? Math.round(100 * statistics.getCountUndefined() / statistics.getCountTotal()) : 0;
		String numOfUndefinedText = translate("assessment.tool.num.undefined", Integer.toString(statistics.getCountUndefined()), Integer.toString(numOfUndefinedPercent));
		undefinedLink.setI18nKey(numOfUndefinedText);
		
		passedChart.setPassedPercent(new PassedPercent(numOfPassedPercent, numOfFailedPercent));
	}

	private void updateLaunchModel() {
		List<LaunchRow> rows = new ArrayList<>(2);
		
		LaunchRow participantRow = new LaunchRow();
		
		participantRow.setNumIdentities(memberStatistics.getNumOfParticipants());
		participantRow.setNumLaunches(memberStatistics.getNumOfParticipantsLoggedIn());
		
		String participantsLinkText = translate("assessment.tool.num.participants", Integer.toString(memberStatistics.getNumOfParticipants()));
		FormLink participantsLink = uifactory.addFormLink("num.participants", CMD_MEMBERS, null, null, null, Link.NONTRANSLATED);
		participantsLink.setI18nKey(participantsLinkText);
		participantRow.setAssessedIdentitiesLink(participantsLink);
		
		rows.add(participantRow);
		
		if(assessmentCallback.canAssessNonMembers()) {
			LaunchRow otherUsersRow = new LaunchRow();
			
			otherUsersRow.setNumIdentities(memberStatistics.getNumOfOtherUsers());
			otherUsersRow.setNumLaunches(memberStatistics.getOthersLoggedIn());
			
			String otherUsersText = translate("assessment.tool.num.other.users", Integer.toString(memberStatistics.getNumOfOtherUsers()));
			FormLink otherUsersLink = uifactory.addFormLink("num.other.users", CMD_NON_MEMBERS, null, null, null, Link.NONTRANSLATED);
			otherUsersLink.setI18nKey(otherUsersText);
			otherUsersRow.setAssessedIdentitiesLink(otherUsersLink);
			
			rows.add(otherUsersRow);
		}
		
		launchTableModel.setObjects(rows);
		launchTableEl.reset();
	}
	
	
	private void updateBusinessGroupModel() {
		if (businessGroupStatistics.isEmpty()) {
			groupTableEl.setVisible(false);
			return;
		}
		
		groupTableEl.setVisible(true);
		List<PassedRow> rows = new ArrayList<>(businessGroupStatistics.size());
		businessGroupStatistics.sort((g1, g2) -> g1.getName().compareToIgnoreCase(g2.getName()));
		for (AssessedBusinessGroup assessedBusinessGroup : businessGroupStatistics) {
			PassedRow row = new PassedRow();
			
			row.setNumPassed(assessedBusinessGroup.getNumOfPassed());
			row.setNumFailed(assessedBusinessGroup.getNumOfFailed());
			row.setNumUndefined(assessedBusinessGroup.getNumOfUndefined());
			row.setNumIdentities(assessedBusinessGroup.getNumOfParticipants());
			
			FormLink link = uifactory.addFormLink("group_" + assessedBusinessGroup.getKey(), CMD_GROUP, null, null, null, Link.NONTRANSLATED);
			link.setI18nKey(assessedBusinessGroup.getName());
			link.setUserObject(assessedBusinessGroup.getKey());
			row.setGroupLink(link);
			
			rows.add(row);
		}
		
		groupTableModel.setObjects(rows);
		groupTableEl.reset();
	}
	
	private void updateCurriculumElementModel() {
		if (curriculumElementStatistics.isEmpty()) {
			curriculumElementTableEl.setVisible(false);
			return;
		}
		
		curriculumElementTableEl.setVisible(true);
		List<PassedRow> rows = new ArrayList<>(curriculumElementStatistics.size());
		curriculumElementStatistics.sort((g1, g2) -> g1.getName().compareToIgnoreCase(g2.getName()));
		for (AssessedCurriculumElement assessedCurriculumElement : curriculumElementStatistics) {
			PassedRow row = new PassedRow();
			
			row.setNumPassed(assessedCurriculumElement.getNumOfPassed());
			row.setNumFailed(assessedCurriculumElement.getNumOfFailed());
			row.setNumUndefined(assessedCurriculumElement.getNumOfUndefined());
			row.setNumIdentities(assessedCurriculumElement.getNumOfParticipants());
			
			FormLink link = uifactory.addFormLink("ce_" + assessedCurriculumElement.getKey(), CMD_CURRICULUM_ELEMENT, null, null, null, Link.NONTRANSLATED);
			link.setI18nKey(assessedCurriculumElement.getName());
			link.setUserObject(assessedCurriculumElement.getKey());
			row.setGroupLink(link);
			
			rows.add(row);
		}
		
		curriculumElementTableModel.setObjects(rows);
		curriculumElementTableEl.reset();
	}
	
	@Override
	public boolean isExpandable() {
		return true;
	}
	
	@Override
	public void setExpanded(boolean expanded) {
		flc.contextPut("expanded", Boolean.valueOf(expanded));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assessedIdentitiesLink == source) {
			fireEvent(ureq, SelectionEvents.USERS_EVENT);
		} else if (groupsLink == source) {
			List<Long> keys = businessGroupStatistics.stream().map(AssessedBusinessGroup::getKey).collect(Collectors.toList());
			fireEvent(ureq, new BusinessGroupEvent(keys));
		} else if (curriculumElementLink == source) {
			List<Long> keys = curriculumElementStatistics.stream().map(AssessedCurriculumElement::getKey).collect(Collectors.toList());
			fireEvent(ureq, new CurriculumElementEvent(keys));
		} else if(passedLink == source) {
			fireEvent(ureq, SelectionEvents.PASSED_EVENT);
		} else if(failedLink == source) {
			fireEvent(ureq, SelectionEvents.FAILED_EVENT);
		} else if(undefinedLink == source) {
			fireEvent(ureq, SelectionEvents.UNDEFINED_EVENT);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_MEMBERS.equals(link.getCmd())) {
				fireEvent(ureq, SelectionEvents.MEMBERS_EVENT);
			} else if (CMD_NON_MEMBERS.equals(link.getCmd())) {
				fireEvent(ureq, SelectionEvents.NON_MEMBERS_EVENT);
			} else if (CMD_GROUP.equals(link.getCmd())) {
				Long key = (Long)link.getUserObject();
				fireEvent(ureq, new BusinessGroupEvent(Collections.singletonList(key)));
			} else if (CMD_CURRICULUM_ELEMENT.equals(link.getCmd())) {
				Long key = (Long)link.getUserObject();
				fireEvent(ureq, new CurriculumElementEvent(Collections.singletonList(key)));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static class LaunchRow {
		
		private FormLink assessedIdentitiesLink;
		private int numIdentities;
		private int numLaunches;

		public FormLink getAssessedIdentitiesLink() {
			return assessedIdentitiesLink;
		}

		public void setAssessedIdentitiesLink(FormLink assessedIdentitiesLink) {
			this.assessedIdentitiesLink = assessedIdentitiesLink;
		}

		public int getNumIdentities() {
			return numIdentities;
		}

		public void setNumIdentities(int numIdentities) {
			this.numIdentities = numIdentities;
		}

		public int getNumLaunches() {
			return numLaunches;
		}

		public void setNumLaunches(int numLaunches) {
			this.numLaunches = numLaunches;
		}
		
	}
	
	public static class LaunchTableModel extends DefaultFlexiTableDataModel<LaunchRow> {

		public LaunchTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			LaunchRow launchRow = getObject(row);
			if(col >= 0 && col < LaunchCols.values().length) {
				switch(LaunchCols.values()[col]) {
					case assessedIdentities: return launchRow.getAssessedIdentitiesLink();
					case launches: return launchRow;
				}
			}
			return null;
		}
		
		public enum LaunchCols implements FlexiColumnDef {
			assessedIdentities("table.header.num.assessed.identities"),
			launches("table.header.num.launches");
			
			private final String i18nKey;
			
			private LaunchCols(String i18nKey) {
				this.i18nKey = i18nKey;
			}
			
			@Override
			public String i18nHeaderKey() {
				return i18nKey;
			}
		}
	}
	
	public static class LaunchesCellRenderer extends IconCssCellRenderer {

		@Override
		protected String getIconCssClass(Object val) {
			if (val instanceof LaunchRow) {
				LaunchRow row = (LaunchRow)val;
				if (row.getNumLaunches() == 0) {
					return "o_icon o_icon_fw o_red_led";
				} else if (row.getNumLaunches() == row.getNumIdentities()) {
					return "o_icon o_icon_fw o_green_led";
				} else {
					return "o_icon o_icon_fw o_yellow_led";
				}
			}
			return null;
		}

		@Override
		protected String getCellValue(Object val) {
			if (val instanceof LaunchRow) {
				LaunchRow row = (LaunchRow)val;
				return row.getNumLaunches() + " / " + row.getNumIdentities();
			}
			return null;
		}
		
	}
	
	public static class PassedRow {
		
		private FormLink groupLink;
		private int numIdentities;
		private int numPassed;
		private int numFailed;
		private int numUndefined;
		
		public FormLink getGroupLink() {
			return groupLink;
		}
		
		public void setGroupLink(FormLink groupLink) {
			this.groupLink = groupLink;
		}

		public int getNumIdentities() {
			return numIdentities;
		}

		public void setNumIdentities(int numIdentities) {
			this.numIdentities = numIdentities;
		}
		
		public int getNumPassed() {
			return numPassed;
		}
		
		public void setNumPassed(int numPassed) {
			this.numPassed = numPassed;
		}

		public int getNumFailed() {
			return numFailed;
		}

		public void setNumFailed(int numFailed) {
			this.numFailed = numFailed;
		}
		
		public int getNumUndefined() {
			return numUndefined;
		}

		public void setNumUndefined(int numUndefined) {
			this.numUndefined = numUndefined;
		}
		
	}
	
	public static class PassedTableModel extends DefaultFlexiTableDataModel<PassedRow> {

		public PassedTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			PassedRow passedRow = getObject(row);
			if(col >= 0 && col < GroupCols.values().length) {
				switch(GroupCols.values()[col]) {
					case group: return passedRow.getGroupLink();
					case curriculumElement: return passedRow.getGroupLink();
					case members: return passedRow.getNumIdentities();
					case passed: return passedRow;
				}
			}
			return null;
		}
		
		public enum GroupCols implements FlexiColumnDef {
			group("table.header.group"),
			curriculumElement("table.header.curriculum.element"),
			members("table.header.num.members"),
			passed("table.header.passed");
			
			private final String i18nKey;
			
			private GroupCols(String i18nKey) {
				this.i18nKey = i18nKey;
			}
			
			@Override
			public String i18nHeaderKey() {
				return i18nKey;
			}
		}
	}
	
	public class PassedStatsRenderer implements FlexiCellRenderer {
		
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			if(cellValue instanceof PassedRow) {
				PassedRow passedRow = (PassedRow)cellValue;
				if (passedRow.getNumIdentities() > 0) {
					int passedPercent = Math.round(100f * (passedRow.getNumPassed() / (float)passedRow.getNumIdentities()));
					int failedPercent = Math.round(100f * (passedRow.getNumFailed() / (float)passedRow.getNumIdentities()));
					int undefinedPercent = Math.round(100f * (passedRow.getNumUndefined() / (float)passedRow.getNumIdentities()));
					
					String tooltip = translate("passed.tooltip", Integer.toString(passedPercent), Integer.toString(failedPercent),
							Integer.toString(undefinedPercent));
					target.append("<div class='o_passed_progress'>");
					target.append("<div class='progress' title='").append(tooltip).append("'>");
					appendBar(target, passedPercent, "o_passed_progress_bar");
					appendBar(target, failedPercent, "o_failed_progress_bar");
					target.append("</div>");
					target.append(" <div class='o_values'>").append(passedRow.getNumPassed()).append(" / ").append(passedRow.getNumFailed())
							.append(" / ").append(passedRow.getNumUndefined()).append("</div>");
					target.append("</div>");
				}
			}
		}

		private void appendBar(StringOutput sb, int percent, String cssClass) {
			sb.append("<div class='progress-bar ").append(cssClass).append("' style='width:").append(percent).append("%'>");
			sb.append("<div class='sr-only'>").append(percent).append("%</div>");
			sb.append("</div>");
		}
	}

}
