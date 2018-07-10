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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.resource.OresHelper;
import org.olat.fileresource.types.BlogFileResource;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.FeedResourceSecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.search.document.FeedItemDocument;
import org.olat.modules.webFeed.ui.FeedItemDisplayConfig;
import org.olat.modules.webFeed.ui.blog.BlogUIFactory;
import org.olat.portfolio.EPAbstractHandler;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * 
 * Description:<br>
 * The handler for the life blog artefact
 * 
 * <P>
 * Initial Date:  8 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LiveBlogArtefactHandler extends EPAbstractHandler<LiveBlogArtefact> {

	public static final String LIVEBLOG = "[LiveBlog:";
	
	private FeedManager manager;

	@Override
	public String getType() {
		return LiveBlogArtefact.TYPE;
	}

	@Override
	public LiveBlogArtefact createArtefact() {
		LiveBlogArtefact artefact = new LiveBlogArtefact();
		manager = FeedManager.getInstance();
		OLATResourceable ores = manager.createBlogResource();
		artefact.setBusinessPath(LIVEBLOG + ores.getResourceableId() + "]");
		return artefact;
	}

	@Override
	public Controller createDetailsController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact, boolean readOnlyMode) {
		FeedSecurityCallback callback = new FeedResourceSecurityCallback(false);
		String businessPath = artefact.getBusinessPath();
		Long resid = Long.parseLong(businessPath.substring(10, businessPath.length() - 1));
		OLATResource ores = OLATResourceManager.getInstance().findResourceable(resid, BlogFileResource.TYPE_NAME);
		FeedItemDisplayConfig displayConfig = new FeedItemDisplayConfig(false, false, readOnlyMode);
		return BlogUIFactory.getInstance(ureq.getLocale()).createMainController(ores, ureq, wControl, callback, displayConfig);
	}

	@Override
	public boolean isProvidingSpecialMapViewController() {
		return true;
	}

	@Override
	public Controller getSpecialMapViewController(UserRequest ureq, WindowControl wControl, AbstractArtefact artefact) {
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		boolean isAdministrator = ureq.getIdentity().equalsByPersistableKey(artefact.getAuthor())
				|| ureq.getUserSession().getRoles().isManagerOf(OrganisationRoles.administrator, securityManager.getRoles(artefact.getAuthor()));

		FeedSecurityCallback callback = new FeedResourceSecurityCallback(isAdministrator);
		String businessPath = artefact.getBusinessPath();
		Long resid = Long.parseLong(businessPath.substring(10, businessPath.length() - 1));
		OLATResource ores = OLATResourceManager.getInstance().findResourceable(resid, BlogFileResource.TYPE_NAME);
		FeedItemDisplayConfig displayConfig = new FeedItemDisplayConfig(false, true, true);
		return BlogUIFactory.getInstance(ureq.getLocale()).createMainController(ores, ureq, wControl, callback, displayConfig);
	}

	@Override
	protected void getContent(AbstractArtefact artefact, StringBuilder sb, SearchResourceContext context, EPFrontendManager ePFManager) {
		String businessPath = artefact.getBusinessPath();
		if(StringHelper.containsNonWhitespace(businessPath)) {
			manager = FeedManager.getInstance();
			String oresId = businessPath.substring(LIVEBLOG.length(), businessPath.length() - 1);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(BlogFileResource.TYPE_NAME, Long.parseLong(oresId));
			Feed feed = manager.loadFeed(ores);
			List<Item> publishedItems = manager.loadPublishedItems(feed);

			for (Item item : publishedItems) {
				OlatDocument itemDoc = new FeedItemDocument(item, context);
				String content = itemDoc.getContent();
				sb.append(content);
			}
		}
	}
	
	public class DummyFilter implements Filter {

		@Override
		public String filter(String original) {
			return original;
		}
		
	}
}