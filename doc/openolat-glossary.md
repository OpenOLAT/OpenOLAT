# OpenOlat Glossary

A reference glossary of product-specific terms used in OpenOlat.
These are terms that have specific meaning within the OpenOlat LMS context
and should be used consistently across translations, documentation, and code.

**Total terms:** 172

## System roles

### Administrator

Has full system administration rights.

*Canonical key:* `org.olat.repository:role.administrator`

### Author

Can create and manage learning resources.

*Canonical key:* `org.olat.course.nodes.basiclti:author.roles`

### Education manager

Can manage educational programs.

*Canonical key:* `org.olat.admin.user.bulkChange:table.role.educationmanager`

### Group manager

Can manage groups across the system.

### Learning resource manager

Can manage learning resources across the system.

*Canonical key:* `org.olat.repository:role.learning.resource.manager`

### Line manager

Has line management permissions for user oversight.

*Canonical key:* `org.olat.admin.user:role.linemanager`

### Principal

Has principal oversight permissions.

*Canonical key:* `org.olat.repository:role.principal`

### Quality manager

Can manage quality management processes.

*Canonical key:* `org.olat.admin.user:role.qualitymanager`

### Question bank manager

Can manage the question bank pool.

*Canonical key:* `org.olat.admin.user.bulkChange:table.role.poolmanager`

### Roles manager

Can manage role assignments.

*Canonical key:* `org.olat.admin.user:role.rolesmanager`

### User manager

Can manage user accounts and roles.

*Canonical key:* `org.olat.user.ui.organisation:role.usermanager`


## Course/Resource roles

### Coach

Tutors/coaches participants in a course or group.

*Canonical key:* `org.olat.repository:access.info.role.coach`

### Owner

Owner of a learning resource or course.

*Canonical key:* `org.olat.repository:access.info.role.owner`

### Participant

A learner enrolled in a course or group.

*Canonical key:* `org.olat.repository:access.info.role.participant`


## Curriculum roles

### Master coach

Supervises coaches across curriculum implementations.

*Canonical key:* `org.olat.course.member:role.origin.cpl.mastercoach`


## Course Planner roles

### Product owner

Manages a complete product (curriculum) in Course Planner.

*Canonical key:* `org.olat.modules.curriculum.ui:add.curriculumowner`

### Element owner

Manages a specific curriculum element within Course Planner.

*Canonical key:* `org.olat.modules.curriculum.ui:add.curriculumelementowner`


## Access roles

### Guest

Anonymous user with limited read-only access.

*Canonical key:* `org.olat.course.noderight.ui:role.guest`

### Invitee

External user invited to access specific resources.

*Canonical key:* `org.olat.admin.user:role.invitee`


## Modules

### Access control

Booking and offer management for learning resources.

*Canonical key:* `org.olat.repository:tab.accesscontrol`

### Assessment management

Central overview and management of assessments.

*Canonical key:* `org.olat.course.assessment.ui.mode:admin.menu.title`

### Catalog

Structured catalog for publishing and finding learning resources.

*Canonical key:* `org.olat.repository.ui.author.copy.wizard:catalog`

### Certificates

Automatic or manual PDF certificate generation.

*Canonical key:* `org.olat.home:menu.certificates`

### Course Planner

Manage curricula, implementations, and educational structures (formerly "Curriculum").

*Canonical key:* `org.olat.course.member:course.curriculum`

### Course reminders

Automated email reminders based on course conditions.

*Canonical key:* `org.olat.modules.reminder.ui:admin.menu.title`

### Document pool

Taxonomy-based document management area.

*Canonical key:* `org.olat.core.commons.chiefcontrollers:DocumentPool`

### Events / Absences

Lecture block management with absence tracking.

*Canonical key:* `org.olat.modules.lecture.ui:admin.menu.title`

### Groups

Collaborative groups for learning and project work.

*Canonical key:* `org.olat.repository.ui.author.copy.wizard:groups`

### Levels/Grading

Configuration of grading scales and performance levels.

*Canonical key:* `org.olat.modules.grade.ui:admin.menu.title`

### Library

Document library for shared reading materials.

*Canonical key:* `org.olat.course.style.ui:system.image.title`

### Media Center

Central repository for media elements reusable across content.

*Canonical key:* `org.olat.user.ui.admin:tool.personal.media.center`

### Organisations

Hierarchical organizational structure for role-scoped management.

