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
* <p>
*/
package org.olat.core.gui.components.form.flexible;

import java.io.File;
import java.lang.management.MemoryType;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.form.flexible.elements.AutoCompleter;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MemoryElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement.Layout;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SliderElement;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleExampleText;
import org.olat.core.gui.components.form.flexible.impl.components.SimpleFormErrorText;
import org.olat.core.gui.components.form.flexible.impl.elements.AutoCompleterImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.DownloadLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormReset;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.form.flexible.impl.elements.FormToggleImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.IntegerElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.JSDateChooser;
import org.olat.core.gui.components.form.flexible.impl.elements.MemoryElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.MultiSelectionTreeImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.MultipleSelectionElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.SelectboxSelectionImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.SingleSelectionImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.SliderElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.SpacerElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.StaticTextElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.TextAreaElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.TextBoxListElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.TextElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.gui.components.rating.RatingFormItem;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.themes.Theme;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.tree.INodeFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * Factory class to create the flexible form elements.
 * 
 * @author patrickb
 * 
 */
public class FormUIFactory {
	// inject later via spring
	private static FormUIFactory INSTANCE = new FormUIFactory();

	FormUIFactory() {
		// no public constructors.
	}

	
	/**
	 * 
	 * @return
	 */
	public static FormUIFactory getInstance() {
		return INSTANCE;
	}

	/**
	 * helper for all factory methods, to check if a label should be set or not.
	 * 
	 * each addXXXX method in the factory should have a smallest possible one, which is using the "name" as "i18nkey" for the label. And another method 
	 * with at least one parameter more, the "string i18nkeylabel". Furthermore the latter method should use the setLabelIfNotNull method to decide whether
	 * a label is set (and translated).
	 * 
	 * @param i18nLabel the i18n key to set the label, or <code>null</code> to disable the label.
	 * @param fi
	 */
	static FormItem setLabelIfNotNull(String i18nLabel, FormItem fi){
		if (StringHelper.containsNonWhitespace(i18nLabel)) {
			fi.setLabel(i18nLabel, null);
			fi.showLabel(true);
		}else{
			fi.showLabel(false);
		}
		return fi;
	}
	
	/**
	 * Date chooser is a text field with an icon, which on click shows a java script calendar to choose a date/time.
	 * This method uses the name to set the i18nkey of the label.
	 * <p>
	 * If no label is desired use the {@link FormUIFactory#addDateChooser(String, String, String, FormItemContainer)} with <code>null</code> as i18nLabel.
	 * 
	 * @param name
	 * @param initValue
	 * @param formLayout
	 * @return
	 */
	public DateChooser addDateChooser(String name, Date initValue, FormItemContainer formLayout) {
		return addDateChooser(name, name, initValue, formLayout);
	}

	/**
	 * Date chooser is a text field with an icon, which on click shows a java script calendar to choose a date/time.
	 * 
	 * @param name
	 * @param initValue
	 * @param i18nLabel
	 * @param formLayout
	 * @return
	 */	
	public DateChooser addDateChooser(String name, String i18nLabel, Date initValue, FormItemContainer formLayout) {
		JSDateChooser tmp = new JSDateChooser(name, initValue, formLayout.getTranslator().getLocale());
		setLabelIfNotNull(i18nLabel, tmp);
		formLayout.add(tmp);
		return tmp;
	}
	
	/**
	 * create an integer Element.
	 * This method uses the name to set the i18nkey of the label.
	 * <p>
	 * If no label is desired use the {@link FormUIFactory#addIntegerElement(String, String, int, FormItemContainer)} with <code>null</code> as i18nLabel.
	 * 
	 * @param name
	 * @param initVal
	 * @param formLayout
	 * @return
	 */
	public IntegerElement addIntegerElement(String name, int initVal, FormItemContainer formLayout) {
		return addIntegerElement(name, name, initVal, formLayout);
	}
	/**
	 * create an integer Element
	 * 
	 * @param name
	 * @param initVal
	 * @param formLayout
	 * @return
	 */
	public IntegerElement addIntegerElement(String name, String i18nLabel, int initVal, FormItemContainer formLayout) {
		IntegerElement tmp = new IntegerElementImpl(name, initVal);
		setLabelIfNotNull(i18nLabel, tmp);
		formLayout.add(tmp);
		return tmp;
	}
	
	/**
	 * Create a multiple selection element with check-boxes horizontal aligned.
	 * This method uses the name to set the i18nkey of the label.
	 * <p>
	 * If no label is desired use the {@link FormUIFactory#addCheckboxesHorizontal(String, String, FormItemContainer, String[], String[], String[])} with <code>null</code> as i18nLabel.
	 * @param name
	 * @param layouter
	 * @param keys
	 * @param values
	 * @return
	 */
	public MultipleSelectionElement addCheckboxesHorizontal(String name, FormItemContainer formLayout, String[] keys, String values[]) {
		return addCheckboxesHorizontal(name, name, formLayout, keys, values);
	}
	
	/**
	 * Create a multiple selection element with check-boxes horizontal aligned.
	 * 
	 * @param name
	 * @param i18nLabel
	 * @param formLayout
	 * @param keys
	 * @param values
	 * @return
	 */
	public MultipleSelectionElement addCheckboxesHorizontal(String name, String i18nLabel, FormItemContainer formLayout, String[] keys, String values[]) {
		MultipleSelectionElement mse = new MultipleSelectionElementImpl(name);
		mse.setKeysAndValues(keys, values);
		setLabelIfNotNull(i18nLabel, mse);
		formLayout.add(mse);
		return mse;
	}
	
	/**
	 * Create a multiple selection element with check-boxes that is rendered in vertical columns
	 * This method uses the name to set the i18nkey of the label.
	 * <p>
	 * If no label is desired use the {@link FormUIFactory#addCheckboxesVertical(String, String, FormItemContainer, String[], String[], String[], int)} with <code>null</code> as i18nLabel.
	 * @param name
	 * @param layouter
	 * @param keys
	 * @param values
	 * @param cssClasses
	 * @param columns Currently 1 and 2 columns are supported
	 * @return
	 */
	public MultipleSelectionElement addCheckboxesVertical(String name, FormItemContainer formLayout, String[] keys, String values[], int columns) {
		return addCheckboxesVertical(name, name, formLayout, keys, values, null, null, columns);
	}
	
