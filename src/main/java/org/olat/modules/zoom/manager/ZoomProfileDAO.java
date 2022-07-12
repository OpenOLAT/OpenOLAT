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
package org.olat.modules.zoom.manager;

import org.olat.core.commons.persistence.DB;
import org.olat.ims.lti13.LTI13Tool;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.modules.zoom.model.ZoomProfileImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static org.olat.modules.zoom.ZoomProfile.ZoomProfileStatus.active;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ZoomProfileDAO {

    @Autowired
    private DB dbInstance;

    public ZoomProfile createProfile(String name, String ltiKey, LTI13Tool lti13Tool, String token) {
        ZoomProfileImpl zoomProfile = new ZoomProfileImpl();
        zoomProfile.setCreationDate(new Date());
        zoomProfile.setLastModified(zoomProfile.getCreationDate());
        zoomProfile.setName(name);
        zoomProfile.setStatus(active);
        zoomProfile.setLtiKey(ltiKey);
        zoomProfile.setStudentsCanHost(false);
        zoomProfile.setToken(token);
        zoomProfile.setLtiTool(lti13Tool);
        dbInstance.getCurrentEntityManager().persist(zoomProfile);
        return zoomProfile;
    }

    public ZoomProfile getProfile(Long key) {
        return dbInstance.getCurrentEntityManager().find(ZoomProfileImpl.class, key);
    }

    public List<ZoomProfile> getProfiles() {
        String q = "select profile from zoomprofile profile";
        return dbInstance.getCurrentEntityManager().createQuery(q, ZoomProfile.class).getResultList();
    }

    public ZoomProfile updateProfile(ZoomProfile zoomProfile) {
        zoomProfile.setLastModified(new Date());
        return dbInstance.getCurrentEntityManager().merge(zoomProfile);
    }

    public void deleteProfile(ZoomProfile zoomProfile) {
        dbInstance.deleteObject(zoomProfile);
    }

    public boolean isInUse(ZoomProfile zoomProfile) {
        String query = "select 1 from zoomconfig c where c.profile=:zoomProfile";
        return !dbInstance.getCurrentEntityManager()
                .createQuery(query)
                .setParameter("zoomProfile", zoomProfile)
                .getResultList()
                .isEmpty();
    }
}
