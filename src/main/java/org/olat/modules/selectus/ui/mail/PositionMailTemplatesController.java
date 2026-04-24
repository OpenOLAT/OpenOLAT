/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.MailService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionMailTemplate;
import org.olat.modules.selectus.model.letter.LetterConfiguration;
import org.olat.modules.selectus.model.letter.LetterConfigurationXStream;
import org.olat.modules.selectus.ui.RecruitingMainController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.feedback.appsfeedback.FeedbackHelper;
import org.olat.modules.selectus.ui.mail.PositionMailTemplateRow.Type;
import org.olat.modules.selectus.ui.mail.PositionMailTemplatesDataModel.TemplateCols;
import org.olat.modules.selectus.ui.position.PositionEditableController;
import org.olat.modules.selectus.ui.rejection.TemplateForEmailController;

/**
 * 
 * Initial date: 23 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionMailTemplatesController extends FormBasicController
implements PositionEditableController, FlexiTableCssDelegate {
	
	private Position position;
	private final boolean embedded;
	private final boolean readOnly;
	private final List<Type> acceptedTypes;
	
	private FormLink addTemplateButton;
	private FlexiTableElement tableEl;
	private PositionMailTemplatesDataModel tableModel;
	
	private CloseableModalController cmc;
	private PositionMailTemplateEditController editTemplateCtrl;
	private PositionMailTemplateResetController confirmResetCtrl;
	private PositionMailTemplateDeleteController confirmDeleteCtrl;
	private PositionMailAndLettersEditController addTemplateCtrl;
	private MailTemplateSingleLanguageEditController editPositionTemplateCtrl;
	private MailTemplateMultiLanguageEditController editPositionMLTemplateCtrl;
	private PositionMailAndLettersEditController mailAndLetterEditCtrl;
	
	@Autowired
	private MailService mailService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public PositionMailTemplatesController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, "templates", Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale()));
		setTranslator(Util.createPackageTranslator(TemplateForEmailController.class, getLocale(), getTranslator()));
		acceptedTypes = Arrays.asList(Type.values());
		this.position = position;
		this.readOnly = readOnly;
		embedded = false;
		
		initForm(ureq);
		loadModel();
		loadInformations();
	}
	
	public PositionMailTemplatesController(UserRequest ureq, WindowControl wControl, Form rootForm, Position position, List<Type> acceptedTypes, boolean readOnly) {
		super(ureq, wControl, LAYOUT_CUSTOM, "templates_embedded", rootForm);
		setTranslator(Util.createPackageTranslator(TemplateForEmailController.class, getLocale(),
				Util.createPackageTranslator(RecruitingMainController.class, getLocale(),
						Util.createPackageTranslator(PositionMailTemplatesController.class, getLocale()))));
		this.acceptedTypes = new ArrayList<>(acceptedTypes);
		this.position = position;
		this.readOnly = readOnly;
		embedded = true;
		
		initForm(ureq);
		loadModel();
		loadInformations();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!embedded && !readOnly) {
			((FormLayoutContainer)formLayout).contextPut("withLetter", Boolean.valueOf(recruitingModule.isMailLetterEnabled()));
			addTemplateButton = uifactory.addFormLink("add.template", "add.template", null, formLayout, Link.BUTTON);
			addTemplateButton.setIconLeftCSS("o_icon o_icon_add");
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TemplateCols.templateId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.templateName));
		if(recruitingModule.isMailLetterEnabled() && (acceptedTypes.contains(Type.system) || acceptedTypes.contains(Type.custom))) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.letterName));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TemplateCols.recipient));
		
		if(!readOnly) {
			StaticFlexiCellRenderer customizeRenderer = new StaticFlexiCellRenderer(translate("customize"), "edit");
			customizeRenderer.setIconLeftCSS("o_icon o_icon_edit");
			StaticFlexiCellRenderer editRenderer = new StaticFlexiCellRenderer(translate("edit"), "edit");
			editRenderer.setIconLeftCSS("o_icon o_icon_edit");
			DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel(TemplateCols.edit.i18nHeaderKey(), TemplateCols.edit.ordinal(), "edit",
					new EditCellRenderer(customizeRenderer, editRenderer, getTranslator()));
			editCol.setHeaderLabel(translate("table.action"));
			columnsModel.addFlexiColumnModel(editCol);
			
			StaticFlexiCellRenderer resetRenderer = new StaticFlexiCellRenderer(translate("reset"), "reset");
			resetRenderer.setIconLeftCSS("o_icon o_icon_reset");
			StaticFlexiCellRenderer deleteRenderer = new StaticFlexiCellRenderer(translate("delete"), "delete");
			deleteRenderer.setIconLeftCSS("o_icon o_icon_delete_item");
			DefaultFlexiColumnModel deleteCol = new DefaultFlexiColumnModel(TemplateCols.reset.i18nHeaderKey(), TemplateCols.reset.ordinal(), "reset",
					new ResetCellRenderer(resetRenderer, deleteRenderer));
			deleteCol.setHeaderLabel(translate("table.action"));
			columnsModel.addFlexiColumnModel(deleteCol);
		}
		
		tableModel = new PositionMailTemplatesDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "mail-templates-v2");
		tableEl.setCssDelegate(this);
	}

	@Override
	public String getWrapperCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getTableCssClass(FlexiTableRendererType type) {
		return null;
	}

	@Override
	public String getRowCssClass(FlexiTableRendererType type, int pos) {
		PositionMailTemplateRow row = tableModel.getObject(pos);
		return row.isEnabled() ? null : "fx_r_mail_disabled";
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		position = updatedPosition;
		loadModel();
		loadInformations();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmResetCtrl == source || addTemplateCtrl == source
				|| editPositionMLTemplateCtrl == source || editPositionTemplateCtrl == source
				|| mailAndLetterEditCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event instanceof NewPositionSavedEvent) {
				if(confirmResetCtrl == source) {
					position = confirmResetCtrl.getPosition();
				} else if(addTemplateCtrl == source) {
					position = addTemplateCtrl.getPosition();
				} else if(editPositionMLTemplateCtrl == source) {
					position = editPositionMLTemplateCtrl.getPosition();
				} else if(editPositionTemplateCtrl == source) {
					position = editPositionTemplateCtrl.getPosition();
				} else if(mailAndLetterEditCtrl == source) {
					position = mailAndLetterEditCtrl.getPosition();
				}
				loadModel();
				fireEvent(ureq, event);
			} else if(event == Event.CANCELLED_EVENT ) {
				// Letter editing allow to save and cancel after
				if(addTemplateCtrl == source) {
					position = addTemplateCtrl.getPosition();
					loadModel();
				} else if(mailAndLetterEditCtrl == source ) {
					position = mailAndLetterEditCtrl.getPosition();
					loadModel();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(editTemplateCtrl == source
				|| confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT || event == Event.CANCELLED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			if("CLOSE_MODAL_EVENT".equals(event.getCommand())) {
				loadModel();
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editPositionMLTemplateCtrl);
		removeAsListenerAndDispose(editPositionTemplateCtrl);
		removeAsListenerAndDispose(mailAndLetterEditCtrl);
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(confirmResetCtrl);
		removeAsListenerAndDispose(editTemplateCtrl);
		removeAsListenerAndDispose(addTemplateCtrl);
		removeControllerListener(cmc);
		editPositionMLTemplateCtrl = null;
		editPositionTemplateCtrl = null;
		mailAndLetterEditCtrl = null;
		confirmDeleteCtrl = null;
		confirmResetCtrl = null;
		editTemplateCtrl = null;
		addTemplateCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addTemplateButton == source) {
			doAddTemplate(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditTemplate(ureq, tableModel.getObject(se.getIndex()));
				} else if("reset".equals(se.getCommand())) {
					doConfirmResetTemplate(ureq, tableModel.getObject(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDeleteTemplate(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private String getLetterName(String configuration) {
		if(StringHelper.containsNonWhitespace(configuration)) {
			LetterConfiguration config = LetterConfigurationXStream.fromXml(configuration);
			return config.getTitle();
		}
		return null;
	}
	
	private boolean isCustomized(String text, String textDe, String textFr, String letterConfiguration) {
		return StringHelper.containsNonWhitespace(text) || StringHelper.containsNonWhitespace(textDe)
				|| StringHelper.containsNonWhitespace(textFr) || isLetterCustomized(letterConfiguration);
	}
	
	private boolean isCustomizedI18nKey(String text, String i18nKey, String letterConfiguration) {
		String original = translate(i18nKey);
		return (StringHelper.containsNonWhitespace(text) && !normalize(text).equals(normalize(original)))
				|| isLetterCustomized(letterConfiguration);
	}
	
	private boolean isCustomizedFeedback(String text, String letterConfiguration) {
		
		String original = FeedbackHelper.getDefaultTemplateBodyHtml(position, salutationGenerator, getLocale());
		String normalizedOriginal = normalize(original);
		String normalizedText = normalize(text);
		return (StringHelper.containsNonWhitespace(text) && !normalizedOriginal.equals(normalizedText))
				|| isLetterCustomized(letterConfiguration);
	}
	
	private boolean isLetterCustomized(String letterConfiguration) {
		return StringHelper.containsNonWhitespace(letterConfiguration);
	}
	
	private String normalize(String text) {
		if(text == null) return "";
		if(!StringHelper.isHtml(text)) {
			text = Formatter.escWithBR(text).toString();
		}
		return text.replace("\r", "")
				.replace("\n", "")
				.replace(" ", "")
				.replace("<br/>", "<br>")
				.replace("<p>", "")
				.replace("</p>", "")
				.replace("&quot;", "&#34;")
				.replace("\"", "&#34;");
	}
	
	private void loadInformations() {
		if(embedded && recruitingModule.isReferenceApplicantManagement()
				&& acceptedTypes.contains(Type.confirmationApplicationWithRefereeManagement)) {
			String path = "[Positions:0][Position:" + position.getKey() + "][Edit:0][References:0]";
			String refereeConfigUrl = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(path);
			String informations = translate("mail.templates.description.with.management", refereeConfigUrl);
			flc.contextPut("infos", informations);
		}
	}
	
	private void loadModel() {
		List<PositionMailTemplateRow> rows = new ArrayList<>();
		if(acceptedTypes.contains(Type.confirmationApplication)) {
			String i18nKey = recruitingModule.isReferenceApplicantManagement() ? "type.confirmationApplicationWithoutMgmt" : "type.confirmationApplication";
			rows.add(new PositionMailTemplateRow(null, translate(i18nKey),
					Type.confirmationApplication, translate("recipient.applicant"),
					isCustomized(position.getApplicationConfirmationMailTemplate(), position.getApplicationConfirmationMailTemplateDe(), position.getApplicationConfirmationMailTemplateFr(), position.getApplicationConfirmationMailLetter()),
					!position.isApplicantRefereeManagementEnabled(), false));
		}
		if(recruitingModule.isReferenceApplicantManagement() && acceptedTypes.contains(Type.confirmationApplicationWithRefereeManagement)) {
			rows.add(new PositionMailTemplateRow(null, translate("type.confirmationApplicationMgmt"),
					Type.confirmationApplicationWithRefereeManagement, translate("recipient.applicant"),
					isCustomized(position.getApplicationConfirmationWithRefereeManagementMailTemplate(), position.getApplicationConfirmationWithRefereeManagementMailTemplateDe(), position.getApplicationConfirmationWithRefereeManagementMailTemplateFr(), position.getApplicationConfirmationWithRefereeManagementMailLetter()),
					position.isApplicantRefereeManagementEnabled(), false));
		}
		if(!recruitingModule.isApplicationDuplicateEmailsAllowed() && acceptedTypes.contains(Type.confirmationApplicationDuplicate)) {
			rows.add(new PositionMailTemplateRow(null, translate("type.confirmationApplicationDuplicate"),
					Type.confirmationApplicationDuplicate, translate("recipient.applicant"),
					isCustomized(position.getApplicationConfirmationDuplicateMailTemplate(), position.getApplicationConfirmationDuplicateMailTemplateDe(), position.getApplicationConfirmationDuplicateMailTemplateFr(), position.getApplicationConfirmationDuplicateMailLetter()),
					true, false));
		}

		if(acceptedTypes.contains(Type.committeeReminder)) {
			rows.add(new PositionMailTemplateRow(null, translate("type.committeeReminder"),
					Type.committeeReminder,  translate("recipient.committee.member.not.rated"),
					isCustomized(position.getCommitteeReminderMailTemplate(), null, null, position.getCommitteeReminderMailLetter()),
					position.getCommitteeReminderDate() != null, false));
		}
		if(recruitingModule.isReferenceEnabled()) {
			if(acceptedTypes.contains(Type.expert)) {
				rows.add(new PositionMailTemplateRow(null, translate("type.expert"),
						Type.expert, translate("recipient.expert"),
						isCustomizedI18nKey(position.getExpertRecommandationMailTemplate(), "reference.expert.mail.body",
								position.getExpertRecommandationMailLetter()),
						position.isExpertRecommendationEnabled(), false));
			}
			if(acceptedTypes.contains(Type.comparativeExpert) && recruitingModule.isComparativeAssessmentExpertsEnabled()) {
				rows.add(new PositionMailTemplateRow(null, translate("type.comparative.expert"),
						Type.comparativeExpert, translate("recipient.expert"),
						isCustomizedI18nKey(position.getExpertRecommandationMailTemplate(), "reference.comparative.expert.mail.body",
								position.getComparativeAssessmentExpertMailLetter()),
						position.isComparativeAssessmentExpertEnabled(), false));
			}
			
			if(acceptedTypes.contains(Type.referee)) {
				rows.add(new PositionMailTemplateRow(null, translate("type.referee"),
						Type.referee, translate("recipient.referee"),
						isCustomizedI18nKey(position.getRefereeRecommandationMailTemplate(), "reference.recommendation.mail.body",
								position.getRefereeRecommandationMailLetter()),
						position.isRefereeRecommendationEnabled(), false));
			}
		}
		
		if(acceptedTypes.contains(Type.feedback)) {
			List<ApplicationsFeedbackConfiguration> configurations = feedbackService
					.getApplicationsFeedbackConfigurations(position);
			for(ApplicationsFeedbackConfiguration configuration:configurations) {
				String name = null;
				if(configurations.size() == 1) {
					name = translate("type.feedback");
				}
				rows.add(new PositionMailTemplateRow(name, configuration,
						translate("recipient.faculty.member"),
						isCustomizedFeedback(configuration.getMailTemplate(), configuration.getMailLetter()),
						configuration.isEnabled()));
			}
		}
		
		if(acceptedTypes.contains(Type.system)) {
			String[] mailTemplates = recruitingModule.getMailTemplateTitles();
			if(mailTemplates.length == 0) {
				String defaultName = translate("default.template");
				rows.add(new PositionMailTemplateRow(ApplicationMailTemplate.DEFAULT_TEMPLATE, defaultName,
						Type.system, translate("recipient.applicant"), false, true, true));
			} else {
				for(String mailTemplate:mailTemplates) {
					rows.add(new PositionMailTemplateRow(mailTemplate, mailTemplate,
							Type.system, translate("recipient.applicant"), false, true, true));
				}
			}
		}

		if(acceptedTypes.contains(Type.custom)) {
			List<PositionMailTemplate> customTemplates = mailService.getTemplates(position);
			for(PositionMailTemplate customTemplate:customTemplates) {
				boolean systemTemplate = false;
				for(PositionMailTemplateRow row:rows) {
					if(customTemplate.getId().equals(row.getId())) {
						row.setMailTemplate(customTemplate);
						row.setLetterName(getLetterName(customTemplate.getLetter()));
						systemTemplate = true;
					}	
				}
				
				if(!systemTemplate) {
					rows.add(new PositionMailTemplateRow(customTemplate, translate("recipient.applicant"),
						getLetterName(customTemplate.getLetter())));
				}
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void doAddTemplate(UserRequest ureq) {
		if(guardModalController(addTemplateCtrl)) return;

		addTemplateCtrl = new PositionMailAndLettersEditController(ureq, getWindowControl(), position,
				null, Type.custom, null, true);
		listenTo(addTemplateCtrl);
		
		String title = translate("add.template.title");
		cmc = new CloseableModalController(getWindowControl(), "c", addTemplateCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditTemplate(UserRequest ureq, PositionMailTemplateRow row) {
		if(guardModalController(mailAndLetterEditCtrl)) return;

		String templateName = tableModel.getName(row);
		mailAndLetterEditCtrl = new PositionMailAndLettersEditController(ureq, getWindowControl(), position,
				row, row.getType(), templateName, row.isWithLetter());
		listenTo(mailAndLetterEditCtrl);
		
		String title = translate("edit.template", new String[] { StringHelper.escapeHtml(templateName) });
		cmc = new CloseableModalController(getWindowControl(), "c", mailAndLetterEditCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeleteTemplate(UserRequest ureq, PositionMailTemplateRow row) {
		if(guardModalController(confirmDeleteCtrl)) return;

		String templateName = tableModel.getName(row);
		confirmDeleteCtrl = new PositionMailTemplateDeleteController(ureq, getWindowControl(), row.getMailTemplate(), templateName);
		listenTo(confirmDeleteCtrl);
		
		String title = translate("confirm.delete.template.title", new String[] { StringHelper.escapeHtml(templateName) });
		cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmResetTemplate(UserRequest ureq, PositionMailTemplateRow row) {
		if(guardModalController(confirmResetCtrl)) return;

		String templateName = tableModel.getName(row);
		confirmResetCtrl = new PositionMailTemplateResetController(ureq, getWindowControl(),
				position, row, templateName);
		listenTo(confirmResetCtrl);
		
		String title = translate("confirm.reset.template.title", new String[] { StringHelper.escapeHtml(templateName) });
		cmc = new CloseableModalController(getWindowControl(), "c", confirmResetCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	} 
}
