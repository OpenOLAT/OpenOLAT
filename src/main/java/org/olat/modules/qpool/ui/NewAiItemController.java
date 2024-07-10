/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool.ui;

import static org.olat.core.util.StringHelper.EMPTY;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.commons.services.ai.event.AiQuestionItemsCreatedEvent;
import org.olat.core.commons.services.ai.event.AiServiceFailedEvent;
import org.olat.core.commons.services.ai.model.AiMCQuestionData;
import org.olat.core.commons.services.ai.model.AiMCQuestionsResponse;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
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
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentItemFactory;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;
import org.olat.ims.qti21.pool.QTI21QPoolServiceProvider;
import org.olat.ims.qti21.questionimport.AssessmentItemAndMetadata;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;

/**
 * 
 * Choose the type of the new item and send an event
 * 
 * Initial date: 13.06.2023<br>
 * 
 * @author gnaegi@frentix.com, http://www.frentix.com
 *
 */
public class NewAiItemController extends FormBasicController {

	private TextElement contentEl;

	private final Set<TaxonomyLevel> allTaxonomyLevels;

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	protected QuestionPoolModule qpoolModule;
	@Autowired
	protected QPoolService qpoolService;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;

	@Autowired
	private AiSPI aiSPI;
	
	public NewAiItemController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback qPoolSecurityCallback, boolean ignoreCompetences) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		Taxonomy taxonomy = qpoolService.getQPoolTaxonomy();
		allTaxonomyLevels = new HashSet<>(taxonomyService.getTaxonomyLevels(taxonomy));

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_new_item_form");
		setFormWarning("warn.beta.feature");
		setFormDescription("ai.desc");

		contentEl = uifactory.addTextAreaElement("ai.content", 10, 100, "", formLayout);
		contentEl.setPlaceholderKey("ai.content.placeholder", null);
		contentEl.setNotLongerThanCheck(6000, "form.error.toolong");
		contentEl.setNotEmptyCheck();
		contentEl.setExampleKey("ai.max", new String[]{"6000"});
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("new.item", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String input = contentEl.getValue();
		
		int numberQuestions = Math.max(1, Math.round(input.length() / 500));
		AiMCQuestionsResponse response = aiSPI.generateMCQuestionsResponse(input, numberQuestions);
		
		if (response.isSuccess()) {
			// create all items in the document and fire event with item list to parent
			List<QuestionItem> questionItems = new ArrayList<>();

			for (AiMCQuestionData questionData : response.getQuestions()) {
				QuestionItem item = doCreateMCItem(ureq, questionData);
				if (item != null) {
					questionItems.add(item);					
				}				
			}
			if (questionItems.size() > 0) {
				fireEvent(ureq, new AiQuestionItemsCreatedEvent(questionItems));								
				return;
			}			
			
		} else {
			// something went wrong, print error and quit
			fireEvent(ureq, new AiServiceFailedEvent(StringHelper.xssScan(response.getError())));				
			return;
		}			
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private QuestionItem doCreateMCItem(UserRequest ureq, AiMCQuestionData itemData) {
		// 1) Create basic item using the builder in RAM
		String title = itemData.getTitle();
		String question = itemData.getQuestion();
		if (title == null || question == null) {
			return null;			
		}
		// question and scoring settings
		MultipleChoiceAssessmentItemBuilder mcItemBuilder = new MultipleChoiceAssessmentItemBuilder(StringHelper.xssScan(title), "New answer", qtiService.qtiSerializer());
		mcItemBuilder.setQuestion(StringHelper.xssScan(question));
		mcItemBuilder.clearSimpleChoices();
		mcItemBuilder.setShuffle(true);
		mcItemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		mcItemBuilder.setMaxScore(1d);
		mcItemBuilder.clearMapping();
		// create correct answers
		for (String correctValue : itemData.getCorrectAnswers()) {
			correctValue = StringHelper.xssScan(correctValue);
			ChoiceInteraction interaction = mcItemBuilder.getChoiceInteraction();
			SimpleChoice newChoice = AssessmentItemFactory.createSimpleChoice(interaction, correctValue,
					mcItemBuilder.getQuestionType().getPrefix());
			mcItemBuilder.addSimpleChoice(newChoice);
			mcItemBuilder.addCorrectAnswer(newChoice.getIdentifier());
		}
		// create the wrong answers
		for (String wrongValue : itemData.getWrongAnswers()) {
			wrongValue = StringHelper.xssScan(wrongValue);
			ChoiceInteraction interaction = mcItemBuilder.getChoiceInteraction();
			SimpleChoice newChoice = AssessmentItemFactory.createSimpleChoice(interaction, wrongValue,
					QTI21QuestionType.mc.getPrefix());
			mcItemBuilder.addSimpleChoice(newChoice);
		}		
		mcItemBuilder.build();
		
		// 2) Add metadata
		AssessmentItemAndMetadata metaItem = new AssessmentItemAndMetadata(mcItemBuilder);
		// Meta: topic
		String topic = itemData.getTopic();
		if (topic != null) {
			metaItem.setTopic(StringHelper.xssScan(topic));			
		}
		// Meta: keywords and taxonomy
		String keywords = itemData.getKeywords();
		if (keywords != null) {
			metaItem.setKeywords(StringHelper.xssScan(keywords));
		}
		String subject = itemData.getSubject();
		if (subject != null) {
			// Try mapping to a taxonomy
			TaxonomyLevel finalTaxonomy = null;
			Set<TaxonomyLevel> taxonomies = new HashSet<TaxonomyLevel>();
			taxonomies.addAll(searchTaxonomyLevels(subject));
			int currentLevel = 0;
			for (TaxonomyLevel taxLevel : taxonomies) {
				// use the most specific taxonomy if multiple have been found
				int level = StringUtils.countMatches(taxLevel.getMaterializedPathIdentifiers(), "/");
				if (level > currentLevel) {					
					finalTaxonomy = taxLevel;			
				}
			}
			if (finalTaxonomy == null) {
				// try fallback to keywords
				String[] keywordsArray = keywords.split(",");
				for (String keyword : keywordsArray) {
					keyword = keyword.trim();
					taxonomies.addAll(searchTaxonomyLevels(keyword));
				}		
				currentLevel = 0;
				for (TaxonomyLevel taxLevel : taxonomies) {
					// use the most specific taxonomy if multiple have been found
					int level = StringUtils.countMatches(taxLevel.getMaterializedPathIdentifiers(), "/");
					if (level > currentLevel) {					
						finalTaxonomy = taxLevel;			
					}
				}
			}
			if (finalTaxonomy != null) {
				metaItem.setTaxonomyPath(finalTaxonomy.getMaterializedPathIdentifiers());
			}
		}
		// Meta: used AI service and AI model
		metaItem.setEditor("OpenOlat.AI.QTI12.Generator." + aiSPI.getId());
		metaItem.setEditorVersion(aiSPI.getQuestionGenerationModel());
		
		// 3) Persist item in question pool using the Excel import SPI
		QTI21QPoolServiceProvider spi = CoreSpringFactory.getImpl(QTI21QPoolServiceProvider.class);
		QuestionItem importedItem = spi.importExcelItem(getIdentity(), metaItem, getLocale());
		QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(), Action.CREATE_QUESTION_ITEM_BY_IMPORT);
		builder.withAfter(importedItem);
		qpoolService.persist(builder.create());
		
		// 4: Set review status, but only if review process is not enabled		
		if( !qpoolModule.isReviewProcessEnabled() && importedItem instanceof QuestionItemImpl) { 
			QuestionItemImpl itemImpl = (QuestionItemImpl)importedItem;
			itemImpl.setQuestionStatus(QuestionStatus.review);
			qpoolService.updateItem(itemImpl);
		}
		
		// 5: Set default license
		if (licenseModule.isEnabled(licenseHandler)) {
			// The QItemFactory may create a no license as part of the import process.
			// But for new question items the default license should be created.
			// So delete the no license first, so that the default license can be created.
			licenseService.delete(importedItem);
			licenseService.createDefaultLicense(importedItem, licenseHandler, getIdentity());
		}

		return importedItem;
	}
	
	
	/** 
	 * Helper method to search for a taxonomy that matches the given keyword
	 * @param searchText the keyword to search for in the taxonomy
	 * @return
	 */
	private Set<TaxonomyLevel> searchTaxonomyLevels(String searchText) {
		String lowerSearchText = searchText.toLowerCase();
		return allTaxonomyLevels.stream()
				.filter(level -> searchTaxonomyLevel(level, lowerSearchText))
				.collect(Collectors.toSet());
	}

	/**
	 * Compare taxonomy level and search text using the translated taxonomy name
	 * @param taxonomylevel
	 * @param searchText
	 * @return
	 */
	private boolean searchTaxonomyLevel(TaxonomyLevel taxonomylevel, String searchText) {
		String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomylevel, EMPTY);
		return displayName.toLowerCase().indexOf(searchText) > -1;
	}
	
}