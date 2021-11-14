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
package org.olat.modules.qpool.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.coordinate.LockResult;
import org.olat.modules.qpool.QuestionItemSecurityCallback;
import org.olat.modules.qpool.ui.events.QItemReviewEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ReviewActionController extends BasicController {

	private VelocityContainer mainVC;
	private Link startReviewLink;
	private Link reviewLink;
	
	private QuestionItemSecurityCallback securityCallback;
	private final LockResult lock;
	
	@Autowired
	private UserManager userManager;

	protected ReviewActionController(UserRequest ureq, WindowControl wControl,
			QuestionItemSecurityCallback securityCallback, LockResult lock) {
		super(ureq, wControl);
		this.securityCallback = securityCallback;
		this.lock = lock;
		
		mainVC = createVelocityContainer("review_action");
		
		startReviewLink = LinkFactory.createButton("process.activate.start.review", mainVC, this);
		startReviewLink.setVisible(false);
		reviewLink = LinkFactory.createButton("process.activate.review", mainVC, this);
		reviewLink.setVisible(false);
		
		putInitialPanel(mainVC);
		updateUI();
	}
	
	public void setSecurityCallback(QuestionItemSecurityCallback securityCallback) {
		this.securityCallback = securityCallback;
		updateUI();
	}
	
	private void updateUI() {
		startReviewLink.setVisible(securityCallback.canStartReview() && lock.isSuccess());
		reviewLink.setVisible(securityCallback.canReview());

		String translatedMessage = null;
		if (securityCallback.canStartReview()) {
			if (lock.isSuccess()) {
				translatedMessage = translate("process.activate.start.review.description");
			} else {
				String displayName = "???";
				if (lock.getOwner() != null) {
					displayName = userManager.getUserDisplayName(lock.getOwner());
				}
				translatedMessage = translate("process.activate.locked", new String[] {displayName});
			}
		} else if (securityCallback.canReviewNotStartable()) {
			translatedMessage = translate("process.activate.not.reviewable.description");
		} else if (securityCallback.canReview()) {
			translatedMessage = translate("process.activate.review.description");
		} 
		mainVC.contextPut("reviewMessage", translatedMessage); 
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == startReviewLink) {
			fireEvent(ureq, new QItemReviewEvent(QItemReviewEvent.START));
		} else 	if (source == reviewLink) {
			fireEvent(ureq, new QItemReviewEvent(QItemReviewEvent.DO));
		}
	}
}
