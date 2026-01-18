package org.olat.modules.assessment.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import org.olat.modules.assessment.manager.AssessmentTemplateDAO;
import org.olat.modules.assessment.model.AssessmentTemplateImpl;
import org.olat.modules.assessment.service.impl.AssessmentTemplateServiceImpl;

public class AssessmentTemplateServiceTest {

    @Test
    public void createAndExportTemplate() {
        // lightweight test using a fake DAO
        AssessmentTemplateDAO fakeDao = new AssessmentTemplateDAO() {
            private AssessmentTemplateImpl tmpl;
            @Override
            public AssessmentTemplateImpl createTemplate(String name, String description, String content, Long creatorKey) {
                tmpl = new AssessmentTemplateImpl();
                tmpl.setName(name);
                tmpl.setDescription(description);
                tmpl.setContent(content);
                tmpl.setCreatorKey(creatorKey);
                return tmpl;
            }
            @Override
            public AssessmentTemplateImpl getTemplateById(Long key) { return tmpl; }
            @Override
            public java.util.List<AssessmentTemplateImpl> listTemplates() { return java.util.Collections.singletonList(tmpl); }
            @Override
            public AssessmentTemplateImpl updateTemplate(AssessmentTemplateImpl t) { tmpl = t; return tmpl; }
            @Override
            public void deleteTemplate(Long key) { tmpl = null; }
        };

        AssessmentTemplateServiceImpl svc = new AssessmentTemplateServiceImpl();
        // inject fake DAO
        try {
            java.lang.reflect.Field f = AssessmentTemplateServiceImpl.class.getDeclaredField("templateDao");
            f.setAccessible(true);
            f.set(svc, fakeDao);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        AssessmentTemplateImpl created = svc.createTemplate("tpl1", "desc", "{\"nodes\":[]}", 123L);
        assertNotNull(created);
        String exported = svc.exportTemplate(created.getKey());
        assertNotNull(exported);
        assertEquals("{\"nodes\":[]}", exported);
    }
}
