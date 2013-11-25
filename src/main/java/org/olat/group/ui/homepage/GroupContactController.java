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
package org.olat.group.ui.homepage;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.group.BusinessGroup;
import org.olat.modules.co.ContactFormController;

/**
 * 
 * Initial Date:  Aug 19, 2009 <br>
 * @author twuersch, www.frentix.com
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupContactController extends BasicController {
	
	private final VelocityContainer content;
	private ContactFormController contactForm;
	private final BaseSecurity securityManager;
	
	public GroupContactController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl);
		
		securityManager = BaseSecurityManager.getInstance();
		content = createVelocityContainer("groupcontact");

		// per default contact the group owners.
		if (securityManager.countIdentitiesOfSecurityGroup(businessGroup.getOwnerGroup()) != 0) {
			ContactMessage contactMessage = createContactMessage(ureq.getIdentity(), "form.to.owners", businessGroup.getOwnerGroup());
			contactForm = new ContactFormController(ureq, getWindowControl(), false, false, false, false, contactMessage);
			listenTo(contactForm);
			content.put("contactForm",	contactForm.getInitialComponent());
		} else {
			content.contextPut("nobodyErrorMessage", translate("contactform.noowners"));
		}
		
		putInitialPanel(content);
	}
	
	private ContactMessage createContactMessage(Identity from, String contactListName, SecurityGroup destinationGroup) {
		ContactMessage contactMessage = new ContactMessage(from);
		ContactList contactList = new ContactList(translate(contactListName));
		List<Identity> members = securityManager.getIdentitiesOfSecurityGroup(destinationGroup);
		for (Identity member : members) {
			contactList.add(member);
		}
		contactMessage.addEmailTo(contactList);
		return contactMessage;
	}

	@Override
	protected void doDispose() {
		// Automatic dispose, so nothing to do here.
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to do here.
	}
}
