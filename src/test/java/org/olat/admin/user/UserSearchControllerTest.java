package org.olat.admin.user;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.manager.OrganisationDAO;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.gui.util.WindowControlMocker;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.RolesByOrganisation;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.session.UserSessionManager;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;


public class UserSearchControllerTest extends OlatTestCase {

    @Autowired
    private UserSessionManager sessionManager;
    @Autowired
    private OrganisationDAO organisationDao;

    @Test
    public void getUserObject() {
        Identity id = JunitTestHelper.createAndPersistIdentityAsRndUser("Member-1");
        Organisation organisation = organisationDao.createAndPersistOrganisation(
                "OpenOLAT EE", UUID.randomUUID().toString(), null, null, null);

        List<RolesByOrganisation> rbo = new ArrayList<>();
        rbo.add(new RolesByOrganisation(organisation, new OrganisationRoles[] { OrganisationRoles.sysadmin} ));

        Roles sysadmin = Roles.valueOf(rbo, false, false);

        HttpSession httpSession = new MockHttpSession();
        UserSession userSession = sessionManager.getUserSession(null, httpSession);
        userSession.setRoles(sysadmin);

        SyntheticUserRequest ureq = new SyntheticUserRequest(id, Locale.ENGLISH, userSession);
        WindowControl windowControl = new WindowControlMocker();
        UserSearchController userSearchCtrl = new UserSearchController(ureq, windowControl, false, false, false, true);
        SimpleStackedPanel panel = (SimpleStackedPanel) userSearchCtrl.getInitialComponent();
        VelocityContainer vc = (VelocityContainer) panel.getContent();
        String vc_root = Util.getPackageVelocityRoot(UserSearchController.class);
        assertThat(vc.getPage()).isEqualTo(vc_root + "/usersearch.html");
    }
}