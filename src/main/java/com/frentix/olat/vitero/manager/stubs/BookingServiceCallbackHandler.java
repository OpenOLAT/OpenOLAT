
/**
 * BookingServiceCallbackHandler.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.4.1  Built on : Aug 13, 2008 (05:03:35 LKT)
 */

    package com.frentix.olat.vitero.manager.stubs;

    /**
     *  BookingServiceCallbackHandler Callback class, Users can extend this class and implement
     *  their own receiveResult and receiveError methods.
     */
    public abstract class BookingServiceCallbackHandler{



    protected Object clientData;

    /**
    * User can pass in any object that needs to be accessed once the NonBlocking
    * Web service call is finished and appropriate method of this CallBack is called.
    * @param clientData Object mechanism by which the user can pass in user data
    * that will be avilable at the time this callback is called.
    */
    public BookingServiceCallbackHandler(Object clientData){
        this.clientData = clientData;
    }

    /**
    * Please use this constructor if you don't want to set any clientData
    */
    public BookingServiceCallbackHandler(){
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
            * auto generated Axis2 call back method for getBookingListByGroupInFuture method
            * override this method for handling normal response from getBookingListByGroupInFuture operation
            */
           public void receiveResultgetBookingListByGroupInFuture(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.GetBookingListByGroupInFutureResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookingListByGroupInFuture operation
           */
            public void receiveErrorgetBookingListByGroupInFuture(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for createBooking method
            * override this method for handling normal response from createBooking operation
            */
           public void receiveResultcreateBooking(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.CreateBookingResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from createBooking operation
           */
            public void receiveErrorcreateBooking(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBookingListByDate method
            * override this method for handling normal response from getBookingListByDate operation
            */
           public void receiveResultgetBookingListByDate(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.GetBookingListByDateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookingListByDate operation
           */
            public void receiveErrorgetBookingListByDate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBookingListByUserAndDate method
            * override this method for handling normal response from getBookingListByUserAndDate operation
            */
           public void receiveResultgetBookingListByUserAndDate(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.GetBookingListByUserAndDateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookingListByUserAndDate operation
           */
            public void receiveErrorgetBookingListByUserAndDate(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBookingListByUserInFuture method
            * override this method for handling normal response from getBookingListByUserInFuture operation
            */
           public void receiveResultgetBookingListByUserInFuture(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.GetBookingListByUserInFutureResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookingListByUserInFuture operation
           */
            public void receiveErrorgetBookingListByUserInFuture(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBookingTimeById method
            * override this method for handling normal response from getBookingTimeById operation
            */
           public void receiveResultgetBookingTimeById(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.GetBookingTimeByIdResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookingTimeById operation
           */
            public void receiveErrorgetBookingTimeById(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBookingById method
            * override this method for handling normal response from getBookingById operation
            */
           public void receiveResultgetBookingById(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.GetBookingByIdResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookingById operation
           */
            public void receiveErrorgetBookingById(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for deleteBooking method
            * override this method for handling normal response from deleteBooking operation
            */
           public void receiveResultdeleteBooking(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.DeleteBookingResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from deleteBooking operation
           */
            public void receiveErrordeleteBooking(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBookingByBookingTimeId method
            * override this method for handling normal response from getBookingByBookingTimeId operation
            */
           public void receiveResultgetBookingByBookingTimeId(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.GetBookingByBookingTimeIdResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookingByBookingTimeId operation
           */
            public void receiveErrorgetBookingByBookingTimeId(java.lang.Exception e) {
            }
                
           /**
            * auto generated Axis2 call back method for getBookingListByGroupAndDate method
            * override this method for handling normal response from getBookingListByGroupAndDate operation
            */
           public void receiveResultgetBookingListByGroupAndDate(
                    com.frentix.olat.vitero.manager.stubs.BookingServiceStub.GetBookingListByGroupAndDateResponse result
                        ) {
           }

          /**
           * auto generated Axis2 Error handler
           * override this method for handling error response from getBookingListByGroupAndDate operation
           */
            public void receiveErrorgetBookingListByGroupAndDate(java.lang.Exception e) {
            }
                


    }
    