*Canonical key:* `org.olat.course.learningpath.ui:config.exceptional.obligation.organisations`

### Projects

Project management tool with to-dos, files, and decisions.

*Canonical key:* `org.olat.admin.user:view.projects`

### Quality management

Tools for quality assurance surveys and data collections.

*Canonical key:* `org.olat.admin.site.ui:QualitySiteDef`

### Question bank

Shared pool of assessment questions across courses.

*Canonical key:* `org.olat.ims.qti21.ui.editor:form.pool`

### Taxonomy

Hierarchical classification system for competences and content.

*Canonical key:* `org.olat.repository:selected.taxonomy.tree`

### To-do

Personal and course-related task management.

*Canonical key:* `org.olat.course.todo.ui:course.todo.collection.overview.change.todo`

### ePortfolio

Electronic portfolio for learner reflection and evidence collection.

*Canonical key:* `org.olat.collaboration:collabtools.named.hasPortfolio`


### Absence management

Module for tracking and managing participant absences from lectures.

### Contact tracing

Location-based contact tracing system.

*Canonical key:* `org.olat.modules.contacttracing.ui:admin.menu.title`


## Features

### Assessment inspection

Supervised review of completed assessments.

*Canonical key:* `org.olat.course.assessment.ui.inspection:inspection.overview.title`

### Assessment mode

Controlled exam environment with restricted browser access.

*Canonical key:* `org.olat.course.nodes.iq:assessment.mode`

### Badges

Digital badges awarded for achievements.

*Canonical key:* `org.olat.home:menu.badges`

### Coaching

Cross-course overview for coaches to monitor learners.

*Canonical key:* `org.olat.home:course.coached`

### Conventional course

Traditional course design with menu-based navigation (non-learning-path).

*Canonical key:* `org.olat.repository:course.design.classic.type`

### Evidence of achievement

Record of a user's performance in assessable course elements.

*Canonical key:* `org.olat.home:menu.efficiencyStatements`

### File Hub

Central file browser aggregating files from courses, groups, and personal storage.

*Canonical key:* `org.olat.home:file.hub`

### Learning path

Sequential course design with progress tracking and completion criteria.

*Canonical key:* `org.olat.course.learningpath.ui:access.provider.name`

### REST API

RESTful API for external system integration.

*Canonical key:* `org.olat.admin.restapi:rest.title`

### WebDAV

Web-based Distributed Authoring and Versioning for file access.

*Canonical key:* `org.olat.core.commons.services.webdav.ui:admin.menu.title`


### Assessment tool

Central interface for coaches and owners to evaluate, grade, and manage learner assessments.

*Canonical key:* `org.olat.course.assessment.ui:command.assessment`

### Audio/Video recording

Recording capabilities for audio and video content.

*Canonical key:* `org.olat.modules.audiovideorecording:admin.menu.title`

### Bulk assessment

Feature allowing coaches to assess multiple participants simultaneously.

*Canonical key:* `org.olat.course.assessment.ui:menu.bulkfocus`

### Content editor

WYSIWYG editor for creating rich content within Page course elements.

### Course editor

Tool used to build and modify course structure and course elements.

*Canonical key:* `org.olat.course.editor:header.tools.editor`

### Credit points

System for awarding and tracking academic credit points for course completion.

*Canonical key:* `org.olat.home:menu.creditpoint`

### Data collection

Surveys used within the Quality Management module to gather feedback.

*Canonical key:* `org.olat.modules.quality.ui:data.collection.title`

### Instant messaging

Built-in chat/messenger for real-time communication between users.

### Notes

Personal note-taking feature for users.

*Canonical key:* `org.olat.home:menu.note`

### OAI-PMH

Open Archives Initiative Protocol for Metadata Harvesting, used for catalog exposure.

*Canonical key:* `org.olat.modules.oaipmh.ui:admin.menu.title`

### Peer review

Feature enabling participants to review each other's work in task elements.

### Recertification

Process of re-earning a certificate after expiration within a certification program.

### Role switching

Ability for multi-role members to change their active role perspective within a course.

### Safe Exam Browser

Integration with Safe Exam Browser (SEB) for secure online exams.

*Canonical key:* `org.olat.course.assessment.ui.mode:mode.safeexambrowser`

### Subscriptions

User subscriptions to notifications about changes in courses and resources.

*Canonical key:* `org.olat.home:menu.notifications`

