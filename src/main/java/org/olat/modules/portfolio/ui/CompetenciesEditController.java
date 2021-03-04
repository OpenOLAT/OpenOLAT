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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reusable controller to show the list of competencies and edit them with
 * a small edit button. When edited, an Event.CHANGED is fired
 * 
 * Initial date: 28.07.2017<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class CompetenciesEditController extends FormBasicController {
	
	private TextBoxListElement competenciesEl;
	private FormLink editLink;
	private FormSubmit saveButton;

	List<TextBoxItem> existingCompetencies;
	List<TextBoxItem> availableTaxonomyLevels;
	
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public CompetenciesEditController(UserRequest ureq, WindowControl wControl, Page portfolioPage) {
		super(ureq, wControl, "competencies_edit");
		
		List<TaxonomyCompetence> competencies = portfolioService.getRelatedCompetencies(portfolioPage, true);
		existingCompetencies = new ArrayList<>();
		for (TaxonomyCompetence competence : competencies) {
			TextBoxItemImpl competenceTextBoxItem = new TextBoxItemImpl(competence.getTaxonomyLevel().getDisplayName(), competence.getTaxonomyLevel().getKey().toString());
			competenceTextBoxItem.setCustomCSS("o_competence");
			existingCompetencies.add(competenceTextBoxItem);
		}
		
		availableTaxonomyLevels = new ArrayList<>();
		if (portfolioModule.isTaxonomyLinkingReady()) {
			for (Taxonomy taxonomy : portfolioModule.getLinkedTaxonomies()) {
				for (TaxonomyLevel taxonomyLevel : taxonomyService.getTaxonomyLevels(taxonomy)) {
					availableTaxonomyLevels.add(new TextBoxItemImpl(taxonomyLevel.getDisplayName(), taxonomyLevel.getKey().toString()));
				}
			}
		}

		initForm(ureq);
		/* we add domID to competencies_edit.html to reduce DIV count */
		this.flc.getFormItemComponent().setDomReplacementWrapperRequired(false);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_entry_tags_form");
		
		competenciesEl = uifactory.addTextBoxListElement("competencies", "competencies", "competencies.hint", existingCompetencies, formLayout, getTranslator());
		competenciesEl.setHelpText(translate("competencies.hint"));
		competenciesEl.setAllowDuplicates(false);
		competenciesEl.setAllowNewValues(false);
		competenciesEl.setElementCssClass("o_block_inline");
		competenciesEl.getComponent().setSpanAsDomReplaceable(false);
		competenciesEl.setIcon("o_icon_competences");
		competenciesEl.setAutoCompleteContent(availableTaxonomyLevels);
		competenciesEl.setCustomCSSForItems("o_competence");
		
		
		editLink = uifactory.addFormLink("edit", "edit", "edit", null, formLayout, Link.LINK);
		editLink.setCustomEnabledLinkCSS("o_button_textstyle");
		
		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		saveButton.setVisible(false);
		saveButton.setElementCssClass("btn-xs");
		
		// on init set to read-only
		initFormEditableState(false);
	}

	/**
	 * Internal helper to hide and disable elements of the form depending on the
	 * editable state of the form
	 * 
	 * @param editable
	 */
	private void initFormEditableState(boolean editable) {
		competenciesEl.setEnabled(editable);
		editLink.setVisible(!editable);
		saveButton.setVisible(editable);
		// Special label when no categories are there
		if (competenciesEl.getValueList().isEmpty()) {
			editLink.setI18nKey("competencies.add");			
		} else {
			editLink.setI18nKey("edit");
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == editLink) {
			initFormEditableState(true);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
		initFormEditableState(false);
	}

	/**
	 * @return The list of competencies as visually configured in the box
	 */
	public List<TextBoxItem> getUpdatedCompetencies() {
		return competenciesEl.getValueItems();
	}
	
	@Override
	protected void doDispose() {
		//
	}
}