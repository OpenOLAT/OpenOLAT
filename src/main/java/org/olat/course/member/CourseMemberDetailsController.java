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
package org.olat.course.member;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.member.manager.OriginQueries;
import org.olat.course.member.model.OriginCoursePlannerRow;
import org.olat.course.member.model.OriginCourseRow;
import org.olat.course.member.model.OriginGroupRow;
import org.olat.group.ui.main.AbstractMemberListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserPortraitService;
import org.olat.user.UserPropertiesInfoController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2025-12-17<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CourseMemberDetailsController extends FormBasicController {

	public static final Event EDIT_EVENT = new Event("edit");
	
	private Object userObject;
	private final Identity identity;
	private final RepositoryEntry repoEntry;
	private final UserCourseInformations courseInfos;
	private FormLink editMembershipButton;
	private OriginCourseTableModel originCourseTableModel;
	private FlexiTableElement originCourseTable;
	private OriginGroupTableModel originGroupTableModel;
	private FlexiTableElement originGroupTable;
	private OriginCoursePlannerTableModel originCoursePlannerTableModel;
	private FlexiTableElement originCoursePlannerTable;
	
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private UserCourseInformationsManager userCourseInfosMgr;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private OriginQueries originQueries;

	public CourseMemberDetailsController(UserRequest ureq, WindowControl wControl, Form rootForm,
										 Identity identity, Long repoEntryKey) {
		super(ureq, wControl, LAYOUT_CUSTOM, "member_details_view", rootForm);
		setTranslator(Util.createPackageTranslator(AbstractMemberListController.class, getLocale(), getTranslator()));
		
		this.identity = identity;
		this.repoEntry = repositoryService.loadByKey(repoEntryKey);
		this.courseInfos = userCourseInfosMgr.getUserCourseInformations(repoEntry.getOlatResource(), identity);
		initForm(ureq);
		reloadModel();
	}

	public Object getUserObject() {
		return userObject;
	}

	public void setUserObject(Object userObject) {
		this.userObject = userObject;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initPortrait(formLayout, ureq);
		initEditMembership(formLayout);
		initOriginCourseTable(formLayout);
		initOriginGroupTable(formLayout);
		initOriginCoursePlannerTable(formLayout);
	}

	private void initPortrait(FormItemContainer formLayout, UserRequest ureq) {
		Formatter formatter = Formatter.getInstance(getLocale());
		UserPropertiesInfoController.Builder lvBuilder = UserPropertiesInfoController.LabelValues.builder();
		
		Roles roles = securityManager.getRoles(identity);
		String typeKey;
		if (roles.isInvitee()) {
			typeKey = "filter.user.type.invitee";
		} else if (roles.isGuestOnly()) {
			typeKey = "filter.user.type.guest";
		} else {
			typeKey = "filter.user.type.registered";
		}
		lvBuilder.add(translate("filter.user.type"), translate(typeKey));

		if (courseInfos != null) {
			String addedDate = formatter.formatDate(courseInfos.getInitialLaunch());
			lvBuilder.add(translate("course.membership.creation"), addedDate);
		}

		if (securityModule.isUserLastVisitVisible(ureq.getUserSession().getRoles())) {
			if (courseInfos != null) {
				String lastVisit = "";
				String numOfVisits = "0";
				if (courseInfos.getRecentLaunch() != null) {
					lastVisit = formatter.formatDate(courseInfos.getRecentLaunch());
				}
				if (courseInfos.getVisit() >= 0) {
					numOfVisits = Integer.toString(courseInfos.getVisit());
				}
				lvBuilder.add(translate("course.lastTime"), lastVisit);
				lvBuilder.add(translate("course.numOfVisits"), numOfVisits);
			}
		}

		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		String userPropsId = MemberListController.class.getCanonicalName();
		FormBasicController profile = new UserPropertiesInfoController(ureq, getWindowControl(),
				mainForm, identity, userPropsId, lvBuilder.build(), profileConfig);
		listenTo(profile);
		formLayout.add("portrait", profile.getInitialFormItem());
	}

	private void initEditMembership(FormItemContainer formLayout) {
		editMembershipButton = uifactory.addFormLink("edit.member", formLayout, Link.BUTTON);
		editMembershipButton.setIconLeftCSS("o_icon o_icon_edit");
	}
	
	private void initOriginCourseTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		OriginRoleCellRenderer originRoleCellRenderer = new OriginRoleCellRenderer(getLocale(), false);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginCourseTableModel.Cols.role, originRoleCellRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginCourseTableModel.Cols.created));
		originCourseTableModel = new OriginCourseTableModel(columnsModel);
		originCourseTable = uifactory.addTableElement(getWindowControl(), "originCourseTable", originCourseTableModel, 
				20, false, getTranslator(), formLayout);
		originCourseTable.setCustomizeColumns(false);
		originCourseTable.setNumOfRowsEnabled(false);
	}
	
	private void initOriginGroupTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		OriginRoleCellRenderer originRoleCellRenderer = new OriginRoleCellRenderer(getLocale(), false);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginGroupTableModel.Cols.role, originRoleCellRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginGroupTableModel.Cols.group));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginGroupTableModel.Cols.created));
		originGroupTableModel = new OriginGroupTableModel(columnsModel);
		originGroupTable = uifactory.addTableElement(getWindowControl(), "originGroupTable", originGroupTableModel, 
				20, false, getTranslator(), formLayout);
		originGroupTable.setCustomizeColumns(false);
		originGroupTable.setNumOfRowsEnabled(false);
	}
	
	private void initOriginCoursePlannerTable(FormItemContainer formLayout) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		OriginRoleCellRenderer originRoleCellRenderer = new OriginRoleCellRenderer(getLocale(), true);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginCoursePlannerTableModel.Cols.role, originRoleCellRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginCoursePlannerTableModel.Cols.element));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginCoursePlannerTableModel.Cols.extRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginCoursePlannerTableModel.Cols.product));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(OriginCoursePlannerTableModel.Cols.created));
		originCoursePlannerTableModel = new OriginCoursePlannerTableModel(columnsModel);
		originCoursePlannerTable = uifactory.addTableElement(getWindowControl(), "originCoursePlannerTable", originCoursePlannerTableModel, 
				20, false, getTranslator(), formLayout);
		originCoursePlannerTable.setCustomizeColumns(false);
		originCoursePlannerTable.setNumOfRowsEnabled(false);
	}
	
	public void reloadModel() {
		List<OriginCourseRow> originCourseRows = originQueries.getCourseOrigins(repoEntry.getKey(), identity.getKey());
		originCourseTableModel.setObjects(originCourseRows);
		originCourseTable.reset(true, true, true);
		originCourseTable.setVisible(!originCourseRows.isEmpty());
		
		List<OriginGroupRow> originGroupRows = originQueries.getGroupOrigins(repoEntry.getKey(), identity.getKey());
		originGroupTableModel.setObjects(originGroupRows);
		originGroupTable.reset(true, true, true);
		originGroupTable.setVisible(!originGroupRows.isEmpty());
		
		List<OriginCoursePlannerRow> originCoursePlannerRows = originQueries.getCoursePlannerOrigins(repoEntry.getKey(), identity.getKey());
		originCoursePlannerTableModel.setObjects(originCoursePlannerRows);
		originCoursePlannerTable.reset(true, true, true);
		originCoursePlannerTable.setVisible(!originCoursePlannerRows.isEmpty());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		
		if (editMembershipButton == source) {
			fireEvent(ureq, EDIT_EVENT);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
