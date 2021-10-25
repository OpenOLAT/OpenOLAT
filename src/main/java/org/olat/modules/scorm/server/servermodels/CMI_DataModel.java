/**
 *  RELOAD TOOLS
 *
 *  Copyright (c) 2003 Oleg Liber, Bill Olivier, Phillip Beauvoir, Paul Sharples
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Project Management Contact:
 *
 *  Oleg Liber
 *  Bolton Institute of Higher Education
 *  Deane Road
 *  Bolton BL3 5AB
 *  UK
 *
 *  e-mail:   o.liber@bolton.ac.uk
 *
 *
 *  Technical Contact:
 *
 *  Phillip Beauvoir
 *  e-mail:   p.beauvoir@bolton.ac.uk
 *
 *  Paul Sharples
 *  e-mail:   p.sharples@bolton.ac.uk
 *
 *  Web:      http://www.reload.ac.uk
 *
 */

package org.olat.modules.scorm.server.servermodels;

import org.jdom2.Comment;
import org.jdom2.Document;
import org.jdom2.Element;


/**
 * Class to create a new sco CMI JDOM datamodel which will be
 * accssed by the runtime environment and updated accordingly
 * @author Paul Sharples
*/
public class CMI_DataModel extends XMLDocument {
    // some default strings to be used in building the data model
    private String _cmiVersion = "3.4";
    private String _userId;
    private String _userName;
    private String _max_time_allowed;
    private String _time_limit_action;
    private String _data_from_lms;
    private String _mastery_score;
    private String _lesson_location;
    private String _lesson_mode;
    private String _credit_mode;

    /**
     * Our JDOM CMI document
     */
    private Document _model;

    /**
     * Our unique signature
     */
    static final String[] scorm_comments = {
        "This is a version SCORM 1.2 SCO CMI Datamodel",
        "Spawned from Reload Scorm Player - http://www.reload.ac.uk"
    };


    /**
     * default constructor
     */
    public CMI_DataModel(){
    //    
    }

    /**
     * Alternate constructor to allow setup of various datamodel values
     * @param username
     * @param userid
     * @param max_time_allowed
     * @param time_limit_action
     * @param data_from_lms
     * @param mastery_score
     * @param lesson_mode pass null for the default mode "normal". Allowed are only "browse", "review" and "normal"
     * @param credit_mode pass null for the default mode "credit". Allowed are only "credit" and "no-credit"
     */
    public CMI_DataModel(String userid, String username,
                              String max_time_allowed, String time_limit_action,
                              String data_from_lms, String mastery_score,
                              String lesson_mode, String credit_mode
                              ) {
        _userName = username;
        _userId = userid;
        _max_time_allowed = max_time_allowed;
        _time_limit_action = time_limit_action;
        _data_from_lms = data_from_lms;
        _mastery_score = mastery_score;
        // updated 2003-12-19 . Lesson_location should be initialized to an empty string.
        _lesson_location = "";
        _lesson_mode = lesson_mode;
        _credit_mode = credit_mode;
    }

    /**
     * Returns the whole model
     * @return the JDOM cmi model
     */
    public Document getModel(){
        return _model;
    }

