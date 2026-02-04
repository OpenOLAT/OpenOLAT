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
package org.olat.course.nodes.video;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.id.Organisation;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSContainerMapper;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.course.nodes.videotask.ui.ConfirmChangeVideoController;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Editcontroller of videonode
 * 
 * @author dfakae, dirk.furrer@frentix.com, http://www.frentix.com
 *
 */
public class VideoEditController extends ActivateableTabbableDefaultController implements ControllerEventListener, ReferenceContentProvider {

	private static final List<String> RESOURCE_TYPES = List.of(VideoFileResource.TYPE_NAME);

	public static final String PANE_TAB_VIDEOCONFIG = "pane.tab.videoconfig";
	private static final String[] paneKeys = { PANE_TAB_VIDEOCONFIG};

	public static final String NLS_ERROR_VIDEOREPOENTRYMISSING = "error.videorepoentrymissing";
	public static final String NLS_ERROR_VIDEOREPOENTRYDELETED = "error.videorepoentrydeleted";
	public static final String NLS_ERROR_VIDEOREPOENTRYRESTRICTED = "error.videorepoentryrestricted";

	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	public static final String CONFIG_KEY_AUTOPLAY = "autoplay";
	public static final String CONFIG_KEY_COMMENTS = "comments";
	public static final String CONFIG_KEY_RATING = "rating";
	public static final String CONFIG_KEY_FORWARD_SEEKING_RESTRICTED = "forwardSeekingRestricted";
	public static final String CONFIG_KEY_TITLE = "title";
	public static final String CONFIG_KEY_COURSE_SPECIFIC_COMMENTS_RATINGS = "courseSpecificCommentsRatings";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT = "descriptionSelect";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT_NONE = "none";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT_RESOURCE = "resourceDescription";
	public static final String CONFIG_KEY_DESCRIPTION_SELECT_CUSTOM = "customDescription";

	public static final String CONFIG_KEY_QUESTIONS = "questions";
	public static final String CONFIG_KEY_ANNOTATIONS = "annotations";
	public static final String CONFIG_KEY_SEGMENTS = "segments";
	public static final String CONFIG_KEY_OVERLAY_COMMENTS = "overlayComments";
	
	public static final String CONFIG_KEY_DESCRIPTION_CUSTOMTEXT = "descriptionText";

	private TabbedPane myTabbedPane;
	private final VelocityContainer configurationVC;
	private final IconPanelLabelTextContent iconPanelContent;

	private final ModuleConfiguration config;
	private final VideoCourseNode videoNode;

	private CloseableModalController cmc;
	private VideoDisplayController previewCtrl;
	private VideoOptionsForm videoOptionsCtrl;
	private final RepositoryEntryReferenceController referenceCtrl;
	private ConfirmChangeVideoController confirmChangeVideoCtrl;

	@Autowired
	private VideoManager videoManager;
	@Autowired
	private RepositoryService repositoryService;

