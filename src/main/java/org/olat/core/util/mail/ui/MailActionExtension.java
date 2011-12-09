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

import org.olat.core.extensions.action.GenericActionExtension;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tree.GenericTreeNode;

/**
 * 
 * Description:<br>
 * MailActionExtension is used in MinimalHomeController (Home-menu). it returns
 * a MailTreeNode which displays an iconDecorator if user has unread e-mails in
 * his inbox
 * 
 * <P>
 * Initial Date: 13.09.2011 <br>
 * 
 * @author Sergio Trentini, sergio.trentini@frentix.com, www.frentix.com
 */
public class MailActionExtension extends GenericActionExtension {

	@Override
	public GenericTreeNode createMenuNode(UserRequest ureq) {
		GenericTreeNode node = new MailTreeNode(ureq.getIdentity());
		node.setAltText(getDescription(ureq.getLocale()));
		node.setTitle(getActionText(ureq.getLocale()));
		node.setIconCssClass(getIconCssClass());
		node.setCssClass(getCssClass());

		node.setUserObject(this);

		return node;
	}

}
