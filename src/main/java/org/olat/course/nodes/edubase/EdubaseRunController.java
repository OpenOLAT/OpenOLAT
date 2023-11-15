/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.edubase;

import java.util.List;
import java.util.Map;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.course.nodes.EdubaseCourseNode;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.ui.PostDataMapper;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.edubase.BookSection;
import org.olat.modules.edubase.EdubaseLoggingAction;
import org.olat.modules.edubase.EdubaseManager;
import org.olat.modules.edubase.EdubaseModule;
import org.olat.modules.edubase.model.PositionComparator;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 21.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class EdubaseRunController extends BasicController {

	private final static String EVENT_RUN = "run";

	private static final String OVERVIEW_DESCRIPTION_ENABLED = "overview_description_enabled";
	private static final String OVERVIEW_DESCRIPTION_DISABLED = "overview_description_disabled";

	private Panel mainPanel;
	private Component overviewContainer;
	private final ModuleConfiguration moduleConfiguration;
	private Controller viewerController;
	private List<BookSection> bookSections;
	
	@Autowired
	private EdubaseManager edubaseManager;
	@Autowired
	private EdubaseModule edubaseModule;
	@Autowired
	private LTIManager ltiManager;

	public EdubaseRunController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl);

		this.moduleConfiguration = moduleConfiguration;

		overviewContainer = createOverviewComponent();
		mainPanel = new Panel("edubasePanel");
		mainPanel.setContent(overviewContainer);
		putInitialPanel(mainPanel);
	}

	private Component createOverviewComponent() {
		VelocityContainer container;

		if (moduleConfiguration.getBooleanSafe(EdubaseCourseNode.CONFIG_DESCRIPTION_ENABLED)) {
			container = createVelocityContainer(OVERVIEW_DESCRIPTION_ENABLED);
		} else {
			container = createVelocityContainer(OVERVIEW_DESCRIPTION_DISABLED);
		}

		bookSections =
				moduleConfiguration.getList(EdubaseCourseNode.CONFIG_BOOK_SECTIONS, BookSection.class).stream()
						.map(bs -> edubaseManager.appendCoverUrl(bs))
						.sorted(new PositionComparator())
						.toList();
		container.contextPut("bookSections", bookSections);

		for (BookSection bookSection : bookSections) {
			Link nodeLink = LinkFactory.createLink("startReader_" + bookSection.getPosition(), container, this);
			nodeLink.setCustomDisplayText(getTranslator().translate("open.document"));
			nodeLink.setIconRightCSS("o_icon o_icon_start");
			nodeLink.setUserObject(bookSection);
			nodeLink.setNewWindow(true, true);
		}

		EdubaseViewHelper edubaseViewHelper = new EdubaseViewHelper(getTranslator());
		container.contextPut("helper", edubaseViewHelper);
		container.contextPut("run", EVENT_RUN);

		return container;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == overviewContainer && EVENT_RUN.equals(event.getCommand().substring(0, EVENT_RUN.length()))) {
			// Start Edubase Reader if book cover is clicked
			int bookSectionIndex = Integer.parseInt(event.getCommand().substring(EVENT_RUN.length()));
			BookSection bookSection = bookSections.get(bookSectionIndex);
			openViewer(ureq, bookSection);
		} else if (source instanceof Link startReaderLink) {
			// Start Edubase Reader if description disabled
			BookSection bookSection = (BookSection) startReaderLink.getUserObject();
			openViewer(ureq, bookSection);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == viewerController && event == Event.BACK_EVENT) {
			doBack();
		}
	}

	private void openViewer(UserRequest ureq, BookSection bookSection) {
		getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(createMapperUri(ureq, bookSection)));
		ThreadLocalUserActivityLogger.log(EdubaseLoggingAction.BOOK_SECTION_LAUNCHED, getClass(), LoggingResourceable.wrap(bookSection));
	}

	private String createMapperUri(UserRequest ureq, BookSection bookSection) {
		String launchUrl = edubaseManager.getLtiLaunchUrl(bookSection);
		String baseUrl = edubaseModule.getLtiBaseUrl();
		String oauthConsumerKey = edubaseModule.getOauthKey();
		String oauthSecret = edubaseModule.getOauthSecret();

		LTIContext context = new EdubaseContext(ureq.getUserSession().getIdentityEnvironment(),
				bookSection.getPageTo(), edubaseModule.isMultiPakEnabled(), moduleConfiguration);
		Map<String, String> unsignedProps = ltiManager.forgeLTIProperties(getIdentity(), getLocale(), context, true,
				true, false);

		Mapper contentMapper = new PostDataMapper(unsignedProps, launchUrl, baseUrl, oauthConsumerKey, oauthSecret, false);
		String mapperUri = registerMapper(ureq, contentMapper);
		mapperUri = mapperUri + "/";
		return mapperUri;
	}

	private void doBack() {
		mainPanel.setContent(overviewContainer);
		removeAsListenerAndDispose(viewerController);
		viewerController = null;
	}
}
