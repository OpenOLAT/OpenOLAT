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
package org.olat.modules.zoom.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.zoom.ZoomManager;
import org.olat.modules.zoom.ZoomProfile;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmDeleteProfileController extends FormBasicController {

    private FormLink deleteLink;

    final private ZoomProfile zoomProfile;

    @Autowired
    private ZoomManager zoomManager;

    public ConfirmDeleteProfileController(UserRequest ureq, WindowControl wControl, ZoomProfile zoomProfile) {
        super(ureq, wControl, "confirm_delete_profile");
        this.zoomProfile = zoomProfile;
        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        if (formLayout instanceof FormLayoutContainer) {
            FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
            String msg = translate("confirm.delete.profile", zoomProfile.getName());
            layoutCont.contextPut("msg", msg);
        }

        uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
        deleteLink = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (deleteLink == source) {
            doDelete(ureq);
        }
        super.formInnerEvent(ureq, source, event);
    }

    private void doDelete(UserRequest ureq) {
        zoomManager.deleteProfile(zoomProfile);
        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formOK(UserRequest ureq) {
        //
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }
}
