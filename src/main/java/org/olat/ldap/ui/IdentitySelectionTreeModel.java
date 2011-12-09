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
package org.olat.ldap.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.id.Identity;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.INodeFilter;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * <h3>Description:</h3> This tree model displays a list of identities
 * <p>
 * Initial Date: 11.11.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class IdentitySelectionTreeModel  extends GenericTreeModel implements INodeFilter {
	/**
	 * Constructor
	 * 
	 * @param identities
	 *            The list of identities
	 * @param usageIdentifyer
	 *            The usageIdentifyer to tell the model which user properties
	 *            should be used
	 * @param locale
	 *            The locale used to format the user properties
	 */
	public IdentitySelectionTreeModel(List<Identity> identities, String usageIdentifyer, Locale locale) {
		// Add the root node
		GenericTreeNode gtn = new GenericTreeNode();
		gtn.setAccessible(false);
		gtn.setTitle("");
		gtn.setIdent("_ROOT_");
		setRootNode(gtn);
		// Add each identity
		List<UserPropertyHandler> properHandlerList = UserManager.getInstance().getUserPropertyHandlersFor(usageIdentifyer, false);
		for (Identity identity : identities) {
			// collect user name information
			StringBuffer sb = new StringBuffer();
			sb.append(identity.getName()).append(": ");
			boolean first = true;
			// collect user properties information
			for (UserPropertyHandler userProperty : properHandlerList) {
				if (first) first = false;
				else sb.append(", ");
				sb.append(userProperty.getUserProperty(identity.getUser(), locale));
			}
			// Create child node
			GenericTreeNode identityNode = new GenericTreeNode();
			identityNode.setAccessible(true);
			identityNode.setTitle(sb.toString());
			identityNode.setUserObject(identity);
			identityNode.setIdent(identity.getName());
			// add child to tree - the tree is flat, only one hierarchy
			gtn.addChild(identityNode);
		}			
	}

	/**
	 * @see org.olat.core.util.tree.INodeFilter#accept(org.olat.core.util.nodes.INode)
	 */
	public boolean accept(INode node) {
		return true;
	}

	/**
	 * Get all identities from the set of tree nodes identifyers
	 * 
	 * @param selected
	 * @return
	 */
	public List<Identity> getIdentities(Set<String> selected) {
		List<Identity> identities = new ArrayList<Identity>();
		for (String ident : selected) {
			Identity identity = (Identity) getNodeById(ident).getUserObject();
			identities.add(identity);
		}			
		return identities;
	}
}


