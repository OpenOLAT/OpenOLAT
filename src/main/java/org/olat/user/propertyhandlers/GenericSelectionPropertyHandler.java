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
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.ui.UsrPropHandlerCfgFactory;

/**
 * 
 * Description:<br>
 * This class handles genericSelection User Properties. A Generic Selection User
 * Property can either be a Single-Selection or Multi-Selection Field.
 * Configuration of this PropertyHandler is done through the admin-gui (via
 * <code>UsrPropHandlerCfgFactory</code> and
 * <code>GenericSelectionPropertyHandlerController</code>)
 * 
 * 
 * See also FXOLAT-114
 * 
 * <P>
 * Initial Date: 29.08.2011 <br>
 * 
 * @author strentini
 */
public class GenericSelectionPropertyHandler extends AbstractUserPropertyHandler {

	private static final String PROP_MULTISELECT = "ismulti";
	private static final String PROP_SELKEYS = "selkeys";

	private static final String PROP_MULTISELECT_TRUE = "1";
	
	private boolean isMultiselect;
	private String[] selectionKeys;

	private static final String NO_SEL_KEY = "gsph.doselect";
	public static final String KEY_DELIMITER = ",";

	private UsrPropHandlerCfgFactory cfgFactory;

	/**
	 * is this property a Multi-Select or Single-Select
	 * 
	 * @return
	 */
	public boolean isMultiSelect() {
		return isMultiselect;
	}

	/**
	 * returns a String-Array holding all selection-keys
	 * (the options in a drop-down)
	 * 
	 * @return
	 */
	public String[] getSelectionKeys() {
		return selectionKeys;
	}

	/**
	 * if flag is true, sets this UserProperty as multi-select
	 * 
	 * @param multi
	 */
	public void setMultiSelect(boolean multi) {
		isMultiselect = multi;
	}

	/**
	 * sets the array of selection-keys
	 * (the options in a drop-down)
	 * @param keys
	 */
	public void setSelectionKeys(String[] keys) {
		selectionKeys = keys;
	}

	/**
	 * saves the configuration of this property
	 */
	public void saveConfig() {
		Map<String, String> configMap = new HashMap<>();
		String isMulti = (isMultiselect) ? PROP_MULTISELECT_TRUE : "0";
		configMap.put(PROP_MULTISELECT, isMulti);

		StringBuilder sb = new StringBuilder();
		for (String key : selectionKeys) {
			if (StringHelper.containsNonWhitespace(key)) 
				sb.append(key + ",");
		}
		configMap.put(PROP_SELKEYS, sb.toString());

		cfgFactory.saveConfigForHandler(this, configMap);
	}

	/**
	 * [spring] setter
	 * 
	 * @param factory
	 */
	public void setHandlerConfigFactory(UsrPropHandlerCfgFactory factory) {
		cfgFactory = factory;
		Map<String, String> handlerConfig = cfgFactory.loadConfigForHandler(this);

		//handlerConfig can be empty, if we don't have a config yet (e.g. a new property added in xml)
		//->check for this
		if(handlerConfig.containsKey(PROP_MULTISELECT)){
			isMultiselect = (handlerConfig.get(PROP_MULTISELECT).equals(PROP_MULTISELECT_TRUE));
		}else{
			isMultiselect =false;
		}
		if (handlerConfig.containsKey(PROP_SELKEYS)) {
			selectionKeys = handlerConfig.get(PROP_SELKEYS).split(",");
		} else {
			selectionKeys = new String[0];
		}
	}

	/**
	 * 
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#addFormItem(java.util.Locale,
	 *      org.olat.core.id.User, java.lang.String, boolean,
	 *      org.olat.core.gui.components.form.flexible.FormItemContainer)
	 */
	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {

		FormItem newItem = null;

		if (isMultiselect) {
			MultipleSelectionElement mse = FormUIFactory.getInstance().addCheckboxesHorizontal(getName(), i18nFormElementLabelKey(),
					formItemContainer, selectionKeys, getTranslatedValues(selectionKeys, locale));
			for (String sKey : getSelectedKeys(user)) {
				mse.select(sKey, true);
			}
			newItem = mse;
		} else {
			String[] allKeys = new String[selectionKeys.length + 1];
			System.arraycopy(selectionKeys, 0, allKeys, 1, selectionKeys.length);
			allKeys[0] = NO_SEL_KEY;

			SingleSelection sse = FormUIFactory.getInstance().addDropdownSingleselect(getName(), i18nFormElementLabelKey(), formItemContainer,
					allKeys, getTranslatedValues(allKeys, locale), null);
			// make pre-selection of the formItem
			String internalValue = getInternalValue(user);
			
			if (isValidValue(user, internalValue, null, null) && sse.containsKey(internalValue)) {
				sse.select(internalValue, true);
			}
			newItem = sse;
		}

		// enable/disable according to settings
		UserManager um = UserManager.getInstance();
		if (um.isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
			newItem.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			newItem.setMandatory(true);
		}

		return newItem;
	}

