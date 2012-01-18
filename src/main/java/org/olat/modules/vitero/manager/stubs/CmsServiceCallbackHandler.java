
/**
 * CmsServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

    package org.olat.modules.vitero.manager.stubs;

    /**
     *  CmsServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class CmsServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public CmsServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public CmsServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getCustomerFolder method
            * override this method for handling normal response from getCustomerFolder operation
            */
           public void receiveResultgetCustomerFolder(
                    org.olat.modules.vitero.manager.stubs.CmsServiceStub.GetCustomerFolderResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getCustomerFolder operation
           */
            public void receiveErrorgetCustomerFolder(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getGroupFolder method
            * override this method for handling normal response from getGroupFolder operation
            */
           public void receiveResultgetGroupFolder(
                    org.olat.modules.vitero.manager.stubs.CmsServiceStub.GetGroupFolderResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getGroupFolder operation
           */
            public void receiveErrorgetGroupFolder(java.lang.Exception e) {
            }
                


    }
    