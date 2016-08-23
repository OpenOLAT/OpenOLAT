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
package org.olat.course.highscore.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.chart.BarChartComponent;
import org.olat.core.gui.components.chart.BarSeries;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.user.DisplayPortraitController;
import org.springframework.beans.factory.annotation.Autowired;

import edu.emory.mathcs.backport.java.util.Arrays;

public class HighScoreController extends DefaultController{
	
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(HighScoreController.class);

	private final VelocityContainer myContent;

	protected HighScoreController(UserRequest ureq, WindowControl wControl) {
		super(wControl);
		Translator fallbackTrans = Util.createPackageTranslator(CourseNode.class, ureq.getLocale());

		myContent = new VelocityContainer("highscore", VELOCITY_ROOT + "/highscore.html", fallbackTrans, this);

	}
	
//	Identity markedIdentity = ureq.getIdentity();
//	boolean revealIdentities = false;
//	HighScoreController highScoreCtr = new HighScoreController(ureq, wControl, msCourseNode, markedIdentity, revealIdentities);
//	myContent.put("highScore", highScoreCtr.getInitialComponent());
	
	public HighScoreController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			AssessableCourseNode courseNode, Identity markedIdentity, boolean revealIdentities) {
		super(wControl);	
		Translator fallbackTrans = Util.createPackageTranslator(CourseNode.class, ureq.getLocale());
		Translator trans = Util.createPackageTranslator(HighScoreController.class, ureq.getLocale(), fallbackTrans);
		
		String s = VELOCITY_ROOT;

		myContent = new VelocityContainer("highscore", VELOCITY_ROOT + "/highscore.html", trans, this);

//		DisplayPortraitController portrait = new DisplayPortraitController(ureq, wControl, markedIdentity, false, true);
//		Component compi = portrait.getInitialComponent();
//		myContent.put("portrait", compi);
		
		RepositoryEntry entry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CourseGroupManager cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
		List<Identity> participants = cgm.getParticipants();
		List<Identity>  coaches = cgm.getCoaches();
		//cgm.getParticipantsFromAreas();
		List<Identity> participantz = cgm.getParticipantsFromBusinessGroups();
		List<AssessmentEntry>  assessEntries = userCourseEnv.getCourseEnvironment().getAssessmentManager().getAssessmentEntries(courseNode);
				
		List<HighScoreTableEntry> members = new ArrayList<>();
		List<HighScoreTableEntry> members2 = new ArrayList<>();
		Identity ownIdentity = ureq.getIdentity();

		for (AssessmentEntry assessmentEntry : assessEntries) {
			members.add(new HighScoreTableEntry((int)(assessmentEntry.getScore().floatValue() * 10),
					assessmentEntry.getIdentity().getName(), assessmentEntry.getIdentity()));			
		}
		
//		for (Identity identity : participants) {
//			if (participants.indexOf(identity) < 5) {
//				members.add(new TableEntry((int)(Math.random()*100), identity.getName(), identity));					
////			} else if (participants.indexOf(identity) > 2 && ownIdentity.getKey().equals(identity.getKey())){
////				members2.add(new TableEntry((int)(Math.random()*100), identity.getName(), identity));
//			}
//		}
		
		members = members.stream()
				.sorted((a,b) -> Float.compare(b.getScore(),a.getScore()))
				.collect(Collectors.toList());
		
		members2 = members.stream()
				.skip(2)
				.filter(a -> a.getIdentity().equals(ownIdentity))
				.collect(Collectors.toList());
		
		final List<HighScoreTableEntry> members3 = members;
		int[] indices = IntStream.range(0, members.size())
                .filter(i -> members3.get(i).getIdentity().equals(ownIdentity))
                .toArray();
		
		members = members.stream()
				.limit(4)
				.collect(Collectors.toList());
		
		
		String[] localizer = { "first", "second", "third" };
		for (int i = 0; i < localizer.length; i++) {
			myContent.contextPut(localizer[i], (members.size() > i) ? members.get(i).getName() : "");
			myContent.contextPut("score" + (i + 1), (members.size() > i) ? members.get(i).getScore() : "");
			if (members.size() > i) {
				DisplayPortraitController portrait = new DisplayPortraitController(ureq, wControl,
						members.get(i).getIdentity(), false, true);
				Component compi = portrait.getInitialComponent();
				myContent.put("portrait" + (i + 1), compi);
			}
		}
//		myContent.contextPut("first", (members.size()>0)?members.get(0).getName():"");
//		myContent.contextPut("second", (members.size()>1)?members.get(1).getName():"");
//		myContent.contextPut("third", (members.size()>2)?members.get(2).getName():"");
//		BarChartComponent barchart = new BarChartComponent("barchart");
//		barchart.addSeries(new BarSeries());
		
		BarChartComponent chartCmp = new BarChartComponent("stat");
	      List<String> labels = Arrays.asList(new String[]{"eins","zwei","drei","vier"});
	      List<Integer> values = Arrays.asList(new Integer[]{12,55,33,24});
	      BarSeries serie = new BarSeries();
	      for(int i=0; i<labels.size(); i++) {
	        double value = values.get(i).doubleValue();
	        String category = labels.get(i);
	        serie.add(value, category);
	      }
	      chartCmp.addSeries(serie);
		
		myContent.put("barchart", chartCmp);
		
		TableGuiConfiguration tgc = new TableGuiConfiguration();
		tgc.setPreferencesOffered(true, "TableGuiDemoPrefs");
		TableController table = new TableController(tgc, ureq, getWindowControl(), trans);

		TableGuiConfiguration tgc2 = new TableGuiConfiguration();
		tgc2.setPreferencesOffered(true, "TableGuiDemoPrefs");
		TableController table2 = new TableController(tgc2, ureq, getWindowControl(), trans);
		
		TableController[] tables = {table,table2};
		for (int i = 0; i < tables.length; i++) {
			for (int j = 0; j < 3; j++) {
				tables[i].addColumnDescriptor(
						new DefaultColumnDescriptor("highscore.table.header" + (j+1), j, null, ureq.getLocale()));
			}
		}
		

//		table.addColumnDescriptor(new DefaultColumnDescriptor("highscore.table.header1", 0, null, ureq.getLocale()));
//		table.addColumnDescriptor(new DefaultColumnDescriptor("highscore.table.header2", 1, null, ureq.getLocale()));
//		table.addColumnDescriptor(new DefaultColumnDescriptor("highscore.table.header3", 2, null, ureq.getLocale()));
//		table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header4", 3, null, ureq.getLocale()));
//		table.addColumnDescriptor(new DefaultColumnDescriptor("guidemo.table.header5", 4, null, ureq.getLocale()));
//		table.addColumnDescriptor(new CustomRenderColumnDescriptor("guidemo.table.header6", 5, null, ureq.getLocale(),
//				CustomRenderColumnDescriptor.ALIGNMENT_CENTER, new ImageCellRenderer()));

		table.setTableDataModel(new SampleTableModel(members,0));
		myContent.put("table", table.getInitialComponent());
		
		if(!members2.isEmpty()){
			table2.setTableDataModel(new SampleTableModel(members2,indices[0]));
			myContent.put("table2", table2.getInitialComponent());
		}		
		setInitialComponent(myContent);

	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub
		
	}

}
class SampleTableModel extends BaseTableDataModelWithoutFilter<java.util.List<Object>> {
	@Autowired
	private RepositoryManager repositoryManager;

