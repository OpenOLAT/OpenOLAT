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
package org.olat.user.ui.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.user.UserDataExport;
import org.olat.user.UserDataExport.ExportStatus;
import org.olat.user.UserDataExportService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDataExportController extends FormBasicController {
	
	private MultipleSelectionElement exportEl;
	
	private final Identity identity;
	private final UserDataExport currentDataExport;
	
	@Autowired
	private UserDataExportService exportService;
	
	public UserDataExportController(UserRequest ureq, WindowControl wControl, Identity identity) {
		super(ureq, wControl, "user_data_export");
		this.identity = identity;
		currentDataExport = exportService.getCurrentData(identity);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			boolean processing = currentDataExport != null && (currentDataExport.getStatus() == ExportStatus.processing
					|| currentDataExport.getStatus() == ExportStatus.requested);
			((FormLayoutContainer)formLayout).contextPut("processing", Boolean.valueOf(processing));

			boolean ready = currentDataExport != null && currentDataExport.getStatus() == ExportStatus.ready;
			((FormLayoutContainer)formLayout).contextPut("ready", Boolean.valueOf(ready));
			
			String businessPath = exportService.getDownloadURL(identity);
			String url = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
			((FormLayoutContainer)formLayout).contextPut("dataBusinessPath", url);
			
			if(!processing) {
				List<ExportLabel> exports = getAvailableExports();
				String[] keys = new String[exports.size()];
				String[] values = new String[exports.size()];
				for(int i=exports.size(); i-->0; ) {
					ExportLabel export = exports.get(i);
					keys[i] = export.getId();
					values[i] = export.getLabel();
				}
				exportEl = uifactory.addCheckboxesVertical("export.options", formLayout, keys, values, 1);
				exportEl.setMandatory(true);
				uifactory.addFormSubmitButton("export.start", formLayout);
			}
		}
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(!exportEl.isAtLeastSelected(1)) {
			exportEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> exportIds = exportEl.getSelectedKeys();
		exportService.requestExportData(identity, exportIds, getIdentity());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private List<ExportLabel> getAvailableExports() {
		List<String> exportIds = exportService.getExporterIds();
		List<ExportLabel> labels = new ArrayList<>(exportIds.size());
		for(String exportId:exportIds) {
			labels.add(new ExportLabel(exportId, translate(exportId)));
		}
		Collections.sort(labels);
		return labels;
	}
	
	private static final class ExportLabel implements Comparable<ExportLabel> {
		
		private final String id;
		private final String label;
		
		public ExportLabel(String id, String label) {
			this.id = id;
			this.label = label;
		}

		public String getId() {
			return id;
		}

		public String getLabel() {
			return label;
		}

		@Override
		public int compareTo(ExportLabel o) {
			return label.compareToIgnoreCase(o.label);
		}	
	}
}
