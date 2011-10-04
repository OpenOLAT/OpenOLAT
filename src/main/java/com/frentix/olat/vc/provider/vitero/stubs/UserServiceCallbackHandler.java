
/**
 * UserServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

    package com.frentix.olat.vc.provider.vitero.stubs;

    /**
     *  UserServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class UserServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public UserServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public UserServiceCallbackHandler(){
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
            * auto generated Axis2 call back method for getUserList method
            * override this method for handling normal response from getUserList operation
            */
           public void receiveResultgetUserList(
                    com.frentix.olat.vc.provider.vitero.stubs.UserServiceStub.GetUserListResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserList operation
           */
            public void receiveErrorgetUserList(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserListByGroup method
            * override this method for handling normal response from getUserListByGroup operation
            */
           public void receiveResultgetUserListByGroup(
                    com.frentix.olat.vc.provider.vitero.stubs.UserServiceStub.GetUserListByGroupResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserListByGroup operation
           */
            public void receiveErrorgetUserListByGroup(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserListByPosition method
            * override this method for handling normal response from getUserListByPosition operation
            */
           public void receiveResultgetUserListByPosition(
                    com.frentix.olat.vc.provider.vitero.stubs.UserServiceStub.GetUserListByPositionResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserListByPosition operation
           */
            public void receiveErrorgetUserListByPosition(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                
               // No methods generated for meps other than in-out
                
           /**
            * auto generated Axis2 call back method for createUser method
            * override this method for handling normal response from createUser operation
            */
           public void receiveResultcreateUser(
                    com.frentix.olat.vc.provider.vitero.stubs.UserServiceStub.CreateUserResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createUser operation
           */
            public void receiveErrorcreateUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUser method
            * override this method for handling normal response from getUser operation
            */
           public void receiveResultgetUser(
                    com.frentix.olat.vc.provider.vitero.stubs.UserServiceStub.GetUserResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUser operation
           */
            public void receiveErrorgetUser(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserCount method
            * override this method for handling normal response from getUserCount operation
            */
           public void receiveResultgetUserCount(
                    com.frentix.olat.vc.provider.vitero.stubs.UserServiceStub.GetUserCountResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserCount operation
           */
            public void receiveErrorgetUserCount(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getUserListByCustomer method
            * override this method for handling normal response from getUserListByCustomer operation
            */
           public void receiveResultgetUserListByCustomer(
                    com.frentix.olat.vc.provider.vitero.stubs.UserServiceStub.GetUserListByCustomerResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getUserListByCustomer operation
           */
            public void receiveErrorgetUserListByCustomer(java.lang.Exception e) {
            }
                
               // No methods generated for meps other than in-out
                


    }
    