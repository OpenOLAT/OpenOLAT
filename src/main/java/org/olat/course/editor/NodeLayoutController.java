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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormJSHelper;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.CourseNodeHelper;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.Overview;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategory.Type;
import org.olat.course.style.ColorCategoryResolver;
import org.olat.course.style.ColorCategorySearchParams;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.Header;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.TeaserImageStyle;
import org.olat.course.style.ui.ColorCategoryChooserController;
import org.olat.course.style.ui.CourseStyleUIFactory;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 20 Jun 2021<br>>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 */
public class NodeLayoutController extends FormBasicController {
	
	private static final String KEY_TITLE = "long";
	private static final String KEY_DESCRIPTION = "description";
	private static final String TEASER_IMAGE_NODE = "node";
	private static final String TEASER_IMAGE_STYLE_NODE = "node";
	private static final String COLOR_CATEGORY_NODE = "node";
	private static final String COLOR_CATEGORY_CUSTOM = "custom";
	private static final ColorCategorySearchParams SEARCH_PARAMS_RESOLVER = ColorCategorySearchParams.builder()
			.addType(Type.technical)
			.build();
	private static final ColorCategorySearchParams SEARCH_PARAMS_SELECTION = ColorCategorySearchParams.builder()
			.addColorTypes()
			.withEnabled(Boolean.TRUE)
			.build();
	
	private MultipleSelectionElement displayEl;
	private SingleSelection teaserImageTypeEl;
	private SingleSelection teaserImageNodeEl;
	private SingleSelection teaserImageSystemEl;
	private FileElement teaserImageUploadEl;
	private SingleSelection teaserImageStyleTypeEl;
	private SingleSelection teaserImageStyleNodeEl;
	private boolean teaserImageStyleNodeNeverVisible = true;
	private SingleSelection colorCategoryTypeEl;
	private SingleSelection colorCategoryNodeEl;
	private FormLink colorCategorySelectionEl;
	private FormLayoutContainer previewCont;
	
	private CloseableCalloutWindowController calloutCtrl;
	private ColorCategoryChooserController colorCategoryChooserCtrl;
	private NodeLayoutPreviewController previewCtrl;
	
	private final ICourse course;
	private final CourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	private final CourseEditorTreeNode editorTreeNode;
	private final ColorCategoryResolver colorCategoryResolver;
	private final boolean inSTOverview;
	private ImageSource teaserImageSource;
	private String colorCategoryIdentifier;
	private TeaserImageStyle teaserImageStyle;
	
	@Autowired
	private CourseStyleService courseStyleService;
	@Autowired
	private LearningPathService learningPathService;

