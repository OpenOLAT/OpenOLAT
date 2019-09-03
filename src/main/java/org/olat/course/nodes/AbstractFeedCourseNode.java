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
import java.util.Locale;

import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.feed.FeedNodeEditController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;

/**
 * The podcast course node.
 * 
 * <P>
 * Initial Date: Mar 30, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class AbstractFeedCourseNode extends GenericCourseNode {
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	protected ModuleConfiguration config;
	protected Condition preConditionReader, preConditionPoster, preConditionModerator;

	/**
	 * @param type
	 */
	public AbstractFeedCourseNode(String type) {
		super(type);
		updateModuleConfigDefaults(true);
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#updateModuleConfigDefaults(boolean)
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		this.config = getModuleConfiguration();
		if (isNewNode) {
			// No startpage
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			config.setConfigurationVersion(1);
			// restrict moderator access to course admins and owners
			preConditionModerator = getPreConditionModerator();
			preConditionModerator.setEasyModeCoachesAndAdmins(true);
			preConditionModerator.setConditionExpression(preConditionModerator.getConditionFromEasyModeConfiguration());
			preConditionModerator.setExpertMode(false);
			// restrict poster access to course admins and owners
			preConditionPoster = getPreConditionPoster();
			preConditionPoster.setEasyModeCoachesAndAdmins(true);
			preConditionPoster.setConditionExpression(preConditionPoster.getConditionFromEasyModeConfiguration());
			preConditionPoster.setExpertMode(false);
		}
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);

		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), this);
		NotificationsManager.getInstance().delete(subsContext);
	}

	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionReader, envMapper);
		postImportCondition(preConditionPoster, envMapper);
		postImportCondition(preConditionModerator, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionReader, envMapper, backwardsCompatible);
		postExportCondition(preConditionPoster, envMapper, backwardsCompatible);
		postExportCondition(preConditionModerator, envMapper, backwardsCompatible);
	}

	/**
	 * @see org.olat.course.nodes.AbstractAccessableCourseNode#createNodeRunConstructionResult(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation, java.lang.String)
	 */
	@Override
	public abstract NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl control,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd);

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#isConfigValid(org.olat.course.editor.CourseEditorEnv)
	 */
	@Override
	public abstract StatusDescription[] isConfigValid(CourseEditorEnv cev);

	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		this.config = getModuleConfiguration();
		String repoSoftkey = (String) config.get(CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryManager rm = RepositoryManager.getInstance();
		return rm.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	/**
	 * @return Returns the preConditionModerator.
	 */
	public Condition getPreConditionModerator() {
		if (preConditionModerator == null) {
			preConditionModerator = new Condition();
		}
		preConditionModerator.setConditionId("moderator");
		return preConditionModerator;
	}

	/**
	 * @param preConditionModerator The preConditionModerator to set.
	 */
	public void setPreConditionModerator(Condition preConditionModerator) {
		if (preConditionModerator == null) {
			preConditionModerator = getPreConditionModerator();
		}
		preConditionModerator.setConditionId("moderator");
		this.preConditionModerator = preConditionModerator;
	}

	/**
	 * @return Returns the preConditionPoster.
	 */
	public Condition getPreConditionPoster() {
		if (preConditionPoster == null) {
			preConditionPoster = new Condition();
		}
		preConditionPoster.setConditionId("poster");
		return preConditionPoster;
	}

	/**
	 * @param preConditionPoster The preConditionPoster to set.
	 */
	public void setPreConditionPoster(Condition preConditionPoster) {
		if (preConditionPoster == null) {
			preConditionPoster = getPreConditionPoster();
		}
		preConditionPoster.setConditionId("poster");
		this.preConditionPoster = preConditionPoster;
	}

	/**
	 * @return Returns the preConditionReader.
	 */
	public Condition getPreConditionReader() {
		if (preConditionReader == null) {
			preConditionReader = new Condition();
		}
		preConditionReader.setConditionId("reader");
		return preConditionReader;
	}

	/**
	 * @param preConditionReader The preConditionReader to set.
	 */
	public void setPreConditionReader(Condition preConditionReader) {
		if (preConditionReader == null) {
			preConditionReader = getPreConditionReader();
		}
		preConditionReader.setConditionId("reader");
		this.preConditionReader = preConditionReader;
	}

	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		// evaluate the preconditions
		boolean reader = (getPreConditionReader().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionReader()));
		nodeEval.putAccessStatus("reader", reader);
		boolean poster = (getPreConditionPoster().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionPoster()));
		nodeEval.putAccessStatus("poster", poster);
		boolean moderator = (getPreConditionModerator().getConditionExpression() == null ? true : ci
				.evaluateCondition(getPreConditionModerator()));
		nodeEval.putAccessStatus("moderator", moderator);

		boolean visible = (getPreConditionVisibility().getConditionExpression() == null ? true : ci
				.evaluateCondition(getPreConditionVisibility()));
		nodeEval.setVisible(visible);
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = getReferencedRepositoryEntry();
		if (re == null) return;
		// build current export ZIP for feed learning resource
		FeedManager.getInstance().getFeedArchive(re.getOlatResource());
		// trigger resource file export
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}

	public void importFeed(RepositoryHandler handler, File importDirectory, Identity owner, Organisation organisation, Locale locale) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if (rie.anyExportedPropertiesAvailable()) {
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			FeedNodeEditController.setReference(re, getModuleConfiguration());
		} else {
			FeedNodeEditController.removeReference(getModuleConfiguration());
		}
	}
}
