package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.RemoveArchiveProfileApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.RemoveArchiveProfileApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class RemoveArchiveProfileApiServiceFactory {
    private final static RemoveArchiveProfileApiService service = new RemoveArchiveProfileApiServiceImpl();

    public static RemoveArchiveProfileApiService getRemoveArchiveProfileApi() {
        return service;
    }
}
