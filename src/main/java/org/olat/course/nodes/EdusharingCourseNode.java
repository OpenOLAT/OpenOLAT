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
package org.olat.course.nodes;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.edusharing.ui.EdusharingEditController;
import org.olat.course.nodes.edusharing.ui.EdusharingRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.edusharing.EdusharingException;
import org.olat.modules.edusharing.EdusharingHtmlElement;
import org.olat.modules.edusharing.EdusharingProvider;
import org.olat.modules.edusharing.EdusharingService;
import org.olat.modules.edusharing.EdusharingUsage;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.repository.ui.settings.RepositoryEdusharingProvider;

/**
 * 
 * Initial date: 20 May 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = -2441585212441673115L;

	private static final Logger log = Tracing.createLoggerFor(EdusharingCourseNode.class);
	
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(EdusharingRunController.class);

	public static final String TYPE = "edusharing";
	public static final String ICON_CSS = "o_edusharing_icon";
	
	// configuration
	private static final int CURRENT_VERSION = 1;
	public static final String CONFIG_VERSION = "version";
	public static final String CONFIG_VERSION_VALUE_CURRENT = "current";
	public static final String CONFIG_VERSION_VALUE_LATEST = "latest";
	public static final String CONFIG_SHOW_LICENSE = "show.license";
	public static final String CONFIG_SHOW_METADATA = "show.metadata";
	public static final String CONFIG_IDENTIFIER = "identifier";
	public static final String CONFIG_ES_OBJECT_URL = "es.object.url";
	public static final String CONFIG_ES_TITLE = "es.title";
	public static final String CONFIG_ES_MIME_TYPE = "es.mimetype";
	public static final String CONFIG_ES_MEDIA_TYPE = "es.media.type";
	public static final String CONFIG_ES_RESOURCE_TYPE = "es.resource.type";
	public static final String CONFIG_ES_RESOURCE_VERSION = "es.resource.version";
	public static final String CONFIG_ES_REPO_TYPE = "es.repo.type";
	public static final String CONFIG_ES_WINDOW_VERISON = "es.window.version";
	public static final String CONFIG_ES_WINDOW_HEIGHT = "es.window.height";
	public static final String CONFIG_ES_WINDOW_WIDTH = "es.window.width";
	public static final String CONFIG_ES_RATIO = "es.ratio";
	
	public EdusharingCourseNode() {
		this(null);
	}
	
	public EdusharingCourseNode(CourseNode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment userCourseEnv) {
		RepositoryEntry courseEntry = userCourseEnv.getCourseEditorEnv().getCourseGroupManager().getCourseEntry();
		EdusharingEditController editCtrl = new EdusharingEditController(ureq, wControl, stackPanel, this, courseEntry);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(userCourseEnv.getCourseEditorEnv().getCurrentCourseNodeId());
		NodeEditController nodeEditCtr = new NodeEditController(ureq, wControl, stackPanel, course,
				chosenNode, userCourseEnv, editCtrl);
		nodeEditCtr.addControllerListener(editCtrl);
		return nodeEditCtr;
	}
	
	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller controller = new EdusharingRunController(ureq, wControl, this);
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, ICON_CSS);
		return new NodeRunConstructionResult(ctrl);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		StatusDescription sd = StatusDescription.NOERROR;
		if (!getModuleConfiguration().has(CONFIG_IDENTIFIER)) {
			String shortKey = "error.no.item.short";
			String longKey = "error.no.item.long";
			String[] params = new String[] { this.getShortTitle() };
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, TRANSLATOR_PACKAGE);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(EdusharingEditController.PANE_TAB_CONFIG);
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE,
				getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		
		if (isNewNode) {
			config.setStringValue(CONFIG_VERSION, CONFIG_VERSION_VALUE_CURRENT);
			config.setBooleanEntry(CONFIG_SHOW_LICENSE, true);
			config.setBooleanEntry(CONFIG_SHOW_METADATA, true);
		}
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}

	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		EdusharingService edusharingService = CoreSpringFactory.getImpl(EdusharingService.class);
		
		String identifier = getModuleConfiguration().getStringValue(CONFIG_IDENTIFIER);
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		RepositoryEdusharingProvider provider = new RepositoryEdusharingProvider(re, getIdent());
		List<EdusharingUsage> usages = edusharingService.loadUsages(provider.getOlatResourceable(), provider.getSubPath());
		for (EdusharingUsage usage : usages) {
			if (!usage.getIdentifier().equals(identifier)) {
				edusharingService.deleteUsage(usage);
			}
		}
		
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		postImportCopy(course, this, owner);
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCrourse, context);
		postImportCopy(course, this, envMapper.getAuthor());
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CourseNode copyInstance = super.createInstanceForCopy(isNewTitle, course, author);
		postImportCopy(course, copyInstance, author);
		return copyInstance;
	}

	private void postImportCopy(ICourse course, CourseNode courseNode, Identity identity) {
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		ModuleConfiguration config = courseNode.getModuleConfiguration();
		config.setStringValue(CONFIG_IDENTIFIER, createIdentifier());
		EdusharingHtmlElement element = createEdusharingHtmlElement(config);
		EdusharingUsage usage = null;
		try {
			usage = getOrCreateUsage(courseEntry, courseNode.getIdent(), element, identity);
		} catch (Exception e) {
			log.debug("", e);
		}
		if (usage == null) {
			log.warn("edu-sharing course node without usage! {}, nodeIdend={}", courseEntry, courseNode.getIdent());
		}
	}

	public String createIdentifier() {
		return UUID.randomUUID().toString().replace("-", "");
	}

	public EdusharingUsage getOrCreateUsage(RepositoryEntry courseEntry, String subIdent, EdusharingHtmlElement element,
			Identity identity) {
		EdusharingProvider provider = new RepositoryEdusharingProvider(courseEntry, subIdent);
		EdusharingService edusharingService = CoreSpringFactory.getImpl(EdusharingService.class);
		Optional<EdusharingUsage> usage = edusharingService
				.loadUsages(provider.getOlatResourceable(), provider.getSubPath())
				.stream()
				.filter(u -> element.getIdentifier().equals(u.getIdentifier()))
				.findFirst();
		if (usage.isPresent()) {
			return usage.get();
		}
		
		try {
			return edusharingService.createUsage(identity, element, provider);
		} catch (EdusharingException e) {
			log.info("Creation of edu-sharing usage failed.", e);
		}
		
		return null;
	}
	
	private EdusharingHtmlElement createEdusharingHtmlElement(ModuleConfiguration config) {
		String identifier = config.getStringValue(EdusharingCourseNode.CONFIG_IDENTIFIER);
		String objectUrl = config.getStringValue(EdusharingCourseNode.CONFIG_ES_OBJECT_URL);
		EdusharingHtmlElement element = new EdusharingHtmlElement(identifier, objectUrl);
		
		String version = config.getStringValue(EdusharingCourseNode.CONFIG_ES_WINDOW_VERISON);
		element.setVersion(version);

		String mimeType = config.getStringValue(EdusharingCourseNode.CONFIG_ES_MIME_TYPE);
		element.setMimeType(mimeType);
		
		String mediaType = config.getStringValue(EdusharingCourseNode.CONFIG_ES_MEDIA_TYPE);
		element.setMediaType(mediaType);
		
		String width = config.getStringValue(EdusharingCourseNode.CONFIG_ES_WINDOW_WIDTH);
		element.setWidth(width);
		
		String hight = config.getStringValue(EdusharingCourseNode.CONFIG_ES_WINDOW_HEIGHT);
		element.setHight(hight);
		
		return element;
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		EdusharingService edusharingService = CoreSpringFactory.getImpl(EdusharingService.class);
		
		RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		RepositoryEdusharingProvider provider = new RepositoryEdusharingProvider(re, getIdent());
		edusharingService.deleteUsages(provider);
		
		super.cleanupOnDelete(course);
	}

}