	public NodeLayoutController(UserRequest ureq, WindowControl wControl, CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CourseStyleUIFactory.class, getLocale(), getTranslator()));
		this.courseNode = courseNode;
		this.userCourseEnv = userCourseEnv;
		course = CourseFactory.loadCourse(userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry());
		editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
		colorCategoryResolver = courseStyleService.getColorCategoryResolver(SEARCH_PARAMS_RESOLVER, course.getCourseConfig().getColorCategoryIdentifier());
		teaserImageSource = courseNode.getTeaserImageSource();
		teaserImageStyle = courseNode.getTeaserImageStyle();
		// check if ColorCategory exists
		colorCategoryIdentifier = courseStyleService.getColorCategory(courseNode.getColorCategoryIdentifier(), ColorCategory.IDENTIFIER_DEFAULT_COURSE_NODE).getIdentifier();
		inSTOverview = isInSTOverview();
		
		initForm(ureq);
		updateTeaserImageUI();
		updateTeaserImageStyleUI();
		updateColorCategoryUI();
		updatePreviewUI(ureq, false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.layout");
		
		SelectionValues displayKV = new SelectionValues();
		displayKV.add(entry(KEY_TITLE, translate("nodeConfigForm.title.long")));
		displayKV.add(entry(KEY_DESCRIPTION, translate("nodeConfigForm.metadata.all")));
		displayEl = uifactory.addCheckboxesVertical("nodeConfigForm.display_options", formLayout, displayKV.keys(), displayKV.values(), 1);
		displayEl.addActionListener(FormEvent.ONCHANGE);
		if (CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT.equals(courseNode.getDisplayOption())) {
			displayEl.select(KEY_TITLE, true);
			displayEl.select(KEY_DESCRIPTION, true);
		} else if (CourseNode.DISPLAY_OPTS_TITLE_CONTENT.equals(courseNode.getDisplayOption())) {
			displayEl.select(KEY_TITLE, true);
		} else if (CourseNode.DISPLAY_OPTS_DESCRIPTION_CONTENT.equals(courseNode.getDisplayOption())) {
			displayEl.select(KEY_DESCRIPTION, true);
		} else if (CourseNode.DISPLAY_OPTS_CONTENT.equals(courseNode.getDisplayOption())) {
			//
		}
		
		SelectionValues teaserImageTypeKV = new SelectionValues();
		teaserImageTypeKV.add(entry(ImageSourceType.course.name(), translate("teaser.image.type.course")));
		teaserImageTypeKV.add(entry(ImageSourceType.inherited.name(), translate("teaser.image.type.inherited")));
		teaserImageTypeKV.add(entry(TEASER_IMAGE_NODE, translate("teaser.image.type.node")));
		teaserImageTypeEl = uifactory.addDropdownSingleselect("teaser.image.type", formLayout, teaserImageTypeKV.keys(), teaserImageTypeKV.values());
		teaserImageTypeEl.addActionListener(FormEvent.ONCHANGE);
		ImageSourceType type = teaserImageSource != null ? teaserImageSource.getType() : ImageSourceType.DEFAULT_COURSE_NODE;
		if (ImageSourceType.course == type || ImageSourceType.inherited == type) {
			teaserImageTypeEl.select(type.name(), true);
		} else {
			teaserImageTypeEl.select(TEASER_IMAGE_NODE, true);
		}
		
		SelectionValues teaserImageNodeKV = new SelectionValues();
		teaserImageNodeKV.add(entry(ImageSourceType.custom.name(), translate("teaser.image.type.upload")));
		teaserImageNodeKV.add(entry(ImageSourceType.system.name(), translate("teaser.image.type.system")));
		teaserImageNodeKV.add(entry(ImageSourceType.none.name(), translate("teaser.image.type.none")));
		teaserImageNodeEl = uifactory.addRadiosHorizontal("teaser.image.custom", null, formLayout, teaserImageNodeKV.keys(), teaserImageNodeKV.values());
		teaserImageNodeEl.addActionListener(FormEvent.ONCHANGE);
		if (ImageSourceType.custom == type || ImageSourceType.system == type || ImageSourceType.none == type) {
			teaserImageNodeEl.select(type.name(), true);
		} else {
			teaserImageNodeEl.select(ImageSourceType.system.name(), true);
		}
		
		SelectionValues teaserImageKV = new SelectionValues();
		courseStyleService.getSystemTeaserImageSources().stream().forEach(
				source -> teaserImageKV.add(entry(
						source.getFilename(),
						CourseStyleUIFactory.translateSystemImage(getTranslator(), source.getFilename()))));
		teaserImageKV.sort(SelectionValues.VALUE_ASC);
		teaserImageSystemEl = uifactory.addDropdownSingleselect("teaser.image.system", formLayout, teaserImageKV.keys(), teaserImageKV.values());
		teaserImageSystemEl.addActionListener(FormEvent.ONCHANGE);
		if (teaserImageSource != null && ImageSourceType.system == teaserImageSource.getType()) {
			if (teaserImageSystemEl.containsKey(teaserImageSource.getFilename())) {
				teaserImageSystemEl.select(teaserImageSource.getFilename(), true);
			} else {
				teaserImageTypeEl.select(ImageSourceType.inherited.name(), true);
				teaserImageSystemEl.setVisible(false);
			}
		}
		
		teaserImageUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "teaser.image.upload", formLayout);
		teaserImageUploadEl.setMaxUploadSizeKB(IMAGE_LIMIT_KB, null, null);
		String teaserImageExampleKey = teaserImageStyle != null &&  TeaserImageStyle.cover == teaserImageStyle
				? "teaser.image.upload.example.cover"
				: "teaser.image.upload.example";
		teaserImageUploadEl.setExampleKey(teaserImageExampleKey, null);
		teaserImageUploadEl.addActionListener(FormEvent.ONCHANGE);
		teaserImageUploadEl.limitToMimeType(IMAGE_MIME_TYPES, "error.mimetype", new String[]{ IMAGE_MIME_TYPES.toString()} );
		if (teaserImageSource != null && ImageSourceType.custom == teaserImageSource.getType()) {
			ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry());
			VFSLeaf image = courseStyleService.getImage(course, courseNode);
			if (image instanceof LocalFileImpl) {
				teaserImageUploadEl.setInitialFile(((LocalFileImpl)image).getBasefile());
			}
		}
		
		SelectionValues teaserImageStyleTypeKV = new SelectionValues();
		teaserImageStyleTypeKV.add(entry(TeaserImageStyle.course.name(), translate("teaser.image.style.type.course")));
		teaserImageStyleTypeKV.add(entry(TeaserImageStyle.inherited.name(), translate("teaser.image.style.type.inherited")));
		teaserImageStyleTypeKV.add(entry(TEASER_IMAGE_STYLE_NODE, translate("teaser.image.style.type.node")));
		teaserImageStyleTypeEl = uifactory.addDropdownSingleselect("teaser.image.style", formLayout, teaserImageStyleTypeKV.keys(), teaserImageStyleTypeKV.values());
		teaserImageStyleTypeEl.addActionListener(FormEvent.ONCHANGE);
		if (TeaserImageStyle.course == teaserImageStyle || TeaserImageStyle.inherited == teaserImageStyle) {
			teaserImageStyleTypeEl.select(teaserImageStyle.name(), true);
		} else {
			teaserImageStyleTypeEl.select(TEASER_IMAGE_STYLE_NODE, true);
		}
		
		SelectionValues teaserImageStyleNodeKV = new SelectionValues();
		Arrays.stream(TeaserImageStyle.values())
				.filter(style -> !style.isTechnical())
				.forEach(style -> teaserImageStyleNodeKV.add(entry(style.name(), translate(CourseStyleUIFactory.getI18nKey(style)))));
		teaserImageStyleNodeEl = uifactory.addRadiosHorizontal("teaser.image.style.node", null, formLayout, teaserImageStyleNodeKV.keys(), teaserImageStyleNodeKV.values());
		teaserImageStyleNodeEl.addActionListener(FormEvent.ONCHANGE);
		if (TEASER_IMAGE_STYLE_NODE.equals(teaserImageStyleTypeEl.getSelectedKey())) {
			if (teaserImageStyleNodeEl.containsKey(teaserImageStyle.name())) {
				teaserImageStyleNodeEl.select(teaserImageStyle.name(), true);
			} else {
				teaserImageStyleNodeEl.select(TeaserImageStyle.DEFAULT_COURSE_NODE.name(), true);
			}
			teaserImageStyleNodeNeverVisible = false;
		} else {
			teaserImageStyleNodeEl.setVisible(false);
		}
		
		SelectionValues colorCategoryTypeKV = new SelectionValues();
		colorCategoryTypeKV.add(entry(ColorCategory.IDENTIFIER_COURSE, translate("color.category.type.course")));
		colorCategoryTypeKV.add(entry(ColorCategory.IDENTIFIER_INHERITED, translate("color.category.type.inherited")));
		colorCategoryTypeKV.add(entry(COLOR_CATEGORY_NODE, translate("color.category.type.node")));
		colorCategoryTypeEl = uifactory.addDropdownSingleselect("color.category", formLayout, colorCategoryTypeKV.keys(), colorCategoryTypeKV.values());
		colorCategoryTypeEl.addActionListener(FormEvent.ONCHANGE);
		if (ColorCategory.IDENTIFIER_COURSE.equals(colorCategoryIdentifier) || ColorCategory.IDENTIFIER_INHERITED.equals(colorCategoryIdentifier)) {
			colorCategoryTypeEl.select(colorCategoryIdentifier, true);
		} else {
			colorCategoryTypeEl.select(COLOR_CATEGORY_NODE, true);
		}
		
		SelectionValues colorCategoryNodeKV = new SelectionValues();
		colorCategoryNodeKV.add(entry(COLOR_CATEGORY_CUSTOM, translate("color.category.type.custom")));
		colorCategoryNodeKV.add(entry(ColorCategory.IDENTIFIER_NO_COLOR, translate("color.category.type.none")));
		colorCategoryNodeEl = uifactory.addRadiosHorizontal("color.category.node", null, formLayout, colorCategoryNodeKV.keys(), colorCategoryNodeKV.values());
		colorCategoryNodeEl.addActionListener(FormEvent.ONCHANGE);
		if (ColorCategory.IDENTIFIER_NO_COLOR.equals(colorCategoryIdentifier)) {
			colorCategoryNodeEl.select(ColorCategory.IDENTIFIER_NO_COLOR, true);
		} else {
			colorCategoryNodeEl.select(COLOR_CATEGORY_CUSTOM, true);
		}
		
		colorCategorySelectionEl = uifactory.addFormLink("color.category.selection", "color.category.selection", "",
				translate("color.category.selection"), formLayout, Link.NONTRANSLATED);
		colorCategorySelectionEl.setElementCssClass("o_colcal_ele");
		
		String page = Util.getPackageVelocityRoot(NodeLayoutController.class) + "/layout_preview_cont.html"; 
		previewCont = FormLayoutContainer.createCustomFormLayout("layout.preview", getTranslator(), page);
		previewCont.setLabel("layout.preview", null);
		formLayout.add(previewCont);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("nodeConfigForm.save", buttonLayout)
			.setElementCssClass("o_sel_node_editor_submit");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == displayEl) {
			updatePreviewUI(ureq, true);
		} else if (source == teaserImageTypeEl) {
			updateTeaserImageUI();
			updatePreviewUI(ureq, true);
		} else if (source == teaserImageNodeEl) {
			updateTeaserImageUI();
			updatePreviewUI(ureq, true);
		} else if (source == teaserImageStyleTypeEl) {
			updateTeaserImageStyleUI();
			updatePreviewUI(ureq, true);
		} else if (source == teaserImageStyleNodeEl) {
			updateTeaserImageStyleUI();
			updatePreviewUI(ureq, true);
		} else if (source == teaserImageSystemEl) {
			updatePreviewUI(ureq, true);
		} else if (source == teaserImageUploadEl) {
			updatePreviewUI(ureq, true);
		} else if (source == colorCategoryTypeEl) {
			updateColorCategorySelectionUI();
			updatePreviewUI(ureq, true);
		} else if (source == colorCategoryNodeEl) {
			updateColorCategorySelectionUI();
			updatePreviewUI(ureq, true);
		} else if (source == colorCategorySelectionEl) {
			doChooseColorCategory(ureq);
			updatePreviewUI(ureq, false);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (colorCategoryChooserCtrl == source) {
			if (event == Event.DONE_EVENT) {
				colorCategoryIdentifier = colorCategoryChooserCtrl.getColorCategory().getIdentifier();
				updateColorCategoryUI();
				updatePreviewUI(ureq, true);
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
		
		teaserImageUploadEl.clearError();
		if (teaserImageUploadEl.isVisible()) {
			List<ValidationStatus> fileStatus = new ArrayList<>();
			teaserImageUploadEl.validate(fileStatus);
			if (fileStatus.isEmpty()) {
				if (teaserImageUploadEl.getUploadFile() == null && teaserImageUploadEl.getInitialFile() == null) {
					teaserImageUploadEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String displayOption = getDisplayOption();
		courseNode.setDisplayOption(displayOption);
		
		teaserImageSource = null;
		ImageSourceType type = ImageSourceType.DEFAULT_COURSE_NODE;
		if (teaserImageNodeEl.isVisible() && teaserImageNodeEl.isOneSelected()) {
			if (teaserImageNodeEl.getSelectedKey().equals(ImageSourceType.none.name())) {
				type = ImageSourceType.none;
			} else if (teaserImageNodeEl.getSelectedKey().equals(ImageSourceType.system.name())) {
				teaserImageSource = courseStyleService.getSystemTeaserImageSource(teaserImageSystemEl.getSelectedKey());
			} else if (teaserImageNodeEl.getSelectedKey().equals(ImageSourceType.custom.name())) {
				if (teaserImageUploadEl.getUploadFile() != null) {
					teaserImageSource = courseStyleService.storeImage(course, courseNode, getIdentity(),
							teaserImageUploadEl.getUploadFile(), teaserImageUploadEl.getUploadFileName());
				} else if (teaserImageUploadEl.getInitialFile() != null) {
					teaserImageSource = courseNode.getTeaserImageSource();
				}
			}
		} else if (teaserImageTypeEl.isOneSelected()) {
			if (teaserImageTypeEl.getSelectedKey().equals(ImageSourceType.course.name())) {
				type = ImageSourceType.course;
			} else if (teaserImageTypeEl.getSelectedKey().equals(ImageSourceType.inherited.name())) {
				type = ImageSourceType.inherited;
			}
		}
		
		if (teaserImageSource == null) {
			teaserImageSource = courseStyleService.createEmptyImageSource(type);
		}
		courseNode.setTeaserImageSource(teaserImageSource);
		
		if (ImageSourceType.custom != teaserImageSource.getType()) {
			courseStyleService.deleteImage(course, courseNode);
			teaserImageUploadEl.setInitialFile(null);
			teaserImageUploadEl.reset();
		}
		
		courseNode.setTeaserImageStyle(teaserImageStyle);
		courseNode.setColorCategoryIdentifier(colorCategoryIdentifier);
		
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private String getDisplayOption() {
		String displayOption = CourseNode.DISPLAY_OPTS_CONTENT;
		
		if (displayEl.isKeySelected(KEY_TITLE)) {
			if (displayEl.isKeySelected(KEY_DESCRIPTION)) {
				displayOption = CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT;
			} else {
				displayOption = CourseNode.DISPLAY_OPTS_TITLE_CONTENT;
			}
		} else if (displayEl.isKeySelected(KEY_DESCRIPTION)) {
			displayOption = CourseNode.DISPLAY_OPTS_DESCRIPTION_CONTENT;
		}
		
		return displayOption;
	}
	
	private void updateTeaserImageUI() {
		teaserImageNodeEl.setVisible(teaserImageTypeEl.isOneSelected() && teaserImageTypeEl.getSelectedKey().equals(TEASER_IMAGE_NODE));
		
		boolean nodeImage = teaserImageNodeEl.isVisible() && teaserImageNodeEl.isOneSelected();
		teaserImageUploadEl.setVisible(nodeImage && teaserImageNodeEl.getSelectedKey().equals(ImageSourceType.custom.name()));
		teaserImageSystemEl.setVisible(nodeImage && teaserImageNodeEl.getSelectedKey().equals(ImageSourceType.system.name()));
		if (teaserImageSystemEl.isVisible() && !teaserImageSystemEl.isOneSelected() && teaserImageSystemEl.getKeys().length > 0) {
			teaserImageSystemEl.select(teaserImageSystemEl.getKey(0), true);
		}
	}
	
	private void updateTeaserImageStyleUI() {
		teaserImageStyleNodeEl.setVisible(teaserImageStyleTypeEl.isOneSelected() && teaserImageStyleTypeEl.getSelectedKey().equals(TEASER_IMAGE_STYLE_NODE));
		if (teaserImageStyleNodeNeverVisible && teaserImageStyleNodeEl.isVisible()) {
			teaserImageStyleNodeNeverVisible = false;
			TeaserImageStyle technicalStyle = getTechnicalStyle(teaserImageStyle);
			if (technicalStyle != null) {
				teaserImageStyleNodeEl.select(technicalStyle.name(), true);
			} else {
				teaserImageStyleNodeEl.select(TeaserImageStyle.DEFAULT_COURSE_NODE.name(), true);
			}
		}
		
		if (teaserImageStyleTypeEl.isOneSelected()) {
			String selectedKey = teaserImageStyleTypeEl.getSelectedKey();
			if (TeaserImageStyle.course.name().equals(selectedKey) || TeaserImageStyle.inherited.name().equals(selectedKey)) {
				teaserImageStyle = TeaserImageStyle.valueOf(selectedKey);
			} else if (teaserImageStyleNodeEl.isVisible() && teaserImageStyleNodeEl.isOneSelected()) {
				teaserImageStyle = TeaserImageStyle.valueOf(teaserImageStyleNodeEl.getSelectedKey());
			}
		}
	}

	private void updateColorCategorySelectionUI() {
		if (colorCategoryTypeEl.isOneSelected()) {
			String selectedKey = colorCategoryTypeEl.getSelectedKey();
			if (ColorCategory.IDENTIFIER_COURSE.equals(selectedKey) || ColorCategory.IDENTIFIER_INHERITED.equals(selectedKey)) {
				colorCategoryIdentifier = selectedKey;
			} else if (colorCategoryNodeEl.isVisible() && colorCategoryNodeEl.isOneSelected() && colorCategoryNodeEl.getSelectedKey().equals(ColorCategory.IDENTIFIER_NO_COLOR)) {
				colorCategoryIdentifier = ColorCategory.IDENTIFIER_NO_COLOR;
			}
			updateColorCategoryUI();
		}
	}
	
	private void doChooseColorCategory(UserRequest ureq) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(colorCategoryChooserCtrl);
		
		colorCategoryChooserCtrl = new ColorCategoryChooserController(ureq, getWindowControl(), SEARCH_PARAMS_SELECTION);
		listenTo(colorCategoryChooserCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				colorCategoryChooserCtrl.getInitialComponent(), colorCategorySelectionEl.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void updateColorCategoryUI() {
		boolean nodeVisible = colorCategoryTypeEl.isOneSelected() && colorCategoryTypeEl.getSelectedKey().equals(COLOR_CATEGORY_NODE);
		colorCategoryNodeEl.setVisible(nodeVisible);
		
		boolean custom = colorCategoryNodeEl.isVisible()
				&& colorCategoryNodeEl.isOneSelected()
				&& COLOR_CATEGORY_CUSTOM.equals(colorCategoryNodeEl.getSelectedKey());
		if (custom) {
			ColorCategory colorCategory = colorCategoryResolver.getColorCategory(colorCategoryIdentifier, editorTreeNode);
			colorCategoryIdentifier = colorCategory.getIdentifier();
			String categoryName = CourseStyleUIFactory.translate(getTranslator(), colorCategory);
			String iconLeftCss = CourseStyleUIFactory.getIconLeftCss(colorCategory);
			colorCategorySelectionEl.setI18nKey(categoryName);
			colorCategorySelectionEl.setIconLeftCSS(iconLeftCss);
		}
		colorCategorySelectionEl.setVisible(custom);
	}
	
	public void updatePreviewUI(UserRequest ureq, boolean dirty) {
		if (previewCtrl == null) {
			previewCtrl = new NodeLayoutPreviewController(ureq, getWindowControl(), courseNode);
			listenTo(previewCtrl);
			previewCont.put("preview", previewCtrl.getInitialComponent());
		}
		
		String iconCSSClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass();
		ColorCategory colorCategory = colorCategoryResolver.getColorCategory(colorCategoryIdentifier, editorTreeNode);
		String colorCategoryCss = colorCategoryResolver.getCss(colorCategory);
		Mapper mapper = createPreviewImageMapper();
		TeaserImageStyle previewStyle = getPreviewImageStyle();

		org.olat.course.style.Header.Builder headerBuilder = Header.builder();
		headerBuilder.withIconCss(iconCSSClass);
		String displayOption = getDisplayOption();
		CourseStyleUIFactory.addMetadata(headerBuilder, courseNode, displayOption, true);
		headerBuilder.withColorCategoryCss(colorCategoryCss);
		if (mapper != null) {
			headerBuilder.withTeaserImage(mapper, previewStyle);
		}
		Header header = headerBuilder.build();
		
		Overview overview = null;
		if (inSTOverview) {
			org.olat.course.nodes.st.Overview.Builder overviewBuilder = Overview.builder();
			overviewBuilder.withNodeIdent(courseNode.getIdent());
			overviewBuilder.withIconCss(iconCSSClass);
			overviewBuilder.withTitle(courseNode.getLongTitle());
			overviewBuilder.withSubTitle(CourseNodeHelper.getDifferentlyStartingShortTitle(courseNode));
			overviewBuilder.withDescription(courseNode.getDescription());
			overviewBuilder.withColorCategoryCss(colorCategoryCss);
			if (mapper != null) {
				overviewBuilder.withTeaserImage(mapper, previewStyle);
			}
			if (LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType())) {
				CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
				LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode, editorTreeNode.getParent());
				overviewBuilder.withDuration(learningPathConfigs.getDuration());
				overviewBuilder.withStartDateConfig(learningPathConfigs.getStartDateConfig());
				overviewBuilder.withEndDateConfig(learningPathConfigs.getEndDateConfig());
			}
			overview = overviewBuilder.build();
		}
		
		previewCtrl.update(ureq, header, overview);
		
		if (dirty) {
			String dirtyOnLoad = FormJSHelper.setFlexiFormDirtyOnLoad(flc.getRootForm());
			getWindowControl().getWindowBackOffice().sendCommandTo(new JSCommand(dirtyOnLoad));
		}
	}

	private Mapper createPreviewImageMapper() {
		Mapper mapper = null;
		if (teaserImageUploadEl.isVisible()) {
			if (teaserImageUploadEl.getUploadFile() != null) {
				mapper = new VFSMediaMapper(teaserImageUploadEl.getUploadFile());
			} else if (teaserImageUploadEl.getInitialFile() != null) {
				mapper = new VFSMediaMapper(teaserImageUploadEl.getInitialFile());
			}
		} else if (teaserImageSystemEl.isVisible() && teaserImageSystemEl.isOneSelected()) {
			File file = courseStyleService.getSystemTeaserImageFile(teaserImageSystemEl.getSelectedKey());
			if (file != null) {
				mapper = new VFSMediaMapper(file);
			}
		} else {
			ImageSourceType type = teaserImageTypeEl.isOneSelected()
					? ImageSourceType.toEnum(teaserImageTypeEl.getSelectedKey())
					: ImageSourceType.DEFAULT_COURSE_NODE;
			if (ImageSourceType.course == type) {
				mapper = courseStyleService.getTeaserImageMapper(course);
			} else if (ImageSourceType.inherited == type) {
				if (editorTreeNode == null && editorTreeNode.getParent() != null) {
					mapper = courseStyleService.getTeaserImageMapper(course, editorTreeNode.getParent());
				} else {
					mapper = courseStyleService.getTeaserImageMapper(course);
				}
			}
		}
		return mapper;
	}

	private TeaserImageStyle getPreviewImageStyle() {
		TeaserImageStyle previewStyle = teaserImageStyle;
		
		TeaserImageStyle technicalStyle = getTechnicalStyle(previewStyle);
		if (technicalStyle != null) {
			previewStyle = technicalStyle;
		}
		
		return previewStyle;
	}

	private TeaserImageStyle getTechnicalStyle(TeaserImageStyle style) {
		TeaserImageStyle technicalStyle = null;
		if (TeaserImageStyle.course == style) {
			technicalStyle = course.getCourseConfig().getTeaserImageStyle();
		} else if (TeaserImageStyle.inherited == style) {
			if (editorTreeNode != null && editorTreeNode.getParent() != null) {
				technicalStyle = courseStyleService.getTeaserImageStyle(course, editorTreeNode.getParent());
			} else {
				technicalStyle = course.getCourseConfig().getTeaserImageStyle();
			}
		}
		return technicalStyle;
	}

	private boolean isInSTOverview() {
		TreeNode treeNode = course.getEditorTreeModel().getNodeById(courseNode.getIdent());
		if (treeNode != null && treeNode.getParent() instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode parent = (CourseEditorTreeNode)treeNode.getParent();
			if (parent.getCourseNode() instanceof STCourseNode) {
				STCourseNode stCourseNode = (STCourseNode)parent.getCourseNode();
				ModuleConfiguration stConfig = stCourseNode.getModuleConfiguration();
				String displayType = stConfig.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
				if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC.equals(displayType) 
						|| STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW.equals(displayType)) {
					String childrenFilterConfig = stConfig.getStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_FILTER, STCourseNodeEditController.CONFIG_VALUE_CHILDREN_ALL);
					if (STCourseNodeEditController.CONFIG_VALUE_CHILDREN_SELECTION.equals(childrenFilterConfig)) {
						String childNodesConfig = stConfig.getStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_IDENTS, "");
						List<String> childNodes = Arrays.asList(childNodesConfig.split(","));
						if (childNodes.contains(courseNode.getIdent())) {
							return true;
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}


}
