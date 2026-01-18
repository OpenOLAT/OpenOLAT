package org.olat.modules.assessment.manager;

import java.util.List;

import jakarta.persistence.TypedQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Repository;
import org.olat.modules.assessment.model.AssessmentTemplateImpl;

import java.util.List;

@Repository
public class AssessmentTemplateDAO {

    @PersistenceContext(unitName = "OpenOlatPU")
    private EntityManager em;

    public AssessmentTemplateImpl createTemplate(String name, String description, String content, Long creatorKey) {
        AssessmentTemplateImpl tmpl = new AssessmentTemplateImpl();
        tmpl.setName(name);
        tmpl.setDescription(description);
        tmpl.setContent(content);
        tmpl.setCreatorKey(creatorKey);
        em.persist(tmpl);
        return tmpl;
    }

    public AssessmentTemplateImpl updateTemplate(AssessmentTemplateImpl tmpl) {
        return em.merge(tmpl);
    }

    public AssessmentTemplateImpl getTemplateById(Long key) {
        return em.find(AssessmentTemplateImpl.class, key);
    }

    public List<AssessmentTemplateImpl> listTemplates() {
        TypedQuery<AssessmentTemplateImpl> q = em.createQuery("select t from assessmenttemplate t order by t.name", AssessmentTemplateImpl.class);
        return q.getResultList();
    }

    public void deleteTemplate(Long key) {
        AssessmentTemplateImpl tmpl = getTemplateById(key);
        if (tmpl != null) {
            em.remove(tmpl);
        }
    }
}
