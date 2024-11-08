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
package org.olat.resource.accesscontrol.provider.invoice;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Nov 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class InvoiceModule extends AbstractSpringModule {
	
	private static final String DELIMITER = "::";
	private static final String CURRENCIES = "method.invoice.currencies";
	private static final String CURRENCY_DEFAULT = "method.invoice.currency.default";
	private static final String CANCELLING_FEE_DEFAULT = "method.invoice.cancelling.fee.default";
	private static final String CANCELLING_FEE_DEADLINE_DAYS_DEFAULT = "method.invoice.cancelling.fee.deadline.days.default";
	
	@Value("${method.invoice.currencies}")
	private String currenciesValue;
	private List<String> currencies;
	@Value("${method.invoice.currency.default}")
	private String currencyDefault;
	@Value("${method.invoice.cancelling.fee.default}")
	private String cancellingFeeDefault;
	private Map<String, BigDecimal> cancellingFeeDefaults;
	@Value("${method.invoice.cancelling.fee.deadline.days.default}")
	private Integer cancellingFeeDeadlineDaysDefault;

	@Autowired
	public InvoiceModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}
	
	@Override
	public void init() {
		updateProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		currenciesValue = getStringPropertyValue(CURRENCIES, currenciesValue);
		currencies = null;
		
		currencyDefault = getStringPropertyValue(CURRENCY_DEFAULT, currencyDefault);
		
		cancellingFeeDefault = getStringPropertyValue(CANCELLING_FEE_DEFAULT, cancellingFeeDefault);
		cancellingFeeDefaults = null;
		
		String cancellingFeeDefaultObj = getStringPropertyValue(CANCELLING_FEE_DEADLINE_DAYS_DEFAULT, true);
		if (StringHelper.isLong(cancellingFeeDefaultObj)) {
			cancellingFeeDeadlineDaysDefault = Integer.valueOf(cancellingFeeDefaultObj);
		}
	}
	
	public List<String> getCurrencies() {
		if (currencies == null) {
			currencies = Arrays.stream(currenciesValue.split(DELIMITER)).toList();
		}
		return currencies;
	}

	public String getCurrencyDefault() {
		return currencyDefault;
	}

	public Map<String, BigDecimal> getCancellingFeeDefaults() {
		if (cancellingFeeDefaults == null) {
			cancellingFeeDefaults = new HashMap<>(3);
			if (StringHelper.containsNonWhitespace(cancellingFeeDefault)) {
				for (String fee : cancellingFeeDefault.split(DELIMITER)) {
					try {
						if (fee.length() > 4) {
							String currency = fee.substring(0, 3);
							String ammountValue = fee.substring(3);
							BigDecimal ammount = new BigDecimal(ammountValue);
							cancellingFeeDefaults.put(currency.toUpperCase(), ammount);
						}
					} catch (Exception e) {
						//
					}
				}
			}
		}
		return cancellingFeeDefaults;
	}

	public void setCancellingFeeDefaults(Map<String, BigDecimal> cancellingFeeDefaults) {
		cancellingFeeDefault = cancellingFeeDefaults.entrySet().stream()
				.map(kv -> kv.getKey().toUpperCase() + kv.getValue().toString())
				.collect(Collectors.joining(DELIMITER));
		setStringProperty(CANCELLING_FEE_DEFAULT, cancellingFeeDefault, true);
		
		this.cancellingFeeDefaults = null;
	}

	public Integer getCancellingFeeDeadlineDaysDefault() {
		return cancellingFeeDeadlineDaysDefault;
	}

	public void setCancellingFeeDeadlineDaysDefault(Integer cancellingFeeDeadlineDaysDefault) {
		this.cancellingFeeDeadlineDaysDefault = cancellingFeeDeadlineDaysDefault;
		if (cancellingFeeDeadlineDaysDefault != null) {
			setIntProperty(CANCELLING_FEE_DEADLINE_DAYS_DEFAULT, cancellingFeeDeadlineDaysDefault, true);
		} else {
			removeProperty(CANCELLING_FEE_DEADLINE_DAYS_DEFAULT, true);
		}
	}
}
