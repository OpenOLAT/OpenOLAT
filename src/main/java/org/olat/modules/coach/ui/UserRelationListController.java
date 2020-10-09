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
package org.olat.modules.coach.ui;

import java.util.List;

import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.RelationRole;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.StudentStatEntry;
import org.olat.modules.coach.security.RoleSecurityCallbackFactory;
import org.olat.user.ui.role.RelationRolesAndRightsUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 25 May 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com
 */
public class UserRelationListController extends AbstactCoachListController implements Activateable2 {

    @Autowired
    private CoachingService coachingService;
    @Autowired
    private IdentityRelationshipService identityRelationshipService;

    private RelationRole relationRole;

    public UserRelationListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, RelationRole relationRole) {
        super(ureq, wControl, stackPanel);

        this.relationRole = identityRelationshipService.getRole(relationRole.getKey());
        this.securityCallback = RoleSecurityCallbackFactory.create(this.relationRole.getRights());

        super.initForm(ureq);
        loadModel();
    }

    @Override
    protected void loadModel() {
        List<StudentStatEntry> students = coachingService.getUserStatistics(getIdentity(), relationRole, userPropertyHandlers, getLocale());
        model.setObjects(students);
        tableEl.reset();
        tableEl.reloadData();
    }

    protected UserOverviewController selectStudent(UserRequest ureq, StudentStatEntry studentStat) {
        Identity student = securityManager.loadIdentityByKey(studentStat.getIdentityKey());
        OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, student.getKey());
        WindowControl bwControl = addToHistory(ureq, ores, null);

        int index = model.getObjects().indexOf(studentStat);
        String roleTranslation = RelationRolesAndRightsUIFactory.getTranslatedContraRole(relationRole, getLocale());
        userCtrl = new UserOverviewController(ureq, bwControl, stackPanel, studentStat, student, index, model.getRowCount(), roleTranslation, securityCallback);
        listenTo(userCtrl);

        String displayName = userManager.getUserDisplayName(student);
        stackPanel.pushController(displayName, userCtrl);
        return userCtrl;
    }
}
