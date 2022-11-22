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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Container;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormDecorator;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.translator.Translator;

/**
 * <h2>Summary:</h2>
 * <b>setting panels content</b> is optional and <tt>params</tt> can always be <tt>null</tt>
 * <ul>
 * <li><tt>set<i>Xyz</i>Key(String i18nKey, String[] params)</tt></li>
 * <li><tt>setErrorComponent(FormItem quickFixWizard)</tt></li>
 * </ul>
 * <b>accessing different panels</b> within velocity of a form item:
 * <ul>
 * <li><tt>$r.render("<i>formElementName</i>_LABEL</tt>")</li>
 * <li><tt>$r.render("<i>formElementName</i>_ERROR</tt>")</li>
 * <li><tt>$r.render("<i>formElementName</i>_EXAMPLE</tt>")</li>
 * </ul>
 * find out about <b>form item attributes</b> by querying it from the velocity
 * page with:
 * <ul>
 * <li><tt>$f.isMandatory("<i>formElementName</i></tt>")</li>
 * <li><tt>$f.hasError("<i>formElementName</i></tt>")</li>
 * <li><tt>$f.hasExample("<i>formElementName</i></tt>")</li>
 * <li><tt>$f.hasLabel("<i>formElementName</i></tt>")</li>
 * </ul>
 * whereas <tt>$f.</tt> is the {@link FormDecorator} instance in the velocity.
 * <p>
 * 
 * <h2>Description:</h2>
 * Each implemented form item consists of 4 panels appearing as div's in the
 * html rendered page. They belong together in a <i>logical way</i>, but are
 * <i>layoutable</i>, e.g. placeable anywhere within a velocity page. The 
 * following figure illustrates the logical composition.<br>
 * <img src="doc-files/fi_mainlayout.png"/><br>
 * <ul>
 * <li>the Element panel<br>
 * The form element panel holds for example one or more checkboxes, a
 * dropdownbox, an input field, ... any other html form element, a link!, a
 * custom element!</li>
 * <li>the Label panel<br>
 * textlabel for the element</li>
 * <li>the Error panel<br>
 * Either a textlabel as error text or another form element. This form element
 * can then start a subworkflow explaining the error or for fixing the error.</li>
 * <li>the Example panel<br>
 * example text, explaing an input format or how to proceed with the shown
 * element. </li>
 * </ul>
 * The following figure shows a concrete example of a yes-no switch, the form
 * element is labeled "Depending on date". The screenshot is taken in <i>debug
 * mode mini</i> which is available in development mode only.
 * SingleSelectionImpl is the FormItem implemenation in this case.<br>
 * <img src="doc-files/fi_layout.png"/>
 * <p>
 * The java code to create the above yes-no-switch looks as follows. Please note
 * the anonymous class creation style, with a initializer block. This helps
 * grouping the code: <img src="doc-files/fi_dateswitchcode.png"/><br>
 * <ul>
 * <li>519: <tt>dateSwitch</tt> is the name of the element in the velocity
 * container</li>
 * <li>521/522: <tt>form.easy.dateSwitch</tt> is the i18n key for labeling,
 * and the label is made visible.</li>
 * <li>523/524: sets the i18n keys for yes and no, and selects no as default</li>
 * <li>528: add an onclick listener, so you get informed if yes or no is
 * clicked</li>
 * <li>529: final step of adding the form element to form (layout), it is then
 * available in the velocity page layouting the form under the name
 * <tt>dateSwitch</tt></li>
 * </ul>
 * 
 * <p>
 * The form item goes to a velocity page under defined name, e.g.
 * <tt>dateSwitch</tt>, and the corresponding logical panels are accessed
 * with the following convention:
 * <ul>
 * <li><tt><i>form element name</i>_LABEL</tt></li>
 * <li><tt><i>form element name</i>_ERROR</tt></li>
 * <li><tt><i>form element name</i>_EXAMPLE</tt></li>
 * </ul>
 * <img src="doc-files/fi_velocity.png"/><br>
 * It is very common that a layout needs information about whether a form item
 * has an error, example, label or is mandatory. This information is available
 * through a {@link org.olat.core.gui.components.form.flexible.FormDecorator}
 * which can be accessed for example as <tt>$f.hasError("dateSwitch")</tt>.
 * <P>
 * The {@link FormItem} is aggregated within {@link FormItemContainer} and both
 * together form the <i>composite</i> pattern.<br>
 * They play together with the {@link Component} and {@link Container}
 * <p>
 * 
 * Initial Date: 24.11.2006 <br>
 * 
 * @author patrickb
 */
