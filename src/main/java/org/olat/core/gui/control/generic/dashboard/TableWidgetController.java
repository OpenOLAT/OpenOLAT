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
package org.olat.core.gui.control.generic.dashboard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: Oct 27, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class TableWidgetController extends FormBasicController {
	
	public static final String CMD_ROW_CLICKED = "row.clicked";

	public TableWidgetController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(TableWidgetController.class, getLocale(), getTranslator()));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String page = Util.getPackageVelocityRoot(TableWidgetController.class) + "/widget_table.html";
		FormLayoutContainer widgetCont = FormLayoutContainer.createCustomFormLayout("widgetCont", getTranslator(), page);
		widgetCont.setRootForm(mainForm);
		formLayout.add(widgetCont);
		
		widgetCont.contextPut("title", getTitle());
		widgetCont.contextPut("tableTitle", getTableTitle());
		widgetCont.contextPut("indicatorsComponentName", createIndicators(widgetCont));
		widgetCont.contextPut("tableComponentName", createTable(widgetCont));
		widgetCont.contextPut("emptyComponentName", createEmptyState(widgetCont));
		widgetCont.contextPut("showAllComponentName", createShowAll(widgetCont));
	}
	
	protected abstract String getTitle();
	protected abstract String getTableTitle();
	protected abstract String createIndicators(FormLayoutContainer widgetCont);
	protected abstract String createTable(FormLayoutContainer widgetCont);
	protected abstract String createShowAll(FormLayoutContainer widgetCont);
	
	/**
	 * @param widgetCont 
	 */
	protected String createEmptyState(FormLayoutContainer widgetCont) {
		return null;
	}
	
	public FlexiCellRenderer wrapCellLink(FlexiCellRenderer renderer) {
		StaticFlexiCellRenderer staticRenderer = new StaticFlexiCellRenderer(CMD_ROW_CLICKED, renderer);
		staticRenderer.setLinkCSS("o_cell_stretch_full o_link_uncolored");
		return staticRenderer;
	}
	
	/**
	 * Convenience method can be used by children if desired.
	 */
	public FormLink createShowAllLink(FormLayoutContainer widgetCont) {
		FormLink toAllLink = uifactory.addFormLink("to.all.entries", widgetCont);
		toAllLink.setIconRightCSS("o_icon o_icon_start");
		return toAllLink;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	public static class HideHeaderDelegate extends DefaultFlexiTableCssDelegate {
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return "o_thead_sr_only";
		}
		
	}

}
