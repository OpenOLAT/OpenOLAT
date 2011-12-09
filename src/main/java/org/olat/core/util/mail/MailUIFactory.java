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

package org.olat.core.util.mail;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.mail.ui.MailContextResolver;
import org.olat.core.util.mail.ui.MailListController;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  25 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailUIFactory {
	
	public static Controller createInboxController(UserRequest ureq, WindowControl wControl, MailContextResolver resolver) {
		return new MailListController(ureq, wControl, false, resolver);
	}
	
	public static Controller createOutboxController(UserRequest ureq, WindowControl wControl, MailContextResolver resolver) {
		return new MailListController(ureq, wControl, true, resolver);
	}
	
	//fxdiff :: FXOLAT-250  we need factory-methods with standard params to create inbox/outbox controllers with the <code>FactoryControllerCreator</code>
	// (used for minimalHome / genericmaincontroller)
	public static Controller createInboxController(UserRequest ureq, WindowControl wControl){
		MailContextResolver resolver = (MailContextResolver)CoreSpringFactory.getBean("mailBoxExtension");
		return  MailUIFactory.createInboxController(ureq, wControl, resolver);
	}
	
	public static Controller createOutboxController(UserRequest ureq, WindowControl wControl){
		MailContextResolver resolver = (MailContextResolver)CoreSpringFactory.getBean("mailBoxExtension");
		return MailUIFactory.createOutboxController(ureq, wControl, resolver);
	}

}
