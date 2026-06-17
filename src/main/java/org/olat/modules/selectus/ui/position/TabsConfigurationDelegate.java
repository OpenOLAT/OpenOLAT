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
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.DefaultGlobalSettings;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.MailerSender;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.OrganisationUnit;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.ReferenceSendMailType;
import org.olat.modules.selectus.model.SubjectAndBody;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.ApplicationAppliedController;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;
import org.olat.modules.selectus.ui.app_wizard.InstructionsController;
import org.olat.modules.selectus.ui.app_wizard.RefereesStepController;

/**
 * 
 * Initial date: 23 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TabsConfigurationDelegate {
	
	private static final Logger log = Tracing.createLoggerFor(TabsConfigurationDelegate.class);
	
	private final Tab tab;
	
	private final FormUIFactory uifactory = FormUIFactory.getInstance();
	
	public TabsConfigurationDelegate(Tab tab) {
		this.tab = tab;
	}
	
	public void defaultHelpText(Position position, TabConfiguration configuration) {
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		List<Locale> positionLanguages = recruitingModule.getPositionLocales(position);
		for(Locale locale:positionLanguages) {
			defaultHelpText(position, configuration, locale);
		}
	}
	
	private void defaultHelpText(Position position, TabConfiguration configuration, Locale locale) {
		if(tab == Tab.referees) {
			String hintKey;
			if(position.getRefereeRecommandationSendMailType() == ReferenceSendMailType.auto) {
				hintKey = "wizard.referees.explain.auto";
			} else {
				hintKey = "wizard.referees.explain.staff";
			}
			setHelp(configuration, hintKey, null, locale);
			String additionalHintKey = "wizard.referees.explain.additional";
			setAdditionalHelp(configuration, additionalHintKey, null, locale);
		} else if(tab == Tab.personalData) {
			String hintKey = "wizard.edit_person.explanation";
			setHelp(configuration, hintKey, null, locale);
		} else if(tab == Tab.academicalBackground) {
			String hintKey = "wizard.edit_background.explanation";
			setHelp(configuration, hintKey, null, locale);
		} else if(tab == Tab.project) {
			String hintKey = "wizard.edit_project.explanation";
			setHelp(configuration, hintKey, null, locale);
		} else if(tab == Tab.documents) {
			String hintKey = "wizard.documents.explanation";
			setHelp(configuration, hintKey, null, locale);
		} else if(tab == Tab.reviewAndSubmit) {
			if(!StringHelper.containsNonWhitespace(configuration.getHelp(locale))) {
				String text = getWarningReviewAndSubmit(position, locale);
				configuration.setHelp(text, locale);
			}
		} else if(tab == Tab.confirmation) {
			if(!StringHelper.containsNonWhitespace(configuration.getHelp(locale))) {
				String text = getConfirmation(locale);
				configuration.setHelp(text, locale);
			}
		}
	}

	public String getConfirmation(Locale locale) {
		Translator translator = Util.createPackageTranslator(ApplicationAppliedController.class, locale);
		String[] names = new String[] {
			"$titleAndName", 			// 0
			"",							// 1
			"$applicantFirstName",		// 2
			"$applicantLastName",		// 3
			"$applicantTitleLastName",	// 4
			"$positionMail"				// 5
		};
		String text1 = translator.translate("add_application.text1", names);
		String text2 = translator.translate("add_application.text2", names);
		
		StringBuilder sb = new StringBuilder();
		sb.append("<p><strong>").append(text1).append("</strong></p><p>").append(text2).append("</p>");
		return sb.toString();
	}
	
	public String getWarningReviewAndSubmit(Position position, Locale locale) {
		String hintKey = "wizard.review_submit.warning";
		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		RecruitingService recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
		OrganisationUnit organisationSettings = recruitingService.getOrganisationUnit(position);
		String[] i18nArguments = new String[] {
			recruitingModule.getStaffMail(position, organisationSettings)	// 0
		};
		Translator translator = Util.createPackageTranslator(RefereesStepController.class, locale);
		return translator.translate(hintKey, i18nArguments);
	}
	
	public String getDefaultInstructions(Identity identity, Position position, WindowControl wControl, Translator translator, Locale locale) {
		UserRequest ureq = new SyntheticUserRequest(identity, locale);
		InstructionsController previewCtrl = new InstructionsController(ureq, wControl, null, position, new TabConfiguration(), true);
		return createResultHTML(previewCtrl.getInitialComponent(), translator);
	}
	
	private String createResultHTML(Component cmp, Translator translator) {
		String pagePath = Util.getPackageVelocityRoot(this.getClass()) + "/render.html";
		//generate VelocityContainer and put Component
		VelocityContainer mainVC = new VelocityContainer("html", pagePath, translator, null);
		mainVC.put("cmp", cmp);
	
		URLBuilder ubu = new URLBuilder("auth", "1", "0", "-");
		//render VelocityContainer to StringOutPut
		Renderer renderer = Renderer.getInstance(mainVC, translator, ubu, new RenderResult(), new DefaultGlobalSettings(), "-");
		try(StringOutput sb = new StringOutput(32000)) {
			renderer.render(sb, cmp, null);
			return sb.toString();
		} catch(Exception e) {
			log.error("", e);
			return "";
		}
	}
	
	private void setAdditionalHelp(TabConfiguration configuration, String hintKey, String[] args, Locale locale) {
		String text = configuration.getAdditionalHelp(locale);
		if(!StringHelper.containsNonWhitespace(text)) {
			Translator translator = Util.createPackageTranslator(RefereesStepController.class, locale);
			text = translator.translate(hintKey, args);
			configuration.setAdditionalHelp(text, locale);
		}
	}
	
	private void setHelp(TabConfiguration configuration, String hintKey, String[] args, Locale locale) {
		String text = configuration.getHelp(locale);
		if(!StringHelper.containsNonWhitespace(text)) {
			Translator translator = Util.createPackageTranslator(RefereesStepController.class, locale);
			text = translator.translate(hintKey, args);
			configuration.setHelp(text, locale);
		}
	}
	
	public void updateHelps(List<Locale> positionLanguages, TabConfiguration configuration,
			FormLayoutContainer helpContainer, List<TextElement> helpEls, List<TextElement> additionalHelpEls, WindowControl wControl, boolean richText) {
		List<Locale> missingLocales = new ArrayList<>(positionLanguages);
		for(TextElement helpEl:helpEls) {
			Object locale = helpEl.getUserObject();
			helpEl.setVisible(positionLanguages.contains(locale));
			missingLocales.remove(locale);
		}
		
		for(Locale locale:missingLocales) {
			TextElement textEl = initHelpText(locale, positionLanguages, configuration, helpContainer, wControl, false, richText);
			helpEls.add(textEl);
			
			if(additionalHelpEls != null) {
				TextElement additionalEl = initHelpText(locale, positionLanguages, configuration, helpContainer, wControl, true, richText);
				additionalHelpEls.add(additionalEl);
			}
		}
		
		for(TextElement helpEl:helpEls) {
			Locale locale = (Locale)helpEl.getUserObject();
			helpEl.setVisible(positionLanguages.contains(locale));
			labelHelpText(locale, positionLanguages, helpEl, false);
		}
		
		if(additionalHelpEls != null) {
			for(TextElement additionalHelpEl:additionalHelpEls) {
				Locale locale = (Locale)additionalHelpEl.getUserObject();
				additionalHelpEl.setVisible(positionLanguages.contains(locale));
				labelHelpText(locale, positionLanguages, additionalHelpEl, true);
			}
		}
	}
	
	private String helpTextI18nFix(boolean additional) {
		String i18n;
		if(tab == Tab.referees && additional) {
			i18n = "help.refereemgmt";
		} else {
			if(tab == Tab.confirmation) {
				i18n = "text";
			} else {
				i18n = "help";
			}
			if(additional) {
				i18n = i18n.concat(".additional");
			}
		}
		return i18n;
	}
	
	public void updateHeadings(List<Locale> positionLanguages, TabConfiguration configuration,
			FormLayoutContainer helpContainer, List<TextElement> headingEls) {
		List<Locale> missingLocales = new ArrayList<>(positionLanguages);
		for(TextElement headingEl:headingEls) {
			Object locale = headingEl.getUserObject();
			headingEl.setVisible(positionLanguages.contains(locale));
			missingLocales.remove(locale);
		}
		
		for(Locale locale:missingLocales) {
			TextElement textEl = initHeading(locale, positionLanguages, configuration, helpContainer);
			headingEls.add(textEl);
		}
		
		for(TextElement headingEl:headingEls) {
			Locale locale = (Locale)headingEl.getUserObject();
			headingEl.setVisible(positionLanguages.contains(locale));
			labelHeading(locale, positionLanguages, headingEl);
		}
	}
	
	public FormLayoutContainer initHeadings(List<Locale> positionLanguages, TabConfiguration configuration,
			FormItemContainer formLayout, Form mainForm, List<TextElement> headingsEls, boolean readOnly) {
		FormLayoutContainer headingContainer = FormLayoutContainer.createDefaultFormLayout_2_10("headings", formLayout.getTranslator());
		formLayout.add("headings", headingContainer);
		headingContainer.setRootForm(mainForm);
		for(Locale locale:positionLanguages) {
			TextElement textEl = initHeading(locale, positionLanguages, configuration, headingContainer);
			textEl.setEnabled(!readOnly);
			headingsEls.add(textEl);
		}
		return headingContainer;
	}
	
	private TextElement initHeading(Locale locale, List<Locale> positionLanguages, TabConfiguration configuration, FormItemContainer headingContainer) {
		String lang = locale.getLanguage();
		String text = StringHelper.escapeHtml(configuration.getHeading(locale));
		String id = "attr_name_heading_" + lang;
		String i18nLabel = "edit.heading.name";
		TextElement textEl = uifactory.addTextElement(id, i18nLabel, 255, text, headingContainer);
		labelHeading(locale, positionLanguages, textEl);
		textEl.setUserObject(locale);
		return textEl;
	}
	
	private void labelHeading(Locale locale, List<Locale> positionLanguages, TextElement textEl) {
		String lang = locale.getLanguage();
		if(positionLanguages.size() > 1) {
			textEl.setLabel("edit.heading.name_ml", new String[]{ lang });
			textEl.setElementCssClass("o_sel_help_name_" + lang);
		} else {
			textEl.setElementCssClass("o_sel_help_name");
		}
		
		String i18nHint = "edit.heading.hint";
		String hint = textEl.getTranslator().translate(i18nHint);
		if(StringHelper.containsNonWhitespace(hint)) {
			textEl.setHelpText(hint);
		}
	}
	
	public FormLayoutContainer initHelpTexts(List<Locale> positionLanguages, TabConfiguration configuration,
			FormItemContainer formLayout, Form mainForm, List<TextElement> helpEls, List<TextElement> additionalHelpEls,
			WindowControl wControl, boolean richText, boolean readOnly) {
		FormLayoutContainer helpContainer = FormLayoutContainer.createDefaultFormLayout_2_10("helps", formLayout.getTranslator());
		formLayout.add("helps", helpContainer);
		helpContainer.setRootForm(mainForm);
		for(Locale locale:positionLanguages) {
			TextElement textEl = initHelpText(locale, positionLanguages, configuration, helpContainer, wControl, false, richText);
			textEl.setEnabled(!readOnly);
			helpEls.add(textEl);
			
			if(additionalHelpEls != null) {
				TextElement additionalEl = initHelpText(locale, positionLanguages, configuration, helpContainer, wControl, true, richText);
				additionalEl.setEnabled(!readOnly);
				additionalHelpEls.add(additionalEl);
			}
		}
		return helpContainer;
	}
	
	private TextElement initHelpText(Locale locale, List<Locale> positionLanguages, TabConfiguration configuration,
			FormItemContainer helpContainer, WindowControl wControl, boolean additional, boolean richText) {
		String lang = locale.getLanguage();
		String text = additional ? configuration.getAdditionalHelp(locale) : configuration.getHelp(locale);
		TextElement textEl;

		String i18nFix = helpTextI18nFix(additional);
		String id = "attr_name_" + lang + (additional ? "" : "_add");
		String i18nLabel = "edit." + i18nFix + ".name";
		
		if(richText) {
			RichTextElement richTextEl = uifactory.addRichTextElementForStringDataMinimalistic(id, i18nLabel, text, 5, 60, helpContainer, wControl);
			richTextEl.getEditorConfiguration().setPathInStatusBar(false);
			textEl = richTextEl;
		} else {
			textEl = uifactory.addTextAreaElement(id, i18nLabel, 32000, 3, 60, false, false, false, text, helpContainer);
		}
		textEl.setUserObject(locale);
		labelHelpText(locale, positionLanguages, textEl, additional);
		return textEl;
	}
	
	private void labelHelpText(Locale locale, List<Locale> positionLanguages, TextElement textEl, boolean additional) {
		String lang = locale.getLanguage();
		String i18n = helpTextI18nFix(additional);
		if(positionLanguages.size() > 1) {
			textEl.setLabel("edit." + i18n + ".name_ml", new String[]{ lang });
			textEl.setElementCssClass("o_sel_help_name_" + lang);
		} else {
			textEl.setElementCssClass("o_sel_help_name");
		}
		
		String i18nHint = "edit." + i18n + ".hint";
		String hint = textEl.getTranslator().translate(i18nHint);
		if(StringHelper.containsNonWhitespace(hint)) {
			textEl.setHelpText(hint);
		}
	}
	
	public void save(Position position, TabConfiguration configuration, List<TextElement> helpEls, List<TextElement> additionalHelpEls, List<TextElement> headingsEls) {
		for(TextElement helpEl:helpEls) {
			Locale locale = (Locale)helpEl.getUserObject();
			configuration.setHelp(helpEl.getValue(), locale);
		}
		if(additionalHelpEls != null) {
			for(TextElement additionalHelpEl:additionalHelpEls) {
				Locale locale = (Locale)additionalHelpEl.getUserObject();
				configuration.setAdditionalHelp(additionalHelpEl.getValue(), locale);
			}
		}
		if(headingsEls != null) {
			for(TextElement headingEl:headingsEls) {
				Locale locale = (Locale)headingEl.getUserObject();
				configuration.setHeading(headingEl.getValue(), locale);
			}
		}
		position.setTabConfiguration(configuration.getTab(), configuration);
	}
	
	public String render(String message, Application application, SalutationGenerator salutationGenerator, Translator translator) {
		RecruitingService recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
		MailerSender mailSender = recruitingService.createMailSender();
		SubjectAndBody subjectAndBody = new SubjectAndBody("", message);
		RecruitingMailTemplate template = new RecruitingMailTemplate(application.getKey(), "confirmation", null, null, null, null,
				null, null, subjectAndBody, salutationGenerator, translator);
		VelocityContext context = template.getContext();
		template.putVariablesInMailContext(context, application, null, null, null, null, null, application.getPosition());
		return mailSender.renderTemplate(context, message);
	}

}
