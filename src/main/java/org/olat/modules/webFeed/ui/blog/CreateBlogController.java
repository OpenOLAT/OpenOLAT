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
package org.olat.modules.webFeed.ui.blog;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.IAddController;
import org.olat.repository.controllers.RepositoryAddCallback;

/**
 * Controller that handles the creation of a new podcast resource.
 * <P>
 * Initial Date: Mar 18, 2009 <br>
 * 
 * @author gwassmann
 */
public class CreateBlogController extends DefaultController implements IAddController {
	private OLATResourceable feedResource;

	/**
	 * Constructor
	 * 
	 * @param addCallback
	 * @param ureq
	 * @param wControl
	 */
	protected CreateBlogController(RepositoryAddCallback addCallback, UserRequest ureq, WindowControl wControl) {
		super(wControl);
		if (addCallback != null) {
			FeedManager manager = FeedManager.getInstance();
			// Create a new podcast feed resource
			feedResource = manager.createBlogResource();
			Translator trans = new PackageTranslator("org.olat.repository", ureq.getLocale());
			addCallback.setDisplayName(trans.translate(feedResource.getResourceableTypeName()));
			addCallback.setResourceable(feedResource);
			addCallback.setResourceName(manager.getFeedKind(feedResource));
			addCallback.finished(ureq);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// Nothing to dispose
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Component source, Event event) {
	// Nothing to catch
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#getTransactionComponent()
	 */
	public Component getTransactionComponent() {
		// No additional workflow for feed creation
		return null;
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#repositoryEntryCreated(org.olat.repository.RepositoryEntry)
	 */
	@Override
	public void repositoryEntryCreated(RepositoryEntry re) {
	// Nothing to do here, but thanks for asking.
	}
	
	@Override
	public void repositoryEntryCopied(RepositoryEntry sourceEntry, RepositoryEntry newEntry) {
		//
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionAborted()
	 */
	public void transactionAborted() {
		FeedManager.getInstance().delete(feedResource);
	}

	/**
	 * @see org.olat.repository.controllers.IAddController#transactionFinishBeforeCreate()
	 */
	public boolean transactionFinishBeforeCreate() {
		// Don't finish before creation (?!)
		return true;
	}

}
