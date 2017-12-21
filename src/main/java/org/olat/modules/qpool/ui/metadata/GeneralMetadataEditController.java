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
import static org.olat.modules.qpool.ui.metadata.MetaUIFactory.validateSelection;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.model.QEducationalContext;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.tree.QPoolTaxonomyTreeBuilder;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GeneralMetadataEditController extends FormBasicController {

	private static final String NO_KEY = "noKey";
	
	private TextElement topicEl;
	private SingleSelection taxonomyLevelEl;
	private SingleSelection contextEl;
	private TextElement keywordsEl;
	private TextElement coverageEl;
	private TextElement addInfosEl;
	private TextElement languageEl;
	private SingleSelection assessmentTypeEl;
	private FormLayoutContainer buttonsCont;

	private QuestionItem item;

	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private QPoolTaxonomyTreeBuilder qpoolTaxonomyTreeBuilder;
	
	public GeneralMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item,
			QuestionItemSecurityCallback securityCallback) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		
		initForm(ureq);
		setItem(item, securityCallback);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String topic = item.getTopic();
		topicEl = uifactory.addTextElement("general.topic", "general.topic", 1000, topic, formLayout);
		
		taxonomyLevelEl = uifactory.addDropdownSingleselect("process.start.review.taxonomy.level", formLayout,
				new String[0], new String[0]);
		
		List<QEducationalContext> levels = qpoolService.getAllEducationlContexts();
		String[] contextKeys = new String[ levels.size() + 1 ];
		String[] contextValues = new String[ levels.size() + 1];
		int count = 1;
		for(QEducationalContext level:levels) {
			contextKeys[count] = level.getLevel();
			String translation = translate("item.level." + level.getLevel().toLowerCase());
			if(translation.length() > 128) {
				translation = level.getLevel();
			}
			contextValues[count++] = translation;
		}
		if (count > 1) {
			contextKeys[0] = NO_KEY;
			contextValues[0] = "-";
		}
		contextEl = uifactory.addDropdownSingleselect("educational.context", "educational.context", formLayout,
				contextKeys, contextValues, null);
		contextEl.setEnabled(count > 1);
		if (StringHelper.containsNonWhitespace(item.getEducationalContextLevel())) {
			contextEl.select(item.getEducationalContextLevel(), true);
		}
		
		String keywords = item.getKeywords();
		keywordsEl = uifactory.addTextElement("general.keywords", "general.keywords", 1000, keywords, formLayout);
		
		String addInfos = item.getAdditionalInformations();
		addInfosEl = uifactory.addTextElement("general.additional.informations", "general.additional.informations", 256,
				addInfos, formLayout);
		
		String coverage = item.getCoverage();
		coverageEl = uifactory.addTextElement("general.coverage", "general.coverage", 1000, coverage, formLayout);

		String language = item.getLanguage();
		languageEl = uifactory.addTextElement("general.language", "general.language", 10, language, formLayout);
		
		String[] assessmentTypeKeys = new String[]{ NO_KEY, "summative", "formative", "both"};
		String[] assessmentTypeValues = new String[]{ "-",
			translate("question.assessmentType.summative"), translate("question.assessmentType.formative"),
			translate("question.assessmentType.both"),	
		};
		assessmentTypeEl = uifactory.addDropdownSingleselect("question.assessmentType", "question.assessmentType",
				formLayout, assessmentTypeKeys, assessmentTypeValues, null);
		if(StringHelper.containsNonWhitespace(item.getAssessmentType())) {
			assessmentTypeEl.select(item.getAssessmentType(), true);
		}
		
		buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void buildTaxonomyLevelEl(QuestionItemSecurityCallback securityCallback) {
		System.out.println(securityCallback.canRemoveTaxonomy());
		qpoolTaxonomyTreeBuilder.loadTaxonomyLevelsSelection(getIdentity(), securityCallback.canRemoveTaxonomy());
		String[] selectableKeys = qpoolTaxonomyTreeBuilder.getSelectableKeys();
		String[] selectableValues = qpoolTaxonomyTreeBuilder.getSelectableValues();
		taxonomyLevelEl.setKeysAndValues(selectableKeys, selectableValues, null);
		if (item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl) item;
			TaxonomyLevel selectedTaxonomyLevel = itemImpl.getTaxonomyLevel();
			if(selectedTaxonomyLevel != null) {
				String selectedTaxonomyLevelKey = String.valueOf(selectedTaxonomyLevel.getKey());
				for(String taxonomyKey: qpoolTaxonomyTreeBuilder.getSelectableKeys()) {
					if(taxonomyKey.equals(selectedTaxonomyLevelKey)) {
						taxonomyLevelEl.select(taxonomyKey, true);
					}
				}
				if (!taxonomyLevelEl.isOneSelected()) {
					selectableValues[0] = ((QuestionItemImpl) item).getTaxonomyLevel().getDisplayName();
					taxonomyLevelEl.setEnabled(false);
				}
			}
		}
	}

	private void setReadOnly(QuestionItemSecurityCallback securityCallback) {
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

	public void setItem(QuestionItem item, QuestionItemSecurityCallback securityCallback) {
		this.item = item;
		if (securityCallback != null) {
			buildTaxonomyLevelEl(securityCallback);
			setReadOnly(securityCallback);
		}
	}
		
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= validateElementLogic(topicEl, topicEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(keywordsEl, keywordsEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(coverageEl, coverageEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(addInfosEl, addInfosEl.getMaxLength(), false, true);
		allOk &= validateElementLogic(languageEl, languageEl.getMaxLength(), true, true);
		allOk &= validateSelection(assessmentTypeEl, true);
		return allOk && super.validateFormLogic(ureq);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			itemImpl.setTopic(topicEl.getValue());
			
			String selectedKey = taxonomyLevelEl.getSelectedKey();
			TaxonomyLevel taxonomyLevel = qpoolTaxonomyTreeBuilder.getTaxonomyLevel(selectedKey);
			if(taxonomyLevel != null) {
				itemImpl.setTaxonomyLevel(taxonomyLevel);
			}
	
			QEducationalContext context = contextEl.isOneSelected() && !NO_KEY.equals(contextEl.getSelectedKey())
					? qpoolService.getEducationlContextByLevel(contextEl.getSelectedKey())
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
			
			String assessmentType = assessmentTypeEl.isOneSelected() && !NO_KEY.equals(assessmentTypeEl.getSelectedKey())
					? assessmentTypeEl.getSelectedKey()
					: null;
			itemImpl.setAssessmentType(assessmentType);
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}

}