/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.DocumentType;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.DecisionRubricDefinition;
import org.olat.modules.selectus.model.PolicyLink;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionAttributeDefinition;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 26 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyPositionConfigurationController extends FormBasicController {

	private TextElement idElement;
	private MultipleSelectionElement availableLanguageEls;
	private List<TextElement> posTitleLanguagesEl = new ArrayList<>(2);
	private List<TextElement> shortTitleLanguagesEl = new ArrayList<>(2);
	
	private DateChooser applicationDeadlineEl;
	private DateChooser ratingDeadlineEl;
	private DateChooser committeeReminderEl;
	private DateChooser refereeSubmissionDeadlineEl;
	private DateChooser refereeApplicantManagementDeadlineEl;
	private DateChooser expertSubmissionDeadlineEl;
	private DateChooser comparativeExpertSubmissionDeadlineEl;
	private DateChooser feedbackDeadlineEl;
	private DateChooser publicFeedbackDeadlineEl;
	

	private final Locale[] positionLanguages;
	private final String[] positionLanguagesKeys;
	private final String[] positionLanguagesValues;
	
	private Position positionToCopy;
	private Position newPosition;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private MailService mailService;
	@Autowired
	private AuditService auditService;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public CopyPositionConfigurationController(UserRequest ureq, WindowControl wControl, Position positionToCopy) {
		super(ureq, wControl, Util.createPackageTranslator(RecruitingHelper.class, ureq.getLocale()));
		this.positionToCopy = positionToCopy;
		
		positionLanguages = recruitingModule.getPositionLocales();
		positionLanguagesKeys = new String[positionLanguages.length];
		for(int i=positionLanguages.length; i-->0; ) {
			positionLanguagesKeys[i] = positionLanguages[i].getLanguage();
		}
		positionLanguagesValues = new String[positionLanguages.length];
		for(int i=positionLanguages.length; i-->0; ) {
			positionLanguagesValues[i] = positionLanguages[i].getDisplayLanguage(getLocale());
		}
		
		initForm(ureq);
		updateAvailableMultiLanguagesFields();
	}
	
	public Position getNewPosition() {
		return newPosition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		availableLanguageEls = uifactory.addCheckboxesHorizontal("position.languages", formLayout, positionLanguagesKeys, positionLanguagesValues);
		availableLanguageEls.setElementCssClass("o_sel_position_languages");
		availableLanguageEls.setVisible(positionLanguages.length > 1);
		availableLanguageEls.addActionListener(FormEvent.ONCHANGE);
		
		String availableLanguages = positionToCopy.getAvailableLanguages();
		if(StringHelper.containsNonWhitespace(availableLanguages) && !"-".equals(availableLanguages)) {
			String[] availableLanguageArr = positionToCopy.getAvailableLanguagesArray();
			for(int i=0; i<positionLanguagesKeys.length; i++) {
				for(int j=0; j<availableLanguageArr.length; j++) {
					if(positionLanguagesKeys[i].equals(availableLanguageArr[j])) {
						availableLanguageEls.select(positionLanguagesKeys[i], true);
					}
				}
			}
		} else {
			for(int i=0; i<positionLanguagesKeys.length; i++) {
				availableLanguageEls.select(positionLanguagesKeys[i], true);
			}
		}
		
		boolean focus = false;
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			TextElement posTitleEl = uifactory.addTextElement("position_title_" + lang, "edit.position_title", 256, "", formLayout);
			posTitleEl.setMandatory(true);
			posTitleEl.setUserObject(locale);
			if(!focus) {
				posTitleEl.setFocus(focus);
				focus = true;
			}
			if(positionLanguages.length > 1) {
				posTitleEl.setLabel("edit.position_title_ml", new String[]{ lang });
				posTitleEl.setElementCssClass("o_sel_position_title_" + lang);
			} else {
				posTitleEl.setElementCssClass("o_sel_position_title");
			}
			posTitleLanguagesEl.add(posTitleEl);
		}

		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			TextElement shortTitleEl = uifactory.addTextElement("short_title_" + lang, "edit.short_title", 256, "", formLayout);
			shortTitleEl.setMandatory(true);
			shortTitleEl.setUserObject(locale);
			if(positionLanguages.length > 1) {
				shortTitleEl.setLabel("edit.short_title_ml", new String[]{ lang });
				shortTitleEl.setElementCssClass("o_sel_position_shorttitle_" + lang);
			} else {
				shortTitleEl.setElementCssClass("o_sel_position_shorttitle");
			}
			shortTitleLanguagesEl.add(shortTitleEl);
		}
		
		idElement = uifactory.addTextElement("position_id", "edit.position_id", 32, "", formLayout);
		idElement.setMandatory(!recruitingModule.isPositionPlannigIdOptional());
		idElement.setVisible(recruitingModule.isPositionPlannigIdEnabled());
		idElement.setElementCssClass("o_sel_position_id");
		
		applicationDeadlineEl = uifactory.addDateChooser("copy.application.deadline", "copy.application.deadline", null, formLayout);
		
		ratingDeadlineEl = uifactory.addDateChooser("copy.rating.deadline", "copy.rating.deadline", null, formLayout);
		ratingDeadlineEl.setDateChooserTimeEnabled(true);
		//TODO selectus ratingDeadlineEl.setShowCet(true);
		
		committeeReminderEl = uifactory.addDateChooser("copy.committee.reminder", "copy.committee.reminder", null, formLayout);
		committeeReminderEl.setVisible(positionToCopy.getCommitteeReminderDate() != null);
		committeeReminderEl.setMandatory(committeeReminderEl.isVisible());
		
		refereeSubmissionDeadlineEl = uifactory.addDateChooser("copy.referee.deadline", "copy.referee.deadline", null, formLayout);
		refereeSubmissionDeadlineEl.setVisible(recruitingModule.isReferenceEnabled() && positionToCopy.isRefereeRecommendationEnabled());
		refereeSubmissionDeadlineEl.setMandatory(refereeSubmissionDeadlineEl.isVisible());
		
		refereeApplicantManagementDeadlineEl = uifactory.addDateChooser("copy.app.deadline", "copy.referee.applicant.deadline", null, formLayout);
		refereeApplicantManagementDeadlineEl.setVisible(recruitingModule.isReferenceApplicantManagement() && positionToCopy.isApplicantRefereeManagementEnabled());
		refereeApplicantManagementDeadlineEl.setMandatory(refereeApplicantManagementDeadlineEl.isVisible());

		expertSubmissionDeadlineEl = uifactory.addDateChooser("copy.expert.deadline", "copy.expert.deadline", null, formLayout);
		expertSubmissionDeadlineEl.setVisible(recruitingModule.isReferenceEnabled() && positionToCopy.isExpertRecommendationEnabled());
		expertSubmissionDeadlineEl.setMandatory(expertSubmissionDeadlineEl.isVisible());
		
		comparativeExpertSubmissionDeadlineEl = uifactory.addDateChooser("copy.comparative.expert.deadline", "copy.comparative.expert.deadline", null, formLayout);
		comparativeExpertSubmissionDeadlineEl.setVisible(recruitingModule.isReferenceEnabled() && positionToCopy.isComparativeAssessmentExpertEnabled());
		comparativeExpertSubmissionDeadlineEl.setMandatory(comparativeExpertSubmissionDeadlineEl.isVisible());
		
		feedbackDeadlineEl = uifactory.addDateChooser("copy.feedback.deadline", "copy.feedback.deadline", null, formLayout);
		feedbackDeadlineEl.setVisible(hasFeedbacks());
		feedbackDeadlineEl.setMandatory(feedbackDeadlineEl.isVisible());
		
		publicFeedbackDeadlineEl = uifactory.addDateChooser("copy.public.feedback.deadline", "copy.public.feedback.deadline", null, formLayout);
		publicFeedbackDeadlineEl.setVisible(recruitingModule.isPublicFeedbackEnabled() && positionToCopy.isPublicFeedbackEnabled());
		publicFeedbackDeadlineEl.setMandatory(publicFeedbackDeadlineEl.isVisible());
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private boolean hasFeedbacks() {
		List<ApplicationsFeedbackConfiguration> feedbackConfigurations = feedbackService
				.getApplicationsFeedbackConfigurations(positionToCopy);
		for(ApplicationsFeedbackConfiguration feedbackConfiguration:feedbackConfigurations) {
			if(feedbackConfiguration.isEnabled()) {
				return true;
			}
		}
		return false;
	}
	
	private Set<Locale> getSelectedLocale() {
		Collection<String> availableLanguages = availableLanguageEls.getSelectedKeys();
		Set<Locale> availableLocales = new HashSet<>();
		for(int i=positionLanguages.length; i-->0; ) {
			if(availableLanguages.contains(positionLanguages[i].getLanguage())) {
				availableLocales.add(positionLanguages[i]);
			}
		}
		return availableLocales;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(TextElement titleEl:posTitleLanguagesEl) {
			allOk &= RecruitingHelper.validateTextElement(titleEl, 255, true, new OWASPAntiSamyXSSFilter());
		}
		for(TextElement titleEl:shortTitleLanguagesEl) {
			allOk &= RecruitingHelper.validateTextElement(titleEl, 255, true, new OWASPAntiSamyXSSFilter());
		}
		
		allOk &= RecruitingHelper.validateTextElement(idElement, 255, true, new OWASPAntiSamyXSSFilter());
		
		allOk &= RecruitingHelper.validateDateChooser(committeeReminderEl);
		allOk &= RecruitingHelper.validateDateChooser(refereeSubmissionDeadlineEl);
		allOk &= RecruitingHelper.validateDateChooser(refereeApplicantManagementDeadlineEl);
		allOk &= RecruitingHelper.validateDateChooser(expertSubmissionDeadlineEl);
		allOk &= RecruitingHelper.validateDateChooser(comparativeExpertSubmissionDeadlineEl);
		allOk &= RecruitingHelper.validateDateChooser(feedbackDeadlineEl);
		allOk &= RecruitingHelper.validateDateChooser(publicFeedbackDeadlineEl);
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(availableLanguageEls == source) {
			updateAvailableMultiLanguagesFields();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateAvailableMultiLanguagesFields() {
		Set<Locale> availableLocales = getSelectedLocale();
		if(availableLocales.isEmpty() || positionLanguages.length == 1) {
			Locale defaultLocale = recruitingModule.getPositionDefaultLocale();
			availableLocales = Collections.singleton(defaultLocale);
		}
		
		for(TextElement mlEl:posTitleLanguagesEl) {
			Locale locale = (Locale)mlEl.getUserObject();
			String lang = locale.getLanguage();
			
			mlEl.setVisible(availableLocales.contains(locale));
			mlEl.setMandatory(availableLocales.contains(locale));
			mlEl.setElementCssClass("o_sel_position_title_" + lang);
			mlEl.setLabel("edit.position_title_ml", new String[]{ lang });
		}
		for(TextElement mlEl:shortTitleLanguagesEl) {
			Locale locale = (Locale)mlEl.getUserObject();
			String lang = locale.getLanguage();

			mlEl.setVisible(availableLocales.contains(locale));
			mlEl.setMandatory(availableLocales.contains(locale));
			mlEl.setLabel("edit.short_title_ml", new String[]{ lang });
			mlEl.setElementCssClass("o_sel_position_shorttitle_" + lang);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		positionToCopy = recruitingService.getPosition(positionToCopy.getKey());
		
		Position position = recruitingService.createPosition(positionToCopy.getOrganisation());
		doCommitFormValues(position);
		doCopyPositionValues(position);
		doCopyCustomTabs(position);
		
		newPosition = recruitingService.savePosition(position);
		dbInstance.commit();
		
		Map<Long,Long> originalToCopyAttributesKeys = doCopyAdditionalAttributes(newPosition);
		doCopyFacultyFeedback(newPosition, originalToCopyAttributesKeys);
		newPosition = recruitingService.savePosition(newPosition);
		dbInstance.commit();
		
		doCopyReview(newPosition);
		doCopyDecisionTool(newPosition);
		doCopyTags(newPosition);
		doCopyMailTemplates(newPosition);
		doCopyProfileVisibility(newPosition, originalToCopyAttributesKeys);
		
		newPosition = recruitingService.savePosition(newPosition);
		dbInstance.commit();
		
		doCopyAttachments(newPosition);
		newPosition = recruitingService.savePosition(newPosition);
		dbInstance.commit();
		
		String after = auditService.toAuditXml(newPosition);
		String messageI18n = "audit.log.position.copy.configuration.from";
		Locale positionDefLocale = recruitingModule.getPositionDefaultLocale();	
		String[] messageArgs = new String[] {
			newPosition.getMLTitle(positionDefLocale),
			positionToCopy.getMLTitle(positionDefLocale),
		};
		auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, null, after,
				messageI18n, messageArgs, getTranslator(), position, getIdentity());

		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doCopyAttachments(Position position) {
		position.setDocument1(doCopyAttachment(positionToCopy.getDocument1()));
		position.setDocument2(doCopyAttachment(positionToCopy.getDocument2()));
		position.setDocument3(doCopyAttachment(positionToCopy.getDocument3()));
	}
	
	private Attachment doCopyAttachment(Attachment attachment) {
		if(attachment == null) return null;
		
		byte[] data = recruitingService.getAttachmentDatas(attachment);
		return recruitingService.setAttachmentDatas(null, attachment.getName(), DocumentType.pdf, data);
	}
	
	private void doCopyMailTemplates(Position position) {
		List<PositionMailTemplate> customTemplates = mailService.getTemplates(positionToCopy);
		for(PositionMailTemplate customTemplate:customTemplates) {
			PositionMailTemplate copy = mailService.createTemplate(position, customTemplate.getId(), customTemplate.getName());
			copy.setSubject(customTemplate.getSubject(), Locale.ENGLISH);
			copy.setSubject(customTemplate.getSubjectDe(), Locale.GERMAN);
			copy.setBody(customTemplate.getBody(), Locale.ENGLISH);
			copy.setBody(customTemplate.getBodyDe(), Locale.GERMAN);
			copy.setLetter(customTemplate.getLetter());
			mailService.updateTemplate(copy);
		}	
	}
	
	private void doCopyTags(Position position) {
		List<Category> categories = taggingService.getPositionCategories(positionToCopy);
		for(Category category:categories) {
			if(category.getPosition() != null) {
				taggingService.createCategory(category.getName(), category.getColor(), position);
			}
		}
	}
	
	private void doCopyFacultyFeedback(Position position, Map<Long,Long> originalToCopyAttributesKeys) {
		Date feedbackDeadline = getDate(feedbackDeadlineEl);
		if(feedbackDeadline == null) return;
		
		List<ApplicationsFeedbackConfiguration> feedbackConfigurations = feedbackService
				.getApplicationsFeedbackConfigurations(positionToCopy);
		if(feedbackConfigurations.isEmpty()) return;
		
		Map<String, ApplicationsFeedbackConfiguration> configsMap = feedbackConfigurations.stream().collect(Collectors
				.toMap(ApplicationsFeedbackConfiguration::getConfigurationName, c -> c, (u, v) -> u));

		String defaultConfigurationName = translate("edit.apps.feedback.default.configuration");
		List<ApplicationsFeedbackConfiguration> copyList = feedbackService
				.getOrCreateApplicationsFeedbackConfigurations(defaultConfigurationName, position);
		for(ApplicationsFeedbackConfiguration copy:copyList) {
			ApplicationsFeedbackConfiguration config = configsMap.get(copy.getConfigurationName());
			if(config != null) {
				copy.setDeadline(feedbackDeadlineEl.getDate());
				copy.setDocuments(config.getDocuments());
				copy.setEnabled(config.isEnabled());
				copy.setExpertsDocs(config.isExpertsDocs());
				copy.setRefereesDocs(config.isRefereesDocs());
				copy.setExpertsComparativeAssessmentDocs(config.isExpertsComparativeAssessmentDocs());
				copy.setMailSubject(config.getMailSubject());
				copy.setMailTemplate(config.getMailTemplate());
				copy.setFields(copyProfileVisibilityFields(config.getFields(), originalToCopyAttributesKeys));
				feedbackService.updateApplicationsFeedbackConfiguration(copy);
			}
		}
	}
	
	private void doCopyProfileVisibility(Position position, Map<Long,Long> originalToCopyAttributesKeys) {
		position.setExpertRecommendationFields(copyProfileVisibilityFields(positionToCopy.getExpertRecommendationFields(), originalToCopyAttributesKeys));
		position.setRefereeRecommendationFields(copyProfileVisibilityFields(positionToCopy.getRefereeRecommendationFields(), originalToCopyAttributesKeys));
		position.setComparativeAssessmentExpertFields(copyProfileVisibilityFields(positionToCopy.getComparativeAssessmentExpertFields(), originalToCopyAttributesKeys));
	}
	
	private Set<String> copyProfileVisibilityFields(Set<String> fields, Map<Long,Long> originalToCopyAttributesKeys) {
		Set<String> fieldsCopy = new HashSet<>();
		for(String field:fields) {
			if(field.startsWith(RecruitingModule.APP_CUSTOM_FIELD_PREFIX)) {
				String key = field.substring(RecruitingModule.APP_CUSTOM_FIELD_PREFIX.length());
				Long newKey = originalToCopyAttributesKeys.get(Long.valueOf(key));
				if(newKey == null) {
					continue;
				}
				field = RecruitingModule.APP_CUSTOM_FIELD_PREFIX + newKey;
			}
			fieldsCopy.add(field);
		}
		return fieldsCopy;
	}
	
	private void doCopyDecisionTool(Position position) {
		if(!positionToCopy.isDecisionTool()) return;
		
		position.setDecisionTool(positionToCopy.isDecisionTool());

		List<DecisionRubricDefinition> definitions = recruitingService.getDecisionRubricDefinition(positionToCopy);
		for(int i=0; i<definitions.size(); i++) {
			DecisionRubricDefinition definition = definitions.get(i);
			DecisionRubricDefinition copy = recruitingService.createDecisionRubricDefinition();
			copy.setPos(i);
			copy.setRubric(definition.getRubric());
			copy.setSum(definition.isSum());
			copy.setType(definition.getType());
			copy.setWeight(definition.getWeight());
			recruitingService.saveDecisionRubricDefinition(copy, position);
		}
	}
	
	private void doCopyReview(Position position) {
		if(!positionToCopy.isReviewEnabled()) return;
		
		position.setReviewEnabled(positionToCopy.isReviewEnabled());
		PositionReviewDefinition config = positionToCopy.getReviewDefinition();
		
		PositionReviewDefinition copy = reviewService.createReviewDefinition();
		copy.setReviewCommentEnabled(config.isReviewCommentEnabled());
		
		copy.setReviewNameVisibility(config.getReviewNameVisibility());
		copy.setReviewVisibilityCommittee(config.getReviewVisibilityCommittee());
		copy.setReviewFillCommittee(config.getReviewFillCommittee());
		copy.setReviewVisibilityHead(config.getReviewVisibilityHead());
		copy.setReviewFillHead(config.getReviewFillHead());
		copy.setReviewVisibilitySecretary(config.getReviewVisibilitySecretary());
		copy.setReviewFillSecretary(config.getReviewFillSecretary());
		copy.setReviewVisibilityExofficio(config.getReviewVisibilityExofficio());
		copy.setReviewFillExofficio(config.getReviewFillExofficio());
		
		copy.setDefaultSliderSteps(config.getDefaultSliderSteps());
		copy.setDefaultSliderLeftLabel(config.getDefaultSliderLeftLabel());
		copy.setDefaultSliderRightLabel(config.getDefaultSliderRightLabel());
		
		copy.setReviewStatisticsEnabled(config.getReviewStatisticsEnabled());
		copy.setReviewRadarChartEnabled(config.getReviewRadarChartEnabled());
		
		copy = reviewService.saveReviewDefinition(copy);
		position.setReviewDefinition(copy);
		
		List<ReviewElementDefinition> elements = config.getElements();
		for(ReviewElementDefinition element:elements) {
			if(element == null) continue;
			
			ReviewElementDefinition elementCopy = reviewService.createReviewElement(copy, element.getType())
					.reviewElementDefinition();
			elementCopy.setLabel(element.getLabel());
			reviewService.saveReviewElement(elementCopy);
		}
	}
	
	private Map<Long,Long> doCopyAdditionalAttributes(Position position) {
		List<PositionAttributeDefinition> attributeDefinitions = positionToCopy.getAttributesDefinitions();
		
		Map<Long,Long> originalToCopyMap = new HashMap<>();
		for(PositionAttributeDefinition attributeDefinition:attributeDefinitions) {
			PositionAttributeDefinition attr = recruitingService.createAttributeDefinition(position,
					attributeDefinition.getTabEnum(), attributeDefinition.getTypeEnum(),
					attributeDefinition.getLabel(Locale.ENGLISH), attributeDefinition.getLabel(Locale.GERMAN),
					attributeDefinition.isMandatory(), 
					attributeDefinition.getPlaceholder(Locale.ENGLISH), attributeDefinition.getPlaceholder(Locale.GERMAN));
			attr.setAttributeConfiguration(attributeDefinition.getAttributeConfiguration());
			recruitingService.persistAttributeDefinition(attr);
			position.getAttributesDefinitions().add(attr);
			originalToCopyMap.put(attributeDefinition.getKey(), attr.getKey());
		}
		
		return originalToCopyMap;
	}
	
	private void doCopyCustomTabs(Position position) {
		List<Tab> customTabs = positionToCopy.getCustomTabsList();
		position.setCustomTabsList(customTabs);
		if(customTabs != null && !customTabs.isEmpty()) {
			for(Tab customTab:customTabs) {
				TabConfiguration config = positionToCopy.getTabConfiguration(customTab);
				position.setTabConfiguration(customTab, config);
			}
		}
	}
	
	private void doCopyPositionValues(Position position) {
		position.setDepartment(positionToCopy.getDepartment());
		position.setDepartmentDe(positionToCopy.getDepartmentDe());
		
		position.setDescription(positionToCopy.getDescription());
		position.setDescriptionDe(positionToCopy.getDescriptionDe());
		
		position.setMessageToCommitte(positionToCopy.getMessageToCommitte());
		
		position.setHomepage(positionToCopy.getHomepage());

		position.setCommitteeReminderMailSubject(positionToCopy.getCommitteeReminderMailSubject());
		position.setCommitteeReminderMailTemplate(positionToCopy.getCommitteeReminderMailTemplate());
		position.setCommitteeReminderMailLetter(positionToCopy.getCommitteeReminderMailLetter());
		
		position.setApplicationConfirmationMailTemplate(positionToCopy.getApplicationConfirmationMailTemplate());
		position.setApplicationConfirmationMailTemplateDe(positionToCopy.getApplicationConfirmationMailTemplateDe());
		position.setApplicationConfirmationMailLetter(positionToCopy.getApplicationConfirmationMailLetter());
		
		position.setApplicationConfirmationDuplicateMailTemplate(positionToCopy.getApplicationConfirmationDuplicateMailTemplate());
		position.setApplicationConfirmationDuplicateMailTemplateDe(positionToCopy.getApplicationConfirmationDuplicateMailTemplateDe());
		position.setApplicationConfirmationDuplicateMailLetter(positionToCopy.getApplicationConfirmationDuplicateMailLetter());
		
		position.setProfessorship(positionToCopy.getProfessorship());

		position.setJobAds(positionToCopy.getJobAds());
		
		position.setMailSetting(positionToCopy.getMailSetting());
		position.setSenderMail(positionToCopy.getSenderMail());
		position.setBccMail(positionToCopy.getBccMail());

		position.setDecisionTool(positionToCopy.isDecisionTool());

		position.setApplicationProject(positionToCopy.isApplicationProject());
		position.setApplicationAcademicalBackground(positionToCopy.isApplicationAcademicalBackground());
		
		position.setAvailableDocuments(positionToCopy.getAvailableDocuments());
		position.setPdfDocuments(positionToCopy.getPdfDocuments());
		position.setXlsxDocuments(positionToCopy.getXlsxDocuments());
		position.setDocxDocuments(positionToCopy.getDocxDocuments());
		position.setJpgDocuments(positionToCopy.getJpgDocuments());
		position.setMandatoryDocuments(positionToCopy.getMandatoryDocuments());
		position.setStaffDocuments(positionToCopy.getStaffDocuments());
		position.setDocumentsInCombinedFile(positionToCopy.getDocumentsInCombinedFile());
		position.setDocumentNames(positionToCopy.getDocumentNames());
		position.setDocumentNamesDe(positionToCopy.getDocumentNamesDe());
		position.setDocumentExplain(positionToCopy.getDocumentExplain());
		position.setDocumentExplainDe(positionToCopy.getDocumentExplainDe());
		position.setDocumentSizes(positionToCopy.getDocumentSizes());
		
		position.setPolicyLink1(copyPolicyLink(positionToCopy.getPolicyLink1()));
		position.setPolicyLink2(copyPolicyLink(positionToCopy.getPolicyLink2()));
		position.setPolicyLink3(copyPolicyLink(positionToCopy.getPolicyLink3()));
		position.setPolicyLink4(copyPolicyLink(positionToCopy.getPolicyLink4()));

		position.setCommitteeCommentEnabled(positionToCopy.isCommitteeCommentEnabled());
		position.setCommitteeCommentVisiblity(positionToCopy.getCommitteeCommentVisiblity());
		
		position.setSystemTagsEnabled(positionToCopy.isSystemTagsEnabled());
		position.setPositionTagsEnabled(positionToCopy.isPositionTagsEnabled());
		
		position.setTabsConfiguration(positionToCopy.getTabsConfiguration());
	}
	
	private PolicyLink copyPolicyLink(PolicyLink link) {
		PolicyLink clone = null;
		if(link != null) {
			clone = new PolicyLink();
			clone.setLabel(link.getLabel());
			clone.setUrl(link.getUrl());
		}
		return clone;
	}
	
	private void doCommitFormValues(Position position) {
		Collection<String> availableLanguages = availableLanguageEls.getSelectedKeys();
		StringBuilder availableLanguagesSb = new StringBuilder();
		for(String availableLanguage:availableLanguages) {
			if(availableLanguagesSb.length() > 0) availableLanguagesSb.append(",");
			availableLanguagesSb.append(availableLanguage);
		}
		if(availableLanguages.isEmpty()) {
			position.setAvailableLanguages("-");
		} else {
			position.setAvailableLanguages(availableLanguagesSb.toString());
		}
		
		for(TextElement titleEl:posTitleLanguagesEl) {
			if(titleEl.isVisible()) {
				Locale locale = (Locale)titleEl.getUserObject();
				position.setPositionTitle(titleEl.getValue(), locale);
			}
		}
		
		for(TextElement titleEl:shortTitleLanguagesEl) {
			if(titleEl.isVisible()) {
				Locale locale = (Locale)titleEl.getUserObject();
				position.setShortTitle(titleEl.getValue(), locale);
			}
		}

		position.setPlaningsNumber(idElement.getValue());
		position.setOrganisation(positionToCopy.getOrganisation());

		Date applicationDeadline = applicationDeadlineEl.getDate();
		position.setApplicationDeadline(applicationDeadline);
		Date ratingDeadline = ratingDeadlineEl.getDate();
		position.setRatingDeadline(ratingDeadline);
		Date committeeReminder = getDate(committeeReminderEl);
		position.setCommitteeReminderDate(committeeReminder);

		Date refereeSubmissionDeadline = getDate(refereeSubmissionDeadlineEl);
		if(refereeSubmissionDeadline != null) {
			position.setRefereeRecommendationEnabled(true);
			position.setRefereeRecommandationDeadline(refereeSubmissionDeadline);
			position.setRefereeRecommandationMailSubject(positionToCopy.getRefereeRecommandationMailSubject());
			position.setRefereeRecommandationMailTemplate(positionToCopy.getRefereeRecommandationMailTemplate());
			position.setRefereeRecommandationMailLetter(positionToCopy.getRefereeRecommandationMailLetter());
			position.setRefereeRecommandationSendMailType(positionToCopy.getRefereeRecommandationSendMailType());
			position.setRefereeRecommendationDocuments(positionToCopy.getRefereeRecommendationDocuments());
			position.setRefereeConfirmationSubmissionMailSubject(positionToCopy.getRefereeConfirmationSubmissionMailSubject());
			position.setRefereeConfirmationSubmissionMailTemplate(positionToCopy.getRefereeConfirmationSubmissionMailTemplate());
			position.setMinReferees(positionToCopy.getMinReferees());
			position.setMaxReferees(positionToCopy.getMaxReferees());
		}
		
		Date applicantRefereeMgmtDeadline = getDate(refereeApplicantManagementDeadlineEl);
		if(applicantRefereeMgmtDeadline != null) {
			position.setApplicantRefereeManagementEnabled(true);
			position.setApplicantRefereeManagementDeadline(applicantRefereeMgmtDeadline);
			position.setApplicationConfirmationWithRefereeManagementMailTemplate(positionToCopy.getApplicationConfirmationWithRefereeManagementMailTemplate());
			position.setApplicationConfirmationWithRefereeManagementMailTemplateDe(positionToCopy.getApplicationConfirmationWithRefereeManagementMailTemplateDe());
			position.setApplicationConfirmationWithRefereeManagementMailLetter(positionToCopy.getApplicationConfirmationWithRefereeManagementMailLetter());
		}
		
		Date expertSubmissionDeadline = getDate(expertSubmissionDeadlineEl);
		if(expertSubmissionDeadline != null) {
			position.setExpertRecommendationEnabled(true);
			position.setExpertRecommandationDeadline(expertSubmissionDeadline);
			position.setExpertRecommandationMailSubject(positionToCopy.getExpertRecommandationMailSubject());
			position.setExpertRecommandationMailTemplate(positionToCopy.getExpertRecommandationMailTemplate());
			position.setExpertRecommandationMailLetter(positionToCopy.getExpertRecommandationMailLetter());
			position.setExpertRecommendationDocuments(positionToCopy.getExpertRecommendationDocuments());
			position.setExpertConfirmationSubmissionMailSubject(positionToCopy.getExpertConfirmationSubmissionMailSubject());
			position.setExpertConfirmationSubmissionMailTemplate(positionToCopy.getExpertConfirmationSubmissionMailTemplate());
		}
		
		Date comparativeExpertSubmissionDeadline = getDate(comparativeExpertSubmissionDeadlineEl);
		if(comparativeExpertSubmissionDeadline != null) {
			position.setComparativeAssessmentExpertEnabled(true);
			position.setComparativeAssessmentExpertDeadline(comparativeExpertSubmissionDeadline);
			position.setComparativeAssessmentExpertMailSubject(positionToCopy.getComparativeAssessmentExpertMailSubject());
			position.setComparativeAssessmentExpertMailTemplate(positionToCopy.getComparativeAssessmentExpertMailTemplate());
			position.setComparativeAssessmentExpertMailLetter(positionToCopy.getComparativeAssessmentExpertMailLetter());
			position.setComparativeAssessmentExpertDocuments(positionToCopy.getComparativeAssessmentExpertDocuments());
			position.setComparativeAssessmentExpertConfirmationSubmissionMailSubject(positionToCopy.getComparativeAssessmentExpertConfirmationSubmissionMailSubject());
			position.setComparativeAssessmentExpertConfirmationSubmissionMailTemplate(positionToCopy.getComparativeAssessmentExpertConfirmationSubmissionMailTemplate());
		}

		Date publicFeedbackDeadline = getDate(publicFeedbackDeadlineEl);
		if(publicFeedbackDeadline != null) {
			position.setPublicFeedbackDeadline(publicFeedbackDeadline);
			position.setPublicFeedbackEnabled(true);
		}
	}
	
	private Date getDate(DateChooser el) {
		return el.isVisible() ? el.getDate() : null;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
