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
package org.olat.login.webauthn.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.login.LoginModule;
import org.olat.login.webauthn.OLATWebAuthnManager;
import org.olat.login.webauthn.PasskeyLevels;
import org.springframework.beans.factory.annotation.Autowired;

import com.webauthn4j.data.AttestationConveyancePreference;
import com.webauthn4j.data.UserVerificationRequirement;

/**
 * 
 * Initial date: 10 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WebAuthnAuthenticationAdminController extends FormBasicController {
	
	private static final String UPGRADE_KEY = "upgrade";

	private FormToggle enabledEl;
	private SingleSelection attestationEl;
	private SingleSelection skipPasskeyEl;
	private SingleSelection userVerificationEl;
	private MultipleSelectionElement upgradeEl;
	private FlexiTableElement levelsEl;
	private LevelsDataModel levelModel;
	private FormLayoutContainer levelsCont;
	private FormLayoutContainer expertCont;
	
	private int count = 0;
	
	private CloseableModalController cmc;
	private ConfirmDisablePasskeyController confirmDisableCtrl;
	
	@Autowired
	private LoginModule loginModule;
	@Autowired
	private OLATWebAuthnManager webAuthnManager;
	
	public WebAuthnAuthenticationAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "passkey_admin");
		initForm(ureq);
		loadLevels();
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer enableCont = uifactory.addDefaultFormLayout("enableCont", null, formLayout);
		initEnableform(enableCont);
		
		String levelPage = velocity_root + "/passkey_admin_levels.html";
		levelsCont = uifactory.addCustomFormLayout("levelsCont", null, levelPage, formLayout);
		initLevelsForm(levelsCont);
		
		expertCont = uifactory.addDefaultFormLayout("expertCont", null, formLayout);
		initExpertForm(expertCont);
	}
	
	private void initEnableform(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("admin.configuration.title"));
		formLayout.setElementCssClass("o_sel_passkey_admin_configuration");
		
		enabledEl = uifactory.addToggleButton("enabled.passkey", "enabled.passkey", translate("on"), translate("off"), formLayout);
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		if(loginModule.isOlatProviderWithPasskey()) {
			enabledEl.toggleOn();
		} else {
			enabledEl.toggleOff();
		}
	}
	
	private void initLevelsForm(FormLayoutContainer formLayout) {
		SelectionValues upgradePK = new SelectionValues();
		upgradePK.add(SelectionValues.entry(UPGRADE_KEY, translate("level.upgrade.option")));
		upgradeEl = uifactory.addCheckboxesHorizontal("level.upgrade", "level.upgrade", formLayout, upgradePK.keys(), upgradePK.values());
		upgradeEl.addActionListener(FormEvent.ONCHANGE);
		upgradeEl.setAjaxOnly(true);
		upgradeEl.setFormLayout("3_9");
		if(loginModule.isPasskeyUpgradeAllowed()) {
			upgradeEl.select(upgradePK.keys()[0], true);
		}
		
		DropdownItem allRoleDropdown = uifactory.addDropdownMenu("tool.roles", "tool.roles", null, formLayout, getTranslator());
		allRoleDropdown.setElementCssClass("o_sel_passkey_level_all_roles");
		allRoleDropdown.setOrientation(DropdownOrientation.right);
		
		for(PasskeyLevels level:PasskeyLevels.values()) {
			String levelKey = level.name().toLowerCase();
			FormLink levelLink = uifactory.addFormLink("tool.level.".concat(levelKey), formLayout, Link.LINK);
			levelLink.setElementCssClass("o_sel_passkey_".concat(levelKey));
			allRoleDropdown.addElement(levelLink);
			levelLink.setUserObject(level);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LevelCols.role));
		DefaultFlexiColumnModel level1Col = new DefaultFlexiColumnModel(LevelCols.level1, new LevelSingleSelectionCellRenderer(0));
		level1Col.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
		columnsModel.addFlexiColumnModel(level1Col);
		DefaultFlexiColumnModel level2Col = new DefaultFlexiColumnModel(LevelCols.level2, new LevelSingleSelectionCellRenderer(1));
		level2Col.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
		columnsModel.addFlexiColumnModel(level2Col);
		DefaultFlexiColumnModel level3Col = new DefaultFlexiColumnModel(LevelCols.level3, new LevelSingleSelectionCellRenderer(2));
		level3Col.setAlignment(FlexiColumnModel.ALIGNMENT_CENTER);
		columnsModel.addFlexiColumnModel(level3Col);
		
		levelModel = new LevelsDataModel(columnsModel);
		levelsEl = uifactory.addTableElement(getWindowControl(), "levels", levelModel, 24, false, getTranslator(), formLayout);
		levelsEl.setExportEnabled(false);
		levelsEl.setNumOfRowsEnabled(false);
		levelsEl.setCustomizeColumns(false);
	}
	
	private void loadLevels() {
		OrganisationRoles[] roles = BaseSecurityModule.getUserAllowedRoles();
		List<LevelRow> rows = new ArrayList<>(roles.length);
		for(int i=roles.length; i-->0; ) {
			OrganisationRoles role = roles[i];
			SelectionValues levelsPK = new SelectionValues();
			
			String prefix = role + ".";
			levelsPK.add(SelectionValues.entry(prefix + PasskeyLevels.level1.name(), translate("table.header.level1")));
			levelsPK.add(SelectionValues.entry(prefix + PasskeyLevels.level2.name(), translate("table.header.level2")));
			levelsPK.add(SelectionValues.entry(prefix + PasskeyLevels.level3.name(), translate("table.header.level3")));
			
			SingleSelection levelEl = uifactory.addRadiosVertical("revel_" + (++count), null, flc, levelsPK.keys(), levelsPK.values());
			levelEl.setUserObject(role);
			levelEl.addActionListener(FormEvent.ONCHANGE);
			rows.add(new LevelRow(role, translate("admin.props." + roles[i].name() + "s"), levelEl));
			PasskeyLevels level = loginModule.getPasskeyLevel(role);
			if(level != null) {
				levelEl.select(prefix + level.name(), true);
			} else {
				levelEl.select(prefix + PasskeyLevels.level2.name(), true);
			}
		}
		
		levelModel.setObjects(rows);
		levelsEl.reset(true, true, true);
	}

	private void initExpertForm(FormLayoutContainer formLayout) {
		formLayout.setFormTitle(translate("admin.configuration.expert"));
		
		SelectionValues laterCountPK = new SelectionValues();
		laterCountPK.add(SelectionValues.entry("0", translate("later.count.never")));
		laterCountPK.add(SelectionValues.entry("5", "5"));
		laterCountPK.add(SelectionValues.entry("10", "10"));
		laterCountPK.add(SelectionValues.entry("-1", translate("later.count.forever")));
		String selectedValue = Long.toString(loginModule.getPasskeyMaxSkip());
		if(!laterCountPK.containsKey(selectedValue)) {
			laterCountPK.add(SelectionValues.entry(selectedValue, selectedValue));
		}
		skipPasskeyEl = uifactory.addDropdownSingleselect("later.count", formLayout, laterCountPK.keys(), laterCountPK.values());
		skipPasskeyEl.addActionListener(FormEvent.ONCHANGE);
		if(laterCountPK.containsKey(selectedValue)) {
			skipPasskeyEl.select(selectedValue, true);
		}
		
		SelectionValues userVerificationPK = new SelectionValues();
		userVerificationPK.add(SelectionValues.entry(UserVerificationRequirement.DISCOURAGED.getValue(), translate("user.verification.discouraged")));
		userVerificationPK.add(SelectionValues.entry(UserVerificationRequirement.PREFERRED.getValue(), translate("user.verification.preferred")));
		userVerificationPK.add(SelectionValues.entry(UserVerificationRequirement.REQUIRED.getValue(), translate("user.verification.required")));
		userVerificationEl = uifactory.addDropdownSingleselect("passkey.user.verification", formLayout,
				userVerificationPK.keys(), userVerificationPK.values());
		userVerificationEl.select(loginModule.getPasskeyUserVerification().getValue(), true);
		userVerificationEl.addActionListener(FormEvent.ONCHANGE);
		userVerificationEl.setVisible(enabledEl.isOn());
		
		SelectionValues attestationPK = new SelectionValues();
		attestationPK.add(SelectionValues.entry(AttestationConveyancePreference.NONE.getValue(), translate("attestation.none")));
		attestationPK.add(SelectionValues.entry(AttestationConveyancePreference.DIRECT.getValue(), translate("attestation.direct")));
		attestationPK.add(SelectionValues.entry(AttestationConveyancePreference.INDIRECT.getValue(), translate("attestation.indirect")));
		attestationPK.add(SelectionValues.entry(AttestationConveyancePreference.ENTERPRISE.getValue(), translate("attestation.interprise")));
		attestationEl = uifactory.addDropdownSingleselect("passkey.attestation", formLayout,
				attestationPK.keys(), attestationPK.values());
		attestationEl.select(loginModule.getPasskeyAttestationConveyancePreference().getValue(), true);
		attestationEl.addActionListener(FormEvent.ONCHANGE);
		attestationEl.setVisible(enabledEl.isOn());
	}
	
	private void updateUI() {
		boolean enabled = enabledEl.isOn();
		levelsCont.setVisible(enabled);
		expertCont.setVisible(enabled);
		levelsEl.setVisible(enabled);
		upgradeEl.setVisible(enabled);
		userVerificationEl.setVisible(enabled);
		attestationEl.setVisible(enabled);
	}
	
	private void setDefaults() {
		boolean enabled = enabledEl.isOn();
		if(enabled) {
			upgradeEl.select(UPGRADE_KEY, enabled);
		}
	}
	
	
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmDisableCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSave();
				updateUI();
			} else {
				enabledEl.toggleOn();
				updateUI();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			doSave();
			updateUI();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDisableCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDisableCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enabledEl == source) {
			doToggleEnable(ureq);	
		} else if(userVerificationEl == source || attestationEl == source || upgradeEl == source || skipPasskeyEl == source) {
			doSave();
			updateUI();
		} else if(source instanceof SingleSelection levelEl && levelEl.getUserObject() instanceof OrganisationRoles role) {
			doSave(levelEl, role);
		} else if(source instanceof FormLink levelLink && levelLink.getUserObject() instanceof PasskeyLevels level) {
			doApplyLevel(level);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
		updateUI();
	}
	
	private void doToggleEnable(UserRequest ureq) {
		if(enabledEl.isOn()) {
			doSave();
			updateUI();
			setDefaults();
		} else {
			long userWithOnlyPasskey = webAuthnManager.countIdentityWithOnlyPasskey();
			if(userWithOnlyPasskey <= 0) {
				doSave();
				updateUI();
			} else {
				doConfirmDisable(ureq, userWithOnlyPasskey);
			}
		}
	}
	
	private void doConfirmDisable(UserRequest ureq, long userWithOnlyPasskey) {
		confirmDisableCtrl = new ConfirmDisablePasskeyController(ureq, getWindowControl(), userWithOnlyPasskey);
		listenTo(confirmDisableCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDisableCtrl.getInitialComponent(),
				true, translate("disable.passkey"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doApplyLevel(PasskeyLevels level) {
		List<LevelRow> rows = levelModel.getObjects();
		for(LevelRow row:rows) {
			OrganisationRoles role = row.role();
			row.levelEl().select(role.name() + "." + level.name(), true);
			loginModule.setPasskeyLevel(role, level);
		}
	}
	
	private void doSave() {
		boolean enabled = enabledEl.isOn();
		loginModule.setOlatProviderWithPasskey(enabled);
		
		if(enabled && userVerificationEl.isOneSelected()) {
			String selectedValue = UserVerificationRequirement.create(userVerificationEl.getSelectedKey()).getValue();
			loginModule.setPasskeyUserVerification(selectedValue);
		}
		
		if(enabled && attestationEl.isOneSelected()) {
			String selectedValue = AttestationConveyancePreference.create(attestationEl.getSelectedKey()).getValue();
			loginModule.setPasskeyAttestationConveyancePreference(selectedValue);
		}
		if(enabled) {
			loginModule.setPasskeyUpgradeAllowed(upgradeEl.isAtLeastSelected(1));
			if(skipPasskeyEl.isOneSelected()) {
				loginModule.setPasskeyMaxSkip(Long.parseLong(skipPasskeyEl.getSelectedKey()));
			}
		}
	}

	private void doSave(SingleSelection levelEl, OrganisationRoles role) {
		if(levelEl.isOneSelected()) {
			String selectedKey = levelEl.getSelectedKey();
			String levelStr = selectedKey.substring(role.name().length() + 1);
			PasskeyLevels level = PasskeyLevels.valueOf(levelStr);
			loginModule.setPasskeyLevel(role, level);
		}
	}
	
	public record LevelRow(OrganisationRoles role, String roleLabel, SingleSelection levelEl) {
		//
	}
	
	private static class LevelsDataModel extends DefaultFlexiTableDataModel<LevelRow> {
		
		private static final LevelCols[] COLS = LevelCols.values();
		
		public LevelsDataModel(FlexiTableColumnModel columnsModel) {
			super(columnsModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			LevelRow levelRow = getObject(row);
			switch(COLS[col]) {
				case role: return levelRow.roleLabel();
				case level1, level2, level3: return levelRow;
				default: return "ERROR";
			}
		}
	}
	
	public enum LevelCols implements FlexiSortableColumnDef {
		role("table.header.role"),
		level1("table.header.level1"),
		level2("table.header.level2"),
		level3("table.header.level3");
		
		private final String i18nKey;
		
		private LevelCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
}
