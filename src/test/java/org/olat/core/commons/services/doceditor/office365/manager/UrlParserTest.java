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
package org.olat.core.commons.services.doceditor.office365.manager;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * 
 * Initial date: 2 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UrlParserTest {

	private UrlParser sut = new UrlParser();
	
	@Test
	public void shouldGetProtocolAndDomain() {
		String urlSrc = "https://FFC-excel.officeapps.live.com/x/_layouts/xlviewerinternal.aspx?<ui=UI_LLCC&><rs=DC_LLCC&><dchat=DISABLE_CHAT&><hid=HOST_SESSION_ID&><sc=SESSION_CONTEXT&><wopisrc=WOPI_SOURCE&><IsLicensedUser=BUSINESS_USER&><actnavid=ACTIVITY_NAVIGATION_ID&>";
		
		String protocolAndDomain = sut.getProtocolAndDomain(urlSrc);
		
		assertThat(protocolAndDomain).isEqualTo("https://FFC-excel.officeapps.live.com");
	}
	
	@Test
	public void shouldStripQuery() {
		String url = "https://FFC-excel.officeapps.live.com/x/_layouts/xlembed.aspx?<ui=UI_LLCC&><rs=DC_LLCC&><dchat=DISABLE_CHAT&><hid=HOST_SESSION_ID&><sc=SESSION_CONTEXT&><wopisrc=WOPI_SOURCE&>";
		
		String strippedUrl = sut.stripQuery(url);
		
		assertThat(strippedUrl).isEqualTo("https://FFC-excel.officeapps.live.com/x/_layouts/xlembed.aspx");
	}
	
	@Test
	public void shouldGetLanguageParameter() {
		String url = "https://FFC-excel.officeapps.live.com/x/_layouts/xlembed.aspx?<ui=UI_LLCC&><rs=DC_LLCC&><dchat=DISABLE_CHAT&><hid=HOST_SESSION_ID&><sc=SESSION_CONTEXT&><wopisrc=WOPI_SOURCE&>";
		
		String languageParameter = sut.getLanguageParameter(url);
		
		assertThat(languageParameter).isEqualTo("ui");
	}
	
	@Test
	public void shouldGetLanguageParameterNotFound() {
		String url = "https://FFC-excel.officeapps.live.com/x/_layouts/xlembed.aspx?<rs=DC_LLCC&><dchat=DISABLE_CHAT&><hid=HOST_SESSION_ID&><sc=SESSION_CONTEXT&><wopisrc=WOPI_SOURCE&>";
		
		String languageParameter = sut.getLanguageParameter(url);
		
		assertThat(languageParameter).isNull();
	}


}
