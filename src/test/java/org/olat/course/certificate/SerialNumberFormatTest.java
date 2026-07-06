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
package org.olat.course.certificate;

import java.time.LocalDate;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 3 juil. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class SerialNumberFormatTest {

	private static final LocalDate DATE = LocalDate.of(2026, 3, 7);

	@Test
	public void generateExample() {
		SerialNumberFormat format = SerialNumberFormat.parse("REF-${year}-${counter:5}");
		Assert.assertEquals("REF-2026-00001", format.generate(1L, DATE));
		Assert.assertEquals("REF-2026-00042", format.generate(42L, DATE));
	}

	@Test
	public void plainCounter() {
		SerialNumberFormat format = SerialNumberFormat.parse("${counter}");
		Assert.assertEquals("1", format.generate(1L, DATE));
		Assert.assertEquals("12345", format.generate(12345L, DATE));
	}

	@Test
	public void counterLongerThanPadding() {
		SerialNumberFormat format = SerialNumberFormat.parse("${counter:3}");
		Assert.assertEquals("001", format.generate(1L, DATE));
		Assert.assertEquals("123456", format.generate(123456L, DATE));
	}

	@Test
	public void yearMonthDay() {
		SerialNumberFormat format = SerialNumberFormat.parse("${year}${month}${day}-${counter}");
		Assert.assertEquals("20260307-7", format.generate(7L, DATE));
	}

	@Test
	public void whitespaceInToken() {
		SerialNumberFormat format = SerialNumberFormat.parse("${ counter : 4 }");
		Assert.assertEquals("0009", format.generate(9L, DATE));
	}

	@Test
	public void unknownTokenKeptVerbatim() {
		SerialNumberFormat format = SerialNumberFormat.parse("A-${unknown}-${counter}");
		Assert.assertEquals("A-${unknown}-5", format.generate(5L, DATE));
	}

	@Test
	public void hasCounter() {
		Assert.assertTrue(SerialNumberFormat.parse("${counter}").hasCounter());
		Assert.assertTrue(SerialNumberFormat.parse("X-${counter:3}").hasCounter());
		Assert.assertFalse(SerialNumberFormat.parse("${year}").hasCounter());
		Assert.assertFalse(SerialNumberFormat.parse(null).hasCounter());
		Assert.assertFalse(SerialNumberFormat.parse("").hasCounter());
	}

	@Test
	public void nullFormat() {
		Assert.assertEquals("", SerialNumberFormat.parse(null).generate(1L, DATE));
	}
}
