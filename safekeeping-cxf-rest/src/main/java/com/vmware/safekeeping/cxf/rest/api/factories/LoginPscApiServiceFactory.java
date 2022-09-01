package com.vmware.safekeeping.cxf.rest.api.factories;

import com.vmware.safekeeping.cxf.rest.api.LoginPscApiService;
import com.vmware.safekeeping.cxf.rest.api.impl.LoginPscApiServiceImpl;

@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class LoginPscApiServiceFactory {
    private final static LoginPscApiService service = new LoginPscApiServiceImpl();

    public static LoginPscApiService getLoginPscApi() {
        return service;
    }
}
