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

import java.util.Date;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.jamwiki.DataHandler;
import org.jamwiki.DefaultDataHandler;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.wiki.WikiPage;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class WikiPageDocument extends OlatDocument {

	private static final long serialVersionUID = -1210392466207248182L;
	private static final Logger log = Tracing.createLoggerFor(WikiPageDocument.class);
	private static final DataHandler DUMMY_DATA_HANDLER = new DefaultDataHandler();
	

	public WikiPageDocument() {
		super();
	}

	public static Document createDocument(SearchResourceContext searchResourceContext, WikiPage wikiPage) {		
		WikiPageDocument wikiPageDocument = new WikiPageDocument();

		long userId = wikiPage.getInitalAuthor();
		if (userId != 0) {
			Identity  identity = CoreSpringFactory.getImpl(BaseSecurity.class).loadIdentityByKey(Long.valueOf(userId));
			if(identity != null) {
				wikiPageDocument.setAuthor(identity.getName());
			}
		}
		wikiPageDocument.setTitle(wikiPage.getPageName());
		wikiPageDocument.setContent(getContent(wikiPage));
		wikiPageDocument.setCreatedDate(new Date(wikiPage.getCreationTime()));
		wikiPageDocument.setLastChange(new Date(wikiPage.getModificationTime()));
		wikiPageDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		wikiPageDocument.setDocumentType(searchResourceContext.getDocumentType());
		wikiPageDocument.setCssIcon("o_wiki_icon");
		wikiPageDocument.setParentContextType(searchResourceContext.getParentContextType());
		wikiPageDocument.setParentContextName(searchResourceContext.getParentContextName());
		
		if (log.isDebugEnabled()) log.debug(wikiPageDocument.toString());
		return wikiPageDocument.getLuceneDocument();
	}
	
	private static String getContent(WikiPage wikiPage) {
		try {
			ParserInput input = new ParserInput();
			input.setWikiUser(null);
			input.setAllowSectionEdit(false);
			input.setDepth(2);
			input.setContext("");
			input.setLocale(Locale.ENGLISH);
			input.setTopicName("dummy");
			input.setUserIpAddress("0.0.0.0");
			input.setDataHandler(DUMMY_DATA_HANDLER);
			input.setVirtualWiki("/olat");

			AbstractParser parser = new JFlexParser(input);
			ParserDocument parsedDoc = parser.parseHTML(wikiPage.getContent());
			String parsedContent = parsedDoc.getContent();
			return FilterFactory.getHtmlTagAndDescapingFilter().filter(parsedContent);
		} catch(Exception e) {
			log.error("", e);
			return wikiPage.getContent();
		}
	}
}
