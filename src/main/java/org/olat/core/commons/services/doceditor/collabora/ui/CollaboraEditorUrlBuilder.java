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
package org.olat.core.commons.services.doceditor.collabora.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.doceditor.collabora.CollaboraService;
import org.olat.core.commons.services.doceditor.wopi.Access;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class CollaboraEditorUrlBuilder {
	
	// Source: https://github.com/LibreOffice/online/blob/master/loleaflet/src/main.js
	
	private final Access access;
	private String lang;
	private String permission;
	private boolean closeButton = false;
	private boolean debug = false;

	@Autowired
	private CollaboraService collaboraService;

	static CollaboraEditorUrlBuilder builder(Access access) {
		return new CollaboraEditorUrlBuilder(access);
	}
	
	private CollaboraEditorUrlBuilder(Access access) {
		this.access = access;
		CoreSpringFactory.autowireObject(this);
	}
	
	CollaboraEditorUrlBuilder withLang(String lang) {
		this.lang = lang;
		return this;
	}
	
	CollaboraEditorUrlBuilder withPermission(String permission) {
		this.permission = permission;
		return this;
	}
	
	CollaboraEditorUrlBuilder withCloseButton(boolean closeButton) {
		this.closeButton = closeButton;
		return this;
	}
	
	CollaboraEditorUrlBuilder withDebug(boolean debug) {
		this.debug = debug;
		return this;
	}
	
	String build() {
		StringBuilder url = new StringBuilder();
		String editorBaseUrl = collaboraService.getEditorBaseUrl(access.getMetadata());
		url.append(editorBaseUrl);
		
		StringBuilder wopiPath = new StringBuilder();
		wopiPath.append(Settings.getServerContextPathURI());
		wopiPath.append(RestSecurityHelper.SUB_CONTEXT);
		wopiPath.append("/collabora/wopi/files/");
		wopiPath.append(access.getMetadata().getUuid());
		url.append("WOPISrc=");
		url.append(StringHelper.urlEncodeUTF8(wopiPath.toString()));
		
		url.append("&access_token=").append(access.getToken());
		
		if (StringHelper.containsNonWhitespace(lang)) {
			url.append("&lang=").append(lang);
		}
		
		if (StringHelper.containsNonWhitespace(permission)) {
			url.append("&permission=").append(permission);
		}
		
		if (closeButton) {
			// Apparently that parameter is ignored. The close button is always visible!
			url.append("&closebutton=1");
		}
		
		if (debug) {
			url.append("&debug=1");
		}
		
		return url.toString();
	}

}
