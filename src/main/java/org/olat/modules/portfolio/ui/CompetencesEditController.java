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

import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.util.Util;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.CompetenceBrowserController;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Reusable controller to show the list of competences and edit them with
 * a small edit button. When edited, an Event.CHANGED is fired
 * 
 * Initial date: 28.07.2017<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class CompetencesEditController extends FormBasicController {
	
	private TextBoxListElement competencesEl;
	private FormLink editLink;
	private FormSubmit saveButton;

	private List<TextBoxItem> existingCompetences;
	private List<TextBoxItem> availableTaxonomyLevels;
	
	private FormLink openBrowserLink;
	
	@Autowired
	private PortfolioService portfolioService;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public CompetencesEditController(UserRequest ureq, WindowControl wControl, Page portfolioPage) {
		super(ureq, wControl, "competences_edit");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		
		List<TaxonomyCompetence> competences = portfolioService.getRelatedCompetences(portfolioPage, true);
		existingCompetences = new ArrayList<>();
		for (TaxonomyCompetence competence : competences) {
			TaxonomyLevel taxonomyLevel = competence.getTaxonomyLevel();
			TextBoxItemImpl competenceTextBoxItem = new TextBoxItemImpl(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel), taxonomyLevel.getKey().toString());
			competenceTextBoxItem.setTooltip(taxonomyLevel.getMaterializedPathIdentifiersWithoutSlash());
			competenceTextBoxItem.setCustomCSS("o_competence");
			existingCompetences.add(competenceTextBoxItem);
		}
		
		availableTaxonomyLevels = new ArrayList<>();
		if (portfolioModule.isTaxonomyLinkingReady()) {
			for (Taxonomy taxonomy : portfolioModule.getLinkedTaxonomies()) {
				for (TaxonomyLevel taxonomyLevel : taxonomyService.getTaxonomyLevels(taxonomy)) {
					if (taxonomyLevel.getType() != null) {
						if(taxonomyLevel.getType().isAllowedAsCompetence() == false) {
							// Do not list items, which are not marked as available for competences
							continue;
						}
					}
					
					TextBoxItem item = new TextBoxItemImpl(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel), taxonomyLevel.getKey().toString());
					item.setDropDownInfo(taxonomyLevel.getMaterializedPathIdentifiersWithoutSlash());
					item.setTooltip(taxonomyLevel.getMaterializedPathIdentifiersWithoutSlash());
					availableTaxonomyLevels.add(item);
				}
			}
		}
		
		initForm(ureq);
		/* we add domID to competences_edit.html to reduce DIV count */
		this.flc.getFormItemComponent().setDomReplacementWrapperRequired(false);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_pf_edit_entry_tags_form");
		
		competencesEl = uifactory.addTextBoxListElement("competences", "competences", "competences.hint", existingCompetences, formLayout, getTranslator());
		competencesEl.setHelpText(translate("competences.hint"));
		competencesEl.setAllowDuplicates(false);
		competencesEl.setAllowNewValues(false);
		competencesEl.setElementCssClass("o_block_inline");
		competencesEl.getComponent().setSpanAsDomReplaceable(false);
		competencesEl.setIcon("o_icon_competences");
		competencesEl.setAutoCompleteContent(availableTaxonomyLevels);
		competencesEl.setCustomCSSForItems("o_competence");
		competencesEl.setShowSaveButton(true);
		competencesEl.setShowInlineLabel(true);
		competencesEl.setIconTitleKey("competences");
		
		
		
		editLink = uifactory.addFormLink("edit", "edit", "edit", null, formLayout, Link.LINK);
		editLink.setCustomEnabledLinkCSS("o_button_textstyle");
		
		saveButton = uifactory.addFormSubmitButton("save", formLayout);
		saveButton.setVisible(false);
		saveButton.setElementCssClass("btn-xs");
		
		openBrowserLink = uifactory.addFormLink("open.browser", formLayout, Link.LINK);
		openBrowserLink.setCustomEnabledLinkCSS("o_button_textstyle");
		openBrowserLink.setPopup(new LinkPopupSettings(800, 600, "Open"));
		
		
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
		competencesEl.setEnabled(editable);
		editLink.setVisible(!editable);
		saveButton.setVisible(editable);
		// Special label when no categories are there
		if (competencesEl.getValueList().isEmpty()) {
			editLink.setI18nKey("add");			
		} else {
			editLink.setI18nKey("edit");
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == editLink) {
			initFormEditableState(true);
		} else if(source == competencesEl) {
			fireEvent(ureq, Event.CHANGED_EVENT);
			initFormEditableState(false);
		} else if(source == openBrowserLink) {
			List<Taxonomy> linkedTaxonomies = portfolioModule.getLinkedTaxonomies();
			List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevels(linkedTaxonomies);
			ControllerCreator competenceBrowserCreator = (lureq, lwControl) -> new CompetenceBrowserController(lureq,
					lwControl, linkedTaxonomies, taxonomyLevels, false);
			ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, competenceBrowserCreator);
			openInNewBrowserWindow(ureq, layoutCtrlr, false);
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
		initFormEditableState(false);
	}

	/**
	 * @return The list of competences as visually configured in the box
	 */
	public List<TextBoxItem> getUpdatedCompetences() {
		return competencesEl.getValueItems();
	}
}