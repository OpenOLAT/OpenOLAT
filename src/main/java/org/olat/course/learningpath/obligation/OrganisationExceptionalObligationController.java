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
package org.olat.course.learningpath.obligation;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationRefImpl;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OrganisationNameComparator;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.Util;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationExceptionalObligationController extends FormBasicController
		implements ExceptionalObligationController {
	
	private MultipleSelectionElement organisationsEl;
	
	@Autowired
	private OrganisationService organisationService;

	public OrganisationExceptionalObligationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	public List<ExceptionalObligation> getExceptionalObligations() {
		return organisationsEl.getSelectedKeys().stream()
				.map(Long::valueOf)
				.map(OrganisationRefImpl::new)
				.map(this::createExceptionalObligation)
				.collect(Collectors.toList());
	}

	private ExceptionalObligation createExceptionalObligation(OrganisationRef organisationRef) {
		OrganisationExceptionalObligation exceptionalObligation = new OrganisationExceptionalObligation();
		exceptionalObligation.setType(OrganisationExceptionalObligationHandler.TYPE);
		exceptionalObligation.setOrganisationRef(organisationRef);
		return exceptionalObligation;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues organisationKV = new SelectionValues();
		organisationService.getOrganisations(getIdentity(), ureq.getUserSession().getRoles(),
				OrganisationRoles.administrator, OrganisationRoles.learnresourcemanager, OrganisationRoles.author)
		 	.stream()
			.sorted(new OrganisationNameComparator(getLocale()))
			.forEach(organisation -> organisationKV.add(
					SelectionValues.entry(organisation.getKey().toString(),
					organisation.getDisplayName())));
		
		organisationsEl = uifactory.addCheckboxesDropdown("organisations", "config.exceptional.obligation.organisations", formLayout,
				organisationKV.keys(), organisationKV.values());
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonCont.setElementCssClass("o_button_group_right o_block_top");
		buttonCont.setRootForm(mainForm);
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("config.exceptional.obligation.add.button", buttonCont);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
