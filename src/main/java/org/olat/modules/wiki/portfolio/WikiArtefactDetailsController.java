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

import org.jamwiki.DataHandler;
import org.jamwiki.model.Topic;
import org.jamwiki.model.WikiFile;
import org.jamwiki.parser.AbstractParser;
import org.jamwiki.parser.ParserDocument;
import org.jamwiki.parser.ParserInput;
import org.jamwiki.parser.jflex.JFlexParser;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * 
 * Description:<br>
 * Show the specific part of the WikiArtefact
 * 
 * <P>
 * Initial Date:  11 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class WikiArtefactDetailsController extends BasicController {
	
	private static final Logger log = Tracing.createLoggerFor(WikiArtefactDetailsController.class);

	private final VelocityContainer vC;

	public WikiArtefactDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		super(ureq, wControl);
		WikiArtefact fArtefact = (WikiArtefact)artefact;
		vC = createVelocityContainer("details");
		EPFrontendManager ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");
		String wikiText = getContent(ePFMgr.getArtefactFullTextContent(fArtefact));
		vC.contextPut("text", wikiText);
		putInitialPanel(vC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
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
	    input.setDataHandler(new DummyDataHandler());
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
	
	public static class DummyDataHandler implements DataHandler {

		@Override
		public boolean exists(String virtualWiki, String topic) {
			return true;
		}

		@Override
		public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Object transactionObject) throws Exception {
			return null;
		}

		@Override
		public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
			return null;
		}
	}
}

