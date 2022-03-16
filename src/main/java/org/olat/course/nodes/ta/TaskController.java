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

package org.olat.course.nodes.ta;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.SystemFileFilter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.properties.Property;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  02.09.2004
 *
 * @author Mike Stock
 * 
 * Comment:  
 * 
 */

public class TaskController extends BasicController {
	
	private static final Logger log = Tracing.createLoggerFor(TaskController.class);
	
	private static final String ACTION_PREVIEW = "ta.preview";
	private static final String ACTION_SELECT = "seltask";
	private static final String ACTION_DESELECT = "deseltask";
	
	private static final String VC_NOMORETASKS = "nomoretasks";
	private static final String VC_ASSIGNEDTASK = "assignedtask";
	private static final String VC_ASSIGNEDTASK_NEWWINDOW = "newwindow";
	private static final String VC_TASKTEXT = "taskText";
	
	static final String PROP_SAMPLED = "smpl";
	/** Property key for "assigned" property. */
	public static final String PROP_ASSIGNED = "ass";
	
	/** Configuration parameter indicating manual selection task type. */
	public static final String TYPE_MANUAL = "manual";
	/** Configuration parameter indicating auto selection task type. */
	public static final String TYPE_AUTO = "auto";
	/** Configuration parameter indicating task-preview mode. */
	public static final String WITH_PREVIEW = "preview";
	/** Configuration parameter indicating non task-preview mode. */
	public static final String WITHOUT_PREVIEW = "no_preview";
	/** Configuration parameter indicating task-deselect mode. */
	public static final String WITH_DESELECT = "deselect";
	/** Configuration parameter indicating non task-deselect mode. */
	public static final String WITHOUT_DESELECT = "no_deselect";
	
	// config
	private String taskType;
	private String taskText;
	private File taskFolder;
	private boolean samplingWithReplacement = true;
	private Boolean hasPreview = Boolean.FALSE;
	private Boolean isDeselectable = Boolean.FALSE;

	private TACourseNode node;
	private CourseEnvironment courseEnv;
	private UserCourseEnvironment userCourseEnv;
	
	private VelocityContainer myContent;
	private Link taskLaunchButton;
	private TableController tableCtr;	
	private DeselectableTaskTableModel taskTableModel;
	private String assignedTask;
	
