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
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanNullCellRenderer;
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
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionEditApplicationEditStepsTableModel.EditStepCols;
import org.olat.modules.selectus.ui.position.component.EditStepStatusRenderer;
import org.olat.modules.selectus.ui.position.model.EditStepRow;

/**
 * 
 * Initial date: 12 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditApplicationEditStepsController extends FormBasicController implements PositionEditableController {
	
	private FormLink addStepButton;
	private FlexiTableElement tableEl;
	private PositionEditApplicationEditStepsTableModel tableModel;
	
	private Position position;
	private final boolean readOnly;
	private final boolean customStepsEnabled;
	private List<Locale> positionLanguages;
	
	private CloseableModalController cmc;
	private CloseableCalloutWindowController editCallout;
	private PositionEditCustomStepController editLabelCtrl;
	private ConfirmDeleteStepController confirmDeleteStepCtrl;
	private PositionEditCustomStepController addCustomStepCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditApplicationEditStepsController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, "steps", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.readOnly = readOnly;
		customStepsEnabled = recruitingModule.isPositionCustomStepsEnabled();
		positionLanguages = recruitingModule.getPositionLocales(position);
		
		initForm(ureq);
		loadModel();
		updateAddStep();
	}
	
	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("customSteps", Boolean.valueOf(customStepsEnabled));
		}

		addStepButton = uifactory.addFormLink("add.step", formLayout, Link.BUTTON);
		addStepButton.setVisible(customStepsEnabled);
		
		boolean multiLanguages = (positionLanguages.size() > 1 && (position == null || position.getAvailableLanguagesArray().length != 1));
		int inputAlignement = multiLanguages ? FlexiColumnModel.ALIGNMENT_RIGHT : FlexiColumnModel.ALIGNMENT_LEFT;

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		DefaultFlexiColumnModel nameColumn = new DefaultFlexiColumnModel(EditStepCols.name);
		nameColumn.setAlignment(inputAlignement);
		columnsModel.addFlexiColumnModel(nameColumn);
		if(multiLanguages) {
			DefaultFlexiColumnModel editNameColumn = new DefaultFlexiColumnModel(EditStepCols.edit);
			editNameColumn.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
			columnsModel.addFlexiColumnModel(editNameColumn);
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EditStepCols.status,
				new EditStepStatusRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EditStepCols.infos));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EditStepCols.enable.i18nHeaderKey(), EditStepCols.enable.ordinal(), "enable",
				new BooleanNullCellRenderer(valueOfStaticFlexiCellRenderer("disable.step", "enable"),
						valueOfStaticFlexiCellRenderer("enable.step", "enable"), null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EditStepCols.delete.i18nHeaderKey(), EditStepCols.delete.ordinal(), "delete",
				new BooleanNullCellRenderer(valueOfStaticFlexiCellRenderer("delete", "delete"), null, null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EditStepCols.up.i18nHeaderKey(), EditStepCols.up.ordinal(), "up",
				new BooleanNullCellRenderer(valueOfStaticFlexiCellRenderer("up", "up"), null, null)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EditStepCols.down.i18nHeaderKey(), EditStepCols.down.ordinal(), "down",
				new BooleanNullCellRenderer(valueOfStaticFlexiCellRenderer("down", "down"), null, null)));

		tableModel = new PositionEditApplicationEditStepsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "steps", tableModel, 25, true, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_edit_ml_table o_applications_steps");
		
		uifactory.addFormSubmitButton("save", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	private StaticFlexiCellRenderer valueOfStaticFlexiCellRenderer(String i18nKey, String action) {
		StaticFlexiCellRenderer renderer = new StaticFlexiCellRenderer(translate(i18nKey), action);
		renderer.setPush(true);
		return renderer;
	}
	
	private void loadModel() {
		List<EditStepRow> rows = new ArrayList<>(12);
		rows.add(new EditStepRow(Tab.instructions, translate("edit.step.instructions"),
				translate("explain.step.instructions"), true, false));
		rows.add(new EditStepRow(Tab.dataProtection, translate("edit.step.data.protections"),
				translate("explain.step.data.protections"), true, false));
		rows.add(new EditStepRow(Tab.personalData, translate("edit.step.personal.data"),
				translate("explain.step.personal.data"), true, false));
		
		if(recruitingModule.isPositionAcademicalBackgroundEnabled()) {
			if(recruitingModule.isPositionAcademicalBackgroundConfigurable()) {
				boolean acamdemicalBackgroundEnabled = position.isApplicationAcademicalBackground();
				rows.add(new EditStepRow(Tab.academicalBackground, translate("edit.step.academical.background"),
						translate("explain.step.academical.background.configurable"), acamdemicalBackgroundEnabled, true));
			} else {
				// always enabled
				rows.add(new EditStepRow(Tab.academicalBackground, translate("edit.step.academical.background"),
						translate("explain.step.academical.background"), true, false));
			}
		}
		
		if(recruitingModule.isApplicationProjectEnabled()) {
			boolean projectEnabled = position.isApplicationProject();
			rows.add(new EditStepRow(Tab.project, translate("edit.step.project"),
					translate("explain.step.project"), projectEnabled, true));
		}
		
		if(recruitingModule.isPositionCustomStepsEnabled()) {
			List<Tab> tabsList = position.getCustomTabsList();
			for(Tab tab:tabsList) {
				TabConfiguration configuration = position.getTabConfiguration(tab);
				
				EditStepRow customStep = new EditStepRow(tab, translate("edit.step.custom"),
						translate("explain.step.custom"), true, true);
				customStep.setCustomName(configuration.getTitle());
				customStep.setCustomNameDe(configuration.getTitleDe());
				customStep.setCustomNameFr(configuration.getTitleFr());
				customStep.setEnabled(!configuration.isDisabled());
				rows.add(customStep);
				
				if(!readOnly) {
					forgeRow(customStep, configuration, tab);
				}
			}
		}
		
		boolean hasDocuments = !position.getAvailableDocuments().isEmpty();
		boolean isDocumentsStaffOnly = (hasDocuments && isDocumentsStaffOnly()) || !hasDocuments;
		rows.add(new EditStepRow(Tab.documents, translate("edit.step.documents"),
				translate("explain.step.documents"), hasDocuments, false, isDocumentsStaffOnly));

		if(recruitingModule.isReferenceEnabled()) {	
			boolean refereeEnabled = position.isRefereeRecommendationEnabled();
			boolean expertEnabled = position.isExpertRecommendationEnabled();
			boolean refereeStepEnabled = refereeEnabled
					|| (recruitingModule.isReferenceExpertsBlackListEnabled() && (refereeEnabled || expertEnabled));
			String refereeI18nKey = recruitingModule.isReferenceExpertsBlackListEnabled()
					? "explain.step.references.exclusion.list" : "explain.step.references";
			rows.add(new EditStepRow(Tab.referees, translate("edit.step.references"),
					translate(refereeI18nKey), refereeStepEnabled, false));
		}

		rows.add(new EditStepRow(Tab.reviewAndSubmit, translate("edit.step.review.and.submit"),
				translate("explain.step.review.and.submit"), true, false));
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeRow(EditStepRow row, TabConfiguration configuration, Tab tab) {
		String title = configuration.getTitle(getFieldLocale());
		TextElement titleEl = uifactory.addTextElement("title_".concat(tab.name()), null, 255, title, flc);
		titleEl.setEnabled(!readOnly);
		row.setTitleEl(titleEl);
		titleEl.setUserObject(row);
			
		FormLink editLabelButton = uifactory.addFormLink("mllabel_".concat(tab.name()), "mllabel", "", null, null, Link.BUTTON | Link.NONTRANSLATED);
		editLabelButton.setDomReplacementWrapperRequired(false);
		editLabelButton.setIconLeftCSS("o_icon o_icon-lg o_icon_language");
		editLabelButton.setUserObject(row);
		editLabelButton.setVisible(!readOnly);
		row.setEditLabelButton(editLabelButton);
	}
	
	private Locale getFieldLocale() {
		if(position == null) {
			return recruitingModule.getReportingLocale();
		}

		Locale fieldLocale = null;
		if(positionLanguages != null && !positionLanguages.isEmpty()) {
			if(positionLanguages.size() == 1) {
				fieldLocale = positionLanguages.get(0);
			} else {
				// prefer English if possible
				for(Locale positionLanguage:positionLanguages) {
					if(positionLanguage.getLanguage().equals(getLocale().getLanguage())) {
						fieldLocale = getLocale();
					}
				}
				
				if(fieldLocale == null) {
					fieldLocale = positionLanguages.get(0);
				}
			}
		}
		
		if(fieldLocale == null) {
			fieldLocale = getLocale();
		}
		return fieldLocale;
	}
	
	private boolean isDocumentsStaffOnly() {
		Set<String> available = position.getAvailableDocuments();
		Set<String> staff = position.getStaffDocuments();
		if(available.isEmpty()) {
			return false;
		}
		available.removeAll(staff);
		return available.isEmpty();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addCustomStepCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doFinalizeAddCustomStep(addCustomStepCtrl.getRow());
			}
			cmc.deactivate();
			cleanUp();
		} else if(editLabelCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				tableEl.reset(false, false, true);
			}
			editCallout.deactivate();
			cleanUp();
		} else if(confirmDeleteStepCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doFinalizeDeleteStep(confirmDeleteStepCtrl.getRow());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteStepCtrl);
		removeAsListenerAndDispose(addCustomStepCtrl);
		removeAsListenerAndDispose(editLabelCtrl);
		removeAsListenerAndDispose(editCallout);
		removeAsListenerAndDispose(cmc);
		confirmDeleteStepCtrl = null;
		addCustomStepCtrl = null;
		editLabelCtrl = null;
		editCallout = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addStepButton == source) {
			doAddCustomStep(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				EditStepRow row = tableModel.getObject(se.getIndex());
				if("enable".equals(se.getCommand()) && row != null) {
					row.setEnabled(!row.isEnabled());
				} else if("up".equals(se.getCommand()) && row != null) {
					doMoveUp(row, se.getIndex());
				} else if("down".equals(se.getCommand()) && row != null) {
					doMoveDown(row, se.getIndex());
				} else if("delete".equals(se.getCommand()) && row != null) {
					doConfirmDelete(ureq, se.getIndex());
				}
				tableEl.reset(false, false, true);
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("mllabel".equals(link.getCmd()) && link.getUserObject() instanceof EditStepRow) {
				doEditLabel(ureq, link, (EditStepRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doAddCustomStep(UserRequest ureq) {
		Tab nextTab = nextCustomTab();
		EditStepRow customRow = new EditStepRow(nextTab, translate("edit.step.custom"),
				translate("explain.step.custom"), true, true);
		addCustomStepCtrl = new PositionEditCustomStepController(ureq, getWindowControl(), position, customRow);
		listenTo(addCustomStepCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "c", addCustomStepCtrl.getInitialComponent(), translate("edit.step.custom"));
		cmc.activate();
		listenTo(cmc);	
	}
	
	private Tab nextCustomTab() {
		List<Tab> usedTabs = new ArrayList<>();
		List<EditStepRow> rows = tableModel.getObjects();
		for(EditStepRow row:rows) {
			usedTabs.add(row.step());
		}
		for(Tab tab:Tab.values()) {
			if(!tab.customStep() || usedTabs.contains(tab)) {
				continue;
			}
			return tab;
		}
		return null;
	}
	
	private void doFinalizeAddCustomStep(EditStepRow customRow) {
		int customRowIndex = tableModel.indexOf(customRow.step());
		if(customRowIndex < 0) {
			TabConfiguration configuration = position.getTabConfiguration(customRow.step());
			forgeRow(customRow, configuration, customRow.step());
			if(customRow.getTitleEl() != null) {
				customRow.getTitleEl().setValue(customRow.getCustomName(getFieldLocale()));
			}
			
			int reviewIndex = tableModel.indexOf(Tab.documents);
			List<EditStepRow> rows = tableModel.getObjects();
			rows.add(reviewIndex, customRow);
			tableModel.setObjects(rows);
		}
		tableEl.reset(true, true, true);
		updateAddStep();
	}
	
	private void updateAddStep() {
		List<EditStepRow> rows = tableModel.getObjects();
		long numOfCustomStep = rows.stream()
				.filter(row -> row.step().customStep())
				.count();
		addStepButton.setEnabled(numOfCustomStep < 4);
	}
	
	private void doMoveUp(EditStepRow row, int index) {
		List<EditStepRow> rows = tableModel.getObjects();
		int previousIndex = index - 1;
		if(previousIndex >= 0 && previousIndex < rows.size()) {
			rows.remove(index);
			rows.add(previousIndex, row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private void doMoveDown(EditStepRow row, int index) {
		List<EditStepRow> rows = tableModel.getObjects();
		int nextIndex = index + 1;
		if(nextIndex >= 0 && nextIndex < rows.size()) {
			rows.remove(index);
			rows.add(nextIndex, row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void doConfirmDelete(UserRequest ureq, int index) {
		EditStepRow row = tableModel.getObject(index);
		if(row == null) {
			loadModel();
		} else {
			confirmDeleteStepCtrl = new ConfirmDeleteStepController(ureq, this.getWindowControl(), row);
			listenTo(confirmDeleteStepCtrl);
			
			String title = translate("confirm.delete.step.title", StringHelper.escapeHtml(row.getCustomName(getLocale())));
			cmc = new CloseableModalController(getWindowControl(), "c", confirmDeleteStepCtrl.getInitialComponent(), title);
			cmc.activate();
			listenTo(cmc);	
		}
	}
	
	private void doFinalizeDeleteStep(EditStepRow row) {
		List<EditStepRow> rows = tableModel.getObjects();
		rows.remove(row);
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		updateAddStep();
	}
	
	private void doEditLabel(UserRequest ureq, FormLink link, EditStepRow row) {
		commitChanges();
		editLabelCtrl = new PositionEditCustomStepController(ureq, getWindowControl(),  position, row);
		listenTo(editLabelCtrl);

		String title = translate("edit.step.custom");
		editCallout = new CloseableCalloutWindowController(ureq, getWindowControl(), editLabelCtrl.getInitialComponent(),
				link.getFormDispatchId(), title, true, "");
		listenTo(editCallout);
		editCallout.activate();
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		List<EditStepRow> rows = tableModel.getObjects();
		for(EditStepRow row:rows) {
			TextElement titleEl = row.getTitleEl();
			if(titleEl != null && titleEl.isEnabled()) {
				titleEl.clearError();
				if(!StringHelper.containsNonWhitespace(titleEl.getValue())) {
					titleEl.setErrorKey("form.legende.mandatory");
					allOk &= false;
				} else if(titleEl.getValue().length() > 35) {
					titleEl.setErrorKey("input.toolong", new String[]{ "35" });
					allOk &= false;
				}
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
		
		commitChanges();

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
	
	private void commitChanges() {
		Locale fieldLocale = getFieldLocale();
		List<EditStepRow> rows = tableModel.getObjects();
		List<Tab> customStep = new ArrayList<>();
		for(EditStepRow row:rows) {
			if(row.step() == Tab.academicalBackground) {
				position.setApplicationAcademicalBackground(row.isEnabled());
			} else if(row.step() == Tab.project) {
				position.setApplicationProject(row.isEnabled());
			} else if(row.step().customStep()) {
				if(row.isDeleted()) {
					// ignore
				} else {
					customStep.add(row.step());
					
					TabConfiguration customTab = position.getTabConfiguration(row.step());
					customTab.setDisabled(!row.isEnabled());
					customTab.setTitle(row.getCustomName());
					customTab.setTitleDe(row.getCustomNameDe());
					customTab.setTitleFr(row.getCustomNameFr());
					if(row.getTitleEl() != null && StringHelper.containsNonWhitespace(row.getTitleEl().getValue())) {
						customTab.setTitle(row.getTitleEl().getValue(), fieldLocale);
					}
					position.setTabConfiguration(row.step(), customTab);
				}
			}
		}
		
		position.setCustomTabsList(customStep);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

}
