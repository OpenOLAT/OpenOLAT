/**
 * OpenOLAT - Online Learning and Training
 * Assessment Template List Controller
 */
package org.olat.modules.assessment.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.modules.assessment.model.AssessmentTemplateImpl;
import org.olat.modules.assessment.service.AssessmentTemplateService;

/**
 * Controller to display and manage assessment templates.
 * 
 * @author OpenOLAT Enhancement
 */
public class AssessmentTemplateListController extends BasicController {

    private final VelocityContainer mainVC;
    private final Link createLink;
    private CloseableModalController cmc;
    private AssessmentTemplateEditController editCtrl;
    
    private final AssessmentTemplateService templateService;
    
    public AssessmentTemplateListController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl);
        
        templateService = CoreSpringFactory.getImpl(AssessmentTemplateService.class);
        
        mainVC = createVelocityContainer("template_list");
        
        createLink = LinkFactory.createButton("template.create", mainVC, this);
        createLink.setIconLeftCSS("o_icon o_icon_add");
        
        loadTemplates();
        
        putInitialPanel(mainVC);
    }
    
    private void loadTemplates() {
        List<AssessmentTemplateImpl> templates = templateService.listTemplates();
        List<TemplateRow> rows = new ArrayList<>();
        
        int counter = 0;
        for (AssessmentTemplateImpl template : templates) {
            TemplateRow row = new TemplateRow(template);
            
            // Create action links for each row
            Link editLink = LinkFactory.createLink("edit_" + counter, "edit", "edit", mainVC, this);
            editLink.setCustomDisplayText(translate("template.edit"));
            editLink.setIconLeftCSS("o_icon o_icon_edit");
            editLink.setUserObject(template);
            row.setEditLink(editLink);
            
            Link deleteLink = LinkFactory.createLink("delete_" + counter, "delete", "delete", mainVC, this);
            deleteLink.setCustomDisplayText(translate("template.delete"));
            deleteLink.setIconLeftCSS("o_icon o_icon_delete");
            deleteLink.setUserObject(template);
            row.setDeleteLink(deleteLink);
            
            Link exportLink = LinkFactory.createLink("export_" + counter, "export", "export", mainVC, this);
            exportLink.setCustomDisplayText(translate("template.export"));
            exportLink.setIconLeftCSS("o_icon o_icon_download");
            exportLink.setUserObject(template);
            row.setExportLink(exportLink);
            
            rows.add(row);
            counter++;
        }
        
        mainVC.contextPut("templates", rows);
        mainVC.contextPut("hasTemplates", !rows.isEmpty());
    }
    
    @Override
    protected void event(UserRequest ureq, Component source, Event event) {
        if (source == createLink) {
            doCreateTemplate(ureq);
        } else if (source instanceof Link) {
            Link link = (Link) source;
            String cmd = link.getCommand();
            AssessmentTemplateImpl template = (AssessmentTemplateImpl) link.getUserObject();
            
            if ("edit".equals(cmd)) {
                doEditTemplate(ureq, template);
            } else if ("delete".equals(cmd)) {
                doDeleteTemplate(template);
            } else if ("export".equals(cmd)) {
                doExportTemplate(template);
            }
        }
    }
    
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == editCtrl) {
            if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
                loadTemplates();
            }
            cmc.deactivate();
            cleanUp();
        } else if (source == cmc) {
            cleanUp();
        }
    }
    
    private void cleanUp() {
        removeAsListenerAndDispose(editCtrl);
        removeAsListenerAndDispose(cmc);
        editCtrl = null;
        cmc = null;
    }
    
    private void doCreateTemplate(UserRequest ureq) {
        editCtrl = new AssessmentTemplateEditController(ureq, getWindowControl(), null);
        listenTo(editCtrl);
        
        cmc = new CloseableModalController(getWindowControl(), translate("close"), 
                editCtrl.getInitialComponent(), true, translate("template.create"));
        listenTo(cmc);
        cmc.activate();
    }
    
    private void doEditTemplate(UserRequest ureq, AssessmentTemplateImpl template) {
        editCtrl = new AssessmentTemplateEditController(ureq, getWindowControl(), template);
        listenTo(editCtrl);
        
        cmc = new CloseableModalController(getWindowControl(), translate("close"), 
                editCtrl.getInitialComponent(), true, translate("template.edit"));
        listenTo(cmc);
        cmc.activate();
    }
    
    private void doDeleteTemplate(AssessmentTemplateImpl template) {
        templateService.deleteTemplate(template.getKey());
        showInfo("template.deleted");
        loadTemplates();
    }
    
    private void doExportTemplate(AssessmentTemplateImpl template) {
        String exported = templateService.exportTemplate(template.getKey());
        if (exported != null) {
            showInfo("template.exported");
        }
    }
    
    /**
     * Row wrapper for template display
     */
    public static class TemplateRow {
        private final AssessmentTemplateImpl template;
        private Link editLink;
        private Link deleteLink;
        private Link exportLink;
        
        public TemplateRow(AssessmentTemplateImpl template) {
            this.template = template;
        }
        
        public Long getKey() {
            return template.getKey();
        }
        
        public String getName() {
            return template.getName();
        }
        
        public String getDescription() {
            return template.getDescription();
        }
        
        public Link getEditLink() {
            return editLink;
        }
        
        public void setEditLink(Link editLink) {
            this.editLink = editLink;
        }
        
        public Link getDeleteLink() {
            return deleteLink;
        }
        
        public void setDeleteLink(Link deleteLink) {
            this.deleteLink = deleteLink;
        }
        
        public Link getExportLink() {
            return exportLink;
        }
        
        public void setExportLink(Link exportLink) {
            this.exportLink = exportLink;
        }
    }
}
