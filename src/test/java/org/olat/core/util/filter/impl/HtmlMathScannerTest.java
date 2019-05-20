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
package org.olat.core.util.filter.impl;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olat.core.util.WebappHelper;

/**
 * 
 * Initial date: 18 mai 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class HtmlMathScannerTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { true, "<p><span class=\"math\" title=\"%5Coverbrace%7Bx+%5Ccdots+x%5C%2C%7D%5E%7Bk%5Crm%5C%3Btimes%7D%20%5Cqquad%0A%5Cunderbrace%7Bx+y+z%5C%2C%7D_%7B%3E%5C%2C0%7D\">\\overbrace{x+\\cdots+x\\,}^{k\\rm\\;times} \\qquad \\underbrace{x+y+z\\,}_{&gt;\\,0}</span></p>" },
                { true, "\\(math\\)" },
                { true, "Ceci <p>\\[math\\]</p>" },
                { true, "Was ist das <p>$$math$$</p>" },
                { true, "<span><math xmlns=\"http://www.w3.org/1998/Math/MathML\"><semantics><mrow><mi>π</mi><mo>=</mo><mfrac><mi>C</mi><mi>d</mi></mfrac></mrow><annotation encoding=\"SnuggleTeX\">$\\pi = \\frac{C}{d}</annotation></semantics></math>" },
                { false, "math" },
                { false, "<p>math</p>" }
        });
    }
	

	private final HtmlMathScanner filter = new HtmlMathScanner();

	private boolean hasMath;
	private String input;
	private boolean markersSettings;

	public HtmlMathScannerTest(boolean hasMath, String input) {
		this.hasMath = hasMath;
		this.input = input;
	}
	
	@Before
	public void setMathJaxMarkers() {
		markersSettings = WebappHelper.isMathJaxMarkers(); 
		new WebappHelper().setMathJaxMarkers(true);
	}
	
	@After
	public void resetMathJaxMarkers() {
		new WebappHelper().setMathJaxMarkers(markersSettings);
	}

	@Test
	public void hasMath() {
		boolean output = filter.scan(input);
		Assert.assertEquals(hasMath, output);
	}
}
