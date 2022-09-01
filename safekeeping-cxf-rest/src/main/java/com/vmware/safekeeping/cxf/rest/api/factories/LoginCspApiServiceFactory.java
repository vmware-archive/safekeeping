package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.LoginCspApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.LoginCspApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class LoginCspApiServiceFactory {
    private final static LoginCspApiService service = new LoginCspApiServiceImpl();

    public static LoginCspApiService getLoginCspApi() {
        return service;
    }
}
