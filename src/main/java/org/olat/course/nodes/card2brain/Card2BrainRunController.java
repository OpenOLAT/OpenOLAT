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
package org.olat.course.nodes.card2brain;

import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.nodes.Card2BrainCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.lti.LTIContext;
import org.olat.ims.lti.LTIManager;
import org.olat.ims.lti.ui.PostDataMapper;
import org.olat.ims.lti.ui.TalkBackMapper;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.card2brain.Card2BrainModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.04.2017<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Card2BrainRunController extends BasicController {

	private Panel main;

	private CourseNode courseNode;
	private final ModuleConfiguration config;
	private final CourseEnvironment courseEnv;

	private final LTIManager ltiManager;

	@Autowired
	private Card2BrainModule card2BrainModule;

	public Card2BrainRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			Card2BrainCourseNode card2BrainCourseNode) {
		super(ureq, wControl);

		this.courseNode = card2BrainCourseNode;
		this.config = card2BrainCourseNode.getModuleConfiguration();
		this.courseEnv = userCourseEnv.getCourseEnvironment();
		ltiManager = CoreSpringFactory.getImpl(LTIManager.class);

		main = new Panel("card2brainPanel");
		runCard2Brain(ureq);
		putInitialPanel(main);
	}

	private void runCard2Brain(UserRequest ureq) {
		VelocityContainer container = createVelocityContainer("run");
		
		String url = String.format(card2BrainModule.getBaseUrl(),
				config.getStringValue(Card2BrainCourseNode.CONFIG_FLASHCARD_ALIAS));
		
		String oauth_consumer_key;
		String oauth_secret;
		if (config.getBooleanSafe(Card2BrainCourseNode.CONFIG_ENABLE_PRIVATE_LOGIN)) {
			oauth_consumer_key = (String) config.get(Card2BrainCourseNode.CONFIG_PRIVATE_KEY);
			oauth_secret = (String) config.get(Card2BrainCourseNode.CONFIG_PRIVATE_SECRET);
		} else {
			oauth_consumer_key = card2BrainModule.getEnterpriseKey();
			oauth_secret = card2BrainModule.getEnterpriseSecret();
		}

		String serverUri = Settings.createServerURI();
		String sourcedId = courseEnv.getCourseResourceableId() + "_" + courseNode.getIdent() + "_"
				+ getIdentity().getKey();
		container.contextPut("sourcedId", sourcedId);

		Mapper talkbackMapper = new TalkBackMapper(getLocale(),
				getWindowControl().getWindowBackOffice().getWindow().getGuiTheme().getBaseURI());
		String backMapperUrl = registerCacheableMapper(ureq, sourcedId + "_talkback", talkbackMapper);
		String backMapperUri = serverUri + backMapperUrl + "/";
		String outcomeMapperUri = null;

		LTIContext context = new Card2BrainContext(courseEnv, courseNode, sourcedId, backMapperUri, outcomeMapperUri);

		Map<String, String> unsignedProps = ltiManager.forgeLTIProperties(getIdentity(), getLocale(), context, true,
				true);

		Mapper contentMapper = new PostDataMapper(unsignedProps, url, oauth_consumer_key, oauth_secret, false);

		String mapperUri = registerMapper(ureq, contentMapper);
		container.contextPut("mapperUri", mapperUri + "/");
		main.setContent(container);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		//
	}

}