	/**
	 * 
	 * See above
	 * @param name
	 * @param formLayout
	 * @param keys
	 * @param values
	 * @param iconLeftCSS Icon placed with an &lt;i&gt; tag
	 * @param columns
	 * @return
	 */
	public MultipleSelectionElement addCheckboxesVertical(String name, FormItemContainer formLayout, String[] keys, String values[], String[] iconLeftCSS, int columns) {
		return addCheckboxesVertical(name, name, formLayout, keys, values, null, iconLeftCSS, columns);
	}
	
	public MultipleSelectionElement addCheckboxesVertical(String name, String i18nLabel, FormItemContainer formLayout, String[] keys, String values[], int columns) {
		return addCheckboxesVertical(name, i18nLabel, formLayout, keys, values, null, null, columns);
	}
		
	/**
	 * Create a multiple selection element with check-boxes that is rendered in vertical columns
	 * @param name
	 * @param i18nLabel
	 * @param formLayout
	 * @param keys
	 * @param values
	 * @param cssClasses
	 * @param columns
	 * @return
	 */
	public MultipleSelectionElement addCheckboxesVertical(String name, String i18nLabel, FormItemContainer formLayout,
			String[] keys, String values[], String[] cssClasses, String[] iconLeftCSS, int columns) {
		MultipleSelectionElement mse = new MultipleSelectionElementImpl(name, Layout.vertical, columns);
		mse.setKeysAndValues(keys, values, cssClasses, iconLeftCSS);
		setLabelIfNotNull(i18nLabel, mse);
		formLayout.add(mse);
		return mse;
	}
	
	public MultipleSelectionElement addCheckboxesDropdown(String name, FormItemContainer formLayout) {
		return addCheckboxesDropdown(name, name, formLayout, new String[] {}, new String[] {});
	}
	
	public MultipleSelectionElement addCheckboxesDropdown(String name, String i18nLabel, FormItemContainer formLayout,
			String[] keys, String[] values) {
		return addCheckboxesDropdown(name, i18nLabel, formLayout, keys, values, null, null);
	}
	
	public MultipleSelectionElement addCheckboxesDropdown(String name, String i18nLabel, FormItemContainer formLayout,
			String[] keys, String[] values, String[] cssClasses, String[] iconLeftCSS) {
		MultipleSelectionElement mse = new MultipleSelectionElementImpl(name, Layout.dropdown);
		mse.setKeysAndValues(keys, values, cssClasses, iconLeftCSS);
		setLabelIfNotNull(i18nLabel, mse);
		formLayout.add(mse);
		return mse;
	}

	/**
	 * Create a multiple selection element as a tree.
	 * @param name
	 * @param i18nLabel Can be null
	 * @param formLayout
	 * @param treemodel
	 * @param selectableFilter
	 * @return
	 */
	public MultipleSelectionElement addTreeMultiselect(String name, String i18nLabel, FormItemContainer formLayout, TreeModel treemodel, INodeFilter selectableFilter){
		MultipleSelectionElement mse = new MultiSelectionTreeImpl(name, treemodel, selectableFilter);
		setLabelIfNotNull(i18nLabel, mse);
		formLayout.add(mse);
		return mse;
	}
	
	public MenuTreeItem addTreeMultiselect(String name, String i18nLabel, FormItemContainer formLayout, TreeModel treemodel, ComponentEventListener listener){
		MenuTreeItem mse = new MenuTreeItem(name, listener);
		mse.setTreeModel(treemodel);
		setLabelIfNotNull(i18nLabel, mse);
		formLayout.add(mse);
		return mse;
	}
	
	/**
	 * Add horizontal aligned radio buttons. <br>
	 * This method uses the name to set the i18nkey of the label.
	 * <p>
	 * If no label is desired use the {@link FormUIFactory#addRadiosHorizontal(String, String, FormItemContainer, String[], String[])} with <code>null</code> as i18nLabel.
	 * 
	 * @param name item identifier and i18n key for the label
	 * @param formLayout
	 * @param theKeys the radio button keys
	 * @param theValues the radio button display values
	 * @return
	 */
	public SingleSelection addRadiosHorizontal(final String name, FormItemContainer formLayout, final String[] theKeys, final String[] theValues) {
		return addRadiosHorizontal(name, name, formLayout, theKeys, theValues);
	}
	
	/**
	 * Add horizontal aligned radio buttons. <br>
	 * 
	 * @param name
	 * @param i18nLabel
	 * @param formLayout
	 * @param theKeys
	 * @param theValues
	 * @return
	 */
	public SingleSelection addRadiosHorizontal(final String name, final String i18nLabel, FormItemContainer formLayout, final String[] theKeys, final String[] theValues) {
		SingleSelection ss = new SingleSelectionImpl(name, name, SingleSelection.Layout.horizontal);
		ss.setKeysAndValues(theKeys, theValues, null);
		setLabelIfNotNull(i18nLabel, ss);
		formLayout.add(ss);
		return ss;
	}
	
	
	/**
	 * Add vertical aligned radio buttons<br>
	 * This method uses the name to set the i18nkey of the label.
	 * <p>
	 * If no label is desired use the {@link FormUIFactory#addRadiosVertical(String, String, FormItemContainer, String[], String[])} with <code>null</code> as i18nLabel.
	 * 
	 * @param name item identifier and i18n key for the label
	 * @param formLayout
	 * @param theKeys the radio button keys
	 * @param theValues the radio button display values
	 * @return
	 */
	public SingleSelection addRadiosVertical(final String name, FormItemContainer formLayout, final String[] theKeys, final String[] theValues) {
		return addRadiosVertical(name, name, formLayout, theKeys, theValues);
	}

