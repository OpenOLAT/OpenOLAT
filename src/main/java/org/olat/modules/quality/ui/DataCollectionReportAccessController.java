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
package org.olat.modules.quality.ui;

import static org.olat.modules.quality.QualityReportAccessReference.of;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.QualityDataCollection;
import org.olat.modules.quality.QualitySecurityCallback;

/**
 * 
 * Initial date: 03.11.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DataCollectionReportAccessController extends ReportAccessController {

	private final QualitySecurityCallback secCallback;
	private QualityDataCollection dataCollection;

	protected DataCollectionReportAccessController(UserRequest ureq, WindowControl wControl, 
			QualitySecurityCallback secCallback, QualityDataCollection dataCollection) {
		super(ureq, wControl, of(dataCollection));
		this.secCallback = secCallback;
		this.dataCollection = dataCollection;
		initForm(ureq);
	}
	
	public void setDataCollection(QualityDataCollection dataCollection, UserRequest ureq) {
		this.dataCollection = dataCollection;
		initTable(ureq);
	}

	@Override
	protected boolean canEditReportAccessOnline() {
		return secCallback.canEditDataCollections();
	}

	@Override
	protected boolean canEditReportAccessEmail() {
		return secCallback.canEditReportAccessEmail(dataCollection);
	}


}
