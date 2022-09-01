package com.vmware.safekeeping.cxf.rest.api.impl;

import com.vmware.safekeeping.cxf.rest.api.*;
import com.vmware.safekeeping.cxf.rest.model.*;

import com.vmware.safekeeping.cxf.rest.model.ResultActionDisconnectSso;

import java.util.Map;
import java.util.List;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.validation.constraints.*;
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class LogoutApiServiceImpl extends LogoutApiService {
    @Override
    public Response logout(SecurityContext securityContext) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
