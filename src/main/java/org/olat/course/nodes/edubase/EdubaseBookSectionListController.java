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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.logging.AssertException;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.EdubaseCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.edubase.BookDetails;
import org.olat.modules.edubase.BookSection;
import org.olat.modules.edubase.EdubaseManager;
import org.olat.modules.edubase.model.BookSectionImpl;
import org.olat.modules.edubase.model.PositionComparator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 21.06.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdubaseBookSectionListController extends FormBasicController {

	private static final String BOOK_ID_PREFIX = "book-id";
	private static final String ADD_PREFIX = "add";
	private static final String RM_PREFIX = "rm";
	private static final String DOWN_PREFIX = "down";
	private static final String UP_PREFIX = "up";
	private static final String DETAILS_PREFIX = "details";
	private static final String PAGE_FROM_PREFIX = "page-from";
	private static final String PAGE_TO_PREFIX = "page-to";
	private static final String DESC_PREFIX = "desc";

	private final ModuleConfiguration config;

	private int countBookSections = 0;
	private final List<BookSectionWrapper> bookSectionWrappers = new ArrayList<>();
	private FormLayoutContainer bookSectionsCont;
	private CloseableModalController closeableModalController;
	private FormBasicController detailsController;

	@Autowired
	private EdubaseManager edubaseManager;

	public EdubaseBookSectionListController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl, LAYOUT_VERTICAL);

		this.config = moduleConfiguration;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.book.section.list");
		setFormContextHelp("Knowledge Transfer#_edubase");

		String page = velocity_root + "/bookSectionList.html";
		bookSectionsCont = FormLayoutContainer.createCustomFormLayout("bookSections", getTranslator(), page);
		bookSectionsCont.setRootForm(mainForm);
		formLayout.add(bookSectionsCont);

		// BookSections
		List<BookSection> bookSections = new ArrayList<>(config.getList(EdubaseCourseNode.CONFIG_BOOK_SECTIONS, BookSectionImpl.class));
		bookSections.stream()
			.map(bs -> edubaseManager.appendCoverUrl(bs))
			.sorted(new PositionComparator())
			.forEach(this::wrapBookSection);
		ensureBookSectionWrappersHaveAnEntry();

		bookSectionsCont.contextPut("bookSections", bookSectionWrappers);
		recalculateUpDownLinks();

		// Submit Button
		FormLayoutContainer buttonLayout = FormLayoutContainer.createDefaultFormLayout_2_10("buttons", getTranslator());
		buttonLayout.setRootForm(mainForm);
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	/**
	 * Check if the bookSectionWrappers has at least one item. If it has no item
	 * a new BookSeactionWrapper is created and added to the
	 * bookSectionWrappers,
	 */
	private void ensureBookSectionWrappersHaveAnEntry() {
		if (bookSectionWrappers.isEmpty()) {
			wrapBookSection(new BookSectionImpl());
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		for (BookSectionWrapper bookSectionWrapper: bookSectionWrappers) {
			String bookId = bookSectionWrapper.getBookIdEl().getValue();
			if(!StringHelper.containsNonWhitespace(bookId)) {
				bookSectionWrapper.getBookIdEl().setErrorKey("form.legende.mandatory", null);
				allOk = false;
			} else {
				boolean isValidBookId = edubaseManager.validateBookId(bookId);
				if (!isValidBookId) {
					bookSectionWrapper.getBookIdEl().setErrorKey("form.error.wrong.section.id", null);
					allOk = false;
				}
			}
			allOk &= validatePositiveInt(bookSectionWrapper.getPageFromEl());
			allOk &= validatePositiveInt(bookSectionWrapper.getPageToEl());
			allOk &= validateToHigherFrom(bookSectionWrapper.getPageFromEl(), bookSectionWrapper.getPageToEl());
		}

		return allOk;
	}

	private boolean validatePositiveInt(TextElement el) {
		boolean allOk = true;

		if(el.isVisible()) {
			String value = el.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Integer intValue = Integer.parseInt(value);
					if (intValue <= 0) throw new AssertException("negativ number");
				} catch(Exception e) {
					el.setErrorKey("form.error.wrong.int", null);
					allOk = false;
				}
			}
		}

		return allOk;
	}
	
	private boolean validateToHigherFrom(TextElement fromEl, TextElement toEl) {
		boolean allOk = true;
		
		try {
			Integer from = Integer.parseInt(fromEl.getValue());
			Integer to = Integer.parseInt(toEl.getValue());
			if (from > 0 && to > 0 && from > to) {
				toEl.setErrorKey("form.error.page.to.higher.from", null);
				allOk = false;
			}
		} catch (Exception e) {
			// validate only if integers
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink button = (FormLink)source;
			String cmd = button.getCmd();
			if(RM_PREFIX.equals(cmd)) {
				doRemoveBookSection((BookSectionWrapper)button.getUserObject());
			} else if(ADD_PREFIX.equals(cmd)) {
				doAddBookSection();
			} else if(UP_PREFIX.equals(cmd)) {
				doMoveBookSectionUp((BookSectionWrapper)button.getUserObject());
			} else if(DOWN_PREFIX.equals(cmd)) {
				doMoveBookSectionDown((BookSectionWrapper)button.getUserObject());
			} else if(DETAILS_PREFIX.equals(cmd)) {
				BookSectionWrapper bookSectionWrapper = (BookSectionWrapper)button.getUserObject();
				showDetails(ureq, bookSectionWrapper);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof EdubaseConfigController && event.equals(Event.DONE_EVENT)) {
			// reload if the descriptions are enabled and show/hide them
			boolean discriptionEnabled = config.getBooleanSafe(EdubaseCourseNode.CONFIG_DESCRIPTION_ENABLED);
			showHideDescriptions(discriptionEnabled);
		} else if (source == detailsController && event.equals(FormEvent.DONE_EVENT)) {
			closeableModalController.deactivate();
			removeAsListenerAndDispose(closeableModalController);
			closeableModalController = null;
			removeAsListenerAndDispose(detailsController);
			detailsController = null;
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		setFormWarning(null);
		setBookIdsAndCovers();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void doDispose() {
		//
	}

	protected ModuleConfiguration getUpdatedConfig() {
		config.setList(EdubaseCourseNode.CONFIG_BOOK_SECTIONS, getBookSections(bookSectionWrappers));
		return config;
	}

	/**
	 * Converts the List of BookSectionWrapper to a List of BookSection. It adds
	 * the position in the list to every BookSection.
	 *
	 * If the the List contains only a dummy BookSectionWrapper it returns an
	 * empty List.
	 *
	 * @param bookSectionWrappers
	 * @return
	 */
	private List<BookSection> getBookSections(List<BookSectionWrapper> bookSectionWrappers) {
		// check if only dummy BookSectionWrapper
		if (bookSectionWrappers.size() == 1
				&& !StringHelper.containsNonWhitespace(bookSectionWrappers.get(0).getBookIdEl().getValue())) {
			return new ArrayList<>();
		}

		for (int i = 0; i < bookSectionWrappers.size(); i++) {
			bookSectionWrappers.get(i).getBookSection().setPosition(i);
		}

		return bookSectionWrappers.stream()
				.map(BookSectionWrapper::getBookSection)
				.collect(Collectors.toList());
	}

	private void showHideDescriptions(boolean show) {
		bookSectionWrappers.stream()
				.map(wrapper -> wrapper.getDescriptionEl())
				.forEach(descEl -> descEl.setVisible(show));
	}

	private void doAddBookSection() {
		BookSection bookSection = new BookSectionImpl();
		wrapBookSection(bookSection);
		flc.setDirty(true);
	}

	private void doRemoveBookSection(BookSectionWrapper bookSectionWrapper) {
		bookSectionWrappers.remove(bookSectionWrapper);
		ensureBookSectionWrappersHaveAnEntry();
		flc.setDirty(true);
	}

	private void doMoveBookSectionUp(BookSectionWrapper bookSectionWrapper) {
		int index = bookSectionWrappers.indexOf(bookSectionWrapper) - 1;
		if(index >= 0 && index < bookSectionWrappers.size()) {
			bookSectionWrappers.remove(bookSectionWrapper);
			bookSectionWrappers.add(index, bookSectionWrapper);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}

	private void doMoveBookSectionDown(BookSectionWrapper bookSectionWrapper) {
		int index = bookSectionWrappers.indexOf(bookSectionWrapper) + 1;
		if(index > 0 && index < bookSectionWrappers.size()) {
			bookSectionWrappers.remove(bookSectionWrapper);
			bookSectionWrappers.add(index, bookSectionWrapper);
		}
		recalculateUpDownLinks();
		flc.setDirty(true);
	}

	private void recalculateUpDownLinks() {
		int numOfBookSections = bookSectionWrappers.size();
		for(int i=0; i<numOfBookSections; i++) {
			BookSectionWrapper bookSectionWrapper = bookSectionWrappers.get(i);
			bookSectionWrapper.getUpLinkEl().setEnabled(i != 0);
			bookSectionWrapper.getDownLinkEl().setEnabled(i < (numOfBookSections - 1));
		}
	}

	private void showDetails(UserRequest ureq, BookSectionWrapper wrapper) {
		String bookId = setParsedBookId(wrapper);
		BookDetails bookDetails = edubaseManager.fetchBookDetails(bookId);
		edubaseManager.appendCoverUrl(wrapper.getBookSection());
		detailsController = new EdubaseBookSectionDetailsController(
				ureq,
				getWindowControl(),
				wrapper,
				bookDetails);
		listenTo(detailsController);
		closeableModalController = new CloseableModalController(
				getWindowControl(),
				translate("close"),
				detailsController.getInitialComponent(),
				true,
				translate("edubase.book.section.details.form.title"));
		listenTo(closeableModalController);
		closeableModalController.activate();
	}

	private void setBookIdsAndCovers() {
		for (BookSectionWrapper wrapper: bookSectionWrappers) {
			setParsedBookId(wrapper);
			edubaseManager.appendCoverUrl(wrapper.getBookSection());
		}
	}

	private String setParsedBookId(BookSectionWrapper wrapper) {
		String bookId = edubaseManager.parseBookId(wrapper.getBookIdEl().getValue());
		wrapper.getBookIdEl().setValue(bookId);
		return bookId;
	}

	private void wrapBookSection(BookSection bookSection) {
		String bookSectionId = "" + countBookSections++;

		// remove
		FormLink removeLink = uifactory.addFormLink(RM_PREFIX.concat(bookSectionId), RM_PREFIX, "", null, bookSectionsCont,
				Link.NONTRANSLATED);
		removeLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete");
		bookSectionsCont.add(removeLink);
		bookSectionsCont.add(RM_PREFIX.concat(bookSectionId), removeLink);

		// add
		FormLink addLink = uifactory.addFormLink(ADD_PREFIX.concat(bookSectionId), ADD_PREFIX, "", null, bookSectionsCont,
				Link.NONTRANSLATED);
		addLink.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
		bookSectionsCont.add(addLink);
		bookSectionsCont.add(ADD_PREFIX.concat(bookSectionId), addLink);

		// up
		FormLink upLink = uifactory.addFormLink(UP_PREFIX.concat(bookSectionId), UP_PREFIX, "", null, bookSectionsCont,
				Link.NONTRANSLATED);
		upLink.setIconLeftCSS("o_icon o_icon-lg o_icon_move_up");
		bookSectionsCont.add(upLink);
		bookSectionsCont.add(UP_PREFIX.concat(bookSectionId), upLink);

		// down
		FormLink downLink = uifactory.addFormLink(DOWN_PREFIX.concat(bookSectionId), DOWN_PREFIX, "", null, bookSectionsCont,
				Link.NONTRANSLATED);
		downLink.setIconLeftCSS("o_icon o_icon-lg o_icon_move_down");
		bookSectionsCont.add(downLink);
		bookSectionsCont.add(DOWN_PREFIX.concat(bookSectionId), downLink);

		// Details button
		FormLink detailsLinkEl = uifactory.addFormLink(DETAILS_PREFIX.concat(bookSectionId), DETAILS_PREFIX, "edubase.book.section.details",
				null, bookSectionsCont, Link.BUTTON);
		detailsLinkEl.setElementCssClass("o_edubase_bs_details");
		bookSectionsCont.add(detailsLinkEl);
		bookSectionsCont.add(DETAILS_PREFIX.concat(bookSectionId), detailsLinkEl);

		// book id
		TextElement bookIdEl = uifactory.addTextElement(BOOK_ID_PREFIX.concat(bookSectionId), "edubase.book.section.id", 128,
				bookSection.getBookId(), bookSectionsCont);
		bookIdEl.setMandatory(true);
		bookIdEl.setHelpTextKey("edubase.book.section.id.help", null);

		// page from
		String pageFrom = bookSection.getPageFrom() != null ? Integer.toString(bookSection.getPageFrom()) : null;
		TextElement pageFromEl = uifactory.addTextElement(PAGE_FROM_PREFIX.concat(bookSectionId),
				"edubase.book.section.page.from", 6, pageFrom, bookSectionsCont);

		// page to
		String pageTo = bookSection.getPageTo() != null ? Integer.toString(bookSection.getPageTo()) : null;
		TextElement pageToEl = uifactory.addTextElement(PAGE_TO_PREFIX.concat(bookSectionId),
				"edubase.book.section.page.to", 6, pageTo, bookSectionsCont);

		// title
		TextElement titleEl = uifactory.addTextElement("title".concat(bookSectionId), "edubase.book.section.title",
				128, bookSection.getTitle(), bookSectionsCont);

		// description
		RichTextElement descriptionEl = uifactory.addRichTextElementForStringDataMinimalistic(
				DESC_PREFIX.concat(bookSectionId), "edubase.book.section.description", bookSection.getDescription(), 4, -1,
				bookSectionsCont, getWindowControl());
		boolean discriptionEnabled = config.getBooleanSafe(EdubaseCourseNode.CONFIG_DESCRIPTION_ENABLED);
		descriptionEl.setVisible(discriptionEnabled);

		bookSectionWrappers.add(new BookSectionWrapper(bookSection, removeLink, addLink, upLink, downLink, detailsLinkEl,
				bookIdEl, pageFromEl, pageToEl, titleEl, descriptionEl));
	}

    public final class BookSectionWrapper {

		private final BookSection bookSection;
		private final FormLink removeLinkEl;
		private final FormLink addLinkEl;
		private final FormLink upLinkEl;
		private final FormLink downLinkEl;
		private final FormLink detailsLinkEl;
		private final TextElement bookIdEl;
		private final TextElement pageFromEl;
		private final TextElement pageToEl;
		private final TextElement titleEl;
		private final RichTextElement descriptionEl;

		public BookSectionWrapper(BookSection bookSection, FormLink removeLinkEl, FormLink addLinkEl, FormLink upLinkEl,
				FormLink downLinkEl, FormLink detailsLinkEl, TextElement bookIdEl, TextElement pageFromEl,
				TextElement pageToEl, TextElement titleEl, RichTextElement descriptionEl) {
			this.bookSection = bookSection;
			this.removeLinkEl = removeLinkEl;
			removeLinkEl.setUserObject(this);
			this.addLinkEl = addLinkEl;
			addLinkEl.setUserObject(this);
			this.upLinkEl = upLinkEl;
			upLinkEl.setUserObject(this);
			this.downLinkEl = downLinkEl;
			downLinkEl.setUserObject(this);
			this.detailsLinkEl = detailsLinkEl;
			detailsLinkEl.setUserObject(this);
			this.bookIdEl = bookIdEl;
			bookIdEl.setUserObject(this);
			this.pageFromEl = pageFromEl;
			pageFromEl.setUserObject(this);
			this.pageToEl = pageToEl;
			pageToEl.setUserObject(this);
			this.titleEl = titleEl;
			titleEl.setUserObject(this);
			this.descriptionEl = descriptionEl;
			descriptionEl.setUserObject(this);
		}

		public BookSection getBookSection() {
			bookSection.setBookId(bookIdEl.getValue());
			try {
				bookSection.setPageFrom(Integer.valueOf(pageFromEl.getValue()));
			} catch (Exception e) {
				bookSection.setPageFrom(null);
			}
			try {
				bookSection.setPageTo(Integer.valueOf(pageToEl.getValue()));
			} catch (Exception e) {
				bookSection.setPageTo(null);
			}
			if (StringHelper.containsNonWhitespace(titleEl.getValue())) {
				bookSection.setTitle(titleEl.getValue());
			} else {
				bookSection.setTitle(null);
			}
			if (StringHelper.containsNonWhitespace(descriptionEl.getValue())) {
				bookSection.setDescription(descriptionEl.getValue());
			} else {
				bookSection.setDescription(null);
			}
			return bookSection;
		}

		public FormLink getRemoveLinkEl() {
			return removeLinkEl;
		}

		public FormLink getAddLinkEl() {
			return addLinkEl;
		}

		public FormLink getUpLinkEl() {
			return upLinkEl;
		}

		public FormLink getDownLinkEl() {
			return downLinkEl;
		}

		public FormLink getDetailsLinkEl() {
			return detailsLinkEl;
		}

		public TextElement getBookIdEl() {
			return bookIdEl;
		}

		public TextElement getPageFromEl() {
			return pageFromEl;
		}

		public TextElement getPageToEl() {
			return pageToEl;
		}

		public TextElement getTitleEl() {
			return titleEl;
		}

		public RichTextElement getDescriptionEl() {
			return descriptionEl;
		}
	}

}
