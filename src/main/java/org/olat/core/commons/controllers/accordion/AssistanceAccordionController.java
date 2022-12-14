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

    private final List<HelpItem> helpItems = new ArrayList<>();

    public AssistanceAccordionController(UserRequest ureq, WindowControl wControl, Translator translator, String titleKey) {
        super(ureq, wControl, translator);

        mainVC = createVelocityContainer("assistanceAccordion");
        mainVC.contextPut("title", translator.translate(titleKey));
        mainVC.contextPut("helpItems", helpItems);

        putInitialPanel(mainVC);
    }

    public void addQuestionAnswer(String titleKey, String detailsKey, Component[] components) {
        int index = helpItems.size();
        helpItems.add(new HelpItem(index, titleKey, detailsKey, components));
    }

    public void setHelpLink(String helpLinkKey, String contextHelpUrl) {
        mainVC.contextPut("contextHelpText", getTranslator().translate(helpLinkKey));
        mainVC.contextPut("contextHelpUrl", contextHelpUrl);
    }

    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
    	if (source == mainVC) {
    		// Persist expand/close events in help items for support full page refresh / redrawing 
    		String iParam = ureq.getParameter("i");
    		if (!StringHelper.isLong(iParam)) {
    			return; // some hacker or something
    		}
    		int pos = Integer.parseInt(iParam);
    		HelpItem item = helpItems.get(pos);
    		String cmd = event.getCommand();
    		item.setExpanded("e".equals(cmd));
    	}
    }

    public class HelpItem {
        private final String detailsText;
        private final String title;

        private boolean expanded = false;
        

        private final Component[] components;

        public HelpItem(int index, String titleKey, String detailsKey, Component[] components) {
            this.detailsText = StringHelper.containsNonWhitespace(detailsKey)? getTranslator().translate(detailsKey): null;
            this.title = getTranslator().translate(titleKey);

            this.components = components;
            if (components != null) {
                for (Component component : components) {
                    mainVC.put(component.getComponentName(), component);
                }
            }
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
        public String getTitle() {
            return title;
        }

        public Component[] getComponents() {
            return components;
        }

        public boolean hasComponents() {
            return components != null && components.length > 0;
        }
    }
}
