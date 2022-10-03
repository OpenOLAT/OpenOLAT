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
package org.olat.core.commons.controllers.accordion;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 *
 * Initial date: 2022-08-10<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
public class AssistanceAccordionController extends BasicController {

    private final VelocityContainer mainVC;

    private final Translator translator;

    private final List<HelpItem> helpItems = new ArrayList<>();

    public AssistanceAccordionController(UserRequest ureq, WindowControl wControl, Translator translator, String titleKey) {
        super(ureq, wControl);

        this.translator = translator;

        mainVC = createVelocityContainer("assistanceAccordion");
        mainVC.contextPut("title", translator.translate(titleKey));
        mainVC.contextPut("helpItems", helpItems);

        putInitialPanel(mainVC);

        updateUI();
    }

    private void updateUI() {
        for (HelpItem helpItem : helpItems) {
            helpItem.update();
        }
    }

    public void addQuestionAnswer(String titleKey, String detailsKey, Component[] components) {
        int index = helpItems.size();
        helpItems.add(new HelpItem(index, titleKey, detailsKey, components));
        updateUI();
    }

    public void setHelpLink(String helpLinkKey, String contextHelpUrl) {
        mainVC.contextPut("contextHelpText", translator.translate(helpLinkKey));
        mainVC.contextPut("contextHelpUrl", contextHelpUrl);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        for (HelpItem helpItem : helpItems) {
            if (helpItem.expandButton == source || helpItem.titleLink == source) {
                helpItem.setExpanded(!helpItem.isExpanded());
                helpItem.update();
                mainVC.contextPut("helpItems", helpItems);
            }
        }
    }

    public class HelpItem {
        private final String detailsText;

        private boolean expanded = false;

        private final Link expandButton;

        private final Link titleLink;

        private final Component[] components;

        public HelpItem(int index, String titleKey, String detailsKey, Component[] components) {
            this.detailsText = StringHelper.containsNonWhitespace(detailsKey)? translator.translate(detailsKey): null;

            String expandButtonName = "expandButton_" + index;
            expandButton = LinkFactory.createCustomLink(expandButtonName, expandButtonName, null,
                    Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, mainVC, AssistanceAccordionController.this);
            expandButton.setCustomEnabledLinkCSS("o_clean_link");

            String titleLinkName = "titleLink_" + index;
            titleLink = LinkFactory.createCustomLink(titleLinkName, titleLinkName, null,
                    Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, mainVC, AssistanceAccordionController.this);
            titleLink.setCustomDisplayText(translator.translate(titleKey));
            titleLink.setCustomEnabledLinkCSS("o_clean_link");

            this.components = components;
            if (components != null) {
                for (Component component : components) {
                    mainVC.put(component.getComponentName(), component);
                }
            }
        }

        public void update() {
            String expandedIcon = expanded ? "o_icon_details_collaps" : "o_icon_details_expand";
            expandButton.setIconLeftCSS("o_icon o_icon-2x " + expandedIcon);
        }

        public boolean isExpanded() {
            return expanded;
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
        }

        public String getDetailsText() {
            return detailsText;
        }

        public Link getExpandButton() {
            return expandButton;
        }

        public Link getTitleLink() {
            return titleLink;
        }

        public Component[] getComponents() {
            return components;
        }

        public boolean hasComponents() {
            return components != null && components.length > 0;
        }
    }
}
