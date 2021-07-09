/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.style.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.CourseStyleService;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 29 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ColorCategoryChooserController extends BasicController {
	
	private final ColorCategory inheritedColorCategory;
	private ColorCategory colorCategory;
	
	@Autowired
	private CourseStyleService courseStyleService;

	public ColorCategoryChooserController(UserRequest ureq, WindowControl wControl,
			ColorCategorySearchParams searchParams, ColorCategory inheritedColorCategory) {
		super(ureq, wControl);
		this.inheritedColorCategory = inheritedColorCategory;
		VelocityContainer mainVC = createVelocityContainer("color_category_chooser");
		
		List<ColorCategory> colorCategories = courseStyleService.getColorCategories(searchParams);
		Collections.sort(colorCategories);
		
		List<String> cmpNames = new ArrayList<>(colorCategories.size());
		for (ColorCategory colorCategory : colorCategories) {
			addColor(mainVC, cmpNames, colorCategory);
		}
		mainVC.contextPut("cmpNames", cmpNames);
		putInitialPanel(mainVC);
	}
	
	private void addColor(VelocityContainer colorVC, List<String> cmpNames, ColorCategory colorCategory) {
		String categoryName = ColorCategory.IDENTIFIER_INHERITED.equals(colorCategory.getIdentifier())
				? CourseStyleUIFactory.translateInherited(getTranslator(), inheritedColorCategory)
				: CourseStyleUIFactory.translate(getTranslator(), colorCategory);
		String iconLeftCss = ColorCategory.IDENTIFIER_INHERITED.equals(colorCategory.getIdentifier())
				? CourseStyleUIFactory.getIconLeftCss(inheritedColorCategory)
				: CourseStyleUIFactory.getIconLeftCss(colorCategory);
		extracted(colorVC, cmpNames, colorCategory, categoryName, iconLeftCss);
	}

	private void extracted(VelocityContainer colorVC, List<String> cmpNames, ColorCategory colorCategory,
			String categoryName, String iconLeftCss) {
		String name = "o_colcat_" + colorCategory.getIdentifier();
		cmpNames.add(name);
		Link colorCategoryEl = LinkFactory.createCustomLink(name, "select", categoryName,
				Link.NONTRANSLATED, colorVC, this);
		colorCategoryEl.setIconLeftCSS(iconLeftCss);
		colorCategoryEl.setUserObject(colorCategory);
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link){
			Link colorLink = (Link) source;
			colorCategory = (ColorCategory)colorLink.getUserObject();
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
	
	public ColorCategory getColorCategory() {
		return colorCategory;
	}

	@Override
	protected void doDispose() {
		//
	}
}