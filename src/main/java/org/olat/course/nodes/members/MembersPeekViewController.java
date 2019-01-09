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
package org.olat.course.nodes.members;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.MembersCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * <p>Initial date: May 19, 2016
 * @author lmihalkovic, http://www.frentix.com
 */
public class MembersPeekViewController extends BasicController {

	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService businessGroupService;	
	
	
	private final CourseEnvironment courseEnv;
	private TableController tableController;

	List<Row> entries = new ArrayList<>();

	public MembersPeekViewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, ModuleConfiguration config) {
		super(ureq, wControl);

		courseEnv = userCourseEnv.getCourseEnvironment();
		
		readFormData(config);
		initForm(ureq);
	}

	private void initForm(UserRequest ureq) {
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table table-condensed");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		
		removeAsListenerAndDispose(tableController);
		tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(tableController);
		
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("members.type", 0, null, ureq.getLocale()));
		tableController.addColumnDescriptor(new DefaultColumnDescriptor("members.count", 1, null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT));
		tableController.setTableDataModel(new DefaultTableDataModel<Row>(entries) {
			@Override
			public int getColumnCount() {
				return 2;
			}

			@Override
			public Object getValueAt(int row, int col) {
				Row r = entries.get(row);
				if (col == 0) { return r.col1; }
				if (col == 1) { return r.col2; }
				return null;
			}
		});
		
		putInitialPanel(tableController.getInitialComponent());
	}
	
	protected void readFormData(ModuleConfiguration config) {
		CourseGroupManager cgm = courseEnv.getCourseGroupManager();
		
		boolean withOwners = config.getBooleanSafe(MembersCourseNode.CONFIG_KEY_SHOWOWNER);
		boolean withCoaches = config.anyTrue(MembersCourseNode.CONFIG_KEY_COACHES_COURSE, MembersCourseNode.CONFIG_KEY_COACHES_ALL)
				|| config.hasAnyOf(MembersCourseNode.CONFIG_KEY_COACHES_GROUP, MembersCourseNode.CONFIG_KEY_COACHES_AREA, MembersCourseNode.CONFIG_KEY_COACHES_CUR_ELEMENT);
		boolean withParticipants = config.anyTrue(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_COURSE, MembersCourseNode.CONFIG_KEY_PARTICIPANTS_ALL)
				|| config.hasAnyOf(MembersCourseNode.CONFIG_KEY_PARTICIPANTS_GROUP, MembersCourseNode.CONFIG_KEY_PARTICIPANTS_AREA, MembersCourseNode.CONFIG_KEY_PARTICIPANTS_CUR_ELEMENT);
		
		
		RepositoryEntry courseRepositoryEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		List<Identity> owners;
		if(withOwners) {
			owners = MembersHelpers.getOwners(repositoryService, courseRepositoryEntry);
		} else {
			owners = new ArrayList<>();
		}
		
		List<Identity> coaches = new ArrayList<>();
		if(withCoaches) {
			MembersHelpers.addCoaches(config, cgm, businessGroupService, coaches);
		}
		
		List<Identity> participants = new ArrayList<>();
		if(withParticipants) {
			MembersHelpers.addParticipants(config, cgm, businessGroupService, participants);
		}
		
		MembersCourseNodeConfiguration nodeConfig = (MembersCourseNodeConfiguration)CourseNodeFactory.getInstance().getCourseNodeConfiguration("cmembers");
		boolean deduplicateList = nodeConfig.isDeduplicateList();
		
		Predicate<Identity> deduplicatCatch = new Deduplicate();
		if(withOwners) {
			List<Identity> filteredOwners = owners.stream()
					.filter(deduplicatCatch)
					.collect(Collectors.toList());
			entries.add(new Row(translate("members.owners"), Integer.toString(filteredOwners.size())));
		}

		if(withCoaches) {
			if(!deduplicateList) {
				deduplicatCatch = new Deduplicate();
			}
			List<Identity> filteredCoaches = coaches.stream()
					.filter(deduplicatCatch)
					.collect(Collectors.toList());
			entries.add(new Row(translate("members.coaches"), Integer.toString(filteredCoaches.size())));
		}

		if(withParticipants) {
			if(!deduplicateList) {
				deduplicatCatch = new Deduplicate();
			}
			List<Identity> filteredParticipants = participants.stream()
					.filter(deduplicatCatch)
					.collect(Collectors.toList());
			entries.add(new Row(translate("members.participants"), Integer.toString(filteredParticipants.size())));
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing
	}

	@Override
	protected void doDispose() {
		// nothing
	}
	
	private static class Row {
		private String col1;
		private String col2;
		public Row(String col1, String col2) {
			this.col1 = col1;
			this.col2 = col2;
		}
	}
	
	private static class Deduplicate implements Predicate<Identity> {
		
		private final Set<Identity> duplicateCatcher = new HashSet<>();

		@Override
		public boolean test(Identity t) {
			if(duplicateCatcher.contains(t)) {
				return false;
			}
			duplicateCatcher.add(t);
			return true;
		}	
	}
}
