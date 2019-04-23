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
package org.olat.modules.adobeconnect.ui;

import org.olat.core.gui.translator.Translator;
import org.olat.modules.adobeconnect.model.AdobeConnectError;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;

/**
 * 
 * Initial date: 23 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectErrorHelper {
	
	public static final String formatErrors(Translator translator, AdobeConnectErrors errors) {
		StringBuilder sb = new StringBuilder(1024);
		sb.append(translator.translate("error.prefix"))
		  .append("<ul>");
		for(AdobeConnectError error:errors.getErrors()) {
			sb.append("<li>")
			  .append(translator.translate("error." + error.getCode().name(), error.getArguments()))
			  .append("</li>");
		}
		return sb.append("</ul>").toString();
	}
}