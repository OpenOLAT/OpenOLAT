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
package org.olat.core.gui.components.form.flexible.impl.elements.richText.plugins.olatmovieviewer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;

/**
 * 
 * This is a proxy to get some information from nanoo.tv about the size of the video.
 * 
 * Initial date: 28 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TinyDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(TinyDispatcher.class);

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse httpResponse) {
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for 
			ureq = new UserRequestImpl("tiny", request, httpResponse);
		} catch(NumberFormatException nfe) {
			DispatcherModule.sendBadRequest(request.getPathInfo(), httpResponse);
			return;
		}

		String nanooId = ureq.getParameter("nanooId");
		if(ureq.getUserSession().isAuthenticated() && StringHelper.containsNonWhitespace(nanooId) && nanooId.length() > 5) {
			HttpClientService httpClientService = CoreSpringFactory.getImpl(HttpClientService.class);
			try(CloseableHttpClient httpClient = httpClientService.createHttpClient()) {
				String url = "https://www.nanoo.tv/services/oembed?url=https%3A//nanoo.tv/link/v/" + nanooId + "&format=json";
				HttpGet get = new HttpGet(url);
				get.addHeader("Accept", "application/json");
				get.addHeader("Content-type", "application/json");

				HttpResponse response = httpClient.execute(get);
				int status = response.getStatusLine().getStatusCode();
				if(status == 200) {//created
					String content = EntityUtils.toString(response.getEntity());
					JSONObject jsonPayload = new JSONObject(content);
					JSONObject object = new JSONObject();
					object.put("width", jsonPayload.optInt("width"));
					object.put("height", jsonPayload.optInt("height"));
					object.write(httpResponse.getWriter());
				} else {
					httpResponse.setStatus(status);
				}
			} catch(Exception e) {
				log.error("", e);
			}
		}
	}
}
