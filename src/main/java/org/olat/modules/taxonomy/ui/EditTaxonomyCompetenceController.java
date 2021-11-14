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
package org.olat.modules.taxonomy.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTaxonomyCompetenceController extends FormBasicController {

	private DateChooser expirationEl;
	
	private TaxonomyLevel taxonomyLevel;
	private TaxonomyCompetence competence;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public EditTaxonomyCompetenceController(UserRequest ureq, WindowControl wControl, TaxonomyCompetence competence) {
		super(ureq, wControl);
		this.competence = competence;
		taxonomyLevel = competence.getTaxonomyLevel();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String fullName = userManager.getUserDisplayName(competence.getIdentity());
		uifactory.addStaticTextElement("taxonomy.competence.fullName", fullName, formLayout);

		TaxonomyCompetenceTypes competenceType = competence.getCompetenceType();
		String type = translate(competenceType.name());
		uifactory.addStaticTextElement("taxonomy.competence.type", type, formLayout);
		
		TaxonomyLevelManagedFlag marker = TaxonomyLevelManagedFlag.getCorrespondingFlag(competenceType);
		expirationEl = uifactory.addDateChooser("taxonomy.competence.expiration", competence.getExpiration(), formLayout);
		expirationEl.setEnabled(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, marker));
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		if(expirationEl.isEnabled()) {//save only if there is something to update
			uifactory.addFormSubmitButton("save", buttonsCont);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		competence.setExpiration(expirationEl.getDate());
		competence = taxonomyService.updateTaxonomyLevelCompetence(competence);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}