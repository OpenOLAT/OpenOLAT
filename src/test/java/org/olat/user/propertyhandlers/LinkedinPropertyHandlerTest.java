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
package org.olat.user.propertyhandlers;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * Initial date: 14 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class LinkedinPropertyHandlerTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "https://www.linkedin.com/in/openolat-lms", Boolean.TRUE },
                { "https://www.linkedin.com/company/frentix-gmbh", Boolean.TRUE },
                { "https://www.linkedin.com/company/openolat-world", Boolean.TRUE },
                { "http://www.linkedi.com/company/openolat-world", Boolean.FALSE }
        });
    }
    
    private boolean valid;
    private String url;
    
    public LinkedinPropertyHandlerTest(String url, Boolean valid) {
    	this.url = url;
    	this.valid = valid;
    }
    
	@Test
	public void validate() {
		boolean ok = LinkedinPropertyHandler.validUrl(url);
		Assert.assertEquals(valid, ok);
	}
}
