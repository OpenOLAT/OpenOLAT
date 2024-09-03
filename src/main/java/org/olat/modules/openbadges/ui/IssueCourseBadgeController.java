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
package org.olat.modules.openbadges.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.openbadges.BadgeClass;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-08-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class IssueCourseBadgeController extends FormBasicController {
	private BadgeClass badgeClass;
	private BadgeEarnersTableModel tableModel;
	private FlexiTableElement tableEl;
	private List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private BaseSecurityModule baseSecurityModule;
	@Autowired
	private OpenBadgesManager openBadgesManager;

	protected IssueCourseBadgeController(UserRequest ureq, WindowControl wControl, BadgeClass badgeClass) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.badgeClass = badgeClass;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean isAdministrator = baseSecurityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(BadgeEarnersTableModel.usageIdentifier, isAdministrator);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		int colIndex = BadgeEarnersTableModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(BadgeEarnersTableModel.usageIdentifier, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(
					visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex,
					null, true, "userProp-" + colIndex
			));
			colIndex++;
		}

		Map<Long, AssessmentEntry> identityKeyToAssessmentEntry = new HashMap<>();
		ICourse course = CourseFactory.loadCourse(badgeClass.getEntry());
		String rootIdent = course.getRunStructure().getRootNode().getIdent();
		AssessmentToolSecurityCallback secCallback = new AssessmentToolSecurityCallback(true, false, false, true, true, true, null, null);
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(
				badgeClass.getEntry(), rootIdent, null, secCallback);
		assessmentToolManager.getAssessmentEntries(getIdentity(), params, null)
				.stream()
				.filter(entry -> entry.getIdentity() != null)
				.forEach(entry -> identityKeyToAssessmentEntry.put(entry.getIdentity().getKey(), entry));
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);

		List<BadgeEarnerRow> rows = new ArrayList<>();
		for (Identity assessedIdentity : assessedIdentities) {
			AssessmentEntry assessmentEntry = identityKeyToAssessmentEntry.get(assessedIdentity.getKey());
			if (assessmentEntry == null) {
				continue;
			}

			BadgeEarnerRow row = new BadgeEarnerRow(assessedIdentity, userPropertyHandlers, getLocale());
			rows.add(row);
		}

		tableModel = new BadgeEarnersTableModel(columnsModel, getLocale());
		tableModel.setObjects(rows);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 10,
				false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.reset();
		tableEl.reloadData();

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("issueBadge", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		badgeClass = openBadgesManager.getBadgeClass(badgeClass.getUuid());
		openBadgesManager.issueBadge(badgeClass, getSelectedIdentities(), getIdentity());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private List<Identity> getSelectedIdentities() {
		List<Identity> selectedEntities = new ArrayList<>();
		Set<Integer> selectedIndices = tableEl.getMultiSelectedIndex();
		for (Integer selectedIndex : selectedIndices) {
			Identity identity = tableModel.getObject(selectedIndex).getIdentity();
			if (identity != null) {
				selectedEntities.add(identity);
			}
		}
		return selectedEntities;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
