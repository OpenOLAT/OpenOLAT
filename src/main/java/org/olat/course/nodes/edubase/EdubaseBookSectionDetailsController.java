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
package org.olat.course.nodes.edubase;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.edubase.EdubaseBookSectionListController.BookSectionWrapper;
import org.olat.modules.edubase.BookDetails;

/**
 *
 * Initial date: 22.09.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseBookSectionDetailsController extends FormBasicController {

	private FormLink useTitleEl;

	private final BookSectionWrapper bookSectionWrapper;
	private final BookDetails bookDetails;

	public EdubaseBookSectionDetailsController(UserRequest ureq, WindowControl wControl,
			BookSectionWrapper bookSectionWrapper, BookDetails bookDetails) {
		super(ureq, wControl);
		this.bookSectionWrapper = bookSectionWrapper;
		this.bookDetails = bookDetails;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement(
				"edubase.book.section.details.authors",
				"edubase.book.section.details.authors",
				bookDetails.getAuthors(),
				formLayout);
		uifactory.addStaticTextElement(
				"edubase.book.section.details.title",
				"edubase.book.section.details.title",
				bookDetails.getTitle(),
				formLayout);
		uifactory.addStaticTextElement(
				"edubase.book.section.details.subtitle",
				"edubase.book.section.details.subtitle",
				bookDetails.getSubtitle(),
				formLayout);
		uifactory.addStaticTextElement(
				"edubase.book.section.details.publisher",
				"edubase.book.section.details.publisher",
				bookDetails.getPublisher(),
				formLayout);
		uifactory.addStaticTextElement(
				"edubase.book.section.details.edition",
				"edubase.book.section.details.edition",
				bookDetails.getEdition(),
				formLayout);
		uifactory.addStaticTextElement(
				"edubase.book.section.details.number.pages",
				"edubase.book.section.details.number.pages",
				bookDetails.getNumberOfPages(),
				formLayout);
		uifactory.addStaticTextElement(
				"edubase.book.section.details.isbn",
				"edubase.book.section.details.isbn",
				bookDetails.getIsbn(),
				formLayout);
		uifactory.addStaticTextElement(
				"edubase.book.section.details.description",
				"edubase.book.section.details.description",
				bookDetails.getDescription(),
				formLayout);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("save", getTranslator());
		formLayout.add(buttonLayout);
		if (StringHelper.containsNonWhitespace(bookDetails.getTitle())) {
			useTitleEl = uifactory.addFormLink("edubase.book.section.details.use.title", buttonLayout, Link.BUTTON);
		}
		uifactory.addFormLink("cancel", buttonLayout, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			if (source == useTitleEl) {
				useTitleFromDetails();
			}
			fireEvent(ureq, FormEvent.DONE_EVENT);
		}
	}

	private void useTitleFromDetails() {
		String title = bookDetails.getTitle();
		bookSectionWrapper.getTitleEl().setValue(title);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
