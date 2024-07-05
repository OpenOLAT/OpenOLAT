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
package org.olat.modules.openbadges.ui;

import java.util.List;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.repository.RepositoryEntryRef;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-07-04<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeWidgetController extends BasicController {

	private static final String ISSUED_BADGES = "[IssuedBadges:0]";
	private static final String MY_BADGES = "[MyBadges:0]";

	private static final String[] SWIPER_JS = new String[] { "js/swiper/swiper-bundle.min.js" };

	private final VelocityContainer mainVC;
	private final RepositoryEntryRef courseEntryRef;
	private final List<BadgeAssertion> ruleEarnedBadgeAssertions;
	private final boolean myBadges;

	@Autowired
	private OpenBadgesManager openBadgesManager;

	public BadgeWidgetController(UserRequest ureq, WindowControl wControl, RepositoryEntryRef courseEntryRef,
								 List<BadgeAssertion> ruleEarnedBadgeAssertions, boolean myBadges) {
		super(ureq, wControl);
		this.courseEntryRef = courseEntryRef;
		this.ruleEarnedBadgeAssertions = ruleEarnedBadgeAssertions;
		this.myBadges = myBadges;

		mainVC = createVelocityContainer("badge_widget");
		mainVC.setDomReplacementWrapperRequired(false);
		putInitialPanel(mainVC);
		initUI(ureq);
		updateUI();
	}

	private void initUI(UserRequest ureq) {
		JSAndCSSComponent js = new JSAndCSSComponent("js", SWIPER_JS, null);
		mainVC.put("js", js);
		String mediaUrl = registerMapper(ureq, new BadgeAssertionMediaFileMapper());
		mainVC.contextPut("mediaUrl", mediaUrl);
		String courseBusinessPath = "[RepositoryEntry:" + courseEntryRef.getKey() + "]";
		String badgesUrl = BusinessControlFactory.getInstance()
				.getAuthenticatedURLFromBusinessPathStrings(courseBusinessPath,
						myBadges ? MY_BADGES : ISSUED_BADGES);
		mainVC.contextPut("badgesUrl", badgesUrl);
	}

	private void updateUI() {
		mainVC.contextPut("ruleEarnedBadgeAssertions", ruleEarnedBadgeAssertions);
		mainVC.contextPut("singleMode", ruleEarnedBadgeAssertions.size() <= 1);
		mainVC.contextPut("sliderMode", ruleEarnedBadgeAssertions.size() > 1);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {

	}

	private class BadgeAssertionMediaFileMapper implements Mapper {

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			VFSLeaf vfsLeaf = openBadgesManager.getBadgeAssertionVfsLeaf(relPath);
			if (vfsLeaf == null) {
				return new NotFoundMediaResource();
			}
			return new VFSMediaResource(vfsLeaf);
		}
	}
}
