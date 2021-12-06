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
package org.olat.course.style.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.util.ArrayHelper.emptyStrings;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.CourseModule;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.Header;
import org.olat.course.style.Header.Builder;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.TeaserImageStyle;
import org.olat.course.style.ui.SystemImageDataModel.SystemImageCols;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 July 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SystemImageAdminController extends FormBasicController {
	
	private static final String CMD_EDIT = "edit";
	private static final String CMD_DELETE = "delete";
	
	private FormLayoutContainer styleCont;
	private SingleSelection teaserImageTypeEl;
	private SingleSelection teaserImageSystemEl;
	private SingleSelection teaserImageStyleEl;
	private FormLayoutContainer headerPreviewCont;
	private FormLink addSystemImageLink;
	private FlexiTableElement tableEl;
	private SystemImageDataModel dataModel;
	
	private CloseableModalController cmc;
	private SystemImageEditController editCtrl;
	private DialogBoxController deleteDialogCtrl;
	private HeaderController headerCtrl;
	
	@Autowired
	private CourseStyleService courseStyleService;
	@Autowired
	private CourseModule courseModule;

	public SystemImageAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		initForm(ureq);
		loadModel(ureq);
		updateTeaserImageUI();
		updateHeaderPreviewUI(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		styleCont = FormLayoutContainer.createDefaultFormLayout("node.style", getTranslator());
		styleCont.setFormTitle(translate("course.style.defaults"));
		styleCont.setRootForm(mainForm);
		formLayout.add("node.style", styleCont);
		
		SelectionValues teaserImageTpeKV = new SelectionValues();
		teaserImageTpeKV.add(entry(ImageSourceType.system.name(), translate("teaser.image.type.system")));
		teaserImageTpeKV.add(entry(ImageSourceType.none.name(), translate("teaser.image.type.none")));
		teaserImageTypeEl = uifactory.addRadiosHorizontal("teaser.image.type", styleCont, teaserImageTpeKV.keys(), teaserImageTpeKV.values());
		teaserImageTypeEl.addActionListener(FormEvent.ONCHANGE);
		ImageSourceType imageSourceType = courseModule.getTeaserImageSourceType() != null
				? courseModule.getTeaserImageSourceType()
						: ImageSourceType.none;
		teaserImageTypeEl.select(imageSourceType.name(), true);
		
		teaserImageSystemEl = uifactory.addDropdownSingleselect("teaser.image.system", styleCont, emptyStrings(), emptyStrings());
		teaserImageSystemEl.addActionListener(FormEvent.ONCHANGE);
		updateTeaserImageSystemUI();
		
		SelectionValues teaserImageStyleKV = new SelectionValues();
		Arrays.stream(TeaserImageStyle.values())
				.filter(style -> !style.isTechnical())
				.forEach(style -> teaserImageStyleKV.add(entry(style.name(), translate(CourseStyleUIFactory.getI18nKey(style)))));
		teaserImageStyleEl = uifactory.addRadiosHorizontal("teaser.image.style", styleCont, teaserImageStyleKV.keys(), teaserImageStyleKV.values());
		teaserImageStyleEl.addActionListener(FormEvent.ONCHANGE);
		TeaserImageStyle teaserImageStyle = courseModule.getTeaserImageStyle() != null
				? courseModule.getTeaserImageStyle()
				: TeaserImageStyle.DEFAULT_COURSE;
		teaserImageStyleEl.select(teaserImageStyle.name(), true);
		
		
		String page = Util.getPackageVelocityRoot(HeaderController.class) + "/header_preview.html"; 
		headerPreviewCont = FormLayoutContainer.createCustomFormLayout("preview.header", getTranslator(), page);
		headerPreviewCont.setLabel("preview.header", null);
		styleCont.add(headerPreviewCont);
		
		FormLayoutContainer libraryCont = FormLayoutContainer.createVerticalFormLayout("library", getTranslator());
		libraryCont.setFormTitle(translate("system.image.title"));
		libraryCont.setRootForm(mainForm);
		formLayout.add(libraryCont);
		
		FormLayoutContainer tableButtonsCont = FormLayoutContainer.createVerticalFormLayout("tableButtons", getTranslator());
		tableButtonsCont.setElementCssClass("o_button_group_right");
		tableButtonsCont.setRootForm(mainForm);
		formLayout.add(tableButtonsCont);
		
		addSystemImageLink = uifactory.addFormLink("system.image.add", tableButtonsCont, Link.BUTTON);
		addSystemImageLink.setIconLeftCSS("o_icon o_icon_add");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemImageCols.preview, new SystemImagePreviewRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemImageCols.filename));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SystemImageCols.translaton));
		DefaultFlexiColumnModel editCol = new DefaultFlexiColumnModel(SystemImageCols.edit.i18nHeaderKey(),
				SystemImageCols.edit.ordinal(), CMD_EDIT,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(SystemImageCols.edit.i18nHeaderKey()), CMD_EDIT), null));
		columnsModel.addFlexiColumnModel(editCol);
		DefaultFlexiColumnModel deleteCol = new DefaultFlexiColumnModel(SystemImageCols.delete.i18nHeaderKey(),
				SystemImageCols.delete.ordinal(), CMD_DELETE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate(SystemImageCols.delete.i18nHeaderKey()), CMD_DELETE), null));
		columnsModel.addFlexiColumnModel(deleteCol);
		
		dataModel = new SystemImageDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-system-image");
	}
	
	private void loadModel(UserRequest ureq) {
		List<ImageSource> systemImageSources = courseStyleService.getSystemTeaserImageSources();
		systemImageSources.sort((i1, i2) -> i1.getFilename().compareToIgnoreCase(i2.getFilename()));
		List<SystemImageRow> rows = new ArrayList<>(systemImageSources.size());
		for (ImageSource systemImageSource: systemImageSources) {
			SystemImageRow row = new SystemImageRow();
			
			String filename = systemImageSource.getFilename();
			row.setFilename(filename);
			String translation = CourseStyleUIFactory.translateSystemImage(getTranslator(), filename);
			row.setTranslation(translation);
			
			File file = courseStyleService.getSystemTeaserImageFile(filename);
			VFSMediaMapper mapper = new VFSMediaMapper(file);
			String mapperUrl = registerMapper(ureq, mapper);
			row.setMapperUrl(mapperUrl);
			
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	private void updateTeaserImageUI() {
		boolean hasDefault = teaserImageTypeEl.isOneSelected() && ImageSourceType.system.name().equals(teaserImageTypeEl.getSelectedKey());
		teaserImageSystemEl.setVisible(hasDefault);
		teaserImageStyleEl.setVisible(hasDefault);
		headerPreviewCont.setVisible(hasDefault);
	}
	
	private void updateTeaserImageSystemUI() {
		SelectionValues teaserImageKV = new SelectionValues();
		courseStyleService.getSystemTeaserImageSources().stream().forEach(
				source -> teaserImageKV.add(entry(
						source.getFilename(),
						CourseStyleUIFactory.translateSystemImage(getTranslator(), source.getFilename()))));
		teaserImageKV.sort(SelectionValues.VALUE_ASC);
		teaserImageSystemEl.setKeysAndValues(teaserImageKV.keys(), teaserImageKV.values(), null);
		
		String teaserImageFilename = courseModule.getTeaserImageFilename();
		if (teaserImageSystemEl.containsKey(teaserImageFilename)) {
			teaserImageSystemEl.select(teaserImageFilename, true);
		} else if (teaserImageSystemEl.getKeys().length > 0) {
			teaserImageSystemEl.select(teaserImageSystemEl.getKey(0), true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == teaserImageTypeEl) {
			doUpdateTeaserImage();
			updateHeaderPreviewUI(ureq);
		} else if (source == teaserImageSystemEl) {
			doUpdateTeaserImage();
			updateHeaderPreviewUI(ureq);
		} else if (source == teaserImageStyleEl) {
			doUpdateTeaserImage();
			updateHeaderPreviewUI(ureq);
		} else if(addSystemImageLink == source) {
			doAddSystemImage(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				SystemImageRow row = dataModel.getObject(se.getIndex());
				if(CMD_EDIT.equals(cmd)) {
					doEditSystemImage(ureq, row.getFilename());
				} else if (CMD_DELETE.equals(cmd)) {
					doConfirmDeletion(ureq, row.getFilename());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (editCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq);
				updateTeaserImageSystemUI();
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(deleteDialogCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				String filename = (String)deleteDialogCtrl.getUserObject();
				doDelete(ureq, filename);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doUpdateTeaserImage() {
		ImageSourceType type = teaserImageTypeEl.isOneSelected()
				? ImageSourceType.toEnum(teaserImageTypeEl.getSelectedKey())
				: ImageSourceType.none;
		courseModule.setTeaserImageSourceType(type);
		
		String filename = teaserImageSystemEl.isOneSelected()? teaserImageSystemEl.getSelectedKey(): null;
		courseModule.setTeaserImageFilename(filename);
		
		TeaserImageStyle teaserImageStyle = teaserImageStyleEl.isOneSelected()
				? TeaserImageStyle.valueOf(teaserImageStyleEl.getSelectedKey())
				: TeaserImageStyle.DEFAULT_COURSE;
		courseModule.setTeaserImageStyle(teaserImageStyle);
		
		updateTeaserImageUI();
	}
	
	private void updateHeaderPreviewUI(UserRequest ureq) {
		removeAsListenerAndDispose(headerCtrl);
		headerCtrl = null;
		
		Header header = createPreviewHeader();
		headerCtrl = new HeaderController(ureq, getWindowControl(), header);
		listenTo(headerCtrl);
		headerPreviewCont.put("header", headerCtrl.getInitialComponent());
	}
	
	private Header createPreviewHeader() {
		Builder builder = Header.builder();
		builder.withIconCss("o_CourseModule_icon");
		builder.withTitle(translate("preview.header.title"));
		
		if (teaserImageSystemEl.isVisible()) {
			File file = courseStyleService.getSystemTeaserImageFile(courseModule.getTeaserImageFilename());
			if (file != null) {
				Mapper mapper = new VFSMediaMapper(file);
				builder.withTeaserImage(mapper, courseModule.getTeaserImageStyle());
			}
		}
		
		return builder.build();
	}

	private void doAddSystemImage(UserRequest ureq) {
		editCtrl = new SystemImageEditController(ureq, getWindowControl(), null);
		listenTo(editCtrl);
		
		String title = translate("system.image.add.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditSystemImage(UserRequest ureq, String filename) {
		editCtrl = new SystemImageEditController(ureq, getWindowControl(), filename);
		listenTo(editCtrl);
		
		String title = translate("system.image.edit.title");
		cmc = new CloseableModalController(getWindowControl(), "close", editCtrl.getInitialComponent(), true,
				title, true);
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirmDeletion(UserRequest ureq, String filename) {
		String title = translate("system.image.delete.confirm.title");
		String translatedFilename = CourseStyleUIFactory.translateSystemImage(getTranslator(), filename);
		String text = translate("system.image.delete.confirm.text", new String[] { translatedFilename, filename });
		deleteDialogCtrl = activateYesNoDialog(ureq, title, text, deleteDialogCtrl);
		deleteDialogCtrl.setUserObject(filename);
	}

	private void doDelete(UserRequest ureq, String filename) {
		courseStyleService.deleteSystemImage(filename);
		loadModel(ureq);
		updateTeaserImageSystemUI();
		flc.setDirty(true);
		if (courseModule.getTeaserImageFilename().equals(filename)) {
			teaserImageTypeEl.select(ImageSourceType.none.name(), true);
			doUpdateTeaserImage();
		}
	}

}
