/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */

package org.olat.user.propertyhandlers.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.InputType;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.SingleKey;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This class displays a Table with all PropertyHandlers in the System. The
 * admin-user can then globally activate/deactivate a PropertyHandler
 * 
 * <P>
 * Initial Date: 24.08.2011 <br>
 * 
 * @author strentini
 */
public class UsrPropCfgTableController extends FormBasicController {

	private FormLayoutContainer propTableFlc;
	private CloseableModalController handlerPopupCtr;
	private UsrPropHandlerCfgController handlerConfigCtrl;
	private CloseableCalloutWindowController translatorCallout;
	private SingleKeyTranslatorController singleKeyTrnsCtrl;

	//note: if you change these, change also in propTable.html
	private static final String FT_NAME_PREFIX_GRPN = "grpn_";
	private static final String FT_NAME_PREFIX_TGL = "tgl_";
	private static final String FT_NAME_PREFIX_TRANSNAME = "transname_";
	private static final String FT_NAME_PREFIX_TRANSL = "translate_";
	private static final String FT_NAME_PREFIX_HDNL = "hndl_";

	private Map<String, FormToggle> rowToggleButtonsMap;

	private DialogBoxController deactPropertyYesNoCtrl;

	private static final String[] GROUP_KEYS = { "account", "person", "contact", "address", "institute" };

	@Autowired
	private UsrPropCfgManager usrPropCfgMng;
	@Autowired
	private UsrPropHandlerCfgFactory handlerConfigCtrlFactory;

	public UsrPropCfgTableController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		rowToggleButtonsMap = new HashMap<>();

