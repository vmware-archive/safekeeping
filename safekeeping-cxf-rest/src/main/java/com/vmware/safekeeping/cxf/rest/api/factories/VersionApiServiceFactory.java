package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.VersionApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.VersionApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class VersionApiServiceFactory {
    private final static VersionApiService service = new VersionApiServiceImpl();

    public static VersionApiService getVersionApi() {
        return service;
    }
}
