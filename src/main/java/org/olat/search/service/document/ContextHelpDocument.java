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
*/
package org.olat.search.service.document;

import java.io.StringWriter;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.velocity.context.Context;
import org.olat.core.commons.contextHelp.ContextHelpDispatcher;
import org.olat.core.commons.services.search.OlatDocument;
import org.olat.core.gui.render.velocity.VelocityHelper;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.search.service.SearchResourceContext;

/**
 * Description:<br>
 * The context help document indexes a context sensitive help page
 * 
 * <P>
 * Initial Date:  05.11.2008 <br>
 * @author gnaegi
 */
public class ContextHelpDocument extends OlatDocument {
	private static final OLog log = Tracing.createLoggerFor(ContextHelpDocument.class);
	private static final Pattern HTML_TAG_PATTERN = Pattern.compile("</?[a-zA-Z0-9]+\\b[^>]*>");
  private static final Pattern HTML_SPACE_PATTERN = Pattern.compile("&nbsp;");
	
	// Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.contexthelp";

	/**
	 * Constructor
	 */
	public ContextHelpDocument() {
		super();
	}
	
	/**
	 * Factory method to create a search document for a context sensitive help page
	 * @param searchResourceContext
	 * @param bundleName
	 * @param page
	 * @param pageTranslator
	 * @param ctx
	 * @param pagePath
	 * @return
	 */
	public static Document createDocument(SearchResourceContext searchResourceContext, String bundleName, String page, Translator pageTranslator, Context ctx, String pagePath) {
		ContextHelpDocument contextHelpDocument = new ContextHelpDocument();	
		I18nManager i18nMgr = I18nManager.getInstance();
		
		// Set all know attributes
		searchResourceContext.setFilePath(ContextHelpDispatcher.createContextHelpURI(pageTranslator.getLocale(), bundleName, page));
		contextHelpDocument.setResourceUrl(searchResourceContext.getResourceUrl());//to adhere to the [path=...] convention
		contextHelpDocument.setLastChange(new Date(i18nMgr.getLastModifiedDate(pageTranslator.getLocale(), bundleName)));
		String lang = I18nManager.getInstance().getLanguageTranslated(pageTranslator.getLocale().toString(), I18nModule.isOverlayEnabled());
		contextHelpDocument.setDocumentType(TYPE);	
		contextHelpDocument.setCssIcon("b_contexthelp_icon");
		contextHelpDocument.setTitle(pageTranslator.translate("chelp." + page.split("\\.")[0] + ".title") + " (" + lang + ")");
		
		try {
			VelocityHelper vh = VelocityHelper.getInstance();
			StringWriter wOut = new StringWriter(10000);
			vh.mergeContent(pagePath, ctx, wOut, null);
			String mergedContent = wOut.toString();
			// Remove any HTML stuff from page
			Matcher m = HTML_TAG_PATTERN.matcher(mergedContent);
			mergedContent = m.replaceAll(" ");
			// Remove all &nbsp
			m = HTML_SPACE_PATTERN.matcher(mergedContent);
			mergedContent = m.replaceAll(" ");
			// Finally set content
			contextHelpDocument.setContent(mergedContent);
		} catch (Exception e) {
			log.error("Error indexing context help: " + bundleName + " / " + page + " in " + pageTranslator.getLocale(), e);
			contextHelpDocument.setContent("");
		}
		
		if (log.isDebug()) log.debug(contextHelpDocument.toString());
		return contextHelpDocument.getLuceneDocument();
	}

}