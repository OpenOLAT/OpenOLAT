/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.gui.render;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNoException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

/**
 * Description:<br>
 * downloads the entry page from olat*.uzh.ch and checks if the response
 * headers are sent with proper cache control entries like:
 * Cache-Control: max-age=31536000, public
 * This allows browsers to cache static resources and minimize traffic which returns 304 (not modified). 
 * If the tests fails make sure that http.conf from apache has the following entry
 * 
 * <FilesMatch "\.(ico|pdf|flv|jpg|jpeg|png|gif|js|css|swf)$"> 
 *	Header set Cache-Control "max-age=31536000, public" 
 * </FilesMatch>
 * 
 * <P>
 * Initial Date: 17.01.2011 <br>
 * 
 * @author guido
 */
public class TestRenderStaticURLCacheHeaders {

	private String[] uris = {"https://olatng.uzh.ch/", "https://olat.uzh.ch/"};
	
	@Test
	public void cacheHeaders() {
		for (int i = 0; i < uris.length; i++) {
			try {
				loadFromUrl(uris[i]);
			} catch (Exception e) {
				fail(uris[i]+" from server failed with the following message: "+e.getMessage());
			}
		}
	}
	

	public void loadFromUrl(String uri) throws IOException, MalformedURLException, ParseException {
		URL url;
		HttpURLConnection uc = null;
		int responseCode = 0;
		try {
			url = new URL(uri + "olat/dmz/");
			uc = (HttpURLConnection) url.openConnection();
			responseCode = uc.getResponseCode();
		} catch (Exception e) {
			//skip if connection cannot be made but check every few month that url's are correct
			SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy");
			Date when = format.parse("27.03.11"); 
			//set date in future to pass test if urls are not correct anymore
			//but if time has passed we want the test to fail to adjust the url's
			if (new Date().before(when)) {
				assumeNoException(e);
			}
		}

		assertEquals(200, responseCode);
		assertEquals(true, uc.getContentType().startsWith("text/html"));
		BufferedReader in = new BufferedReader(new InputStreamReader(uc
				.getInputStream()));
		String inputLine;
		// grab the first url that points to an static resource to check headers
		while ((inputLine = in.readLine()) != null) {
			if (inputLine.contains("src=\"/olat/raw/")) {
				//System.out.println("Found line: " + inputLine);
				break;
			}
		}
		in.close();

		String jsUrl = inputLine.substring(inputLine.indexOf("src=\"") + 6,
				inputLine.lastIndexOf("\""));
		assertTrue(jsUrl.startsWith("olat/"));
		url = new URL(uri + jsUrl);
		uc = (HttpURLConnection) url.openConnection();
		assertEquals(200, uc.getResponseCode());
		// the cached elements in olat e.g. js, css ... should have an cache
		// lifetime of one year
		assertEquals("max-age=31536000, public", uc.getHeaderField("Cache-Control"));

	}

}
