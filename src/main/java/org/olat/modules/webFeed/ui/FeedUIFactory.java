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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.webFeed.ui;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.course.ICourse;
import org.olat.course.nodes.AbstractFeedCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;

/**
 * Abstract Factory Pattern for the user interface of different feed types.
 * 
 * <P>
 * Initial Date: Jul 30, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedUIFactory {

	public static final String VC_ITEMS_NAME = "items";
	public static final String VC_ITEM_NAME = "item";
	public static final String VC_RIGHT_NAME = "right_column";
	public static final String VC_INFO_NAME = "info";

	public abstract Translator getTranslator();

	public abstract void setTranslator(Locale locale);
	
	public abstract VelocityContainer createInfoVelocityContainer(BasicController controller);

	public abstract VelocityContainer createItemsVelocityContainer(BasicController controller);

	public abstract VelocityContainer createItemVelocityContainer(BasicController controller);

	public abstract VelocityContainer createRightColumnVelocityContainer(BasicController controller);

	/* used for course node */
	public FeedMainController createMainController(OLATResourceable ores, UserRequest ureq, WindowControl wControl, FeedSecurityCallback callback,
			Long courseId, String nodeId) {
		return new FeedMainController(ores, ureq, wControl, courseId, nodeId, this, callback, null);
	}

	public FeedMainController createMainController(OLATResourceable ores, UserRequest ureq, WindowControl wControl, FeedSecurityCallback callback) {
		return new FeedMainController(ores, ureq, wControl, null, null, this, callback, null);
	}
	
	// with specific FeedItemDisplayConfig
	public FeedMainController createMainController(final OLATResourceable ores, final UserRequest ureq, final WindowControl wControl, final FeedSecurityCallback callback, FeedItemDisplayConfig displayConfig) {
		return new FeedMainController(ores, ureq, wControl, null, null, this, callback, displayConfig);
	}
	
	public abstract IAddController createAddController(RepositoryAddCallback addCallback, UserRequest ureq, WindowControl wControl);
	
	public abstract FormBasicController createItemFormController(UserRequest ureq, WindowControl wControl, Item item, Feed feed);

	public abstract TabbableController createNodeEditController(AbstractFeedCourseNode courseNode, ICourse course, UserCourseEnvironment uce, UserRequest ureq,
			WindowControl control);
	
	
}
