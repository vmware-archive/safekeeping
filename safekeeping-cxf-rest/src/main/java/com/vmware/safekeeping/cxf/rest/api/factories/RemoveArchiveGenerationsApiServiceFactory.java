package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.RemoveArchiveGenerationsApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.RemoveArchiveGenerationsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class RemoveArchiveGenerationsApiServiceFactory {
    private final static RemoveArchiveGenerationsApiService service = new RemoveArchiveGenerationsApiServiceImpl();

    public static RemoveArchiveGenerationsApiService getRemoveArchiveGenerationsApi() {
        return service;
    }
}