public interface FormItem extends FormBaseComponentIdProvider {
	public static final String ERRORC = "_ERROR";

	public static final String EXAMPLEC = "_EXAMPLE";

	public static final String LABELC = "_LABEL";
	
	/**
	 * called if just the form values must be remembered for the next render
	 * process. Do not validate data and create error messages.
	 * <p>
	 * This method must be implemented by a specialised form item provider.
	 * 
	 * @param ureq
	 * @param dispatchIDs
	 * @param nextPos
	 */
	public void evalFormRequest(UserRequest ureq);

	/**
	 * Validate the data in the field, create error messages or update any
	 * component.
	 * 
	 * @return true if the data can be safely saved
	 */
	public boolean validate();

	/**
	 * reset the data in the field to a initial/predefined value. This method is 
	 * called if in a form a reset request is issued. It is the counterpart to 
	 * the validate call which  
	 */
	public void reset();
	
	/**
	 * true if the element should (try to) get focus, false is default
	 * @param hasFocus
	 */
	public void setFocus(boolean hasFocus);
	/**
	 * true if this element tries to get focus.
	 * @return
	 */
	public boolean hasFocus();
	
	public String getElementCssClass();
	
	public void setElementCssClass(String cssClass);
	
	/**
	 * called if this component is dispatched, e.g. was clicked (double clicked,
	 * ...)
	 * <p>
	 * Please note, that the caller is not the GUI Framework but the form items
	 * manageing class. A default and exemplary implemention is {@link Form}.
	 * <p>
	 * This method must be implemented by a specialised form item provider.
	 * 
	 * @param ureq
	 * @param dispatchIDs
	 * @param nextPos
	 */
	public void doDispatchFormRequest(UserRequest ureq);

	/**
	 * The form item's name within an managing form container. This has nothing
	 * to do with a name displayed on the screen to the user.<br>
	 * The default implementation {@link FormItemImpl} uses the name to make the
	 * element available in the velocity container.
	 * <p>
	 * This method is used by a form infrastructure provider.
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * The id of the item, can be null
	 */
	public String getFormItemId();
	
	/**
	 * Return the id for the attribute for of the label or null
	 * if the component isn't an HTML form control.
	 */
	public String getForId();

	/**
	 * The elements panel to be rendered by the GUI Framework.
	 * 
	 * @return
	 */
	public Component getComponent();
	
	

	/**
	 * a form item always has a root container where it belongs to. The root
	 * container is responsible for the opening and closing HTML FORM tag.
	 * <p>
	 * This method must be implemented by a specialised form item provider.
	 * 
	 * @return
	 */
	public Form getRootForm();

	/**
	 * label panel of the form item, <code>null</code> if no label
	 * provided/rendered
	 * <p>
	 * This method is used by a form infrastructure provider.
	 * 
	 * @return
	 */
	public Component getLabelC();

	/**
	 * label panel of the form item, <code>null</code> if no label
	 * provided/rendered
	 * <p>
	 * This method is used by a form infrastructure provider.
	 * 
	 * @return
	 */
	public String getLabelText();

	/**
	 * Key to be translated with the form translator and placed in the label
	 * panel.
	 * 
	 * @param labelkey
	 *            i18n key
	 * @param params
	 *            i18n key parameters
	 */
	public void setLabel(String labelkey, String[] params);
	
	public void setLabel(String labelkey, String[] params, boolean translate);
	
	/**
	 * true if the form item should contain a (valid) value.
	 * 
	 * @return
	 */
	public boolean isMandatory();

	/**
	 * Shows a <i>mandatory</i> icon next to this form item. This is only a GUI cue, it does not activate any
	 * validators. It is in the developer's responsibility to validate the form. 
	 * 
	 * @param isMandatory Whether this form item should have a <i>mandatory</i> icon.
	 */
	public void setMandatory(boolean isMandatory);
	
	/**
	 * Sets the i18n key for this form item's error message and displays the error message if showErro(true) is set.
	 * 
	 * @param errorKey i18n key for the error message.
	 * @param params Additional error message contents. 
	 */
	public void setErrorKey(String errorKey, String[] params);

