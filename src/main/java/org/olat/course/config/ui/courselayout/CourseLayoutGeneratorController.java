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
package org.olat.course.config.ui.courselayout;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.course.style.CourseStyleService.IMAGE_LIMIT_KB;
import static org.olat.course.style.CourseStyleService.IMAGE_MIME_TYPES;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.olat.core.commons.services.image.ImageService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockResult;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.core.util.vfs.filters.VFSItemSuffixFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.config.CourseConfig;
import org.olat.course.config.CourseConfigEvent;
import org.olat.course.config.CourseConfigEvent.CourseConfigType;
import org.olat.course.config.ui.CourseSettingsController;
import org.olat.course.config.ui.courselayout.attribs.AbstractLayoutAttribute;
import org.olat.course.config.ui.courselayout.attribs.PreviewLA;
import org.olat.course.config.ui.courselayout.attribs.SpecialAttributeFormItemHandler;
import org.olat.course.config.ui.courselayout.elements.AbstractLayoutElement;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.run.RunMainController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.ColorCategoryResolver;
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
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Present different templates for course-layouts and let user generate his own.
 * 
 * <P>
 * Initial Date:  01.02.2011 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class CourseLayoutGeneratorController extends FormBasicController {

	private static final String COLOR_CATEGORY_CUSTOM = "custom";
	private static final ColorCategorySearchParams SEARCH_PARAMS = ColorCategorySearchParams.builder()
			.addColorTypes()
			.withEnabled(Boolean.TRUE)
			.build();
	private static final String ELEMENT_ATTRIBUTE_DELIM = "__";
	private static final String PREVIEW_IMAGE_NAME = "preview.png";
	
	private static final String[] onKeys = new String[] {"xx"};
	private final String[] onValues;

	private SingleSelection styleSel;
	private SelectionElement menuEl;
	private MultipleSelectionElement menuNodeIconsEl;
	private MultipleSelectionElement menuPathEl;
	private SelectionElement breadCrumbEl;
	private FileElement logoUpl;
	private FormLayoutContainer previewImgFlc;
	private FormLayoutContainer styleFlc;
	private LinkedHashMap<String, Map<String, FormItem>> guiWrapper;
	private Map<String, Map<String, String>> persistedCustomConfig;
	private FormLayoutContainer logoImgFlc;
	private FormLink logoDel;
	private FormLayoutContainer styleCont;
	private SingleSelection teaserImageTypeEl;
	private SingleSelection teaserImageSystemEl;
	private FileElement teaserImageUploadEl;
	private SingleSelection teaserImageStyleEl;
	private SingleSelection colorCategoryEl;
	private FormLink colorCategorySelectionEl;
	private FormLayoutContainer headerPreviewCont;
	
	private CloseableCalloutWindowController calloutCtrl;
	private ColorCategoryChooserController colorCategoryChooserCtrl;
	private HeaderController headerCtrl;
	
	private boolean elWithErrorExists = false;
	private final boolean editable;
	private final boolean readOnly;
	private final boolean learningPath;
	
	private LockResult lockEntry;
	private CourseConfig courseConfig;
	private final RepositoryEntry courseEntry;
	private CourseEnvironment courseEnvironment;
	private final ColorCategoryResolver colorCategoryResolver;
	private ImageSource teaserImageSource;
	private TeaserImageStyle teaserImageStyle;
	private String colorCategoryIdentifier;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CustomConfigManager customCMgr;
	@Autowired
	private CourseStyleService courseStyleService;

	public CourseLayoutGeneratorController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, CourseConfig courseConfig,
			CourseEnvironment courseEnvironment, boolean editable, boolean readOnly) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(CourseSettingsController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CourseStyleUIFactory.class, getLocale(), getTranslator()));

		this.courseEntry = entry;
		this.courseConfig = courseConfig;
		this.courseEnvironment = courseEnvironment;
		colorCategoryResolver = courseStyleService.getColorCategoryResolver(SEARCH_PARAMS, courseConfig.getColorCategoryIdentifier());
		lockEntry = CoordinatorManager.getInstance().getCoordinator().getLocker()
				.acquireLock(entry.getOlatResource(), getIdentity(), CourseFactory.COURSE_EDITOR_LOCK, getWindow());
		this.editable = (lockEntry != null && lockEntry.isSuccess()) && editable && !readOnly;
		this.readOnly = readOnly;
		this.learningPath = LearningPathNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType());
		this.onValues = new String[] {translate("on")};
		colorCategoryIdentifier = courseConfig.getColorCategoryIdentifier();
		
		// stack the translator to get attribs/elements
		Translator pt = Util.createPackageTranslator(AbstractLayoutAttribute.class, getLocale(), getTranslator());
		pt = Util.createPackageTranslator(AbstractLayoutElement.class, getLocale(), pt);
		pt = Util.createPackageTranslator(RunMainController.class, getLocale(), pt);
		setTranslator(pt);
		
		persistedCustomConfig = customCMgr.getCustomConfig(courseEnvironment);
		
		teaserImageSource = courseConfig.getTeaserImageSource();
		teaserImageStyle = courseConfig.getTeaserImageStyle();
		initForm(ureq);
		updateMenuUI(false);
		updateTeaserImageUI();
		updateColorCategoryUI();
		updateHeaderPreviewUI(ureq, false);
		
		if(lockEntry != null && !lockEntry.isSuccess()) {
			String lockerName = "???";
			if(lockEntry.getOwner() != null) {
				lockerName = userManager.getUserDisplayName(lockEntry.getOwner());
			}
			if(lockEntry.isDifferentWindows()) {
				showWarning("error.editoralreadylocked.same.user", new String[] { lockerName });
			} else {
				showWarning("error.editoralreadylocked", new String[] { lockerName });
			}
		}
	}
	
	@Override
	protected void doDispose() {
		if (lockEntry != null && lockEntry.isSuccess()) {
			CoordinatorManager.getInstance().getCoordinator().getLocker().releaseLock(lockEntry);
			lockEntry = null;
		}
        super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createDefaultFormLayout("course.layout", getTranslator());
		layoutCont.setFormTitle(translate("tab.layout.title"));
		layoutCont.setRootForm(mainForm);
		formLayout.add("course.layout", layoutCont);
		
		List<String> keys = new ArrayList<>();
		List<String> vals = new ArrayList<>();
		List<String> csss = new ArrayList<>();

		String actualCSSSettings = courseConfig.getCssLayoutRef();
		
		// add a default option
		keys.add(CourseLayoutHelper.CONFIG_KEY_DEFAULT);
		vals.add(translate("course.layout.default"));
		csss.add("");
		
		// check for old legacy template, only available if yet one set
		if(actualCSSSettings.startsWith("/") && actualCSSSettings.lastIndexOf('/') == 0) {
			keys.add(actualCSSSettings);
			vals.add(translate("course.layout.legacy", actualCSSSettings));
			csss.add("");
		} 
		
		// add css from hidden coursecss-folder
		VFSItem coursecssCont = courseEnvironment.getCourseFolderContainer().resolve(CourseLayoutHelper.COURSEFOLDER_CSS_BASE);
		if (coursecssCont instanceof VFSContainer) {
			List<VFSItem> coursecssStyles = ((VFSContainer)coursecssCont).getItems(new VFSItemSuffixFilter(new String[]{ "css" }));
			if (coursecssStyles != null) {
				for (VFSItem vfsItem : coursecssStyles) {
					keys.add(CourseLayoutHelper.COURSEFOLDER_CSS_BASE + "/" + vfsItem.getName());
					vals.add(translate("course.layout.legacy", vfsItem.getName()));
					csss.add("");
				}
			}
		}

		// get the olat-wide templates
		List<VFSItem> templates = CourseLayoutHelper.getCourseThemeTemplates();
		if (templates != null) {
			for (VFSItem vfsItem : templates) {
				if (CourseLayoutHelper.isCourseThemeFolderValid((VFSContainer) vfsItem)){
					keys.add(CourseLayoutHelper.CONFIG_KEY_TEMPLATE + vfsItem.getName());
					String name = translate("course.layout.template", vfsItem.getName());
					vals.add(name);
					csss.add("");
				}
			}
		}
		
		// get the predefined template for this course if any
		VFSItem predefCont = courseEnvironment.getCourseBaseContainer().resolve(CourseLayoutHelper.LAYOUT_COURSE_SUBFOLDER + "/" + CourseLayoutHelper.CONFIG_KEY_PREDEFINED);
		if (predefCont != null && CourseLayoutHelper.isCourseThemeFolderValid((VFSContainer) predefCont)) {
			keys.add(CourseLayoutHelper.CONFIG_KEY_PREDEFINED);
			vals.add(translate("course.layout.predefined"));
			csss.add("");
		}

		// add option for customizing
		keys.add(CourseLayoutHelper.CONFIG_KEY_CUSTOM);
		vals.add(translate("course.layout.custom"));
		csss.add("");
		
		String[] theKeys = ArrayHelper.toArray(keys);
		String[] theValues = ArrayHelper.toArray(vals);
		String[] theCssClasses = ArrayHelper.toArray(csss);
		
		styleSel = uifactory.addDropdownSingleselect("course.layout.selector", layoutCont, theKeys, theValues, theCssClasses);
		styleSel.addActionListener(FormEvent.ONCHANGE);
		styleSel.setEnabled(editable);
		if (keys.contains(actualCSSSettings)){
			styleSel.select(actualCSSSettings, true);
		} else {
			styleSel.select(CourseLayoutHelper.CONFIG_KEY_DEFAULT, true);
		}

		previewImgFlc = FormLayoutContainer.createCustomFormLayout("preview.image", getTranslator(), velocity_root + "/image.html");
		layoutCont.add(previewImgFlc);
		previewImgFlc.setLabel("preview.image.label", null);		
		refreshPreviewImage(ureq, actualCSSSettings);		
		
		logoImgFlc = FormLayoutContainer.createCustomFormLayout("logo.image", getTranslator(), velocity_root + "/image.html");
		layoutCont.add(logoImgFlc);
		logoImgFlc.setLabel("logo.image.label", null);		
		refreshLogoImage(ureq);	
		
		// offer upload for 2nd logo
		if(editable) {
			logoUpl = uifactory.addFileElement(getWindowControl(), getIdentity(), "upload.second.logo", layoutCont);
			logoUpl.addActionListener(FormEvent.ONCHANGE);
			Set<String> mimeTypes = new HashSet<>();
			mimeTypes.add("image/*");
			logoUpl.limitToMimeType(mimeTypes, "logo.file.type.error", null);
			logoUpl.setMaxUploadSizeKB(2048, "logo.size.error", null);
		}
		
		// prepare the custom layouter
		styleFlc = FormLayoutContainer.createCustomFormLayout("style", getTranslator(), velocity_root + "/style.html");
		layoutCont.add(styleFlc);
		styleFlc.setLabel(null, null);
		enableDisableCustom(CourseLayoutHelper.CONFIG_KEY_CUSTOM.equals(actualCSSSettings));
		
		FormLayoutContainer navigationCont = FormLayoutContainer.createDefaultFormLayout("course.navigation", getTranslator());
		navigationCont.setFormTitle(translate("course.navigation"));
		navigationCont.setRootForm(mainForm);
		formLayout.add("course.navigation", navigationCont);
		
		menuEl = uifactory.addCheckboxesHorizontal("menuIsOn", "chkbx.menu.onoff", navigationCont, onKeys, onValues);
		menuEl.select(onKeys[0], courseConfig.isMenuEnabled());
		menuEl.addActionListener(FormEvent.ONCHANGE);
		menuEl.setEnabled(editable);
		
		menuNodeIconsEl = uifactory.addCheckboxesHorizontal("chkbx.menu.node.icons.onoff", navigationCont, onKeys, onValues);
		menuNodeIconsEl.select(onKeys[0], courseConfig.isMenuNodeIconsEnabled());
		menuNodeIconsEl.setEnabled(editable);
		
		menuPathEl = uifactory.addCheckboxesHorizontal("chkbx.menu.path.onoff", navigationCont, onKeys, onValues);
		menuPathEl.select(onKeys[0], courseConfig.isMenuPathEnabled());
		menuPathEl.setEnabled(editable);
		
		breadCrumbEl = uifactory.addCheckboxesHorizontal("breadCrumbIsOn", "chkbx.breadcrumb.onoff", navigationCont, onKeys, onValues);
		breadCrumbEl.select(onKeys[0], courseConfig.isBreadCrumbEnabled());
		breadCrumbEl.addActionListener(FormEvent.ONCHANGE);
		breadCrumbEl.setEnabled(editable);
		
		styleCont = FormLayoutContainer.createDefaultFormLayout("node.style", getTranslator());
		styleCont.setFormTitle(translate("node.style.defaults"));
		styleCont.setRootForm(mainForm);
		formLayout.add("node.style", styleCont);
		
		SelectionValues teaserImageTpeKV = new SelectionValues();
		teaserImageTpeKV.add(entry(ImageSourceType.custom.name(), translate("teaser.image.type.upload")));
		teaserImageTpeKV.add(entry(ImageSourceType.system.name(), translate("teaser.image.type.system")));
		teaserImageTpeKV.add(entry(ImageSourceType.none.name(), translate("teaser.image.type.none")));
		teaserImageTypeEl = uifactory.addRadiosHorizontal("teaser.image.type", styleCont, teaserImageTpeKV.keys(), teaserImageTpeKV.values());
		teaserImageTypeEl.addActionListener(FormEvent.ONCHANGE);
		teaserImageTypeEl.setEnabled(editable);
		ImageSourceType type = teaserImageSource != null ? teaserImageSource.getType() : ImageSourceType.none;
		teaserImageTypeEl.select(type.name(), true);
		
		SelectionValues teaserImageKV = new SelectionValues();
		courseStyleService.getSystemTeaserImageSources().stream().forEach(
				source -> teaserImageKV.add(entry(
						source.getFilename(),
						CourseStyleUIFactory.translateSystemImage(getTranslator(), source.getFilename()))));
		teaserImageKV.sort(SelectionValues.VALUE_ASC);
		teaserImageSystemEl = uifactory.addDropdownSingleselect("teaser.image.system", styleCont, teaserImageKV.keys(), teaserImageKV.values());
		teaserImageSystemEl.addActionListener(FormEvent.ONCHANGE);
		teaserImageSystemEl.setEnabled(editable);
		if (teaserImageSource != null && ImageSourceType.system == teaserImageSource.getType()) {
			if (teaserImageSystemEl.containsKey(teaserImageSource.getFilename())) {
				teaserImageSystemEl.select(teaserImageSource.getFilename(), true);
			} else {
				teaserImageTypeEl.select(ImageSourceType.none.name(), true);
				teaserImageSystemEl.setVisible(false);
			}
		}
		
		teaserImageUploadEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "teaser.image.upload", styleCont);
		teaserImageUploadEl.setMaxUploadSizeKB(IMAGE_LIMIT_KB, null, null);
		teaserImageUploadEl.addActionListener(FormEvent.ONCHANGE);
		teaserImageUploadEl.setEnabled(editable);
		teaserImageUploadEl.limitToMimeType(IMAGE_MIME_TYPES, "error.mimetype", new String[]{ IMAGE_MIME_TYPES.toString()} );
		if (ImageSourceType.custom.name().equals(teaserImageTypeEl.getSelectedKey())) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			VFSLeaf image = courseStyleService.getImage(course);
			if (image instanceof LocalFileImpl) {
				teaserImageUploadEl.setInitialFile(((LocalFileImpl)image).getBasefile());
			}
		}
		
		SelectionValues teaserImageStyleKV = new SelectionValues();
		Arrays.stream(TeaserImageStyle.values())
				.filter(style -> !style.isTechnical())
				.forEach(style -> teaserImageStyleKV.add(entry(style.name(), translate(CourseStyleUIFactory.getI18nKey(style)))));
		teaserImageStyleEl = uifactory.addRadiosHorizontal("teaser.image.style", styleCont, teaserImageStyleKV.keys(), teaserImageStyleKV.values());
		teaserImageStyleEl.addActionListener(FormEvent.ONCHANGE);
		if (teaserImageStyle == null || !teaserImageStyleEl.containsKey(teaserImageStyle.name())) {
			teaserImageStyle = TeaserImageStyle.DEFAULT_COURSE;
		}
		teaserImageStyleEl.select(teaserImageStyle.name(), true);
		
		SelectionValues colorCategoryKV = new SelectionValues();
		colorCategoryKV.add(entry(COLOR_CATEGORY_CUSTOM, translate("color.category.type.custom")));
		colorCategoryKV.add(entry(ColorCategory.IDENTIFIER_NO_COLOR, translate("color.category.type.none")));
		colorCategoryEl = uifactory.addRadiosHorizontal("color.category", styleCont, colorCategoryKV.keys(), colorCategoryKV.values());
		colorCategoryEl.addActionListener(FormEvent.ONCHANGE);
		colorCategoryEl.setEnabled(editable);
		if (colorCategoryEl.containsKey(colorCategoryIdentifier)) {
			colorCategoryEl.select(colorCategoryIdentifier, true);
		} else if (StringHelper.containsNonWhitespace(colorCategoryIdentifier)) {
			colorCategoryEl.select(COLOR_CATEGORY_CUSTOM, true);
		} else {
			colorCategoryEl.select(ColorCategory.IDENTIFIER_NO_COLOR, true);
		}
		
		colorCategorySelectionEl = uifactory.addFormLink("color.category.selection", "color.category.selection", "",
				translate("color.category.selection"), styleCont, Link.NONTRANSLATED);
		colorCategorySelectionEl.setElementCssClass("o_colcal_ele");
		colorCategorySelectionEl.setEnabled(editable);
		
		String page = Util.getPackageVelocityRoot(HeaderController.class) + "/header_preview.html"; 
		headerPreviewCont = FormLayoutContainer.createCustomFormLayout("preview.header", getTranslator(), page);
		headerPreviewCont.setLabel("preview.header", null);
		styleCont.add(headerPreviewCont);
		
		if(!readOnly) {
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			buttonsCont.setRootForm(mainForm);
			styleCont.add(buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
			if(editable) {
				uifactory.addFormSubmitButton("course.layout.save", buttonsCont);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == styleSel) {
			String selection = styleSel.getSelectedKey();
			if (CourseLayoutHelper.CONFIG_KEY_CUSTOM.equals(selection)) {
				enableDisableCustom(true);
			} else {
				enableDisableCustom(false);
			}
			refreshPreviewImage(ureq, selection); // in any case!
		} else if (source == logoUpl && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
			if (logoUpl.isUploadSuccess()) {
				File newFile = logoUpl.getUploadFile();
				String newFilename = logoUpl.getUploadFileName();
				boolean isValidFileType = newFilename.toLowerCase().matches(".*[.](png|jpg|jpeg|gif)");
				if (!isValidFileType) {
					logoUpl.setErrorKey("logo.file.type.error", null);
				} else {
					logoUpl.clearError();
				}
				
				if (processUploadedImage(newFile)){
					logoUpl.reset();
					showInfo("logo.upload.success");
					refreshLogoImage(ureq);
				} else {
					showError("logo.upload.error");
				}
			}
		} else if (source.getName().contains(ELEMENT_ATTRIBUTE_DELIM)){
			// some selections changed, refresh to get new preview
			prepareStyleEditor(compileCustomConfigFromGuiWrapper());
		} else if (source == logoDel){
			VFSItem logo = (VFSItem) logoDel.getUserObject();
			logo.delete();
			refreshLogoImage(ureq);
		} else if (source == menuEl) {
			updateMenuUI(true);
		} else if (source == teaserImageTypeEl) {
			updateTeaserImageUI();
			updateHeaderPreviewUI(ureq, true);
		} else if (source == teaserImageSystemEl) {
			updateHeaderPreviewUI(ureq, true);
		} else if (source == teaserImageUploadEl) {
			updateHeaderPreviewUI(ureq, true);
		} else if (source == teaserImageStyleEl) {
			teaserImageStyle = TeaserImageStyle.valueOf(teaserImageStyleEl.getSelectedKey());
			updateTeaserImageUI();
			updateHeaderPreviewUI(ureq, true);
		} else if (source == colorCategoryEl) {
			updateColorCategorySelectionUI();
			updateHeaderPreviewUI(ureq, true);
		} else if (source == colorCategorySelectionEl) {
			doChooseColorCategory(ureq);
			updateHeaderPreviewUI(ureq, false);
		} 
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (colorCategoryChooserCtrl == source) {
			if (event == Event.DONE_EVENT) {
				colorCategoryIdentifier = colorCategoryChooserCtrl.getColorCategory().getIdentifier();
				updateColorCategoryUI();
				updateHeaderPreviewUI(ureq, true);
			}
			calloutCtrl.deactivate();
			cleanUp();
			styleCont.setDirty(true);
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(colorCategoryChooserCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		colorCategoryChooserCtrl = null;
		calloutCtrl = null;
	}
	
	private void enableDisableCustom(boolean onOff){
		if (onOff) prepareStyleEditor(persistedCustomConfig);
		styleFlc.setVisible(onOff);
		styleFlc.setEnabled(editable);
		if(logoUpl != null) logoUpl.setVisible(onOff);
		logoImgFlc.setVisible(onOff);
	}
	
	// process uploaded file according to image size and persist in <course>/layout/logo.xy
	private boolean processUploadedImage(File image){
		int height = 0;
		int width = 0;
		int[] size = customCMgr.getImageSize(image);
		if (size != null) {
			width = size[0];
			height = size[1];
		} else {
			return false;
		}
		// target file:
		String fileType = logoUpl.getUploadFileName().substring(logoUpl.getUploadFileName().lastIndexOf('.'));
		VFSContainer base = (VFSContainer) courseEnvironment.getCourseBaseContainer().resolve(CourseLayoutHelper.LAYOUT_COURSE_SUBFOLDER);
		if (base == null) {
			base = courseEnvironment.getCourseBaseContainer().createChildContainer(CourseLayoutHelper.LAYOUT_COURSE_SUBFOLDER);
		}
		VFSContainer customBase = (VFSContainer) base.resolve("/" + CourseLayoutHelper.CONFIG_KEY_CUSTOM);
		if (customBase==null) {
			customBase = base.createChildContainer(CourseLayoutHelper.CONFIG_KEY_CUSTOM);
		}
		if (customBase.resolve("logo" + fileType) != null) customBase.resolve("logo" + fileType).delete();
		VFSLeaf targetFile = customBase.createChildLeaf("logo" + fileType);
		int maxHeight = CourseLayoutHelper.getLogoMaxHeight();
		int maxWidth = CourseLayoutHelper.getLogoMaxWidth();
		if (height > maxHeight || width > maxWidth){
			// scale image
			try {
				ImageService helper = CourseLayoutHelper.getImageHelperToUse();
				String extension = FileUtils.getFileSuffix(logoUpl.getUploadFileName());
				helper.scaleImage(image, extension, targetFile, maxWidth, maxHeight);
			} catch (Exception e) {
				logError("could not find to be scaled image", e);
				return false;
			}
		} else {
			// only persist without scaling
			try(InputStream in = new FileInputStream(image);
					OutputStream out = targetFile.getOutputStream(false)) {
				FileUtils.copy(in, out);
			} catch (IOException e) {
				logError("Problem reading uploaded image to copy", e);
				return false;
			}
		}
		return true;
	}
	

	private void refreshPreviewImage(UserRequest ureq, String template) {
		VFSContainer baseFolder = CourseLayoutHelper.getThemeBaseFolder(courseEnvironment, template);
		if (baseFolder != null) {
			VFSItem preview = baseFolder.resolve("/" + PREVIEW_IMAGE_NAME);
			if (preview instanceof VFSLeaf) {
				ImageComponent image = new ImageComponent(ureq.getUserSession(), "preview");
				previewImgFlc.setVisible(true);
				previewImgFlc.put("preview", image);
				image.setMedia((VFSLeaf) preview);
				image.setMaxWithAndHeightToFitWithin(300, 300);
				return;
			}
		}
		previewImgFlc.setVisible(false);
		previewImgFlc.remove(previewImgFlc.getComponent("preview"));
	}
	
	private void refreshLogoImage(UserRequest ureq){
		VFSContainer baseFolder = CourseLayoutHelper.getThemeBaseFolder(courseEnvironment, CourseLayoutHelper.CONFIG_KEY_CUSTOM);
		VFSItem logo = customCMgr.getLogoItem(baseFolder);
		if (logo instanceof VFSLeaf) {
			ImageComponent image = new ImageComponent(ureq.getUserSession(), "preview");
			logoImgFlc.setVisible(true);
			logoImgFlc.put("preview", image);
			image.setMedia((VFSLeaf)logo);
			image.setMaxWithAndHeightToFitWithin(300, 300);
			logoDel = uifactory.addFormLink("logo.delete", logoImgFlc, Link.BUTTON_XSMALL);
			logoDel.setUserObject(logo);
			logoDel.setVisible(editable);
			return;
		}	
		logoImgFlc.setVisible(false);
		logoImgFlc.remove(logoImgFlc.getComponent("preview"));
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
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		OLATResource courseRes = courseEntry.getOlatResource();
		if(CourseFactory.isCourseEditSessionOpen(courseRes.getResourceableId())) {
			showWarning("error.editoralreadylocked", new String[] { "???" });
			return;
		}
		
		String selection = styleSel.getSelectedKey();
		
		ICourse course = CourseFactory.openCourseEditSession(courseRes.getResourceableId());
		courseEnvironment = course.getCourseEnvironment();
		courseConfig = courseEnvironment.getCourseConfig();
		courseConfig.setCssLayoutRef(selection);
		
		if(CourseLayoutHelper.CONFIG_KEY_CUSTOM.equals(selection)){
			Map<String, Map<String, String>> customConfig = compileCustomConfigFromGuiWrapper();		
			customCMgr.saveCustomConfigAndCompileCSS(customConfig, courseEnvironment);
			persistedCustomConfig = customConfig;
			if (!elWithErrorExists) {
				prepareStyleEditor(customConfig);
			}
		}
		
		boolean menuEnabled = menuEl.isSelected(0);
		courseConfig.setMenuEnabled(menuEnabled);
		if (menuNodeIconsEl.isVisible()) {
			courseConfig.setMenuNodeIconsEnabled(menuNodeIconsEl.isAtLeastSelected(1));
		}
		if (menuPathEl.isVisible()) {
			courseConfig.setMenuPathEnabled(menuPathEl.isAtLeastSelected(1));
		}
		boolean breadCrumbEnabled = breadCrumbEl.isSelected(0);
		courseConfig.setBreadCrumbEnabled(breadCrumbEnabled);
		
		ImageSourceType type =  teaserImageTypeEl.isOneSelected()
				? ImageSourceType.toEnum(teaserImageTypeEl.getSelectedKey())
				: ImageSourceType.DEFAULT_COURSE;
		if (ImageSourceType.system == type && teaserImageSystemEl.isOneSelected()) {
			teaserImageSource = courseStyleService.getSystemTeaserImageSource(teaserImageSystemEl.getSelectedKey());
		} else if (ImageSourceType.custom == type) {
			if (teaserImageUploadEl.getUploadFile() != null) {
				teaserImageSource = courseStyleService.storeImage(course, getIdentity(),
						teaserImageUploadEl.getUploadFile(), teaserImageUploadEl.getUploadFileName());
			} else if (teaserImageUploadEl.getInitialFile() != null) {
				teaserImageSource = courseConfig.getTeaserImageSource();
			}
		} else {
			teaserImageSource = courseStyleService.createEmptyImageSource(type);
		}
		courseConfig.setTeaserImageSource(teaserImageSource);
		
		if (ImageSourceType.custom != type) {
			courseStyleService.deleteImage(course);
			teaserImageUploadEl.setInitialFile(null);
			teaserImageUploadEl.reset();
		}
			
		courseConfig.setTeaserImageStyle(teaserImageStyle);
		
		courseConfig.setColorCategoryIdentifier(colorCategoryIdentifier);
		
		CourseFactory.setCourseConfig(course.getResourceableId(), courseConfig);
		CourseFactory.closeCourseEditSession(course.getResourceableId(), true);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
    		.fireEventToListenersOf(new CourseConfigEvent(CourseConfigType.layout, course.getResourceableId()), course);
		
		// inform course-settings-dialog about changes:
		fireEvent(ureq, Event.CHANGED_EVENT);
		fireEvent(ureq, RunMainController.RELOAD_COURSE_NODE);
	}
	
	private Map<String, Map<String, String>> compileCustomConfigFromGuiWrapper(){
		// get config from wrapper-object
		elWithErrorExists = false;
		Map<String, Map<String, String>> customConfig = new HashMap<>();
		for (Iterator<Entry<String, Map<String, FormItem>>> iterator = guiWrapper.entrySet().iterator(); iterator.hasNext();) {
			Entry<String, Map<String, FormItem>> type =  iterator.next();
			String cIdent = type.getKey();
			Map<String, String> elementConfig = new HashMap<>();
			Map<String, FormItem> element = type.getValue();
			for (Entry<String, FormItem> entry : element.entrySet()) {
				String attribName = entry.getKey();
				if (!attribName.equals(PreviewLA.IDENTIFIER)){ // exclude preview
					FormItem foItem = entry.getValue();
					String value = "";
					if (foItem instanceof SingleSelection) {
						value = ((SingleSelection)foItem).isOneSelected() ? ((SingleSelection)foItem).getSelectedKey() : "";
					} else if (foItem.getUserObject() instanceof SpecialAttributeFormItemHandler) {
						// enclosed item
						SpecialAttributeFormItemHandler specHandler = (SpecialAttributeFormItemHandler) foItem.getUserObject();
						value = specHandler.getValue();						
						if (specHandler.hasError()) {
							elWithErrorExists = true;
						}
					} else {
						throw new AssertException("implement a getValue for this FormItem to get back a processable value.");
					}
					elementConfig.put(attribName, value);
				}
			}			
			customConfig.put(cIdent, elementConfig);
		}
		return customConfig;		
	}
	
	
	private void prepareStyleEditor(Map<String, Map<String, String>> customConfig){
		guiWrapper = new LinkedHashMap<>(); //keep config order

		List<AbstractLayoutElement> allElements = customCMgr.getAllAvailableElements();
		List<AbstractLayoutAttribute> allAttribs = customCMgr.getAllAvailableAttributes();
		styleFlc.contextPut("allAttribs", allAttribs);
		styleFlc.setUserObject(this); // needed reference to get listener back.
		
		for (AbstractLayoutElement abstractLayoutElement : allElements) {			
			String elementType = abstractLayoutElement.getLayoutElementTypeName();
			Map<String, String> elConf = customConfig.get(elementType);
			AbstractLayoutElement concreteElmt = abstractLayoutElement.createInstance(elConf);
			
			HashMap<String, FormItem> elAttribGui = new HashMap<>();

			List<AbstractLayoutAttribute> attributes = concreteElmt.getAvailableAttributes();
			for (AbstractLayoutAttribute attrib : attributes) {
				String compName = elementType + ELEMENT_ATTRIBUTE_DELIM + attrib.getLayoutAttributeTypeName();
				FormItem fi = attrib.getFormItem(compName, styleFlc);
				fi.addActionListener(FormEvent.ONCHANGE);
				elAttribGui.put(attrib.getLayoutAttributeTypeName(), fi);
			}			
			guiWrapper.put(elementType, elAttribGui);			
		}		
		styleFlc.contextPut("guiWrapper", guiWrapper);
	}
	
	private void updateMenuUI(boolean dirty) {
		boolean menuEnabled = menuEl.isSelected(0);
		menuNodeIconsEl.setVisible(learningPath && menuEnabled);
		menuPathEl.setVisible(learningPath && menuEnabled);
		if (dirty) {
			markDirty();
		}
	}
	
	private void updateTeaserImageUI() {
		ImageSourceType type = teaserImageTypeEl.isOneSelected()
				? ImageSourceType.toEnum(teaserImageTypeEl.getSelectedKey())
				: ImageSourceType.DEFAULT_COURSE;
		teaserImageUploadEl.setVisible(ImageSourceType.custom == type);
		teaserImageSystemEl.setVisible(ImageSourceType.system == type);
		if (teaserImageSystemEl.isVisible() && !teaserImageSystemEl.isOneSelected() && teaserImageSystemEl.getKeys().length > 0) {
			teaserImageSystemEl.select(teaserImageSystemEl.getKey(0), true);
		}
		
		String teaserImageExampleKey = teaserImageStyleEl.isOneSelected() && teaserImageStyleEl.isKeySelected(TeaserImageStyle.cover.name())
				? "teaser.image.upload.example.cover"
				: "teaser.image.upload.example";
		teaserImageUploadEl.setExampleKey(teaserImageExampleKey, null);
	}

	private void updateColorCategorySelectionUI() {
		if (colorCategoryEl.isOneSelected()) {
			String selectedKey = colorCategoryEl.getSelectedKey();
			if (!COLOR_CATEGORY_CUSTOM.equals(selectedKey)) {
				colorCategoryIdentifier = selectedKey;
			}
			updateColorCategoryUI();
		}
	}
	
	private void doChooseColorCategory(UserRequest ureq) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(colorCategoryChooserCtrl);
		
		colorCategoryChooserCtrl = new ColorCategoryChooserController(ureq, getWindowControl(), SEARCH_PARAMS);
		listenTo(colorCategoryChooserCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				colorCategoryChooserCtrl.getInitialComponent(), colorCategorySelectionEl.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void updateColorCategoryUI() {
		boolean custom = colorCategoryEl.isOneSelected() && COLOR_CATEGORY_CUSTOM.equals(colorCategoryEl.getSelectedKey());
		if (custom) {
			ColorCategory colorCategory = colorCategoryResolver.getColorCategory(colorCategoryIdentifier, null);
			colorCategoryIdentifier = colorCategory.getIdentifier();
			String categoryName = CourseStyleUIFactory.translate(getTranslator(), colorCategory);
			String iconLeftCss = CourseStyleUIFactory.getIconLeftCss(colorCategory);
			colorCategorySelectionEl.setI18nKey(categoryName);
			colorCategorySelectionEl.setIconLeftCSS(iconLeftCss);
		}
		colorCategorySelectionEl.setVisible(custom);
	}

	private void updateHeaderPreviewUI(UserRequest ureq, boolean dirty) {
		removeAsListenerAndDispose(headerCtrl);
		headerCtrl = null;
		
		Header header = createPreviewHeader();
		headerCtrl = new HeaderController(ureq, getWindowControl(), header);
		listenTo(headerCtrl);
		headerPreviewCont.put("header", headerCtrl.getInitialComponent());
	
		if (dirty) {
			markDirty();
		}
	}
	
	private Header createPreviewHeader() {
		Builder builder = Header.builder();
		builder.withIconCss("o_CourseModule_icon");
		builder.withTitle(translate("preview.header.title"));
		ColorCategory colorCategory = colorCategoryResolver.getColorCategory(colorCategoryIdentifier, null);
		String colorCategoryCss = colorCategoryResolver.getCss(colorCategory);
		builder.withColorCategoryCss(colorCategoryCss);
		Mapper mapper = null;
		Boolean transparent = false;
		if (teaserImageUploadEl.isVisible()) {
			if (teaserImageUploadEl.getUploadFile() != null) {
				mapper = new VFSMediaMapper(teaserImageUploadEl.getUploadFile());
				transparent = courseStyleService.isImageTransparent(teaserImageUploadEl.getUploadFile());
			} else if (teaserImageUploadEl.getInitialFile() != null) {
				mapper = new VFSMediaMapper(teaserImageUploadEl.getInitialFile());
				transparent = courseStyleService.isImageTransparent(teaserImageUploadEl.getInitialFile());
			}
		} else if (teaserImageSystemEl.isVisible()) {
			String selectedKey =  teaserImageSystemEl.isOneSelected()
					? teaserImageSystemEl.getSelectedKey()
					: teaserImageSystemEl.getKeys().length > 0? teaserImageSystemEl.getKey(0): null;
			File file = courseStyleService.getSystemTeaserImageFile(selectedKey);
			if (file != null) {
				mapper = new VFSMediaMapper(file);
				transparent = courseStyleService.isImageTransparent(file);
			}
		}
		if (mapper != null) {
			builder.withTeaserImage(mapper, transparent, teaserImageStyle);
		}
		
		return builder.build();
	}

}
