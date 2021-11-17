package org.olat.restapi.repository.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.components.form.flexible.impl.elements.richText.RichTextContainerMapper;
import org.olat.core.gui.control.generic.iframe.IFrameDisplayController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.course.nodes.video.VideoEditController;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.QTI21QuestionType;
import org.olat.ims.qti21.model.xml.AssessmentHtmlBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.manager.VideoMediaMapper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.restapi.support.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.olat.restapi.repository.course.CoursesWebService.loadCourse;
import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getRoles;

@Tag(name = "CoursesDetailWebService")
@Component
@Path("repo/courses/detail")
public class CoursesDetailWebService {
    private static final Logger log = Tracing.createLoggerFor(CoursesDetailWebService.class);
    private static final String VERSION = "1.0";

    @Autowired
    private RepositoryManager repositoryManager;

    @Autowired
    private VideoManager videoManager;

    @Autowired
    private CourseElementWebService courseElementWebService;

    @Autowired
    private QTI21Service qtiService;

    @Autowired
    @GET
    @Path("version")
    @Operation(summary = "The version of the Courses Detail WebService", description = "The version of the Courses Detail WebService")
    @ApiResponse(responseCode = "200", description = "The version of this specific Courses Detail Web Service")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVersion() {
        return Response.ok(VERSION).build();
    }


