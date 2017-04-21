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
package org.olat.course.archiver;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.nodes.CourseNode;
import org.olat.ims.qti.export.OptionsChooseForm;
import org.olat.ims.qti.export.QTIExportItemFormatConfig;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * user interface to determine export config
 * 
 * Initial Date: 11.04.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class ExportOptionsController extends FormBasicController {

	private static final String ITEMCOLS = "itemcols";
	private static final String POSCOL = "poscol";
	private static final String POINTCOL = "pointcol";
	private static final String TIMECOLS = "timecols";

	private MultipleSelectionElement downloadOptionsEl;
	
	private String[] optionKeys, optionVals;
	
	@Autowired
	private FormatConfigHelper configHelper;

	
	public ExportOptionsController(UserRequest ureq, WindowControl wControl, CourseNode courseNode) {
		super(ureq, wControl);
		Translator fallback = Util.createPackageTranslator(OptionsChooseForm.class, getLocale());
		setTranslator(Util.createPackageTranslator(getTranslator(), fallback, getLocale()));
		
		optionKeys = new String[]{ITEMCOLS, POSCOL, POINTCOL, TIMECOLS};
		optionVals = new String[] {
				translate("form.itemcols"),
				translate("form.poscol"),
				translate("form.pointcol"),
				translate("form.timecols")
		};	
		
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
		
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		downloadOptionsEl = uifactory.addCheckboxesVertical("setting", "form.title", formLayout, optionKeys, optionVals, 1);
		QTIExportItemFormatConfig c = configHelper.doLoadQTIExportFormatConfig(ureq);
		downloadOptionsEl.select(ITEMCOLS, c.hasResponseCols());
		downloadOptionsEl.select(POSCOL,   c.hasPositionsOfResponsesCol());
		downloadOptionsEl.select(POINTCOL, c.hasPointCol());
		downloadOptionsEl.select(TIMECOLS, c.hasTimeCols());		
		
		FormItemContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonContainer);		
		uifactory.addFormSubmitButton("save", buttonContainer);
		uifactory.addFormCancelButton("cancel", buttonContainer, ureq, getWindowControl());
	}
	
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= downloadOptionsEl.isAtLeastSelected(1);
		if(!allOk) {
			downloadOptionsEl.setErrorKey("nodechoose.config.error", null);			
		}
		return allOk &= super.validateFormLogic(ureq);
	}
	
	
	@Override
	protected void formOK(UserRequest ureq) {
		doUpdateMemberListConfig(ureq);
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doUpdateMemberListConfig(UserRequest ureq) {
		// save new config in GUI prefs
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			boolean itemcols = downloadOptionsEl.isSelected(0);
			boolean poscol = downloadOptionsEl.isSelected(1);
			boolean pointcol = downloadOptionsEl.isSelected(2);
			boolean timecols = downloadOptionsEl.isSelected(3);
			configHelper.updateQTIExportFormatConfig(ureq, itemcols, poscol, pointcol, timecols);
		}
	}

}