	/**
	 * Add vertical aligned radio buttons<br>
	 * 
	 * @param name
	 * @param i18nLabel
	 * @param formLayout
	 * @param theKeys
	 * @param theValues
	 * @return
	 */
	public SingleSelection addRadiosVertical(final String name, final String i18nLabel, FormItemContainer formLayout, final String[] theKeys, final String[] theValues) {
		SingleSelection ss = new SingleSelectionImpl(name, name,  SingleSelection.Layout.vertical);
		ss.setKeysAndValues(theKeys, theValues, null);
		setLabelIfNotNull(i18nLabel, ss);
		formLayout.add(ss);
		return ss;
	}
	
	public SingleSelection addDropdownSingleselect(final String name, FormItemContainer formLayout, final String[] theKeys, final String[] theValues) {
		return addDropdownSingleselect(name, name, name, formLayout, theKeys, theValues, null);
	}

	public SingleSelection addDropdownSingleselect(final String name, final String i18nLabel, FormItemContainer formLayout, final String[] theKeys, final String[] theValues) {
		return addDropdownSingleselect(name, name, i18nLabel, formLayout, theKeys, theValues, null);
	}

	/**
	 * Add a drop down menu (also called pulldown menu), with a label's i18n key being the same as the <code>name<code>.
	 * If you do not want a label, use the {@link FormUIFactory#addDropdownSingleselect(String, String, FormItemContainer, String[], String[], String[])} 
	 * method with the <code>i18nKey</code> and set it <code>null</code>
	 * 
	 * @param name item identifier and i18n key for the label
	 * @param formLayout
	 * @param theKeys the menu selection keys
	 * @param theValues the menu display values
	 * @param theCssClasses the css classes to style the menu items or NULL to use no special styling
	 * @return
	 */
	public SingleSelection addDropdownSingleselect(final String name, FormItemContainer formLayout, final String[] theKeys, final String[] theValues, final String[] theCssClasses) {
		return addDropdownSingleselect(name, name, name, formLayout, theKeys, theValues, theCssClasses);
	}
	
	/**
	 * Add a drop down menu (also called pulldown menu).
	 * @param name
	 * @param labelKey i18n key for the label, may be <code>null</code> indicating no label.
	 * @param formLayout
	 * @param theKeys
	 * @param theValues
	 * @param theCssClasses
	 * @return
	 */
	public SingleSelection addDropdownSingleselect(final String name, final String i18nLabel, FormItemContainer formLayout, final String[] theKeys, final String[] theValues, final String[] theCssClasses) {
		return addDropdownSingleselect(name, name, i18nLabel, formLayout, theKeys, theValues, theCssClasses);
	}
	
	/**
	 * Add a drop down menu (also called pulldown menu).
	 * @param id The unique identifier of the selection box (can be null, will be auto generated)
	 * @param name
	 * @param labelKey i18n key for the label, may be <code>null</code> indicating no label.
	 * @param formLayout
	 * @param theKeys
	 * @param theValues
	 * @param theCssClasses
	 * @return
	 */
	public SingleSelection addDropdownSingleselect(final String id, final String name, final String i18nLabel, FormItemContainer formLayout, final String[] theKeys, final String[] theValues, final String[] theCssClasses) {
		SingleSelection ss = new SelectboxSelectionImpl(id, name, formLayout.getTranslator().getLocale());
		ss.setKeysAndValues(theKeys, theValues, theCssClasses);
		setLabelIfNotNull(i18nLabel, ss);
		formLayout.add(ss);
		return ss;
	}
	

	/**
	 * Add a static text, with a label's i18n key being the same as the <code>name<code>.
	 * If you do not want a label, use the {@link FormUIFactory#addStaticTextElement(String, String, String, FormItemContainer)} 
	 * method with the <code>i18nKey</code> and set it <code>null</code>
	 * 
	 * @param name
	 * @param translatedText
	 * @param formLayout
	 * @return
	 */
	public StaticTextElement addStaticTextElement(String name, String translatedText, FormItemContainer formLayout) {
		return addStaticTextElement(name,name,translatedText,formLayout);
	}

	/**
	 * Add a static text. 
	 * @param name
	 * @param i18nLabel
	 * @param translatedText
	 * @param formLayout
	 * @return
	 */
	public StaticTextElement addStaticTextElement(String name, String i18nLabel,String translatedText, FormItemContainer formLayout) {
		StaticTextElement ste = new StaticTextElementImpl(name, translatedText == null ? "" : translatedText);
		setLabelIfNotNull(i18nLabel, ste);
		formLayout.add(ste);
		return ste;
	}
	
	public TextElement addInlineTextElement(String name, String value, FormItemContainer formLayout, FormBasicController listener) {
		TextElement ie = new TextElementImpl(null, name, value, TextElementImpl.HTML_INPUT_TYPE_TEXT, true);
		ie.addActionListener(FormEvent.ONCLICK);
		if(listener != null){
			formLayout.add(ie);
		}
		return ie;
	}
	
	/**
	 * Inserts an HTML horizontal bar (&lt;HR&gt;) element.
	 * 
	 * @param name
	 * @param formLayout
	 * @return
	 */
	public SpacerElement addSpacerElement(String name, FormItemContainer formLayout, boolean onlySpaceAndNoLine) {
		SpacerElement spacer = new SpacerElementImpl(name);
		if (onlySpaceAndNoLine) {
			spacer.setSpacerCssClass("o_spacer_noline");
		}
		formLayout.add(spacer);
		return spacer;
	}
	 
	/**
	 * adds a given text formatted in example style as part of the form.
	 * @param name
	 * @param text
	 * @param formLayout
	 * @return
	 */
	public FormItem addStaticExampleText(String name, String text, FormItemContainer formLayout){
		return addStaticExampleText(name, name, text, formLayout);
	}
	
	/**
	 * 
	 * @param name
	 * @param i18nLabel i18n key for label, null to disable
	 * @param text
	 * @param formLayout
	 * @return
	 */
	public FormItem addStaticExampleText(String name, String i18nLabel, String text, FormItemContainer formLayout){
		final SimpleExampleText set = new SimpleExampleText(name, text);
		//wrap the SimpleExampleText Component within a FormItem
		FormItem fiWrapper = new FormItemImpl("simpleExampleTextWrapper_"+name) {
			
			@Override
			protected Component getFormItemComponent() {
				return set;
			}
		
			@Override
			public void validate(List<ValidationStatus> validationResults) {
				//nothing to do
			}
		
			@Override
			protected void rootFormAvailable() {
			 //nothing to do		
			}
		
			@Override
			public void reset() {
				//nothing to do
			}
		
			@Override
			public void evalFormRequest(UserRequest ureq) {
			 //nothing to do
			}
		};
		setLabelIfNotNull(i18nLabel, fiWrapper);
		formLayout.add(fiWrapper);
		return fiWrapper;
	}
	
