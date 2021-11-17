package org.olat.restapi.repository.course;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.CatalogEntry;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.ui.CatalogEntryImageMapper;
import org.olat.restapi.support.vo.CatalogDetailVO;
import org.olat.restapi.support.vo.CatalogEntryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.*;
import java.util.*;

@Tag(name = "CoursesCatalogWebService")
@Component
@Path("repo/catalog/detail/")
public class CoursesCatalogWebService {
    private static final String VERSION = "1.0";

    private static final Logger log = Tracing.createLoggerFor(CoursesCatalogWebService.class);
    @Autowired
    private CatalogManager catalogManager;

    @Autowired
    CoursesDetailWebService coursesDetailWebService;

    /**
     * Retrieves the version of the Courses Catalog Web Service
     *
     * @return
     */
    @GET
    @Path("version")
    @Operation(summary = "Retrieves the version of the Courses Catalog WebService", description = "Retrieves the version of the  Courses Catalog WebService")
    @ApiResponse(responseCode = "200", description = "The version of this specific Courses Catalog WebService ")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getVersion() {
        return Response.ok(VERSION).build();
    }

    @GET
    @Operation(summary = "Returns the list of roots detail", description = "Returns the list of roots detail")
    @ApiResponse(responseCode = "200", description = "Array of results for the whole the roots", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))),
            @Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class)))})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getRoots(@Context HttpServletRequest httpRequest,
                             @Context Request request,
                             @Context UriInfo uriInfo) {
        var entryVOes = new CatalogDetailVO[0];
        List<CatalogEntry> rootEntries = catalogManager.getRootCatalogEntries();
        if (!ObjectUtils.isEmpty(rootEntries)) {
            CatalogEntry root = rootEntries.get(0);
            List<CatalogEntry> moduleEntry = sortCatalog(root, catalogManager.getChildrenOf(root));
            entryVOes = toArrayCatalogDetailVO(moduleEntry, getImageRootURL());
        }
        return Response.ok(entryVOes).build();
    }

    @GET
    @Path("{parentKey}/children")
    @Operation(summary = "Returns a list of children detail", description = "Returns a list of children detail")
    @ApiResponse(responseCode = "200", description = "The list of children detail", content = {
            @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class))),
            @Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = CatalogEntryVO.class)))})
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response getChildren(@PathParam("parentKey") Long parentKey,
                                @Context HttpServletRequest httpRequest,
                                @Context Request request) {
        var ce = catalogManager.loadCatalogEntry(parentKey);
        var entryVOes = new CatalogDetailVO[0];
        if (!ObjectUtils.isEmpty(ce)) {
            List<CatalogEntry> entries = sortCatalog(ce, catalogManager.getChildrenOf(ce));
            if (ObjectUtils.isEmpty(entries)) {
                return Response.ok(entryVOes).build();
            }
            var imageRootURL = "";
            if (entries.get(0).getType() == 1) {
                imageRootURL = coursesDetailWebService.getImageRootURL();
            } else {
                imageRootURL = getImageRootURL();
            }
            entryVOes = toArrayCatalogDetailVO(entries, imageRootURL);

        }

        return Response.ok(entryVOes).build();
    }

    public List<CatalogEntry> sortCatalog(CatalogEntry ce, List<CatalogEntry> nodeEntries) {
        if (catalogManager.isCategorySortingManually(ce)) {
            Comparator<CatalogEntry> comparator = Comparator.comparingInt(CatalogEntry::getPosition);
            nodeEntries.sort(comparator);
        }
        return nodeEntries;
    }

    private CatalogDetailVO[] toArrayCatalogDetailVO(List<CatalogEntry> entries, String imageRootURL) {
        int count = 0;
        CatalogDetailVO[] entryVOes = new CatalogDetailVO[entries.size()];
        for (CatalogEntry entry : entries) {
            String imageURL = "";
            if (!ObjectUtils.isEmpty(imageRootURL)) {
                String imageName = "";
                if (entry.getType() == 0) {
                    imageName = getImageNameCatalog(entry);
                } else {
                    imageName = coursesDetailWebService.getImageNameCourse(entry.getRepositoryEntry());
                }
                if (!ObjectUtils.isEmpty(imageName))
                    imageURL = imageRootURL + "/" + imageName;
            }
            entryVOes[count++] = get(entry, imageURL);
        }
        return entryVOes;
    }

    public static CatalogDetailVO get(CatalogEntry entry, String imageURL) {
        CatalogDetailVO vo = new CatalogDetailVO();
        vo.setKey(entry.getKey());
        if (entry.getType() == 0) {
            vo.setName(entry.getShortTitle());
            vo.setDescription(FilterFactory.getHtmlTagAndDescapingFilter().filter(entry.getDescription()));
        } else {
            vo.setName(entry.getRepositoryEntry().getDisplayname());
            vo.setDescription(FilterFactory.getHtmlTagAndDescapingFilter().filter(entry.getRepositoryEntry().getDescription()));
        }

        vo.setImageURL(imageURL);
        vo.setParentKey(entry.getParent() == null ? null : entry.getParent().getKey());
        vo.setRepositoryEntryKey(entry.getRepositoryEntry() == null ? null : entry.getRepositoryEntry().getKey());
        CatalogEntry module = getRoot(entry);
        if (module != null)
            vo.setRootName(module.getShortTitle());
        vo.setCourseIds(new ArrayList<>(getListCourseId(entry, new HashSet<>())));
        return vo;
    }

    public String getImageNameCatalog(CatalogEntry entry) {
        VFSLeaf image = catalogManager.getImage(entry);
        if (image != null) return image.getName() + "?m="
                + (image.getMetaInfo().getLastModified().getTime()
                - image.getMetaInfo().getCreationDate().getTime());

        return "";
    }

    String getImageRootURL() {
        MapperKey mapperThumbnailKey =
                CoreSpringFactory.getImpl(MapperService.class).register(null, "catalogentryImage", new CatalogEntryImageMapper());
        return mapperThumbnailKey.getUrl();

    }

    public static CatalogEntry getRoot(CatalogEntry entry) {
        if (ObjectUtils.isEmpty(entry.getParent())) return null;
        else if (ObjectUtils.isEmpty(entry.getParent().getParent())) return entry;
        else return getRoot(entry.getParent());
    }

    public static int countCourse(CatalogEntry entry, int count) {
        if (entry.getType() == 1) count = count + 1;
        for (CatalogEntry children : entry.getChildren()) {
            count = countCourse(children, count);
        }
        return count;
    }

    public static Set<Long> getListCourseId(CatalogEntry entry, Set<Long> courseIds) {
        if (courseIds == null) courseIds = new HashSet<>();
        if (entry == null) return courseIds;
        if (entry.getType() == 1)
            courseIds.add(entry.getRepositoryEntry().getOlatResource().getResourceableId());
        for (CatalogEntry children : entry.getChildren()) {
            courseIds = getListCourseId(children, courseIds);
        }
        return courseIds;
    }

}
