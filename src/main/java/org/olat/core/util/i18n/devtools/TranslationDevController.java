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
package org.olat.core.util.i18n.devtools;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;

public class TranslationDevController extends FormBasicController {

	private String[] values = { "" };
	private String[] keys = { "on" };

	private TextElement moveOrigin;
	private TextElement moveTarget;
	private FormLink submitMove;

	private MultipleSelectionElement sortDryRun;
	private MultipleSelectionElement removeEmptyDryRun;
	private MultipleSelectionElement removeDeletedNotDry;

	private FormLink submitSort;

	private FormLink submitRemoveEmpty;

	private TextElement mergeOrigin;
	private TextElement mergeTarget;
	private FormLink submitMerge;

	private TextElement removeRemoveDeletedOrigin;
	private TextElement removeRemoveDeletedTarget;
	private FormLink submitRemoveDeleted;

	private TextElement deleteKeyLocale;
	private TextElement deleteKeyBundle;
	private TextElement deleteKeyKey;

	private TextElement renameKeyBundle;
	private TextElement renameKeyOrig;
	private TextElement renameKeyTarget;

	private FormLink submitRename;
	private FormLink submitDelete;

    private TextElement moveKeyOrigBundle;
	private TextElement moveKeyKey;
	private TextElement moveKeyTargetBundle;

	private FormLink submitMoveKeyToBundle;
	private TextElement removePackageBundle;
	private FormLink submitRemovePackage;

	private TextElement renameLanguageSource;
	private TextElement renameLanguageTarget;

	private FormLink submitRenameLanguage;
	private FormLink submitGetDupKeys;
	private FormLink submitGetDupVals;
	private TextElement addKeyBundle;
	private TextElement addKeyLocale;
    private TextElement addKeyValue;
	private TextElement addKeyKey;
	private FormLink submitAdd;
	
	@Autowired
	private I18nModule i18nModule;
	@Autowired
	private TranslationDevManager translationDevManager;

	public TranslationDevController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setFormTitle("devtools.title");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		if(!i18nModule.isTransToolEnabled()){
			setFormWarning("devtools.warning");
		}

		// create Key

		uifactory.addStaticTextElement("task.add.key", "", formLayout);
		addKeyLocale = uifactory.addTextElement("addKeyLocale", "task.add.key.locale", 500, "", formLayout);
		addKeyBundle = uifactory.addTextElement("addKeyBundle", "task.add.key.bundle", 500, "", formLayout);
		addKeyKey = uifactory.addTextElement("addKeysKey", "task.add.key.key", 500, "", formLayout);
		addKeyValue = uifactory.addTextElement("addKeyValue", "task.add.key.value", 500, "" , formLayout);

