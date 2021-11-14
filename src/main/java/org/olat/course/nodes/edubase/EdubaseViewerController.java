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
package org.olat.course.nodes.edubase;

import java.util.Map;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.ui.PostDataMapper;
import org.olat.modules.edubase.BookSection;
import org.olat.modules.edubase.EdubaseManager;
import org.olat.modules.edubase.EdubaseModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 17.07.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseViewerController extends BasicController {

	private Component runContainer;
	private Link backLink;

	private BookSection bookSection;

	@Autowired
	private EdubaseModule edubaseModule;
	@Autowired
	private EdubaseManager edubaseManager;
	@Autowired
	private LTIManager ltiManager;

	public EdubaseViewerController(UserRequest ureq, WindowControl wControl, BookSection bookSection) {
		super(ureq, wControl);
		this.bookSection = bookSection;

		runContainer = createRunComponent(ureq);
		putInitialPanel(runContainer);
	}

	private Component createRunComponent(UserRequest ureq) {
		VelocityContainer container = createVelocityContainer("viewer");

		backLink = LinkFactory.createLinkBack(container, this);

		JSAndCSSComponent js = new JSAndCSSComponent("js", new String[] { "js/iframeResizer/iframeResizer.min.js" },
				null);
		container.put("js", js);

		String mapperUri = createMapperUri(ureq);
		container.contextPut("mapperUri", mapperUri);

		return container;
	}

	private String createMapperUri(UserRequest ureq) {
		String launchUrl = edubaseManager.getLtiLaunchUrl(bookSection);
		String baseUrl = edubaseModule.getLtiBaseUrl();
		String oauthConsumerKey = edubaseModule.getOauthKey();
		String oauthSecret = edubaseModule.getOauthSecret();

		LTIContext context = new EdubaseContext(ureq.getUserSession().getIdentityEnvironment(), bookSection.getPageTo());
		Map<String, String> unsignedProps = ltiManager.forgeLTIProperties(getIdentity(), getLocale(), context, true,
				true, false);

		Mapper contentMapper = new PostDataMapper(unsignedProps, launchUrl, baseUrl, oauthConsumerKey, oauthSecret, false);
		String mapperUri = registerMapper(ureq, contentMapper);
		mapperUri = mapperUri + "/";
		return mapperUri;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}
}
