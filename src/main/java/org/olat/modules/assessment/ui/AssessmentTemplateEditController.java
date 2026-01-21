/**
 * OpenOLAT - Online Learning and Training
 * Assessment Template Edit Controller
 */
package org.olat.modules.assessment.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.assessment.model.AssessmentTemplateImpl;
import org.olat.modules.assessment.service.AssessmentTemplateService;

/**
 * Controller for creating and editing assessment templates.
 * 
 * @author OpenOLAT Enhancement
 */
public class AssessmentTemplateEditController extends FormBasicController {

    private TextElement nameEl;
    private TextAreaElement descriptionEl;
    private TextAreaElement contentEl;
    
    private AssessmentTemplateImpl template;
    
    private final AssessmentTemplateService templateService;
    
    public AssessmentTemplateEditController(UserRequest ureq, WindowControl wControl, AssessmentTemplateImpl template) {
        super(ureq, wControl, "template_edit");
        this.template = template;
        this.templateService = CoreSpringFactory.getImpl(AssessmentTemplateService.class);
        
        initForm(ureq);
    }
    
    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        setFormTitle("template.form.title");
        setFormDescription("template.form.description");

        String name = template != null ? template.getName() : "";
        nameEl = uifactory.addTextElement("template.name", "Name", 255, name, formLayout);
        nameEl.setMandatory(true);
        nameEl.setNotEmptyCheck("Please enter a template name.");
        nameEl.setExampleKey("e.g. Midterm Template", null);
        nameEl.setHelpText("Enter a short, descriptive name for this template.");

        String description = template != null ? template.getDescription() : "";
        descriptionEl = uifactory.addTextAreaElement("template.description", "Description", 
            2000, 3, 60, false, false, description, formLayout);
        descriptionEl.setHelpText("Describe the purpose or usage of this template (optional).");

        String content = template != null ? template.getContent() : "";
        contentEl = uifactory.addTextAreaElement("template.content", "Content", 
            10000, 8, 60, false, false, content, formLayout);
        contentEl.setHelpText("Paste or write the assessment structure here (JSON or XML).");

        // Buttons
        FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
        formLayout.add(buttonsCont);

        uifactory.addFormSubmitButton("save", buttonsCont);
        uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
    }
    
    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);
        
        nameEl.clearError();
        if (!StringHelper.containsNonWhitespace(nameEl.getValue())) {
            nameEl.setErrorKey("template.name.error.empty");
            allOk = false;
        }
        
        return allOk;
    }
    
    private void doSave(UserRequest ureq) {
        String name = nameEl.getValue();
        String description = descriptionEl.getValue();
        String content = contentEl.getValue();
        Long creatorKey = getIdentity().getKey();
        
        if (template == null) {
            // Create new template
            templateService.createTemplate(name, description, content, creatorKey);
            showInfo("Template created successfully.");
        } else {
            // Update existing template
            template.setName(name);
            template.setDescription(description);
            template.setContent(content);
            templateService.updateTemplate(template);
            showInfo("Template updated successfully.");
        }
        fireEvent(ureq, Event.DONE_EVENT);
    }
    
    @Override
    protected void formOK(UserRequest ureq) {
        doSave(ureq);
    }
}
