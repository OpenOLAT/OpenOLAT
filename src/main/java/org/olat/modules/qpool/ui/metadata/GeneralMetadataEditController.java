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

import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateElementLogic;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
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
import org.olat.modules.qpool.MetadataSecurityCallback;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemEditable;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory.KeyValues;
import org.olat.modules.qpool.ui.tree.QPoolTaxonomyTreeBuilder;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GeneralMetadataEditController extends FormBasicController {

	private TextElement topicEl;
	private SingleSelection taxonomyLevelEl;
	private SingleSelection contextEl;
	private TextElement keywordsEl;
	private TextElement coverageEl;
	private TextElement addInfosEl;
	private TextElement languageEl;
	private SingleSelection assessmentTypeEl;
	private FormLayoutContainer buttonsCont;

	private final QPoolSecurityCallback qPoolSecurityCallback;
	private QuestionItem item;
	private MetadataSecurityCallback securityCallback;
	private final boolean ignoreCompetences;

	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QPoolTaxonomyTreeBuilder qpoolTaxonomyTreeBuilder;
	
	public GeneralMetadataEditController(UserRequest ureq, WindowControl wControl,
			QPoolSecurityCallback qPoolSecurityCallback, QuestionItem item, MetadataSecurityCallback securityCallback,
			boolean ignoreCompetences, boolean wideLayout) {
		super(ureq, wControl, wideLayout ? LAYOUT_DEFAULT : LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.qPoolSecurityCallback = qPoolSecurityCallback;
		this.item = item;
		this.securityCallback = securityCallback;
		this.ignoreCompetences = ignoreCompetences;
		
		initForm(ureq);
		setReadOnly();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String topic = item.getTopic();
		topicEl = uifactory.addTextElement("general.topic", "general.topic", 1000, topic, formLayout);
		topicEl.setElementCssClass("o_sel_qpool_metadata_topic");
		
		taxonomyLevelEl = uifactory.addDropdownSingleselect("general.taxonomy.level", formLayout, new String[0],
				new String[0]);
		taxonomyLevelEl.setElementCssClass("o_sel_qpool_metadata_taxonomy");
		buildTaxonomyLevelEl();
		taxonomyLevelEl.setVisible(qPoolSecurityCallback.canUseTaxonomy());
		
		KeyValues contexts = MetaUIFactory.getContextKeyValues(getTranslator(), qpoolService);
		contextEl = uifactory.addDropdownSingleselect("educational.context", "educational.context", formLayout,
				contexts.getKeys(), contexts.getValues(), null);
		contextEl.setElementCssClass("o_sel_qpool_metadata_context");
		contextEl.enableNoneSelection();
		contextEl.setEnabled(contexts.getKeys().length > 0);
		if (item.getEducationalContext() != null) {
			contextEl.select(item.getEducationalContext().getKey().toString(), true);
		}
		contextEl.setVisible(qPoolSecurityCallback.canUseEducationalContext());
		
		String keywords = item.getKeywords();
		keywordsEl = uifactory.addTextElement("general.keywords", "general.keywords", 1000, keywords, formLayout);
		keywordsEl.setElementCssClass("o_sel_qpool_metadata_keywords");
		
		String addInfos = item.getAdditionalInformations();
		addInfosEl = uifactory.addTextElement("general.additional.informations", "general.additional.informations", 256,
				addInfos, formLayout);
		addInfosEl.setElementCssClass("o_sel_qpool_metadata_add_infos");
		
		String coverage = item.getCoverage();
		coverageEl = uifactory.addTextElement("general.coverage", "general.coverage", 1000, coverage, formLayout);
		coverageEl.setElementCssClass("o_sel_qpool_metadata_coverage");

		String language = item.getLanguage();
		languageEl = uifactory.addTextElement("general.language", "general.language", 10, language, formLayout);
		languageEl.setElementCssClass("o_sel_qpool_metadata_language");
		
		KeyValues types = MetaUIFactory.getAssessmentTypes(getTranslator());
		assessmentTypeEl = uifactory.addDropdownSingleselect("question.assessmentType", "question.assessmentType",
				formLayout, types.getKeys(), types.getValues(), null);
		assessmentTypeEl.setElementCssClass("o_sel_qpool_metadata_assessment_type");
		assessmentTypeEl.enableNoneSelection();
		if(StringHelper.containsNonWhitespace(item.getAssessmentType())) {
			assessmentTypeEl.select(item.getAssessmentType(), true);
		}
		
		buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_sel_qpool_metadata_buttons");
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
	}

	private void buildTaxonomyLevelEl() {
		qpoolTaxonomyTreeBuilder.loadTaxonomyLevelsSelection(getTranslator(), getIdentity(), securityCallback.canRemoveTaxonomy(), ignoreCompetences);
		String[] selectableKeys = qpoolTaxonomyTreeBuilder.getSelectableKeys();
		String[] selectableValues = qpoolTaxonomyTreeBuilder.getSelectableValues();
		taxonomyLevelEl.setKeysAndValues(selectableKeys, selectableValues, null);

		TaxonomyLevel selectedTaxonomyLevel = item.getTaxonomyLevel();
		if(selectedTaxonomyLevel != null) {
			String selectedTaxonomyLevelKey = String.valueOf(selectedTaxonomyLevel.getKey());
			for(String taxonomyKey: qpoolTaxonomyTreeBuilder.getSelectableKeys()) {
				if(taxonomyKey.equals(selectedTaxonomyLevelKey)) {
					taxonomyLevelEl.select(taxonomyKey, true);
				}
			}
			if (!taxonomyLevelEl.isOneSelected() && selectedTaxonomyLevel != null) {
				if (selectableKeys.length == 0) {
					selectableKeys = new String[] {"dummy"};
					selectableValues = new String[1];
				}
				selectableValues[0] = TaxonomyUIFactory.translateDisplayName(getTranslator(), selectedTaxonomyLevel);
				taxonomyLevelEl.setEnabled(false);
			}
		}

		taxonomyLevelEl.addActionListener(FormEvent.ONCHANGE);
		setTaxonomicPath();
	}

	private void setReadOnly() {
		boolean canEditMetadata = securityCallback.canEditMetadata();
		topicEl.setEnabled(canEditMetadata);
		taxonomyLevelEl.setEnabled(canEditMetadata);
		contextEl.setEnabled(canEditMetadata);
		keywordsEl.setEnabled(canEditMetadata);
		coverageEl.setEnabled(canEditMetadata);
		addInfosEl.setEnabled(canEditMetadata);
		languageEl.setEnabled(canEditMetadata);
		assessmentTypeEl.setEnabled(canEditMetadata);
		buttonsCont.setVisible(canEditMetadata);
	}

	public void setItem(QuestionItem item, MetadataSecurityCallback securityCallback) {
		this.item = item;
		this.securityCallback = securityCallback;
		if (securityCallback != null) {
			buildTaxonomyLevelEl();
			setReadOnly();
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (taxonomyLevelEl == source) {
			setTaxonomicPath();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void setTaxonomicPath() {
		String selectedKey = taxonomyLevelEl.isOneSelected()? taxonomyLevelEl.getSelectedKey(): null;
		TaxonomyLevel taxonomyLevel = qpoolTaxonomyTreeBuilder.getTaxonomyLevel(selectedKey);
		String taxonomicPath = "";
		if (taxonomyLevel != null) {
			taxonomicPath = taxonomyLevel.getMaterializedPathIdentifiers();
		}
		taxonomyLevelEl.setExampleKey("general.taxonomy.path", new String[] {taxonomicPath});
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateElementLogic(topicEl, topicEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(keywordsEl, keywordsEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(coverageEl, coverageEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(addInfosEl, addInfosEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(languageEl, languageEl.getMaxLength(), false, true);
		return allOk && super.validateFormLogic(ureq);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemEditable) {
			QuestionItemEditable itemImpl = (QuestionItemEditable)item;
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.UPDATE_QUESTION_ITEM_METADATA);
			if(item instanceof QuestionItemImpl) {
				builder.withBefore(item);
			}
			
			itemImpl.setTopic(topicEl.getValue());
			
			if (taxonomyLevelEl.isOneSelected()) {
				String selectedKey = taxonomyLevelEl.getSelectedKey();
				TaxonomyLevel taxonomyLevel = qpoolTaxonomyTreeBuilder.getTaxonomyLevel(selectedKey);
				itemImpl.setTaxonomyLevel(taxonomyLevel);
			} else {
				itemImpl.setTaxonomyLevel(null);
			}
	
			QEducationalContext context = contextEl.isOneSelected()
					? MetaUIFactory.getContextByKey(contextEl.getSelectedKey(), qpoolService)
					: null;
			itemImpl.setEducationalContext(context);
			
			if(StringHelper.containsNonWhitespace(keywordsEl.getValue())) {
				itemImpl.setKeywords(keywordsEl.getValue());
			} else {
				itemImpl.setKeywords("");
			}
			
			if(StringHelper.containsNonWhitespace(coverageEl.getValue())) {
				itemImpl.setCoverage(coverageEl.getValue());
			} else {
				itemImpl.setCoverage("");
			}
			
			if(StringHelper.containsNonWhitespace(addInfosEl.getValue())) {
				itemImpl.setAdditionalInformations(addInfosEl.getValue());
			} else {
				itemImpl.setAdditionalInformations(null);
			}
			
			itemImpl.setLanguage(languageEl.getValue());
			
			String assessmentType = assessmentTypeEl.isOneSelected()? assessmentTypeEl.getSelectedKey(): null;
			itemImpl.setAssessmentType(assessmentType);

			if(item instanceof QuestionItemImpl) {
				item = qpoolService.updateItem(item);
				builder.withAfter(item);
				qpoolService.persist(builder.create());
			}
			fireEvent(ureq, new QItemEdited(item));
		}
	}

	@Override
	protected void formResetted(UserRequest ureq) {
		buildTaxonomyLevelEl();
		super.formResetted(ureq);
	}

}