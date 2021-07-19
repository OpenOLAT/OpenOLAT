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

package org.olat.course.editor;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.course.style.CourseStyleService.IMAGE_LIMIT_KB;
import static org.olat.course.style.CourseStyleService.IMAGE_MIME_TYPES;

import java.io.File;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.Header;
import org.olat.course.style.Header.Builder;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.TeaserImageStyle;
import org.olat.course.style.ui.ColorCategoryChooserController;
import org.olat.course.style.ui.CourseStyleUIFactory;
import org.olat.course.style.ui.HeaderController;
import org.olat.course.tree.CourseEditorTreeNode;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 20 Jun 2021<br>>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class NodeLayoutController extends FormBasicController {
	
	private static final ColorCategorySearchParams SEARCH_PARAMS = ColorCategorySearchParams.builder()
			.withEnabled(Boolean.TRUE)
			.build();
	
	private static final String[] displayOptionsKeys = new String[]{
		CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT,
		CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT,
		CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT,
		CourseNode.DISPLAY_OPTS_TITLE_CONTENT,
		CourseNode.DISPLAY_OPTS_CONTENT};
	
	private SingleSelection displayOptionsEl;
	private SingleSelection teaserImageTypeEl;
	private SingleSelection teaserImageSystemEl;
	private FileElement teaserImageUploadEl;
	private FormLink colorCategoryEl;
	private FormLayoutContainer headerPreviewCont;
	private StaticTextElement noHeaderPreviewEl;
	
	private CloseableCalloutWindowController calloutCtrl;
	private ColorCategoryChooserController colorCategoryChooserCtrl;
	private HeaderController headerCtrl;
	
	private final ICourse course;
	private final CourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	private ImageSource teaserImageSource;
	private ColorCategory colorCategory;
	private ColorCategory inheritedColorCategory;
	private TeaserImageStyle teaserImageStyle;
	
	
	@Autowired
	private CourseStyleService courseStyleService;

	public NodeLayoutController(UserRequest ureq, WindowControl wControl, CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CourseStyleUIFactory.class, getLocale(), getTranslator()));
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		this.teaserImageSource = courseNode.getTeaserImageSource();
		course = CourseFactory.loadCourse(userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry());
		teaserImageStyle = course.getCourseConfig().getTeaserImageStyle();
		if (teaserImageStyle == null) {
			teaserImageStyle = TeaserImageStyle.gradient;
		}
		
		initForm(ureq);
		updateTeaserImageUI();
		doSetColorCategory(courseNode.getColorCategoryIdentifier());
		updateHeaderPreviewUI(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.layout");
		
		String[] values = new String[]{
				translate("nodeConfigForm.short_title_desc_content"),
				translate("nodeConfigForm.title_desc_content"),
				translate("nodeConfigForm.short_title_content"),
				translate("nodeConfigForm.title_content"),
				translate("nodeConfigForm.content_only")};
		displayOptionsEl = uifactory.addDropdownSingleselect("displayOptions", "nodeConfigForm.display_options",
				formLayout, displayOptionsKeys, values, null);
		displayOptionsEl.addActionListener(FormEvent.ONCHANGE);
		for(String displayOptionsKey:displayOptionsKeys) {
			if(displayOptionsKey.equals(courseNode.getDisplayOption())) {
				displayOptionsEl.select(displayOptionsKey, true);
			}
		}
		
		SelectionValues teaserImageTpeKV = new SelectionValues();
		teaserImageTpeKV.add(entry(ImageSourceType.course.name(), translate("teaser.image.type.course")));
		teaserImageTpeKV.add(entry(ImageSourceType.courseNode.name(), translate("teaser.image.type.upload")));
		teaserImageTpeKV.add(entry(ImageSourceType.system.name(), translate("teaser.image.type.system")));
		teaserImageTypeEl = uifactory.addRadiosHorizontal("teaser.image.type", formLayout, teaserImageTpeKV.keys(), teaserImageTpeKV.values());
		teaserImageTypeEl.addActionListener(FormEvent.ONCHANGE);
		ImageSourceType type = teaserImageSource != null ? teaserImageSource.getType() : ImageSourceType.course;
		teaserImageTypeEl.select(type.name(), true);
		
		SelectionValues teaserImageKV = new SelectionValues();
		courseStyleService.getSystemTeaserImageSources().stream().forEach(
				source -> teaserImageKV.add(entry(source.getFilename(), source.getFilename())));
		teaserImageKV.sort(SelectionValues.VALUE_ASC);
		teaserImageSystemEl = uifactory.addDropdownSingleselect("teaser.image.system", formLayout, teaserImageKV.keys(), teaserImageKV.values());
		teaserImageSystemEl.addActionListener(FormEvent.ONCHANGE);
		if (teaserImageSource != null && ImageSourceType.system == teaserImageSource.getType()) {
			if (teaserImageSystemEl.containsKey(teaserImageSource.getFilename())) {
				teaserImageSystemEl.select(teaserImageSource.getFilename(), true);
			} else {
				teaserImageTypeEl.select(ImageSourceType.course.name(), true);
				teaserImageSystemEl.setVisible(false);
			}
		}
		
		teaserImageUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "teaser.image.upload", formLayout);
		teaserImageUploadEl.setMaxUploadSizeKB(IMAGE_LIMIT_KB, null, null);
		teaserImageUploadEl.addActionListener(FormEvent.ONCHANGE);
		teaserImageUploadEl.limitToMimeType(IMAGE_MIME_TYPES, "error.mimetype", new String[]{ IMAGE_MIME_TYPES.toString()} );
		if (ImageSourceType.courseNode.name().equals(teaserImageTypeEl.getSelectedKey())) {
			ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry());
			VFSLeaf image = courseStyleService.getImage(course, courseNode);
			if (image instanceof LocalFileImpl) {
				teaserImageUploadEl.setInitialFile(((LocalFileImpl)image).getBasefile());
			}
		}
		
		colorCategoryEl = uifactory.addFormLink("color.category", "color.category", "", translate("color.category"),
				formLayout, Link.NONTRANSLATED);
		colorCategoryEl.setElementCssClass("o_colcal_ele");
		
		String page = Util.getPackageVelocityRoot(HeaderController.class) + "/header_preview.html"; 
		headerPreviewCont = FormLayoutContainer.createCustomFormLayout("preview.header", getTranslator(), page);
		headerPreviewCont.setLabel("preview.header", null);
		formLayout.add(headerPreviewCont);
		
		noHeaderPreviewEl = uifactory.addStaticTextElement("no.header.preview", "preview.header",
				translate("preview.header.no.header"), formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("nodeConfigForm.save", buttonLayout)
			.setElementCssClass("o_sel_node_editor_submit");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == displayOptionsEl) {
			updateHeaderPreviewUI(ureq);
		} else if (source == teaserImageTypeEl) {
			updateTeaserImageUI();
			updateHeaderPreviewUI(ureq);
		} else if (source == teaserImageSystemEl) {
			updateHeaderPreviewUI(ureq);
		} else if (source == teaserImageUploadEl) {
			updateHeaderPreviewUI(ureq);
		} else if (source == colorCategoryEl) {
			doChooseColorCategory(ureq);
			updateHeaderPreviewUI(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (colorCategoryChooserCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doSetColorCategory(colorCategoryChooserCtrl.getColorCategory().getIdentifier());
				updateHeaderPreviewUI(ureq);
			}
			calloutCtrl.deactivate();
			cleanUp();
			flc.setDirty(true);
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(colorCategoryChooserCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		colorCategoryChooserCtrl = null;
		calloutCtrl = null;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(!displayOptionsEl.isOneSelected()) {
			displayOptionsEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		teaserImageUploadEl.clearError();
		if (teaserImageUploadEl.isVisible()) {
			if (teaserImageUploadEl.getUploadFile() == null && teaserImageUploadEl.getInitialFile() == null) {
				teaserImageUploadEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		courseNode.setDisplayOption(displayOptionsEl.getSelectedKey());
		
		ImageSourceType type =  teaserImageTypeEl.isOneSelected()
				? ImageSourceType.toEnum(teaserImageTypeEl.getSelectedKey())
				: ImageSourceType.course;
		if (ImageSourceType.course == type) {
			teaserImageSource = null;
		} else if (ImageSourceType.system == type && teaserImageSystemEl.isOneSelected()) {
			teaserImageSource = courseStyleService.getSystemTeaserImageSource(teaserImageSystemEl.getSelectedKey());
		} else if (ImageSourceType.courseNode == type) {
			if (teaserImageUploadEl.getUploadFile() != null) {
				teaserImageSource = courseStyleService.storeImage(course, courseNode, getIdentity(),
						teaserImageUploadEl.getUploadFile(), teaserImageUploadEl.getUploadFileName());
			}
		}
		courseNode.setTeaserImageSource(teaserImageSource);
		
		if (ImageSourceType.courseNode != type) {
			ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry());
			courseStyleService.deleteImage(course, courseNode);
		}
		
		String colorCategoryIdentifier = colorCategory != null? colorCategory.getIdentifier(): null;
		courseNode.setColorCategoryIdentifier(colorCategoryIdentifier);
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void updateTeaserImageUI() {
		ImageSourceType type = teaserImageTypeEl.isOneSelected()
				? ImageSourceType.toEnum(teaserImageTypeEl.getSelectedKey())
				: ImageSourceType.course;
		teaserImageUploadEl.setVisible(ImageSourceType.courseNode == type);
		teaserImageSystemEl.setVisible(ImageSourceType.system == type);
	}
	
	private void doChooseColorCategory(UserRequest ureq) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(colorCategoryChooserCtrl);
		
		ColorCategory inheritedColorCategory = getInheritedColorCategory();
		colorCategoryChooserCtrl = new ColorCategoryChooserController(ureq, getWindowControl(), SEARCH_PARAMS, inheritedColorCategory);
		listenTo(colorCategoryChooserCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				colorCategoryChooserCtrl.getInitialComponent(), colorCategoryEl.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doSetColorCategory(String identifier) {
		colorCategory = courseStyleService.getColorCategory(identifier, ColorCategory.IDENTIFIER_FALLBACK_COURSE_NODE);
		String categoryName;
		String iconLeftCss;
		if (ColorCategory.IDENTIFIER_INHERITED.equals(colorCategory.getIdentifier())) {
			ColorCategory inheritedColorCategory = getInheritedColorCategory();
			categoryName = CourseStyleUIFactory.translateInherited(getTranslator(), inheritedColorCategory);
			iconLeftCss = CourseStyleUIFactory.getIconLeftCss(inheritedColorCategory);
		} else {
			categoryName = CourseStyleUIFactory.translate(getTranslator(), colorCategory);
			iconLeftCss = CourseStyleUIFactory.getIconLeftCss(colorCategory);
		}
		colorCategoryEl.setI18nKey(categoryName);
		colorCategoryEl.setIconLeftCSS(iconLeftCss);
	}

	private ColorCategory getInheritedColorCategory() {
		if (inheritedColorCategory == null) {
			CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
			inheritedColorCategory = courseStyleService.getColorCategoryResolver(null, course.getCourseConfig().getColorCategoryIdentifier())
					.getInheritedColorCategory(editorTreeNode);
		}
		return inheritedColorCategory;
	}
	
	private void updateHeaderPreviewUI(UserRequest ureq) {
		removeAsListenerAndDispose(headerCtrl);
		headerCtrl = null;
		
		Header header = createPreviewHeader();
		if (CourseStyleUIFactory.hasValues(header)) {
			headerCtrl = new HeaderController(ureq, getWindowControl(), header);
			listenTo(headerCtrl);
			headerPreviewCont.put("header", headerCtrl.getInitialComponent());
			headerPreviewCont.setVisible(true);
			noHeaderPreviewEl.setVisible(false);
		} else {
			headerPreviewCont.setVisible(false);
			noHeaderPreviewEl.setVisible(true);
		}
	}
	
	private Header createPreviewHeader() {
		Builder builder = Header.builder();
		
		String displayOption = displayOptionsEl.getSelectedKey();
		if (CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getShortTitle());
		} else if (CourseNode.DISPLAY_OPTS_TITLE_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getLongTitle());
		} else if (CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getShortTitle());
		} else if (CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {
			builder.withTitle(courseNode.getLongTitle());
		}
		builder.withObjectives(courseNode.getObjectives());
		builder.withInstruction(courseNode.getInstruction());
		builder.withInstrucionalDesign(courseNode.getInstructionalDesign());
		
		String colorCategoryCss = ColorCategory.IDENTIFIER_INHERITED.equals(colorCategory.getIdentifier())
					? getInheritedColorCategory().getCssClass()
					: colorCategory.getCssClass();
		builder.withColorCategoryCss(colorCategoryCss);
		
		Mapper mapper = null;
		if (teaserImageUploadEl.isVisible()) {
			if (teaserImageUploadEl.getUploadFile() != null) {
				mapper = new VFSMediaMapper(teaserImageUploadEl.getUploadFile());
			} else if (teaserImageUploadEl.getInitialFile() != null) {
				mapper = new VFSMediaMapper(teaserImageUploadEl.getUploadFile());
			}
		} else if (teaserImageSystemEl.isVisible() && teaserImageSystemEl.isOneSelected()) {
			File file = courseStyleService.getSystemTeaserImageFile(teaserImageSystemEl.getSelectedKey());
			if (file != null) {
				mapper = new VFSMediaMapper(file);
			}
		} else {
			ImageSource courseImageSource = course.getCourseConfig().getTeaserImageSource();
			if (courseImageSource != null) {
				if (ImageSourceType.course == courseImageSource.getType()) {
					VFSLeaf vfsLeaf = courseStyleService.getImage(course);
					if (vfsLeaf != null) {
						mapper = new VFSMediaMapper(vfsLeaf);
					}
				} else if (ImageSourceType.system == courseImageSource.getType()) {
					File file = courseStyleService.getSystemTeaserImageFile(courseImageSource.getFilename());
					if (file != null) {
						mapper = new VFSMediaMapper(file);
					}
				}
			}
		}
		if (mapper != null) {
			builder.withTeaserImage(mapper, teaserImageStyle);
		}
		
		return builder.build();
	}

}
