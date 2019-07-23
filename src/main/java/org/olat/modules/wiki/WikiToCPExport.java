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

package org.olat.modules.wiki;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Formatter;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.ims.cp.CPCore;
import org.olat.modules.wiki.gui.components.wikiToHtml.StaticExportWikiDataHandler;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * To generate an snapshot of the current wiki users can export it in an
 * offlinereadeble html export. The export contains also an IMS content package
 * manifest and therefore can be reimported as an static html content which is
 * no longer editable back into OLAT or any other IMS CP capable LMS.
 * <P>
 * Initial Date: Aug 7, 2006 <br>
 * 
 * @author guido
 */
public class WikiToCPExport {

	public static final String WIKI_MANIFEST_IDENTIFIER = "wiki_cp_export_v1";
	private final OLATResourceable ores;
	private final Translator trans;
	private final JFlexParser parser;
	
	/**
	 * 
	 * @param ores   the repositoryEntry of the wiki OR the businessGroup, if the wiki is in a group
	 * @param ident
	 * @param trans
	 */
	public WikiToCPExport(OLATResourceable ores, Translator trans) {
		this.ores = ores;
		this.trans = trans;

		StaticExportWikiDataHandler datahandler = new StaticExportWikiDataHandler();
		datahandler.setWiki(ores);

		ParserInput parserInput = new ParserInput();
		parserInput.setWikiUser(null);
		parserInput.setAllowSectionEdit(false);
		parserInput.setDepth(10);
		parserInput.setContext("");
		// input.setTableOfContents(null);
		parserInput.setLocale(new Locale("en"));
		parserInput.setVirtualWiki("");
		parserInput.setTopicName("dummy");
		parserInput.setUserIpAddress("0.0.0.0");
		parserInput.setDataHandler(datahandler);

		parser = new JFlexParser(parserInput);
	}

	protected String createJsMappingContent(Wiki wiki) {
		StringBuilder sb = new StringBuilder();
		List<WikiPage> pages = wiki.getPagesByDate();

		// create javascript assoz. array
		sb.append("var mappings = new Array();\n");
		for (final Iterator<WikiPage> iter = pages.iterator(); iter.hasNext();) {
			final WikiPage page = iter.next();
			sb.append("mappings[\"").append(page.getPageName().replace("&", "%26").toLowerCase(Locale.ENGLISH)).append("\"] = ");
			sb.append("\"").append(page.getPageId()).append(".html\"\n");
		}

		// create function
		sb.append("function mapLinks() {");
		sb.append("var anchors = document.getElementsByTagName(\"a\");");
		sb.append("for (var i=0; i<anchors.length; i++) {");
		sb.append("var anchor = anchors[i];");
		sb.append("var href = anchor.getAttribute(\"href\");");
		sb.append("if (href && href.indexOf(\"//Media\") >= 0) { anchor.setAttribute(\"href\", href.substr(8)); }");
		sb.append("else if (href && href.substr(0,2).indexOf(\"//\") != -1) {");
		sb.append("anchor.setAttribute(\"href\", mappings[decodeURI(href.substr(2)).toLowerCase()]);");
		sb.append("}");
		sb.append("}");
		sb.append("}");
		sb.append("window.onload = mapLinks;");

		return sb.toString();
	}

