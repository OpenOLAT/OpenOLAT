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
package org.olat.modules.curriculum.ui;

import java.util.Comparator;
import java.util.List;

import org.olat.basesecurity.RightProvider;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.user.ui.role.EditRelationRoleController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 11 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumPlannerRightsController extends FormBasicController {

	private MultipleSelectionElement curriculumUserOverviewEl;

	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private List<RightProvider> relationRights;

	public CurriculumPlannerRightsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(EditRelationRoleController.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("role.identifier", "curriculum.admin.organisation.role", translate("curriculum.admin.planner"), formLayout);

		relationRights.sort(Comparator.comparing(RightProvider::getUserRelationsPosition));

		String[] cssClasses = new String[relationRights.size()];
		SelectionValues rightsKeyValues = new SelectionValues();
		for(int i=0; i < relationRights.size(); i++) {
			RightProvider provider = relationRights.get(i);
			if (provider.getParent() != null) {
				cssClasses[i] = "o_checkbox_indented";
			}
			String val = provider.getTranslatedName(getLocale());
			rightsKeyValues.add(SelectionValues.entry(provider.getRight(), val));
		}

		curriculumUserOverviewEl = uifactory.addCheckboxesVertical("curriculum.user.rights.overview",
				"curriculum.user.rights.overview", formLayout, rightsKeyValues.keys(), rightsKeyValues.values(),
				cssClasses, null, 1);
		curriculumUserOverviewEl.addActionListener(FormEvent.ONCHANGE);
		List<String> selectedRights = curriculumModule.getUserOverviewRightList();
		for(String selectedRight:selectedRights) {
			if(rightsKeyValues.containsKey(selectedRight)) {
				curriculumUserOverviewEl.select(selectedRight, true);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(curriculumUserOverviewEl == source) {
			curriculumModule.setUserOverviewRightList(curriculumUserOverviewEl.getSelectedKeys());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
