package com.vmware.safekeeping.cxf.rest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.vmware.safekeeping.cxf.rest.support.User; 

public class GlobalState {
  static  private Map<String, User> usersList=new HashMap<String, User>();

  static public  Map<String, User> getUsersList() {
 	return  usersList;
 }
    
  static public User precheck()  {
//		if (this.wsContext == null) {
//			throw new UnrecognizedToken("wsContext is null");
//		}
		final String id ="";// check(this.wsContext.getMessageContext());
		final User result = getUsersList().get(id);
		result.setLastOperation((new Date()).getTime());
		return result;
	}
    
}
