package org.olat.modules.assessment.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.olat.modules.assessment.manager.AssessmentTemplateDAO;
import org.olat.modules.assessment.model.AssessmentTemplateImpl;
import org.olat.modules.assessment.service.AssessmentTemplateService;

@Service
public class AssessmentTemplateServiceImpl implements AssessmentTemplateService {

    @Autowired
    private AssessmentTemplateDAO templateDao;

    @Override
    @Transactional
    public AssessmentTemplateImpl createTemplate(String name, String description, String content, Long creatorKey) {
        return templateDao.createTemplate(name, description, content, creatorKey);
    }

    @Override
    public AssessmentTemplateImpl getTemplate(Long templateKey) {
        return templateDao.getTemplateById(templateKey);
    }

    @Override
    public List<AssessmentTemplateImpl> listTemplates() {
        return templateDao.listTemplates();
    }

    @Override
    @Transactional
    public AssessmentTemplateImpl updateTemplate(AssessmentTemplateImpl template) {
        return templateDao.updateTemplate(template);
    }

    @Override
    @Transactional
    public void deleteTemplate(Long templateKey) {
        templateDao.deleteTemplate(templateKey);
    }

    @Override
    public String exportTemplate(Long templateKey) {
        AssessmentTemplateImpl tmpl = templateDao.getTemplateById(templateKey);
        if (tmpl == null) return null;
        // For now export the raw content as-is. Could wrap metadata in future.
        return tmpl.getContent();
    }

    @Override
    @Transactional
    public AssessmentTemplateImpl importTemplate(String name, String description, String content, Long creatorKey) {
        return templateDao.createTemplate(name, description, content, creatorKey);
    }

    @Override
    @Transactional
    public boolean applyTemplateToCourse(Long templateKey, Long repositoryEntryKey) {
        AssessmentTemplateImpl tmpl = templateDao.getTemplateById(templateKey);
        if (tmpl == null) return false;
        // TODO: apply the structure contained in tmpl.getContent() to the course identified by repositoryEntryKey.
        // This would typically involve mapping template nodes to course nodes and creating assessment entries.
        // For now this is a placeholder that should be implemented together with course API collaborators.
        return true;
    }
}
