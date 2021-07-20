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
import java.util.Arrays;
import java.util.List;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.TreeNode;
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
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.Overview;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.style.ColorCategory;
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
	private FormLayoutContainer previewCont;
	
	private CloseableCalloutWindowController calloutCtrl;
	private ColorCategoryChooserController colorCategoryChooserCtrl;
	private NodeLayoutPreviewController previewCtrl;
	
	private final ICourse course;
	private final CourseNode courseNode;
	private final UserCourseEnvironment userCourseEnv;
	private final boolean inSTOverview;
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
		inSTOverview = isInSTOverview();
		
		initForm(ureq);
		updateTeaserImageUI();
		doSetColorCategory(courseNode.getColorCategoryIdentifier());
		updatePreviewUI(ureq);
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
		if (source == displayOptionsEl) {
			updatePreviewUI(ureq);
		} else if (source == teaserImageTypeEl) {
			updateTeaserImageUI();
			updatePreviewUI(ureq);
		} else if (source == teaserImageSystemEl) {
			updatePreviewUI(ureq);
		} else if (source == teaserImageUploadEl) {
			updatePreviewUI(ureq);
		} else if (source == colorCategoryEl) {
			doChooseColorCategory(ureq);
			updatePreviewUI(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (colorCategoryChooserCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doSetColorCategory(colorCategoryChooserCtrl.getColorCategory().getIdentifier());
				updatePreviewUI(ureq);
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
	
	public void updatePreviewUI(UserRequest ureq) {
		if (previewCtrl == null) {
			previewCtrl = new NodeLayoutPreviewController(ureq, getWindowControl(), courseNode);
			listenTo(previewCtrl);
			previewCont.put("preview", previewCtrl.getInitialComponent());
		}
		
		String iconCSSClass = CourseNodeFactory.getInstance().getCourseNodeConfigurationEvenForDisabledBB(courseNode.getType()).getIconCSSClass();
		String colorCategoryCss = ColorCategory.IDENTIFIER_INHERITED.equals(colorCategory.getIdentifier())
					? getInheritedColorCategory().getCssClass()
					: colorCategory.getCssClass();
		Mapper mapper = createPreviewImageMapper();

		org.olat.course.style.Header.Builder headerBuilder = Header.builder();
		headerBuilder.withIconCss(iconCSSClass);
		String displayOption = displayOptionsEl.getSelectedKey();
		CourseStyleUIFactory.addMetadata(headerBuilder, courseNode, displayOption, true);
		headerBuilder.withColorCategoryCss(colorCategoryCss);
		if (mapper != null) {
			headerBuilder.withTeaserImage(mapper, teaserImageStyle);
		}
		Header header = headerBuilder.build();
		
		Overview overview = null;
		if (inSTOverview) {
			org.olat.course.nodes.st.Overview.Builder overviewBuilder = Overview.builder();
			overviewBuilder.withNodeIdent(courseNode.getIdent());
			overviewBuilder.withIconCss(iconCSSClass);
			overviewBuilder.withTitle(courseNode.getShortTitle());
			overviewBuilder.withSubTitle(courseNode.getLongTitle());
			overviewBuilder.withDescription(courseNode.getDescription());
			overviewBuilder.withColorCategoryCss(colorCategoryCss);
			if (mapper != null) {
				overviewBuilder.withTeaserImage(mapper, teaserImageStyle);
			}
			overview = overviewBuilder.build();
		}
		
		previewCtrl.update(ureq, header, overview);
	}

	private Mapper createPreviewImageMapper() {
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
		return mapper;
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
