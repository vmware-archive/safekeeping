package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.CheckArchiveGenerationsApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.CheckArchiveGenerationsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T22:00:47.492Z[GMT]")public class CheckArchiveGenerationsApiServiceFactory {
    private final static CheckArchiveGenerationsApiService service = new CheckArchiveGenerationsApiServiceImpl();

    public static CheckArchiveGenerationsApiService getCheckArchiveGenerationsApi() {
        return service;
    }
}
