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
package org.olat.ims.qti21.restapi;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.ims.qti21.QTI21Service;

import uk.ac.ed.ph.jqtiplus.xmlutils.xslt.XsltStylesheetCache;
import uk.ac.ed.ph.qtiworks.mathassess.XsltStylesheetCacheAdapter;
import uk.ac.ed.ph.qtiworks.mathassess.glue.AsciiMathHelper;

/**
 * 
 * Initial date: 11.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MathWebDispatcher implements Dispatcher {
	
	private static final Logger log = Tracing.createLoggerFor(MathWebDispatcher.class);

	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response) {
		UserRequest ureq = null;
		try{
			//upon creation URL is checked for 
			ureq = new UserRequestImpl("math", request, response);
		} catch(NumberFormatException nfe) {
			DispatcherModule.sendBadRequest(request.getPathInfo(), response);
			return;
		}

		String asciiMathInput =  ureq.getParameter("input");
		Map<String, String> upConvertedAsciiMathInput;
		if(StringHelper.containsNonWhitespace(asciiMathInput)) {
			XsltStylesheetCache stylesheetCache = CoreSpringFactory.getImpl(QTI21Service.class).getXsltStylesheetCache();
			AsciiMathHelper asciiMathHelper = new AsciiMathHelper(new XsltStylesheetCacheAdapter(stylesheetCache));
			upConvertedAsciiMathInput = asciiMathHelper.upConvertAsciiMathInput(asciiMathInput);
		} else {
			upConvertedAsciiMathInput = Collections.emptyMap();
		}
		
		try {
			JSONObject object = new JSONObject();
			for(Map.Entry<String, String> entry:upConvertedAsciiMathInput.entrySet()) {
				object.append(entry.getKey(), entry.getValue());
			}
			object.write(response.getWriter());
		} catch (JSONException | IOException e) {
			log.error("", e);
		}
	}
}
