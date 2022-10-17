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
import org.olat.group.BusinessGroup;
import org.olat.ims.lti13.LTI13ToolDeployment;
import org.olat.modules.zoom.ZoomConfig;
import org.olat.modules.zoom.ZoomProfile;
import org.olat.modules.zoom.model.ZoomConfigImpl;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 *
 * Initial date: 2022-07-07<br>
 * @author cpfranger, christoph.pfranger@frentix.com, https://www.frentix.com
 *
 */
@Service
public class ZoomConfigDAO {

    @Autowired
    private DB dbInstance;

    public ZoomConfig createConfig(ZoomProfile profile, LTI13ToolDeployment toolDeployment, String description) {
        ZoomConfigImpl zoomConfig = new ZoomConfigImpl();
        zoomConfig.setCreationDate(new Date());
        zoomConfig.setLastModified(zoomConfig.getCreationDate());
        zoomConfig.setDescription(description);
        zoomConfig.setProfile(profile);
        zoomConfig.setLtiToolDeployment(toolDeployment);
        dbInstance.getCurrentEntityManager().persist(zoomConfig);
        return zoomConfig;
    }

    public boolean configExists(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) {
        if (entry != null & subIdent != null) {
            String query = "select 1 from zoomconfig c inner join c.ltiToolDeployment as td where td.entry.id=:entryId and td.subIdent=:subIdent";
            return !dbInstance.getCurrentEntityManager()
                    .createQuery(query)
                    .setParameter("entryId", entry.getKey())
                    .setParameter("subIdent", subIdent)
                    .getResultList()
                    .isEmpty();
        }
        if (businessGroup != null) {
            String query = "select 1 from zoomconfig c where c.ltiToolDeployment.businessGroup.id=:businessGroupId";
            return !dbInstance.getCurrentEntityManager()
                    .createQuery(query)
                    .setParameter("businessGroupId", businessGroup.getKey())
                    .getResultList()
                    .isEmpty();
        }
        return false;
    }

    public ZoomConfig getConfig(RepositoryEntry entry, String subIdent, BusinessGroup businessGroup) throws NoResultException, NonUniqueResultException {
        if (entry != null && subIdent != null) {
            String query = "select c from zoomconfig c inner join c.ltiToolDeployment as td where td.entry.id=:entryId and td.subIdent=:subIdent";
            List<ZoomConfig> config = dbInstance.getCurrentEntityManager()
                    .createQuery(query, ZoomConfig.class)
                    .setParameter("entryId", entry.getKey())
                    .setParameter("subIdent", subIdent)
                    .getResultList();
            return config == null || config.isEmpty() ? null : config.get(0);
        }
        if (businessGroup != null) {
            String query = "select c from zoomconfig c where c.ltiToolDeployment.businessGroup.id=:businessGroupId";
            List<ZoomConfig> config = dbInstance.getCurrentEntityManager()
                    .createQuery(query, ZoomConfig.class)
                    .setParameter("businessGroupId", businessGroup.getKey())
                    .getResultList();
            return config == null || config.isEmpty() ? null : config.get(0);
        }
        throw new NoResultException("Invalid set of parameters to read a Zoom config");
    }

    public List<ZoomConfig> getConfigs(Long entryId) {
        String query = "select c from zoomconfig c inner join c.ltiToolDeployment as td inner join td.entry as e where e.key=:entryId";
        return dbInstance
                .getCurrentEntityManager()
                .createQuery(query, ZoomConfig.class)
                .setParameter("entryId", entryId)
                .getResultList();
    }

    public void deleteConfig(ZoomConfig zoomConfig) {
        dbInstance.deleteObject(zoomConfig);
    }

    public Optional<ZoomConfig> getConfig(String contextId) {
        String query = "select c from zoomconfig c where c.ltiToolDeployment.contextId=:contextId";
        return dbInstance
                .getCurrentEntityManager()
                .createQuery(query, ZoomConfig.class)
                .setParameter("contextId", contextId)
                .getResultStream()
                .findFirst();
    }
}