### Video collection

Central area aggregating all shared video learning resources.

### Whiteboard

Visual collaboration tool (powered by draw.io) within the Projects module.


## Areas

### Authoring

The area where authors create and manage learning resources.

*Canonical key:* `org.olat.repository.ui.author:author.title`


## Concepts

### Certification program

A structured certification path with recertification requirements.

*Canonical key:* `org.olat.course.certificate.ui:options.certificate.program.title`

### Curriculum element

A node in the curriculum tree representing a module, semester, or subject.

### Implementation

A concrete offering of a curriculum element with dates and members.

*Canonical key:* `org.olat.modules.curriculum.ui:report.header.implementation`

### Offer

A booking configuration that controls access to a learning resource.

*Canonical key:* `org.olat.resource.accesscontrol.ui:filter.offer`

### Repository entry

A managed learning resource in the authoring area (course, test, form, etc.).


### Access code

A password-based booking method restricting course enrollment to code holders.

*Canonical key:* `org.olat.resource.accesscontrol.ui:accesscontrol.name.token`

### Booking order

A record created when a user books or enrolls in a course through the catalog.

*Canonical key:* `org.olat.modules.curriculum.ui:booking.orders`

### Coach folder

A dedicated file area visible only to coaches within a course.

### Competence

Skill or competence assigned to users via taxonomy.

*Canonical key:* `org.olat.admin.user:view.competences`

### Learning area

A named grouping of course groups used to simplify group-based visibility rules.

*Canonical key:* `org.olat.course.condition:form.easy.learningGroup.area`

### Learning progress

Tracking of user progress through a learning path course.

### Product

Top-level entity in Course Planner representing a curriculum with implementations and courses.

*Canonical key:* `org.olat.modules.curriculum.ui:curriculum.title`

### Publication status

Lifecycle state of a learning resource: Preparation, Review, Access for Coach, Published, Finished.

*Canonical key:* `org.olat.repository:cif.publish`

### Storage folder

Internal file management area within a course for storing course files.

*Canonical key:* `org.olat.course.editor:command.coursefolder`

### Template course

A source course from which implementation instances are automatically created in Course Planner.


## Standards

### QTI 2.1

Question and Test Interoperability standard for assessment items.

*Canonical key:* `org.olat.core.commons.services.export.ui:archive.qti21`

### SCORM

Sharable Content Object Reference Model for e-learning content packaging.

*Canonical key:* `org.olat.course.archiver:scorm`


## Integrations

### LTI

Learning Tools Interoperability standard for external tool integration.

*Canonical key:* `org.olat.ims.lti13.ui:admin.menu.title`

### Microsoft Teams

Microsoft Teams virtual meeting integration.

*Canonical key:* `org.olat.collaboration:collabtools.named.hasTeams`

### Zoom

Zoom video conferencing integration.

*Canonical key:* `org.olat.collaboration:collabtools.named.hasZoom`


### BigBlueButton

Web conferencing integration for virtual classrooms.

*Canonical key:* `org.olat.collaboration:collabtools.named.hasBigBlueButton`

### card2brain

card2brain flashcard learning integration.

### draw.io

draw.io diagramming integration used for Whiteboard in Projects.

### Edubase

Edubase digital textbook integration.

### edu-sharing

edu-sharing educational content repository integration.

### GoToMeeting

GoToMeeting/GoToTraining virtual meeting integration.

*Canonical key:* `org.olat.modules.gotomeeting.ui:admin.menu.title`

### JupyterHub

JupyterHub interactive computing integration.

### Mediasite

Sonic Foundry Mediasite lecture capture integration.

### Opencast

Opencast lecture recording and video management integration.

### OpenMeetings

Apache OpenMeetings web conferencing integration.

### SharePoint / OneDrive

Microsoft SharePoint and OneDrive file access integration.

### Vitero

Vitero virtual team room integration.


## Course Elements

### Adobe Connect

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes.adobeconnect:title_vc`

### Appointment scheduling

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_appointments`

### Assessment

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_ms`

### Assignment of dates

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_den`

### BigBlueButton

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_bigbluebutton`

### Blog

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_blog`

### CP learning content

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes.cp:title_cp`

### Check list

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_checklist`

### Check list (old)

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_cl`

### E-mail

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_co`

### Enrolment

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes.en:title_en`

### External page

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_tu`

