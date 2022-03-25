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
package org.olat.modules.gotomeeting.ui;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.util.Formatter;
import org.olat.modules.gotomeeting.GoToMeeting;
import org.olat.modules.gotomeeting.GoToMeetingManager;
import org.olat.modules.gotomeeting.GoToRegistrant;
import org.olat.modules.gotomeeting.model.GoToError;
import org.olat.modules.gotomeeting.model.GoToErrors;
import org.olat.modules.gotomeeting.model.GoToRecordingsG2T;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.03.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GoToMeetingController extends BasicController {

	private Link registerLink, confirmLink, joinLink, startLink, openRecordingsLink;
	private final VelocityContainer mainVC;
	
	private GoToMeeting meeting;
	private GoToRegistrant registrant;
	private final boolean administrator, moderator, readOnly;
	
	private CloseableModalController cmc;
	private GoToRecordingsController recordingsCtrl;
	
	@Autowired
	private GoToMeetingManager meetingMgr;
	
	public GoToMeetingController(UserRequest ureq, WindowControl wControl,
			GoToMeeting meeting, boolean administrator, boolean moderator, boolean readOnly) {
		super(ureq, wControl);
		this.readOnly = readOnly;
		this.moderator = moderator;
		this.administrator = administrator;
		
		mainVC = createVelocityContainer("meeting");
		mainVC.contextPut("title", meeting.getName());
		mainVC.contextPut("description", meeting.getDescription());

		if(administrator || moderator) {
			startLink = LinkFactory.createButtonLarge("training.start", mainVC, this);
			startLink.setTarget("_blank");
		}
		registerLink = LinkFactory.createButtonLarge("training.register", mainVC, this);
		confirmLink = LinkFactory.createButtonLarge("training.confirm", mainVC, this);
		confirmLink.setTarget("_blank");
		joinLink = LinkFactory.createButtonLarge("training.join", mainVC, this);
		joinLink.setTarget("_blank");
		
		openRecordingsLink = LinkFactory.createButton("recordings", mainVC, this);

		GoToError error = new GoToError();
		this.meeting = meetingMgr.getMeeting(meeting, error);
		this.registrant = meetingMgr.getRegistrant(meeting, getIdentity());
		if(error.hasError() && error.getError() != null) {
			mainVC.contextPut("errorMessage", translate(error.getError().i18nKey()));
		}
		
		List<GoToRecordingsG2T> recordings = meetingMgr.getRecordings(meeting, error);
		openRecordingsLink.setVisible(recordings != null && recordings.size() > 0);

		Date start = meeting.getStartDate();
		Date end = meeting.getEndDate();
		Formatter formatter = Formatter.getInstance(getLocale());
		mainVC.contextPut("start", formatter.formatDateAndTime(start));
		mainVC.contextPut("end", formatter.formatDateAndTime(end));

		putInitialPanel(mainVC);
		updateButtons();
	}
	
	private void updateButtons() {
		if(readOnly) {
			startLink.setVisible(false);
			registerLink.setVisible(false);
			confirmLink.setVisible(false);
			joinLink.setVisible(false);
		} else {
			Date start = meeting.getStartDate();
			Date end = meeting.getEndDate();
			Date now = new Date();
			boolean canStart = (start.compareTo(now) <= 0 && end.compareTo(now) > 0);
	
			Calendar cal = Calendar.getInstance();
			cal.setTime(start);
			cal.add(Calendar.MINUTE, -60);
			Date startMinusOne = cal.getTime();
			boolean canCoachStart = (startMinusOne.compareTo(now) <= 0 && end.compareTo(now) > 0);
			
			boolean ended = (end.compareTo(now) <= 0);
			if(administrator || moderator) {
				if(canCoachStart) {
					startLink.setVisible(true);
					registerLink.setVisible(false);
				} else if(ended) {
					startLink.setVisible(false);
					registerLink.setVisible(false);
				} else if(registrant == null) {
					startLink.setVisible(false);
					registerLink.setVisible(true);
				} else {
					startLink.setVisible(false);
					registerLink.setVisible(false);
				}
				confirmLink.setVisible(false);
				joinLink.setVisible(false);
			} else if(ended) {
				registerLink.setVisible(false);
				confirmLink.setVisible(false);
				joinLink.setVisible(false);
			} else if(canStart) {
				if(registrant == null) {
					registerLink.setVisible(false);
					confirmLink.setVisible(false);
					joinLink.setVisible(true);
				} else {
					registerLink.setVisible(false);
					confirmLink.setVisible(false);
					joinLink.setVisible(true);
				}
			} else {
				if(registrant == null) {
					registerLink.setVisible(true);
					confirmLink.setVisible(false);
					joinLink.setVisible(false);
				} else {
					registerLink.setVisible(false);
					confirmLink.setVisible(true);
					joinLink.setVisible(false);
				}
			}
		}
		mainVC.setDirty(true);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(joinLink == source) {
			doJoin(ureq);
		} else if(registerLink == source) {
			doRegister();
		} else if(confirmLink == source) {
			doConfirm(ureq);
		} else if(startLink == source) {
			doStart(ureq);
		} else if(openRecordingsLink == source) {
			doOpenRecordings(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(recordingsCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(recordingsCtrl);
		removeAsListenerAndDispose(cmc);
		recordingsCtrl = null;
		cmc = null;
	}

	private void doStart(UserRequest ureq) {
		if(registrant == null) {
			GoToError error = new GoToError();
			registrant = meetingMgr.registerTraining(meeting, getIdentity(), error);
		}
		GoToError error = new GoToError();
		String startUrl = meetingMgr.startTraining(meeting, error);
		if(startUrl != null) {
			RedirectMediaResource redirect = new RedirectMediaResource(startUrl);
			ureq.getDispatchResult().setResultingMediaResource(redirect);
		} else if(error.getError() == GoToErrors.TrainingInSession) {
			String joinUrl = registrant.getJoinUrl();
			RedirectMediaResource redirect = new RedirectMediaResource(joinUrl);
			ureq.getDispatchResult().setResultingMediaResource(redirect);
		}
	}
	
	private void doRegister() {
		if(registrant == null) {
			GoToError error = new GoToError();
			registrant = meetingMgr.registerTraining(meeting, getIdentity(), error);
			if(registrant != null) {
				showInfo("training.register.success");
			} else {
				showWarning("training.register.failed");
			}
		}
		updateButtons();
	}
	
	private void doConfirm(UserRequest ureq) {
		if(registrant != null) {
			String confirmUrl = registrant.getConfirmUrl();
			if(confirmUrl != null) {
				RedirectMediaResource redirect = new RedirectMediaResource(confirmUrl);
				ureq.getDispatchResult().setResultingMediaResource(redirect);
			}
		}
		updateButtons();
	}

	private void doJoin(UserRequest ureq) {
		boolean join = false;
		GoToError error = new GoToError();
		if(registrant == null) {
			registrant = meetingMgr.registerTraining(meeting, getIdentity(), error);
		}
		if(registrant != null) {
			String joinUrl = registrant.getJoinUrl();
			if(joinUrl != null) {
				RedirectMediaResource redirect = new RedirectMediaResource(joinUrl);
				ureq.getDispatchResult().setResultingMediaResource(redirect);
				join = true;
			}
		}
		if(!join) {
			final String errorMessage;
			if(error.hasError()) {
				if(error.getError() != null) {
					errorMessage = translate(error.getError().i18nKey());
				} else {
					errorMessage = translate("error.code.unkown");
				}
			} else {
				errorMessage = translate("error.code.unkown");
			}
			
			ControllerCreator creator =  BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, (lureq, lwControl) -> {
				// Wrap in column layout, popup window needs a layout controller
				String title = "";
				String text = errorMessage;
				Controller ctr = MessageUIFactory.createErrorMessage(lureq, lwControl, title, text);
				LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, ctr);
				layoutCtr.addDisposableChildController(ctr);
				return layoutCtr;
			});
	
			openInNewBrowserWindow(ureq, creator, false);
		}
		updateButtons();
	}
	
	private void doOpenRecordings(UserRequest ureq) {
		if(guardModalController(recordingsCtrl)) return;
		
		recordingsCtrl = new GoToRecordingsController(ureq, getWindowControl(), meeting);
		listenTo(recordingsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), recordingsCtrl.getInitialComponent(),
				true, translate("recordings"));
		cmc.activate();
		listenTo(cmc);
	}
}
