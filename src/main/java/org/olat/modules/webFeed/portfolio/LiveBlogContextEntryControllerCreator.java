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

package org.olat.modules.webFeed.portfolio;

import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.ContextEntryControllerCreator;
import org.olat.core.id.context.DefaultContextEntryControllerCreator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedResourceSecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.modules.webFeed.ui.blog.BlogUIFactory;
import org.olat.resource.OLATResourceManager;

/**
 * 
 * Description:<br>
 * Context entry controller creator for live blogs
 * 
 * <P>
 * Initial Date:  21 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LiveBlogContextEntryControllerCreator  {
	
	private static final OLog log = Tracing.createLoggerFor(LiveBlogContextEntryControllerCreator.class);

	public LiveBlogContextEntryControllerCreator(final FeedManager feedManager) {
		
		NewControllerFactory.getInstance().addContextEntryControllerCreator("LiveBlog", new LBContextEntryControllerCreator(feedManager));	
	}
	
	private static class LBContextEntryControllerCreator extends DefaultContextEntryControllerCreator {
		
		private final FeedManager feedManager;
		
		public LBContextEntryControllerCreator(FeedManager feedManager) {
			this.feedManager = feedManager;
		}
		
		@Override
		public ContextEntryControllerCreator clone() {
			return this;
		}

		@Override
		public Controller createController(List<ContextEntry> ces, UserRequest ureq, WindowControl wControl) {
			OLATResourceable ores = ces.get(0).getOLATResourceable();
			ores = OLATResourceManager.getInstance().findResourceable(ores.getResourceableId(), BlogFileResource.TYPE_NAME);
			Feed feed = feedManager.loadFeed(ores);
			boolean isOwner = feed.getAuthor() != null && ureq.getIdentity() != null && feed.getAuthor().equals(ureq.getIdentity().getName());
			FeedSecurityCallback secCallback = new FeedResourceSecurityCallback(isOwner);
			FeedMainController controller = new FeedMainController(ores, ureq, wControl, BlogUIFactory.getInstance(ureq.getLocale()), secCallback);
			return new LayoutMain3ColsController(ureq, wControl, controller);
		}

		@Override
		public String getTabName(ContextEntry ce, UserRequest ureq) {
			OLATResourceable ores = ce.getOLATResourceable();
			ores = OLATResourceManager.getInstance().findResourceable(ores.getResourceableId(), BlogFileResource.TYPE_NAME);
			Feed feed = feedManager.loadFeed(ores);
			return feed.getTitle();
		}

		@Override
		public boolean validateContextEntryAndShowError(ContextEntry ce, UserRequest ureq, WindowControl wControl) {
			try {
				OLATResourceable ores = ce.getOLATResourceable();
				ores = OLATResourceManager.getInstance().findResourceable(ores.getResourceableId(), BlogFileResource.TYPE_NAME);
				Feed feed = feedManager.loadFeed(ores);
				return feed != null;
			} catch (Exception e) {
				log.warn("Try to load a live blog with an invalid context entry: " + ce, e);
				return false;
			}
		}
		
	}
}