	protected String createIMSManifest(Wiki wiki, Identity ident) {
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		sb.append("<manifest xmlns=\"http://www.imsglobal.org/xsd/imscp_v1p1\" " + "xmlns:imsmd=\"http://www.imsglobal.org/xsd/imsmd_v1p2\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + "identifier=\"");
		sb.append(WIKI_MANIFEST_IDENTIFIER);
		sb.append("\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imscp_v1p1 imscp_v1p1.xsd "
				+ "http://www.imsglobal.org/xsd/imsmd_v1p2 imsmd_v1p2p2.xsd\">");

		sb.append("<metadata><imsmd:lom><imsmd:general>");
		sb.append("<imsmd:identifier></imsmd:identifier>");
		sb.append("<imsmd:title><imsmd:langstring xml:lang=\"de\">");
		sb.append("");
		sb.append("</imsmd:langstring></imsmd:title>");
		sb.append("<imsmd:language>de</imsmd:language>");
		sb.append("<imsmd:description><imsmd:langstring xml:lang=\"de\">");
		sb.append("Exported wiki from the OLAT Learning Management System");
		sb.append("</imsmd:langstring></imsmd:description>");
		sb.append("<imsmd:keyword><imsmd:langstring xml:lang=\"de\">OLAT Wiki export</imsmd:langstring></imsmd:keyword>");
		sb.append("</imsmd:general>");

		sb.append("<imsmd:lifecycle>");
		sb.append("<imsmd:version><imsmd:langstring xml:lang=\"de\">1.0</imsmd:langstring></imsmd:version>");
		sb.append("<imsmd:status>");
		sb.append("<imsmd:source><imsmd:langstring xml:lang=\"x-none\">LOMv1.0</imsmd:langstring></imsmd:source>");
		sb.append("<imsmd:value><imsmd:langstring xml:lang=\"x-none\">Draft</imsmd:langstring></imsmd:value>");
		sb.append("</imsmd:status>");
		sb.append("<imsmd:contribute>");
		sb.append("<imsmd:role>");
		sb.append("<imsmd:source><imsmd:langstring xml:lang=\"x-none\">LOMv1.0</imsmd:langstring></imsmd:source>");
		sb.append("<imsmd:value><imsmd:langstring xml:lang=\"x-none\">Author</imsmd:langstring></imsmd:value>");
		sb.append("</imsmd:role>");
		sb.append("<imsmd:centity><imsmd:vcard>");
		sb.append(ident.getUser().getProperty(UserConstants.FIRSTNAME, trans.getLocale())).append(" ")
				.append(ident.getUser().getProperty(UserConstants.LASTNAME, trans.getLocale()));
		sb.append("</imsmd:vcard></imsmd:centity>");
		sb.append("<imsmd:date><imsmd:datetime>");
		sb.append(Formatter.formatDatetime(new Date(System.currentTimeMillis())));
		sb.append("</imsmd:datetime></imsmd:date>");
		sb.append("</imsmd:contribute>");
		sb.append("</imsmd:lifecycle>");
		sb.append("<imsmd:technical><imsmd:format>text/html</imsmd:format></imsmd:technical>");
		sb.append("</imsmd:lom></metadata>");

		sb.append("<organizations default=\"");
		sb.append(CPCore.OLAT_ORGANIZATION_IDENTIFIER);
		sb.append("\">");
		sb.append("<organization identifier=\"");
		sb.append(CPCore.OLAT_ORGANIZATION_IDENTIFIER);
		sb.append("\" structure=\"hierarchical\">");
		sb.append("<title>");
		String name = "";
		if (WikiManager.getInstance().isGroupContextWiki(ores)) {
			BusinessGroup group = CoreSpringFactory.getImpl(BusinessGroupService.class).loadBusinessGroup(ores.getResourceableId());
			name = group.getName();
			sb.append(trans.translate("wiki.exported.from.group", new String[] { name }));
		} else {
			if(ores instanceof RepositoryEntry){
				name = ((RepositoryEntry)ores).getDisplayname();
			}else{
				RepositoryEntry entry = RepositoryManager.getInstance().lookupRepositoryEntry(ores, true);
				name = entry.getDisplayname();
			}
			sb.append(trans.translate("wiki.exported.from.repository", new String[] { name }));
		}
		sb.append("</title>");

		createItems(sb, wiki);
		sb.append("</organization></organizations>");

		sb.append("<resources>");
		createResources(sb, wiki);
		sb.append("</resources>");

		sb.append("</manifest>"); // close manifest
		return sb.toString();
	}

	private void createItems(StringBuilder sb, Wiki wiki) {
		// <item identifier="ITEM-Einleitung" identifierref="Resource-Einleitung"
		// isvisible="true">
		// <title>Einleitung</title>
		// </item>
		List<WikiPage> topLevelPages = new ArrayList<>();
		topLevelPages.add(wiki.getPage(WikiPage.WIKI_INDEX_PAGE));
		WikiPage pageAtoZ = wiki.getPage(WikiPage.WIKI_A2Z_PAGE);
		pageAtoZ.setContent(wiki.getAllPageNamesSorted());
		topLevelPages.add(pageAtoZ);
		topLevelPages.add(wiki.getPage(WikiPage.WIKI_MENU_PAGE));

		for (WikiPage page : topLevelPages) {
			sb.append(openingItemTag(page));
			sb.append(titleTag(page));
			// If the page is the a-z page, append all pages as subitems
			if (page.getPageName().equals(WikiPage.WIKI_A2Z_PAGE)) {
				List<WikiPage> allPages = wiki.getAllPagesWithContent();
				Collections.sort(allPages, new WikiPageComparator());
				// remove all toplevel pages, otherwise we would get duplicate
				// identifiers
				allPages.removeAll(topLevelPages);
				for (WikiPage subPage : allPages) {
					sb.append(openingItemTag(subPage));
					sb.append(titleTag(subPage));
					sb.append(closingItemTag());
					// }
				}
			}
			sb.append(closingItemTag());
		}
	}

