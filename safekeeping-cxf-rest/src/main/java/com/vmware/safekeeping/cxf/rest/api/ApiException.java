package com.vmware.safekeeping.cxf.rest.api;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")
public class ApiException extends Exception {
    private int code;

    public ApiException(int code, String msg) {
	super(msg);
	this.code = code;
    }
}
