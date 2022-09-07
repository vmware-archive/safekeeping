package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.StatusArchiveApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.StatusArchiveApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class StatusArchiveApiServiceFactory {
    private final static StatusArchiveApiService service = new StatusArchiveApiServiceImpl();

    public static StatusArchiveApiService getStatusArchiveApi() {
        return service;
    }
}
