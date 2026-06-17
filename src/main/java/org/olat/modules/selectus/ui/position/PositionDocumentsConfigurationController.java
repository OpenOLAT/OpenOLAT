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
import java.util.EnumMap;
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
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.DocumentEnum;
import org.olat.modules.selectus.DocumentOption;
import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.app_wizard.DocumentsController;
import org.olat.modules.selectus.ui.components.DocumentVisibilityRenderer;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionDocumentsDataModel.DocumentCols;
import org.olat.modules.selectus.ui.position.model.PositionDocumentRow;
import org.olat.modules.selectus.ui.reference.ReferenceHelper;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  30 jul. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PositionDocumentsConfigurationController extends FormBasicController implements PositionEditableController {
	
	protected static final int FEEDBACKS_OFFSET = 5000;
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	private static final String[] docKeys = new String[]{"available","mandatory"};
	private final String[] docValues = new String[docKeys.length];
	private static final String[] usageKeys = new String[] { "wizard", "staff" };
	private final String[] usageValues = new String[usageKeys.length];

	private List<FormLink> previewButtons = new ArrayList<>(2);
	private List<TextElement> helpEls = new ArrayList<>(2);
	private FlexiTableElement tableEl;
	private FormLayoutContainer helpContainer;
	private PositionDocumentsDataModel tableModel;
	private final FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
	
	private int count;
	private Position position;
	private final boolean readOnly;

	private TabConfiguration tabConfiguration;
	private List<ApplicationsFeedbackConfiguration> configurations;
	private final TabsConfigurationDelegate tabsConfigurationDelegate;
	
	private List<Locale> positionLanguages;
	
	private CloseableModalController cmc;
	private DocumentsController documentsCtrl;
	private PositionEditDocumentNameController editDocumentNameCtrl;
	private CloseableCalloutWindowController editDocumentNameCallout; 
	private PositionEditDocumentConfigurationController editDocumentConfigurationCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionDocumentsConfigurationController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, "edit_documents", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		tabConfiguration = position.getTabConfiguration(Tab.documents);
		tabsConfigurationDelegate = new TabsConfigurationDelegate(Tab.documents);
		tabsConfigurationDelegate.defaultHelpText(position, tabConfiguration);
		positionLanguages = recruitingModule.getPositionLocales(position);
		
		docValues[0] = translate("document.available");
		docValues[1] = translate("document.mandatory");
		usageValues[0] = translate("document.wizard");
		usageValues[1] = translate("document.staff");
		
		loadFeedbackConfigurations();
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		helpContainer = tabsConfigurationDelegate.initHelpTexts(positionLanguages, tabConfiguration, formLayout, mainForm,
				helpEls, null, getWindowControl(), false, readOnly);
		
		setFormDescription("edit.form_description.document");
		formLayout.setElementCssClass("o_sel_edit_position_documents_form");
		
		initColumnsModel();
		tableModel = new PositionDocumentsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel,
				DocumentEnum.values().length + 1, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);

		FormSubmit submitButton = uifactory.addFormSubmitButton("submit", formLayout);
		submitButton.setVisible(!readOnly);
		initPreviewButtons(formLayout);
		FormCancel cancelButton = uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		cancelButton.setVisible(!readOnly);
	}
	
	private void loadFeedbackConfigurations() {
		if(recruitingModule.isMembersFeedbackEnabled() && position.getKey() != null) {
			String defaultConfigurationName = translate("edit.apps.feedback.default.configuration");
			List<ApplicationsFeedbackConfiguration>  allConfigurations = feedbackService
					.getOrCreateApplicationsFeedbackConfigurations(defaultConfigurationName, position);
			configurations = allConfigurations.stream()
					.filter(ApplicationsFeedbackConfiguration::isEnabled)
					.collect(Collectors.toList());
			
		} else {
			configurations = new ArrayList<>();
		}
	}
	
	private void initColumnsModel() {
		columnsModel.clear();
		
		boolean multiLanguages = false;
		if(positionLanguages.size() > 1 && position.getAvailableLanguagesArray().length != 1) {
			multiLanguages = true;
		}
		int inputAlignement = multiLanguages ? FlexiColumnModel.ALIGNMENT_RIGHT : FlexiColumnModel.ALIGNMENT_LEFT;
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.enabled));
		DefaultFlexiColumnModel nameCol = new DefaultFlexiColumnModel(DocumentCols.documentName);
		nameCol.setAlignment(inputAlignement);
		columnsModel.addFlexiColumnModel(nameCol);
		if(multiLanguages) {
			DefaultFlexiColumnModel editLabelCol = new DefaultFlexiColumnModel(DocumentCols.editDocumentName);
			editLabelCol.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
			columnsModel.addFlexiColumnModel(editLabelCol);
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.usage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.mandatory));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.combined));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.documentSize));
		if(recruitingModule.isReferenceEnabled()) {
			if(position.isRefereeRecommendationEnabled()) {
				String title = translate("title.document.visible.referees");
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.referees, new DocumentVisibilityRenderer(title)));
			}
			if(position.isExpertRecommendationEnabled()) {
				String title = translate("title.document.visible.experts");
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.experts, new DocumentVisibilityRenderer(title)));
			}
		}
		if(recruitingModule.isReferenceEnabled() && recruitingModule.isComparativeAssessmentExpertsEnabled() && position.isComparativeAssessmentExpertEnabled()) {
			String title = translate("title.document.visible.comparative.experts");
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DocumentCols.comparativeExperts, new DocumentVisibilityRenderer(title)));
		}
		
		if(recruitingModule.isMembersFeedbackEnabled()) {
			for(int i=0; i<configurations.size(); i++) {
				ApplicationsFeedbackConfiguration configuration = configurations.get(i);
				if(configuration.isEnabled()) {
					String title = translate("title.document.visible.members");
					DefaultFlexiColumnModel columnModel = new DefaultFlexiColumnModel("config", i + FEEDBACKS_OFFSET, false, null, new DocumentVisibilityRenderer(title));
					columnModel.setHeaderLabel(configuration.getConfigurationName());
					columnsModel.addFlexiColumnModel(columnModel);
				}
			}
		}
		
		if(!readOnly) {
			StaticFlexiCellRenderer editRenderer = new StaticFlexiCellRenderer(translate("edit"), "edit");
			editRenderer.setPush(true);
			editRenderer.setDirtyCheck(false);
			editRenderer.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "edit", null, -1, "edit", false, null, FlexiColumnModel.ALIGNMENT_LEFT, editRenderer));
		}

		if(tableEl != null) {
			tableEl.reset(false, false, true);
			for(int i=0; i<columnsModel.getColumnCount(); i++) {
				tableEl.setColumnModelVisible(columnsModel.getColumnModel(i), true);
			}			
		}
	}
	
	private void initPreviewButtons(FormItemContainer formLayout) {
		if(!previewButtons.isEmpty()) {
			for(FormLink previewButton:previewButtons) {
				formLayout.remove(previewButton);
			}
			previewButtons.clear();
		}
		
		List<Locale> locales  = recruitingModule.getPositionLocales(position);
		for(Locale locale:locales) {
			String link;
			if(locales.size() == 1) {
				link = translate("edit.template.preview");
			} else {
				link = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
			}
			FormLink previewButton = uifactory.addFormLink("preview_".concat(locale.getLanguage()), "preview", link, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			previewButton.setUserObject(locale);
			previewButton.setVisible(!readOnly);
			previewButtons.add(previewButton);
		}
		formLayout.contextPut("previewButtons", previewButtons);
	}
	
	private void loadModel() {
		Set<String> available = position.getAvailableDocuments();
		Set<String> mandatory = position.getMandatoryDocuments();
		Set<String> staff = position.getStaffDocuments();
		Set<DocumentEnum> combined = position.getDocumentsInCombinedFile();
		if(combined.isEmpty()) {
			combined.addAll(recruitingModule.getDocumentsInCombinedFile());
		}
		Map<DocumentEnum,Integer> sizes = position.getDocumentSizes();
		Set<DocumentEnum> pdfDocuments = position.getPdfDocuments();
		Set<DocumentEnum> xlsxDocuments = position.getXlsxDocuments();
		Set<DocumentEnum> docxDocuments = position.getDocxDocuments();
		Set<DocumentEnum> jpgDocuments = position.getJpgDocuments();

		Set<String> forExperts = position.getExpertRecommendationDocuments();
		Set<String> forReferees = position.getRefereeRecommendationDocuments();
		Set<String> forComparativeExperts = position.getComparativeAssessmentExpertDocuments();

		List<PositionDocumentRow> rows = new ArrayList<>(DocumentEnum.values().length);
		for(DocumentOption docOption:recruitingModule.getDocumentOptions()) {
			DocumentEnum doc = docOption.getDoc();
			if(DocumentEnum.combined == doc) {
				continue;
			}
			
			PositionDocumentRow row = new PositionDocumentRow(doc);
			row.setTypePdf(pdfDocuments.contains(doc));
			row.setTypeXlsx(xlsxDocuments.contains(doc));
			row.setTypeDocx(docxDocuments.contains(doc));
			row.setTypeJpg(jpgDocuments.contains(doc));
			
			forgeRow(row, docOption, flc);
			rows.add(row);
			
			for(Locale locale:positionLanguages) {
				String documentName = position.getDocumentName(doc, locale);
				row.setDocumentNames(documentName, locale);
				String documentExplain = position.getDocumentExplain(doc, locale);
				row.setDocumentExplain(documentExplain, locale);
				if(locale.getLanguage().equals(getLocale().getLanguage()) || positionLanguages.size() == 1) {
					row.getDocumentNameEl().setValue(documentName);
					row.getDocumentNameEl().setUserObject(locale);
				}
			}
			
			boolean enable = available.contains(doc.name());
			row.getEditNameButton().setVisible(enable);
			if(enable) {
				row.getEnableEl().select(onKeys[0], true);
			}
			if(mandatory.contains(doc.name())) {
				row.getMandatoryEl().select(onKeys[0], true);
			}
			if(staff.contains(doc.name())) {
				row.getUsageEl().select(usageKeys[1], true);
			} else {
				row.getUsageEl().select(usageKeys[0], true);
			}
			if(combined.contains(doc) && row.isHasOnlyPdf()) {
				row.getCombinedEl().select(onKeys[0], true);
			}
			if(sizes.containsKey(doc)) {
				row.getDocumentSizeEl().setValue(sizes.get(doc).toString());
			}
			
			row.setExperts(forExperts.contains("all") || forExperts.contains(doc.name()));
			row.setReferees(forReferees.contains("all") || forReferees.contains(doc.name()));
			row.setComparativeExperts(forComparativeExperts.contains("all") || forComparativeExperts.contains(doc.name()));
			
			boolean[] members = new boolean[configurations.size()];
			for(int i=0; i<configurations.size(); i++) {
				members[i] = configurations.get(i).getDocuments().contains(doc.name());
			}
			
			row.setFeedbackMembers(members);
			enabled(row, enable);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		tableEl.setElementCssClass("o_edit_ml_table");
	}
	
	private void forgeRow(PositionDocumentRow row, DocumentOption docOption, FormItemContainer formLayout) {
		MultipleSelectionElement enableEl = uifactory.addCheckboxesHorizontal("ena_" + (++count), null, formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.setDomReplacementWrapperRequired(false);
		enableEl.setEnabled(!readOnly);
		row.setEnableEl(enableEl);
		
		String name;
		if(positionLanguages.size() == 1 && !positionLanguages.get(0).equals(getLocale())) {
			Translator translator = Util.createPackageTranslator(PositionController.class, positionLanguages.get(0));
			name = translator.translate(row.getDocument().i18nKey());
		} else {
			name = translate(row.getDocument().i18nKey());
		}
		
		TextElement nameEl = uifactory.addTextElement("docname_" + (++count), null, 100, name, formLayout);
		nameEl.setDomReplacementWrapperRequired(false);
		nameEl.setPlaceholderText(name);
		nameEl.setEnabled(!readOnly);
		row.setDocumentNameEl(nameEl);
		
		MultipleSelectionElement mandatoryEl = uifactory.addCheckboxesHorizontal("mandatory_" + (++count), null, formLayout, onKeys, onValues);
		mandatoryEl.setEnabled(!readOnly);
		row.setMandatoryEl(mandatoryEl);
		
		SingleSelection usageEl = uifactory.addDropdownSingleselect("usage_" + (++count), null, formLayout, usageKeys, usageValues, null);
		usageEl.addActionListener(FormEvent.ONCHANGE);
		usageEl.setDomReplacementWrapperRequired(false);
		usageEl.setEnabled(!readOnly);
		usageEl.setUserObject(row);
		row.setUsageEl(usageEl);
		
		MultipleSelectionElement combinedEl = uifactory.addCheckboxesHorizontal("combined_" + (++count), null, formLayout, onKeys, onValues);
		combinedEl.setDomReplacementWrapperRequired(false);
		row.setCombinedEl(combinedEl);
		
		String size = Integer.toString(docOption.getMaxSize());
		TextElement sizeEl = uifactory.addTextElement("docsize_" + (++count), null, 4, size, formLayout);
		sizeEl.setDomReplacementWrapperRequired(false);
		sizeEl.setDisplaySize(4);
		sizeEl.setEnabled(!readOnly);
		row.setDocumentSizeEl(sizeEl);
		
		FormLink editNameButton = uifactory.addFormLink("mlname_" + (++count), "mlname", "", null, null, Link.BUTTON | Link.NONTRANSLATED);
		editNameButton.setDomReplacementWrapperRequired(false);
		editNameButton.setIconLeftCSS("o_icon o_icon-lg o_icon_language");
		editNameButton.setUserObject(row);
		editNameButton.setEnabled(!readOnly);
		row.setEditNameButton(editNameButton);
	}
	
	@Override
	public Position getPosition() {
		return position;
	}
	
	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		
		positionLanguages = recruitingModule.getPositionLocales(position);
		
		tabsConfigurationDelegate.updateHelps(positionLanguages, tabConfiguration, helpContainer,
				helpEls, null, getWindowControl(), false);
		
		loadFeedbackConfigurations();
		initColumnsModel();
		loadModel();
		initPreviewButtons(flc);
		
		if(tableEl != null) {
			tableEl.reset(false, false, true);
			for(int i=0; i<columnsModel.getColumnCount(); i++) {
				tableEl.setColumnModelVisible(columnsModel.getColumnModel(i), true);
			}			
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(documentsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(editDocumentConfigurationCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doFinalizeEdit(editDocumentConfigurationCtrl.getDocumentRow(), editDocumentConfigurationCtrl.isDocumentEnabled());
			}
			markDirty();
			cmc.deactivate();
			cleanUp();
		} else if(editDocumentNameCtrl == source) {
			if(editDocumentNameCallout != null) {
				editDocumentNameCallout.deactivate();
			}
			markDirty();
			cleanUp();
		} else if(cmc == source || editDocumentNameCallout == source) {
			markDirty();
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editDocumentConfigurationCtrl);
		removeAsListenerAndDispose(editDocumentNameCallout);
		removeAsListenerAndDispose(editDocumentNameCtrl);
		removeAsListenerAndDispose(documentsCtrl);
		removeAsListenerAndDispose(cmc);
		editDocumentConfigurationCtrl = null;
		editDocumentNameCallout = null;
		editDocumentNameCtrl = null;
		documentsCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		List<PositionDocumentRow> rows = tableModel.getObjects();
		for(PositionDocumentRow row:rows) {
			row.getDocumentSizeEl().clearError();
			String value = row.getDocumentSizeEl().getValue();
			if(StringHelper.containsNonWhitespace(value) && !StringHelper.isLong(value)) {
				row.getDocumentSizeEl().setErrorKey("form.error.nointeger");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}
		String before = auditService.toAuditXml(position);

		commitSettings();
		
		tabsConfigurationDelegate.save(position, tabConfiguration, helpEls, null, null);

		position = recruitingService.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update position: {}", position.toStringFull());
		
		String after = auditService.toAuditXml(position);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}
	
	private void commitSettings() {
		Set<String> available = new HashSet<>();
		Set<String> mandatory = new HashSet<>();
		Set<String> staff = new HashSet<>();
		Set<DocumentEnum> combined = new HashSet<>();
		Map<DocumentEnum, Integer> sizes = new EnumMap<>(DocumentEnum.class);
		List<PositionDocumentRow> rows = tableModel.getObjects();
		
		List<DocumentEnum> pdfDocuments = new ArrayList<>();
		List<DocumentEnum> xlsxDocuments = new ArrayList<>();
		List<DocumentEnum> docxDocuments = new ArrayList<>();
		List<DocumentEnum> jpgDocuments = new ArrayList<>();
		
		for(PositionDocumentRow row:rows) {
			DocumentEnum doc = row.getDocument();
			String name = doc.name();
			if(row.getEnableEl().isAtLeastSelected(1)) {
				available.add(name);
			}
			if(row.getMandatoryEl().isAtLeastSelected(1)) {
				mandatory.add(name);
			}
			if(row.getUsageEl().isOneSelected() && "staff".equals(row.getUsageEl().getSelectedKey())) {
				staff.add(name);
			}
			
			String sizeStr = row.getDocumentSizeEl().getValue();
			if(StringHelper.containsNonWhitespace(sizeStr) && StringHelper.isLong(sizeStr)) {
				sizes.put(doc, Integer.valueOf(sizeStr));
			}
			for(Locale locale:positionLanguages) {
				Translator translator = getLocale().equals(locale) ? getTranslator() : Util.createPackageTranslator(PositionController.class, locale);
				String defaultDocName = translator.translate(doc.i18nKey());
				String defaultExplainName = translator.translate(doc.i18nExplainKey());
				
				String docName;
				if(locale.equals(row.getDocumentNameEl().getUserObject())) {
					docName = row.getDocumentNameEl().getValue();
				} else {
					docName = row.getDocumentName(locale);
				}
				
				if(StringHelper.containsNonWhitespace(docName) && !docName.equals(defaultDocName)) {
					position.setDocumentName(doc, locale, docName);
				} else {
					position.setDocumentName(doc, locale, null);
				}
				
				String explain = row.getDocumentExplain(locale);
				if(StringHelper.containsNonWhitespace(explain) && !explain.equals(defaultExplainName)) {
					position.setDocumentExplain(doc, locale, explain);
				} else {
					position.setDocumentExplain(doc, locale, null);
				}
			}
			
			if(row.isTypePdf()) {
				pdfDocuments.add(doc);
			}
			if(row.isTypeXlsx()) {
				xlsxDocuments.add(doc);
			}
			if(row.isTypeDocx()) {
				docxDocuments.add(doc);
			}
			if(row.isTypeJpg()) {
				jpgDocuments.add(doc);
			}
			
			if(row.getCombinedEl().isAtLeastSelected(1)
					&& row.isHasOnlyPdf()) {
				combined.add(doc);
			}
		}
		
		position.setPdfDocuments(pdfDocuments);
		position.setXlsxDocuments(xlsxDocuments);
		position.setDocxDocuments(docxDocuments);
		position.setJpgDocuments(jpgDocuments);
		position.setAvailableDocuments(available);
		position.setMandatoryDocuments(mandatory);
		position.setStaffDocuments(staff);
		position.setDocumentsInCombinedFile(combined);
		position.setDocumentSizes(sizes);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("preview".equals(link.getCmd()) && link.getUserObject() instanceof Locale) {
				doPreview(ureq, (Locale)link.getUserObject());
			} else if("mlname".equals(link.getCmd()) && link.getUserObject() instanceof PositionDocumentRow) {
				doEditDocumentName(ureq, link, (PositionDocumentRow)link.getUserObject());
			}
		} else if(source instanceof MultipleSelectionElement) {
			MultipleSelectionElement sel = (MultipleSelectionElement) source;
			String name = sel.getName();
			if(name.startsWith("ena_")) {
				enabled((PositionDocumentRow)sel.getUserObject(), sel.isAtLeastSelected(1));
				markDirty();
			}
			checkEmptyDocumentNames();
		} else if(source instanceof SingleSelection) {
			SingleSelection sel = (SingleSelection)source;
			String name = sel.getName();
			if(name.startsWith("usage_")) {
				PositionDocumentRow row = (PositionDocumentRow)sel.getUserObject();
				MultipleSelectionElement enableEl = row.getEnableEl();
				enabled((PositionDocumentRow)sel.getUserObject(), enableEl.isAtLeastSelected(1));
				markDirty();
			}
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					PositionDocumentRow row = tableModel.getObject(se.getIndex());
					doEdit(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doEditDocumentName(UserRequest ureq, FormLink link, PositionDocumentRow row) {
		commitSettings();
		editDocumentNameCtrl = new PositionEditDocumentNameController(ureq, getWindowControl(), position, row);
		listenTo(editDocumentNameCtrl);

		editDocumentNameCallout = new CloseableCalloutWindowController(ureq, getWindowControl(), editDocumentNameCtrl.getInitialComponent(),
				link.getFormDispatchId(), translate("edit"), true, "");
		listenTo(editDocumentNameCallout);
		editDocumentNameCallout.activate();
	}
	
	private void doEdit(UserRequest ureq, PositionDocumentRow row) {
		commitSettings();
		
		editDocumentConfigurationCtrl = new PositionEditDocumentConfigurationController(ureq, getWindowControl(), position, row);
		listenTo(editDocumentConfigurationCtrl);
		
		String name = row.getDocumentNameEl().getValue();
		if(!StringHelper.containsNonWhitespace(name)) {
			name = translate(row.getDocument().i18nKey());
		}
		String title = translate("edit.document.title", new String[] { StringHelper.escapeHtml(name) });
		cmc = new CloseableModalController(getWindowControl(), "c", editDocumentConfigurationCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doFinalizeEdit(PositionDocumentRow row, boolean enabled) {
		enabled(row, enabled);
		tableEl.reset(false, false, true);
	}
	
	private void checkEmptyDocumentNames() {
		List<PositionDocumentRow> rows = tableModel.getObjects();
		for(PositionDocumentRow row:rows) {
			String value = row.getDocumentNameEl().getValue();
			if(!StringHelper.containsNonWhitespace(value)) {
				Locale locale = (Locale)row.getDocumentNameEl().getUserObject();
				Translator translator = locale == null || getLocale().equals(locale)
						? getTranslator() : Util.createPackageTranslator(PositionController.class, locale);
				String defaultDocName = translator.translate(row.getDocument().i18nKey());
				row.getDocumentNameEl().setValue(defaultDocName);
			}
		}
	}

	private void enabled(PositionDocumentRow row, boolean enable) {
		row.getDocumentNameEl().setEnabled(enable && !readOnly);
		row.getDocumentSizeEl().setEnabled(enable && !readOnly);
		row.getMandatoryEl().setEnabled(enable && !readOnly);
		if(!enable) {
			row.getMandatoryEl().uncheckAll();
		} else if("staff".equals(row.getUsageEl().getSelectedKey())) {
			row.getMandatoryEl().uncheckAll();
			row.getMandatoryEl().setEnabled(false);
		}
		row.getUsageEl().setEnabled(enable && !readOnly);
		row.getEditNameButton().setVisible(enable && !readOnly);
		
		if(row.isHasOnlyPdf()) {
			row.getCombinedEl().setEnabled(enable && !readOnly);
		} else {
			if(row.getCombinedEl().isAtLeastSelected(1)) {
				row.getCombinedEl().uncheckAll();
			}
			row.getCombinedEl().setEnabled(false);
		}
	}
	
	private void doPreview(UserRequest ureq, Locale locale) {
		commitSettings();
		
		if(!locale.getLanguage().equals(getLocale().getLanguage())) {
			ureq = new SyntheticUserRequest(getIdentity(), locale);
		}
		
		TabConfiguration tempConfiguration = new TabConfiguration();
		for(TextElement helpEl:helpEls) {
			Locale loc = (Locale)helpEl.getUserObject();
			tempConfiguration.setHelp(helpEl.getValue(), loc);
		}
		
		Application application = ReferenceHelper.generateDummyApplication(position);
		documentsCtrl = new DocumentsController(ureq, getWindowControl(), null, position, application, tempConfiguration, false, false, true);
		listenTo(documentsCtrl);
		
		String title;
		if(previewButtons.size() == 1) {
			title = translate("edit.template.preview");
		} else {
			title = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
		}
		cmc = new CloseableModalController(getWindowControl(), "c", documentsCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}