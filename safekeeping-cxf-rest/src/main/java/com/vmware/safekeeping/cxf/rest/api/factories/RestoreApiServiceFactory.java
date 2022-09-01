package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.RestoreApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.RestoreApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class RestoreApiServiceFactory {
    private final static RestoreApiService service = new RestoreApiServiceImpl();

    public static RestoreApiService getRestoreApi() {
        return service;
    }
}
