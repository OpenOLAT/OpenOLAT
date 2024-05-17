/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.widget;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;

/**
 * 
 * Initial date: 15 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class WidgetFactory {
	
	public static WidgetGroup createWidgetGroup(String name, VelocityContainer vc) {
		WidgetGroup comp = new WidgetGroup(name);
		if (vc != null) {
			vc.put(comp.getComponentName(), comp);
		}
		return comp;
	}
	
	public static TextWidget createTextWidget(String name, VelocityContainer vc, String title, String iconCss) {
		return createTextWidget(name, vc, title, null, iconCss, null, null, null, null, null);
	}
	
	public static TextWidget createTextWidget(String name, VelocityContainer vc, String title, String subTitle,
			String iconCss, String value, String valueCssClass, String additionalCssClass, String additionalText,
			Component additionalComp) {
		TextWidget widget = new TextWidget(name);
		if (vc != null) {
			vc.put(widget.getComponentName(), widget);
		}
		
		widget.setTitle(title);
		widget.setSubTitle(subTitle);
		widget.setIconCss(iconCss);
		widget.setValue(value);
		widget.setValueCssClass(valueCssClass);
		widget.setAdditionalCssClass(additionalCssClass);
		widget.setAdditionalText(additionalText);
		widget.setAdditionalComp(additionalComp);
		
		return widget;
	}
	
	public static FigureWidget createFigureWidget(String name, VelocityContainer vc, String title, String iconCss) {
		return createFigureWidget(name, vc, title, null, iconCss, null, null, null, null, null, null);
	}
	
	public static FigureWidget createFigureWidget(String name, VelocityContainer vc, String title, String subTitle,
			String iconCss, String value, String valueCssClass, String desc, String additionalCssClass, String additionalText,
			Component additionalComp) {
		FigureWidget widget = new FigureWidget(name);
		if (vc != null) {
			vc.put(widget.getComponentName(), widget);
		}
		
		widget.setTitle(title);
		widget.setSubTitle(subTitle);
		widget.setIconCss(iconCss);
		widget.setValue(value);
		widget.setValueCssClass(valueCssClass);
		widget.setDesc(desc);
		widget.setAdditionalCssClass(additionalCssClass);
		widget.setAdditionalText(additionalText);
		widget.setAdditionalComp(additionalComp);
		
		return widget;
	}
	
	public static ComponentWidget createComponentWidget(String name, VelocityContainer vc, String title, String iconCss) {
		return createComponentWidget(name, vc, title, null, iconCss, null, null, null, null, null);
	}
	
	public static ComponentWidget createComponentWidget(String name, VelocityContainer vc, String title, String subTitle,
			String iconCss, Component content, String mainCss, String additionalCssClass, String additionalText,
			Component additionalComp) {
		ComponentWidget widget = new ComponentWidget(name);
		if (vc != null) {
			vc.put(widget.getComponentName(), widget);
		}
		
		widget.setTitle(title);
		widget.setSubTitle(subTitle);
		widget.setIconCss(iconCss);
		widget.setContent(content);
		widget.setMainCss(mainCss);
		widget.setAdditionalCssClass(additionalCssClass);
		widget.setAdditionalText(additionalText);
		widget.setAdditionalComp(additionalComp);
		
		return widget;
	}

}
