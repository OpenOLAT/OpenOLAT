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
package org.olat.portfolio.ui.structel;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingService;
import org.olat.core.commons.services.commentAndRating.impl.ui.UserCommentsAndRatingsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalWindowWrapperController;
import org.olat.portfolio.model.structel.PortfolioStructure;

/**
 * 
 * Description:<br>
 * Button which popup the comments and ratings controller
 * 
 * <P>
 * Initial Date:  16 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPAddCommentsController extends BasicController {
	
	private final PortfolioStructure element;
	private final CommentAndRatingService commentAndRatingService;
	
	private final Link commentLink;
	private final VelocityContainer vc;
	private UserCommentsAndRatingsController commentsAndRatingCtr;
	private CloseableModalWindowWrapperController commentsBox;

	public EPAddCommentsController(UserRequest ureq, WindowControl wControl, PortfolioStructure element) {
		super(ureq, wControl);
		
		this.element = element;
		
		String subPath = null;
		PortfolioStructure root = element;
		if(element.getRoot() != null) {
			root = element.getRoot();
			subPath = element.getKey().toString();
		}
		commentAndRatingService = (CommentAndRatingService) CoreSpringFactory.getBean(CommentAndRatingService.class);
		commentAndRatingService.init(getIdentity(), root.getOlatResource(), subPath, false, ureq.getUserSession().getRoles().isGuestOnly());

		vc = createVelocityContainer("commentLink");
		
		commentLink = LinkFactory.createLink("commentLink", vc, this);
		commentLink.setCustomEnabledLinkCSS("b_eportfolio_comment_link b_comments");
		Long numberOfComments = commentAndRatingService.getUserCommentsManager().countComments();
		commentLink.setCustomDisplayText(translate("commentLink", new String[]{numberOfComments.toString()}));
		
		putInitialPanel(vc);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(commentLink == source) {
			popUpCommentBox(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(commentsBox == source) {
			Long numberOfComments = commentAndRatingService.getUserCommentsManager().countComments();
			commentLink.setCustomDisplayText(translate("commentLink", new String[]{numberOfComments.toString()}));
		}
	}

	private void popUpCommentBox(UserRequest ureq) {
		if (commentsAndRatingCtr == null) {
			commentsAndRatingCtr = commentAndRatingService.createUserCommentsAndRatingControllerExpandable(ureq, getWindowControl());
			commentsAndRatingCtr.addUserObject(element);
			commentsAndRatingCtr.expandComments(ureq);
			listenTo(commentsAndRatingCtr);
		}
		String title = translate("commentLink", new String[]{element.getTitle()});
		commentsBox = new CloseableModalWindowWrapperController(ureq, getWindowControl(), title, commentsAndRatingCtr.getInitialComponent(),
				"addComment" + element.getKey());
		listenTo(commentsBox);
		commentsBox.activate();
	}
}
