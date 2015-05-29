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

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.util.Util;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 02.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CountryCodePropertyHandler extends AbstractUserPropertyHandler {
	
	private static final String KEY_PREFIX = "country.code.";
	private static final String NO_SEL_KEY = "nocountry.doselect";
	
	private static final String[] countryKeys = {
		"country.code.AL",
		"country.code.DZ",
		"country.code.AS",
		"country.code.AD",
		"country.code.AO",
		"country.code.AI",
		"country.code.AQ",
		"country.code.AG",
		"country.code.AR",
		"country.code.AM",
		"country.code.AW",
		"country.code.AU",
		"country.code.AT",
		"country.code.AZ",
		"country.code.BS",
		"country.code.BH",
		"country.code.BD",
		"country.code.BB",
		"country.code.BY",
		"country.code.BE",
		"country.code.BZ",
		"country.code.BJ",
		"country.code.BM",
		"country.code.BT",
		"country.code.BO",
		"country.code.BQ",
		"country.code.BA",
		"country.code.BW",
		"country.code.BV",
		"country.code.BR",
		"country.code.IO",
		"country.code.BN",
		"country.code.BG",
		"country.code.BF",
		"country.code.BI",
		"country.code.KH",
		"country.code.CM",
		"country.code.CA",
		"country.code.CV",
		"country.code.KY",
		"country.code.CF",
		"country.code.TD",
		"country.code.CL",
		"country.code.CN",
		"country.code.CX",
		"country.code.CC",
		"country.code.CO",
		"country.code.KM",
		"country.code.CG",
		"country.code.CD",
		"country.code.CK",
		"country.code.CR",
		"country.code.CI",
		"country.code.HR",
		"country.code.CU",
		"country.code.CW",
		"country.code.CY",
		"country.code.CZ",
		"country.code.DK",
		"country.code.DJ",
		"country.code.DM",
		"country.code.DO",
		"country.code.EC",
		"country.code.EG",
		"country.code.SV",
		"country.code.GQ",
		"country.code.ER",
		"country.code.EE",
		"country.code.ET",
		"country.code.FK",
		"country.code.FO",
		"country.code.FJ",
		"country.code.FI",
		"country.code.FR",
		"country.code.GF",
		"country.code.PF",
		"country.code.TF",
		"country.code.GA",
		"country.code.GM",
		"country.code.GE",
		"country.code.DE",
		"country.code.GH",
		"country.code.GI",
		"country.code.GR",
		"country.code.GL",
		"country.code.GD",
		"country.code.GP",
		"country.code.GU",
		"country.code.GT",
		"country.code.GG",
		"country.code.GN",
		"country.code.GW",
		"country.code.GY",
		"country.code.HT",
		"country.code.HM",
		"country.code.VA",
		"country.code.HN",
		"country.code.HK",
		"country.code.HU",
		"country.code.IS",
		"country.code.IN",
		"country.code.ID",
		"country.code.IR",
		"country.code.IQ",
		"country.code.IE",
		"country.code.IM",
		"country.code.IL",
		"country.code.IT",
		"country.code.JM",
		"country.code.JP",
		"country.code.JE",
		"country.code.JO",
		"country.code.KZ",
		"country.code.KE",
		"country.code.KI",
		"country.code.KP",
		"country.code.KR",
		"country.code.KW",
		"country.code.KG",
		"country.code.LA",
		"country.code.LV",
		"country.code.LB",
		"country.code.LS",
		"country.code.LR",
		"country.code.LY",
		"country.code.LI",
		"country.code.LT",
		"country.code.LU",
		"country.code.MO",
		"country.code.MK",
		"country.code.MG",
		"country.code.MW",
		"country.code.MY",
		"country.code.MV",
		"country.code.ML",
		"country.code.MT",
		"country.code.MH",
		"country.code.MQ",
		"country.code.MR",
		"country.code.MU",
		"country.code.YT",
		"country.code.MX",
		"country.code.FM",
		"country.code.MD",
		"country.code.MC",
		"country.code.MN",
		"country.code.ME",
		"country.code.MS",
		"country.code.MA",
		"country.code.MZ",
		"country.code.MM",
		"country.code.NA",
		"country.code.NR",
		"country.code.NP",
		"country.code.NL",
		"country.code.NC",
		"country.code.NZ",
		"country.code.NI",
		"country.code.NE",
		"country.code.NG",
		"country.code.NU",
		"country.code.NF",
		"country.code.MP",
		"country.code.NO",
		"country.code.OM",
		"country.code.PK",
		"country.code.PW",
		"country.code.PS",
		"country.code.PA",
		"country.code.PG",
		"country.code.PY",
		"country.code.PE",
		"country.code.PH",
		"country.code.PN",
		"country.code.PL",
		"country.code.PT",
		"country.code.PR",
		"country.code.QA",
		"country.code.RE",
		"country.code.RO",
		"country.code.RU",
		"country.code.RW",
		"country.code.BL",
		"country.code.SH",
		"country.code.KN",
		"country.code.LC",
		"country.code.MF",
		"country.code.PM",
		"country.code.VC",
		"country.code.WS",
		"country.code.SM",
		"country.code.ST",
		"country.code.SA",
		"country.code.SN",
		"country.code.RS",
		"country.code.SC",
		"country.code.SL",
		"country.code.SG",
		"country.code.SX",
		"country.code.SK",
		"country.code.SI",
		"country.code.SB",
		"country.code.SO",
		"country.code.ZA",
		"country.code.GS",
		"country.code.ES",
		"country.code.LK",
		"country.code.SD",
		"country.code.SR",
		"country.code.SJ",
		"country.code.SZ",
		"country.code.SE",
		"country.code.CH",
		"country.code.SY",
		"country.code.TW",
		"country.code.TJ",
		"country.code.TZ",
		"country.code.TH",
		"country.code.TL",
		"country.code.TG",
		"country.code.TK",
		"country.code.TO",
		"country.code.TT",
		"country.code.TN",
		"country.code.TR",
		"country.code.TM",
		"country.code.TC",
		"country.code.TV",
		"country.code.UG",
		"country.code.UA",
		"country.code.AE",
		"country.code.GB",
		"country.code.US",
		"country.code.UM",
		"country.code.UY",
		"country.code.UZ",
		"country.code.VU",
		"country.code.VE",
		"country.code.VN",
		"country.code.VG",
		"country.code.VI",
		"country.code.WF",
		"country.code.EH",
		"country.code.YE",
		"country.code.ZM",
		"country.code.ZW",
	};

	/**
	 * returns a String-Array holding all selection-keys
	 * (the options in a drop-down)
	 * 
	 * @return
	 */
	public String[] getSelectionKeys() {
		return countryKeys;
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

		String[] allKeys = new String[countryKeys.length + 1];
		System.arraycopy(countryKeys, 0, allKeys, 1, countryKeys.length);
		allKeys[0] = NO_SEL_KEY;

		SingleSelection sse = FormUIFactory.getInstance().addDropdownSingleselect(getName(), i18nFormElementLabelKey(), formItemContainer,
				allKeys, getTranslatedValues(allKeys, locale), null);
		// make pre-selection of the formItem
		String internalValue = getInternalValue(user);
		if(internalValue == null || internalValue.isEmpty() || NO_SEL_KEY.equals(internalValue)) {
			sse.select(NO_SEL_KEY, true);
		} else if(isValidValue(user, internalValue, null, null) && internalValue != null) {
			sse.select("country.code." + internalValue, true);
		}

		// enable/disable according to settings
		UserManager um = UserManager.getInstance();
		if (um.isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
			sse.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			sse.setMandatory(true);
		}
		
		return sse;
	}

	@Override
	public String getUserProperty(User user, Locale locale) {
		String val = super.getUserProperty(user, locale);
		if(NO_SEL_KEY.equals(val)) {
			val = "";
		}
		return val;
	}

	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getInternalValue(org.olat.core.id.User)
	 */
	@Override
	public String getInternalValue(User user) {
		String value = super.getInternalValue(user);
		return (value == null ? NO_SEL_KEY : value);
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		if (formItem.isMandatory()) {
			SingleSelection ssel = (SingleSelection) formItem;
			if (ssel.getSelectedKey().equals(NO_SEL_KEY)) {
				ssel.setErrorKey("form.legende.mandatory", null);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		boolean allOk = false;
		if (value == null || value.equals(NO_SEL_KEY)) {
			allOk = true;
		} else {
			if(!value.startsWith(KEY_PREFIX)) {
				value = KEY_PREFIX + value;
			}
			
			for (int i=countryKeys.length; i-->0; ) {
				if (countryKeys[i].equals(value)) {
					allOk = true;
					break;
				}
			}
		}
		return allOk;
	}

	@Override
	public String getStringValue(FormItem formItem) {
		if (formItem instanceof SingleSelection) {
			SingleSelection sel = (SingleSelection)formItem;
			if(sel.isOneSelected()) {
				String value = sel.getSelectedKey();
				if(value.startsWith(KEY_PREFIX)) {
					value = value.substring(KEY_PREFIX.length());
				} else if(value.equals(NO_SEL_KEY)) {
					value = null;
				}
				return value;
			} else {
				return null;
			}
		}
		return null;
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
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

	/**
	 * @see org.olat.core.id.UserField#getUserFieldValueAsHTML(org.olat.core.id.User,
	 *      java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		StringBuilder htmlValue = new StringBuilder();
		Translator trans = Util.createPackageTranslator(this.getClass(), locale);
		htmlValue.append(trans.translate(getInternalValue(user)));
		return htmlValue.toString();
	}
}