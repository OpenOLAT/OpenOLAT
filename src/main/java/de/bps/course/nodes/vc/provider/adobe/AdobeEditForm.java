//<OLATCE-103>
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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.vc.provider.adobe;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;

/**
 * Description:<br>
 * Edit form for Adobe Connect specific options.
 *
 * <P>
 * Initial Date: 30.08.2010 <br>
 *
 * @author jens Lindner (jlindne4@hs-mittweida.de)
 * @author skoeber
 */
public class AdobeEditForm extends FormBasicController {

  // GUI
  private FormSubmit submit;
  private MultipleSelectionElement multiSelectOptions;
  private static String OPTION_START_MEETING = "vc.access.start";
  private static String OPTION_OPEN_MEETING = "vc.access.open";
  private boolean showOptions;

  // data
  private AdobeConnectConfiguration config;

  public AdobeEditForm(UserRequest ureq, WindowControl wControl, boolean showOptions, AdobeConnectConfiguration config) {
    super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
    this.config = config;
    this.showOptions = showOptions;

    initForm(this.flc, this, ureq);
  }

  @Override
  protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

    // meeting options
    String[] accessKeys = new String[] {OPTION_OPEN_MEETING, OPTION_START_MEETING};
    String[] accessVals = new String[] {translate(OPTION_OPEN_MEETING), translate(OPTION_START_MEETING)};
    multiSelectOptions = uifactory.addCheckboxesVertical("vc.access", "vc.access.label", formLayout, accessKeys, accessVals, 1);
    multiSelectOptions.select(OPTION_START_MEETING, !config.isGuestStartMeetingAllowed());
    multiSelectOptions.select(OPTION_OPEN_MEETING, !config.isGuestAccessAllowed());
    multiSelectOptions.setVisible(showOptions);
    multiSelectOptions.addActionListener(FormEvent.ONCHANGE);
    multiSelectOptions.showLabel(false);

    submit = new FormSubmit("subm", "submit");
    formLayout.add(submit);
  }
  
  @Override
  protected void doDispose() {
    // nothing to dispose
  }

  @Override
  protected void formOK(UserRequest ureq) {
    // read data from form elements
    if(showOptions) {
    	config.setGuestAccessAllowed(!multiSelectOptions.getSelectedKeys().contains(OPTION_OPEN_MEETING));
    	config.setGuestStartMeetingAllowed(!multiSelectOptions.getSelectedKeys().contains(OPTION_START_MEETING));
    }
    fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
  }

  protected AdobeConnectConfiguration getConfig() {
    return config;
  }
}
//</OLATCE-103>