	public VideoEditController(UserRequest ureq, WindowControl wControl, VideoCourseNode videoNode, ICourse course) {
		super(ureq, wControl);
		
		this.videoNode = videoNode;
		config = videoNode.getModuleConfiguration();
		configurationVC = createVelocityContainer("edit");
		
		iconPanelContent = new IconPanelLabelTextContent("content");

		RepositoryEntry videoEntry = getVideoReference(config, false);
		String selectionTitle = translate("select.video.learning.resource");
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.video.resource.selected"))
				.withIconCss("o_icon o_FileResource-MOVIE_icon").build();
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		List<Organisation> defaultOrganisations = repositoryService.getOrganisations(courseEntry);
		CourseNodeReferenceProvider referenceProvider = new CourseNodeReferenceProvider(repositoryService,
				RESOURCE_TYPES, defaultOrganisations, emptyStateConfig, selectionTitle, this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, videoEntry, referenceProvider);
		listenTo(referenceCtrl);
		configurationVC.put("reference", referenceCtrl.getInitialComponent());

		putInitialPanel(configurationVC);
		updateEditController(ureq, videoEntry);
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		//
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_VIDEOCONFIG), "o_sel_video_configuration", configurationVC);
	}

	@Override
	public String[] getPaneKeys() {
		return paneKeys;
	}

	@Override
	public TabbedPane getTabbedPane() {
		return myTabbedPane;
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				RepositoryEntry newEntry = referenceCtrl.getRepositoryEntry();
				RepositoryEntry currentEntry = getVideoReference(config, false);
				referenceCtrl.setRepositoryEntry(ureq, currentEntry);
				doConfirmChange(ureq, newEntry, currentEntry);
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(ureq);
			}
		} else if (source == videoOptionsCtrl) {
			if (event == NodeEditController.NODECONFIG_CHANGED_EVENT){
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			}
		} else if (source == confirmChangeVideoCtrl) {
			if (event == Event.DONE_EVENT) {
				doChangeResource(ureq, confirmChangeVideoCtrl.getNewVideoEntry());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(confirmChangeVideoCtrl);
		removeAsListenerAndDispose(cmc);
		confirmChangeVideoCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	private void doConfirmChange(UserRequest ureq, RepositoryEntry newEntry, RepositoryEntry currentEntry) {
		if (currentEntry == null || currentEntry.equals(newEntry)) {
			doChangeResource(ureq, newEntry);
		} else {
			confirmChangeVideoCtrl = new ConfirmChangeVideoController(ureq, getWindowControl(), newEntry);
			listenTo(confirmChangeVideoCtrl);
			String title = translate("replace.video");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), 
					confirmChangeVideoCtrl.getInitialComponent(), title);
			listenTo(cmc);
			cmc.activate();
		}
	}

	private void doChangeResource(UserRequest ureq, RepositoryEntry newEntry) {
		doVideoReference(ureq, newEntry);
		updateEditController(ureq, newEntry);
	}

	private void updateEditController(UserRequest ureq, RepositoryEntry videoEntry) {
		removeAsListenerAndDispose(videoOptionsCtrl);
		videoOptionsCtrl = null;

		if (videoEntry == null) {
			configurationVC.contextPut("showOptions", Boolean.FALSE);
		} else {
			updateReferenceContentUI(videoEntry);

			configurationVC.contextPut("showOptions", Boolean.TRUE);
			videoOptionsCtrl = new VideoOptionsForm(ureq, getWindowControl(), videoEntry, config);
			configurationVC.put("videoOptions", videoOptionsCtrl.getInitialComponent());
			listenTo(videoOptionsCtrl);
		}
	}

	private void updateReferenceContentUI(RepositoryEntry videoEntry) {
		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);

		VideoSegments segments = videoManager.loadSegments(videoEntry.getOlatResource());
		int numOfSegments = 0;
		int numOfTerms = 0;
		if (segments != null) {
			numOfSegments = segments.getSegments() == null ? 0 : segments.getSegments().size();
			numOfTerms = segments.getCategories() == null ? 0 : segments.getCategories().size();
		}
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("video.terms"), Integer.toString(numOfTerms)));
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("video.segments"), Integer.toString(numOfSegments)));

		iconPanelContent.setLabelTexts(labelTexts);

		String warning = videoManager.isInUse(videoEntry) ? translate("error.edit.restricted.in.use") : null;
		iconPanelContent.setWarning(warning);
	}


	private void doVideoReference(UserRequest ureq, RepositoryEntry newEntry) {
		if (newEntry == null) {
			return;
		}

		referenceCtrl.setRepositoryEntry(ureq, newEntry);

		setVideoReference(newEntry, config);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doPreview(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(previewCtrl);
		
		RepositoryEntry repositoryEntry = getVideoReference(config, false);
		if (videoManager.isRestrictedDomain(repositoryEntry)) {
			showWarning(NLS_ERROR_VIDEOREPOENTRYRESTRICTED);
			return;
		}
		VideoDisplayOptions options = videoNode.getVideoDisplay(repositoryEntry, true);
		previewCtrl = new VideoDisplayController(ureq, getWindowControl(), repositoryEntry, null, null, options);
		listenTo(previewCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), previewCtrl.getInitialComponent(), 
				true, translate("command.preview"));
		listenTo(cmc);
		cmc.activate();
	}

	public static RepositoryEntry getVideoReference(ModuleConfiguration config, boolean strict) {
		if (config == null) {
			if (strict) {
				throw new AssertException("missing config in Video");
			}
			return null;
		}
		String repoSoftkey = (String) config.get(VideoEditController.CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) {
			if (strict) {
				throw new AssertException("invalid config when being asked for references");
			}
			return null;
		}
		return RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
	}

	public static boolean isModuleConfigValid(ModuleConfiguration moduleConfiguration) {
		boolean isValid = moduleConfiguration.get(CONFIG_KEY_REPOSITORY_SOFTKEY) != null;
		if (isValid) {
			Object repoEntry = getVideoReference(moduleConfiguration, false);
			if (repoEntry == null) {
				isValid = false;
				removeVideoReference(moduleConfiguration);
			}
		}
		
		return isValid;
	}

	public static void setVideoReference(RepositoryEntry re, ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}

	public static void removeVideoReference(ModuleConfiguration moduleConfiguration){
		moduleConfiguration.remove(CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
}

class VideoOptionsForm extends FormBasicController {

	/**
	 * Simple form for the Videooptions
	 *
	 * @author Dirk Furrer
	 */
	@Autowired	
	protected VideoManager videoManager;

	private SelectionElement videoComments;
	private SelectionElement videoRating;
	private SelectionElement videoAutoplay;
	private SelectionElement videoForwardSeekingRestricted;
	private SelectionElement title;
	private MultipleSelectionElement videoElements;
	private SingleSelection description;
	private RichTextElement descriptionField;
	private StaticTextElement descriptionRepoField;
	private boolean titleEnabled;
	private boolean commentsEnabled;
	private boolean ratingEnabled;
	private boolean autoplay;
	private boolean forwardSeekingRestricted;
	
	private String mediaRepoBaseUrl;
	private final RepositoryEntry repoEntry;
	private final ModuleConfiguration config;

	VideoOptionsForm(UserRequest ureq, WindowControl wControl, RepositoryEntry repoEntry, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.config = moduleConfiguration;
		this.repoEntry = repoEntry;
		
		commentsEnabled = config.getBooleanSafe(VideoEditController.CONFIG_KEY_COMMENTS);
		ratingEnabled = config.getBooleanSafe(VideoEditController.CONFIG_KEY_RATING);
		autoplay = config.getBooleanSafe(VideoEditController.CONFIG_KEY_AUTOPLAY);
		forwardSeekingRestricted = config.getBooleanSafe(VideoEditController.CONFIG_KEY_FORWARD_SEEKING_RESTRICTED);
		titleEnabled = config.getBooleanSafe(VideoEditController.CONFIG_KEY_TITLE);
		
		RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(repoEntry);
		VFSContainer mediaContainer = handler.getMediaContainer(repoEntry);
		if(mediaContainer != null) {
			mediaRepoBaseUrl = registerMapper(ureq, new VFSContainerMapper(mediaContainer.getParentContainer()));
		}
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_COMMENTS, videoComments.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_RATING, videoRating.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_AUTOPLAY, videoAutoplay.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_FORWARD_SEEKING_RESTRICTED, videoForwardSeekingRestricted.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_TITLE, title.isSelected(0));
		config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT, description.getSelectedKey());
		if("customDescription".equals(description.getSelectedKey())) {
			config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT, descriptionField.getValue());
		}
		
		Collection<String> selectedElements = videoElements.getSelectedKeys();
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_ANNOTATIONS,
				selectedElements.contains(VideoEditController.CONFIG_KEY_ANNOTATIONS));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_QUESTIONS,
				selectedElements.contains(VideoEditController.CONFIG_KEY_QUESTIONS));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_SEGMENTS,
				selectedElements.contains(VideoEditController.CONFIG_KEY_SEGMENTS));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_OVERLAY_COMMENTS,
				selectedElements.contains(VideoEditController.CONFIG_KEY_OVERLAY_COMMENTS));

		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer elementsCont = uifactory.addDefaultFormLayout("elements", null, formLayout);
		initElementsForm(elementsCont);
		
		FormLayoutContainer optionsCont = uifactory.addDefaultFormLayout("options", null, formLayout);
		optionsCont.setFormTitle(translate("optionsSection"));
		initOptionsForm(optionsCont);
	}
	
	private void initElementsForm(FormItemContainer formLayout) {
		SelectionValues elementsValues = new SelectionValues();
		elementsValues.add(SelectionValues.entry(VideoEditController.CONFIG_KEY_ANNOTATIONS, translate("video.config.elements.annotations")));
		elementsValues.add(SelectionValues.entry(VideoEditController.CONFIG_KEY_SEGMENTS, translate("video.config.elements.segments")));
		elementsValues.add(SelectionValues.entry(VideoEditController.CONFIG_KEY_OVERLAY_COMMENTS, translate("video.config.elements.comments")));
		elementsValues.add(SelectionValues.entry(VideoEditController.CONFIG_KEY_QUESTIONS, translate("video.config.elements.questions")));
		videoElements = uifactory.addCheckboxesVertical("videoElements", "video.config.elements", formLayout,
				elementsValues.keys(), elementsValues.values(), 1);
		videoElements.select(VideoEditController.CONFIG_KEY_ANNOTATIONS, config.getBooleanSafe(VideoEditController.CONFIG_KEY_ANNOTATIONS, true));
		videoElements.select(VideoEditController.CONFIG_KEY_QUESTIONS, config.getBooleanSafe(VideoEditController.CONFIG_KEY_QUESTIONS, true));
		videoElements.select(VideoEditController.CONFIG_KEY_SEGMENTS, config.getBooleanSafe(VideoEditController.CONFIG_KEY_SEGMENTS, false));
		videoElements.select(VideoEditController.CONFIG_KEY_OVERLAY_COMMENTS, config.getBooleanSafe(VideoEditController.CONFIG_KEY_OVERLAY_COMMENTS, false));
		videoElements.setElementCssClass("o_sel_video_elements");
	}
	
	private void initOptionsForm(FormItemContainer formLayout) {
		//add checkboxes for displayoptions
		videoComments = uifactory.addCheckboxesHorizontal("videoComments", "video.config.comments", formLayout, new String[]{"xx"}, new String[]{null});
		videoComments.select("xx",commentsEnabled);
		videoRating = uifactory.addCheckboxesHorizontal("videoRating", "video.config.rating", formLayout, new String[]{"xx"}, new String[]{null});
		videoRating.select("xx",ratingEnabled);
		
		uifactory.addSpacerElement("spacer1", formLayout, false);		
		videoAutoplay = uifactory.addCheckboxesHorizontal("videoAutoplay", "video.config.autoplay", formLayout, new String[]{"xx"}, new String[]{null});
		videoAutoplay.select("xx",autoplay);
		videoForwardSeekingRestricted = uifactory.addCheckboxesHorizontal("videoForwardSeekingAllowed", "video.config.forwardSeekingRestricted", formLayout, new String[]{"xx"}, new String[]{null});
		videoForwardSeekingRestricted.select("xx",forwardSeekingRestricted);

		String[] descriptionkeys = new String[]{ VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_NONE, VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_RESOURCE, VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_CUSTOM};
		String[] descriptionValues = new String[]{ translate("description.none"), translate("description.resource"), translate("description.custom") };

		uifactory.addSpacerElement("spacer2", formLayout, false);
		title = uifactory.addCheckboxesHorizontal("title", "video.config.title", formLayout, new String[]{"xx"}, new String[]{null});
		title.select("xx",titleEnabled);
		//add textfield for custom description
		description = uifactory.addDropdownSingleselect("video.config.description", formLayout, descriptionkeys, descriptionValues, null);
		description.addActionListener(FormEvent.ONCHANGE);
		description.select(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT, VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_NONE), true);
		String desc = repoEntry.getDescription();
		descriptionField = uifactory.addRichTextElementForStringDataMinimalistic("description", "", desc, -1, -1, formLayout, getWindowControl());
		descriptionRepoField = uifactory.addStaticTextElement("description.repo", "", "", formLayout);

		updateDescriptionField();
		uifactory.addFormSubmitButton("submit", formLayout);
		//init options-config
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_COMMENTS, videoComments.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_RATING, videoRating.isSelected(0));
		config.setBooleanEntry(VideoEditController.CONFIG_KEY_AUTOPLAY, videoAutoplay.isSelected(0));
		config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT, description.getSelectedKey());
		if(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_CUSTOM.equals(description.getSelectedKey())) {
			config.setStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT, descriptionField.getValue());
		}
	}

	/**
	 * Update visibility of the textfield for entering custom description
	 */
	private void updateDescriptionField() {
		String selectDescOption = description.getSelectedKey();
		if("none".equals(selectDescOption)) {
			descriptionField.setVisible(false);
			descriptionRepoField.setVisible(false);
		} else if(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_RESOURCE.equals(selectDescOption)) {
			descriptionField.setVisible(false);
			descriptionField.setEnabled(false);
			
			String text = repoEntry.getDescription();
			if(StringHelper.containsNonWhitespace(text)) {
				text = StringHelper.xssScan(text);
				if(mediaRepoBaseUrl != null) {
					text = FilterFactory.getBaseURLToMediaRelativeURLFilter(mediaRepoBaseUrl).filter(text);
				}
				text = Formatter.formatLatexFormulas(text);
			}
			descriptionRepoField.setValue(text);
			descriptionRepoField.setVisible(true);
		} else if(VideoEditController.CONFIG_KEY_DESCRIPTION_SELECT_CUSTOM.equals(selectDescOption)) {
			descriptionField.setVisible(true);
			descriptionField.setEnabled(true);
			descriptionField.setValue(config.getStringValue(VideoEditController.CONFIG_KEY_DESCRIPTION_CUSTOMTEXT, ""));
			descriptionRepoField.setVisible(false);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == description){
			updateDescriptionField();
		}
	}
}