	public TextElement addTextElement(final String i18nLabel, final int maxLen, String initialValue,
			FormItemContainer formLayout) {
		return addTextElement(i18nLabel, i18nLabel, maxLen, initialValue, formLayout);
	}
	
	public TextElement addTextElement(String name, final String i18nLabel, final int maxLen, String initialValue,
			FormItemContainer formLayout) {
		String val = initialValue == null ? "" : initialValue;
		return addTextElement(null, name, i18nLabel, maxLen, val, formLayout);
	}
	
	/**
	 * @param id The unique identifier of this text element (can be null)
	 * @param name
	 * @param maxLen
	 * @param initialValue
	 * @param i18nLabel
	 * @param formLayout
	 * @return
	 */
	public TextElement addTextElement(String id, String name, final String i18nLabel, final int maxLen, String initialValue,
			FormItemContainer formLayout) {
		String val = initialValue == null ? "" : initialValue;
		TextElement te = new TextElementImpl(id, name, val);
		te.setNotLongerThanCheck(maxLen, "text.element.error.notlongerthan");
		setLabelIfNotNull(i18nLabel, te);
		te.setMaxLength(maxLen);
		formLayout.add(te);
		return te;
	}
	
	/**
	 * adds a component to choose text elements with autocompletion
	 * see also TextBoxListComponent
	 * @param name
	 * @param i18nLabel
	 * @param inputHint if empty ("") a default will be used
	 * @param initialItems
	 * @param formLayout
	 * @param translator
	 * @return
	 */
	public TextBoxListElement addTextBoxListElement(String name, final String i18nLabel, String inputHint, Map<String, String> initialItems, FormItemContainer formLayout, Translator translator){
		TextBoxListElement tbe = new TextBoxListElementImpl(name, inputHint, initialItems, translator);
		setLabelIfNotNull(i18nLabel, tbe);
		formLayout.add(tbe);
		return tbe;
	}

	public TextElement addPasswordElement(String name, final String i18nLabel, final int maxLen, String initialValue,
			FormItemContainer formLayout) {
		return addPasswordElement(null, name, i18nLabel, maxLen, initialValue, formLayout);
	}
	
	
	
	public TextElement addPasswordElement(String id, String name, final String i18nLabel, final int maxLen, String initialValue,
			FormItemContainer formLayout) {
		TextElement te = new TextElementImpl(id, name, initialValue, TextElementImpl.HTML_INPUT_TYPE_PASSWORD);
		te.setNotLongerThanCheck(maxLen, "text.element.error.notlongerthan");
		setLabelIfNotNull(i18nLabel, te);
		te.setMaxLength(maxLen);
		formLayout.add(te);
		return te;
	}
	
	public AutoCompleter addTextElementWithAutoCompleter(String name, final String i18nLabel, final int maxLen, String initialValue,
			FormItemContainer formLayout) {
		return addTextElementWithAutoCompleter(null, name, i18nLabel, maxLen, initialValue, formLayout);
	}
	
	public AutoCompleter addTextElementWithAutoCompleter(String id, String name, final String i18nLabel, final int maxLen, String initialValue,
			FormItemContainer formLayout) {
		String val = initialValue == null ? "" : initialValue;
		AutoCompleterImpl te = new AutoCompleterImpl(id, name);
		te.setNotLongerThanCheck(maxLen, "text.element.error.notlongerthan");
		setLabelIfNotNull(i18nLabel, te);
		te.setMaxLength(maxLen);
		te.setValue(val);
		formLayout.add(te);
		return te;
	}
	
	/**
	 * Add a multi line text element, using the provided name as i18n key for the label, no max length check set, and fits content hight at maximium (100lnes).
	 * 
	 * @see FormUIFactory#addTextAreaElement(String, String, int, int, int, boolean, boolean, String, FormItemContainer)
	 * @param name
	 * @param rows
	 * @param cols
	 * @param initialValue
	 * @param formLayout
	 * @return
	 */
	public TextAreaElement addTextAreaElement(String name, final int rows, final int cols, String initialValue,	FormItemContainer formLayout) {
		return addTextAreaElement(name, name, -1, rows, cols, true, false, initialValue, formLayout);
	}
	
	/**
	 * Add a multi line text element
	 * @param name
	 * @param i18nLabel i18n key for the label or null to set no label at all.
	 * @param maxLen
	 * @param rows the number of lines or -1 to use default value
	 * @param cols the number of characters per line or -1 to use 100% of the
	 *          available space
	 * @param isAutoHeightEnabled true: element expands to fit content height,
	 *          (max 100 lines); false: specified rows used
	 * @param fixedFontWidth 
	 * @param initialValue Initial value
	 * @param formLayout
	 * @return
	 */
	public TextAreaElement addTextAreaElement(String name, final String i18nLabel, final int maxLen, final int rows, final int cols, boolean isAutoHeightEnabled, boolean fixedFontWidth,
		String initialValue, FormItemContainer formLayout) {
		TextAreaElement te = new TextAreaElementImpl(name, initialValue, rows, cols, isAutoHeightEnabled, fixedFontWidth) {
			{
				setNotLongerThanCheck(maxLen, "text.element.error.notlongerthan");
				// the text.element.error.notlongerthan uses a variable {0} that
				// contains the length maxLen
			}
		};
		setLabelIfNotNull(i18nLabel, te);
		formLayout.add(te);
		return te;
	}

