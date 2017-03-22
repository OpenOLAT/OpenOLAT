package org.olat.ims.qti21.model.xml;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Map;

import uk.ac.ed.ph.jqtiplus.SimpleJqtiFacade;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemProcessingInitializer;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionControllerSettings;
import uk.ac.ed.ph.jqtiplus.state.ItemProcessingMap;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ClassPathResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.FileResourceLocator;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * Run an assessmentItem.
 * 
 * Initial date: 22 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RunningItemHelper {
	
    public static ItemSessionController run(File inputFile, Map<Identifier, ResponseData> responseMap) {
    	final ResourceLocator inputResourceLocator = new FileResourceLocator();
        return run(inputFile.toURI(), responseMap, inputResourceLocator);
    }
    
    public static ItemSessionController run(URI inputUri, Map<Identifier, ResponseData> responseMap) {
        final ResourceLocator inputResourceLocator = new ClassPathResourceLocator();
        return run(inputUri, responseMap, inputResourceLocator);
    }
    
    public static ItemSessionController run(URI inputUri, Map<Identifier, ResponseData> responseMap, ResourceLocator inputResourceLocator) {
        SimpleJqtiFacade simpleJqtiFacade = new SimpleJqtiFacade();
        ResolvedAssessmentItem resolvedAssessmentItem = simpleJqtiFacade.loadAndResolveAssessmentItem(inputResourceLocator, inputUri);
        ItemProcessingMap itemProcessingMap = new ItemProcessingInitializer(resolvedAssessmentItem, false).initialize();
        ItemSessionState itemSessionState = new ItemSessionState();
        ItemSessionControllerSettings itemSessionControllerSettings = new ItemSessionControllerSettings();
        ItemSessionController itemSessionController = simpleJqtiFacade.createItemSessionController(itemSessionControllerSettings, itemProcessingMap, itemSessionState);

        itemSessionController.initialize(new Date());
        itemSessionController.performTemplateProcessing(new Date());
        itemSessionController.enterItem(new Date());
        itemSessionController.bindResponses(new Date(), responseMap);
        itemSessionController.commitResponses(new Date());
        itemSessionController.performResponseProcessing(new Date());
        
        AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
        assessmentItem.getItemBody().willShowFeedback(itemSessionController);

        itemSessionController.endItem(new Date());
        itemSessionController.exitItem(new Date());
        return itemSessionController;
    }
}
