
/**
 * SessionCodeServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

    package org.olat.modules.vitero.manager.stubs;

    /**
     *  SessionCodeServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class SessionCodeServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public SessionCodeServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public SessionCodeServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
           /**
            * auto generated Axis2 call back method for createPersonalGroupSessionCode method
            * override this method for handling normal response from createPersonalGroupSessionCode operation
            */
           public void receiveResultcreatePersonalGroupSessionCode(
                    org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.CreatePersonalGroupSessionCodeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createPersonalGroupSessionCode operation
           */
            public void receiveErrorcreatePersonalGroupSessionCode(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getSessionCodeInformation method
            * override this method for handling normal response from getSessionCodeInformation operation
            */
           public void receiveResultgetSessionCodeInformation(
                    org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.GetSessionCodeInformationResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getSessionCodeInformation operation
           */
            public void receiveErrorgetSessionCodeInformation(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for createBookingSessionCode method
            * override this method for handling normal response from createBookingSessionCode operation
            */
           public void receiveResultcreateBookingSessionCode(
                    org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.CreateBookingSessionCodeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createBookingSessionCode operation
           */
            public void receiveErrorcreateBookingSessionCode(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getTestroomSessionCode method
            * override this method for handling normal response from getTestroomSessionCode operation
            */
           public void receiveResultgetTestroomSessionCode(
                    org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.GetTestroomSessionCodeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getTestroomSessionCode operation
           */
            public void receiveErrorgetTestroomSessionCode(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getPersonalGroupSessionCodes method
            * override this method for handling normal response from getPersonalGroupSessionCodes operation
            */
           public void receiveResultgetPersonalGroupSessionCodes(
                    org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.GetPersonalGroupSessionCodesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPersonalGroupSessionCodes operation
           */
            public void receiveErrorgetPersonalGroupSessionCodes(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for createPersonalBookingSessionCode method
            * override this method for handling normal response from createPersonalBookingSessionCode operation
            */
           public void receiveResultcreatePersonalBookingSessionCode(
                    org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.CreatePersonalBookingSessionCodeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createPersonalBookingSessionCode operation
           */
            public void receiveErrorcreatePersonalBookingSessionCode(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for createVmsSessionCode method
            * override this method for handling normal response from createVmsSessionCode operation
            */
           public void receiveResultcreateVmsSessionCode(
                    org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.CreateVmsSessionCodeResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createVmsSessionCode operation
           */
            public void receiveErrorcreateVmsSessionCode(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getPersonalBookingSessionCodes method
            * override this method for handling normal response from getPersonalBookingSessionCodes operation
            */
           public void receiveResultgetPersonalBookingSessionCodes(
                    org.olat.modules.vitero.manager.stubs.SessionCodeServiceStub.GetPersonalBookingSessionCodesResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getPersonalBookingSessionCodes operation
           */
            public void receiveErrorgetPersonalBookingSessionCodes(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    