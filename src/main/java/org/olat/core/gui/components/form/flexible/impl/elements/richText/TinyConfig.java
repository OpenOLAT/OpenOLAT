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
package org.olat.core.gui.components.form.flexible.impl.elements.richText;


/**
 * 
 * Initial date: 16.10.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TinyConfig {
	/*
	sb.append("menu:{\n")
	.append("    file: {title: 'File', items: 'newdocument print'},\n")
	.append("    edit: {title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall searchreplace'},\n")
	.append("    insert: {title: 'Insert', items: 'media image link | olatmovieviewer olatmatheditor olatsmileys | hr charmap insertdatetime'},\n")
	.append("    view: {title: 'View', items: 'visualblocks visualchars visualaid | preview fullscreen'},\n")
	.append("    format: {title: 'Format', items: 'bold italic underline strikethrough superscript subscript | formats | removeformat'},\n")
	.append("    table: {title: 'Table', items: 'inserttable tableprops deletetable | cell row column'},\n")
	.append("    tools: {title: 'Tools', items: 'code'}\n")
	.append("},\n");
	*/

	protected static final TinyConfig editorConfig;
	protected static final TinyConfig editorFullConfig;
	protected static final TinyConfig fileEditorConfig;
	protected static final TinyConfig minimalisticConfig;

	//min profile
	static {
		String plugins =  "textcolor,hr,olatsmileys,paste,link,quotespliter,tabfocus,visualchars,noneditable";
		String toolbar1 = "undo redo | bold italic underline strikethrough | alignjustify alignright aligncenter alignleft | forecolor backcolor | bullist numlist | link unlink | olatsmileys";
		minimalisticConfig = new TinyConfig(plugins, toolbar1, null, null, null, toolbar1, null);
	}
	//standard profile
	static {
		String plugins =   "contextmenu,textcolor,hr,olatsmileys,paste,link,charmap,quotespliter,olatmatheditor,tabfocus,visualchars,visualblocks,noneditable";
		String toolbar1 =  "bold italic underline strikethrough | alignjustify alignright aligncenter alignleft | formatselect fontselect fontsizeselect forecolor backcolor";
		String toolbar2 =  "bullist numlist | indent outdent | undo redo | removeformat visualchars | sup sub | hr charmap | link unlink | image olatmatheditor olatsmileys";
		
		String[] menu = {
				"edit: {title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall searchreplace'}",
			  "insert: {title: 'Insert', items: 'olatmovieviewer media image link | olatmatheditor hr charmap insertdatetime olatsmileys'}",
			  "view: {title: 'View', items: 'visualblocks visualchars | preview fullscreen'}",
			  "format: {title: 'Format', items: 'bold italic underline strikethrough superscript subscript | removeformat'}"
		};
		String tools1 = "bold italic underline | alignjustify alignright aligncenter alignleft | formatselect | fontselect fontsizeselect | forecolor backcolor | bullist numlist indent outdent | olatmovieviewer image charmap olatsmileys hr link | code";
		editorConfig = new TinyConfig(plugins, toolbar1, toolbar2, null, menu, tools1, null);
	}
	//full profile
	static {
		String plugins =   "contextmenu,textcolor,hr,olatsmileys,paste,link,charmap,quotespliter,olatmatheditor,tabfocus,visualchars,visualblocks,noneditable,table";
		String toolbar1 =  "bold italic underline strikethrough | alignjustify alignright aligncenter alignleft | formatselect fontselect fontsizeselect forecolor backcolor";
		String toolbar2 =  "cut copy paste pastetext | bullist numlist  | indent outdent  | undo redo  | sup sub  | link unlink  | image";
		String toolbar3 =  "table | removeformat visualchars  | hr charmap olatmatheditor olatsmileys";
		
		String[] menu = {
				"edit: {title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall searchreplace'}",
			  "insert: {title: 'Insert', items: 'olatmovieviewer media image link | olatmatheditor hr charmap insertdatetime olatsmileys'}",
			  "view: {title: 'View', items: 'visualblocks visualchars visualaid | preview fullscreen'}",
			  "format: {title: 'Format', items: 'bold italic underline strikethrough superscript subscript | removeformat'}",
			  "table: {title: 'Table', items: 'inserttable tableprops deletetable | cell row column'}"
		};
		String tools1 = "bold italic underline | alignjustify alignright aligncenter alignleft | formatselect | fontselect fontsizeselect | forecolor backcolor | bullist numlist indent outdent | olatmovieviewer image charmap olatsmileys hr link | code";
		editorFullConfig = new TinyConfig(plugins, toolbar1, toolbar2, toolbar3, menu, tools1, null);
	}
	//file profile
	static {
		String plugins =   "textcolor,hr,link,charmap,image,olatmatheditor,importcss,insertdatetime,code,table,tabfocus,visualchars,visualblocks,print,noneditable,fullscreen,contextmenu,olatmovieviewer,searchreplace,olatsmileys,paste,media";
		String toolbar1 =  "save cancel | bold italic underline strikethrough | alignjustify alignright aligncenter alignleft  | styleselect formatselect fontselect fontsizeselect forecolor backcolor";
		String toolbar2 =  "cut copy paste pastetext | search replace  | bullist numlist  | indent outdent  | undo redo  | cite ins del abbr actronym attribs  | sup sub  | link unlink anchor  | image media olatmovieviewer ";
		String toolbar3 =  "table | removeformat visualchars | print fullscreen code | insertdate inserttime hr charmap olatmatheditor olatsmileys";
		
		String[] menu = {
				"file: {title: 'File', items: 'print'}",
				"edit: {title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall searchreplace'}",
			  "insert: {title: 'Insert', items: 'olatmovieviewer media image link | olatmatheditor hr charmap insertdatetime olatsmileys'}",
			  "view: {title: 'View', items: 'visualblocks visualchars visualaid | preview fullscreen'}",
			  "format: {title: 'Format', items: 'bold italic underline strikethrough superscript subscript | formats | removeformat'}",
			  "table: {title: 'Table', items: 'inserttable tableprops deletetable | cell row column'}"
		};
		//String tools1 = "save cancel | bullist numlist indent outdent | link unlink | olatsmileys";
		//String tools2 = "bold italic underline | styleselect fontselect fontsizeselect forecolor backcolor";
		String tools1 = "bold italic underline | styleselect | fontselect fontsizeselect | forecolor backcolor | bullist numlist indent outdent | olatmovieviewer image charmap olatsmileys hr link | code";
		fileEditorConfig = new TinyConfig(plugins, toolbar1, toolbar2, toolbar3, menu, tools1, null);
	}

	private final String plugins;
	
	private final String[] menu;
	private final String tool1;
	private final String tool2;
	
	private final String toolbar1;
	private final String toolbar2;
	private final String toolbar3;
	
	public TinyConfig(String plugins, String toolbar1, String toolbar2, String toolbar3, String[] menu, String tool1, String tool2) {
		this.plugins = plugins;
		this.toolbar1 = toolbar1;
		this.toolbar2 = toolbar2;
		this.toolbar3 = toolbar3;
		
		this.menu = menu;
		this.tool1 = tool1;
		this.tool2 = tool2;
	}
	
	public String getPlugins() {
		return plugins;
	}
	
	public boolean hasMenu() {
		return menu != null && menu.length > 0;
	}
	
	public String[] getMenu() {
		return menu == null ? new String[0] : menu;
	}
	
	public String getTool1() {
		return tool1;
	}	
	
	public String getTool2() {
		return tool2;
	}

	public String getToolbar1() {
		return toolbar1;
	}

	public String getToolbar2() {
		return toolbar2;
	}

	public String getToolbar3() {
		return toolbar3;
	}
	
	public TinyConfig enableCode() {
		return enableFeature("code", true);
	}
	
	public TinyConfig enableImageAndMedia() {
		return enableFeature("image", true)
				.enableFeature("media", false)
				.enableFeature("olatmovieviewer", false);
	}
	
	/**
	 * Remove media + olatmovie
	 * @return
	 */
	public TinyConfig disableMedia() {
		return disableFeature("media").disableFeature("olatmovieviewer");
	}
	
	public TinyConfig disableMathEditor() {
		return disableFeature("olatmatheditor");
	}
	
	public TinyConfig enableFeature(String feature, boolean separator) {
		if(plugins.contains(feature)) {
			return this;
		} else {
			String button = (separator ? " | " : "") + feature;
			String clonedPlugins =  plugins + "," + feature;
			String clonedToolbar1 = toolbar1;
			String clonedToolbar2 = toolbar2;
			String clonedToolbar3 = toolbar3;
			if(clonedToolbar3 != null) {
				clonedToolbar3 += button;
			} else if(clonedToolbar2 != null) {
				clonedToolbar2 += button;
			} else if(clonedToolbar1 != null) {
				clonedToolbar1 += button;
			}
			return new TinyConfig(clonedPlugins, clonedToolbar1, clonedToolbar2, clonedToolbar3, menu, tool1, tool2);
		}
	}
	
	private TinyConfig disableFeature(String feature) {
		if(plugins.contains(feature)) {
			String clonedPlugins =  plugins.replace("," + feature, "");
			String clonedToolbar1 = toolbar1;
			String clonedToolbar2 = toolbar2;
			String clonedToolbar3 = toolbar3;
			if(clonedToolbar3 != null && clonedToolbar3.contains(feature)) {
				clonedToolbar3 = clonedToolbar3.replace(feature, "");
			} else if(clonedToolbar2 != null && clonedToolbar2.contains(feature)) {
				clonedToolbar2 = clonedToolbar2.replace(feature, "");
			} else if(clonedToolbar1 != null && clonedToolbar1.contains(feature)) {
				clonedToolbar1 = clonedToolbar1.replace(feature, "");
			}
			return new TinyConfig(clonedPlugins, clonedToolbar1, clonedToolbar2, clonedToolbar3, menu, tool1, tool2);
		} else {
			return this;
		}
	}
	
	@Override
	public TinyConfig clone() {
		return new TinyConfig(plugins, toolbar1, toolbar2, toolbar3, menu, tool1, tool2);
	}
}