	/**
	 * Add a rich text formattable element that offers basic formatting
	 * functionality and loads the data form the given string value. Use
	 * item.getEditorConfiguration() to add more editor features if you need
	 * them
	 * 
	 * @param name
	 *            Name of the form item
	 * @param i18nLabel
	 *            The i18n key of the label or NULL when no label is used
	 * @param initialValue
	 *            The initial value or NULL if no initial value is available
	 * @param rows
	 *            The number of lines the editor should offer. Use -1 to
	 *            indicate no specific height
	 * @param cols
	 *            The number of characters width the editor should offer. Use -1
	 *            to indicate no specific width
	 * @param externalToolbar
	 *            true: use an external toolbar that is only visible when the
	 *            user clicks into the text area; false: use the static toolbar
	 * @param formLayout The form item container where to add the rich
	 *          text element
	 * @param usess The user session that dispatches the images
	 * @param wControl the current window controller
	 * @param wControl
	 *            the current window controller
	 * @return The rich text element instance
	 */
	public RichTextElement addRichTextElementForStringDataMinimalistic(String name, final String i18nLabel, String initialHTMLValue, final int rows,
			final int cols, FormItemContainer formLayout, WindowControl wControl) {
		// Create richt text element with bare bone configuration
		RichTextElement rte = new RichTextElementImpl(name, initialHTMLValue, rows, cols, formLayout.getRootForm(), formLayout.getTranslator().getLocale());
		setLabelIfNotNull(i18nLabel, rte);
		// Now configure editor
		rte.getEditorConfiguration().setConfigProfileFormEditorMinimalistic(wControl.getWindowBackOffice().getWindow().getGuiTheme());		
		rte.getEditorConfiguration().setPathInStatusBar(false);
		// Add to form and finish
		formLayout.add(rte);
		return rte;
	}

	/**
	 * Add a rich text formattable element that offers simple formatting
	 * functionality and loads the data form the given string value. Use
	 * item.getEditorConfiguration() to add more editor features if you need
	 * them
	 * 
	 * @param name
	 *            Name of the form item
	 * @param i18nLabel
	 *            The i18n key of the label or NULL when no label is used
	 * @param initialValue
	 *            The initial value or NULL if no initial value is available
	 * @param rows
	 *            The number of lines the editor should offer. Use -1 to
	 *            indicate no specific height
	 * @param cols
	 *            The number of characters width the editor should offer. Use -1
	 *            to indicate no specific width
	 * @param externalToolbar
	 *            true: use an external toolbar that is only visible when the
	 *            user clicks into the text area; false: use the static toolbar
	 * @param fullProfile
	 *            false: load only the necessary plugins; true: load all plugins
	 *            from the full profile
	 * @param baseContainer
	 *            The VFS container where to load resources from (images etc) or
	 *            NULL to not allow embedding of media files at all
	 * @param formLayout
	 *            The form item container where to add the richt text element
	 * @param customLinkTreeModel A custom link tree model or NULL not not use a
	 *          custom model
	 * @param formLayout The form item container where to add the rich
	 *          text element
	 * @param usess The user session that dispatches the images
	 * @param wControl the current window controller
	 * @param wControl
	 *            the current window controller
	 * @return The rich text element instance
	 */
	public RichTextElement addRichTextElementForStringData(String name, String i18nLabel, String initialHTMLValue, int rows,
			int cols, boolean fullProfile, VFSContainer baseContainer, CustomLinkTreeModel customLinkTreeModel,
			FormItemContainer formLayout, UserSession usess, WindowControl wControl) {
		// Create richt text element with bare bone configuration
		WindowBackOffice backoffice = wControl.getWindowBackOffice();
		RichTextElement rte = new RichTextElementImpl(name, initialHTMLValue, rows, cols, formLayout.getRootForm(), formLayout.getTranslator().getLocale());
		setLabelIfNotNull(i18nLabel, rte);
		// Now configure editor
		Theme theme = backoffice.getWindow().getGuiTheme();
		rte.getEditorConfiguration().setConfigProfileFormEditor(fullProfile, usess, theme, baseContainer, customLinkTreeModel);			
		// Add to form and finish
		formLayout.add(rte);
		return rte;
	}
	
	public RichTextElement addRichTextElementForStringDataCompact(String name, String i18nLabel, String initialHTMLValue, int rows,
			int cols, VFSContainer baseContainer, FormItemContainer formLayout, UserSession usess, WindowControl wControl) {
		// Create rich text element with bare bone configuration
		RichTextElement rte = new RichTextElementImpl(name, initialHTMLValue, rows, cols, formLayout.getRootForm(), formLayout.getTranslator().getLocale());
		setLabelIfNotNull(i18nLabel, rte);
		// Now configure editor
		Theme theme = wControl.getWindowBackOffice().getWindow().getGuiTheme();
		rte.getEditorConfiguration().setConfigProfileFormCompactEditor(usess, theme, baseContainer);			
		// Add to form and finish
		formLayout.add(rte);
		return rte;
	}
	
	public RichTextElement addRichTextElementForParagraphEditor(String name, String i18nLabel, String initialHTMLValue, int rows,
			int cols, FormItemContainer formLayout, WindowControl wControl) {
		// Create rich text element with bare bone configuration
		RichTextElement rte = new RichTextElementImpl(name, initialHTMLValue, rows, cols, formLayout.getRootForm(), formLayout.getTranslator().getLocale());
		setLabelIfNotNull(i18nLabel, rte);
		// Now configure editor
		rte.getEditorConfiguration().setConfigProfileFormParagraphEditor(wControl.getWindowBackOffice().getWindow().getGuiTheme());		
		rte.getEditorConfiguration().setPathInStatusBar(false);
		// Add to form and finish
		formLayout.add(rte);
		return rte;
	}
	
