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
package org.olat.modules.cemedia.ui.medias;

import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.manager.LicenseTypeDAO;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.TextMode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaCenterLicenseHandler;
import org.olat.modules.cemedia.MediaModule;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.ui.MediaRelationsController;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelection;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 juil. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractCollectMediaController extends FormBasicController {

	private TagSelection tagsEl;
	private RichTextElement descriptionEl;
	private TaxonomyLevelSelection taxonomyLevelEl;
	private SingleSelection licenseEl;
	private TextElement licensorEl;
	private TextElement licenseFreetextEl;
	private MediaRelationsController relationsCtrl;

	protected Media mediaReference;
	private ResourceLicense license;
	private final boolean metadataOnly;
	private String businessPath;

	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private MediaCenterLicenseHandler licenseHandler;
	@Autowired
	protected MediaService mediaService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private MediaModule mediaModule;

	public AbstractCollectMediaController(UserRequest ureq, WindowControl wControl, Media media, Translator translator) {
		this(ureq, wControl, media, translator, false);
	}

	public AbstractCollectMediaController(UserRequest ureq, WindowControl wControl, Media media, Translator translator,
										  boolean metadataOnly) {
		super(ureq, wControl, translator);
		this.mediaReference = media;
		this.metadataOnly = metadataOnly;
		if(media != null) {
			license = licenseService.loadLicense(media);
		}
	}

	protected void createRelationsController(UserRequest ureq) {
		relationsCtrl = new MediaRelationsController(ureq, getWindowControl(), mainForm, null, true, true);
		relationsCtrl.setOpenClose(false);
		listenTo(relationsCtrl);
	}

	protected void initCommonMetadata(FormItemContainer formLayout) {
		List<TagInfo> tagsInfos = mediaService.getTagInfos(mediaReference, getIdentity(), false);
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), tagsInfos);
		tagsEl.setHelpText(translate("categories.hint"));
		tagsEl.setElementCssClass("o_sel_ep_tagsinput");

		List<TaxonomyLevel> levels = mediaService.getTaxonomyLevels(mediaReference);
		Set<TaxonomyLevel> availableTaxonomyLevels = taxonomyService.getTaxonomyLevelsAsSet(mediaModule.getTaxonomyRefs());
		taxonomyLevelEl = uifactory.addTaxonomyLevelSelection("taxonomy.levels", "taxonomy.levels", formLayout,
				getWindowControl(), availableTaxonomyLevels);
		taxonomyLevelEl.setDisplayNameHeader(translate("table.header.taxonomy"));
		taxonomyLevelEl.setSelection(levels);

		String desc = mediaReference == null ? null : mediaReference.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic("artefact.descr", "artefact.descr", desc, 4, -1, formLayout, getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.getEditorConfiguration().setSimplestTextModeAllowed(TextMode.multiLine);

		initLicenseForm(formLayout);

		String link = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		StaticTextElement linkEl = uifactory.addStaticTextElement("artefact.collect.link", "artefact.collect.link", link, formLayout);
		linkEl.setVisible(!metadataOnly && MediaUIHelper.showBusinessPath(businessPath));
	}

	protected void initRelationsAndSaveCancel(FormItemContainer formLayout, UserRequest ureq) {
		if (relationsCtrl != null) {
			FormItem relationsItem = relationsCtrl.getInitialFormItem();
			relationsItem.setFormLayout("0_12");
			formLayout.add(relationsItem);
		}

		FormLayoutContainer buttonsCont = uifactory.addInlineFormLayout("buttons", null, formLayout);
		if (relationsCtrl != null) {
			buttonsCont.setFormLayout("0_12");
		}
		uifactory.addFormSubmitButton("save", "save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	protected void initLicenseForm(FormItemContainer formLayout) {
		if (licenseModule.isEnabled(licenseHandler)) {
			LicenseSelectionConfig licenseSelectionConfig;
			if(license != null) {
				licenseSelectionConfig = LicenseUIFactory.createLicenseSelectionConfig(licenseHandler, license.getLicenseType());
			} else {
				licenseSelectionConfig = LicenseUIFactory.createLicenseSelectionConfig(licenseHandler);
			}
			
			licenseEl = uifactory.addDropdownSingleselect("rights.license", formLayout,
					licenseSelectionConfig.getLicenseTypeKeys(),
					licenseSelectionConfig.getLicenseTypeValues(getLocale()));
			licenseEl.setElementCssClass("o_sel_repo_license");
			licenseEl.setMandatory(licenseSelectionConfig.isLicenseMandatory());
			if (licenseSelectionConfig.getSelectionLicenseTypeKey() != null) {
				licenseEl.select(licenseSelectionConfig.getSelectionLicenseTypeKey(), true);
			} else {
				String noLicenseKey = licenseService.loadLicenseTypeByName(LicenseTypeDAO.NO_LICENSE_NAME).getKey().toString();
				licenseEl.select(noLicenseKey, true);
			}
			licenseEl.addActionListener(FormEvent.ONCHANGE);
			
			String licensor = license == null ? null :  license.getLicensor();
			licensorEl = uifactory.addTextElement("rights.licensor", 1000, licensor, formLayout);

			String freetext = license != null && licenseService.isFreetext(license.getLicenseType())
					? license.getFreetext() : null;
			licenseFreetextEl = uifactory.addTextAreaElement("rights.freetext", 4, 72, freetext, formLayout);
			updateUILicense();
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (licenseEl != null) {
			licenseEl.clearError();
			if (LicenseUIFactory.validateLicenseTypeMandatoryButNonSelected(licenseEl)) {
				licenseEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == licenseEl) {
			updateUILicense();
		}
	}
	
	protected void updateUILicense() {
		LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
	}
	
	protected void setLicenseVisibility(boolean visible) {
		if(licenseEl == null) return;
		
		if(visible && !licenseEl.isVisible() && !licenseEl.isOneSelected()) {
			String noLicenseKey = licenseService.loadLicenseTypeByName(LicenseTypeDAO.NO_LICENSE_NAME).getKey().toString();
			licenseEl.select(noLicenseKey, true);
		}
		
		licenseEl.setVisible(visible);
		licensorEl.setVisible(visible);
		licenseFreetextEl.setVisible(visible);
	}
	
	protected void saveLicense() {
		if (licenseModule.isEnabled(licenseHandler)) {
			license = licenseService.loadOrCreateLicense(mediaReference);
			
			if (licenseEl != null && licenseEl.isOneSelected()) {
				String licenseTypeKey = licenseEl.getSelectedKey();
				LicenseType licneseType = licenseService.loadLicenseTypeByKey(licenseTypeKey);
				license.setLicenseType(licneseType);
			}
			String licensor = null;
			String freetext = null;
			if (licensorEl != null && licensorEl.isVisible()) {
				licensor = StringHelper.containsNonWhitespace(licensorEl.getValue())? licensorEl.getValue(): null;
			}
			if (licenseFreetextEl != null && licenseFreetextEl.isVisible()) {
				freetext = StringHelper.containsNonWhitespace(licenseFreetextEl.getValue())? licenseFreetextEl.getValue(): null;
			}
			license.setLicensor(licensor);
			license.setFreetext(freetext);
			license = licenseService.update(license);
			if(licensorEl != null && licenseFreetextEl != null) {
				licensorEl.setValue(license.getLicensor());
				licenseFreetextEl.setValue(license.getFreetext());
			}
		}
	}

	protected void saveTags() {
		List<String> updatedTags = getTags();
		mediaService.updateTags(getIdentity(), mediaReference, updatedTags);
	}

	protected void saveTaxonomyLevels() {
		Set<TaxonomyLevelRef> updatedLevels = getTaxonomyLevels();
		mediaService.updateTaxonomyLevels(mediaReference, updatedLevels);
	}

	protected void saveRelations() {
		if(getRelationsCtrl() != null) {
			getRelationsCtrl().saveRelations(mediaReference);
		}
	}

	public boolean isMetadataOnly() {
		return metadataOnly;
	}

	public void setDescription(String description) {
		descriptionEl.setValue(description);
	}

	public String getDescription() {
		return descriptionEl.getValue();
	}

	public List<String> getTags() {
		return tagsEl.getDisplayNames();
	}

	public Set<TaxonomyLevelRef> getTaxonomyLevels() {
		return taxonomyLevelEl.getSelection();
	}

	public String getBusinessPath() {
		return businessPath;
	}

	public void setBusinessPath(String businessPath) {
		this.businessPath = businessPath;
	}

	public MediaRelationsController getRelationsCtrl() {
		return relationsCtrl;
	}
}
