/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.mail;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.DefaultMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.letter.LetterConfiguration;
import org.olat.modules.selectus.model.letter.LetterConfigurationXStream;
import org.olat.modules.selectus.model.letter.LetterLanguageConfiguration;
import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;
import org.olat.modules.selectus.ui.position.PositionEditHelper;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 
 * Initial date: 5 avr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionLetterEditController extends FormBasicController {
	
	private TextElement titleEl;
	private Link variablesButton;
	private SingleSelection languageEl;
	private SingleSelection placeholderEl;
	private MultipleSelectionElement enableEl;
	private FormLayoutContainer imagePreviewCont;
	private final List<FileElement> imageElements = new ArrayList<>();
	private final List<TextElement> singleLineElements = new ArrayList<>();
	private final List<TextAreaElement> multiLineElements = new ArrayList<>();
	private final List<RichTextElement> formattedElements = new ArrayList<>();
	
	private FormLayoutContainer titleCont;
	private FormLayoutContainer elementCont;
	private FormLayoutContainer previewCont;
	
	private final Type type;
	private Position position;
	private Identity headOfCommittee;
	private String templateName;
	private PositionMailTemplate mailTemplate;
	private List<Locale> positionLanguages;
	private final PositionMailTemplateRow templateRow;
	private final Map<String,Locale> positionLanguageToLocale = new HashMap<>();
	
	private final String letterMapperUrl;
	private final LetterConfiguration letterConfiguration;
	private MLLetterPlaceholder currentPlaceholder;
	private final Map<Locale,List<LetterPlaceholder>> localeToPlaceholders = new HashMap<>();

	@Autowired
	private DB dbInstance;
	@Autowired
	private MailService mailService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionLetterEditController(UserRequest ureq, WindowControl wControl, Position position,
			PositionMailTemplateRow templateRow, Type type, String templateName) {
		super(ureq, wControl, "letter_editor");
		this.type = type;
		this.position = position;
		this.templateRow = templateRow;
		this.templateName = templateName;
		mailTemplate = templateRow == null ? null : templateRow.getMailTemplate();

		positionLanguages = recruitingModule.getPositionLocales(position);
		PositionEditHelper.calculatePositionLanguages(position, positionLanguages, positionLanguageToLocale);
		
		for(Locale locale:positionLanguages) {
			String template = mailService.getLetterTemplate(locale);
			List<LetterPlaceholder> placeholders = new LetterPlaceholderScanner().scan(template);
			localeToPlaceholders.put(locale, placeholders);
		}
		
		String csrfToken = ureq.getUserSession().getCsrfToken();
		letterMapperUrl = registerCacheableMapper(ureq, null, new LetterMapper(csrfToken));
		
		letterConfiguration = getLetterConfiguration();

		initForm(ureq);

		// select first placeholder
		if(!localeToPlaceholders.isEmpty()) {
			fillPlaceholders(localeToPlaceholders.values().iterator().next()); 
		}
		if(!enableEl.isAtLeastSelected(1)) {
			updateUI();// letter is not enabled
		}
	}
	
	private void fillPlaceholders(List<LetterPlaceholder> placeholders) {
		if(placeholders == null || placeholders.isEmpty()) return;
		
		LetterPlaceholder placeholder = placeholders.get(0);
		placeholderEl.select(placeholder.getId(), true);
		MLLetterPlaceholder mlPlaceholder = getPlaceholderById(placeholder.getId());
		if(mlPlaceholder != null) {
			updateElement(mlPlaceholder);
		}
	}
	
	public void updateMailTemplate(String templateName, PositionMailTemplate mailTemplate) {
		if(StringHelper.containsNonWhitespace(templateName)) {
			this.templateName = templateName;
		}
		this.mailTemplate = mailTemplate;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		titleCont = FormLayoutContainer.createDefaultFormLayout("title-cont", getTranslator());
		formLayout.add(titleCont);
		elementCont = FormLayoutContainer.createVerticalFormLayout("element-cont", getTranslator());
		formLayout.add(elementCont);

		String previewPage = velocity_root + "/letter_preview.html";
		previewCont = FormLayoutContainer.createCustomFormLayout("preview-cont", getTranslator(), previewPage);
		formLayout.add(previewCont);
		
		initTitle(titleCont);
		initElement(elementCont);
		initPreview(previewCont);
		
		String page = velocity_root + "/variable_link.html";
		FormLayoutContainer subCont = uifactory.addCustomFormLayout("cusvar", null, page, elementCont);
		subCont.setDomReplacementWrapperRequired(false);
		variablesButton = LinkFactory.createLink("edit.template.variables", subCont.getFormItemComponent(), listener);
		variablesButton.setDomReplacementWrapperRequired(false);
		variablesButton.setIconLeftCSS("o_icon o_icon_help");
		variablesButton.setPopup(new LinkPopupSettings(800, 600, "Variables"));
		
		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private void initTitle(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("form.letter.title"));
		
		SelectionValues enableKeys = new SelectionValues();
		enableKeys.add(SelectionValues.entry("on", translate("letter.enabled.on")));
		enableEl = uifactory.addCheckboxesHorizontal("letter.enable", "letter.enable", formLayout,
				enableKeys.keys(), enableKeys.values());
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(StringHelper.containsNonWhitespace(getRawLetterConfiguration())) {
			enableEl.select("on", true);
		}
		
		String letterTitle = letterConfiguration.getTitle();
		titleEl = uifactory.addTextElement("letter.title", "letter.title", 128, letterTitle, formLayout);
		titleEl.setMandatory(true);
	}
	
	private void initElement(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("letter.content"));
		formLayout.setElementCssClass("o_letter_elements");
		
		List<LetterPlaceholder> placeholders = Collections.emptyList();
		if(!localeToPlaceholders.isEmpty()) {
			placeholders = localeToPlaceholders.values().iterator().next();
		}
		
		String[] placeholderKeys = new String[placeholders.size()];
		String[] placeholderValues = new String[placeholders.size()];
		for(int i=0; i<placeholders.size(); i++) {
			LetterPlaceholder placeholder = placeholders.get(i);
			placeholderKeys[i] = placeholder.getId();
			placeholderValues[i] = placeholder.getLabel();
		}
		placeholderEl = uifactory.addDropdownSingleselect("letter.placeholders", "letter.placeholders", formLayout, placeholderKeys, placeholderValues, null);
		placeholderEl.addActionListener(FormEvent.ONCHANGE);
		
		initSingleLine(formLayout);
		initMultiLine(formLayout);
		initFormattedLine(formLayout);
		initImage(formLayout);
	}
	
	private void initSingleLine(FormLayoutContainer formLayout) {
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			TextElement el = uifactory.addTextElement("attr_single_".concat(lang), "edit.single.name", 256, "", formLayout);
			setElementLabel(el, "edit.single.name_ml", locale);
			singleLineElements.add(el);
		}
	}
	
	private void initMultiLine(FormLayoutContainer formLayout) {
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			TextAreaElement el = uifactory.addTextAreaElement("attr_multi_".concat(lang), "edit.multi.name", 256, 12, 60, true, false, false, "", formLayout);
			setElementLabel(el, "edit.multi.name_ml", locale);
			multiLineElements.add(el);
		}
	}
	
	private void initFormattedLine(FormLayoutContainer formLayout) {
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			RichTextElement el = uifactory.addRichTextElementForStringDataMinimalistic("attr_formatted_".concat(lang), "edit.multi.name", "", 16, 60, formLayout, getWindowControl());
			setElementLabel(el, "edit.multi.name_ml", locale);
			formattedElements.add(el);
		}
	}
	
	private void initImage(FormLayoutContainer formLayout) {
		for(Locale locale:positionLanguages) {
			String lang = locale.getLanguage();
			FileElement el = uifactory.addFileElement(getWindowControl(), getIdentity(), "attr_image_".concat(lang), "edit.image.name", formLayout);
			el.addActionListener(FormEvent.ONCHANGE);
			setElementLabel(el, "edit.image.name_ml", locale);
			imageElements.add(el);
		}

		String previewPage = velocity_root + "/image_preview.html";
		imagePreviewCont = FormLayoutContainer.createCustomFormLayout("image-preview-cont", getTranslator(), previewPage);
		formLayout.add(imagePreviewCont);
	}
	
	private void setElementLabel(FormItem el, String i18nLabel, Locale locale) {
		el.setMandatory(true);
		el.setUserObject(locale);
		if(positionLanguages.size() > 1) {
			String lang = locale.getLanguage();
			el.setLabel(i18nLabel, new String[]{ lang });
			el.setElementCssClass("o_sel_attr_name_" + lang);
		} else {
			el.setElementCssClass("o_sel_attr_name");
		}
	}
	
	private void initPreview(FormLayoutContainer formLayout) {
		String[] languageKeys = new String[positionLanguages.size()];
		String[] languageValues = new String[positionLanguages.size()];
		for(int i=0;i<positionLanguages.size(); i++) {
			languageKeys[i] = positionLanguages.get(i).getLanguage().toLowerCase();
			languageValues[i] = positionLanguages.get(i).getDisplayLanguage(getLocale());
		}
		languageEl = uifactory.addDropdownSingleselect("letter.language", "letter.language", formLayout, languageKeys, languageValues, null);
		languageEl.addActionListener(FormEvent.ONCHANGE);
		languageEl.select(languageKeys[0], true);

		formLayout.contextPut("indexUrl", letterMapperUrl + "/letter.html");
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(variablesButton == source) {
			// Delegate to the email template editor which know which variables to show
			fireEvent(ureq, new OpenVariablesEvent());
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(previewCont == source && "ONCLICK".equals(event.getCommand())) {
			String cid = ureq.getParameter("cid");
			if("select-placeholder".equals(cid)) {
				commitChanges();
				doPreviewEvent(ureq);
			}
		} else if(placeholderEl == source) {
			commitChanges();
			doSelectVariable();
		} else if(languageEl == source) {
			commitChanges();
		} else if(enableEl == source) {
			updateUI();
		} else if(source instanceof FileElement) {
			commitChanges();
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		commitChanges();

		String configuration = null;
		if(enableEl.isAtLeastSelected(1)) {
			configuration = getConfiguration();
		}
		
		if(type == Type.referee) {
			position.setRefereeRecommandationMailLetter(configuration);
			position = recruitingService.savePosition(position);
		} else if(type == Type.expert) {
			position.setExpertRecommandationMailLetter(configuration);
			position = recruitingService.savePosition(position);
		} else if(type == Type.comparativeExpert) {
			position.setComparativeAssessmentExpertMailLetter(configuration);
			position = recruitingService.savePosition(position);
		} else if(type == Type.committeeReminder) {
			position.setCommitteeReminderMailLetter(configuration);
			position = recruitingService.savePosition(position);
		} else if(type == Type.feedback) {
			templateRow.getFeedbackConfiguration().setMailLetter(configuration);
			feedbackService.updateApplicationsFeedbackConfiguration(templateRow.getFeedbackConfiguration());
		} else if(type == Type.confirmationApplication) {
			position.setApplicationConfirmationMailLetter(configuration);
			position = recruitingService.savePosition(position);
		} else if(type == Type.confirmationApplicationWithRefereeManagement) {
			position.setApplicationConfirmationWithRefereeManagementMailLetter(configuration);
			position = recruitingService.savePosition(position);
		} else if(type == Type.confirmationApplicationDuplicate) {
			position.setApplicationConfirmationDuplicateMailLetter(configuration);
			position = recruitingService.savePosition(position);
		} else {
			if(mailTemplate == null) {
				String id = (templateRow != null && templateRow.isSystemTemplate()) ? templateRow.getId() : Long.toString(CodeHelper.getForeverUniqueID());
				mailTemplate = createTemplate(id);
			} else if(templateRow != null && templateRow.getMailTemplate() != null) {
				mailTemplate = mailService.getTemplate(templateRow.getMailTemplate());
			} else {
				mailTemplate = mailService.getTemplate(mailTemplate);
			}
			mailTemplate.setLetter(configuration);
			mailTemplate = mailService.updateTemplate(mailTemplate);
			if(templateRow != null) {
				templateRow.setMailTemplate(mailTemplate);
			}
		}
		dbInstance.commit();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private PositionMailTemplate createTemplate(String id) {
		final Map<Locale,SubjectAndBody> defaultContent = new HashMap<>();
		if(templateRow != null && templateRow.isSystemTemplate()) {
			for(Locale locale:positionLanguages) {
				final SubjectAndBody subjectAndBody = mailService.rejectionTemplate(position, templateRow.getId(), getHeadOfCommittee(), locale);
				defaultContent.put(locale, subjectAndBody);
			}
		}
		
		String tName = templateName;
		if(tName == null) {
			tName = "-";
		}
		
		PositionMailTemplate template = mailService.createTemplate(position, id, tName);
		if(!defaultContent.isEmpty()) {
			for(Map.Entry<Locale,SubjectAndBody> entry:defaultContent.entrySet()) {
				Locale locale = entry.getKey();
				SubjectAndBody subjectAndBody = entry.getValue();
				template.setSubject(toHtml(subjectAndBody.getSubject()), locale);
				template.setBody(toHtml(subjectAndBody.getBody()), locale);
			}
		}
		return template;
	}
	
	public Identity getHeadOfCommittee() {
		if(headOfCommittee == null) {
			headOfCommittee = recruitingService.getHeadOfCommittee(position);
		}
		return headOfCommittee;
	}

	private String toHtml(String text) {
		if(StringHelper.isHtml(text)) {
			return text;
		}
		StringBuilder sb = Formatter.stripTabsAndReturns(text);
		return sb == null ? "" : sb.toString();
	}

	
	private String getRawLetterConfiguration() {
		String configuration = null;
		if(type == Type.referee) {
			configuration = position.getRefereeRecommandationMailLetter();
		} else if(type == Type.expert) {
			configuration = position.getExpertRecommandationMailLetter();
		} else if(type == Type.comparativeExpert) {
			configuration = position.getComparativeAssessmentExpertMailLetter();
		} else if(type == Type.committeeReminder) {
			configuration = position.getCommitteeReminderMailLetter();
		} else if(type == Type.feedback) {
			configuration = templateRow.getFeedbackConfiguration().getMailLetter();
		} else if(type == Type.confirmationApplication) {
			configuration = position.getApplicationConfirmationMailLetter();
		} else if(type == Type.confirmationApplicationWithRefereeManagement) {
			configuration = position.getApplicationConfirmationWithRefereeManagementMailLetter();
		} else if(type == Type.confirmationApplicationDuplicate) {
			configuration = position.getApplicationConfirmationDuplicateMailLetter();
		} else if(templateRow != null && templateRow.getMailTemplate() != null) {
			configuration = templateRow.getMailTemplate().getLetter();
		}
		return configuration;
	}
	
	private LetterConfiguration getLetterConfiguration() {
		String configuration = getRawLetterConfiguration();
		return configuration == null ? new LetterConfiguration() : LetterConfigurationXStream.fromXml(configuration);
	}
	
	private String getConfiguration() {
		return LetterConfigurationXStream.toXml(letterConfiguration);
	}
	
	private void commitChanges() {
		String title = titleEl.getValue();
		letterConfiguration.setTitle(title);
		
		commitChanges(singleLineElements, false);
		commitChanges(multiLineElements, true);
		commitChanges(formattedElements, false);
		commitImageChanges();
	}
	
	private void commitChanges(List<? extends TextElement> elements, boolean multiLines) {
		for(TextElement element:elements) {
			if(element.isVisible()) {
				String val = element.getValue();
				Locale locale = (Locale)element.getUserObject();
				LetterLanguageConfiguration config = letterConfiguration.getConfiguration(locale);
				String formattedVal = formatValueForHtml(val, multiLines);
				if(!element.isMandatory() && !StringHelper.containsNonWhitespace(formattedVal)) {
					formattedVal = "<!-- -->";
				}
				config.putValue(currentPlaceholder.getId(), formattedVal);
			}
		}
	}
	
	
	private void commitImageChanges() {
		for(FileElement element:imageElements) {
			if(element.isVisible()) {
				File val = element.getUploadFile();
				if(val != null) {
					Locale locale = (Locale)element.getUserObject();
					LetterLanguageConfiguration config = letterConfiguration.getConfiguration(locale);
					try {
						byte[] image = FileUtils.readFileToByteArray(val);
						String format = "data:image/jpeg;base64,";
						String mimeType = element.getUploadMimeType();
						if(mimeType != null && mimeType.contains("png")) {
							format = "data:image/png;base64,";
						}
						
						// UrlEncoder doesn't work with img tag
						String encodedString = format + Base64.getEncoder().encodeToString(image);
						config.putValue(currentPlaceholder.getId(), encodedString);
						imagePreviewCont.contextPut("data", encodedString);
					} catch (IOException e) {
						logError("", e);
					}
				}
			}
		}
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doSelectVariable() {
		if(placeholderEl.isOneSelected()) {
			String id = placeholderEl.getSelectedKey();
			MLLetterPlaceholder placeholder = getPlaceholderById(id);
			if(placeholder != null) {
				updateElement(placeholder);
			}
		}
	}
	
	private void doPreviewEvent(UserRequest ureq) {
		String variable = ureq.getParameter("data-sel-variable");
		MLLetterPlaceholder placeholder = getPlaceholderById(variable);
		if(placeholder != null) {
			updateElement(placeholder);
			placeholderEl.select(placeholder.getId(), true);
		}
	}
	
	private void updateElement(MLLetterPlaceholder placeholder) {
		this.currentPlaceholder = placeholder;
		String placeHolderType = placeholder.getType();
		if("single".equals(placeHolderType)) {
			updateSingleLineElement(placeholder);
		} else if("multi".equals(placeHolderType)) {
			updateMultiLineElement(placeholder);
		} else if("formatted".equals(placeHolderType)) {
			updateFormattedElement(placeholder);
		} else if("image".equals(placeHolderType)) {
			updateImageElement(placeholder);
		}
	}
	
	private void updateSingleLineElement(MLLetterPlaceholder placeholder) {
		updateElementVisibility(placeholder, true, false, false);
	}
	
	private void updateMultiLineElement(MLLetterPlaceholder placeholder) {
		updateElementVisibility(placeholder, false, true, false);
	}
	
	private void updateFormattedElement(MLLetterPlaceholder placeholder) {
		updateElementVisibility(placeholder, false, false, true);
	}
	
	private void updateImageElement(MLLetterPlaceholder placeholder) {
		updateElementVisibility(placeholder, false, false, false);
		
		for(FileElement imageEl:imageElements) {
			imageEl.setVisible(true);
			imageEl.setMandatory(placeholder.isMandatory());
		}
		imagePreviewCont.setVisible(true);
		for(Locale locale:positionLanguages) {
			LetterLanguageConfiguration config = letterConfiguration.getConfiguration(locale);
			String defVal = placeholder.getPlaceholder(locale).getDefaultValue();
			String val = config.getValue(placeholder.getId());
			if(val == null) {
				if(StringHelper.containsNonWhitespace(defVal)) {
					if(defVal.startsWith("data:image/")) {
						imagePreviewCont.contextPut("data_" + locale.getLanguage(), defVal);
					} else {
						imagePreviewCont.contextPut("data_" + locale.getLanguage(), "data:image/jpeg;base64," + defVal);
					}
				} else {
					imagePreviewCont.contextRemove("data_" + locale.getLanguage());
				}
			} else {
				if(val.startsWith("data:image/")) {
					imagePreviewCont.contextPut("data_" + locale.getLanguage(), val);
				} else {
					imagePreviewCont.contextPut("data_" + locale.getLanguage(), "data:image/jpeg;base64," + val);
				}
			}
		}
	}
	
	private void updateElementVisibility(MLLetterPlaceholder placeholder, boolean singleEls, boolean multiEls, boolean formatted) {
		updateElementVisibility(placeholder, singleLineElements, false, singleEls);
		updateElementVisibility(placeholder, multiLineElements, true, multiEls);
		updateElementVisibility(placeholder, formattedElements, false, formatted);
		
		for(FileElement imageEl:imageElements) {
			imageEl.setVisible(false);
		}
		imagePreviewCont.setVisible(false);
	}
	
	private void updateElementVisibility(MLLetterPlaceholder placeholder, List<? extends TextElement> items, boolean multiLines, boolean visible) {
		for(TextElement item:items) {
			item.setVisible(visible);
			item.setMandatory(placeholder.isMandatory());
			if(visible) {
				Locale locale = (Locale)item.getUserObject();
				LetterLanguageConfiguration config = letterConfiguration.getConfiguration(locale);
				String defVal = placeholder.getPlaceholder(locale).getDefaultValue();
				String val = config.getValue(placeholder.getId());
				if(val == null) {
					if(defVal == null) {
						item.setValue("");
					} else {
						item.setValue(formatValueForFlexi(defVal, multiLines));
					}
				} else {
					item.setValue(formatValueForFlexi(val, multiLines));
				}
			} else {
				item.setValue("");
			}	
		}
	}
	
	private String formatValueForFlexi(String val, boolean multiLines) {
		if(val == null || val.startsWith("<!--")) return null;

		if(multiLines) {
			return val.replace("&#10;", "\n").replace("<br>", "\n");
		}
		return val;
	}
	

	private String formatValueForHtml(String val, boolean multiLines) {
		if(val == null) return null;
		
		if(multiLines) {
			return val.replace("\n", "<br>");
		}
		return val;
	}
	
	private MLLetterPlaceholder getPlaceholderById(String id) {
		Map<Locale,LetterPlaceholder> map = new HashMap<>();
		for(Map.Entry<Locale, List<LetterPlaceholder>> entry:localeToPlaceholders.entrySet()) {
			Locale locale = entry.getKey();
			List<LetterPlaceholder> placeholders = entry.getValue();
			for(LetterPlaceholder placeholder:placeholders) {
				if(id.equals(placeholder.getId())) {
					map.put(locale, placeholder);
				}
			}
		}
		return map.isEmpty() ? null : new MLLetterPlaceholder(id, map);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		
		titleEl.setVisible(enabled);
		elementCont.setVisible(enabled);
		previewCont.setVisible(enabled);
	}
	
	private Locale getSelectedLanguage() {
		String selectLanguage = languageEl.getSelectedKey();
		return positionLanguageToLocale.get(selectLanguage);
	}
	
	private class LetterMapper implements Mapper {
		
		private final String csrfToken;

		
		public LetterMapper(String csrfToken) {
			this.csrfToken = csrfToken;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource mediaResource = null;
			if(relPath.endsWith("letter.html")) {
				mediaResource = loadTemplate();	
			}
			return mediaResource;
		}
		
		private MediaResource loadTemplate() {
			String letter = mailService.toLetter(letterConfiguration, getSelectedLanguage());		
			String enrichedLetter = enrichTemplate(letter);
			return new PreviewMediaResource(enrichedLetter);
		}
		
		private String enrichTemplate(String content) {
			int index = content.indexOf("</head>");
			String head = content.substring(0, index);
			String body = content.substring(index);
			
			StringOutput sb = new StringOutput(content.length() + 4096);
			sb.append(head);
			appendStaticJs(sb, "js/jquery/jquery-3.7.1.min.js");
			appendStaticJs(sb, "js/jquery/letter/editor.js");
			appendStaticCss(sb, "js/jquery/letter/editor.css");
			appendEditorJs(sb);
			sb.append(body);
			return sb.toString();
		}
		
		private void appendEditorJs(StringOutput sb) {
			sb.append("<script>")
			  .append("jQuery(function() {\n")
			  .append("  jQuery('body').letterEditor({\n")
			  .append("    formName: '").append(mainForm.getFormName()).append("',\n")//form name
			  .append("    dispIdField: '").append(mainForm.getDispatchFieldId()).append("',\n")//form dispatch id
			  .append("    dispId: '").append(previewCont.getFormDispatchId()).append("',\n")//item id
			  .append("    eventIdField: '").append(mainForm.getEventFieldId()).append("',\n") // form eventFieldId
			  .append("    csrfToken: '").append(csrfToken).append("'\n") // form eventFieldId
			  .append("  })\n")
			  .append("});\n")
			  .append("</script>\n");
		}
		
		private void appendStaticJs(StringOutput sb, String javascript) {
			sb.append("<script src=\"");
			StaticMediaDispatcher.renderStaticURI(sb, javascript);
			sb.append("\"></script>\n");
		}
		
		private void appendStaticCss(StringOutput sb, String css) {
			sb.append("<link rel=\"stylesheet\" href=\"");
			StaticMediaDispatcher.renderStaticURI(sb, css);
			sb.append("\">\n");
		}
	}
	
	private static class MLLetterPlaceholder {
		private final String id;
		private final String type;
		private final boolean mandatory;
		private final Map<Locale,LetterPlaceholder> placeholders;
		
		public MLLetterPlaceholder(String id, Map<Locale,LetterPlaceholder> placeholders) {
			this.id = id;
			this.placeholders = placeholders;
			mandatory = placeholders.values().iterator().next().isMandatory();
			type = placeholders.values().iterator().next().getType();
		}
		
		public String getId() {
			return id;
		}
		
		public String getType() {
			return type;
		}
		
		public boolean isMandatory() {
			return mandatory;
		}
		
		public LetterPlaceholder getPlaceholder(Locale locale) {
			return placeholders.get(locale);
		}
	}
	
	private static class PreviewMediaResource extends DefaultMediaResource {
		
		private String data;
		
		public PreviewMediaResource(String data) {
			this.data = data;
		}
		
		@Override
		public boolean acceptRanges() {
			return false;
		}

		@Override
		public InputStream getInputStream() {
			return new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));
		}
		
		@Override
		public Long getSize() {
			return Long.valueOf(data.getBytes().length);
		}

		@Override
		public void prepare(HttpServletResponse hres) {
			// HTTP 1.1
			hres.setHeader("Cache-Control", "private, no-cache, no-store, must-revalidate, proxy-revalidate, s-maxage=0, max-age=0");
			// HTTP 1.0
			hres.setHeader("Pragma", "no-cache");
			hres.setDateHeader("Expires", 0);
			//
			hres.setContentType("text/html");
			hres.setCharacterEncoding("UTF-8");
		}
	}
}
