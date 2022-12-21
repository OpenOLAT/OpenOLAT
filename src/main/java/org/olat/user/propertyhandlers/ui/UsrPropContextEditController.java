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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.i18n.I18nManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyUsageContext;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This class allows the configuration of a given UserPropertyUsageContext. It
 * displays a Table of all PropertyHandlers. The user can activeate/deactivate
 * each Handler for this context. Additional settings can be made: mark a
 * handler as adminViewonly, ReadOnly, Mandatory
 * 
 * <P>
 * Initial Date: 29.08.2011 <br>
 * 
 * @author strentini
 */
public class UsrPropContextEditController extends FormBasicController {

	private static final String FT_NAME_PREFIX_INCL = "include.";
	private static final String FT_NAME_PREFIX_MAND = "mandatory.";
	private static final String FT_NAME_PREFIX_ADMN = "adminonly.";
	private static final String FT_NAME_PREFIX_TRANS = "trans."; 
	private static final String FT_NAME_PREFIX_USR = "userreadonly.";
	private static final String FT_NAME_PREFIX_MUP = "moveU.";
	private static final String FT_NAME_PREFIX_MDN = "moveD.";

	private FormLayoutContainer contTableFlc;
	private UserPropertyUsageContext context; // the context to edit
	private String contextName;

	private int includedPropertiesCount = 0;//counts how many usrProps are marked as _active_ in the current context
	
	private Map<String, List<FormItem>> rowToggleButtonsMap;
	
	@Autowired
	private UsrPropCfgManager usrPropCfgMng;

	protected UsrPropContextEditController(UserRequest ureq, WindowControl wControl, UserPropertyUsageContext context, String contextName) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.context = context;
		this.contextName = contextName;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		// custom formlayout out of the vc
		contTableFlc = FormLayoutContainer.createCustomFormLayout("contTable", getTranslator(), velocity_root + "/contPropTable.html");
		formLayout.add(contTableFlc);

		// the table-rows
		buildPropertyHandlerListFromContext();

