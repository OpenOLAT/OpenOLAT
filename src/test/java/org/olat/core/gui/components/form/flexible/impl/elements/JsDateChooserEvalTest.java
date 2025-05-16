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
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Initial date: 16 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class JsDateChooserEvalTest {

	@Parameters
	public static Collection<Object[]> data() {
    	return Arrays.asList(new Object[][] {
        		// English
        		{ "1/2/2022", "1/2/22" },
                { "1/2/1922", "1/2/1922" },
                { "1/2/2023", "1/2/2023" },
                { "1/2/2022", "1/2/2022" },
                { "11/21/2025", "  11/21/2025  " },
                { "11/21/2025", "  11/21/25  " },
                // German
                { "1.2.2022", "1.2.22" },
                { "1.2.1922", "1.2.1922" },
                { "11.21.2025", "  11.21.25  " },
                // Slovakia
                { "1. 2. 2022", "1. 2. 22" },
                { "1. 2. 1922", "1. 2. 1922" },
                { "11. 21. 2025", "  11. 21. 25  " }
        });
    }
    
    private String expected;
    private String date;
    
	public JsDateChooserEvalTest(String expected, String date) {
		this.expected = expected;
		this.date = date;
	}
	
	@Test
	public void expandYearToFourDigits() {
		Assert.assertEquals(expected, JSDateChooser.expandYearToFourDigits(date));
	}
}