		submitAdd = uifactory.addFormLink("submitAddKey", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("addKeySpacer", formLayout, false);

		// rename keys
		uifactory.addStaticTextElement("task.rename.key", "", formLayout);
		renameKeyBundle = uifactory.addTextElement("renameKeysBundle", "task.rename.key.bundle", 500, "", formLayout);
		renameKeyOrig = uifactory.addTextElement("renameKeyOrig", "task.rename.key.orig", 500, "", formLayout);
		renameKeyTarget = uifactory.addTextElement("renameKeyTarget", "task.rename.key.target", 500, "", formLayout);

		submitRename = uifactory.addFormLink("submitRenameKey", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("renameKeySpacer", formLayout, false);

		// move Key to bundle
		uifactory.addStaticTextElement("task.move.key.to.bundle", "", formLayout);
		moveKeyKey = uifactory.addTextElement("moveKeysToBundleKey", "task.move.key.key", 500, "", formLayout);
		moveKeyOrigBundle = uifactory.addTextElement("moveKeysToBundleOrig", "task.rename.key.origBundle", 500, "", formLayout);
		moveKeyTargetBundle = uifactory.addTextElement("moveKeysToBundleTarget", "task.rename.key.targetBundle", 500, "", formLayout);

		submitMoveKeyToBundle = uifactory.addFormLink("submitMoveKeyToBundle", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("moveKeySpacer", formLayout, false);

		// deleteKeys
		uifactory.addStaticTextElement("task.remove.key", "", formLayout);
		deleteKeyLocale = uifactory.addTextElement("deleteKeyLocale", "task.remove.key.locale", 500, "", formLayout);
		deleteKeyBundle = uifactory.addTextElement("deleteKeyBundle", "task.remove.key.bundle", 500, "", formLayout);
		deleteKeyKey = uifactory.addTextElement("deleteKeysKey", "task.remove.key.key", 500, "", formLayout);

		submitDelete = uifactory.addFormLink("submitRemoveKey", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("removeKeySpacer", formLayout, false);

		// sortKeys
		uifactory.addStaticTextElement("task.sort.keys", "", formLayout);
		sortDryRun = uifactory.addCheckboxesHorizontal("task.sort.check", formLayout, keys, values);
		sortDryRun.selectAll();

		submitSort = uifactory.addFormLink("submitSort", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("sortSpacer", formLayout, false);

		// movePackage
		uifactory.addStaticTextElement("task.move.package", "", formLayout);
		moveOrigin = uifactory.addTextElement("moveOrig", "task.move.package.source", 500, "", formLayout);
		moveTarget = uifactory.addTextElement("moveTarget", "task.move.package.target", 500, "", formLayout);

		submitMove = uifactory.addFormLink("submitMove", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("moveSpacer", formLayout, false);

		// mergePackage
		uifactory.addStaticTextElement("task.merge.package", "", formLayout);
		mergeOrigin = uifactory.addTextElement("mergeOrig", "task.move.package.source", 500, "", formLayout);
		mergeTarget = uifactory.addTextElement("mergeTarget", "task.move.package.target", 500, "", formLayout);

		submitMerge = uifactory.addFormLink("submitMerge", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("mergeSpacer", formLayout, false);

		// deletePackage
		uifactory.addStaticTextElement("task.remove.package", "", formLayout);
		removePackageBundle = uifactory.addTextElement("removeBundle", "task.remove.package.bundle", 500, "", formLayout);

		submitRemovePackage = uifactory.addFormLink("submitRemovePackage", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("removeSpacer", formLayout, false);

		// rename language
		uifactory.addStaticTextElement("task.rename.language", "", formLayout);
		renameLanguageSource = uifactory.addTextElement("renameLanguageSource", "task.rename.language.source", 500, "", formLayout);
		renameLanguageTarget = uifactory.addTextElement("renameLanguageTarget", "task.rename.language.target", 500, "", formLayout);

		submitRenameLanguage = uifactory.addFormLink("submitRenameLanguage", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("renameLanguageSpacer", formLayout, false);

		// removeEmptyKeys
		uifactory.addStaticTextElement("task.remove.emptyKeys", "", formLayout);
		removeEmptyDryRun = uifactory.addCheckboxesHorizontal("task.remove.emptyKeysCheck", formLayout, keys, values);
		removeEmptyDryRun.selectAll();

		submitRemoveEmpty = uifactory.addFormLink("submitRemoveEmpty", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("emptyKeysSpacer", formLayout, false);

		// RemoveDeletedKeys
		uifactory.addStaticTextElement("task.remove.deletedKeys", "", formLayout);
		removeDeletedNotDry = uifactory.addCheckboxesHorizontal("task.remove.deletedKeysCheck", formLayout, keys, values);
		removeDeletedNotDry.selectAll();
		removeRemoveDeletedOrigin = uifactory.addTextElement("removeDeleteKeyOrig", "task.move.package.source", 500, "", formLayout);
		removeRemoveDeletedTarget = uifactory.addTextElement("removeDeleteKeyTarget", "task.move.package.target", 500, "", formLayout);

		submitRemoveDeleted = uifactory.addFormLink("submitRemoveDeleted", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("deletedKeysSpacer", formLayout, false);

		// get duplicate keys
		submitGetDupKeys = uifactory.addFormLink("submitGetDupKeys", formLayout, Link.BUTTON);
		uifactory.addSpacerElement("getDoupKeysSpacer", formLayout, false);

		// get duplicate values
		submitGetDupVals = uifactory.addFormLink("submitGetDupVals", formLayout, Link.BUTTON);
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == submitAdd){
			translationDevManager.addKey(new Locale(addKeyLocale.getValue()), addKeyBundle.getValue(), addKeyKey.getValue(), addKeyValue.getValue());
		}
		if (source == submitRename) {
			translationDevManager.renameKeyTask(renameKeyBundle.getValue(), renameKeyOrig.getValue(), renameKeyTarget.getValue());
		}
		if (source == submitDelete) {
			translationDevManager.deleteKey(new Locale(deleteKeyLocale.getValue()), deleteKeyBundle.getValue(), deleteKeyKey.getValue());
		}
		if (source == submitSort) {
			translationDevManager.sortKeysTask(!sortDryRun.isSelected(0));
		}
		if (source == submitMoveKeyToBundle) {
			translationDevManager.moveKeyToOtherBundle(moveKeyOrigBundle.getValue(), moveKeyTargetBundle.getValue(), moveKeyKey.getValue());
		}
		if (source == submitMove) {
			translationDevManager.movePackageTask(moveOrigin.getValue(), moveTarget.getValue());
		}
		if (source == submitMerge) {
			translationDevManager.mergePackageTask(mergeOrigin.getValue(), mergeTarget.getValue());
		}
		if (source == submitRemoveEmpty) {
			translationDevManager.removeEmptyKeysTask(!removeEmptyDryRun.isSelected(0));
		}
		if (source == submitRemovePackage) {
			translationDevManager.deletePackage(removePackageBundle.getValue());
		}
		if (source == submitRenameLanguage) {
			translationDevManager.renameLanguageTask(new Locale(renameLanguageSource.getValue()), new Locale(renameLanguageTarget.getValue()));
		}
		if (source == submitGetDupKeys) {
			translationDevManager.getDouplicateKeys();
		}
		if (source == submitGetDupVals) {
			translationDevManager.getDouplicateValues();
		}
	}

}
