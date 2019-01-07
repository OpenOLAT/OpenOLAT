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
package org.olat.modules.edusharing.manager;

import org.olat.core.gui.UserRequest;
import org.olat.core.util.StringHelper;
import org.olat.modules.edusharing.model.SearchResult;

/**
 * 
 * Initial date: 3 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class SearchResultConverter {
	
	private SearchResultConverter() {
		//
	}
	
	static SearchResult fromUserRequest(UserRequest ureq) {
		SearchResult result = new SearchResult();
		
		String nodeId = ureq.getParameter("nodeId");
		if (StringHelper.containsNonWhitespace(nodeId)) {
			result.setObjectUrl(nodeId);
		}
		
		String title = ureq.getParameter("title");
		if (StringHelper.containsNonWhitespace(title)) {
			result.setTitle(title);
		}
		
		String mimeType = ureq.getParameter("mimeType");
		if (StringHelper.containsNonWhitespace(mimeType)) {
			result.setMimeType(mimeType);
		}
		
		String resourceType = ureq.getParameter("resourceType");
		if (StringHelper.containsNonWhitespace(resourceType)) {
			result.setResourceType(resourceType);
		}
		
		String resourceVersion = ureq.getParameter("resourceVersion");
		if (StringHelper.containsNonWhitespace(resourceVersion)) {
			result.setResourceVersion(resourceVersion);
		}
		
		String h = ureq.getParameter("h");
		result.setWindowHight(toInteger(h));
		
		String w = ureq.getParameter("w");
		result.setWindowWidth(toInteger(w));
		
		if (result.getWindowHight() != null && result.getWindowWidth() != null) {
			double ratio = (double)result.getWindowHight().intValue() / result.getWindowWidth().intValue();
			result.setRatio(ratio);
		}
		
		String v = ureq.getParameter("v");
		if (StringHelper.containsNonWhitespace(v)) {
			result.setWindowVersion(v);
		}
		
		String repoType = ureq.getParameter("repoType");
		if (StringHelper.containsNonWhitespace(repoType)) {
			result.setRepoType(repoType);;
		}
		
		String mediatype = ureq.getParameter("mediatype");
		if (StringHelper.containsNonWhitespace(mediatype)) {
			result.setMediaType(mediatype);
		}
		
		return result;
	}
	
	private static Integer toInteger(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Integer.valueOf(value);
			} catch (NumberFormatException e) {
				// 
			}
		}
		return null;
	}
}
