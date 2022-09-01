package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.RetrieveDumpsApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.RetrieveDumpsApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class RetrieveDumpsApiServiceFactory {
    private final static RetrieveDumpsApiService service = new RetrieveDumpsApiServiceImpl();

    public static RetrieveDumpsApiService getRetrieveDumpsApi() {
        return service;
    }
}