    /**
     * Get all courses viewable by the authenticated user
     *
     * @param httpRequest The HTTP request
     * @return
     */
    @POST
    @Operation(summary = "Get courses detail", description = "Get courses detail")
    @ApiResponse(responseCode = "200", description = "List of visible courses detail", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CourseDetailVO.class))),
            @Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CourseDetailVO.class)))})
    public Response getCourseList(@Context HttpServletRequest httpRequest,
                                  @RequestBody CourseRequestVO courseRequestVO) {
        Roles roles = getRoles(httpRequest);
        Identity identity = getIdentity(httpRequest);
        SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters(identity, roles, CourseModule.getCourseTypeName());
        params.setIdRefsAndTitle(courseRequestVO.getRefsAndTitle());
        params.setResIds(courseRequestVO.getCourseIds());
        List<RepositoryEntry> repoEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, -1, false);
        CourseDetailVO[] vos = toCourseDetailVO(repoEntries, getImageRootURL());
        if (!StringUtils.isEmpty(courseRequestVO.getRefsAndTitle())) {
            CourseDetailVO[] filterArray = filterCourseDetailVO(vos, courseRequestVO.getRefsAndTitle());
            return Response.ok(filterArray).build();
        }
        return Response.ok(vos).build();

    }

    private CourseDetailVO[] filterCourseDetailVO(CourseDetailVO[] vos, String refsAndTitle) {
        //remove all elements which is not predictive
        List<CourseDetailVO> result = new ArrayList<>();
        List<CourseDetailVO> lstVos = Arrays.asList(vos);
        String regex = "(?i)\\b" + refsAndTitle;
        Pattern pattern = Pattern.compile(regex);
        for (CourseDetailVO CourseDetailVO : lstVos) {
            //check string here
            Matcher matcher = pattern.matcher(CourseDetailVO.getTitle());
            if (matcher.find()) {
                result.add(CourseDetailVO);
            }
        }

        //return here
        CourseDetailVO[] arrayResult = result.toArray(new CourseDetailVO[0]);
        return arrayResult;

    }


    @GET
    @Path("/{courseId}/elements")
    @Operation(summary = "Retrieves metadata of the course node detail", description = "Retrieves metadata of the course node detail" )
    @ApiResponse(responseCode = "200", description = "The course node metadatas", content = {
            @Content(mediaType = "application/json", schema = @Schema(implementation = CourseNodeDetailVO.class))})
    @ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
    @ApiResponse(responseCode = "404", description = "The course or parentNode not found")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getCourseNodeDetail(@PathParam("courseId") Long courseId,
                                        @Context HttpServletRequest request) {
        ICourse course = loadCourse(courseId);
        List<CourseNodeDetailVO> courseNodeVOList = new ArrayList<>();
        if (course == null) {
            return Response.ok(courseNodeVOList).build();
        }
        MapperKey mapperBaseKey;
        if (course.getRunStructure().getRootNode().getChildCount() > 0) {
            for (int i = 0; i < course.getRunStructure().getRootNode().getChildCount(); i++) {
                INode node = course.getRunStructure().getRootNode().getChildAt(i);
                String linkMedia = "";
                String type = "";
                List<TestQuestionVO> questions = new ArrayList<>();
                CourseEditorTreeNode parentNode = courseElementWebService.getParentNode(course, node.getIdent());
                if (parentNode == null) {
                    return Response.serverError().status(Response.Status.NOT_FOUND).build();
                }
                if ("sp".equals(parentNode.getCourseNode().getType())) {
                    type = "TEXT";
                    String fileName = (String) parentNode.getCourseNode()
                            .getModuleConfiguration().get(SPEditController.CONFIG_KEY_FILE);
                    VFSContainer rootContainer = course.getCourseFolderContainer();
                    VFSContainer g_new_rootContainer;
                    String startURI = ((fileName.charAt(0) == '/') ? fileName.substring(1) : fileName);
                    int sla = startURI.lastIndexOf('/');
                    if (sla != -1) {
                        String root = startURI.substring(0, sla);
                        startURI = startURI.substring(sla + 1);
                        g_new_rootContainer = (VFSContainer) rootContainer.resolve(root);
                    } else {
                        g_new_rootContainer = rootContainer;
                    }
                    String mapperId = IFrameDisplayController.class.getSimpleName() + ":" + VFSManager.getRealPath(g_new_rootContainer);
                    Mapper contentMapper = new RichTextContainerMapper(g_new_rootContainer, mapperId);
                    mapperBaseKey = CoreSpringFactory.getImpl(MapperService.class).register(null, mapperId, contentMapper, 3600);
                    linkMedia = mapperBaseKey.getUrl() + "/" + startURI;

                } else if ("video".equals(parentNode.getCourseNode().getType())) {
                    type = "VIDEO";
                    RepositoryEntry videoEntry = VideoEditController
                            .getVideoReference(parentNode.getCourseNode().getModuleConfiguration(), false);
                    Mapper vfsContainerMapper =
                            new VideoMediaMapper(videoManager.getMasterContainer(videoEntry.getOlatResource()));
                    String transcodingMapperId =
                            CodeHelper.getRAMUniqueID() +
                                    "-transcoding-" + videoEntry.getOlatResource().getResourceableId();
                    mapperBaseKey = CoreSpringFactory.getImpl(MapperService.class).
                            register(null, transcodingMapperId, vfsContainerMapper, 3600);
                    linkMedia = mapperBaseKey.getUrl() + "/" + "video.mp4";

                } else if ("iqtest".equals(parentNode.getCourseNode().getType())) {
                    type = "QUIZ";
                    if (node instanceof IQTESTCourseNode) {
                        IQTESTCourseNode iqtestCourseNode = (IQTESTCourseNode) node;
                        RepositoryEntry testEntry = iqtestCourseNode.getCachedReferencedRepositoryEntry();
                        ResolvedAssessmentTest resolvedAssessmentTest
                                = iqtestCourseNode.loadResolvedAssessmentTest(testEntry);
                        List<AssessmentItemRef> itemRefs = resolvedAssessmentTest.getAssessmentItemRefs();
                        for (AssessmentItemRef itemRef : itemRefs) {
                            ResolvedAssessmentItem resolvedAssessmentItem = resolvedAssessmentTest.getResolvedAssessmentItem(itemRef);
                            AssessmentItem assessmentItem = resolvedAssessmentItem.getRootNodeLookup().extractIfSuccessful();
                            TestQuestionVO questionVO = new TestQuestionVO();
                            questionVO.setContent(assessmentItem.getTitle());
                            questionVO.setId(assessmentItem.getIdentifier());
                            setQuestion(assessmentItem, questionVO);
                            questions.add(questionVO);
                        }
                    }
                }
                CourseNodeDetailVO vo = get(parentNode.getCourseNode(), linkMedia);
                vo.setType(type);
                vo.setQuestions(questions);
                courseNodeVOList.add(vo);
            }
        }
        return Response.ok(courseNodeVOList).build();
    }

    private void setQuestion(AssessmentItem assessmentItem, TestQuestionVO questionVO) {
        QTI21QuestionType questionType = QTI21QuestionType.getType(assessmentItem);
        questionVO.setType(questionType.getPoolQuestionType().name());
        List<String> correctAnswer = new ArrayList<>();
        List<TestAnswerVO> answers = new ArrayList<>();
        String question = "";
        switch (questionType) {
            case sc:
                SingleChoiceAssessmentItemBuilder itemBuilderSC = new SingleChoiceAssessmentItemBuilder(assessmentItem, qtiService.qtiSerializer());
                question = FilterFactory.getHtmlTagAndDescapingFilter().filter(itemBuilderSC.getQuestion());
                for (SimpleChoice choice : itemBuilderSC.getChoices()) {
                    answers.add(getAnswers(choice, itemBuilderSC.getHtmlHelper()));
                    if (itemBuilderSC.isCorrect(choice)) correctAnswer.add(String.valueOf(choice.getIdentifier()));
                }
                break;
            case mc:
                MultipleChoiceAssessmentItemBuilder itemBuilderMC = new MultipleChoiceAssessmentItemBuilder(assessmentItem, qtiService.qtiSerializer());
                question = FilterFactory.getHtmlTagAndDescapingFilter().filter(itemBuilderMC.getQuestion());
                for (SimpleChoice choice : itemBuilderMC.getChoices()) {
                    answers.add(getAnswers(choice, itemBuilderMC.getHtmlHelper()));
                    if (itemBuilderMC.isCorrect(choice)) correctAnswer.add(String.valueOf(choice.getIdentifier()));
                }
                break;
            default:
                new SingleChoiceAssessmentItemBuilder(assessmentItem, qtiService.qtiSerializer());
        }
        if (StringUtils.isNotBlank(question))
            questionVO.setContent(question);
        questionVO.setCorrectAnswers(correctAnswer);
        questionVO.setAnswers(answers);
    }

    public TestAnswerVO getAnswers(SimpleChoice choice, AssessmentHtmlBuilder htmlHelper) {
        TestAnswerVO answer = new TestAnswerVO();
        String choiceContent = htmlHelper.flowStaticString(choice.getFlowStatics());
        answer.setId(choice.getIdentifier().toString());
        answer.setContent(choiceContent);
        return answer;
    }

    public CourseDetailVO[] toCourseDetailVO(List<RepositoryEntry> repoEntries, String imageRootURL) {
        List<CourseDetailVO> voList = new ArrayList<>();
        for (RepositoryEntry repoEntry : repoEntries) {
            try {
                ICourse course = loadCourse(repoEntry.getOlatResource().getResourceableId());
                if (course != null) {
                    String image = getImageNameCourse(repoEntry);
                    if (!ObjectUtils.isEmpty(image)) image = imageRootURL + "/" + image;
                    CourseDetailVO courseVO = get(repoEntry, course, image);
                    voList.add(courseVO);
                }
            } catch (Exception e) {
                log.error("Cannot load the course with this repository entry: {}", repoEntry, e);
            }
        }

        CourseDetailVO[] vos = new CourseDetailVO[voList.size()];
        voList.toArray(vos);
        return vos;
    }

    public CourseDetailVO get(RepositoryEntry re, ICourse course, String imageURL) {
        CourseDetailVO vo = new CourseDetailVO();
        vo.setKey(course.getResourceableId());
        vo.setDisplayName(re.getDisplayname());
        vo.setDescription(FilterFactory.getHtmlTagAndDescapingFilter().filter(re.getDescription()));
        vo.setTitle(course.getCourseTitle());
        vo.setSoftKey(re.getSoftkey());
        vo.setRepoEntryKey(re.getKey());
        vo.setImageURL(imageURL);
        return vo;
    }

    public static CourseNodeDetailVO get(CourseNode node, String linkMedia) {
        CourseNodeDetailVO vo = new CourseNodeDetailVO();
        vo.setId(node.getIdent());
        vo.setLink(linkMedia);
        vo.setParentId(node.getParent() == null ? null : node.getParent().getIdent());
        vo.setShortTitle(node.getShortTitle());
        vo.setShortName(node.getShortName());
        vo.setLongTitle(node.getLongTitle());
        vo.setPosition(node.getPosition());
        return vo;
    }

    public String getImageNameCourse(RepositoryEntry repoEntry) {
        VFSLeaf image = repositoryManager.getImage(repoEntry);
        if (image != null) return image.getName() + "?m=" + image.getMetaInfo().getKey().toString() +
                +(image.getMetaInfo().getLastModified().getTime()
                        - image.getMetaInfo().getCreationDate().getTime());

        return "";
    }

    public String getImageRootURL() {
        MapperKey mapperThumbnailKey =
                CoreSpringFactory.getImpl(MapperService.class).register(
                        null, "repositoryentryImage", new RepositoryEntryImageMapper());
        return mapperThumbnailKey.getUrl();

    }

}
