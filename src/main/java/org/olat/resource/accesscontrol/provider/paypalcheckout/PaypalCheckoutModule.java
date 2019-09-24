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
package org.olat.resource.accesscontrol.provider.paypalcheckout;

import java.util.Arrays;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 23 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PaypalCheckoutModule extends AbstractSpringModule {
	
	private static final String PAYPAL_CLIENT_ID = "paypal.checkout.v2.client.id";
	private static final String PAYPAL_CLIENT_SECRET = "paypal.checkout.v2.client.secret";
	private static final String PAYPAL_CURRENCY = "paypal.checkout.v2.currency";
	
	private static final String[] currencies = new String[] {
			"AUD",
			"CAD",
			"CZK",
			"DKK",
			"EUR",
			"HKD",
			"HUF",
			"ILS",
			"JPY",
			"MXN",
			"NOK",
			"NZD",
			"PHP",
			"PLN",
			"GBP",
			"SGD",
			"SEK",
			"CHF",
			"TWD",
			"THB",
			"TRY",
			"USD"
		};
	
	@Value("${paypal.checkout.v2.client.id}")
	private String clientId;
	@Value("${paypal.checkout.v2.client.secret}")
	private String clientSecret;
	@Value("${paypal.checkout.v2.sandbox:false}")
	private boolean sandbox;
	@Value("${paypal.checkout.v2.currency:CHF}")
	private String paypalCurrency;
	
	@Autowired
	public PaypalCheckoutModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager, true);
	}

	@Override
	public void init() {
		initFromChangedProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		clientId = getStringPropertyValue(PAYPAL_CLIENT_ID, clientId);
		clientSecret = getStringPropertyValue(PAYPAL_CLIENT_SECRET, clientSecret);
		paypalCurrency = getStringPropertyValue(PAYPAL_CURRENCY, paypalCurrency);
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
		setStringProperty(PAYPAL_CLIENT_ID, clientId, true);
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String secret) {
		this.clientSecret = secret;
		setSecretStringProperty(PAYPAL_CLIENT_SECRET, secret, true);
	}
	
	public List<String> getPaypalCurrencies() {
		return Arrays.asList(currencies);
	}
	
	public String getPaypalCurrency() {
		return paypalCurrency;
	}

	public void setPaypalCurrency(String currency) {
		this.paypalCurrency = currency;
		setStringProperty(PAYPAL_CURRENCY, currency, true);
	}

	public boolean isSandbox() {
		return sandbox;
	}

	public void setSandbox(boolean sandbox) {
		this.sandbox = sandbox;
	}
}
