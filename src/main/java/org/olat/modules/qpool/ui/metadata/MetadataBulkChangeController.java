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
package org.olat.modules.qpool.ui.metadata;

import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.toBigDecimal;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.toInt;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateBigDecimal;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateElementLogic;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateInteger;
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateSelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.manager.MetadataConverterHelper;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.ItemRow;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory.KeyValues;
import org.olat.modules.qpool.ui.tree.QPoolTaxonomyTreeBuilder;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.05.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadataBulkChangeController extends FormBasicController {

	private static final String[] EMPTY_VALUES = new String[]{ "" };
	
	private TextElement topicEl;
	private TextElement keywordsEl;
	private TextElement coverageEl;
	private TextElement addInfosEl;
	private TextElement languageEl;
	private SingleSelection taxonomyLevelEl;
	private SingleSelection contextEl;
	private FormLayoutContainer learningTimeContainer;
	private IntegerElement learningTimeDayElement;
	private IntegerElement learningTimeHourElement;
	private IntegerElement learningTimeMinuteElement;
	private IntegerElement learningTimeSecondElement;
	private TextElement correctionTimeElement;
	private SingleSelection assessmentTypeEl;
	private TextElement difficultyEl, stdevDifficultyEl, differentiationEl, numAnswerAltEl;
	private SingleSelection licenseEl;
	private TextElement licensorEl;
	private TextElement licenseFreetextEl;
	private TextElement versionEl;
	private SingleSelection statusEl;
	private FormLayoutContainer licenseWrapperCont;

	private Map<MultipleSelectionElement, FormLayoutContainer> checkboxContainer = new HashMap<>();
	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>();
	
	private KeyValues contextsKeyValues;
	private final QPoolSecurityCallback qpoolSecurityCallback;
	private List<QuestionItem> updatedItems;
	private final List<ItemRow> items;
	private final boolean ignoreCompetences;
	
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QPoolTaxonomyTreeBuilder qpoolTaxonomyTreeBuilder;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;

	public MetadataBulkChangeController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback qpoolSecurityCallback, List<ItemRow> items, boolean ignoreCompetences) {
		super(ureq, wControl, "bulk_change");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		this.qpoolSecurityCallback = qpoolSecurityCallback;
		this.items = items;
		this.ignoreCompetences = ignoreCompetences;
		initForm(ureq);
	}
	
	public List<QuestionItem> getUpdatedItems() {
		return updatedItems == null ? Collections.<QuestionItem>emptyList() : updatedItems;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(items.size() == 1) {
				layoutCont.contextPut("infosMsg", translate("bulk.change.description.singular"));
			} else {
				layoutCont.contextPut("infosMsg", translate("bulk.change.description.plural",
						new String[] { Integer.toString(items.size()) }));
			}
			layoutCont.contextPut("infosCss", items.size() > 20 ? "o_warning" :"o_info");
		}
		
		initGeneralForm(formLayout);
		initQuestionForm(formLayout);
		initTechnicalForm(formLayout);
		initRightsForm(formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void initGeneralForm(FormItemContainer formLayout) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		topicEl = uifactory.addTextElement("general.topic", "general.topic", 1000, null, generalCont);
		decorate(topicEl, generalCont);
		
		if (qpoolSecurityCallback.canUseTaxonomy()) {
			qpoolTaxonomyTreeBuilder.loadTaxonomyLevelsSelection(getTranslator(), getIdentity(), canRemoveTaxonomies(), ignoreCompetences);
			taxonomyLevelEl = uifactory.addDropdownSingleselect("classification.taxonomic.path", generalCont,
					qpoolTaxonomyTreeBuilder.getSelectableKeys(), qpoolTaxonomyTreeBuilder.getSelectableValues(), null);
			decorate(taxonomyLevelEl, generalCont);
		}
	
		if (qpoolSecurityCallback.canUseEducationalContext()) {
			contextsKeyValues = MetaUIFactory.getContextKeyValues(getTranslator(), qpoolService);
			contextEl = uifactory.addDropdownSingleselect("educational.context", "educational.context", generalCont,
					contextsKeyValues.getKeys(), contextsKeyValues.getValues(), null);
			contextEl.enableNoneSelection();
			decorate(contextEl, generalCont);
		}
		
		keywordsEl = uifactory.addTextElement("general.keywords", "general.keywords", 1000, null, generalCont);
		decorate(keywordsEl, generalCont);
		
		addInfosEl = uifactory.addTextElement("general.additional.informations", "general.additional.informations.long",
				256, "", generalCont);
		decorate(addInfosEl, generalCont);
		
		coverageEl = uifactory.addTextElement("general.coverage", "general.coverage", 1000, null, generalCont);
		decorate(coverageEl, generalCont);
		
		languageEl = uifactory.addTextElement("general.language", "general.language", 10, "", generalCont);
		decorate(languageEl, generalCont);
		
		KeyValues types = MetaUIFactory.getAssessmentTypes(getTranslator());
		assessmentTypeEl = uifactory.addDropdownSingleselect("question.assessmentType", "question.assessmentType", generalCont,
				types.getKeys(), types.getValues(), null);
		assessmentTypeEl.enableNoneSelection();
		decorate(assessmentTypeEl, generalCont);
	}
	
	private boolean canRemoveTaxonomies() {
		for (ItemRow item: items) {
			if (!item.getSecurityCallback().canRemoveTaxonomy()) {
				return false;
			}
		}
		return true;
	}

	private void initQuestionForm(FormItemContainer formLayout) {
		FormLayoutContainer questionCont = FormLayoutContainer.createDefaultFormLayout("question", getTranslator());
		questionCont.setRootForm(mainForm);
		formLayout.add(questionCont);
		
		String page = velocity_root + "/learning_time.html";
		learningTimeContainer = FormLayoutContainer.createCustomFormLayout("educational.learningTime", getTranslator(), page);
		learningTimeContainer.setRootForm(mainForm);
		questionCont.add(learningTimeContainer);
		decorate(learningTimeContainer, questionCont);

		learningTimeDayElement = uifactory.addIntegerElement("learningTime.day", "", 0, learningTimeContainer);
		learningTimeDayElement.setDisplaySize(3);
		learningTimeHourElement = uifactory.addIntegerElement("learningTime.hour", "", 0, learningTimeContainer);
		learningTimeHourElement.setDisplaySize(3);
		learningTimeMinuteElement = uifactory.addIntegerElement("learningTime.minute", "", 0, learningTimeContainer);
		learningTimeMinuteElement.setDisplaySize(3);
		learningTimeSecondElement = uifactory.addIntegerElement("learningTime.second", "", 0, learningTimeContainer);
		learningTimeSecondElement.setDisplaySize(3);
		
		correctionTimeElement = uifactory.addTextElement("question.correctionTime", "question.correctionTime", 10, null, questionCont);
		correctionTimeElement.setDisplaySize(4);
		decorate(correctionTimeElement, questionCont);
		
		difficultyEl = uifactory.addTextElement("question.difficulty", "question.difficulty", 10, null, questionCont);
		difficultyEl.setExampleKey("question.difficulty.example", null);
		difficultyEl.setDisplaySize(4);
		decorate(difficultyEl, questionCont);

		stdevDifficultyEl = uifactory.addTextElement("question.stdevDifficulty", "question.stdevDifficulty", 10, null, questionCont);
		stdevDifficultyEl.setExampleKey("question.stdevDifficulty.example", null);
		stdevDifficultyEl.setDisplaySize(4);
		decorate(stdevDifficultyEl, questionCont);
		
		differentiationEl = uifactory.addTextElement("question.differentiation", "question.differentiation", 10, null, questionCont);
		differentiationEl.setExampleKey("question.differentiation.example", null);
		differentiationEl.setDisplaySize(4);
		decorate(differentiationEl, questionCont);
		
		numAnswerAltEl = uifactory.addTextElement("question.numOfAnswerAlternatives", "question.numOfAnswerAlternatives", 10, null, questionCont);
		numAnswerAltEl.setDisplaySize(4);
		decorate(numAnswerAltEl, questionCont);

	}

	private void initTechnicalForm(FormItemContainer formLayout) {
		FormLayoutContainer technicalCont = FormLayoutContainer.createDefaultFormLayout("technical", getTranslator());
		technicalCont.setRootForm(mainForm);
		if (!qpoolModule.isReviewProcessEnabled()) {
			formLayout.add(technicalCont);
		}
		
		versionEl = uifactory.addTextElement("lifecycle.version", "lifecycle.version", 50, null, technicalCont);
		decorate(versionEl, technicalCont);
		
		KeyValues status = MetaUIFactory.getStatus(getTranslator());
		statusEl = uifactory.addDropdownSingleselect("lifecycle.status", "lifecycle.status", technicalCont,
				status.getKeys(), status.getValues(), null);
		decorate(statusEl, technicalCont);
	}

	private void initRightsForm(FormItemContainer formLayout) {
		FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("rights", getTranslator());
		rightsCont.setRootForm(mainForm);
		if (licenseModule.isEnabled(licenseHandler)) {
			formLayout.add(rightsCont);
		}
		
		LicenseSelectionConfig licenseSelectionConfig = LicenseUIFactory.createLicenseSelectionConfig(licenseHandler);
		
		licensorEl = uifactory.addTextElement("rights.licensor", 1000, "", rightsCont);
		decorate(licensorEl, rightsCont);
		
		licenseWrapperCont = FormLayoutContainer.createDefaultFormLayout("rights.license", getTranslator());
		licenseWrapperCont.setRootForm(mainForm);
		rightsCont.add(licenseWrapperCont);
		decorate(licenseWrapperCont, rightsCont);
		
		if (licenseSelectionConfig.getLicenseTypeKeys().length > 0) {
			licenseEl = uifactory.addDropdownSingleselect("rights.license.sel", "rights.license", licenseWrapperCont,
					licenseSelectionConfig.getLicenseTypeKeys(),
					licenseSelectionConfig.getLicenseTypeValues(getLocale()), null);
			licenseEl.addActionListener(FormEvent.ONCHANGE);
			licenseEl.select(licenseSelectionConfig.getLicenseTypeKeys()[0], true);

			licenseFreetextEl = uifactory.addTextAreaElement("rights.freetext", 4, 72, "", licenseWrapperCont);
			licenseFreetextEl.setVisible(false);
		}
	}
	
	private FormItem decorate(FormItem item, FormLayoutContainer formLayout) {
		String itemName = item.getName();
		MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("cbx_" + itemName, itemName, formLayout, new String[] { itemName }, EMPTY_VALUES);
		checkbox.select(itemName, false);
		checkbox.addActionListener(FormEvent.ONCLICK);
		checkbox.setUserObject(item);
		checkboxSwitch.add(checkbox);

		item.setLabel(null, null);
		item.setVisible(false);
		item.setUserObject(checkbox);
		
		checkboxContainer.put(checkbox, formLayout);
		formLayout.moveBefore(checkbox, item);
		return checkbox;
	}
	
	void updateLicenseVisibility() {
		boolean freetextSelected = false;
		if (licenseEl != null && licenseEl.isOneSelected()) {
			String selectedKey = licenseEl.getSelectedKey();
			LicenseType licenseType = licenseService.loadLicenseTypeByKey(selectedKey);
			freetextSelected = licenseService.isFreetext(licenseType);
		}
		licenseFreetextEl.setVisible(freetextSelected);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			checkboxContainer.get(checkbox).setDirty(true);
		} else if(licenseEl == source) {
			updateLicenseVisibility();
			licenseWrapperCont.setDirty(true);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		//general
		allOk &= validateElementLogic(topicEl, topicEl.getMaxLength(), false, isEnabled(topicEl));
		allOk &= validateElementLogic(keywordsEl, keywordsEl.getMaxLength(), false, isEnabled(keywordsEl));
		allOk &= validateElementLogic(coverageEl, coverageEl.getMaxLength(), false, isEnabled(coverageEl));
		allOk &= validateElementLogic(addInfosEl, addInfosEl.getMaxLength(), false, isEnabled(addInfosEl));
		allOk &= validateElementLogic(languageEl, languageEl.getMaxLength(), false, isEnabled(languageEl));
		
		//question
		allOk &= validateBigDecimal(difficultyEl, 0.0d, 1.0d, true);
		allOk &= validateBigDecimal(stdevDifficultyEl, 0.0d, 1.0d, true);
		allOk &= validateBigDecimal(differentiationEl, -1.0d, 1.0d, true);
		
		allOk &= validateInteger(correctionTimeElement, 0, 1000, isEnabled(correctionTimeElement));

		//technical
		allOk &= validateElementLogic(versionEl, versionEl.getMaxLength(), false, isEnabled(versionEl));
		allOk &= validateSelection(statusEl, isEnabled(statusEl));
		
		//rights
		allOk &= validateElementLogic(licensorEl, 1000, false, isEnabled(licensorEl));
		
		return allOk;
	}
	
	private boolean isEnabled(FormItem item) {
		if (item == null) return false;
		
		return ((MultipleSelectionElement)item.getUserObject()).isAtLeastSelected(1);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		updatedItems = new ArrayList<>();
		for(QuestionItemShort item : items) {
			QuestionItem fullItem = qpoolService.loadItemById(item.getKey());
			if(fullItem instanceof QuestionItemImpl) {
				QuestionItemImpl itemImpl = (QuestionItemImpl)fullItem;
				QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
						Action.UPDATE_QUESTION_ITEM_METADATA);
				builder.withBefore(itemImpl);
				
				formOKGeneral(itemImpl);
				formOKQuestion(itemImpl);
				formOKTechnical(itemImpl);
				formOKRights(itemImpl);
				QuestionItem merged = qpoolService.updateItem(itemImpl);
				builder.withAfter(itemImpl);
				qpoolService.persist(builder.create());
				updatedItems.add(merged);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void formOKGeneral(QuestionItemImpl itemImpl) {
		if(isEnabled(topicEl))
			itemImpl.setTopic(topicEl.getValue());
		if(isEnabled(keywordsEl))
			itemImpl.setKeywords(keywordsEl.getValue());
		if(isEnabled(coverageEl))
			itemImpl.setCoverage(coverageEl.getValue());
		if(isEnabled(addInfosEl))
			itemImpl.setAdditionalInformations(addInfosEl.getValue());
		if(isEnabled(languageEl))
			itemImpl.setLanguage(languageEl.getValue());
		if(isEnabled(taxonomyLevelEl)) {
			String selectedKey = taxonomyLevelEl.getSelectedKey();
			TaxonomyLevel taxonomyLevel = qpoolTaxonomyTreeBuilder.getTaxonomyLevel(selectedKey);
			itemImpl.setTaxonomyLevel(taxonomyLevel);
		}
	}
	
	private void formOKQuestion(QuestionItemImpl itemImpl) {
		if(isEnabled(contextEl)) {
			if(contextEl.isOneSelected()) {
				QEducationalContext context = MetaUIFactory.getContextByKey(contextEl.getSelectedKey(), qpoolService);
				itemImpl.setEducationalContext(context);
			} else {
				itemImpl.setEducationalContext(null);
			}
		}
		
		if(isEnabled(learningTimeContainer)) {
			int day = learningTimeDayElement.getIntValue();
			int hour = learningTimeHourElement.getIntValue();
			int minute = learningTimeMinuteElement.getIntValue();
			int seconds = learningTimeSecondElement.getIntValue();
			String timeStr = MetadataConverterHelper.convertDuration(day, hour, minute, seconds);
			itemImpl.setEducationalLearningTime(timeStr);
		}
		
		if(isEnabled(correctionTimeElement) && StringHelper.isLong(correctionTimeElement.getValue())) {
			itemImpl.setCorrectionTime(Integer.valueOf(correctionTimeElement.getValue()));
		}
		if(isEnabled(difficultyEl)) {
			itemImpl.setDifficulty(toBigDecimal(difficultyEl.getValue()));
		}
		if(isEnabled(stdevDifficultyEl)) {
			itemImpl.setStdevDifficulty(toBigDecimal(stdevDifficultyEl.getValue()));
		}
		if(isEnabled(differentiationEl)) {
			itemImpl.setDifferentiation(toBigDecimal(differentiationEl.getValue()));
		}
		if(isEnabled(numAnswerAltEl)) {
			itemImpl.setNumOfAnswerAlternatives(toInt(numAnswerAltEl.getValue()));
		}
		if(isEnabled(assessmentTypeEl)) {
			String assessmentType = assessmentTypeEl.isOneSelected() ? assessmentTypeEl.getSelectedKey() : null;
			itemImpl.setAssessmentType(assessmentType);
		}
	}

	private void formOKTechnical(QuestionItemImpl itemImpl) {
		if(isEnabled(statusEl) && statusEl.isOneSelected())
			itemImpl.setStatus(statusEl.getSelectedKey());
		if(isEnabled(versionEl))
			itemImpl.setItemVersion(versionEl.getValue());
	}
	
	private void formOKRights(QuestionItemImpl itemImpl) {
		if (isEnabled(licenseWrapperCont) || isEnabled(licensorEl)) {
			ResourceLicense license = licenseService.loadOrCreateLicense(itemImpl);
			if (isEnabled(licenseWrapperCont)) {
				if (licenseEl != null && licenseEl.isOneSelected()) {
					String licenseTypeKey = licenseEl.getSelectedKey();
					LicenseType licneseType = licenseService.loadLicenseTypeByKey(licenseTypeKey);
					license.setLicenseType(licneseType);
					String freetext = null;
					if (licenseFreetextEl != null && licenseFreetextEl.isVisible()) {
						freetext = StringHelper.containsNonWhitespace(licenseFreetextEl.getValue())? licenseFreetextEl.getValue(): null;
					}
					license.setFreetext(freetext);
					license = licenseService.update(license);
				}
			}
			if (isEnabled(licensorEl)) {
				license.setLicensor(licensorEl.getValue());
			}
			licenseService.update(license);
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}