	/**
	 * 
	 * This is a version with olat media only. The tiny media is disabled because we need to catch the object
	 * tag use by QTI and interpret it as a olat video. It enable the strict uri validation for file names.
	 * 
	 * @param name
	 * @param i18nLabel
	 * @param initialHTMLValue
	 * @param rows
	 * @param cols
	 * @param baseContainer
	 * @param formLayout
	 * @param usess
	 * @param wControl
	 * @return
	 */
	public RichTextElement addRichTextElementForQTI21(String name, String i18nLabel, String initialHTMLValue, int rows,
			int cols, VFSContainer baseContainer, FormItemContainer formLayout, UserSession usess, WindowControl wControl) {
		// Create rich text element with bare bone configuration
		RichTextElement rte = new RichTextElementImpl(name, initialHTMLValue, rows, cols, formLayout.getRootForm(), formLayout.getTranslator().getLocale());
		setLabelIfNotNull(i18nLabel, rte);
		// Now configure editor
		Theme theme = wControl.getWindowBackOffice().getWindow().getGuiTheme();
		rte.getEditorConfiguration().setConfigProfileFormCompactEditor(usess, theme, baseContainer);
		rte.getEditorConfiguration().setInvalidElements(RichTextConfiguration.INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE_WITH_SCRIPT);
		rte.getEditorConfiguration().setExtendedValidElements("script[src|type|defer]");
		rte.getEditorConfiguration().disableTinyMedia();
		rte.getEditorConfiguration().setFilenameUriValidation(true);
		rte.getEditorConfiguration().setFigCaption(false);
		// Add to form and finish
		formLayout.add(rte);
		return rte;
	}
	
	public RichTextElement addRichTextElementForQTI21Match(String name, String i18nLabel, String initialHTMLValue, int rows,
			int cols, VFSContainer baseContainer, FormItemContainer formLayout, UserSession usess, WindowControl wControl) {
		// Create rich text element with bare bone configuration
		RichTextElement rte = new RichTextElementImpl(name, initialHTMLValue, rows, cols, formLayout.getRootForm(), formLayout.getTranslator().getLocale());
		setLabelIfNotNull(i18nLabel, rte);
		// Now configure editor
		Theme theme = wControl.getWindowBackOffice().getWindow().getGuiTheme();
		rte.getEditorConfiguration().setConfigProfileFormVeryMinimalisticConfigEditor(usess, theme, baseContainer);
		rte.getEditorConfiguration().setInvalidElements(RichTextConfiguration.INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE_WITH_SCRIPT);
		rte.getEditorConfiguration().setExtendedValidElements("script[src|type|defer]");
		rte.getEditorConfiguration().disableTinyMedia();
		rte.getEditorConfiguration().setFilenameUriValidation(true);
		rte.getEditorConfiguration().setFigCaption(false);
		// Add to form and finish
		formLayout.add(rte);
		return rte;
	}

	/**
	 * Add a rich text formattable element that offers complex formatting
	 * functionality and loads the data from the given file path. Use
	 * item.getEditorConfiguration() to add more editor features if you need
	 * them
	 * 
	 * @param name
	 *            Name of the form item
	 * @param i18nLabel
	 *            The i18n key of the label or NULL when no label is used
	 * @param initialValue
	 *            The initial value or NULL if no initial value is available
	 * @param rows
	 *            The number of lines the editor should offer. Use -1 to
	 *            indicate no specific height
	 * @param cols
	 *            The number of characters width the editor should offer. Use -1
	 *            to indicate no specific width
	 * @param externalToolbar
	 *            true: use an external toolbar that is only visible when the
	 *            user clicks into the text area; false: use the static toolbar
	 * @param baseContainer
	 *            The VFS container where to load resources from (images etc) or
	 *            NULL to not allow embedding of media files at all
	 * @param relFilePath
	 *            The path to the file relative to the baseContainer
	 * @param customLinkTreeModel
	 *            A custom link tree model or NULL not not use a custom model
	 * @param formLayout
	 *            The form item container where to add the rich text element
	 * @param usess
	 *            The user session that dispatches the images
	 * @param wControl
	 *            the current window controller
	 * @return The richt text element instance
	 */
	public RichTextElement addRichTextElementForFileData(String name,
			final String i18nLabel, String initialValue, final int rows, int cols,
			VFSContainer baseContainer, String relFilePath,
			CustomLinkTreeModel customLinkTreeModel,
			FormItemContainer formLayout, UserSession usess,
			WindowControl wControl) {
		// Create richt text element with bare bone configuration
		RichTextElement rte = new RichTextElementImpl(name, initialValue, rows, cols, formLayout.getRootForm(), formLayout.getTranslator().getLocale());
		setLabelIfNotNull(i18nLabel, rte);
		// Now configure editor
		rte.getEditorConfiguration().setConfigProfileFileEditor(usess,
				wControl.getWindowBackOffice().getWindow().getGuiTheme(),
				baseContainer, relFilePath, customLinkTreeModel);
		// Add to form and finish
		formLayout.add(rte);
		return rte;
	}
	
	/**
	 * Static text with the error look and feel.
	 * @param name in velocity for <code>$r.render("name")</code>
	 * @param translatedText already translated text that should be displayed.
	 * @return
	 */
	public FormItem createSimpleErrorText(final String name, final String translatedText) {
		FormItem wrapper = new FormItemImpl(name) {
			SimpleFormErrorText mySimpleErrorTextC = new SimpleFormErrorText(name, translatedText);
			
			@Override
			public void validate(List<ValidationStatus> validationResults) {
				// nothing to do 
			}
		
			@Override
			protected void rootFormAvailable() {
			//  nothing to do
		
			}
		
			@Override
			public void reset() {
				// nothing to do
			}
		
			@Override
			protected Component getFormItemComponent() {
				return mySimpleErrorTextC;
			}
		
			@Override
			public void evalFormRequest(UserRequest ureq) {
				// nothing to do
			}
		};
		
		return wrapper; 
	}

	/**
	 * 
	 * @param wControl
	 * @param name
	 * @param tableModel
	 * @param translator
	 * @param formLayout
	 * @return
	 */
	public FlexiTableElement addTableElement(WindowControl wControl, String name, FlexiTableDataModel<?> tableModel,
			Translator translator, FormItemContainer formLayout) {
		FlexiTableElementImpl fte = new FlexiTableElementImpl(wControl, name, translator, tableModel);
		formLayout.add(fte);
		return fte;
	}
	
	public FlexiTableElement addTableElement(WindowControl wControl, String name, FlexiTableDataModel<?> tableModel,
			int pageSize, boolean loadOnInit, Translator translator, FormItemContainer formLayout) {
		FlexiTableElementImpl fte = new FlexiTableElementImpl(wControl, name, translator, tableModel, pageSize, loadOnInit, 0);
		formLayout.add(fte);
		return fte;
	}

