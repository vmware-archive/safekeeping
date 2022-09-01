package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.CheckArchiveGenerationsApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.CheckArchiveGenerationsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class CheckArchiveGenerationsApiServiceFactory {
    private final static CheckArchiveGenerationsApiService service = new CheckArchiveGenerationsApiServiceImpl();

    public static CheckArchiveGenerationsApiService getCheckArchiveGenerationsApi() {
        return service;
    }
}
