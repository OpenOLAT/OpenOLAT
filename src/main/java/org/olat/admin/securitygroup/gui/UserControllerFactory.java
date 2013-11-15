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

package org.olat.admin.securitygroup.gui;

import java.util.List;
import java.util.Locale;

import org.olat.admin.user.UserTableDataModel;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.user.UserManager;
/**
 * 
 * Description:<br>
 * Factory to create a user table controller
 */
public class UserControllerFactory {

	public static TableController createTableControllerFor(TableGuiConfiguration tableConfig, List<Identity> identities, UserRequest ureq,
			WindowControl wControl, String actionCommand) {
		Locale loc = ureq.getLocale();
		Translator trans = Util.createPackageTranslator(UserControllerFactory.class, loc);
		trans = UserManager.getInstance().getPropertyHandlerTranslator(trans);
		TableController tableCtr = new TableController(tableConfig, ureq, wControl, trans);
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = (roles.isAuthor() || roles.isGroupManager() || roles.isUserManager() || roles.isOLATAdmin());
		UserTableDataModel userDataModel = new UserTableDataModel(identities, loc, isAdministrativeUser);
		userDataModel.addColumnDescriptors(tableCtr, actionCommand);
		tableCtr.setTableDataModel(userDataModel);
		return tableCtr;
	}

}
