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
package org.olat.modules.quality.generator.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractGeneratorEditController extends FormBasicController implements TooledController {

	private Link enableLink;
	private Link disableLink;
	private Link deleteLink;

	protected final QualitySecurityCallback secCallback;
	protected final TooledStackedPanel stackPanel;
	protected QualityGenerator generator;
	
	@Autowired
	protected QualityGeneratorService generatorService;
	
	public AbstractGeneratorEditController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, QualityGenerator generator) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		this.generator = generator;
	}
	
	protected void setGenerator(QualityGenerator generator) {
		this.generator = generator;
		updateUI();
	}
	
	protected abstract void updateUI();
	
	@Override
	public void initTools() {
		stackPanel.removeAllTools();
		
		if (secCallback.canActivateGenerators()) {
			String enabled = generator.isEnabled()? "enabled": "disabled";
			
			Dropdown enableDropdown = new Dropdown("generator.enable.dropdown", "generator." + enabled + ".hover", false, getTranslator());
			enableDropdown.setIconCSS("o_icon o_icon-fw o_icon_qual_gen_" + enabled);
			enableDropdown.setOrientation(DropdownOrientation.normal);
		
			enableLink = LinkFactory.createToolLink("generator.enabled", translate("generator.enabled.hover"), this);
			enableLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_enabled");
			enableLink.setVisible(!generator.isEnabled());
			enableDropdown.addComponent(enableLink);
			
			disableLink = LinkFactory.createToolLink("generator.disabled", translate("generator.disabled.hover"), this);
			disableLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_disabled");
			disableLink.setVisible(generator.isEnabled());
			enableDropdown.addComponent(disableLink);
			
			stackPanel.addTool(enableDropdown, Align.left);
		}
		
		long numberDataCollections = generatorService.getNumberOfDataCollections(generator);
		if (secCallback.canDeleteGenerator(numberDataCollections)) {
			deleteLink = LinkFactory.createToolLink("generator.delete", translate("generator.delete"), this);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_delete");
			stackPanel.addTool(deleteLink, Align.left);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == enableLink) {
			fireEvent(ureq, new GeneratorEvent(generator, GeneratorEvent.Action.ENABLE));
		} else if (source == disableLink) {
			fireEvent(ureq, new GeneratorEvent(generator, GeneratorEvent.Action.DISABLE));
		} else if (source == deleteLink) {
			fireEvent(ureq, new GeneratorEvent(generator, GeneratorEvent.Action.DELETE));
		}
		super.event(ureq, source, event);
	}

}
