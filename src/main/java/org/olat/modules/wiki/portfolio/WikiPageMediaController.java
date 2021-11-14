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
package org.olat.modules.wiki.portfolio;

import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.jamwiki.DefaultDataHandler;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.MediaRenderingHints;
import org.olat.modules.portfolio.ui.MediaMetadataController;
import org.olat.modules.portfolio.ui.PortfolioHomeController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class WikiPageMediaController extends BasicController {
	
	private static final Logger log = Tracing.createLoggerFor(WikiPageMediaController.class);

	@Autowired
	private UserManager userManager;

	public WikiPageMediaController(UserRequest ureq, WindowControl wControl, Media media, MediaRenderingHints hints) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(PortfolioHomeController.class, getLocale(), getTranslator()));
		
		VelocityContainer mainVC = createVelocityContainer("details");
		String wikiText = getContent(media.getContent());
		mainVC.contextPut("text", wikiText);

		String desc = media.getDescription();
		mainVC.contextPut("description", StringHelper.containsNonWhitespace(desc) ? desc : null);
		String title = media.getTitle();
		mainVC.contextPut("title", StringHelper.containsNonWhitespace(title) ? title : null);

		mainVC.contextPut("creationdate", media.getCreationDate());
		mainVC.contextPut("author", userManager.getUserDisplayName(media.getAuthor()));

		if(hints.isExtendedMetadata()) {
			MediaMetadataController metaCtrl = new MediaMetadataController(ureq, wControl, media);
			listenTo(metaCtrl);
			mainVC.put("meta", metaCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private static String getContent(String content) {
		try {
			ParserInput input = new ParserInput();
			input.setWikiUser(null);
			input.setAllowSectionEdit(false);
			input.setDepth(2);
			input.setContext("");
			input.setLocale(Locale.ENGLISH);
			input.setTopicName("dummy");
			input.setUserIpAddress("0.0.0.0");
			input.setDataHandler(new DefaultDataHandler());
			input.setVirtualWiki("/olat");

			AbstractParser parser = new JFlexParser(input);
			ParserDocument parsedDoc = parser.parseHTML(content);
			String parsedContent = parsedDoc.getContent();
			return FilterFactory.getHtmlTagAndDescapingFilter().filter(parsedContent);
		} catch(Exception e) {
			log.error("", e);
			return content;
		}
	}
}

