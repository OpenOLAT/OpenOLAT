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
package org.olat.modules.edusharing;

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.UserRequestImpl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.edusharing.model.SearchResult;

/**
 * 
 * Initial date: 20 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingSearchCallbackMapper implements Mapper {

	private static final Logger log = Tracing.createLoggerFor(EdusharingSearchCallbackMapper.class);
	
	private SearchResult searchResult;

	public SearchResult getSearchResult() {
		return searchResult;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		UserRequest ureq = null;
		try{
			ureq = new UserRequestImpl("edusharing", request, null);
			EdusharingConversionService conversionService = CoreSpringFactory.getImpl(EdusharingConversionService.class);
			searchResult = conversionService.toSearchResult(ureq);
		} catch (NumberFormatException nfe) {
			//
		}
		
		StringMediaResource smr = new StringMediaResource();
		
		String encoding = StandardCharsets.ISO_8859_1.name();
		String mimetype = "text/html;charset=" + StringHelper.check4xMacRoman(encoding);
		smr.setContentType(mimetype);
		smr.setEncoding(encoding);
		String content = createContent();
		smr.setData(content);
		return smr;
	}

	private String createContent() {
		StringBuilder sb = new StringBuilder();
		sb.append("<!DOCTYPE html>");
		sb.append("<html>");
		sb.append("<head>");
		sb.append("<meta charset=\"UTF-8\">");
		sb.append("<script>");
		sb.append("  (function() {");
		sb.append("   \"use strict\";");
		sb.append("   parent.closeEsSelection();");
		sb.append("}());");
		sb.append("</script>");
		sb.append("</head>");
		sb.append("<body>");
		sb.append("</body>");
		sb.append("</html>");

		String html = sb.toString();
		log.debug(html);
		return html;
	}

}
