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
import java.util.HashMap;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.olat.login.oauth.model.OAuthUser;
import org.olat.login.oauth.spi.ADFSApi;
import org.olat.login.oauth.spi.FacebookProvider;
import org.olat.login.oauth.spi.Google2Provider;
import org.olat.login.oauth.spi.JSONWebToken;
import org.olat.login.oauth.spi.LinkedInProvider;
import org.olat.login.oauth.spi.SwitchEduIDApi;
import org.olat.login.oauth.spi.TequilaApi;
import org.olat.login.oauth.spi.TequilaProvider;
import org.olat.login.oauth.spi.TwitterProvider;

import com.github.scribejava.apis.openid.OpenIdOAuth2AccessToken;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.Response;

/**
 * 
 * Initial date: 04.11.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OAuthDispatcherTest {
	
	@Test
	public void parseEmail_linkedIn() {
		String profile = "{\"localizedLastName\":\"Smith\",\"id\":\"saasdhgdhj\",\"localizedFirstName\":\"John\"}";
		String email = "{\"elements\":[{\"handle~\":{\"emailAddress\":\"j.smith@openolat.com\"},\"handle\":\"urn:li:emailAddress:477232709\"}]}";

		OAuthUser infos = new LinkedInProvider().parseInfos(profile, email);
		Assert.assertNotNull(infos);
		Assert.assertEquals("saasdhgdhj", infos.getId());
		Assert.assertEquals("John", infos.getFirstName());
		Assert.assertEquals("Smith", infos.getLastName());
		Assert.assertEquals("j.smith@openolat.com", infos.getEmail());
	}
	
	@Test
	public void parseUserInfos_twitter() throws IOException {
		URL jsonUrl = OAuthDispatcherTest.class.getResource("verify_credentials.json");
		String body = IOUtils.toString(jsonUrl, "UTF-8");
		
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
		String body = IOUtils.toString(jsonUrl, "UTF-8");
		
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
		String body = IOUtils.toString(jsonUrl, "UTF-8");
		
		OAuthUser infos = new FacebookProvider().parseInfos(body);
		Assert.assertNotNull(infos);
		Assert.assertEquals("4", infos.getId());
		Assert.assertEquals("John", infos.getFirstName());
		Assert.assertEquals("Smith", infos.getLastName());
		Assert.assertEquals("en_US", infos.getLang()); 
	}
	
	@Test
	public void parseADFSToken() throws JSONException, IOException {
		String responseBody = "{\"access_token\":\"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IkhnbmtjOVBMd0E3ampHMjlWbndpQk43WnlaYyJ9.eyJhdWQiOiJodHRwczovL2tpdmlrLmZyZW50aXguY29tL29sYXQiLCJpc3MiOiJodHRwOi8vYWRmcy5oYW1pbHRvbi5jaC9hZGZzL3NlcnZpY2VzL3RydXN0IiwiaWF0IjoxNDE1MzQ3MDE0LCJleHAiOjE0MTUzNTA2MTQsIlNuIjoiT3Blbk9sYXQiLCJkaXNwbGF5TmFtZVByaW50YWJsZSI6IlRlc3R1c2VyIiwiU0FNQWNjb3VudE5hbWUiOiJ0ZXN0X29wZW5vbGF0IiwiYXV0aF90aW1lIjoiMjAxNC0xMS0wN1QwNzo1Njo1NC4zOTFaIiwiYXV0aG1ldGhvZCI6InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkUHJvdGVjdGVkVHJhbnNwb3J0IiwidmVyIjoiMS4wIiwiYXBwaWQiOiIyNWU1M2VmNC02NTllLTExZTQtYjExNi0xMjNiOTNmNzVjYmEifQ.l17qB7LWkODD66OuRbhjDEdKEQWrEfaeR7hBpbdN8XqGIOMS2sc2xQNYJH9Lh061XOJt9WPqrAW8sHSu2eaR1qpw8o6LcWksKvh0LJbCmVPqQggLDj8Q4kSFIzbs9YAQautTAvobdb_hsoGT9rhGN4SDIcpJA8Uq8JWwYDjWfDCpCVRHZPmyZiOmh-5rBT8SxSiV0QgFexhmbvLAZhaEmsZGzSaj2r39cyK0dlt7OuR_1KjQeB86ycOMP1PT1OAGWJc1lgGP12gDo-FkcK5mOY6mgC8za7OOwgTUkE4pbXwygi4nPBXHQVPku-bWtigLZWfTln4Ght3fqMIzJOQXag\",\"token_type\":\"bearer\",\"expires_in\":3600}";
		Response response = new Response(200, "", new HashMap<>(), responseBody);
		
		OAuth2AccessToken accessToken = new ADFSApi().getAccessTokenExtractor().extract(response);
		Assert.assertNotNull(accessToken);
		
		String token = accessToken.getAccessToken();
		Assert.assertNotNull(token);
		
		//get JSON Web Token
		JSONWebToken jwt = JSONWebToken.parse(accessToken);
		String header = jwt.getHeader();
		Assert.assertNotNull(header);
		String payload = jwt.getPayload();
		System.out.println(payload);
		Assert.assertNotNull(payload);
		JSONObject payloadObj = jwt.getJsonPayload();
		Assert.assertNotNull(payloadObj);
		Assert.assertEquals("test_openolat", payloadObj.opt("SAMAccountName"));
		Assert.assertEquals("OpenOlat", payloadObj.opt("Sn"));
		Assert.assertEquals("Testuser", payloadObj.opt("displayNamePrintable"));
	}
	
	@Test
	public void oAuthUserInfos_toString() throws JSONException {
		OAuthUser infos = new OAuthUser();
		infos.setId("mySecretId");
		infos.setEmail("mySecretEmail@openolat.com");
		String toString = infos.toString();
		Assert.assertTrue(toString.contains("mySecretId"));
		Assert.assertTrue(toString.contains("mySecretEmail@openolat.com"));
	}
	
	@Test
	public void oAuthUserInfos_toString_NULL() throws JSONException {
		OAuthUser infos = new OAuthUser();
		String toString = infos.toString();
		Assert.assertNotNull(toString);
	}
	
	@Test
	public void extractTequilaBearerToken() {
		String responseBody = "\"access_token\": \"Bearer 880a11c9aaae0abf0f6a384c559110d8c7570456\", \"scope\": \"Tequila.profile\"";
		Response response = new Response(200, "", new HashMap<>(), responseBody);
		TequilaApi.TequilaBearerExtractor extractor = new TequilaApi.TequilaBearerExtractor();
		OAuth2AccessToken token = extractor.extract(response);
		String accessToken = token.getAccessToken();
		Assert.assertEquals("880a11c9aaae0abf0f6a384c559110d8c7570456", accessToken);
	}
	
	@Test
	public void parseTequilaUserInfos() {
		String data = "{ \"Sciper\": \"M02491\", \"authscheme\": \"OAuth2\", \"Firstname\": \"Service\", \"Username\": \"Erecruiting_oAuth2\", \"Name\": \"Erecruiting_oAuth2\", \"scope\": \"Tequila.profile\" }";
		OAuthUser infos = new TequilaProvider().parseResponse(data);
		Assert.assertNotNull(infos);
		Assert.assertEquals("Service",  infos.getFirstName());
		Assert.assertEquals("Erecruiting_oAuth2",  infos.getLastName());
		Assert.assertEquals("M02491",  infos.getId());	
	}
	
	@Test
	public void parseSwitchEduID() throws IOException {
		String responseBody = "{\"access_token\":\"AApzZWNyZXQxODM0b0z_NtWtV26CDYReJoasb1zZuZJnWtVvpUFnWkvE0edjLOQloPRkTM5HZ8lJP386olx15qtDTi9CHrGAqE8F2aFWY3TWlPwLR4mYnhMIXI0fy5035AOaS-pUsM2mCDWHX_G4_pbMZiwMhUIl0z7g0eH2od054WgRkm_YpbkG8L7rq4lTcmatP-Y_4RvBqRjaYlP2xnpQptKHheo9vEJM6c6gdTuBGpmeeAFZVzc7bx8fJhFk1uhh75hCbTGZDlIqkEBiKRlq74C82DrLgPGQ3DGIx4xNCqa0NGODTQavs_qhKa-fEWOW1dHk0pvGyqIwCifmGAJ-puC05RSNHQ0WJuKdqZfDT8usA4wtwub_rb6I4PQ3D6Lk9Ql0RFmp_BYwwDJTEBA-5t3Y-Vozqucte3EINSXF6_z3TsS1tl1W2-2T-yOpShUpQ_CwHbiYN0yZemikrtsvZkZ-5RUTxRe_B7LQYY4_8f4AS478yjCZblS1DhE\",\"id_token\":\"eyJraWQiOiJkZWZhdWx0UlNBU2lnbiIsImFsZyI6IlJTMjU2In0.eyJhdF9oYXNoIjoiVUxBVktKU25SWmUxMG11bU9OdVRRZyIsInN1YiI6IlJWNDZETlJRQU9WTTZINllHWkNDQUc1UkJMRVpPVjdOIiwiYXVkIjoiZnJlbnRpeF9vaWRjX2tpdmlrIiwiYWNyIjoicGFzc3dvcmQiLCJhdXRoX3RpbWUiOjE2NTQyNjE4ODMsImlzcyI6Imh0dHBzOlwvXC9sb2dpbi50ZXN0LmVkdWlkLmNoXC8iLCJleHAiOjE2NTQyNzYyODMsImlhdCI6MTY1NDI2MTg4Mywibm9uY2UiOiJkNmNlOGExYS1mOTJmLTRlNjEtYjgwOS0xZDU4ZTdmMjAzOWEifQ.c910sFPZhf0Ya8DtIja25oq7ZZWyO5VpIZ1-tRr3TC7QrJALv8Fw90u5xPdpALptY9K_-oQY28p-qDuHzjf0erGxuJ7lV7jfG1ALlpdBkbIN_EDsmViQRHRFnKAHZmUpBmoltUHYHCkR2ze7d-kBsAYpr1Dxk4tsK6O4_giauUXRdVeYQuypNmtd3WtBt4ExCmg9jHGWEf_IUbn7bh_rzmXqMnuUGl1j1O-EQjzTJrgF3zGrxTqHyQFuoJ534E-tDsF6rOpgDZWCmKOUIFfxyEFLkAB_TJNdch4Byhiyr2-EiEbGrIYkfFXTNwQLd3wyAxXt1v1HzshxbtmJlrFyiw\",\"token_type\":\"Bearer\",\"expires_in\":300}";
		Response response = new Response(200, "", new HashMap<>(), responseBody);
		
		OAuth2AccessToken oauth2AccessToken = new SwitchEduIDApi(null).getAccessTokenExtractor().extract(response);
		Assert.assertNotNull(oauth2AccessToken);
		
		OpenIdOAuth2AccessToken openIdAccessToken = (OpenIdOAuth2AccessToken)oauth2AccessToken;
		JSONWebToken jwToken = JSONWebToken.parse(openIdAccessToken.getOpenIdToken());
		Assert.assertNotNull(jwToken);
		
		JSONObject payload = jwToken.getJsonPayload();
		Assert.assertNotNull( payload);
		Assert.assertEquals("https://login.test.eduid.ch/", payload.get("iss"));
		Assert.assertEquals("d6ce8a1a-f92f-4e61-b809-1d58e7f2039a", payload.get("nonce"));
	}
}
