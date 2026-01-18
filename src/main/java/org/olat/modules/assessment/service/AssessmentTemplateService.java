package org.olat.modules.assessment.service;

import java.util.List;

import org.olat.modules.assessment.model.AssessmentTemplateImpl;

public interface AssessmentTemplateService {

    AssessmentTemplateImpl createTemplate(String name, String description, String content, Long creatorKey);

    AssessmentTemplateImpl getTemplate(Long key);

    List<AssessmentTemplateImpl> listTemplates();

    AssessmentTemplateImpl updateTemplate(AssessmentTemplateImpl template);

    void deleteTemplate(Long key);

    String exportTemplate(Long key);

    AssessmentTemplateImpl importTemplate(String name, String description, String content, Long creatorKey);

    boolean applyTemplateToCourse(Long templateKey, Long repositoryEntryKey);
}
