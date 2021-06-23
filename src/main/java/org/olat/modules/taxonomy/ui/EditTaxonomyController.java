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
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyManagedFlag;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class EditTaxonomyController extends FormBasicController {

	private RichTextElement descriptionEl;
	private TextElement identifierEl, displayNameEl;
	
	private Taxonomy taxonomy;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public EditTaxonomyController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl);
		this.taxonomy = taxonomy;
		
		initForm(ureq);
	}
	
	public Taxonomy getTaxonomy() {
		return taxonomy;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_taxonomy_form");
		
		if(taxonomy != null) {
			String key = taxonomy.getKey().toString();
			uifactory.addStaticTextElement("taxonomy.key", key, formLayout);
			String externalId = taxonomy.getExternalId();
			uifactory.addStaticTextElement("taxonomy.external.id", externalId, formLayout);
		}
		
		String identifier = taxonomy == null ? "" : taxonomy.getIdentifier();
		identifierEl = uifactory.addTextElement("taxonomy.identifier", "taxonomy.identifier", 255, identifier, formLayout);
		identifierEl.setEnabled(!TaxonomyManagedFlag.isManaged(taxonomy, TaxonomyManagedFlag.identifier));
		identifierEl.setMandatory(true);

		String displayName = taxonomy == null ? "" : taxonomy.getDisplayName();
		displayNameEl = uifactory.addTextElement("taxonomy.displayname", "taxonomy.displayname", 255, displayName, formLayout);
		displayNameEl.setEnabled(!TaxonomyManagedFlag.isManaged(taxonomy, TaxonomyManagedFlag.displayName));
		displayNameEl.setMandatory(true);
		
		String description = taxonomy == null ? "" : taxonomy.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataCompact("taxonomy.description", "taxonomy.description", description, 10, 60, null,
				formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.setEnabled(!TaxonomyManagedFlag.isManaged(taxonomy, TaxonomyManagedFlag.description));
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		displayNameEl.clearError();
		if(!StringHelper.containsNonWhitespace(displayNameEl.getValue())) {
			displayNameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (displayNameEl.getValue().length() > 255) {
			displayNameEl.setErrorKey("import.taxonomy.error.displayname.too.long", new String[] {"255"});
			allOk &= false;
		}
		
		identifierEl.clearError();
		if(!StringHelper.containsNonWhitespace(identifierEl.getValue())) {
			identifierEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if (identifierEl.getValue().length() > 64) {
			identifierEl.setErrorKey("import.taxonomy.error.identifier.too.long", new String[] {"64"});
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(taxonomy == null) {
			//create a new one
			taxonomy = taxonomyService
					.createTaxonomy(identifierEl.getValue(), displayNameEl.getValue(), descriptionEl.getValue(), null);
		} else {
			taxonomy = taxonomyService.getTaxonomy(taxonomy);
			taxonomy.setIdentifier(identifierEl.getValue());
			taxonomy.setDisplayName(displayNameEl.getValue());
			taxonomy.setDescription(descriptionEl.getValue());
			taxonomy = taxonomyService.updateTaxonomy(taxonomy);
		}

		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}