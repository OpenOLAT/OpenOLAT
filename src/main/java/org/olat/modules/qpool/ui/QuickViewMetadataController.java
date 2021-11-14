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
package org.olat.modules.qpool.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.qpool.MetadataSecurityCallback;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.security.ReadOnlyMetadataSecurityCallback;
import org.olat.modules.qpool.ui.metadata.MetadatasController;

/**
 * 
 * Initial date: 10.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QuickViewMetadataController extends BasicController {

	private final SimpleStackedPanel metadataPanel;
	private MetadatasController metadataCtrl;
	
	private final QPoolSecurityCallback qPoolSecurityCallback;
	private final MetadataSecurityCallback metadataSecurityCallback;
	
	protected QuickViewMetadataController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback qPoolSecurityCallback) {
		super(ureq, wControl);
		this.qPoolSecurityCallback = qPoolSecurityCallback;
		this.metadataSecurityCallback = new ReadOnlyMetadataSecurityCallback();
		
		metadataPanel = new SimpleStackedPanel("metadata");
		putInitialPanel(metadataPanel);
	}

	public void setItem(UserRequest ureq, QuestionItem item) {
		if(item == null) {
			metadataCtrl = null;
			metadataPanel.setContent(null);
		} else {
			removeAsListenerAndDispose(metadataCtrl);
			metadataCtrl = new MetadatasController(ureq, getWindowControl(), qPoolSecurityCallback, item, metadataSecurityCallback, false, false);
			metadataPanel.setContent(metadataCtrl.getInitialComponent());
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
