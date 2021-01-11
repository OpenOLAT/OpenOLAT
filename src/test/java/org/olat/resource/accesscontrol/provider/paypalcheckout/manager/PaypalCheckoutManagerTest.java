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
package org.olat.resource.accesscontrol.provider.paypalcheckout.manager;

import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.i18n.I18nManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutManager;
import org.olat.resource.accesscontrol.provider.paypalcheckout.PaypalCheckoutModule;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 11 janv. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PaypalCheckoutManagerTest extends OlatTestCase {

	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private PaypalCheckoutModule paypalCheckoutModule;
	@Autowired
	private PaypalCheckoutManager paypalCheckoutManager;
	
	@Test
	public void preferredLocale() {
		paypalCheckoutModule.setPreferredCountries("");
		
		Locale deLocale = i18nManager.getLocaleOrDefault("de");
		String preferredDeLocale = paypalCheckoutManager.getPreferredLocale(deLocale);
		Assert.assertEquals("de_DE", preferredDeLocale);
		
		Locale frLocale = i18nManager.getLocaleOrDefault("fr");
		String preferredFrLocale = paypalCheckoutManager.getPreferredLocale(frLocale);
		Assert.assertEquals("fr_FR", preferredFrLocale);
		
		Locale jpLocale = i18nManager.getLocaleOrDefault("jp");
		String preferredJpLocale = paypalCheckoutManager.getPreferredLocale(jpLocale);
		Assert.assertEquals("ja_JP", preferredJpLocale);
		
		Locale ptBrLocale = i18nManager.getLocaleOrDefault("pt_BR");
		String preferredPtBrLocale = paypalCheckoutManager.getPreferredLocale(ptBrLocale);
		Assert.assertEquals("pt_BR", preferredPtBrLocale);

		Locale enLocale = i18nManager.getLocaleOrDefault("en");
		String preferredEnLocale = paypalCheckoutManager.getPreferredLocale(enLocale);
		Assert.assertTrue(preferredEnLocale.startsWith("en"));
	}
	
	@Test
	public void preferredLocaleInSwitzerland() {
		paypalCheckoutModule.setPreferredCountries("CH");
		
		Locale deLocale = i18nManager.getLocaleOrDefault("de");
		String preferredDeLocale = paypalCheckoutManager.getPreferredLocale(deLocale);
		Assert.assertEquals("de_CH", preferredDeLocale);
		
		Locale frLocale = i18nManager.getLocaleOrDefault("fr");
		String preferredFrLocale = paypalCheckoutManager.getPreferredLocale(frLocale);
		Assert.assertEquals("fr_CH", preferredFrLocale);
		
		Locale jpLocale = i18nManager.getLocaleOrDefault("jp");
		String preferredJpLocale = paypalCheckoutManager.getPreferredLocale(jpLocale);
		Assert.assertEquals("ja_JP", preferredJpLocale);
		
		Locale ptBrLocale = i18nManager.getLocaleOrDefault("pt_BR");
		String preferredPtBrLocale = paypalCheckoutManager.getPreferredLocale(ptBrLocale);
		Assert.assertEquals("pt_BR", preferredPtBrLocale);

		Locale enLocale = i18nManager.getLocaleOrDefault("en");
		String preferredEnLocale = paypalCheckoutManager.getPreferredLocale(enLocale);
		Assert.assertEquals("en_CH", preferredEnLocale);
	}
}