	public FlexiTableElement addTableElement(WindowControl wControl, String name, FlexiTableDataModel<?> tableModel,
											 int pageSize, boolean loadOnInit, Translator translator, FormItemContainer formLayout, int minSearchLength) {
		FlexiTableElementImpl fte = new FlexiTableElementImpl(wControl, name, translator, tableModel, pageSize, loadOnInit, minSearchLength);
		formLayout.add(fte);
		return fte;
	}
	
	/**
	 * creates a form link with the given name which acts also as command, i18n
	 * and component name. 
	 * @param name
	 * @param formLayout
	 * @return
	 */
	public FormLink addFormLink(String name, FormItemContainer formLayout) {
		FormLinkImpl fte = new FormLinkImpl(name);
		formLayout.add(fte);
		return fte;
	}

	/**
	 * Add a form link with the option to choose the presentation, the <code>name</code> parameter is taken as 
	 * to be used in <code>$r.render("<name>")</code>, as i18nkey for the link text, and also the cmd string.<p>
	 * If different values are needed for name, i18nkey link text, use the {@link FormUIFactory#addFormLink(String, String, String, FormItemContainer, int)}. This allows also to set
	 * the i18n key for label.  
	 * 
	 * @param name The name of the form element (identifyer), also used as i18n key
	 * @param formLayout
	 * @param presentation See Link.BUTTON etc
	 * @return
	 */
	public FormLink addFormLink(String name, FormItemContainer formLayout, int presentation) {
		FormLinkImpl fte = new FormLinkImpl(name, name, name, presentation);
		if(formLayout != null) {
			formLayout.add(fte);
		}
		return fte;
	}

	/**
	 * 
	 * @param name to be used to render in velocity <code>$r.render("name")</code>
	 * @param i18nLink i18n key for the link text
	 * @param i18nLabel i18n key for the link elements label, maybe <code>null</code>
	 * @param formLayout FormLink is added as element here
	 * @param presentation See Link.BUTTON etc. 
	 * @return
	 */
	public FormLink addFormLink(String name, String i18nLink, String i18nLabel, FormItemContainer formLayout, int presentation){
		FormLinkImpl fte = new FormLinkImpl(name,name,i18nLink,presentation);
		fte.setI18nKey(i18nLink);
		setLabelIfNotNull(i18nLabel, fte);
		if(formLayout != null) {
			formLayout.add(fte);
		}
		return fte;
	}
	
	/**
	 * 
	 * @param name to be used to render in velocity <code>$r.render("name")</code>
	 * @param cmd The cmd to be used
	 * @param i18nLink i18n key for the link text
	 * @param i18nLabel i18n key for the link elements label, maybe <code>null</code>
	 * @param formLayout FormLink is added as element here
	 * @param presentation See Link.BUTTON etc. 
	 * @return
	 */
	public FormLink addFormLink(String name, String cmd, String i18nLink, String i18nLabel, FormItemContainer formLayout, int presentation){
		FormLinkImpl fte = new FormLinkImpl(name, cmd, i18nLink, presentation);
		fte.setI18nKey(i18nLink);
		setLabelIfNotNull(i18nLabel, fte);
		if(formLayout != null) {
			formLayout.add(fte);
		}
		return fte;
	}

	/**
	 * Add a form link with a special css class
	 * 
	 * @param name The name of the form element (identifyer), also used as i18n key
	 * @param formLayout
	 * @param css class
	 * @return
	 */
	public FormLink addFormLink(String name, FormItemContainer formLayout, String customEnabledLinkCSS) {
		FormLinkImpl fte = new FormLinkImpl(name);
		fte.setCustomEnabledLinkCSS(customEnabledLinkCSS);
		if(formLayout != null) {
			formLayout.add(fte);
		}
		return fte;
	}
	
	/**
	 * Add a download link
	 * @param name
	 * @param linkTitle
	 * @param i18nLabel
	 * @param file
	 * @param formLayout
	 * @return
	 */
	public DownloadLink addDownloadLink(String name,  String linkTitle, String i18nLabel, VFSLeaf file, FormItemContainer formLayout) {
		DownloadLinkImpl fte = new DownloadLinkImpl(name);
		fte.setLinkText(linkTitle);
		fte.setDownloadItem(file);
		setLabelIfNotNull(i18nLabel, fte);
		if(formLayout != null) {
			formLayout.add(fte);
		}
		return fte;
	}
	
	/**
	 * Add a download link
	 * @param name
	 * @param linkTitle
	 * @param i18nLabel
	 * @param file
	 * @param formLayout
	 * @return
	 */
	public DownloadLink addDownloadLink(String name,  String linkTitle, String i18nLabel, VFSLeaf file, FlexiTableElement formLayout) {
		DownloadLinkImpl fte = new DownloadLinkImpl(name);
		fte.setLinkText(linkTitle);
		fte.setDownloadItem(file);
		setLabelIfNotNull(i18nLabel, fte);
		if(formLayout != null) {
			((FlexiTableElementImpl)formLayout).addFormItem(fte);
		}
		return fte;
	}
	
	public DownloadLink addDownloadLink(String name,  String linkTitle, String i18nLabel, File file, FlexiTableElement formLayout) {
		DownloadLinkImpl fte = new DownloadLinkImpl(name);
		fte.setLinkText(linkTitle);
		fte.setDownloadItem(file);
		setLabelIfNotNull(i18nLabel, fte);
		if(formLayout != null) {
			((FlexiTableElementImpl)formLayout).addFormItem(fte);
		}
		return fte;
	}
	
	public DownloadLink addDownloadLink(String name,  String linkTitle, String i18nLabel, MediaResource resource, FlexiTableElement formLayout) {
		DownloadLinkImpl fte = new DownloadLinkImpl(name);
		fte.setLinkText(linkTitle);
		fte.setDownloadMedia(resource);
		setLabelIfNotNull(i18nLabel, fte);
		if(formLayout != null) {
			((FlexiTableElementImpl)formLayout).addFormItem(fte);
		}
		return fte;
	}