	private Panel panel;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	/**
	 * Implements a task component.
	 * @param ureq
	 * @param wControl
	 * @param config
	 * @param node
	 * @param courseEnv
	 */
	public TaskController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config, 
			TACourseNode node, UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);
		
		this.node = node;
		courseEnv = userCourseEnv.getCourseEnvironment();
		this.userCourseEnv = userCourseEnv;
		readConfig(config);
		
		panel = new Panel("myContentPanel");
		
		myContent = createVelocityContainer("taskAssigned");
		
		taskLaunchButton = LinkFactory.createButtonXSmall("task.launch", myContent, this);
		taskLaunchButton.setTarget("_blank");
		taskLaunchButton.setAjaxEnabled(false); // opened in new window
		
		if (StringHelper.containsNonWhitespace(taskText)) { 
			myContent.contextPut(VC_TASKTEXT, taskText);
		}

		// check if user already chose a task
		assignedTask = getAssignedTask(ureq.getIdentity(), courseEnv, node);
		if (assignedTask != null && !isDeselectable()) { // 
			pushTaskToVC();
		} else {
			// prepare choose task
			if (taskType.equals(TYPE_AUTO)) { // automatically choose a task
				assignedTask = assignTask(ureq.getIdentity());
				if (assignedTask != null)	{
					pushTaskToVC();
				}	else {
					myContent.contextPut(VC_NOMORETASKS, translate("task.nomoretasks"));
					panel.setContent(myContent);
				}
			} else { // let user choose a task, or show the table with the available/selected task
				
				myContent = createVelocityContainer("taskChoose");
				
				List<String> availableTasks = compileAvailableTasks();
				if (availableTasks.isEmpty() && assignedTask==null) { // no more tasks available
					myContent.contextPut(VC_NOMORETASKS, translate("task.nomoretasks"));
				} else {
				
				  TableGuiConfiguration tableConfig = new TableGuiConfiguration();
				  tableCtr = new TableController(tableConfig, ureq, wControl, getTranslator());
				  listenTo(tableCtr);
								
				  // No Preview Mode, Show only file-name
				  tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("task.table.th_task", 0, null, ureq.getLocale()));
				  if ( (hasPreview != null) && (hasPreview.booleanValue()==true )) {
				    // Preview Mode
					  DefaultColumnDescriptor columnDescriptor = new DefaultColumnDescriptor("task.table.th_task", 1, ACTION_PREVIEW, ureq.getLocale());
					  columnDescriptor.setIsPopUpWindowAction(true,DefaultColumnDescriptor.DEFAULT_POPUP_ATTRIBUTES );
					  tableCtr.addColumnDescriptor(columnDescriptor);
				  }
				  //always have a select column
				  String selectCmd = userCourseEnv.isCourseReadOnly() ? null : ACTION_SELECT;
				  tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("task.table.th_action", 2, selectCmd, translate("task.table.choose"), "-"));
											
				  int numCols = 0; 
				  Boolean taskCouldBeDeselected = config.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT);			
				  if(!hasPreview) {
					  numCols = 2;
				  } else if (taskCouldBeDeselected==null || !taskCouldBeDeselected){
					  numCols = 3;
				  } else if (taskCouldBeDeselected) {
					  numCols = 4;
					  String deselectCmd = userCourseEnv.isCourseReadOnly() ? null : ACTION_DESELECT;
					  tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("task.table.th_deselect",3, deselectCmd, translate("task.table.deselect"), "-"));
				  }	
				  //the table model shows the available tasks, plus the selected one, if deselectable
				  if(isDeselectable() && assignedTask!=null && !availableTasks.contains(assignedTask)) {
				  	availableTasks.add(assignedTask);
				  }
				  taskTableModel = new DeselectableTaskTableModel(availableTasks, numCols);
				  tableCtr.setTableDataModel(taskTableModel);
				  myContent.put("taskTable", tableCtr.getInitialComponent());				 
				}
			}
		}
		panel.setContent(myContent);
		putInitialPanel(panel);
	}
		

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		
		log.debug("Test Component.event source" + source + "  , event=" + event);
		
		if (source == taskLaunchButton) {
			//deliver files the same way as in preview
			doFileDelivery(ureq, assignedTask);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		
		log.debug("Test Controller.event source" + source + "  , event=" + event);
		
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent ta = (TableEvent) event;
				if (ta.getActionId().equals(TaskController.ACTION_PREVIEW)) {					
					String previewTask = (String)taskTableModel.getValueAt(ta.getRowId(),0);
					doFileDelivery(ureq, previewTask);
				} else if (ta.getActionId().equals(TaskController.ACTION_SELECT)){
					// select a task
					assignedTask = (String)taskTableModel.getValueAt(ta.getRowId(),0);
					List<String> availableTasks = compileAvailableTasks();
					if (!availableTasks.contains(assignedTask)) {
						showWarning("task.chosen");					
						taskTableModel.setObjects(availableTasks);
						tableCtr.modelChanged();
						if (availableTasks.size() == 0) { // no more tasks available
							myContent.contextPut(VC_NOMORETASKS, translate("task.nomoretasks"));
						}
					} else {
						setAssignedTask(ureq.getIdentity(), assignedTask);
						if (!samplingWithReplacement)	markTaskAsSampled(assignedTask);
						if(!isDeselectable()) {
						  pushTaskToVC();
						} else {
							//if assignedTask selected, and deselectable, update taskTableModel 
							List<String> allTasks = compileAvailableTasks();
							if(!samplingWithReplacement) {
								//if assignable to only one user, this means that the assignedTask is no more in the availableTasks, but show it in taskTableModel
							  allTasks.add(assignedTask);
							}						  
						  taskTableModel.setObjects(allTasks);
						  tableCtr.modelChanged();
						}
					}
				} else if (ta.getActionId().equals(TaskController.ACTION_DESELECT)) {
					if(assignedTask!=null) {
						removeAssignedTask(ureq.getIdentity());
						List<String> availableTasks = compileAvailableTasks();
						taskTableModel.setObjects(availableTasks);
						tableCtr.modelChanged();
					}
				}
			}
		}
	}
	
	private void pushTaskToVC() {
		if (assignedTask == null) {
			return;
		}
		
		myContent = createVelocityContainer ("taskAssigned");
		if (StringHelper.containsNonWhitespace(taskText)) {
			myContent.contextPut(VC_TASKTEXT, taskText);
		}

		//String taskFilename = getTaskFilename(assignedTask);
		//taskLaunchButton.setModURI(taskFilename);
		myContent.put("task.launch", taskLaunchButton);
		myContent.contextPut(VC_ASSIGNEDTASK, assignedTask);
		myContent.contextPut(VC_ASSIGNEDTASK_NEWWINDOW,Boolean.TRUE);
		myContent.contextPut("taskIcon", CSSHelper.createFiletypeIconCssClassFor(assignedTask));
		panel.setContent(myContent);
	}

	/**
	 * Auto-assign a task to an identity and mark it as sampled if necessary.
	 * @param identity
	 * @return name of the assigned task or null if no more tasks are available.
	 */
	private String assignTask(Identity identity) {
		List<String> availableTasks = compileAvailableTasks();
		if (availableTasks.isEmpty() && samplingWithReplacement) {
			unmarkAllSampledTasks(); // unmark all tasks if samplingWithReplacement and no more tasks available
			availableTasks = compileAvailableTasks(); // refetch tasks
		}
		if (availableTasks.isEmpty())	return null; // no more task available
		
		String task = availableTasks.get((new Random()).nextInt(availableTasks.size()));
		setAssignedTask(identity, task); // assignes the file to this identity
		if (!samplingWithReplacement)
			markTaskAsSampled(task); // remove the file from available files
		return task;
	}
	
	public static String getAssignedTask(Identity identity, CourseEnvironment courseEnv, CourseNode node) {
		List<Property> samples = courseEnv.getCoursePropertyManager().findCourseNodeProperties(node, identity, null, PROP_ASSIGNED);
		if (samples.isEmpty()) return null; // no sample assigned yet
		return samples.get(0).getStringValue();
	}

	private void setAssignedTask(Identity identity, String task) {
		CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
		Property p = cpm.createCourseNodePropertyInstance(node, identity, null, PROP_ASSIGNED, null, null, task, null);
		cpm.saveProperty(p);
		
		ScoreEvaluation eval = courseAssessmentService.getAssessmentEvaluation(node, userCourseEnv);
		if(eval.getAssessmentStatus() == null || eval.getAssessmentStatus() == AssessmentEntryStatus.notStarted) {
			eval = new ScoreEvaluation(eval.getScore(), eval.getGrade(), eval.getPerformanceClassIdent(),
					eval.getPassed(), AssessmentEntryStatus.inProgress, eval.getUserVisible(), null, null, null, null);
			courseAssessmentService.saveScoreEvaluation(node, getIdentity(), eval, userCourseEnv, false, Role.user);
		}
	}
	
	/**
	 * Cancel the task assignment.
	 * @param identity
	 * @param task
	 */
	private void removeAssignedTask(Identity identity) {
		CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
		//remove assigned
		List<Property> properties = cpm.findCourseNodeProperties(node, identity, null, PROP_ASSIGNED);
		if(properties!=null && properties.size()>0) {
		  Property propety = properties.get(0);
		  cpm.deleteProperty(propety);
		  assignedTask = null;
		}
		//removed sampled  				
		properties = courseEnv.getCoursePropertyManager().findCourseNodeProperties(node, null, null, PROP_SAMPLED);
		if(properties!=null && properties.size()>0) {
		  Property propety = properties.get(0);
		  cpm.deleteProperty(propety);		  
		}		
	}
	
	private void markTaskAsSampled(String task) {
		CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
		Property p = cpm.createCourseNodePropertyInstance(node, null, null, PROP_SAMPLED, null, null, task, null);
		cpm.saveProperty(p);
	}
	
	private void unmarkAllSampledTasks() {
		courseEnv.getCoursePropertyManager().deleteNodeProperties(node, PROP_SAMPLED);
	}
	
	/**
	 * Compiles a list of tasks based on the available files in the task folder,
	 * which have not been sampled so far.
	 * @return List of available tasks.
	 */
	private List<String> compileAvailableTasks() {
		File[] taskSources = taskFolder.listFiles(SystemFileFilter.FILES_ONLY);
		List<String> tasks = new ArrayList<>(taskSources.length);
		List<String> sampledTasks = compileSampledTasks();
		for (int i = 0; i < taskSources.length; i++) {
			File nextTask = taskSources[i];
			if (nextTask.isFile() && !sampledTasks.contains(nextTask.getName()))
					tasks.add(nextTask.getName());
		}
		return tasks;
	}

	/**
	 * Compile a list of tasks marked as sampled.
	 * @return List of sampled tasks.
	 */
	private List<String> compileSampledTasks() {
		List<String> sampledTasks = new ArrayList<>();
		List<Property> samples = courseEnv.getCoursePropertyManager()
			.findCourseNodeProperties(node, null, null, PROP_SAMPLED);
		for (Iterator<Property> iter = samples.iterator(); iter.hasNext();) {
			Property sample = iter.next();
			sampledTasks.add(sample.getStringValue());
		}
		return sampledTasks;
	}
	
	private void readConfig(ModuleConfiguration config) {
		// get task type
		taskType = (String)config.get(TACourseNode.CONF_TASK_TYPE);
		if (!(taskType.equals(TYPE_MANUAL) || taskType.equals(TYPE_AUTO)))
			throw new AssertException("Invalid task type: " + taskType);

		// get folder path
		String sTaskFolder = FolderConfig.getCanonicalRoot() + TACourseNode.getTaskFolderPathRelToFolderRoot(courseEnv, node);
		taskFolder = new File(sTaskFolder);
		if (!taskFolder.exists() && !taskFolder.mkdirs())
			throw new AssertException("Task folder " + sTaskFolder + " does not exist.");
		
		// get sampling type
		Boolean bSampling = (Boolean)config.get(TACourseNode.CONF_TASK_SAMPLING_WITH_REPLACEMENT);
		samplingWithReplacement = (bSampling == null) ? true : bSampling.booleanValue();
		
		// get task introductory text
		taskText = (String)config.get(TACourseNode.CONF_TASK_TEXT);
		
		hasPreview = config.getBooleanEntry(TACourseNode.CONF_TASK_PREVIEW)==null ? false : config.getBooleanEntry(TACourseNode.CONF_TASK_PREVIEW);
		
		isDeselectable = config.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT)==null ? false : config.getBooleanEntry(TACourseNode.CONF_TASK_DESELECT);
		
	}
		
	private boolean isDeselectable() {
		return isDeselectable;
	}

	/**
	 * deliver the selected file and show in a popup
	 tableController* 
	 * @param ureq
	 * @param command
	 */
	private boolean doFileDelivery(UserRequest ureq, String taskFile) {
		VFSContainer forumContainer = VFSManager.olatRootContainer(TACourseNode.getTaskFolderPathRelToFolderRoot(courseEnv, node), null);
		VFSItem item = forumContainer.resolve(taskFile);
		if (item instanceof VFSLeaf) {
			VFSLeaf leaf = (VFSLeaf)item;
			ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(leaf));
			return true;
		} else if (item==null) {
			log.warn("Can not cast to VFSLeaf. item==null, taskFile="  + taskFile);
			return false;
		} else {
			log.warn("Can not cast to VFSLeaf. item.class.name=" + item.getClass().getName() + ", taskFile="+taskFile);
			return false;
		}
	}
	
	/**
	 * 
	 * Description:<br>
	 * Model holding available tasks.
	 * Contains 4 cols: task title, view, select, and deselect.
	 * 
	 * <P>
	 * Initial Date:  20.04.2010 <br>
	 * @author Lavinia Dumitrescu
	 */
	class DeselectableTaskTableModel extends DefaultTableDataModel<String> {
		private int COLUMN_COUNT;

		public DeselectableTaskTableModel(List<String> objects, int num_cols) {
			super(objects);
			COLUMN_COUNT = num_cols;
		}

		@Override
		public int getColumnCount() {			
			return COLUMN_COUNT;
		}

		@Override
		public Object getValueAt(int row, int col) {
			String taskTitle = objects.get(row);
			if (col == 0) {					
					return taskTitle;
			} else if (col == 1) {
				return "View";
			} else if (col == 2) {				
				return new Boolean(!hasAnyTaskAssigned());
			} else if (col == 3) {				
				return new Boolean(isTaskAssigned(taskTitle));
			}

			return "ERROR";
		}
		
		private boolean hasAnyTaskAssigned() {
			return assignedTask!=null;
		}
		
		private boolean isTaskAssigned(String task) {
			return task.equals(assignedTask);
		}		
	}
}