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
package org.olat.core.util;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Initial date: 23 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class FormatLatexFormulasTest {
	
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "<span class='math'></span>", Boolean.TRUE },
                { "<span class='math inline'></span>", Boolean.TRUE },
                { "<div class = \"inline math special\"></div>", Boolean.TRUE },
                { "<span class = math></span>", Boolean.TRUE },
                { "<math></math>", Boolean.TRUE },
                { "<MATH></MATH>", Boolean.TRUE },
        		
                { "<span class = \"nomath\"></span>", Boolean.FALSE },
                { "<span class='test' id='math'></span>", Boolean.FALSE },
                { "<span class='math\"></span>", Boolean.FALSE },
        			// real example of a wiki
                { "&lt;h2&gt;Hier den unformatierten Text eingeben&lt;/h2&gt;<p>Ceci est une page avec LaTeX\n</p><div class=\"math\">x^2</div>", Boolean.TRUE },
                { "<span class=\"math\" title=\"A%20%3D%20%5Cpmatrix%7Ba_%7B11%7D%20%26%20a_%7B12%7D%20%26%20%5Cldots%20%26%20a_%7B1n%7D%5Ccr%0A%20%20%20%20%20%20%20%20%20%20%20%20%20a_%7B21%7D%20%26%20a_%7B22%7D%20%26%20%5Cldots%20%26%20a_%7B2n%7D%5Ccr%0A%20%20%20%20%20%20%20%20%20%20%20%20%20%5Cvdots%20%26%20%5Cvdots%20%26%20%5Cddots%20%26%20%5Cvdots%5Ccr%0A%20%20%20%20%20%20%20%20%20%20%20%20%20a_%7Bm1%7D%20%26%20a_%7Bm2%7D%20%26%20%5Cldots%20%26%20a_%7Bmn%7D%7D%0A%5Cqquad%5Cqquad%0A%5Cpmatrix%7By_1%5Ccr%5Cvdots%5Ccr%20y_k%7D\">A = \\pmatrix{a_{11} &amp; a_{12} &amp; \\ldots &amp; a_{1n}\\cr a_{21} &amp; a_{22} &amp; \\ldots &amp; a_{2n}\\cr \\vdots &amp; \\vdots &amp; \\ddots &amp; \\vdots\\cr a_{m1} &amp; a_{m2} &amp; \\ldots &amp; a_{mn}} \\qquad\\qquad \\pmatrix{y_1\\cr\\vdots\\cr y_k}</span></p>\n\"", Boolean.TRUE },
                { "<DIV CLASS='math'></DIV>", Boolean.TRUE },
                
                { "<span class='math\"''></span>", Boolean.FALSE },
                { "<span class='math\"'\"></span>", Boolean.FALSE },
                { "<span class='math></\"span>", Boolean.FALSE },
                { "<span class='math></'span>", Boolean.FALSE },
                { "<span class='math", Boolean.FALSE },
                { "<span class=math\"", Boolean.FALSE }
        });
    }
    
    private String text;
    private Boolean result;
    
    public FormatLatexFormulasTest(String text, Boolean result) {
    		this.text = text;
    		this.result = result;
    }
    
	@Test
	public void formatLatexFormulas() {
		Assert.assertEquals(text, result.booleanValue(), Formatter.formatLatexFormulas(text).contains("<script"));
	}
}
