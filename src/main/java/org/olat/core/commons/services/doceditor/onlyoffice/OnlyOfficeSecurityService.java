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
package org.olat.core.commons.services.doceditor.onlyoffice;

import org.olat.core.commons.services.doceditor.onlyoffice.model.DocumentImpl;
import org.olat.core.commons.services.doceditor.onlyoffice.model.EditorConfigImpl;

/**
 * 
 * Initial date: 19 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface OnlyOfficeSecurityService {

	boolean isValidSecret(String secret);

	/**
	 * Converts the JWT token of a callback request to Callback object.
	 * 
	 * @see <a href="https://api.onlyoffice.com/editors/signature/request">https://api.onlyoffice.com/editors/signature/request</a>
	 *
	 * @param jwtToken
	 * @return
	 */
	Callback getCallback(String jwtToken);

	/**
	 * Creates the JWT token to open a document in the ONLYOFFICE editor.
	 * 
	 * @see <a href="https://api.onlyoffice.com/editors/signature/browser">https://api.onlyoffice.com/editors/signature/browser</a>
	 *
	 * @param document
	 * @param editorConfig
	 * @return jwtToken
	 */
	String getApiConfigToken(DocumentImpl document, EditorConfigImpl editorConfig);
	
	/**
	 * Creates the JWT token to use for download a file.
	 * 
	 * @see <a href="https://api.onlyoffice.com/editors/signature/request">https://api.onlyoffice.com/editors/signature/request</a>
	 *
	 * @return jwtToken
	 */
	String getFileDonwloadToken();


}
