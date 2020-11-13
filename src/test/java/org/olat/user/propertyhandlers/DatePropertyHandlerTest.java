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
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olat.user.UserImpl;

/**
 * 
 * Initial date: 13 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class DatePropertyHandlerTest {
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "", null },
                { ".", null },
                { ".67", null },
                { "\u0009", null },
                { "19990809", "09.08.1999" },
        });
    }
   
    private String date;
    private String userDate;
    
    public DatePropertyHandlerTest(String date, String userDate) {
    	this.date = date;
    	this.userDate = userDate;
    }
    
	@Test
	public void getUserProperty() {
		UserImpl user = new UserImpl();
		user.setUserProperty("birthDay", date);
		DatePropertyHandler handler = new DatePropertyHandler();
		handler.setName("birthDay");
		
		String val = handler.getUserProperty(user, Locale.GERMAN);
		Assert.assertEquals(val, userDate);
	}
}
