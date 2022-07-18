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
import org.olat.core.util.Util;
import org.olat.modules.qpool.QItemFactory;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.ui.events.QItemCreationCmdEvent;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory;
import org.olat.modules.qpool.ui.tree.QPoolTaxonomyTreeBuilder;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
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

	private TextElement titleEl;
	private SingleSelection typeEl;
	private SingleSelection taxonomyLevelEl;

	private Map<String,QItemFactory> keyToFactoryMap = new HashMap<>();
	private TaxonomyLevel selectedTaxonomyLevel;
	
	private final QPoolSecurityCallback qPoolSecurityCallback;
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private QPoolTaxonomyTreeBuilder qpoolTaxonomyTreeBuilder;

	public NewItemOptionsController(UserRequest ureq, WindowControl wControl,
			QPoolSecurityCallback qPoolSecurityCallback, boolean ignoreCompetences) {
		this(ureq, wControl, qPoolSecurityCallback, null, ignoreCompetences);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
	}

	public NewItemOptionsController(UserRequest ureq, WindowControl wControl,
			QPoolSecurityCallback qPoolSecurityCallback, TaxonomyLevel selectedTaxonomyLevel,
			boolean ignoreCompetences) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.qPoolSecurityCallback = qPoolSecurityCallback;
		this.selectedTaxonomyLevel = selectedTaxonomyLevel;
		qpoolTaxonomyTreeBuilder.loadTaxonomyLevelsSelection(getTranslator(), getIdentity(), true, ignoreCompetences);

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_new_item_form");
		//title
		titleEl = uifactory.addTextElement("general.title", "general.title", 128, "", formLayout);
		titleEl.setElementCssClass("o_sel_item_title");

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
			String typeKey = factory.getType();
			typeKeys[count] = typeKey;
			keyToFactoryMap.put(typeKey, factory);
			valueKeys[count] = factory.getLabel(getLocale());
			count++;
		}

		typeEl = uifactory.addDropdownSingleselect("question.type", "menu.admin.types", formLayout, typeKeys, valueKeys, null);
		typeEl.setElementCssClass("o_sel_item_type");
		
		//subject
		taxonomyLevelEl = uifactory.addDropdownSingleselect("process.start.review.taxonomy.level", formLayout,
				qpoolTaxonomyTreeBuilder.getSelectableKeys(), qpoolTaxonomyTreeBuilder.getSelectableValues(), null);
		if(selectedTaxonomyLevel != null) {
			String selectedTaxonomyLevelKey = String.valueOf(selectedTaxonomyLevel.getKey());
			for(String taxonomyKey: qpoolTaxonomyTreeBuilder.getSelectableKeys()) {
				if(taxonomyKey.equals(selectedTaxonomyLevelKey)) {
					taxonomyLevelEl.select(taxonomyKey, true);
				}
			}
		}
		taxonomyLevelEl.setVisible(qPoolSecurityCallback.canUseTaxonomy());
		taxonomyLevelEl.setElementCssClass("o_sel_item_taxonomy_level");
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("new.item", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= MetaUIFactory.validateElementLogic(titleEl, titleEl.getMaxLength(), true, true);
		
		typeEl.clearError();
		if(!typeEl.isOneSelected()) {
			typeEl.setErrorKey("form.mandatory.hover", null);
			allOk = false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String typeKey = typeEl.getSelectedKey();
		QItemFactory factory = keyToFactoryMap.get(typeKey);
		String title = titleEl.getValue();
		TaxonomyLevel taxonomyLevel = null;
		if (taxonomyLevelEl.isOneSelected()) {
			String selectedKey = taxonomyLevelEl.getSelectedKey();
			taxonomyLevel = qpoolTaxonomyTreeBuilder.getTaxonomyLevel(selectedKey);
		}
		fireEvent(ureq, new QItemCreationCmdEvent(title, taxonomyLevel, factory));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
}