	private int COLUMN_COUNT = 3;
	private List<List<Object>> entries;
	
	
	public SampleTableModel(List<HighScoreTableEntry> members, int rank) {
		
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryService rs = CoreSpringFactory.getImpl(RepositoryService.class);

		RepositoryEntry entry = rm.lookupRepositoryEntry(34570267L); 
		
		List<OLATResource> resourcesForReservations = new ArrayList<>();
		if(entry != null) {
			resourcesForReservations.add(entry.getOlatResource());
		}
		
//		List<ResourceReservation> reservations = acService.getReservations(resourcesForReservations);
		
//		List<TableEntry> members = new ArrayList<TableEntry>();
//		members.add(new TableEntry(22,"Fabian Kiefer"));
//		members.add(new TableEntry(11,"Flad Käfer"));
//		members.add(new TableEntry(17,"Stefanie Müller"));
//		members.add(new TableEntry(35,"Heinrich Kiefer"));
//		members.add(new TableEntry(17,"Frentix Uzzer"));
		

		
		
		int iEntries = members.size() <= 10 ? members.size() : 10;
		this.entries = new ArrayList<List<Object>>(iEntries);
		for (int i=0; i < iEntries; i++) {
			List<Object> row = new ArrayList<Object>(3);
			row.add(rank != 0 ? rank+1 : Integer.toString(i+1));
			row.add(Float.toString(members.get(i).getScore()));
			row.add(members.get(i).getName());
//			row.add("");
			entries.add(row);
		}
	}

	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	public int getRowCount() {
		return entries.size();
	}

	public Object getValueAt(int row, int col) {
		List<Object> entry = entries.get(row);
		return entry.get(col);
	}
}
class ImageCellRenderer implements CustomCellRenderer {

	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		sb.append("<img src=\"");
		Renderer.renderStaticURI(sb, "images/openolat/openolat_logo_16.png");
		sb.append("\" alt=\"An image within a table...\" />");
	}
	
}