	/**
	 * 
	 * @param user
	 * @return
	 */
	private String[] getSelectedKeys(User user) {
		return getInternalValue(user).split(KEY_DELIMITER);
	}

	@Override
	public String getInternalValue(User user) {
		String value = super.getInternalValue(user);
		return (value == null ? NO_SEL_KEY : value);
	}

	@Override
	public String getUserProperty(User user, Locale locale) {
		return getUserPropertyAsHTML(user, locale);
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		if (formItem.isMandatory()) {
			if (isMultiselect) {
				MultipleSelectionElement msel = (MultipleSelectionElement) formItem;
				msel.setErrorKey("form.legende.mandatory", null);
				return msel.isAtLeastSelected(1);
			} else {
				SingleSelection ssel = (SingleSelection) formItem;
				if (ssel.getSelectedKey().equals(NO_SEL_KEY)) {
					ssel.setErrorKey("form.legende.mandatory", null);
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (StringHelper.containsNonWhitespace(value)) {
			if(isMultiselect) {
				List<String> valuesList = splitMultipleValues(value);
				for(String val:valuesList) {
					if(StringHelper.containsNonWhitespace(val) && !isValueValid(val)) {
						if(validationError != null) {
							validationError.setErrorKey("form.name.genericSelectionProperty.error");
						}
						return false;
					}
				}
				return true;
			} else if(isValueValid(value)) {
				return true;
			}
			if(validationError != null) {
				validationError.setErrorKey("form.name.genericSelectionProperty.error");
			}
			return false;
		}
		// null values are ok
		return true;
	}
	
	public static List<String> splitMultipleValues(String value) {
		String[] valueArr = value.split("[,]");
		List<String> values = new ArrayList<>(valueArr.length);
		for(String val:valueArr) {
			if(StringHelper.containsNonWhitespace(val)) {
				values.add(val);
			}
		}
		return values;
	}
	
	private boolean isValueValid(String value) {
		if(StringHelper.containsNonWhitespace(value)) {
			for (int i = 0; i<selectionKeys.length; i++) {
				String key = selectionKeys[i];
				if (key.equals(value)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public String getStringValue(FormItem formItem) {
		if (isMultiselect) {
			Collection<String> selectedKeys = ((MultipleSelectionElement) formItem).getSelectedKeys();
			StringBuilder sb = new StringBuilder();
			for (String sKey : selectedKeys) {
				sb.append(sKey).append(KEY_DELIMITER);
			}
			return sb.toString();
		}
		if (formItem instanceof SingleSelection) {
			SingleSelection sel = (SingleSelection)formItem;
			if(sel.isOneSelected() && !NO_SEL_KEY.equals(sel.getSelectedKey())) {
				return sel.getSelectedKey();
			}
		}
		return null;
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		// see <code>GenderPropertyHandler</code>
		return displayValue;
	}

	/**
	 * Helper method to create translated values that correspond with the
	 * selection keys
	 * 
	 * @param locale
	 * @return an Array holding the translated Strings
	 */
	private String[] getTranslatedValues(String[] keys, Locale locale) {
		Translator trans = Util.createPackageTranslator(this.getClass(), locale);
		String[] values = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			values[i] = trans.translate(keys[i]);
		}
		return values;
	}

	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String val = super.getInternalValue(user);// don't want "no selection" key
		StringBuilder htmlValue = new StringBuilder(64);
		Translator trans = Util.createPackageTranslator(this.getClass(), locale);
		if(val != null) {
			if (isMultiSelect()) {
				for (String value : val.split(KEY_DELIMITER)) {
					if(StringHelper.containsNonWhitespace(value)) {
						if(htmlValue.length() > 0) {
							htmlValue.append(" ");
						}
						if(locale != null) {
							htmlValue.append(trans.translate(value));
						} else  {
							htmlValue.append(value);
						}
					}
				}
			} else if(locale != null) {
				htmlValue.append(trans.translate(val));
			} else  {
				htmlValue.append(val);
			}
		}
		return htmlValue.toString();
	}
}