		// custom formlayout out of the vc
		propTableFlc = FormLayoutContainer.createCustomFormLayout("propTable", getTranslator(), velocity_root + "/propTable.html");
		formLayout.add(propTableFlc);
		buildPropertyHandlerTable();
	}

	/**
	 * 
	 */
	public void refresh() {
		buildPropertyHandlerTable();
	}

	/**
	 * builds the userPropertyHandler-Table
	 */
	private void buildPropertyHandlerTable() {

		List<UserPropertyHandler> myHandlerList = usrPropCfgMng.getUserPropertiesConfigObject().getPropertyHandlers();

		// first search for all group-values in the propertyHandlers
		List<String> allGroupValues = new ArrayList<>();
		for (UserPropertyHandler handler : myHandlerList) {
			String group = handler.getGroup();
			if (!allGroupValues.contains(group)) allGroupValues.add(group);
		}
		// take the defaults
		for (int k = 0; k < GROUP_KEYS.length; k++) {
			String group = GROUP_KEYS[k];
			if (!allGroupValues.contains(group)) allGroupValues.add(group);
		}
		final String[] selectableGroups = allGroupValues.toArray(new String[0]);

		// the table-rows
		String handlerName;
		SingleSelection groupDropdown;
		FormToggle fi;
		FormLink translateLink;
		FormLink handlerLink;
		String translatedName;
		for (UserPropertyHandler handler : myHandlerList) {
			handlerName = handler.getName();
			// the group field
			groupDropdown = uifactory.addDropdownSingleselect(FT_NAME_PREFIX_GRPN + handlerName, null, propTableFlc, selectableGroups,
					selectableGroups, null);

			if (Arrays.asList(selectableGroups).contains(handler.getGroup())) {
				groupDropdown.select(handler.getGroup(), true);
			} else {
				logWarn("could not set group-select to " + handler.getGroup() + " (" + handler.getName() + ")", null);
			}

			groupDropdown.setUserObject(handler);
			groupDropdown.showLabel(false);
			groupDropdown.addActionListener(FormEvent.ONCHANGE);

			// the "active"-toggle button
			fi = uifactory.addToggleButton(FT_NAME_PREFIX_TGL + handlerName, "&nbsp;", propTableFlc, null, null);
			if (usrPropCfgMng.getUserPropertiesConfigObject().isActiveHandler(handler)) fi.toggleOn();
			else fi.toggleOff();

			if (!UsrPropCfgManager.canBeDeactivated(handler)) fi.setEnabled(false);
			fi.setUserObject(handler);
			rowToggleButtonsMap.put(handlerName, fi);

			// the "translate" link
			if (isPropertyHandlerTranslated(handler)) {
				translateLink = uifactory.addFormLink(FT_NAME_PREFIX_TRANSL + handlerName, "upc.edittranslate", null, propTableFlc, Link.LINK);
			} else {
				translateLink = uifactory.addFormLink(FT_NAME_PREFIX_TRANSL + handlerName, "upc.translate", null, propTableFlc, Link.LINK);
				translateLink.setCustomEnabledLinkCSS("o_ochre");
			}
			translateLink.setUserObject(handler);

			// the "handler-config" link
			if (handlerConfigCtrlFactory.hasConfig(handler)) {
				handlerLink = uifactory.addFormLink(FT_NAME_PREFIX_HDNL + handlerName, "upc.hndlconfig", null, propTableFlc, Link.LINK);
				handlerLink.setUserObject(handler);
			}

			// put the translation (in the current language) for the property
			translatedName = I18nManager.getInstance().getLocalizedString(UserPropertyHandler.class.getPackage().getName(),
					handler.i18nFormElementLabelKey(), null, getLocale(), true, true);
			uifactory.addStaticTextElement(FT_NAME_PREFIX_TRANSNAME + handlerName, null, (translatedName == null ? "-" : translatedName),
					propTableFlc);
		}
		propTableFlc.contextPut("rows", myHandlerList);
	}

	private boolean isPropertyHandlerTranslated(UserPropertyHandler handler) {
		String translatedPropertyName = I18nManager.getInstance().getLocalizedString(UserPropertyHandler.class.getPackage().getName(),
				handler.i18nFormElementLabelKey(), null, getLocale(), true, true);
		return (translatedPropertyName != null);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		UserPropertyHandler handler = (UserPropertyHandler) source.getUserObject();

		if (source instanceof FormToggle) {
			FormToggle toggle = (FormToggle) source;
			if (toggle.isOn()) {
				usrPropCfgMng.getUserPropertiesConfigObject().setHandlerAsActive(handler, true);
				usrPropCfgMng.saveUserPropertiesConfig();
			} else {
				deactPropertyYesNoCtrl = activateYesNoDialog(ureq, translate("upc.deact_confirmationtitle"),
						translate("upc.deact_confirmationtext"), deactPropertyYesNoCtrl);
				deactPropertyYesNoCtrl.setUserObject(handler);
			}
		}  else if (source instanceof SingleSelection) {
			SingleSelection groupSel = (SingleSelection) source;
			handler.setGroup(groupSel.getSelectedKey());
			usrPropCfgMng.saveUserPropertiesConfig();
		} else if (source instanceof FormLink) {
			String itemname = source.getName();
			if (itemname.startsWith(FT_NAME_PREFIX_TRANSL)) {
				// open the singlekeyTranslator-controller callout
				SingleKey key2Translate1 = new SingleKey(handler.i18nFormElementLabelKey(), InputType.TEXT_ELEMENT);
				SingleKey key2Translate2 = new SingleKey(handler.i18nColumnDescriptorLabelKey(), InputType.TEXT_ELEMENT);
				List<SingleKey>  keys2Translate = List.of(key2Translate1, key2Translate2);
				singleKeyTrnsCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), keys2Translate,
						UserPropertyHandler.class, null);
				listenTo(singleKeyTrnsCtrl);
				removeAsListenerAndDispose(translatorCallout);
				translatorCallout = new CloseableCalloutWindowController(ureq, getWindowControl(), singleKeyTrnsCtrl.getInitialComponent(),
						(FormLink) source, "Translate:: " + key2Translate1, false, null);
				listenTo(translatorCallout);
				translatorCallout.activate();
			} else if (itemname.startsWith(FT_NAME_PREFIX_HDNL)) {
				handlerConfigCtrl = handlerConfigCtrlFactory.getConfigController(ureq, getWindowControl(), handler);
				listenTo(handlerConfigCtrl);
				if (handlerConfigCtrl.getInitialComponent() != null) {
					handlerPopupCtr = new CloseableModalController(getWindowControl(), "Save", handlerConfigCtrl.getInitialComponent(), true,
							translate("upc.handlerconfigtitle"), false);
					handlerPopupCtr.activate();
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source.equals(singleKeyTrnsCtrl)) {
			translatorCallout.deactivate();
			this.refresh();
		} else if (source.equals(deactPropertyYesNoCtrl)) {
			UserPropertyHandler handler = (UserPropertyHandler) deactPropertyYesNoCtrl.getUserObject();
			if (DialogBoxUIFactory.isYesEvent(event)) {
				usrPropCfgMng.getUserPropertiesConfigObject().setHandlerAsActive(handler, false);
				usrPropCfgMng.saveUserPropertiesConfig();
			} else {
				rowToggleButtonsMap.get(handler.getName()).toggleOn();
			}
		} else if (source.equals(handlerConfigCtrl)) {
			handlerPopupCtr.deactivate();
			removeAsListenerAndDispose(handlerConfigCtrl);
			removeAsListenerAndDispose(handlerPopupCtr);
			handlerConfigCtrl = null;
			handlerPopupCtr = null;
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		usrPropCfgMng.saveUserPropertiesConfig();
	}

	@Override
	protected void doDispose() {
		//
	}

	/**
	 * Description:<br>
	 * Used to compare (and thus sort) userPropertyHandlers
	 * 
	 * <P>
	 * Initial Date: 26.10.2011 <br>
	 * 
	 * @author strentini
	 */
	class UserPropertyHandlerComparator implements Comparator<UserPropertyHandler> {

		private String sortKey;

		public UserPropertyHandlerComparator(String sortKey) {
			this.sortKey = sortKey;
		}

		@Override
		public int compare(UserPropertyHandler o1, UserPropertyHandler o2) {
			if ("upc.name".equals(sortKey)) {
				return o1.getName().compareTo(o2.getName());
			} else if ("upc.group".equals(sortKey)) { return o1.getGroup().compareTo(o2.getGroup()); }
			return 0;
		}
	}

}
