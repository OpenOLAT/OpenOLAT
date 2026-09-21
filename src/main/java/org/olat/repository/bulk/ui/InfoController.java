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
package org.olat.repository.bulk.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.oaipmh.OAIPmhModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 15 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class InfoController extends StepFormBasicController {

	private static final String[] CHANGE_KEYS = new String[] {"change"};
	private static final String NO_CHANGE_KEY = "noChange";
	private static final String ON_KEY = "on";
	private static final String OFF_KEY = "off";

	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>(4);
	private TextElement authorsEl;
	private TextElement languageEl;
	private TextElement expenditureOfWorkEl;
	private SingleSelection oerPubEl;
	private MultipleSelectionElement displayChangeEl;
	private SingleSelection eventsEl;
	private SingleSelection meetTeachersEl;
	private SingleSelection certificateEl;
	private SingleSelection creditPointsEl;
	private MultipleSelectionElement taughtByChangeEl;
	private SingleSelection taughtByTeachersEl;
	private SingleSelection taughtByCoachesEl;
	private SingleSelection taughtByOwnersEl;
	private final List<SingleSelection> displayEls = new ArrayList<>(4);
	private final List<SingleSelection> taughtByEls = new ArrayList<>(3);
	private FormLayoutContainer infoCont;

	private final SettingsContext context;
	private final SettingsBulkEditables editables;
	private final String[] changeValues;

	@Autowired
	private OAIPmhModule oaiPmhModule;

	public InfoController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
		this.editables = (SettingsBulkEditables)runContext.get(SettingsBulkEditables.DEFAULT_KEY);
		this.changeValues = new String[] {translate("settings.bulk.change")};

		initForm(ureq);
		updateDisplayUI();
		updateTaughtByUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		infoCont = FormLayoutContainer.createDefaultFormLayout("infoCont", getTranslator());
		infoCont.setFormTitle(translate("settings.bulk.info.title"));
		infoCont.setFormInfo(RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), "settings.bulk.change.fields"));
		infoCont.setRootForm(mainForm);
		formLayout.add(infoCont);

		authorsEl = uifactory.addTextElement("settings.bulk.authors", 255, context.getAuthors(), infoCont);
		decorate(authorsEl, SettingsBulkEditable.authors);

		languageEl = uifactory.addTextElement("settings.bulk.mainLanguage", null, 16, context.getMainLanguage(), infoCont);
		decorate(languageEl, SettingsBulkEditable.mainLanguage);

		expenditureOfWorkEl = uifactory.addTextElement("settings.bulk.expenditureOfWork", null, 100, context.getExpenditureOfWork(), infoCont);
		expenditureOfWorkEl.setExampleKey("details.expenditureOfWork.example", null);
		decorate(expenditureOfWorkEl, SettingsBulkEditable.expenditureOfWork);

		uifactory.addSpacerElement("course.only.spacer", infoCont, false);

		String courseOnlyInfo = "<i class='o_icon o_icon_warn'> </i> " + translate("settings.bulk.course.only.multi");
		StaticTextElement courseOnlyEl = uifactory.addStaticTextElement("course.only.info", null, courseOnlyInfo, infoCont);
		courseOnlyEl.setElementCssClass("o_form_explanation");

		displayChangeEl = uifactory.addCheckboxesHorizontal("display.change", "settings.bulk.info.display", infoCont, CHANGE_KEYS, changeValues);
		displayChangeEl.addActionListener(FormEvent.ONCLICK);
		displayChangeEl.setEnabled(isOneEditable(SettingsBulkEditable.infoEvents, SettingsBulkEditable.infoMeetTeachers,
				SettingsBulkEditable.infoCertificate, SettingsBulkEditable.infoCreditPoints));

		eventsEl = addInfoRadios("settings.bulk.info.events", infoCont, SettingsBulkEditable.infoEvents, context.getInfoEvents(), displayEls);
		meetTeachersEl = addInfoRadios("settings.bulk.info.meet.teachers", infoCont, SettingsBulkEditable.infoMeetTeachers, context.getInfoMeetTeachers(), displayEls);
		meetTeachersEl.addActionListener(FormEvent.ONCHANGE);
		certificateEl = addInfoRadios("settings.bulk.info.certificate", infoCont, SettingsBulkEditable.infoCertificate, context.getInfoCertificate(), displayEls);
		creditPointsEl = addInfoRadios("settings.bulk.info.credit.points", infoCont, SettingsBulkEditable.infoCreditPoints, context.getInfoCreditPoints(), displayEls);

		boolean displaySelected = context.isSelected(SettingsBulkEditable.infoEvents)
				|| context.isSelected(SettingsBulkEditable.infoMeetTeachers)
				|| context.isSelected(SettingsBulkEditable.infoCertificate)
				|| context.isSelected(SettingsBulkEditable.infoCreditPoints);
		displayChangeEl.select(displayChangeEl.getKey(0), displaySelected);

		uifactory.addSpacerElement("info.spacer", infoCont, false);

		taughtByChangeEl = uifactory.addCheckboxesHorizontal("taught.by.change", "settings.bulk.info.taught.by", infoCont, CHANGE_KEYS, changeValues);
		taughtByChangeEl.addActionListener(FormEvent.ONCLICK);
		taughtByChangeEl.setEnabled(isOneEditable(SettingsBulkEditable.infoTaughtByTeachers,
				SettingsBulkEditable.infoTaughtByCoaches, SettingsBulkEditable.infoTaughtByOwners));

		taughtByTeachersEl = addInfoRadios("settings.bulk.info.taught.by.teachers", infoCont, SettingsBulkEditable.infoTaughtByTeachers, context.getInfoTaughtByTeachers(), taughtByEls);
		taughtByCoachesEl = addInfoRadios("settings.bulk.info.taught.by.coaches", infoCont, SettingsBulkEditable.infoTaughtByCoaches, context.getInfoTaughtByCoaches(), taughtByEls);
		taughtByOwnersEl = addInfoRadios("settings.bulk.info.taught.by.owners", infoCont, SettingsBulkEditable.infoTaughtByOwners, context.getInfoTaughtByOwners(), taughtByEls);

		boolean taughtBySelected = context.isSelected(SettingsBulkEditable.infoTaughtByTeachers)
				|| context.isSelected(SettingsBulkEditable.infoTaughtByCoaches)
				|| context.isSelected(SettingsBulkEditable.infoTaughtByOwners);
		taughtByChangeEl.select(taughtByChangeEl.getKey(0), taughtBySelected);

		uifactory.addSpacerElement("oer.spacer", infoCont, false);

		SelectionValues oerPubElSV = new SelectionValues();
		oerPubElSV.add(entry(ON_KEY, translate("on")));
		oerPubElSV.add(entry(OFF_KEY, translate("off")));
		oerPubEl = uifactory.addRadiosHorizontal("settings.bulk.oer", infoCont, oerPubElSV.keys(), oerPubElSV.values());
		if (oaiPmhModule.isEnabled()) {
			if (context.isCanIndexMetadata()) {
				oerPubEl.select(ON_KEY, true);
			} else {
				oerPubEl.select(OFF_KEY, true);
			}
			decorate(oerPubEl, SettingsBulkEditable.oerPub);
		} else {
			oerPubEl.setVisible(false);
		}
	}

	private void decorate(FormItem item, SettingsBulkEditable editable) {
		boolean selected = context.isSelected(editable);
		String itemName = item.getName();
		MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("cbx_" + itemName, itemName, infoCont, CHANGE_KEYS, changeValues);
		checkbox.select(checkbox.getKey(0), selected);
		checkbox.setEnabled(editables.isEditable(editable));
		checkbox.addActionListener(FormEvent.ONCLICK);
		checkbox.setUserObject(item);
		checkboxSwitch.add(checkbox);

		item.setLabel(null, null);
		item.setVisible(selected);
		item.setUserObject(checkbox);

		infoCont.moveBefore(checkbox, item);
	}

	private SingleSelection addInfoRadios(String i18nLabel, FormLayoutContainer formLayout, SettingsBulkEditable editable,
			Boolean contextValue, List<SingleSelection> group) {
		SelectionValues radioSV = new SelectionValues();
		radioSV.add(entry(NO_CHANGE_KEY, translate("settings.bulk.no.change")));
		radioSV.add(entry(ON_KEY, translate("on")));
		radioSV.add(entry(OFF_KEY, translate("off")));
		SingleSelection radiosEl = uifactory.addRadiosHorizontal(i18nLabel, formLayout, radioSV.keys(), radioSV.values());
		if (contextValue == null) {
			radiosEl.select(NO_CHANGE_KEY, true);
		} else if (contextValue.booleanValue()) {
			radiosEl.select(ON_KEY, true);
		} else {
			radiosEl.select(OFF_KEY, true);
		}
		radiosEl.setEnabled(editables.isEditable(editable));
		group.add(radiosEl);
		return radiosEl;
	}

	private boolean isOneEditable(SettingsBulkEditable... infoEditables) {
		for (SettingsBulkEditable editable : infoEditables) {
			if (editables.isEditable(editable)) {
				return true;
			}
		}
		return false;
	}

	private void updateDisplayUI() {
		boolean visible = displayChangeEl.isAtLeastSelected(1);
		displayEls.forEach(el -> el.setVisible(visible));
		infoCont.setDirty(true);
	}

	private void updateTaughtByUI() {
		boolean visible = taughtByChangeEl.isAtLeastSelected(1);
		taughtByEls.forEach(el -> el.setVisible(visible));
		infoCont.setDirty(true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == displayChangeEl) {
			updateDisplayUI();
		} else if (source == taughtByChangeEl) {
			updateTaughtByUI();
		} else if (checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			infoCont.setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		taughtByChangeEl.clearError();
		if (isSelected(meetTeachersEl, ON_KEY)) {
			boolean oneTaughtByOn = taughtByChangeEl.isAtLeastSelected(1)
					&& (isSelected(taughtByTeachersEl, ON_KEY)
							|| isSelected(taughtByCoachesEl, ON_KEY)
							|| isSelected(taughtByOwnersEl, ON_KEY));
			if (!oneTaughtByOn) {
				taughtByChangeEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		}

		return allOk;
	}

	private boolean isSelected(SingleSelection radiosEl, String key) {
		return radiosEl.isVisible() && radiosEl.isOneSelected() && key.equals(radiosEl.getSelectedKey());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.select(SettingsBulkEditable.authors, authorsEl.isVisible());
		if (authorsEl.isVisible()) {
			context.setAuthors(authorsEl.getValue().trim());
		}

		context.select(SettingsBulkEditable.mainLanguage, languageEl.isVisible());
		if (languageEl.isVisible()) {
			context.setMainLanguage(languageEl.getValue().trim());
		}

		context.select(SettingsBulkEditable.expenditureOfWork, expenditureOfWorkEl.isVisible());
		if (expenditureOfWorkEl.isVisible()) {
			context.setExpenditureOfWork(expenditureOfWorkEl.getValue().trim());
		}

		context.select(SettingsBulkEditable.oerPub, oerPubEl.isVisible());
		if (oerPubEl.isVisible()) {
			context.setCanIndexMetadata(oerPubEl.isKeySelected(ON_KEY));
		}

		context.setInfoEvents(applyRadios(eventsEl, SettingsBulkEditable.infoEvents));
		context.setInfoMeetTeachers(applyRadios(meetTeachersEl, SettingsBulkEditable.infoMeetTeachers));
		context.setInfoCertificate(applyRadios(certificateEl, SettingsBulkEditable.infoCertificate));
		context.setInfoCreditPoints(applyRadios(creditPointsEl, SettingsBulkEditable.infoCreditPoints));
		context.setInfoTaughtByTeachers(applyRadios(taughtByTeachersEl, SettingsBulkEditable.infoTaughtByTeachers));
		context.setInfoTaughtByCoaches(applyRadios(taughtByCoachesEl, SettingsBulkEditable.infoTaughtByCoaches));
		context.setInfoTaughtByOwners(applyRadios(taughtByOwnersEl, SettingsBulkEditable.infoTaughtByOwners));

		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	private Boolean applyRadios(SingleSelection radiosEl, SettingsBulkEditable editable) {
		Boolean value = null;
		if (isSelected(radiosEl, ON_KEY)) {
			value = Boolean.TRUE;
		} else if (isSelected(radiosEl, OFF_KEY)) {
			value = Boolean.FALSE;
		}
		context.select(editable, value != null);
		return value;
	}

}
