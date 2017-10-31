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
package org.olat.modules.qpool.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelEditController extends FormBasicController {

	private TextElement nameEl;

	private TaxonomyLevel taxonomyLevel;
	private final TaxonomyLevel parentLevel;
	
	@Autowired
	private QPoolService qpoolService;
	
	public TaxonomyLevelEditController(UserRequest ureq, WindowControl wControl, TaxonomyLevel parentLevel, TaxonomyLevel taxonomyLevel) {
		super(ureq, wControl, Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));
		
		this.parentLevel = parentLevel;
		this.taxonomyLevel = taxonomyLevel;
		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String parentLine;
		if(taxonomyLevel != null) {
			parentLine = taxonomyLevel.getMaterializedPathIdentifiers();
		} else if(parentLevel != null) {
			parentLine = parentLevel.getMaterializedPathIdentifiers();
		} else {
			parentLine = "/";
		}
		uifactory.addStaticExampleText("parentLine", "classification.taxonomy.parents", parentLine, formLayout);
		
		String name = taxonomyLevel == null ? "" : taxonomyLevel.getDisplayName();
		nameEl = uifactory.addTextElement("classification.taxonomy.level", "classification.taxonomy.level", 128, name, formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		nameEl.clearError();
		if(!StringHelper.containsNonWhitespace(nameEl.getValue())) {
			nameEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		}

		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(taxonomyLevel == null) {
			taxonomyLevel = qpoolService.createTaxonomyLevel(parentLevel, nameEl.getValue(), nameEl.getValue());
		} else {
			String newField = nameEl.getValue();
			taxonomyLevel = qpoolService.updateTaxonomyLevel(taxonomyLevel, newField, newField);
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
