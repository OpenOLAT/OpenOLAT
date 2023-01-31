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
package org.olat.course.nodes.videotask.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.panel.IconPanelLabelTextContent;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.tabbable.ActivateableTabbableDefaultController;
import org.olat.core.logging.AssertException;
import org.olat.course.ICourse;
import org.olat.course.editor.CourseNodeReferenceProvider;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.fileresource.types.VideoFileResource;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.ui.VideoDisplayController;
import org.olat.modules.video.ui.VideoDisplayOptions;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.RepositoryEntryReferenceController;
import org.olat.repository.ui.RepositoryEntryReferenceProvider.ReferenceContentProvider;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskEditController extends ActivateableTabbableDefaultController implements ReferenceContentProvider {


	private static final List<String> RESOURCE_TYPES = List.of(VideoFileResource.TYPE_NAME);
	
	public static final String PANE_TAB_VIDEOCONFIG = "pane.tab.videoconfig";
	public static final String PANE_TAB_ASSESSMENT = "pane.tab.assessment";
	private static final String[] paneKeys = { PANE_TAB_VIDEOCONFIG, PANE_TAB_ASSESSMENT };
	
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "repoSoftKey";
	public static final String CONFIG_KEY_QUESTIONS = "questions";
	public static final String CONFIG_KEY_ANNOTATIONS = "annotations";
	public static final String CONFIG_KEY_SEGMENTS = "segments";
	
	public static final String CONFIG_KEY_CATEGORIES = "segmentCategories";
	public static final String CONFIG_KEY_SORT_CATEGORIES = "sortCategories";
	public static final String CONFIG_KEY_SORT_CATEGORIES_PRESET = "preset";
	public static final String CONFIG_KEY_SORT_CATEGORIES_ALPHABETICAL = "alphabetical";
	
	public static final String CONFIG_KEY_MODE = "taskMode";
	public static final String CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS = "practiceAssignTerms";
	public static final String CONFIG_KEY_MODE_PRACTICE_IDENTIFY_SITUATIONS = "practiceIdentifySituations";
	public static final String CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS = "testIdentifySituations";
	public static final String CONFIG_KEY_MODE_DEFAULT = CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS;
	
	public static final String CONFIG_KEY_ATTEMPTS = "attempts";
	public static final String CONFIG_KEY_ATTEMPTS_PER_SEGMENT = "attemptsPerSegment";
	public static final int CONFIG_KEY_ATTEMPTS_PER_SEGMENT_DEFAULT = 3;
	
	/** configuration: score can be set */
	public static final String CONFIG_KEY_SCORE_ROUNDING = "scoreRounding";
	
	public static final String CONFIG_KEY_WEIGHT_WRONG_ANSWERS = "weightWrongAnswers";
	
	private TabbedPane myTabbedPane;
	private VelocityContainer configurationVC;
	private final IconPanelLabelTextContent iconPanelContent;
	
	private int assessmentTab;
	private final RepositoryEntry entry;
	private final ModuleConfiguration config;
	private final VideoTaskCourseNode videoTaskNode;

	private CloseableModalController cmc;
	private VideoDisplayController previewCtrl;
	private VideoTaskConfigurationEditController configCtrl;
	private RepositoryEntryReferenceController referenceCtrl;
	private VideoTaskAssessmentEditController assessmentCtrl;
	private ConfirmChangeVideoController confirmChangeVideoCtrl;
	
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private RepositoryService repositoryService;
	
	public VideoTaskEditController(UserRequest ureq, WindowControl wControl, ICourse course, VideoTaskCourseNode videoTaskNode) {
		super(ureq, wControl);
		
		this.videoTaskNode = videoTaskNode;
		config = videoTaskNode.getModuleConfiguration();
		entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		configurationVC = createVelocityContainer("edit");
		
		iconPanelContent = new IconPanelLabelTextContent("content");
		
		EmptyStateConfig emptyStateConfig = EmptyStateConfig.builder()
				.withMessageTranslated(translate("no.video.resource.selected"))
				.withIconCss("o_icon o_FileResource-MOVIE_icon")
				.build();
		String selectionTitle = translate("select.video");
		RepositoryEntry videoEntry = getVideoReference(config, false);
		CourseNodeReferenceProvider referenceProvider = new CourseNodeReferenceProvider(repositoryService,
				RESOURCE_TYPES, emptyStateConfig, selectionTitle, this);
		referenceCtrl = new RepositoryEntryReferenceController(ureq, wControl, videoEntry, referenceProvider);
		listenTo(referenceCtrl);
		configurationVC.put("reference", referenceCtrl.getInitialComponent());
		
		assessmentCtrl = new VideoTaskAssessmentEditController(ureq, getWindowControl(), course, videoTaskNode);
		listenTo(assessmentCtrl);
		
		putInitialPanel(configurationVC);
		updateEditController(ureq, videoEntry, false);
	}

	@Override
	public Component getContent(RepositoryEntry repositoryEntry) {
		return iconPanelContent;
	}

	@Override
	public void refresh(Component cmp, RepositoryEntry repositoryEntry) {
		// Refresh is handled on change event.
		
	}

	@Override
	public void addTabs(TabbedPane tabbedPane) {
		myTabbedPane = tabbedPane;
		tabbedPane.addTab(translate(PANE_TAB_VIDEOCONFIG), configurationVC);
		assessmentTab = tabbedPane.addTab(translate(PANE_TAB_ASSESSMENT), assessmentCtrl.getInitialComponent());
		updateTabs();
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == referenceCtrl) {
			if (event == RepositoryEntryReferenceController.SELECTION_EVENT) {
				// Reset reference until the new entry is confirmed
				RepositoryEntry newEntry = referenceCtrl.getRepositoryEntry();
				RepositoryEntry currentEntry = getVideoReference(config, false);
				referenceCtrl.setRepositoryEntry(ureq, currentEntry);
				doConfirmChange(ureq, newEntry, currentEntry);
			} else if (event == RepositoryEntryReferenceController.PREVIEW_EVENT) {
				doPreview(ureq);
			}
		} else if(source == configCtrl) {
			if(event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				updateTabs();
				fireEvent(ureq, event);
			} else if(event == NodeEditController.NODECONFIG_CHANGED_REFRESH_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(source == assessmentCtrl) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
				fireEvent(ureq, NodeEditController.REMINDER_VISIBILITY_EVENT);
			} else if (event == NodeEditController.NODECONFIG_CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		} else if(confirmChangeVideoCtrl == source) {
			if(event == Event.DONE_EVENT) {
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
		if (event == NodeEditController.NODECONFIG_CHANGED_EVENT){
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
		}
	}
	
	private void updateTabs() {
		String mode = this.config.getStringValue(VideoTaskEditController.CONFIG_KEY_MODE, VideoTaskEditController.CONFIG_KEY_MODE_PRACTICE_ASSIGN_TERMS);
		boolean testMode = VideoTaskEditController.CONFIG_KEY_MODE_TEST_IDENTIFY_SITUATIONS.equals(mode);
		myTabbedPane.setEnabled(assessmentTab, testMode);
	}
	
	private void doConfirmChange(UserRequest ureq, RepositoryEntry newEntry, RepositoryEntry currentEntry) {
		try {
			// Check something
			if(currentEntry == null || currentEntry.equals(newEntry)) {
				doChangeResource(ureq, newEntry);
			} else {
				confirmChangeVideoCtrl = new ConfirmChangeVideoController(ureq, getWindowControl(), newEntry);
				listenTo(confirmChangeVideoCtrl);
				String title = translate("replace.entry");
				
				cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmChangeVideoCtrl.getInitialComponent(), title);
				listenTo(cmc);
				cmc.activate();
			}
		} catch (Exception e) {
			logError("", e);
			showError("error.resource.corrupted");
		}
	}
	
	private void doChangeResource(UserRequest ureq, RepositoryEntry newEntry) {
		doVideoReference(ureq, newEntry);
		updateEditController(ureq, newEntry, true);
	}
	
	private void updateEditController(UserRequest ureq, RepositoryEntry videoEntry, boolean replacedVideo) {
		removeAsListenerAndDispose(configCtrl);
		configCtrl = null;
		
		if(replacedVideo) {
			config.setStringValue(VideoTaskEditController.CONFIG_KEY_MODE, VideoTaskEditController.CONFIG_KEY_MODE_DEFAULT);
			config.remove(VideoTaskEditController.CONFIG_KEY_ATTEMPTS);
			config.setIntValue(VideoTaskEditController.CONFIG_KEY_ATTEMPTS_PER_SEGMENT, CONFIG_KEY_ATTEMPTS_PER_SEGMENT_DEFAULT);
			config.remove(VideoTaskEditController.CONFIG_KEY_CATEGORIES);
			
			// reset assessment
			config.setStringValue(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, Boolean.FALSE.toString());
			assessmentCtrl.resetConfiguration();
			fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
			updateTabs();
		}
		
		configurationVC.contextPut("resouceAvailable", Boolean.valueOf(videoEntry != null));
		if(videoEntry == null) {
			configurationVC.remove("configform");
		} else {
			updateReferenceContentUI(videoEntry);
			
			configCtrl = new VideoTaskConfigurationEditController(ureq, getWindowControl(), videoEntry, entry, videoTaskNode);
			listenTo(configCtrl);
			configurationVC.put("configform", configCtrl.getInitialComponent());
		}
	}
	
	private void updateReferenceContentUI(RepositoryEntry videoEntry) {
		List<IconPanelLabelTextContent.LabelText> labelTexts = new ArrayList<>(4);
		
		VideoSegments segments = videoManager.loadSegments(videoEntry.getOlatResource());
		int numOfSegments = 0;
		int numOfCategories = 0;
		if(segments != null) {
			numOfSegments = segments.getSegments() == null ? 0 : segments.getSegments().size();
			numOfCategories = segments.getCategories() == null ? 0 : segments.getCategories().size();
		}
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("video.categories"), Integer.toString(numOfCategories)));
		labelTexts.add(new IconPanelLabelTextContent.LabelText(translate("video.segments"), Integer.toString(numOfSegments)));

		iconPanelContent.setLabelTexts(labelTexts);
	}
	
	private void doVideoReference(UserRequest urequest, RepositoryEntry re) {
		if (re == null) {
			return;
		}
		
		referenceCtrl.setRepositoryEntry(urequest, re);
		
		setVideoReference(re, config);
		fireEvent(urequest, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
	
	private void doPreview(UserRequest ureq) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(previewCtrl);
		
		RepositoryEntry repositoryEntry = getVideoReference(config, false);
		
		VideoDisplayOptions options = videoTaskNode.getVideoDisplay(repositoryEntry, true);
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
		String repoSoftkey = (String) config.get(CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (repoSoftkey == null) {
			if (strict) {
				throw new AssertException("invalid config when being asked for references");
			}
			return null;
		}
		return RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(repoSoftkey, strict);
	}

	/**
	 * Set the referenced repository entry.
	 *
	 * @param re
	 * @param moduleConfiguration
	 */
	public static void setVideoReference(RepositoryEntry re, ModuleConfiguration moduleConfiguration) {
		moduleConfiguration.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}

	public static void removeVideoReference(ModuleConfiguration moduleConfiguration){
		moduleConfiguration.remove(CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
}
