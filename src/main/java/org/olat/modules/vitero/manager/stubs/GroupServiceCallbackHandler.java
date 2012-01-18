
/**
 * GroupServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

    package org.olat.modules.vitero.manager.stubs;

    /**
     *  GroupServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class GroupServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public GroupServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public GroupServiceCallbackHandler(){
        this.clientData = null;
    }

    /**
     * Get the client data
     */

     public Object getClientData() {
        return clientData;
     }

        
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getGroupListByCustomer method
            * override this method for handling normal response from getGroupListByCustomer operation
            */
           public void receiveResultgetGroupListByCustomer(
                    org.olat.modules.vitero.manager.stubs.GroupServiceStub.GetGroupListByCustomerResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getGroupListByCustomer operation
           */
            public void receiveErrorgetGroupListByCustomer(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getGroupByName method
            * override this method for handling normal response from getGroupByName operation
            */
           public void receiveResultgetGroupByName(
                    org.olat.modules.vitero.manager.stubs.GroupServiceStub.GetGroupByNameResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getGroupByName operation
           */
            public void receiveErrorgetGroupByName(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for getGroup method
            * override this method for handling normal response from getGroup operation
            */
           public void receiveResultgetGroup(
                    org.olat.modules.vitero.manager.stubs.GroupServiceStub.GetGroupResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getGroup operation
           */
            public void receiveErrorgetGroup(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for createGroup method
            * override this method for handling normal response from createGroup operation
            */
           public void receiveResultcreateGroup(
                    org.olat.modules.vitero.manager.stubs.GroupServiceStub.CreateGroupResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createGroup operation
           */
            public void receiveErrorcreateGroup(java.lang.Exception e) {
            }
                


    }
    