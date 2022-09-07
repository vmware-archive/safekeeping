/*
 * Safekeeping Server
 * Safekeeping OpenAPI
 *
 * OpenAPI spec version: 1.0.0
 * 
 *
 * NOTE: This class is auto generated by the swagger code generator program.
 * https://github.com/swagger-api/swagger-codegen.git
 * Do not edit the class manually.
 */

package com.vmware.safekeeping.cxf.rest.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.vmware.safekeeping.cxf.rest.model.AbstractResultActionConnectRepository;
import com.vmware.safekeeping.cxf.rest.model.AwsS3RepositoryOptions;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionConnectAwsS3Repository
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class ResultActionConnectAwsS3Repository extends AbstractResultActionConnectRepository implements OneOfTaskResultResult  {
  @JsonProperty("Options")
  private AwsS3RepositoryOptions options = null;

  public ResultActionConnectAwsS3Repository options(AwsS3RepositoryOptions options) {
    this.options = options;
    return this;
  }

  /**
   * Get options
   * @return options
   **/
  @JsonProperty("Options")
  @Schema(description = "")
  @Valid
  public AwsS3RepositoryOptions getOptions() {
    return options;
  }

  public void setOptions(AwsS3RepositoryOptions options) {
    this.options = options;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionConnectAwsS3Repository resultActionConnectAwsS3Repository = (ResultActionConnectAwsS3Repository) o;
    return Objects.equals(this.options, resultActionConnectAwsS3Repository.options) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(options, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionConnectAwsS3Repository {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    options: ").append(toIndentedString(options)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
