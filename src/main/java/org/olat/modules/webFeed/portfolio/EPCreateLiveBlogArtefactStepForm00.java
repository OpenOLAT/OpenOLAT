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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.ui.artefacts.collect.EPCollectStepForm00;

/**
 * 
 * Description:<br>
 * Only retrieve the title and description to apply them to the blog.
 * 
 * <P>
 * Initial Date:  9 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http.//www.frentix.com
 */
public class EPCreateLiveBlogArtefactStepForm00 extends EPCollectStepForm00 {

	private LiveBlogArtefact blogArtefact;

	public EPCreateLiveBlogArtefactStepForm00(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, int layout,
			String customLayoutPageName, AbstractArtefact artefact) {
		super(ureq, wControl, rootForm, runContext, layout, customLayoutPageName, artefact);
		
		
		blogArtefact = (LiveBlogArtefact)artefact;
	}

	/**
	 * @see org.olat.core.gui.control.generic.wizard.StepFormBasicController#formOK(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void formOK(UserRequest ureq) {
		//set title and description
		super.formOK(ureq);
		//copy title and description to the blog
		
		Feed feed = blogArtefact.getFeedLight();
		feed.setAuthor(blogArtefact.getAuthor().getName());
		feed.setTitle(blogArtefact.getTitle());
		feed.setDescription(blogArtefact.getDescription());
		FeedManager.getInstance().updateFeed(feed);
	}
}
