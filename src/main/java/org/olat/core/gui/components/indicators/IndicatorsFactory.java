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
package org.olat.core.gui.components.indicators;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Oct 28, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class IndicatorsFactory {
	
	public static IndicatorsComponent createComponent(String name, VelocityContainer vc) {
		IndicatorsComponent indicatorsComponent = new IndicatorsComponent(name);
		if (vc != null) {
			vc.put(indicatorsComponent.getComponentName(), indicatorsComponent);
		}
		return indicatorsComponent;
	}
	
	public static IndicatorsItem createItem(String name, FormItemContainer formLayout) {
		IndicatorsItemImpl indicatorsItem = new IndicatorsItemImpl(name);
		if (formLayout != null) {
			formLayout.add(indicatorsItem);
		}
		return indicatorsItem;
	}

	public static Link createIndicatorLink(String name, String cmd, String label, String figure, ComponentEventListener listener) {
		String linkText = createLinkText(label, figure);
		Link link = LinkFactory.createCustomLink(name, cmd, linkText, Link.LINK | Link.NONTRANSLATED, null, listener);
		link.setElementCssClass("o_indicator_link");
		return link;
	}

	public static FormLink createIndicatorFormLink(String name, String cmd, String label, String figure, FormItemContainer formLayout) {
		String linkText = createLinkText(label, figure);
		FormLink link = FormUIFactory.getInstance().addFormLink(name, cmd, linkText, null, formLayout, Link.LINK + Link.NONTRANSLATED);
		link.setElementCssClass("o_indicator_link");
		return link;
	}

	public static Link createIndicatorLink(String name, String cmd, String label, Component figure, ComponentEventListener listener) {
		Component indiatorComp = createIndiatorComponent(label, figure);
		Link link = LinkFactory.createCustomLink(name, cmd, null, Link.LINK | Link.NONTRANSLATED, null, listener);
		link.setInnerComponent(indiatorComp);
		return link;
	}
	
	public static Component createIndiatorComponent(String label, Component figure) {
		IndicatorComponent indicatorComp = new IndicatorComponent(label, figure);
		return indicatorComp;
	}
	
	public static String createLinkText(String label, String figure) {
		StringBuilder sb = new StringBuilder();
		sb.append("<div class='o_indicator'>");
		if (StringHelper.containsNonWhitespace(label)) {
			sb.append("<div class='o_indicator_label'>");
			sb.append(label);
			sb.append("</div>");
		}
		if (StringHelper.containsNonWhitespace(figure)) {
			sb.append("<div class='o_indicator_figure o_indicator_figure_text'>");
			sb.append(figure);
			sb.append("</div>");
		}
		sb.append("</div>");
		return sb.toString();
	}

}
