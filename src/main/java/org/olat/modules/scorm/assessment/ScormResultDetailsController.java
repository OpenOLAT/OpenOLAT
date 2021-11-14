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
package org.olat.modules.scorm.assessment;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.scorm.server.servermodels.ScoUtils;

/**
 * 
 * Description:<br>
 * Show the assessment's details of SCORM course for a selected user. A popup shows all the
 * CMI datas of the SCOs visited by the user
 * 
 * <P>
 * Initial Date:  17 août 2009 <br>
 * @author srosse
 */
public class ScormResultDetailsController extends BasicController {
	
	private static final String CMI_RAW_SCORE = "cmi.core.score.raw";
	private static final String CMI_TOTAL_TIME = "cmi.core.total_time";
	
	private VelocityContainer main;
	private TableController summaryTableCtr;
	private TableController cmiTableCtr;
	private CloseableModalController cmc;
	private Link resetButton;
	private DialogBoxController resetConfirmationBox;
	
	private final CourseNode node;
	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnv;


	public ScormResultDetailsController(UserRequest ureq, WindowControl wControl, CourseNode node,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl);
		
		this.node = node;
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		init(ureq);
	}
	
	protected void init(UserRequest ureq) {
		main = createVelocityContainer("scores");
		
		TableGuiConfiguration summaryTableConfig = new TableGuiConfiguration();
		summaryTableConfig.setDownloadOffered(true);
		
		summaryTableCtr = new TableController(summaryTableConfig, ureq, getWindowControl(), getTranslator());
		summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("summary.column.header.date", 0, null, ureq.getLocale()));
		summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("summary.column.header.duration", 1, null, ureq.getLocale()));
		summaryTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("summary.column.header.assesspoints", 2, null, ureq.getLocale()));
		summaryTableCtr.addColumnDescriptor(new StaticColumnDescriptor("sel", "summary.column.header.details", getTranslator().translate("select")));
		listenTo(summaryTableCtr);
		
		loadModel();
		
		main.put("summary", summaryTableCtr.getInitialComponent());
		if(!coachCourseEnv.isCourseReadOnly()) {
			resetButton = LinkFactory.createButton("reset", main, this);
			main.put("resetButton", resetButton);
		}

		putInitialPanel(main);
	}
	
	private void loadModel() {
		CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
		String username = assessedUserCourseEnv.getIdentityEnvironment().getIdentity().getName();
		Map<Date, List<CmiData>> rawDatas = ScormAssessmentManager.getInstance().visitScoDatasMultiResults(username, courseEnv, node);
		summaryTableCtr.setTableDataModel(new SummaryTableDataModelMultiResults(rawDatas));
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == resetButton) {
			String title = translate("reset.title");
			User user = assessedUserCourseEnv.getIdentityEnvironment().getIdentity().getUser();
			String name = user.getProperty(UserConstants.FIRSTNAME, null) + " " + user.getProperty(UserConstants.LASTNAME, null);
			String text = translate("reset.text", new String[]{name});
			resetConfirmationBox = activateOkCancelDialog(ureq, title, text, resetConfirmationBox);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == summaryTableCtr) {
			TableEvent tEvent = (TableEvent)event;
			if (tEvent.getActionId().equals("sel")) {
				TableGuiConfiguration tableConfig = new TableGuiConfiguration();
				tableConfig.setPreferencesOffered(true, "scormAssessmentDetails");
				
				removeAsListenerAndDispose(cmiTableCtr);
				cmiTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
				listenTo(cmiTableCtr);

				cmiTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cmis.column.header.itemId", 0, null, ureq.getLocale()));
				cmiTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cmis.column.header.translatedKey", 1, null, ureq.getLocale()));
				cmiTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cmis.column.header.key", 2, null, ureq.getLocale()));
				cmiTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("cmis.column.header.value", 3, null, ureq.getLocale()));

				// <BPS-252> BPS-252_3	
				int rowId = tEvent.getRowId();
				List<CmiData> data = ((SummaryTableDataModelMultiResults)summaryTableCtr.getTableDataModel()).getObject(rowId);
				cmiTableCtr.setTableDataModel(new CmiTableDataModel(getTranslator(), data));
				// </BPS-252> BPS-252_3
				
				removeAsListenerAndDispose(cmc);
				cmc = new CloseableModalController(getWindowControl(), translate("close"), cmiTableCtr.getInitialComponent());
				listenTo(cmc);

				cmc.activate();
			}
		} else if ( source == resetConfirmationBox) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				//delete scorm
				String username = assessedUserCourseEnv.getIdentityEnvironment().getIdentity().getName();
				CourseEnvironment courseEnv = assessedUserCourseEnv.getCourseEnvironment();
				ScormAssessmentManager.getInstance().deleteResults(username, courseEnv, node);
				fireEvent(ureq, Event.DONE_EVENT);
				loadModel();
			}
		}
	}
	
	public static class CmiTableDataModel extends BaseTableDataModelWithoutFilter<CmiData> {
		private final List<CmiData> datas;
		private final Translator translator;
		private final Pattern pattern = Pattern.compile("[0-9]");
		
		public CmiTableDataModel(Translator translator, List<CmiData> datas) {
			this.datas = datas;
			this.translator = translator;
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return datas.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			CmiData data = datas.get(row);
			switch(col) {
				case 0: return data.getItemId();
				case 1:
					String key = data.getKey();
					String translation;
					
					try {
						Matcher matcher = pattern.matcher(key);
						if(matcher.find()) {
							String pos = key.substring(matcher.start(), matcher.end());
							if(matcher.find()) {
								String pos2 = key.substring(matcher.start(), matcher.end());
								key = key.replace(pos + ".", "").replace(pos2 + ".", "");
								translation = translator.translate(key, new String[]{pos,pos2});
							}
							else {
								key = key.replace(pos + ".", "");
								translation = translator.translate(key, new String[]{pos});
							}
						}
						else {
							translation = translator.translate(key);
						}
						if(translation == null || translation.length() > 150) {
							return data.getKey();
						}
						return translation;
					}
					catch(Exception ex) {
						return key;
					}
				case 2: return data.getKey();
				case 3: return data.getValue();
				default: return "ERROR";
			}
		}
	}
	
	// <OLATCE-289>
	/**
	 * Description:<br>
	 * A TableDataModel for multi scorm results files.
	 * 
	 * <P>
	 * Initial Date:  07.01.2010 <br>
	 * @author thomasw
	 */
	public static class SummaryTableDataModelMultiResults implements TableDataModel<List<CmiData>> {
		
		private final Map<Date, List<CmiData>> objects;
		
		/**
		 * Array of Keys of the Object-Map. The Key is at the same time the 
		 * String representation of the last modified date.
		 */
		private Date[] objectKeys;
		
		public SummaryTableDataModelMultiResults(Map<Date, List<CmiData>> datas) {
			objects = datas;
			if(objects != null) {
				objectKeys = objects.keySet().toArray(new Date[objects.size()]);
			}
		}

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return objects == null ? 0 : objects.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Date dateKey = objectKeys[row];
			List<CmiData> cmiObject = objects.get(dateKey);
			String[] result = calcTimeAndScore(cmiObject);
			switch (col) {
				case 0:
					return dateKey;
				case 1:
					return result[0];
				case 2:
					return result[1];
				default: return "ERROR";
			}
		}
		
		private String[] calcTimeAndScore(List<CmiData> cmiObject) {
			double score = 0;
			String totalTime = null;
			for(CmiData data:cmiObject) {
				String key = data.getKey();
				if(CMI_RAW_SCORE.equals(key)) {
					String value = data.getValue();
					if(StringHelper.containsNonWhitespace(value)) {
						try {
							score += Double.parseDouble(value);
						} catch (NumberFormatException e) {
							//fail silently
						}
					}
				}
				else if(CMI_TOTAL_TIME.equals(key)) {
					String value = data.getValue();
					if(StringHelper.containsNonWhitespace(value)) {
						if(totalTime == null) {
							totalTime = value;
						}
						else {
							totalTime = ScoUtils.addTimes(totalTime, value);
						}
					}
				}
			}
			String[] result =  new String[2];
			result[0] = totalTime;
			result[1] = "" + score;
			return result;
		}

		@Override
		public Object createCopyWithEmptyList() {
			return new SummaryTableDataModelMultiResults(new HashMap<Date, List<CmiData>>());
		}

		@Override
		public List<CmiData> getObject(int row) {
			Date dateKey = objectKeys[row];
			List<CmiData> cmiObject = objects.get(dateKey);
			return cmiObject;
		}

		@Override 
		public void setObjects(List<List<CmiData>> objects) {
			//
		}
	}
	// </OLATCE-289>
}