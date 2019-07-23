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
package org.olat.modules.webFeed.ui.podcast;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.nodes.feed.podcast.PodcastNodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.modules.webFeed.ui.FeedUIFactory;

/**
 * UI factory for podcast controllers.
 *
 * <P>
 * Initial Date: Jun 8, 2009 <br>
 *
 * @author gwassmann
 */
public class PodcastUIFactory extends FeedUIFactory {

	private Translator translator;

	public PodcastUIFactory() {
		super();
	}

	private PodcastUIFactory(Locale locale) {
		super();
		setTranslator(locale);
	}

	public static PodcastUIFactory getInstance(Locale locale) {
		return new PodcastUIFactory(locale);
	}

	@Override
	public Translator getTranslator() {
		return translator;
	}

	@Override
	public void setTranslator(Locale locale) {
		final Translator fallbackTans = Util.createPackageTranslator(FeedMainController.class, locale);
		translator = Util.createPackageTranslator(PodcastUIFactory.class, locale, fallbackTans);
	}

	@Override
	public VelocityContainer createInfoVelocityContainer(BasicController controller) {
		return new VelocityContainer(VC_INFO_NAME, this.getClass(), VC_INFO_NAME, translator, controller);
	}

	@Override
	public VelocityContainer createItemsVelocityContainer(BasicController controller) {
		return new VelocityContainer(VC_ITEMS_NAME, this.getClass(), "episodes", translator, controller);
	}

	@Override
	public VelocityContainer createItemVelocityContainer(BasicController controller) {
		return new VelocityContainer(VC_ITEM_NAME, this.getClass(), "episode", translator, controller);
	}

	@Override
	public VelocityContainer createRightColumnVelocityContainer(BasicController controller) {
		return new VelocityContainer(VC_RIGHT_NAME, this.getClass(), VC_RIGHT_NAME, translator, controller);
	}

	@Override
	public FormBasicController createItemFormController(UserRequest ureq, WindowControl wControl, Item item) {
		return new EpisodeFormController(ureq, wControl, item, getTranslator());
	}

	@Override
	public TabbableController createNodeEditController(AbstractFeedCourseNode courseNode, ICourse course, UserCourseEnvironment uce,
			UserRequest ureq, WindowControl control) {
		return new PodcastNodeEditController(courseNode, course, uce, ureq, control);
	}

}
