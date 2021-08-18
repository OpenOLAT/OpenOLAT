/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.ShortName;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.course.style.ImageSource;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * Initial Date: Feb 9, 2004
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public interface CourseNode extends INode, ShortName {
	
	public static final String DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT = "shorttitle+desc+content";
	public static final String DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT = "title+desc+content";
	public static final String DISPLAY_OPTS_SHORT_TITLE_CONTENT = "shorttitle+content";
	public static final String DISPLAY_OPTS_TITLE_CONTENT = "title+content";
	public static final String DISPLAY_OPTS_DESCRIPTION_CONTENT = "desc+content";
	public static final String DISPLAY_OPTS_CONTENT = "content";

	public String getType();

	public void setIdent(String ident);

	public void setLongTitle(String longTitle);

	public String getShortTitle();
	
	public void setShortTitle(String shortTitle);

	public String getLongTitle();

	public String getDescription();

	public void setDescription(String description);

	public String getObjectives();
	
	public void setObjectives(String objectives);

	public String getInstruction();

	public void setInstruction(String instruction);

	public String getInstructionalDesign();

	public void setInstructionalDesign(String instructionalDesign);

	public String getDisplayOption();
	
	public void setDisplayOption(String displayOption);
	
	public ImageSource getTeaserImageSource();
	
	public void setTeaserImageSource(ImageSource imageSource);
	
	public String getColorCategoryIdentifier();
	
	public void setColorCategoryIdentifier(String colorCategoryIdentifier);

	/**
	 * Set the text that will show up when no access is granted to this node but
	 * the node is still visible to the user
	 * 
	 * @param noAccessExplanation
	 */
	public void setNoAccessExplanation(String noAccessExplanation);

	/**
	 * Get the text that will show up when no access is granted to this node but
	 * the node is still visible to the user
	 * 
	 * @return String
	 */
	public String getNoAccessExplanation();

	/**
	 * Set the visibility precondition. If this condition is true, the node is
	 * visible to the user
	 * 
	 * @param visibilityCondition
	 */
	public void setPreConditionVisibility(Condition visibilityCondition);

	/**
	 * Get the visibility precondition. If this condition is true, the node is
	 * visible to the user
	 * 
	 * @return String
	 */
	public Condition getPreConditionVisibility();

	/**
	 * Get the access precondition. If this condition is true, the node is
	 * accessable for the user
	 * 
	 * @return String
	 */
	public Condition getPreConditionAccess();

	/**
	 * used by the publish process to ensure the reference counters for a
	 * repository entry are correct. (you can only delete Repositoryentries if
	 * there are no references to it) returns exception when called even
	 * !needsReferenceToARespositoryEntry return the referenced RepositoryEntry if
	 * it can be found return null if the referenced RepositoryEntry could not be
	 * found
	 * 
	 * @return the RepositoryEntry (if it still exists in the repository) which is
	 *         referenced in this course node if there is a reference, or null
	 *         otherwise
	 */
	public RepositoryEntry getReferencedRepositoryEntry();

	/**
	 * @return true if this node type potentially has a repository entry
	 *         reference, false otherwhise. Attention: this method does not test
	 *         if ther is actually such a reference. Use getReferencedRepository()
	 *         to get this information.
	 */
	public boolean needsReferenceToARepositoryEntry();

	/**
	 * special configuration, used by the module, e.g. briefcase: quota,
	 * singlepage:chosen file, tunneling: chosen site, etc.
	 * 
	 * @return ModuleConfiguration
	 */
	public ModuleConfiguration getModuleConfiguration();

	/**
	 * Create a course run controller for this node
	 * 
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param userCourseEnv The course environment
	 * @param nodeSecCallback The node evaluation
	 * @return The node run controller
	 * 
	 * ATTENTION:
	 * udpateModuleConfigDefaults(false) should be called inside from the
	 * courseNode.createNodeRunConstructionResult(ureq, bwControl, userCourseEnv, nodeEval, nodecmd)
	 * to set the course node specific configuration default values!
	 */
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd);

	/**
	 * Create a node edit controller for this node to configure node specific
	 * features
	 * 
	 * @param ureq The user request
	 * @param wControl The current window controller
	 * @param course The course
	 * @param euce the editor user course environment provides syntax/semantic
	 *          check methods for conditions
	 * @return A tabbable node edit controller
	 */
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce);
	
	/**
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param nodeSecCallback
	 * @return Controller
	 */
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback);

	/**
	 * Create a minimised view for this course node that gives some insight about
	 * the real content of the view in a limited space. E.g. a forum could list
	 * the two most recent postings or active threads, a folder could show the
	 * newest files.
	 * 
	 * @param ureq
	 * @param userCourseEnv
	 * @param nodeSecCallback
	 * @param small TODO
	 * @param wContro
	 * @param smallPeekview Hint is the peekview should be small (in two columns)
	 * @return 
	 */
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small);
	
	/**
	 * Return a construct with all the informations needed to build the statistics of
	 * a course node.
	 * 
	 * @param ureq
	 * @param wControl
	 * @param userCourseEnv
	 * @param options
	 * @return
	 */
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, StatisticType type);
	
	public boolean isStatisticNodeResultAvailable(UserCourseEnvironment userCourseEnv, StatisticType type);

	/**
	 * @return true if the course node configuration is correct without the course
	 *         context.
	 * @see CourseNode#isConfigValid(UserCourseEnvironment) for a config
	 *      validation method taking the course environment into account.
	 */
	public StatusDescription isConfigValid();

	/**
	 * @param userCourseEnv
	 * @return true if the course node configuration is valid for itself and also
	 *         within the specified course environment.
	 */
	public StatusDescription[] isConfigValid(CourseEditorEnv cev);
	
	
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents);

	/**
	 * Called if this node is ABOUT TO BE deleted. For the time being, the node
	 * may provide a user message which is shown to the user within the publish
	 * confirm dialog.
	 * 
	 * @param ureq The user request
	 * @param course The course
	 * @return The dialogue message if any data will be deleted in the next step
	 */
	public String informOnDelete(Locale locale, ICourse course);

	/**
	 * Called if this node is deleted. Do any cleanup work here.
	 * 
	 * @param course The course
	 */
	public void cleanupOnDelete(ICourse course);

	/**
	 * Archive all node user data to the given directory. This might be one file
	 * or multiple files depending on what data is available. The archived data is
	 * not intendet to be imported again. The files should be viewable, e.g. as
	 * XML or excel files.
	 * 
	 * @param locale The users locale
	 * @param course The course
	 * @param options The options to generate the archive
	 * @param exportStream The directory where the exported files should be
	 *          put. This directory must exist prior to calling this method.
	 * @param path The path in the zip archive (without trailing /) or an empty string
	 * @param charset The charset property of current user
	 * @return true If any data to be archived was found, false otherwise.
	 */
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream, String path, String charset);

	/**
	 * Export all node user data to the given directory. This might be one file or
	 * multiple files depending on what data is available. The archived data is
	 * intendet to be imported again if the course this node is attached to gets
	 * imported.
	 * 
	 * @param exportDirectory The directory where the exported files should be
	 *          put. This directory must exist prior to calling this method.
	 * @param course
	 */
	public void exportNode(File exportDirectory, ICourse course);

	/**
	 * Import a course node's data. The import directory is the root of the
	 * directory with all the data that the node has written previousely during
	 * the export. The node can provide a Controller if any user intervention is
	 * needed. The controller should send a Event.DONE_EVENT after finishing the
	 * user driven import. If no user driven import is necessary, just return null
	 * right away after finishing all importing tasks.
	 * 
	 * @param importDirectory
	 * @param course
	 * @param owner 
	 * @param locale 
	 * @param withReferences 
	 * @param ureq
	 * @param wControl
	 * @return Controller for user driven import, or null after all import tasks
	 *         have finished.
	 */
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences);
	
	/**
	 * Remap the node to the context of the course after import and apply changes from the copy wizard
	 * @param sourceCrourse
	 * @param sourceCourse 
	 */
	public void postCopy(CourseEnvironmentMapper envMapper, Processing type, ICourse course, ICourse sourceCrourse, CopyCourseContext context);
	
	/**
	 * Remap the node to the context of the course after import.
	 */
	public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing type);
	
	/**
	 * 
	 */
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible);

	
	/**
	 * Try to copy the configuration of this course node to
	 * the one passed as parameter. Warning, the 2 nodes can
	 * be from different types.
	 * @param courseNode
	 * @param savedBy 
	 */
	public void copyConfigurationTo(CourseNode courseNode, ICourse course, Identity savedBy);
	
	/**
	 * Create an instance for the copy process. The copy must have a different
	 * unique ID and may take some of the configuration values configured for this
	 * node.
	 * 
	 * @param isNewTitle
	 * @param course the course in which the copying is happening
	 * @return
	 */
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author);

	/**
	 * @return empty list, or list with active condition expressions of the course
	 *         node
	 */
	public List<ConditionExpression> getConditionExpressions();

	/**
	 * explain what the given status description means in the publish environment
	 * 
	 * @param description
	 * @return
	 */
	public StatusDescription explainThisDuringPublish(StatusDescription description);
	
	/**
	 * Return some explanations if an update is planned after publishing
	 * @param cev
	 * @return
	 */
	public List<StatusDescription> publishUpdatesExplanations(CourseEditorEnv cev);
	
	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values.
	 * 
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behavior.
	 * @param parent Can be used to inherit default values.
	 *          May be null if root node. Is of type CourseNode or CourseEditorTreeNode.
	 *          
	 * This is the workflow:
	 * On every click on a entry of the navigation tree, this method will be called
	 * to ensure a valid configration of the depending module. This is only done in
	 * RAM. If the user clicks on that node in course editor and publishes the course
	 * after that, then the updated config will be persisted to disk. Otherwise
	 * everything what is done here has to be done once at every course start.<br>
	 * Every click is too much. Only call this method on course start since changed config will be cached.
	 * If you cache something in course nodes be aware to set such a variable TRANSIENT, 
	 * otherwise the editortree.xml and runstructure.xml of old courses would no longer be compatible. 
	 *  
	 */
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent);
	
	/**
	 * Calculate the access and the visibility in the conventional node access type.
	 *
	 * @param ci
	 * @param nodeEval
	 */
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval);
	
	/**
	 * The rule provider controls the reminder tab in the course editor.
	 * If a course node implements this methods, the Event NodeEditController.REMINDER_VISIBILITY_EVENT
	 * should probably be fired when the configurations have been changed.
	 * 
	 * @param rootNode
	 *
	 * @return
	 */
	public default CourseNodeReminderProvider getReminderProvider(boolean rootNode) {
		return null;
	}
	
	/**
	 * Determines whether the course node contains any dates. 
	 * This includes node specific dates, high score specific dates and date dependant user rights.
	 * 
	 * @return
	 */
	public boolean hasDates();
	
	/**
	 * Returns a map with an i18nKeys and dates
	 * 
	 * @return
	 */
	public List<Map.Entry<String, Date>> getNodeSpecificDatesWithLabel();
	
	public List<NodeRightType> getNodeRightTypes();
	
	public enum Processing {
		runstructure,
		editor
	}
}
