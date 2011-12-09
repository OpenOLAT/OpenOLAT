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
package org.olat.core.util.mail.ui;

import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.manager.MailManager;

/**
 * 
 * Description:<br>
 * overwrites <code>getIconDecorator1CssClass()</code> to display a special icon
 * if the user has unread mails in his inbox.
 * 
 * <P>
 * Initial Date: 24 mars 2011 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 * @author strentini, sergio.trentini@frentix.com
 * 
 */
public class MailTreeNode extends GenericTreeNode {

	private static final long serialVersionUID = -2579792704194953641L;
	
	private final Identity identity;

	public MailTreeNode(Identity identity) {
		this.identity = identity;
	}
	

	@Override
	public String getIconDecorator1CssClass() {
		if(MailManager.getInstance().hasNewMail(identity)) {
			return "b_mail_new";
		}
		return null;
	}
}
