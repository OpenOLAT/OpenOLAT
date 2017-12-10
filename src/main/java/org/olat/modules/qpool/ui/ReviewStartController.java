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
package org.olat.modules.qpool.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.12.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReviewStartController extends FormBasicController {

	private static final String NO_TAXONOMY_LEVEL_KEY = "no-tax";

	private SingleSelection taxonomyLevelEl;

	private final QuestionItem item;
	private final List<TaxonomyLevel> taxonomyLevels;
	
	@Autowired
	private QPoolService qpoolService;
	
	public ReviewStartController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		this.item = item;
		this.taxonomyLevels = qpoolService.getTaxonomyLevel(ureq.getIdentity(), TaxonomyCompetenceTypes.teach);
		
		initForm(ureq);
	}
	
	public TaxonomyLevel getSelectedTaxonomyLevel() {
		Long selectedTaxonomyLevelKey = Long.parseLong(taxonomyLevelEl.getSelectedKey());
		for (TaxonomyLevel taxonomyLevel: taxonomyLevels) {
			if (taxonomyLevel.getKey().equals(selectedTaxonomyLevelKey)) {
				return taxonomyLevel;
			}
		}
		return null;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("process.start.review.description");
		
		String[] taxonomyKeys = new String[taxonomyLevels.size() + 1];
		String[] taxonomyValues = new String[taxonomyLevels.size() + 1];
		taxonomyKeys[0] = NO_TAXONOMY_LEVEL_KEY;
		taxonomyValues[0] = "-";
		for(int i=taxonomyLevels.size(); i-->0; ) {
			TaxonomyLevel taxonomyLevel = taxonomyLevels.get(i);
			taxonomyKeys[i + 1] = taxonomyLevel.getKey().toString();
			taxonomyValues[i + 1] = taxonomyLevel.getDisplayName();
		}
		
		taxonomyLevelEl = uifactory.addDropdownSingleselect("process.start.review.taxonomy.level", formLayout, taxonomyKeys, taxonomyValues, null);
		taxonomyLevelEl.setMandatory(true);
		taxonomyLevelEl.setAllowNoSelection(false);
		taxonomyLevelEl.select(taxonomyKeys[0], false);
		if (item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl) item;
			TaxonomyLevel selectedTaxonomyLevel = itemImpl.getTaxonomyLevel();
			if(selectedTaxonomyLevel != null) {
				String selectedTaxonomyLevelKey = String.valueOf(selectedTaxonomyLevel.getKey());
				for(String taxonomyKey:taxonomyKeys) {
					if(taxonomyKey.equals(selectedTaxonomyLevelKey)) {
						taxonomyLevelEl.select(taxonomyKey, true);
					}
				}
			}
		}

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("process.start.review.button", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());	
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;

		if (!taxonomyLevelEl.isOneSelected() || NO_TAXONOMY_LEVEL_KEY.equals(taxonomyLevelEl.getSelectedKey())) {
			taxonomyLevelEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		}
		
		return allOk && super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

}