    /**
     * buildFreshModel - Method to create a new JDOM
     * CMI sco model.
     * create a fresh sco model starting with <cmi>
     */
    public void buildFreshModel(){
        Element root = new Element ("cmi");
        _model = new Document(root);

        // add the _version
         Element _version = new Element ("_version");
         _version.setText(_cmiVersion);
        _model.getRootElement().addContent(_version);

        // add the <core>
        Element core = new Element ("core");

        // add the children
        Element _childrenCore = new Element("_children");
        _childrenCore.setText("student_id,student_name,lesson_location,credit,lesson_status,entry,score,total_time,lesson_mode,exit,session_time");
        core.addContent(_childrenCore);

        // add the student id
        Element student_id = new Element("student_id");
        student_id.setText(_userId);
        core.addContent(student_id);

        // add the student name
        Element student_name = new Element("student_name");
        student_name.setText(_userName);
        core.addContent(student_name);

        // add the lesson_location
        Element lesson_location = new Element("lesson_location");
        lesson_location.setText(_lesson_location);
        core.addContent(lesson_location);

        // add the credit
        Element credit = new Element("credit");
        if(_credit_mode == null) credit.setText("credit");
        else credit.setText(_credit_mode);
        core.addContent(credit);

        // add the lesson_status
        Element lesson_status = new Element("lesson_status");
        lesson_status.setText("not attempted");
        core.addContent(lesson_status);

        // add the entry
        Element entry = new Element("entry");
        entry.setText("ab-initio");
        core.addContent(entry);

        // add the score
        Element score = new Element("score");
        core.addContent(score);

        // and its children
        Element _childrenScore = new Element("_children");
        _childrenScore.setText("raw,min,max");
        score.addContent(_childrenScore );

        Element scoreraw = new Element("raw");
        scoreraw.setText("");
        score.addContent(scoreraw);

        Element scoremin = new Element("min");
        scoremin.setText("");
        score.addContent(scoremin);

        Element scoremax = new Element("max");
        scoremax.setText("");
        score.addContent(scoremax);

        // add the total_time
        Element total_time = new Element("total_time");
        total_time.setText("0000:00:00.00");
        core.addContent(total_time);

        // add the lesson_mode
        Element lesson_mode = new Element("lesson_mode");
        if(_lesson_mode == null) lesson_mode.setText("normal");
        else lesson_mode.setText(_lesson_mode);
        core.addContent(lesson_mode);

        // add the exit
        Element exit = new Element("exit");
        core.addContent(exit);

        // add the session_time
        Element session_time = new Element("session_time");
        session_time.setText("00:00:00");
        core.addContent(session_time);

        // now add core to the cmi node...
        _model.getRootElement().addContent(core);

        // now continue with the rest of the optional elements...

        // add suspend data
        Element suspend_data = new Element("suspend_data");
        suspend_data.setText("");
        _model.getRootElement().addContent(suspend_data);

        // add launch data
        Element launch_data = new Element("launch_data");
        launch_data.setText(_data_from_lms);
        _model.getRootElement().addContent(launch_data);

        // add comments
        Element comments = new Element("comments");
        comments.setText("");
        _model.getRootElement().addContent(comments);

        // add comments from lms
        Element comments_from_lms = new Element("comments_from_lms");
        comments_from_lms.setText("");
        _model.getRootElement().addContent(comments_from_lms);

        // next do the objectives. Note it will be the servers/sco
        // job to dynamically create values for objectives. We will put
        // a blank default in for now...

        // add objectives
        Element objectives = new Element("objectives");
        _model.getRootElement().addContent(objectives);

        // add children objectives
        Element childrenObjectives = new Element("_children");
        childrenObjectives.setText("id,score,status");
        objectives.addContent(childrenObjectives);

        // add children objectives _count (zero by default)
        Element childrenCount = new Element("_count");
        childrenCount.setText("0");
        objectives.addContent(childrenCount);

        // now do the student data
        Element student_data = new Element("student_data");
        _model.getRootElement().addContent(student_data);

        // add student data children
        Element childrenStudentdata = new Element("_children");
        childrenStudentdata.setText("mastery_score,max_time_allowed,time_limit_action");
        student_data.addContent(childrenStudentdata);

        // now do the mastery_score
        Element mastery_score = new Element("mastery_score");
        mastery_score.setText(_mastery_score);
        student_data.addContent(mastery_score);

        // now do the max_time_allowed
        Element max_time_allowed = new Element("max_time_allowed");
        max_time_allowed.setText(_max_time_allowed);
        student_data.addContent(max_time_allowed);

        // now do the time_limit_action
        Element time_limit_action = new Element("time_limit_action");
        // bug fix page 1.37 SCORM Addendums
        if (_time_limit_action != ""){
            time_limit_action.setText(_time_limit_action);
        }
        else{
            time_limit_action.setText("continue,no message");
        }
        student_data.addContent(time_limit_action);

        // next up, student_preference

        Element student_preference = new Element("student_preference");
        _model.getRootElement().addContent(student_preference);

        // add student_preference children
        Element childrenStudentpreference = new Element("_children");
        childrenStudentpreference.setText("audio,language,speed,text");
        student_preference.addContent(childrenStudentpreference);

        // add audio
        Element audio = new Element("audio");
        audio.setText("0");
        student_preference.addContent(audio);

        // add language
        Element language = new Element("language");
        language.setText("");
        student_preference.addContent(language);

        // add speed
        Element speed = new Element("speed");
        speed.setText("0");
        student_preference.addContent(speed);

        // add text
        Element text = new Element("text");
        text.setText("0");
        student_preference.addContent(text);

        /*
         * finally set up the default interactions - again this would be
         * populated by an sco, so we just put in the bare minimum...
         */

        // add interactions
        Element interactions = new Element("interactions");
        _model.getRootElement().addContent(interactions);

        // add children interactions
        Element childrenInteractions = new Element("_children");
        childrenInteractions.setText("id,objectives,time,type,correct_responses,weighting,student_response,result,latency");
        interactions.addContent(childrenInteractions);

        // add children interactions _count (zero by default)
        Element childrenInteractionsCount = new Element("_count");
        childrenInteractionsCount.setText("0");
        interactions.addContent(childrenInteractionsCount);

        for(int i = 0; i < scorm_comments.length; i++) {
             Comment comment = new Comment(scorm_comments[i]);
             _model.getContent().add(0, comment);
        }        
    }
}