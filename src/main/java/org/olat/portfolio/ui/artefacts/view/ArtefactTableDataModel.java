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
package org.olat.portfolio.ui.artefacts.view;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.util.StringHelper;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.user.UserManager;

/**
 * Description:<br>
 * datamodel for a table with artefacts in it
 * 
 * <P>
 * Initial Date: 20.08.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class ArtefactTableDataModel extends DefaultTableDataModel<AbstractArtefact> {

	private final UserManager userManager;
	private final EPFrontendManager ePFMgr;

	public ArtefactTableDataModel(List<AbstractArtefact> artefacts) {
		super(artefacts);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return 6;
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int,
	 *      int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		AbstractArtefact artefact = objects.get(row);
		switch (col) {
			case 0:
				return artefact.getTitle();
			case 1:
				return artefact.getDescription();
			case 2:
				return artefact.getCreationDate();
			case 3:
				return userManager.getUserDisplayName(artefact.getAuthor());
			case 4: 
				List<String> artTags = ePFMgr.getArtefactTags(artefact);
				return StringHelper.formatAsCSVString(artTags);
			case 5:
				return artefact;
			default:
				return "ERROR";
		}
	}
}