	/**
	 * add a toggle which handles on/off state itself and can be asked for status
	 * with " isOn() ".
	 * 
	 * @param name the name of the element (identifier), also used as i18n key
	 * @param toggleText null if the i18n key should be used and translated, or a text to be on the toggle
	 * @param formLayout
	 * @param toggledOnCSS a special css class for the on state, or null for default
	 * @param toggledOffCSS a special css class for the off state, or null for default
	 * @return
	 */
	public FormToggle addToggleButton(String name, String toggleText, FormItemContainer formLayout, String toggledOnCSS, String toggledOffCSS) {
		FormToggleImpl fte;
		if (StringHelper.containsNonWhitespace(toggleText)) {
			fte = new FormToggleImpl(name, name, toggleText, Link.NONTRANSLATED);
		} else {
			fte = new FormToggleImpl(name, name, name);
		}
		if (toggledOnCSS != null) fte.setToggledOnCSS(toggledOnCSS);
		if (toggledOffCSS != null) fte.setToggledOffCSS(toggledOffCSS);
		formLayout.add(fte);
		return fte;
	}
	
	/**
	 * Add a file upload element, with a label's i18n key being the same as the <code>name<code>.
	 * If you do not want a label, use the {@link FormUIFactory#addFileElement(String, String, FormItemContainer)} 
	 * method with <code>null</code> value for the <code>i18nKey</code>. 
	 * 
	 * @param name
	 * @param formLayout
	 * @return
	 */
	public FileElement addFileElement(WindowControl wControl, String name, FormItemContainer formLayout) {
		return addFileElement(wControl, name, name, formLayout);
	}
	
		
	/**
	 * Add a file upload element
	 * @param name
	 * @param i18nKey
	 * @param formLayout
	 * @return
	 */
	public FileElement addFileElement(WindowControl wControl, String name, String i18nLabel, FormItemContainer formLayout) {
		FileElement fileElement = new FileElementImpl(wControl, name);
		setLabelIfNotNull(i18nLabel, fileElement);
		formLayout.add(fileElement);
		return fileElement;
	}

	/**
	 * Add a form submit button.
	 * 
	 * @param name the button name (identifyer) and at the same time the i18n key of the button
	 * @param formItemContiner The container where to add the button
	 * @return the new form button
	 */
	public FormSubmit addFormSubmitButton(String name, FormItemContainer formLayout) {
		return addFormSubmitButton(name, name, formLayout);
	}

	/**
	 * Add a form submit button.
	 * 
	 * @param name the button name (identifyer) 
	 * @param i18nKey The display key
	 * @param formItemContiner The container where to add the button
	 * @return the new form button
	 */
	public FormSubmit addFormSubmitButton(String name, String i18nKey, FormItemContainer formLayout) {
		return addFormSubmitButton(null, name, i18nKey, formLayout);
	}
	
	/**
	 * Add a form submit button.
	 * 
	 * @param id A fix identifier for state-less behavior, must be unique or null
	 * @param name the button name (identifier) 
	 * @param i18nKey The display key
	 * @param formItemContiner The container where to add the button
	 * @return the new form button
	 */
	public FormSubmit addFormSubmitButton(String id, String name, String i18nKey, FormItemContainer formLayout) {
		FormSubmit subm = new FormSubmit(id, name, i18nKey);
		formLayout.add(subm);
		return subm;
	}
	
	public FormReset addFormResetButton(String name, String i18nKey, FormItemContainer formLayout) {
		FormReset subm = new FormReset(name, i18nKey);
		formLayout.add(subm);
		return subm;
	}

	/**
	 * Add a form cancel button. You must implement the formCancelled() method
	 * in your FormBasicController to get events fired by this button
	 * 
	 * @param name
	 * @param formLayoutContainer
	 * @param ureq
	 * @param wControl
	 * @return
	 */
	public FormCancel addFormCancelButton(String name, FormItemContainer formLayoutContainer, UserRequest ureq, WindowControl wControl) {
		FormCancel cancel = new FormCancel(name, formLayoutContainer, ureq, wControl);
		formLayoutContainer.add(cancel);
		return cancel;
	}

	public MemoryElement addMemoryView(String name, String i18nLabel, MemoryType type, FormItemContainer formLayout) {
		MemoryElementImpl fte = new MemoryElementImpl(name, type);
		setLabelIfNotNull(i18nLabel, fte);
		formLayout.add(fte);
		return fte;
	}
	
	public ProgressBarItem addProgressBar(String name, String i18nLabel, FormItemContainer formLayout) {
		ProgressBarItem fte = new ProgressBarItem(name);
		setLabelIfNotNull(i18nLabel, fte);
		formLayout.add(fte);
		return fte;
	}
	
	public ProgressBarItem addProgressBar(String name, String i18nLabel, int width, float actual, float max, String unitLabel, FormItemContainer formLayout) {
		ProgressBarItem fte = new ProgressBarItem(name, width, actual, max, unitLabel);
		setLabelIfNotNull(i18nLabel, fte);
		formLayout.add(fte);
		return fte;
	}
	
	public SliderElement addSliderElement(String name, String i18nLabel, FormItemContainer formLayout) {
		SliderElementImpl slider = new SliderElementImpl(name);
		setLabelIfNotNull(i18nLabel, slider);
		formLayout.add(slider);
		return slider;
	}
	
	
	public DropdownItem addDropdownMenu(String name, String i18nLabel, FormItemContainer formLayout, Translator translator) {
		DropdownItem dropdown = new DropdownItem(name, name, translator);
		dropdown.setEmbbeded(true);
		dropdown.setButton(true);
		setLabelIfNotNull(i18nLabel, dropdown);
		formLayout.add(dropdown);
		return dropdown;
	}
	
	public RatingFormItem addRatingItem(String name, String i18nLabel, float initialRating, int maxRating, boolean allowUserInput, FormItemContainer formLayout) {
		RatingFormItem ratingCmp = new RatingFormItem(name, initialRating, maxRating, allowUserInput);
		setLabelIfNotNull(i18nLabel, ratingCmp);
		if(i18nLabel != null) {
			ratingCmp.showLabel(true);
		}
		if(formLayout != null) {
			formLayout.add(ratingCmp);
		}
		return ratingCmp;
	}
	
}
