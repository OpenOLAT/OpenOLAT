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
package org.olat.course.certificate.ui;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 28.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesSelectionDataModel extends DefaultFlexiTableDataModel<CertificateInfos> {
	
	protected static final int PASSED_COL = 10001;
	protected static final int SCORE_COL = 10002;
	
	private List<UserPropertyHandler> userPropertyHandlers;
	
	public CertificatesSelectionDataModel(FlexiTableColumnModel columnModel, List<UserPropertyHandler> userPropertyHandlers) {
		super(columnModel);
		this.userPropertyHandlers = userPropertyHandlers;
	}

	@Override
	public Object getValueAt(int row, int col) {
		CertificateInfos infos = getObject(row);
		Identity identity = infos.getAssessedIdentity();
		if(col == PASSED_COL) {
			return infos.getPassed();
		} else if(col == SCORE_COL) {
			Float score = infos.getScore();
			return AssessmentHelper.getRoundedScore(score);
		} else if(col >= 0 && col < userPropertyHandlers.size()) {
			UserPropertyHandler handler = userPropertyHandlers.get(col);
			return handler.getUserProperty(identity.getUser(), null);
		}
		return null;
	}
}
