package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.TaskInfoApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.TaskInfoApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class TaskInfoApiServiceFactory {
    private final static TaskInfoApiService service = new TaskInfoApiServiceImpl();

    public static TaskInfoApiService getTaskInfoApi() {
        return service;
    }
}
