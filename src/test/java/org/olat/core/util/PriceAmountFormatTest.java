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
package org.olat.core.util;

import java.math.BigDecimal;

import org.olat.test.OlatTestCase;
import org.junit.Assert;
import org.junit.Test;

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
public class PriceAmountFormatTest extends OlatTestCase {

	@Test
	public void testFormat() {
		BigDecimal amount100 = new BigDecimal("100");
		String amount100String = PriceAmountFormat.apostrophePoint.format(amount100);
		Assert.assertEquals("100.00", amount100String);
		
		BigDecimal amountSeveralThousand = new BigDecimal("17930.63");
		
		String amountSeveralThousandPointComma = PriceAmountFormat.pointComma.format(amountSeveralThousand);
		Assert.assertEquals("17.930,63", amountSeveralThousandPointComma);
		
		String amountSeveralThousandsSpacePoint = PriceAmountFormat.spacePoint.format(amountSeveralThousand);
		Assert.assertEquals("17 930.63", amountSeveralThousandsSpacePoint);
		
		BigDecimal amountMillions = new BigDecimal("2139444.78");
		
		String amountMillionsApostrophePoint = PriceAmountFormat.apostrophePoint.format(amountMillions);
		Assert.assertEquals("2'139'444.78", amountMillionsApostrophePoint);
		
		String amountMillionsCommaPoint = PriceAmountFormat.commaPoint.format(amountMillions);
		Assert.assertEquals("2,139,444.78", amountMillionsCommaPoint);
		
		String amountMillionsSpaceComma = PriceAmountFormat.spaceComma.format(amountMillions);
		Assert.assertEquals("2 139 444,78", amountMillionsSpaceComma);
		
		String amountZeroApostrophePoint = PriceAmountFormat.apostrophePoint.format(BigDecimal.ZERO);
		Assert.assertEquals("0.00", amountZeroApostrophePoint);
		
		BigDecimal amountVeryCheap = new BigDecimal("12.3");
		String amountVeryCheapSpacePoint = PriceAmountFormat.spacePoint.format(amountVeryCheap);
		Assert.assertEquals("12.30", amountVeryCheapSpacePoint);
		
		String amountVeryCheapPointComma = PriceAmountFormat.pointComma.format(amountVeryCheap);
		Assert.assertEquals("12,30", amountVeryCheapPointComma);
		
		BigDecimal amountPracticallyFree = new BigDecimal("0.70");
		String amountPracticallyFreeSpaceComma = PriceAmountFormat.spaceComma.format(amountPracticallyFree);
		Assert.assertEquals("0,70", amountPracticallyFreeSpaceComma);
		
		String amountPracticallyFreeApostrophePoint = PriceAmountFormat.apostrophePoint.format(amountPracticallyFree);
		Assert.assertEquals("0.70", amountPracticallyFreeApostrophePoint);
	}
}