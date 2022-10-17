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

package org.olat.restapi.system;

import jakarta.ws.rs.Path;

import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Description:<br>
 * This serves information about the document editors.
 * 
 * Initial date: 09 Jul. 2020<br>
 * @author morjen, moritz.jenny@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Document Editor")
@Component
public class DocEditorWebService {
	
	private static final DocEditorSessionWebService docEditorSessionWebService = new DocEditorSessionWebService();

	
	@Path("sessions")
	public DocEditorSessionWebService getStatus() {
		return docEditorSessionWebService;
	}
	
	
}
