package com.vmware.safekeeping.cxf.rest;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.SecurityContext;

import org.glassfish.jersey.server.ContainerRequest;

import com.vmware.safekeeping.cxf.rest.support.User;

public class GlobalState {
    static private Map<String, User> usersList = new HashMap<>();

    static public Map<String, User> getUsersList() {
	return usersList;
    }

    public static Object getDeclaredField(String fieldname, Object o)
	    throws IllegalArgumentException, IllegalAccessException {
	Object result = null;
	boolean found = false;
	// Search this and all superclasses:
	for (Class<?> clas = o.getClass(); clas != null; clas = clas.getSuperclass()) {
	    if (found) {
		break;
	    }
	    // Find the correct field:
	    for (Field method : clas.getDeclaredFields()) {
		if (found) {
		    break;
		}
		// Field found:
		if ((method.getName() == fieldname)) {
		    method.setAccessible(true);
		    result = method.get(o);
		    found = true;
		}
	    }

	}
	return result;
    }

    public static Object getDeclaredMethod(String name, Object o)
	    throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
	Object result = null;
	boolean found = false;
	// Search this and all superclasses:
	for (Class<?> clas = o.getClass(); clas != null; clas = clas.getSuperclass()) {
	    if (found) {
		break;
	    }

	    // Find the correct method:
	    for (Method method : clas.getDeclaredMethods()) {
		if (found) {
		    break;
		}
		// Method found:
		if ((method.getName().startsWith("get")) && (method.getName().length() == (name.length() + 3))) {
		    if (method.getName().toLowerCase().endsWith(name.toLowerCase())) {
			result = method.invoke(o);

			found = true;
		    }
		}
	    }
	}
	return result;
    }

    static public User precheck(SecurityContext securityContext) {
	// org.glassfish.jersey.server.internal.process.SecurityContextInjectee
	try {
	    ContainerRequest cr = (ContainerRequest) getDeclaredField("requestContext", securityContext);
	    final String id =cr.getHeaderString("api_key"); 
	    final User result = getUsersList().get(id);
	    result.setLastOperation((new Date()).getTime());
	    return result;
	} catch (IllegalArgumentException | IllegalAccessException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	return null;
    }
}