		contTableFlc.contextPut("ctxname", contextName);
		contTableFlc.contextPut("ctxdescr", context.getDescription());
		contTableFlc.contextPut("activePropCount", includedPropertiesCount);
	}

	/**
	 * builds a List of handlers to display in the table 
	 * (is also invoked when context changes, to update the gui-table)
	 * 
	 */
	private void buildPropertyHandlerListFromContext() {
		rowToggleButtonsMap = new HashMap<>();
		
		// add form components for each row
		List<UserPropertyHandler> handlerList = new ArrayList<>();

		// loop over the handlers within this context
		for (UserPropertyHandler handler : context.getPropertyHandlers()) {
			handlerList.add(handler);
			addTableRowComponents(handler, true);
		}

		// now add all propertyHandlers that are not in this context
		for (UserPropertyHandler handler : usrPropCfgMng.getUserPropertiesConfigObject().getPropertyHandlers()) {
			if (handlerList.contains(handler)) continue;
			final boolean isActive = usrPropCfgMng.getUserPropertiesConfigObject().isActiveHandler(handler);
			if (!isActive) continue;
			handlerList.add(handler);
			addTableRowComponents(handler, false);
		}
		contTableFlc.contextPut("rows", handlerList);
	}

	/**
	 * adds tableRow-Components (Toggles and Links) for the given Handler 
	 * 
	 * @param handler The handler to create Components for
	 * @param moveable if true, additional "up"/"down" Links will be rendered
	 */
	private void addTableRowComponents(UserPropertyHandler handler, boolean moveable) {
		List<FormItem> rowFormItemComponents = new ArrayList<>();
		final String handlername = handler.getName();
		final boolean isIncluded = context.contains(handler);
		final boolean isMandatory = context.isMandatoryUserProperty(handler);
		final boolean isAdminOnly = context.isForAdministrativeUserOnly(handler);
		final boolean isUserReadOnly = context.isUserViewReadOnly(handler);

		
		// put the translation (in the current language) for the property
		String translatedName = I18nManager.getInstance().getLocalizedString(UserPropertyHandler.class.getPackage().getName(),
				handler.i18nFormElementLabelKey(), null, getLocale(), true, true);
		uifactory.addStaticTextElement(FT_NAME_PREFIX_TRANS + handlername, (translatedName == null ? "-" : translatedName), contTableFlc);
		
		FormToggle ftMandatory = uifactory.addToggleButton(FT_NAME_PREFIX_MAND + handlername, null, "&nbsp;&nbsp;&nbsp;&nbsp;", contTableFlc,
				null, null);
		ftMandatory.setUserObject(handler);
		if (isMandatory) ftMandatory.toggleOn();
		else ftMandatory.toggleOff();
		
		if (!isIncluded  ) {
			ftMandatory.setEnabled(false);
			ftMandatory.setVisible(false);
		}

		FormToggle ftAdminonly = uifactory.addToggleButton(FT_NAME_PREFIX_ADMN + handlername, null, "&nbsp;&nbsp;&nbsp;&nbsp;", contTableFlc, null, null);
		ftAdminonly.setUserObject(handler);
		if (isAdminOnly) ftAdminonly.toggleOn();
		else ftAdminonly.toggleOff();
		if (!isIncluded) {
			ftAdminonly.setEnabled(false);
			ftAdminonly.setVisible(false);
		}

		FormToggle ftUserreadonly = uifactory.addToggleButton(FT_NAME_PREFIX_USR + handlername, null, "&nbsp;&nbsp;&nbsp;&nbsp;", contTableFlc, null, null);
		ftUserreadonly.setUserObject(handler);
		if (isUserReadOnly) ftUserreadonly.toggleOn();
		else ftUserreadonly.toggleOff();
		if (!isIncluded) {
			ftUserreadonly.setEnabled(false);
			ftUserreadonly.setVisible(false);
		}

		FormToggle ftInclude = uifactory.addToggleButton(FT_NAME_PREFIX_INCL + handlername, null, "&nbsp;&nbsp;&nbsp;&nbsp;", contTableFlc, null, null);
		ftInclude.setUserObject(handler);
		if (isIncluded) {
			ftInclude.toggleOn();
			includedPropertiesCount++;	
		}
		else ftInclude.toggleOff();

		// up/down links

		FormLink upLink = uifactory.addFormLink(FT_NAME_PREFIX_MUP + handlername, " ", null, contTableFlc, Link.NONTRANSLATED);
		upLink.setIconLeftCSS("o_icon o_icon_move_up o_icon-lg");
		upLink.setUserObject(handler);
		
		FormLink downLink = uifactory.addFormLink(FT_NAME_PREFIX_MDN + handlername, " ", null, contTableFlc, Link.NONTRANSLATED);
		downLink.setIconRightCSS("o_icon o_icon_move_down o_icon-lg");
		downLink.setUserObject(handler);
		if (!moveable) {
			upLink.setEnabled(false);
			upLink.setVisible(false);
			downLink.setEnabled(false);
			downLink.setVisible(false);
		}

		rowFormItemComponents.add(ftMandatory);
		rowFormItemComponents.add(ftAdminonly);
		rowFormItemComponents.add(ftUserreadonly);
		rowFormItemComponents.add(upLink);
		rowFormItemComponents.add(downLink);

		rowToggleButtonsMap.put(handlername, rowFormItemComponents);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormToggle) {
			FormToggle sourceToggle = (FormToggle) source;
			UserPropertyHandler handler = (UserPropertyHandler) sourceToggle.getUserObject();

			if (sourceToggle.getName().startsWith(FT_NAME_PREFIX_MAND)) {
				context.setAsMandatoryUserProperty(handler, sourceToggle.isOn());
			} else if (sourceToggle.getName().startsWith(FT_NAME_PREFIX_ADMN)) {
				context.setAsAdminstrativeUserOnly(handler, sourceToggle.isOn());
			} else if (sourceToggle.getName().startsWith(FT_NAME_PREFIX_USR)) {
				context.setAsUserViewReadOnly(handler, sourceToggle.isOn());
			} else if (sourceToggle.getName().startsWith(FT_NAME_PREFIX_INCL)) {
				if (sourceToggle.isOn()) {
					// activate the toggles on this row
					List<FormItem> rowFormItems = rowToggleButtonsMap.get(handler.getName());
					for (FormItem item : rowFormItems) {
						item.setEnabled(true);
						item.setVisible(true);
					}
					context.addPropertyHandler(handler);
					includedPropertiesCount++;
				} else {
					// this is removing property from context
					// deactivate the toggles on this row
					List<FormItem> rowFormItems = rowToggleButtonsMap.get(handler.getName());
					for (FormItem item : rowFormItems) {
						item.setEnabled(false);
						item.setVisible(false);
					}
					context.removePropertyHandler(handler);
					includedPropertiesCount--;
					if(!UsrPropCfgManager.canBeOptionalInContext(handler,contextName)){
						showInfo("upc.deact_infotext"	);
					}
				}
			}
			// something was toggled, save changes!
			usrPropCfgMng.saveUserPropertiesConfig();
			
			contTableFlc.contextPut("activePropCount", includedPropertiesCount);
		} else if (source instanceof FormLink) {
			
			// a link, (up/down)
			UserPropertyHandler handler = (UserPropertyHandler) source.getUserObject();
			if (source.getName().startsWith(FT_NAME_PREFIX_MUP)) {
				context.moveHandlerUp(handler);
				usrPropCfgMng.saveUserPropertiesConfig();
				buildPropertyHandlerListFromContext();
			} else if (source.getName().startsWith(FT_NAME_PREFIX_MDN)) {
				context.moveHandlerDown(handler);
				usrPropCfgMng.saveUserPropertiesConfig();
				buildPropertyHandlerListFromContext();
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing to handle
	}

}
