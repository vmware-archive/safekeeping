package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.LogoutApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.LogoutApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class LogoutApiServiceFactory {
    private final static LogoutApiService service = new LogoutApiServiceImpl();

    public static LogoutApiService getLogoutApi() {
        return service;
    }
}
