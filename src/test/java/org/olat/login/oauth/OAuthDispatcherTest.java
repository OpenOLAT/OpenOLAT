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
package org.olat.login.oauth;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.login.oauth.spi.FacebookProvider;
import org.olat.login.oauth.spi.Google2Provider;
import org.olat.login.oauth.spi.LinkedInProvider;
import org.olat.login.oauth.spi.TwitterProvider;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthDispatcherTest {
	
	@Test
	public void parseEmail_linkedIn() {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>")
		  .append("<person>")
		  .append("<first-name>John</first-name>")
		  .append("<last-name>Smith</last-name>")
		  .append("<email-address>j.smith@openolat.com</email-address>")
		  .append("</person>");
		
		OAuthUser infos = new LinkedInProvider().parseInfos(sb.toString());
		Assert.assertNotNull(infos);
		Assert.assertEquals("John", infos.getFirstName());
		Assert.assertEquals("Smith", infos.getLastName());
		Assert.assertEquals("j.smith@openolat.com", infos.getEmail());
	}
	
	@Test
	public void parseUserInfos_twitter() throws IOException {
		URL jsonUrl = OAuthDispatcherTest.class.getResource("verify_credentials.json");
		String body = IOUtils.toString(jsonUrl);
		
		OAuthUser infos = new TwitterProvider().parseInfos(body);
		Assert.assertNotNull(infos);
		Assert.assertEquals("38895958", infos.getId());
		Assert.assertEquals("Sean", infos.getFirstName());
		Assert.assertEquals("Cook", infos.getLastName());
		Assert.assertEquals("en", infos.getLang()); 
	}
	
	@Test
	public void parseUserInfos_google() throws IOException {
		URL jsonUrl = OAuthDispatcherTest.class.getResource("me_google.json");
		String body = IOUtils.toString(jsonUrl);
		
		OAuthUser infos = new Google2Provider().parseInfos(body);
		Assert.assertNotNull(infos);
		Assert.assertEquals("101991806793974537467", infos.getId());
		Assert.assertEquals("John", infos.getFirstName());
		Assert.assertEquals("Smith", infos.getLastName());
		Assert.assertEquals("fr", infos.getLang()); 
	}
	
	@Test
	public void parseUserInfos_facebook() throws IOException {
		URL jsonUrl = OAuthDispatcherTest.class.getResource("me_facebook.json");
		String body = IOUtils.toString(jsonUrl);
		
		OAuthUser infos = new FacebookProvider().parseInfos(body);
		Assert.assertNotNull(infos);
		Assert.assertEquals("4", infos.getId());
		Assert.assertEquals("John", infos.getFirstName());
		Assert.assertEquals("Smith", infos.getLastName());
		Assert.assertEquals("en_US", infos.getLang()); 
	}
}
