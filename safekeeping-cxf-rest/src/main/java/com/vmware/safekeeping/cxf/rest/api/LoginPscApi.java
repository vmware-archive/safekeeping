package com.vmware.safekeeping.cxf.rest.api;

import com.vmware.safekeeping.cxf.rest.model.*;
import com.vmware.safekeeping.cxf.rest.api.LoginPscApiService;
import com.vmware.safekeeping.cxf.rest.api.factories.LoginPscApiServiceFactory;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import com.vmware.safekeeping.cxf.rest.model.PscConnectOptions;
import com.vmware.safekeeping.cxf.rest.model.ResultActionConnectSso;

import java.util.Map;
import java.util.List;
import com.vmware.safekeeping.cxf.rest.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.servlet.ServletConfig;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;


@Path("/loginPsc")


@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class LoginPscApi  {
   private final LoginPscApiService delegate;

   public LoginPscApi(@Context ServletConfig servletContext) {
      LoginPscApiService delegate = null;

      if (servletContext != null) {
         String implClass = servletContext.getInitParameter("LoginPscApi.implementation");
         if (implClass != null && !"".equals(implClass.trim())) {
            try {
               delegate = (LoginPscApiService) Class.forName(implClass).newInstance();
            } catch (Exception e) {
               throw new RuntimeException(e);
            }
         } 
      }

      if (delegate == null) {
         delegate = LoginPscApiServiceFactory.getLoginPscApi();
      }

      this.delegate = delegate;
   }

    @POST
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @Operation(summary = "define the login to the SSO", description = "define the login to the SSO   ", security = {
        @SecurityRequirement(name = "api_key")    }, tags={ "connectivity" })
    @ApiResponses(value = { 
        @ApiResponse(responseCode = "200", description = "Return the connection task", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResultActionConnectSso.class)))),
        
        @ApiResponse(responseCode = "400", description = "Bad request. The server could not understand the request."),
        
        @ApiResponse(responseCode = "401", description = "Unauthorized. The client has not authenticated."),
        
        @ApiResponse(responseCode = "403", description = "Forbidden. The client is not authorized."),
        
        @ApiResponse(responseCode = "404", description = "Not found. The server cannot find the specified resource."),
        
        @ApiResponse(responseCode = "429", description = "The user has sent too many requests."),
        
        @ApiResponse(responseCode = "500", description = "An unexpected error has occurred while processing the request.") })
    public Response loginPsc(@Parameter(in = ParameterIn.DEFAULT, description = "Connection options" ,required=true) PscConnectOptions body

,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.loginPsc(body,securityContext);
    }
}
