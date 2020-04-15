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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.NameValuePair;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 15.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StaticFlexiCellRenderer implements FlexiCellRenderer, ActionDelegateCellRenderer {
	
	private static final Logger log = Tracing.createLoggerFor(StaticFlexiCellRenderer.class);

	private String label;
	private String action;
	private String iconLeftCSS;
	private String iconRightCSS;
	private String linkCSS;
	private String linkTitle;
	private boolean newWindow = false;
	private boolean dirtyCheck = true;
	private FlexiCellRenderer labelDelegate;
	
	public StaticFlexiCellRenderer(String label, String action) {
		this(label, action, false, null, null, null);
	}
	
	public StaticFlexiCellRenderer(String label, String action, boolean newWindow) {
		this(label, action, newWindow, null, null, null);
	}
	
	public StaticFlexiCellRenderer(String label, String action, String linkCSS) {
		this(label, action, false, linkCSS, null, null);
	}
	
	public StaticFlexiCellRenderer(String label, String action, String linkCSS, String iconLeftCSS) {
		this(label, action, false, linkCSS, iconLeftCSS, null);
	}
	
	public StaticFlexiCellRenderer(String label, String action, String linkCSS, String iconLeftCSS, String linkTitle) {
		this(label, action, false, linkCSS, iconLeftCSS, linkTitle);
	}
	
	public StaticFlexiCellRenderer(String label, String action, boolean newWindow, String linkCSS, String iconLeftCSS, String linkTitle) {
		this.label = label;
		this.action = action;
		this.linkCSS = linkCSS;
		this.iconLeftCSS = iconLeftCSS;
		this.linkTitle = linkTitle;
		this.newWindow = newWindow;
	}
	
	public StaticFlexiCellRenderer(String action, FlexiCellRenderer labelDelegate) {
		this.labelDelegate = labelDelegate;
		this.action = action;
	}
	
	public String getLabel() {
		return label;
	}
	
	public FlexiCellRenderer getLabelDelegate() {
		return labelDelegate;
	}

	public String getIconLeftCSS() {
		return iconLeftCSS;
	}

	public void setIconLeftCSS(String iconLeftCSS) {
		this.iconLeftCSS = iconLeftCSS;
	}

	public String getIconRightCSS() {
		return iconRightCSS;
	}

	public void setIconRightCSS(String iconRightCSS) {
		this.iconRightCSS = iconRightCSS;
	}

	public String getLinkCSS() {
		return linkCSS;
	}

	public void setLinkCSS(String linkCSS) {
		this.linkCSS = linkCSS;
	}
	
	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
	
	@Override
	public List<String> getActions() {
		if(StringHelper.containsNonWhitespace(action)) {
			return Collections.singletonList(action);
		}
		return Collections.emptyList();
	}

	public boolean isNewWindow() {
		return newWindow;
	}

	public void setNewWindow(boolean newWindow) {
		this.newWindow = newWindow;
	}

	public boolean isDirtyCheck() {
		return dirtyCheck;
	}

	public void setDirtyCheck(boolean dirtyCheck) {
		this.dirtyCheck = dirtyCheck;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		String cellAction = getAction();
		if(StringHelper.containsNonWhitespace(cellAction)) {
			FlexiTableElementImpl ftE = source.getFlexiTableElement();
			String id = source.getFormDispatchId();
			Form rootForm = ftE.getRootForm();
			NameValuePair pair = new NameValuePair(cellAction, Integer.toString(row));
			
			if(newWindow) {
				String jsCode;
				URLBuilder subu = ubu.createCopyFor(ftE.getRootForm().getInitialComponent());
				try(StringOutput href = new StringOutput()) {
					href.append("o_openTab('");
					subu.buildURI(href, AJAXFlags.MODE_NORMAL,
							new NameValuePair("dispatchuri", ftE.getFormDispatchId()),
							new NameValuePair("dispatchevent", "1"),
							pair);
					href.append("')");
					jsCode = href.toString();
				} catch(IOException e) {
					log.error("", e);
					jsCode = "";
				}
				target.append("<a href=\"javascript:").append(jsCode).append(";\"");
			} else {
				String href = null;
				FlexiTableDataModel<?> model = source.getFlexiTableElement().getTableDataModel();
				if(model instanceof FlexiBusinessPathModel) {
					Object object = source.getFlexiTableElement().getTableDataModel().getObject(row);
					href = ((FlexiBusinessPathModel)model).getUrl(source, object, action);
				}
				if(!StringHelper.containsNonWhitespace(href)) {
					href = "javascript:;";
				}
				String jsCode = FormJSHelper.getXHRFnCallFor(rootForm, id, 1, dirtyCheck, true, false, pair);
				target.append("<a href=\"").append(href).append("\" onclick=\"").append(jsCode).append(";\"");
			}
			
			if(StringHelper.containsNonWhitespace(linkTitle)) {
				target.append(" title=\"").appendHtmlEscaped(linkTitle).append("\"");
			}
			if(StringHelper.containsNonWhitespace(linkCSS)) {
				target.append(" class=\"").append(linkCSS).append("\"");
			}
			target.append(">");
			if(StringHelper.containsNonWhitespace(iconLeftCSS)) {
				target.append("<i class=\"o_icon ").append(iconLeftCSS).append("\">&nbsp;</i>");
			}
			if(labelDelegate == null) {
				target.append(getLabel());
			} else {
				labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
			}
			
			if(StringHelper.containsNonWhitespace(iconRightCSS)) {
				target.append(" <i class=\"o_icon ").append(iconRightCSS).append("\">&nbsp;</i>");
			}
			target.append("</a>");
		} else if(labelDelegate == null) {
			target.append(getLabel());
		} else {
			labelDelegate.render(renderer, target, cellValue, row, source, ubu, translator);
		}
	}
}
