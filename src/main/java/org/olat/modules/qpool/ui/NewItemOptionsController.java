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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ui.events.QItemCreationCmdEvent;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Choose the type of the new item and send an event
 * 
 * Initial date: 21.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NewItemOptionsController extends FormBasicController {

	private static final String NO_TAXONOMY_LEVEL_KEY = "no-tax";

	private TextElement titleEl;
	private SingleSelection typeEl;
	private SingleSelection taxonomyLevelEl;

	private Map<String,QItemFactory> keyToFactoryMap = new HashMap<>();
	private TaxonomyLevel selectedTaxonomyLevel;
	private final List<TaxonomyLevel> taxonomyLevels;
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private QPoolService qpoolService;

	public NewItemOptionsController(UserRequest ureq, WindowControl wControl) {	
		this(ureq, wControl, null);
	}
	
	public NewItemOptionsController(UserRequest ureq, WindowControl wControl, TaxonomyLevel selectedTaxonomyLevel) {	
		super(ureq, wControl);
		this.selectedTaxonomyLevel = selectedTaxonomyLevel;
		this.taxonomyLevels = qpoolService.getTaxonomyLevel(ureq.getIdentity(), TaxonomyCompetenceTypes.teach);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//title
		titleEl = uifactory.addTextElement("general.title", "general.title", 128, "", formLayout);

		//type
		List<QItemFactory> factories = new ArrayList<>();
		for(QPoolSPI spi:qpoolModule.getQuestionPoolProviders()) {
			for(QItemFactory factory:spi.getItemfactories()) {
				factories.add(factory);
			}
		}
		
		int count = 0;
		String[] typeKeys = new String[factories.size()];
		String[] valueKeys = new String[factories.size()];
		for(QItemFactory factory:factories) {
			String typeKey = "item.type." + count;
			typeKeys[count] = typeKey;
			keyToFactoryMap.put(typeKey, factory);
			valueKeys[count] = factory.getLabel(getLocale());
			count++;
		}

		typeEl = uifactory.addDropdownSingleselect("question.type", "menu.admin.types", formLayout, typeKeys, valueKeys, null);
		
		//subject
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
		if(selectedTaxonomyLevel != null) {
			String selectedTaxonomyLevelKey = String.valueOf(selectedTaxonomyLevel.getKey());
			for(String taxonomyKey:taxonomyKeys) {
				if(taxonomyKey.equals(selectedTaxonomyLevelKey)) {
					taxonomyLevelEl.select(taxonomyKey, true);
				}
			}
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("new.item", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		allOk &= MetaUIFactory.validateElementLogic(titleEl, titleEl.getMaxLength(), true, true);
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		}
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String typeKey = typeEl.getSelectedKey();
		QItemFactory factory = keyToFactoryMap.get(typeKey);
		String title = titleEl.getValue();
		TaxonomyLevel taxonomyLevel = getSelectedTaxonomyLevel();
		fireEvent(ureq, new QItemCreationCmdEvent(title, taxonomyLevel, factory));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private TaxonomyLevel getSelectedTaxonomyLevel() {
		if (!NO_TAXONOMY_LEVEL_KEY.equals(taxonomyLevelEl.getSelectedKey())) {
			Long selectedTaxonomyLevelKey = Long.parseLong(taxonomyLevelEl.getSelectedKey());
			for (TaxonomyLevel taxonomyLevel: taxonomyLevels) {
				if (taxonomyLevel.getKey().equals(selectedTaxonomyLevelKey)) {
					return taxonomyLevel;
				}
			}
		}
		return null;
	}
}