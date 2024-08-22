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
	protected static final TinyConfig editorConfig;
	protected static final TinyConfig editorFullConfig;
	protected static final TinyConfig editorCompactConfig;
	protected static final TinyConfig fileEditorConfig;
	protected static final TinyConfig minimalisticConfig;
	protected static final TinyConfig veryMinimalisticWithLinksConfig;
	protected static final TinyConfig veryMinimalisticConfig;
	protected static final TinyConfig readOnlyConfig;
	protected static final TinyConfig paragraphEditorConfig;
	
	//read only profile
	static {
		String plugins = "lists,charmap,image,insertdatetime,table,visualchars,visualblocks,emoticons,link,quotespliter,olatmatheditor,olatmovieviewer,visualchars,visualblocks,media";
		readOnlyConfig = new TinyConfig(plugins, null, "");
	}
	//min profile
	static {
		String plugins = "lists,emoticons,link,quotespliter,visualchars";
		String toolbar1 = "undo redo | bold italic underline | alignjustify alignright aligncenter alignleft | forecolor backcolor | bullist numlist | link unlink emoticons | removeformat";
		minimalisticConfig = new TinyConfig(plugins, null, toolbar1);
	}
	//standard profile
	static {
		String plugins = "lists,emoticons,link,charmap,quotespliter,olatmatheditor,visualchars,visualblocks";
		String[] menu = {
				"edit: {title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall searchreplace'}",
			  "insert: {title: 'Insert', items: 'olatmovieviewer olatrecordaudio media image link | olatmatheditor hr charmap insertdatetime emoticons'}",
			  "view: {title: 'View', items: 'visualblocks visualchars | preview fullscreen'}",
			  "format: {title: 'Format', items: 'bold italic underline strikethrough superscript subscript | removeformat'}"
		};
		String tools1 = "bold italic underline | alignjustify alignright aligncenter alignleft | blocks | fontfamily fontsize | forecolor backcolor | bullist numlist indent outdent | olatmovieviewer olatrecordaudio image charmap emoticons hr link | olatqtifibtext olatqtifibnumerical olatqtihottext olatqtiinlinechoice";
		editorConfig = new TinyConfig(plugins, menu, tools1);
	}
	//compact profile
	static {
		String plugins = "lists,charmap,image,insertdatetime,table,visualchars,visualblocks,emoticons,link,quotespliter,olatmatheditor,olatmovieviewer,visualchars,visualblocks,media";
		String[] menu = {
				"edit: {title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall searchreplace'}",
				"insert: {title: 'Insert', items: 'olatmovieviewer olatrecordaudio media image link | olatmatheditor hr charmap insertdatetime emoticons'}",
				"view: {title: 'View', items: 'visualblocks visualchars visualaid | preview fullscreen'}",
				"format: {title: 'Format', items: 'bold italic underline strikethrough superscript subscript | formats | removeformat'}",
				"table: {title: 'Table', items: 'inserttable tableprops deletetable | cell row column'}"
		};
		String tools1 = "bold italic underline | alignjustify alignright aligncenter alignleft | styles | fontsize | forecolor backcolor | bullist numlist indent outdent | olatmovieviewer olatrecordaudio image olatmatheditor charmap hr link | olatqtifibtext olatqtifibnumerical olatqtihottext olatqtiinlinechoice";
		editorCompactConfig = new TinyConfig(plugins, menu, tools1);
	}
	//invisible
	static {
		String plugins = "charmap,image,insertdatetime,table,visualchars,visualblocks,emoticons,link,quotespliter,olatmatheditor,olatmovieviewer,visualchars,visualblocks,media";
		String tools1 = "bold italic underline | image olatmatheditor";
		veryMinimalisticConfig = new TinyConfig(plugins, null, tools1);
	}
	static {
		String plugins = "colorpicker,textcolor,charmap,image,insertdatetime,table,visualchars,visualblocks,emoticons,link,quotespliter,olatmatheditor,olatmovieviewer,visualchars,visualblocks,media";
		String tools1 = "bold italic underline | image olatmatheditor | link unlink";
		veryMinimalisticWithLinksConfig = new TinyConfig(plugins, null, tools1);
	}
	
	//paragraph editor for content editor
	static {
		String plugins = "lists,link,olatmatheditor";
		String tools1 = "bold italic underline strikethrough | alignjustify alignright aligncenter alignleft | fontsize forecolor backcolor | bullist numlist | link | olatmatheditor removeformat";
		paragraphEditorConfig = new TinyConfig(plugins, null, tools1);
	}
	//full profile
	static {
		String plugins = "advlist,lists,emoticons,link,charmap,quotespliter,olatmatheditor,visualchars,visualblocks,table";
		String[] menu = {
				"edit: {title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall searchreplace'}",
				"insert: {title: 'Insert', items: 'olatmovieviewer olatrecordaudio media image link | olatmatheditor hr charmap insertdatetime emoticons'}",
				"view: {title: 'View', items: 'visualblocks visualchars visualaid | preview fullscreen'}",
				"format: {title: 'Format', items: 'bold italic underline strikethrough superscript subscript | removeformat'}",
				"table: {title: 'Table', items: 'inserttable tableprops deletetable | cell row column'}"
		};
		String tools1 = "bold italic underline | alignjustify alignright aligncenter alignleft | blocks | fontfamily fontsize | forecolor backcolor | bullist numlist indent outdent | olatmovieviewer olatrecordaudio image olatmatheditor charmap emoticons hr link | olatqtifibtext olatqtifibnumerical olatqtihottext olatqtiinlinechoice";
		editorFullConfig = new TinyConfig(plugins, menu, tools1);
	}
	//file profile
	static {
		String plugins = "advlist,lists,link,charmap,image,importcss,insertdatetime,code,table,visualchars,visualblocks,fullscreen,anchor,olatmatheditor,olatmovieviewer,searchreplace,emoticons,media";
		String[] menu = {
			  "edit: {title: 'Edit', items: 'undo redo | cut copy paste pastetext | selectall searchreplace'}",
			  "insert: {title: 'Insert', items: 'olatmovieviewer olatrecordaudio media image link | olatmatheditor hr charmap anchor insertdatetime emoticons'}",
			  "view: {title: 'View', items: 'visualblocks visualchars visualaid | preview fullscreen'}",
			  "format: {title: 'Format', items: 'bold italic underline strikethrough superscript subscript | styles | removeformat'}",
			  "table: {title: 'Table', items: 'inserttable tableprops deletetable | cell row column'}"
		};
		String tools1 = "bold italic underline | styles | fontfamily fontsize | forecolor backcolor | bullist numlist indent outdent | olatmovieviewer olatrecordaudio image charmap olatmatheditor olatedusharing emoticons hr link | olatqtifibtext olatqtifibnumerical olatqtihottext olatqtiinlinechoice | removeformat code";
		fileEditorConfig = new TinyConfig(plugins, menu, tools1);
	}

	private final String plugins;
	
	private final String[] menu;
	private final String tool1;
	
	public TinyConfig(String plugins, String[] menu, String tool1) {
		this.plugins = plugins;
		this.menu = menu;
		this.tool1 = tool1;
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
	
	public boolean isMathEnabled() {
		return plugins != null && plugins.indexOf("olatmatheditor") >= 0;
	}
	
	public TinyConfig enableCharcount() {
		return enableFeature("olatcharcount");
	}
	
	public TinyConfig enableCode() {
		return enableFeature("code");
	}

	public TinyConfig enableAutoResize() {
		return enableFeature("autoresize");
	}

	public TinyConfig disableAutoResize() {
		return disableFeature("autoresize");
	}

	public TinyConfig enableImageAndMedia() {
		return enableFeature("image")
				.enableFeature("media")
				.enableFeature("olatmovieviewer");
	}
	
	public TinyConfig enableQTITools(boolean textEntry, boolean numericalInput, boolean hottext, boolean inlineChoice) {
		TinyConfig config = enableFeature("olatqti");
		if(!textEntry) {
			config = config.disableButtons("olatqtifibtext");
		}
		if(!numericalInput) {
			config = config.disableButtons("olatqtifibnumerical");
		}
		if(!hottext) {
			config = config.disableButtons("olatqtihottext");
		}
		if(!inlineChoice) {
			config = config.disableButtons("olatqtiinlinechoice");
		}
		return config;
	}
	
	/**
	 * Disable image, media and movie plugins.
	 * @return
	 */
	public TinyConfig disableImageAndMedia() {
		return disableFeature("image")
				.disableFeature("media")
				.disableFeature("olatmovieviewer")
				.disableFeature("olatrecordaudio");
	}
	
	public TinyConfig disableMenuAndMenuBar() {
		return new TinyConfig(plugins, new String[0], null);
	}
	
	public TinyConfig disableTinyMedia() {
		return disableFeature("media");
	}
	
	public TinyConfig disableSmileys() {
		return disableFeature("emoticons");
	}
	
	/**
	 * Remove media + olatmovie
	 * @return
	 */
	public TinyConfig disableMedia() {
		return disableFeature("media").disableFeature("olatmovieviewer").disableFeature("olatrecordaudio");
	}
	
	public TinyConfig enableMathEditor() {
		return enableFeature("olatmatheditor")
				.endableButton("olatmatheditor");
	}
	
	public TinyConfig disableMathEditor() {
		return disableFeature("olatmatheditor");
	}
	
	public TinyConfig enableEdusharing() {
		return enableFeature("olatedusharing")
				.endableButton("olatedusharing");
	}

	public TinyConfig disableEdusharing() {
		return disableFeature("olatedusharing")
				.disableButtons("olatedusharing");
	}
	
	public TinyConfig endableButton(String button) {
		TinyConfig config = this;
		
		if (!tool1.contains(button)) {
			String clonedTools =  tool1 + " " + button;
			config = new TinyConfig(plugins, menu, clonedTools);
		}
		
		return config;
	}
	
	public TinyConfig disableButtons(String button) {
		TinyConfig config = this;
		if(tool1.contains(button)) {
			String clonedTools =  tool1.replace(button, "");
			config = new TinyConfig(plugins, menu, clonedTools);
		}
		return config;
	}
	
	public TinyConfig enableFeature(String feature) {
		if(plugins.contains(feature)) {
			return this;
		} else {
			String clonedPlugins =  plugins + "," + feature;
			return new TinyConfig(clonedPlugins, menu, tool1);
		}
	}
	
	private TinyConfig disableFeature(String feature) {
		if(plugins.contains(feature)) {
			String clonedPlugins =  plugins.replace("," + feature, "");
			return new TinyConfig(clonedPlugins, menu, tool1);
		} else {
			return this;
		}
	}
	
	@Override
	public TinyConfig clone() {
		return new TinyConfig(plugins, menu, tool1);
	}
}
