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
package org.olat.ims.qti.editor;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.Mattext;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;

/**
 * Material edit form controller in rich text editor style. All material
 * elements are merged into a single one with a single text element.
 * 
 * @fires Event.CANCELLED_EVENT, Event.DONE_EVENT, QTIObjectBeforeChangeEvent
 *        <P>
 *        Initial Date: Jul 10, 2009 <br>
 * 
 * @author gwassmann
 */
public class MaterialFormController extends FormBasicController {
	private QTIEditorPackage qtiPackage;
	private Material mat;
	private RichTextElement richText;
	private final boolean isRestrictedEditMode;
	private final boolean isBlockedEditMode;
	private String htmlContent = "";

	public MaterialFormController(UserRequest ureq, WindowControl control, Material mat, QTIEditorPackage qtiPackage,
			boolean isRestrictedEditMode, boolean isBlockedEditMode) {
		super(ureq, control, FormBasicController.LAYOUT_VERTICAL);
		this.mat = mat;
		this.qtiPackage = qtiPackage;
		this.htmlContent = mat.renderAsHtmlForEditor();
		this.isBlockedEditMode = isBlockedEditMode;
		this.isRestrictedEditMode = isRestrictedEditMode;
		initForm(ureq);
	}

	@Override
	protected void doDispose() {
	// nothing to get rid of
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String newHtml = richText.getRawValue(); // trust authors, don't to XSS filtering
		//the text fragment is saved in a cdata, remove cdata from movie plugin
		newHtml = newHtml.replace("// <![CDATA[", "").replace("// ]]>", "");
		// Strip unnecessary BR tags at the beginning and the end which are added
		// automaticall by mysterious tiny code and cause problems in FIB questions. (OLAT-4363)
		// Use explicit return which create a P tag if you want a line break.
		if (newHtml.startsWith("<br />") && newHtml.length() > 6) newHtml = newHtml.substring(6);
		if (newHtml.endsWith("<br />") && newHtml.length() > 6) newHtml = newHtml.substring(0, newHtml.length()-6);
		// Remove any conditional comments due to strange behavior in test (OLAT-4518)
		Filter conditionalCommentFilter = FilterFactory.getConditionalHtmlCommentsFilter();
		newHtml = conditionalCommentFilter.filter(newHtml);
		//
		if (htmlContent.equals(newHtml)) {
			// No changes. Cancel editing.
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else {
			if (isRestrictedEditMode) {
				// In restricted edit mode, if the content has changed, write a memento
				// (by firing the before change event).
				QTIObjectBeforeChangeEvent qobce = new QTIObjectBeforeChangeEvent();
				qobce.init(mat.getId(), htmlContent);
				fireEvent(ureq, qobce);
			}
			// Collect the content of all MatElements in a single text element
			// (text/html) and save it (for Material objects with multiple elements
			// such as images, videos, text, breaks, etc. this can be regarded as
			// "lazy migration" to the new rich text style).
			Mattext textHtml = new Mattext(newHtml);
			// A single text/html element will be left over.
			List<QTIObject> elements = new ArrayList<>(1);
			elements.add(textHtml);
			mat.setElements(elements);
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		richText = uifactory.addRichTextElementForStringData("mce", null, htmlContent, 14, -1, true, qtiPackage.getBaseDir(), null,
				formLayout, ureq.getUserSession(), getWindowControl());
		richText.getEditorConfiguration().setFigCaption(false);
		richText.setEnabled(!isBlockedEditMode);

		RichTextConfiguration richTextConfig = richText.getEditorConfiguration();
		// disable <p> element for enabling vertical layouts
		richTextConfig.disableRootParagraphElement();
		// set upload dir to the media dir
		richTextConfig.setFileBrowserUploadRelPath("media");
		// manually enable the source edit button
		richTextConfig.enableCode();
		//allow script tags...
		richTextConfig.setInvalidElements(RichTextConfiguration.INVALID_ELEMENTS_FORM_FULL_VALUE_UNSAVE_WITH_SCRIPT);
		richTextConfig.setExtendedValidElements("script[src|type|defer]");
		
		if(!isBlockedEditMode) {
			uifactory.addFormSubmitButton("submit", formLayout);
		}
	}

	/**
	 * @return The material
	 */
	public Material getMaterial() {
		return mat;
	}
}