	/**
	 * @param page
	 * @return opening item tag
	 */
	private StringBuffer openingItemTag(WikiPage page) {
		StringBuffer sb = new StringBuffer();
		sb.append("<item identifier=\"").append(page.getPageId()).append("\" ").append("identifierref=\"res_");
		sb.append(page.getPageId()).append("\" isvisible=\"true\">");
		return sb;
	}

	/**
	 * @return closing item tag
	 */
	private String closingItemTag() {
		return "</item>";
	}

	/**
	 * @param page
	 * @return entire title tag
	 */
	private StringBuffer titleTag(WikiPage page) {
		StringBuffer sb = new StringBuffer();
		sb.append("<title>");
		if (page.getPageName().equals(WikiPage.WIKI_A2Z_PAGE)) sb.append(trans.translate("navigation.a-z"));
		else if (page.getPageName().equals(WikiPage.WIKI_MENU_PAGE)) sb.append(trans.translate("navigation.menu"));
		else if (page.getPageName().equals(WikiPage.WIKI_INDEX_PAGE)) sb.append(trans.translate("navigation.mainpage"));
		else sb.append("<![CDATA[" + page.getPageName() + "]]>");
		sb.append("</title>");
		return sb;
	}

	private void createResources(StringBuilder sb, Wiki wiki) {
		// <resource identifier="Resource-Einleitung" type="text/html"
		// href="einleitung.html">
		// <file href="einleitung.html" />
		// </resource>
		List<WikiPage> pageNames = wiki.getPagesByDate();
		for (WikiPage page :pageNames) {
			sb.append("<resource identifier=\"res_").append(page.getPageId()).append("\" type=\"text/html\" ").append("href=\"");
			sb.append(page.getPageId()).append(".html\">");
			sb.append("<file href=\"").append(page.getPageId()).append(".html\" />");
			sb.append("</resource>");
		}
	}
	
	protected String wikiPageToHtml(WikiPage page) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		sb.append("<head>\n");
		sb.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\">\n");
		sb.append("<style type=\"text/css\">img {float:right;padding:10px;}</style>\n");
		//sb.append("<script type=\"text/javascript\" src=\"cp_offline_menu_mat/jsMath/easy/load.js\"></script>\n");
		sb.append("<script type=\"text/javascript\" src=\"cp_offline_menu_mat/wiki.js\"></script>\n");
		sb.append("<script type=\"text/javascript\" src=\"mapping.js\"></script>\n");
		sb.append("<link rel=\"StyleSheet\" href=\"cp_offline_menu_mat/wiki.css\" type=\"text/css\" media=\"screen, print\">\n");

		sb.append("</head>\n");
		sb.append("<body>\n");
		sb.append("<h3>").append(getTranslatedWikiPageName(page)).append("</h3>");
		sb.append("<hr><div id=\"olat-wiki\">");
		try {
			ParserDocument doc = parser.parseHTML(page.getContent());
			sb.append(doc.getContent());
		} catch (Exception e) {
			throw new OLATRuntimeException("error while parsing from wiki to CP. ores:" + ores.getResourceableId(), e);
		}
		sb.append("</div></body></html>");
		return sb.toString();
	}
	
	/**
	 * returns the translated wikiPage-name if given page is special (A-Z or the
	 * wiki Menu). Otherwise returns the title of the given wiki page
	 * 
	 * @param page
	 * @return
	 */
	private String getTranslatedWikiPageName(WikiPage page){
		if (page.getPageName().equals(WikiPage.WIKI_A2Z_PAGE)) {
			return trans.translate("navigation.a-z");
		} else if (page.getPageName().equals(WikiPage.WIKI_MENU_PAGE)) {
			return trans.translate("navigation.menu");
		} else {
			return page.getPageName();
		}
	}
}
