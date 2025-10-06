/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.resource.accesscontrol.ui;

import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.Test;
import org.olat.resource.accesscontrol.Price;
import org.olat.resource.accesscontrol.model.PriceImpl;
import org.olat.test.OlatTestCase;

/**
 * 
 * Initial date: 6 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class PriceFormatTest extends OlatTestCase {
	
	@Test
	public void formatForJson() {
		Price price = new PriceImpl();
		price.setCurrencyCode("CHF");
		price.setAmount(new BigDecimal("2000.00"));
		
		String asString = PriceFormat.formatForJson(price);
		Assert.assertEquals("2000.00", asString);
	}
	
	@Test
	public void format() {
		Price price = new PriceImpl();
		price.setCurrencyCode("CHF");
		price.setAmount(new BigDecimal("2000.00"));
		
		String asString = PriceFormat.format(price);
		// This is the default format
		Assert.assertEquals("2'000.00", asString);
	}
}
