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
package org.olat.gui.demo.guidemo;

import org.olat.core.commons.controllers.accordion.AssistanceAccordionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 *
 * Initial date: 2022-08-10<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class GuiDemoAssistanceAccordionController extends BasicController {

    private final Link button1;
    private final Link button2;

    public GuiDemoAssistanceAccordionController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);

        VelocityContainer mainVC = createVelocityContainer("guidemo-accordion");

        // without features
        AssistanceAccordionController assistanceAccordionController1 = new AssistanceAccordionController(ureq, wControl,
                getTranslator(), "GuiDemoAssistanceAccordionController.example.title");
        assistanceAccordionController1.addQuestionAnswer(
                "GuiDemoAssistanceAccordionController.example.itemTitle1",
                "GuiDemoAssistanceAccordionController.example.itemDetails1", null);
        assistanceAccordionController1.addQuestionAnswer(
                "GuiDemoAssistanceAccordionController.example.itemTitle2",
                "GuiDemoAssistanceAccordionController.example.itemDetails2", null);
        mainVC.put("accordion1", assistanceAccordionController1.getInitialComponent());

        // with help link
        AssistanceAccordionController assistanceAccordionController2 = new AssistanceAccordionController(ureq, wControl,
                getTranslator(), "GuiDemoAssistanceAccordionController.example.title");
        assistanceAccordionController2.addQuestionAnswer(
                "GuiDemoAssistanceAccordionController.example.itemTitle1",
                "GuiDemoAssistanceAccordionController.example.itemDetails1", null);
        assistanceAccordionController2.setHelpLink(
                "GuiDemoAssistanceAccordionController.example.helpLinkText",
                "manual_admin/administration/Zoom/");
        mainVC.put("accordion2", assistanceAccordionController2.getInitialComponent());

        // with buttons
        button1 = LinkFactory.createButton("GuiDemoAssistanceAccordionController.example.button1", mainVC, this);
        button1.setCustomEnabledLinkCSS("btn btn-primary");
        button2 = LinkFactory.createButton("GuiDemoAssistanceAccordionController.example.button2", mainVC, this);
        button2.setCustomEnabledLinkCSS("btn btn-primary");
        AssistanceAccordionController assistanceAccordionController3 = new AssistanceAccordionController(ureq, wControl,
                getTranslator(), "GuiDemoAssistanceAccordionController.example.title");
        assistanceAccordionController3.addQuestionAnswer(
                "GuiDemoAssistanceAccordionController.example.itemTitle1",
                "GuiDemoAssistanceAccordionController.example.itemDetails1",
                new Component[] { button1, button2 });
        mainVC.put("accordion3", assistanceAccordionController3.getInitialComponent());

        putInitialPanel(mainVC);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == button1) {
            showInfo("GuiDemoAssistanceAccordionController.example.button1.info");
        } else if (source == button2) {
            showInfo("GuiDemoAssistanceAccordionController.example.button2.info");
        }
    }
}
