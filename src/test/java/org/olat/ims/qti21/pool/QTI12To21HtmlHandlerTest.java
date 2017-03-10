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
package org.olat.ims.qti21.pool;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Initial date: 28 févr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class QTI12To21HtmlHandlerTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "<h1>Heading</h1>Bla<br />Dru", "<h1>Heading</h1><p>Bla<br/>Dru</p>" },
                { "Hello world", "Hello world" },
                { "First line <br/>Second line", "<p>First line <br/>Second line</p>" },
                { "<hr />And some content", "<hr/><p>And some content</p>" },
                { "Some content<br />&nbsp;<br /><strong>Strong content!</strong>", "<p>Some content<br/>\u00A0<br/><strong>Strong content!</strong></p>" },
                // https://jira.openolat.org/browse/OO-2608
                { "What are the two different approaches to set up special conditions?<br />&nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp;<br />",
                  "<p>What are the two different approaches to set up special conditions?<br/>                                   <br/></p>"
                }  
        });
    }
    
    private String html12;
    private String blockedHtml;
    
    public QTI12To21HtmlHandlerTest(String html12, String blockedHtml) {
    	this.html12 = html12;
    	this.blockedHtml = blockedHtml;
    }
    
	@Test
	public void convertHtml() {
		String convertedHtml = new QTI12To21Converter(null, null).blockedHtml(html12);
		Assert.assertEquals(blockedHtml, convertedHtml);
	}
}
