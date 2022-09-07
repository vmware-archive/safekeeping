package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.RemoveArchiveGenerationsApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.RemoveArchiveGenerationsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class RemoveArchiveGenerationsApiServiceFactory {
    private final static RemoveArchiveGenerationsApiService service = new RemoveArchiveGenerationsApiServiceImpl();

    public static RemoveArchiveGenerationsApiService getRemoveArchiveGenerationsApi() {
        return service;
    }
}
