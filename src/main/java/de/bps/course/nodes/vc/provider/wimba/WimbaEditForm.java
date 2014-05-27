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
package de.bps.course.nodes.vc.provider.wimba;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;

/**
 *
 * Description:<br>
 * Edit form for date list and Wimba Classroom specific configuration
 *
 * <P>
 * Initial Date:  12.01.2011 <br>
 * @author skoeber
 */
public class WimbaEditForm extends FormBasicController {

  // GUI
  private FormSubmit submit;
  private MultipleSelectionElement multiSelectOptions;
  private static String OPTION_GUEST_ACCESS = "vc.guest.access";

  // data
  private WimbaClassroomConfiguration config;

  public WimbaEditForm(UserRequest ureq, WindowControl wControl, WimbaClassroomConfiguration config) {
    super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
    this.config = config;

    initForm(this.flc, this, ureq);
  }

  @Override
  protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

    // options
    String[] optionKeys = new String[] {OPTION_GUEST_ACCESS};
    String[] optionVals = new String[] {translate(OPTION_GUEST_ACCESS)};
    multiSelectOptions = uifactory.addCheckboxesVertical("vc.options", "vc.options.label", formLayout, optionKeys, optionVals, 1);
    multiSelectOptions.select(OPTION_GUEST_ACCESS, config.isGuestAccessAllowed());
    multiSelectOptions.showLabel(false);
    
    submit = new FormSubmit("subm", "submit");
    
    formLayout.add(submit);
  }

  @Override
  protected void formOK(UserRequest ureq) {
  	config.setGuestAccessAllowed(multiSelectOptions.getSelectedKeys().contains(OPTION_GUEST_ACCESS));
  	fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
  }

  @Override
  protected void doDispose() {
    // nothing to dispose
  }

}
//<OLATCE-103>