	/**
	 * Sets the i18n key for this form item's error message and displays the error message if showErro(true) is set.
	 * Further you can specify whether it is a warning or an error
	 * 
	 * @param errorKey
	 * @param isWarning
	 * @param params
	 */
	public void setErrorKey(String errorKey, boolean isWarning, String... params);

	/**
	 * a complex "error" message, or a helper wizard to fix the error may be
	 * <p>
	 * This method is used by a form infrastructure provider. <code>null</code>
	 */
	public Component getErrorC();
	
	/**
	 * translated example text, wrapped in component
	 * <p>
	 * This method is used by a form infrastructure provider.
	 * 
	 * @return
	 */
	public Component getExampleC();

	/**
	 * 
	 * <p>
	 * This method is used by a form infrastructure provider.
	 * 
	 * @return
	 */
	public String getExampleText();

	/**
	 * key for example with params to fill
	 * 
	 * @param exampleKey,
	 *            null to clear example
	 * @param params,
	 *            may be null
	 */
	public void setExampleKey(String exampleKey, String[] params);

	/**
	 * Set an optional context help i18n key for this form item
	 * @param helpKey i18n key to be translated with current translator
	 * @param params parameters for i18n key or NULL if no parameters are used
	 */
	public void setHelpTextKey(String helpKey, String[] params);
	
	/**
	 * Set an option context help text for this form item. The help text must be
	 * already translated. Calling this method will override any help text i18n keys.
	 * 
	 * @param helpText The context help text or NULL to use no context help
	 */
	public void setHelpText(String helpText);
	
	/**
	 * Get the translated context help text string for this form item
	 * @return The help text or NULL if no help text is available
	 */
	public String getHelpText();

	/**
	 * Set an optional context help URL to link to external help resources
	 * @param helpUrl An absolute URL with protocol handler etc. 
	 */
	public void setHelpUrl(String helpUrl);

	/**
	 * Set an optional context reference from the official manual. This
	 * generates a link to the OpenOlat-docs server. The HelpLinkSPI is used to
	 * generate the actual help URL from this alias name.
	 * 
	 * @param manualAliasName
	 *            The help page alias.
	 */
	public void setHelpUrlForManualPage(String manualAliasName);
	
	/**
	 * @return The link to an external help for this form item or NULL if no
	 *         help link is available.
	 */
	public String getHelpUrl();
	
	/**
	 * 
	 * @param translator
	 */
	public void setTranslator(Translator translator);

	/**
	 * 
	 * @return
	 */
	public Translator getTranslator();

	/**
	 * item, label, example, error get not visible
	 * 
	 * @param isVisible
	 */
	public void setVisible(boolean isVisible);
	
	/**
	 * true if form item was marked visible.
	 *
	 */
	public boolean isVisible();

	/**
	 * item is enabled/disabled, e.g. editable/read only in contrast to
	 * setVisible this does NOT DISABLE label, example, error!
	 * 
	 * @param isEnabled
	 */
	public void setEnabled(boolean isEnabled);
	
	/**
	 * true if item is marked as editable.
	 * @return
	 */
	public boolean isEnabled();

	/**
	 * 
	 * @return
	 */
	public boolean hasError();

	/**
	 * 
	 * @return
	 */
	public boolean hasWarning();
	
	/**
	 * 
	 * @return
	 */
	public boolean hasLabel();

	/**
	 * 
	 * @return
	 */
	public boolean hasExample();

	/**
	 * 
	 * @param rootForm
	 */
	public void setRootForm(Form rootForm);

	/**
	 * 
	 * @param show
	 */
	public void showLabel(boolean show);

	/**
	 * 
	 * @param show
	 */
	public void showError(boolean show);
	
	/**
	 * error is resolved, e.g. hasError() should return false
	 * and the error component gets invisible (and resetted)
	 *
	 */
	public void clearError();
	
	/**
	 * 
	 * @param show
	 */
	public void showExample(boolean show);

	/**
	 * 
	 * @param listener
	 * @param events
	 */
	public void addActionListener(int events);

	/**
	 * 
	 * @return
	 */
	public int getAction();

	/**
	 * 
	 * @param userObject
	 */
	public void setUserObject(Object userObject);

	/**
	 * 
	 * @return
	 */
	public Object getUserObject();

}
