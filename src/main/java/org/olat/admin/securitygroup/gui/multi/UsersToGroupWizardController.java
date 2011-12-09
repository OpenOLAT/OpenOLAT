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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.admin.securitygroup.gui.multi;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.securitygroup.gui.UserControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.core.commons.persistence.SyncHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.WizardController;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailNotificationEditController;
import org.olat.core.util.mail.MailTemplate;

/**
 * Description:<BR>
 * This wizard controller has three steps:<br>
 * 1) enter list of login names<br>
 * 2) verify matched users<br>
 * 3) add optional mail message
 * <p>
 * Step 3 is optional and only executed when mailTemplate is not NULL.
 * <p>
 * Note that his wizard does only collect data, it does not process any of them.
 * At the end a MultiIdentityChoosenEvent is fired that contains the selected
 * identities and the mail template. The parent controller is expected to do
 * something with those users.
 * <p>
 * Initial Date: Jan 25, 2005
 * 
 * @author Felix Jost, Florian Gnägi
 */

public class UsersToGroupWizardController extends WizardController {
	
	private SecurityGroup securityGroup;
	private BaseSecurity securityManager;
	private VelocityContainer mainVc;
	private UserIdsForm usersForm;
	private TableController newTableC;
	private List<Identity> oks;
	private Link nextButton;
	private Link backButton;
	private MailNotificationEditController mailCtr;
	private MailTemplate mailTemplate;

	// TODO:fj:b WizardController does not need to be a super class!

	/**
	 * assumes that the user seeing this controller has full access rights to the
	 * group (add/remove users)
	 */
	public UsersToGroupWizardController(UserRequest ureq, WindowControl wControl, SecurityGroup aSecurityGroup, MailTemplate mailTemplate) {
		// wizard has two or there steps depending whether the mail template should
		// be configured or not
		super(ureq, wControl, (mailTemplate == null ? 2 : 3));
		
		setBasePackage(UsersToGroupWizardController.class);
		
		this.securityGroup = aSecurityGroup;
		this.securityManager = BaseSecurityManager.getInstance();
		this.mailTemplate = mailTemplate;

		mainVc = createVelocityContainer("index");
		
		nextButton = LinkFactory.createButtonSmall((mailTemplate == null ? "finish" : "next"), mainVc, this);
		backButton = LinkFactory.createButtonSmall("back", mainVc, this);

		usersForm = new UserIdsForm(ureq, wControl);
		listenTo(usersForm);

		// init wizard title and set step 1
		setWizardTitle(translate("import.title"));
		setNextWizardStep(translate("import.title.select"), usersForm.getInitialComponent());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component,
	 *      org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		// default wizard will lissen to cancel wizard event
		super.event(ureq, source, event);

		 if (source == nextButton) {
			if (mailTemplate == null) {
				// wizard stops here - no mail template to fill out
				fireEvent(ureq, new MultiIdentityChosenEvent(this.oks));
			} else {
				// next step is the notification mail form
				removeAsListenerAndDispose(mailCtr);
				mailCtr = new MailNotificationEditController(getWindowControl(), ureq, mailTemplate, false);
				listenTo(mailCtr);
				
				setNextWizardStep(translate("import.title.email"), mailCtr.getInitialComponent());
			}
		} else if (source == backButton) {
			// go back one step in wizard
			setBackWizardStep(translate("import.title.select"), usersForm.getInitialComponent());
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == usersForm) {
			if (event == Event.DONE_EVENT) {
				// calc stuff, preview

				List<Identity> existIdents = securityManager.getIdentitiesOfSecurityGroup(securityGroup);
				oks = new ArrayList<Identity>();
				List<String> isanonymous = new ArrayList<String>();
				List<String> notfounds = new ArrayList<String>();
				List<String> alreadyin = new ArrayList<String>();

				SecurityGroup anonymousSecGroup = securityManager.findSecurityGroupByName(Constants.GROUP_ANONYMOUS);

				// get the logins
				String inp = usersForm.getLoginsString();
				String[] lines = inp.split("\r?\n");
				for (int i = 0; i < lines.length; i++) {
					String username = lines[i].trim();
					if (!username.equals("")) { // skip empty lines
						Identity ident = securityManager.findIdentityByName(username);
						if (ident == null) { // not found, add to not-found-list
							notfounds.add(username);
						} else if (securityManager.isIdentityInSecurityGroup(ident, anonymousSecGroup)) {
							isanonymous.add(username);
						} else {
							// check if already in group
							boolean inGroup = SyncHelper.containsPersistable(existIdents, ident);
							if (inGroup) {
								// added to warning: already in group
								alreadyin.add(ident.getName());
							} else {
								// ok to add -> preview (but filter duplicate entries)
								if (!SyncHelper.containsPersistable(oks, ident)) {
									oks.add(ident);
								}
							}
						}
					}
				}
				// push table and other infos to velocity
				removeAsListenerAndDispose(newTableC);
				newTableC = UserControllerFactory.createTableControllerFor(null, oks, ureq, getWindowControl(), null);
				listenTo(newTableC);
				
				mainVc.put("table", newTableC.getInitialComponent());
				mainVc.contextPut("isanonymous", listNames(isanonymous));
				mainVc.contextPut("notfound", listNames(notfounds));
				mainVc.contextPut("alreadyin", listNames(alreadyin));
				mainVc.contextPut("usercount", new Integer(oks.size()));
				// set table page as next wizard step
				setNextWizardStep(translate("import.title.finish"), mainVc);
			}

		} else if (source == mailCtr) {
			if (event == Event.DONE_EVENT) {
				MultiIdentityChosenEvent multiEvent = new MultiIdentityChosenEvent(this.oks);
				multiEvent.setMailTemplate(mailCtr.getMailTemplate());
				fireEvent(ureq, multiEvent);
			}
		}
	}

	private String listNames(List<String> names) {
		StringBuilder sb = new StringBuilder();
		int cnt = names.size();
		for (int i = 0; i < cnt; i++) {
			String identname = names.get(i);
			sb.append(identname);
			if (i < cnt - 1) sb.append(", ");
		}
		return sb.toString();
	}

	protected void doDispose() {
		//
	}
}

/**
 * Description:<br>
 * Input field for entering user names
 * <P>
 * 
 * @author Felix Jost
 */
class UserIdsForm extends FormBasicController {
	private TextElement idata;

	/**
	 * @param name
	 * @param trans
	 */
	public UserIdsForm(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm (ureq);
	}

	/**
	 * @see org.olat.core.gui.components.Form#validate(org.olat.core.gui.UserRequest)
	 */
	public boolean validate() {
		return !idata.isEmpty("form.legende.mandatory");
	}

	public String getLoginsString() {
		return idata.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
		
	}

	@SuppressWarnings("unused")
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		idata = uifactory.addTextAreaElement("addusers", "form.addusers", -1, 15, 40, true, " ", formLayout);
		idata.setExampleKey ("form.names.example", null);
		uifactory.addFormSubmitButton("next", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}
}
