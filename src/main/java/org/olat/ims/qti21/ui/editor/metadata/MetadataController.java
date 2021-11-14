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
package org.olat.ims.qti21.ui.editor.metadata;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;
import org.olat.modules.qpool.MetadataSecurityCallback;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.metadata.MetadatasController;

/**
 * 
 * Initial date: 8 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadataController extends BasicController {
	
	private final MetadatasController metadataCtrl;
	
	public MetadataController(UserRequest ureq, WindowControl wControl,
			ManifestMetadataBuilder metadataBuilder, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));

		Locale locale = getLocale();
		QuestionItem item = new ManifestMetadataItemized(metadataBuilder, locale.getLanguage());
		QPoolSecurityCallback qPoolSecurityCallback = new AssessmentItemEditorQPoolSecurityCallback();
		MetadataSecurityCallback metadataScurityCallback = new AssessmentItemEditorMetadataSecurityCallback(readOnly);
			
		metadataCtrl = new MetadatasController(ureq, wControl, qPoolSecurityCallback, item, metadataScurityCallback, false, true);
		listenTo(metadataCtrl);
		putInitialPanel(metadataCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(metadataCtrl == source) {
			if(event instanceof QItemEdited) {
				fireEvent(ureq, new MetadataChangedEvent());
			}
		}
		super.event(ureq, source, event);
	}
}
