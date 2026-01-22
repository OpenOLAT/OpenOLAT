package org.olat.modules.assessment.manager;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.modules.assessment.model.AssessmentTemplateImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssessmentTemplateDAO {

    @Autowired
    private DB dbInstance;

    public AssessmentTemplateImpl createTemplate(String name, String description, String content, Long creatorKey) {
        AssessmentTemplateImpl tmpl = new AssessmentTemplateImpl();
        tmpl.setName(name);
        tmpl.setDescription(description);
        tmpl.setContent(content);
        tmpl.setCreatorKey(creatorKey);
        dbInstance.getCurrentEntityManager().persist(tmpl);
        return tmpl;
    }

    public AssessmentTemplateImpl updateTemplate(AssessmentTemplateImpl tmpl) {
        return dbInstance.getCurrentEntityManager().merge(tmpl);
    }

    public AssessmentTemplateImpl getTemplateById(Long templateKey) {
        return dbInstance.getCurrentEntityManager().find(AssessmentTemplateImpl.class, templateKey);
    }

    public List<AssessmentTemplateImpl> listTemplates() {
        String query = "select t from assessmenttemplate t order by t.name";
        return dbInstance.getCurrentEntityManager().createQuery(query, AssessmentTemplateImpl.class)
                .getResultList();
    }

    public void deleteTemplate(Long templateKey) {
        AssessmentTemplateImpl tmpl = getTemplateById(templateKey);
        if (tmpl != null) {
            dbInstance.getCurrentEntityManager().remove(tmpl);
        }
    }
}