### File dialog

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_dialog`

### Folder

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_bc`

### Forum

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_fo`

### Grouptask

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_gta`

### HTML-Page

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_sp`

### LTI page

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes.basiclti:title_lti`

### Link list

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_ll`

### Page

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_cepage`

### Participant Folder

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_pf`

### Participant list

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes.members:title_info`

### Podcast

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_podcast`

### Portfolio task

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_ep`

### Practice

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_practice`

### Questionnaire

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_iqsurv`

### SCORM learning content

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_scorm`

### Selection

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_cns`

### Self-test

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_iqself`

### Structure

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_st`

### Task

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_ita`

### Test

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_qti21assessment`

### Topic assignment

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_projectbroker`

### Topic broker

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_topicbroker`

### Video

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_video`

### Video task

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_videotask`

### Wiki

A course element (building block) that can be added to a course structure.

*Canonical key:* `org.olat.course.nodes:title_wiki`


### card2brain

A course element for flashcard-based learning via card2brain.

*Canonical key:* `org.olat.course.nodes.card2brain:title_card2brain`

### Document

A course element for embedding and displaying documents.

*Canonical key:* `org.olat.course.nodes:title_document`

### Edubase

A course element for digital textbook content via Edubase.

*Canonical key:* `org.olat.course.nodes.edubase:title_edubase`

### edu-sharing

A course element for accessing shared educational resources via edu-sharing.

*Canonical key:* `org.olat.course.nodes.edusharing:title_edusharing`

### GoToMeeting

A course element for GoToMeeting virtual meetings.

*Canonical key:* `org.olat.course.nodes.gotomeeting:title_gotomeeting`

### JupyterHub

A course element for interactive Jupyter computing notebooks.

*Canonical key:* `org.olat.course.nodes.jupyterhub:title_jupyterhub`

### Live stream

A course element for live video streaming.

*Canonical key:* `org.olat.course.nodes.livestream:title_livestream`

### Mediasite

A course element for Sonic Foundry Mediasite lecture capture.

*Canonical key:* `org.olat.course.nodes.mediasite:title_mediasite`

### Opencast

A course element for Opencast lecture recordings and videos.

*Canonical key:* `org.olat.course.nodes.opencast:title_opencast`

### OpenMeetings

A course element for Apache OpenMeetings web conferencing.

*Canonical key:* `org.olat.course.nodes.openmeetings:title_vc`

### Vitero

A course element for Vitero virtual team rooms.

*Canonical key:* `org.olat.course.nodes.vitero:title_vc`


## Learning Resource Types

### Animation

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.ANIM`

### Blog entry

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.modules.ceditor.ui:FileResource.BLOG`

### Excel

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.XLS`

### File

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.core.commons.chiefcontrollers:FileResource.FILE`

### Form

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.FORM`

### Glossary

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.GLOSSARY`

### Image

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.IMAGE`

### Movie

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.MOVIE`

### Other file

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.FILE`

### PDF

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.PDF`

### PowerPoint

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.PPT`

### Resource folder

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.SHAREDFOLDER`

### Sound

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.SOUND`

### Test (QTI 1.2 - no longer supported)

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.TEST`

### Wiki page

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.modules.ceditor.ui:FileResource.WIKI`

### Word

A type of learning resource that can be created or imported in the authoring area.

*Canonical key:* `org.olat.repository:FileResource.DOC`

### Blog

A type of learning resource for blog content.

*Canonical key:* `org.olat.repository:FileResource.BLOG`

### Course

The main learning resource type for structured online courses.

*Canonical key:* `org.olat.repository:FileResource.COURSE`

### Podcast

A type of learning resource for podcast content.

*Canonical key:* `org.olat.repository:FileResource.PODCAST`

### Portfolio 2.0 Template

Template defining structure and tasks for ePortfolio assignments.

*Canonical key:* `org.olat.repository:FileResource.BINDERTMPL`

### Survey

A type of learning resource for questionnaires and surveys.

### Test (QTI 2.1)

Learning resource for assessments in IMS QTI 2.1 format.

*Canonical key:* `org.olat.repository:FileResource.IMSQTI21`

### Video

A type of learning resource for video content with annotations and quizzes.

*Canonical key:* `org.olat.repository:FileResource.VIDEO`

### Wiki

A type of learning resource for collaborative wiki content.

*Canonical key:* `org.olat.repository:FileResource.WIKI`

