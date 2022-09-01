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
import com.vmware.safekeeping.cxf.rest.model.SearchManagementEntityInfoType;
import io.swagger.v3.oas.annotations.media.Schema;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * SearchManagementEntity
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-08-31T19:06:45.540Z[GMT]")public class SearchManagementEntity   {
  @JsonProperty("SearchType")
  private SearchManagementEntityInfoType searchType = null;

  @JsonProperty("SearchValue")
  private String searchValue = null;

  public SearchManagementEntity searchType(SearchManagementEntityInfoType searchType) {
    this.searchType = searchType;
    return this;
  }

  /**
   * Get searchType
   * @return searchType
   **/
  @JsonProperty("SearchType")
  @Schema(description = "")
  @Valid
  public SearchManagementEntityInfoType getSearchType() {
    return searchType;
  }

  public void setSearchType(SearchManagementEntityInfoType searchType) {
    this.searchType = searchType;
  }

  public SearchManagementEntity searchValue(String searchValue) {
    this.searchValue = searchValue;
    return this;
  }

  /**
   * Get searchValue
   * @return searchValue
   **/
  @JsonProperty("SearchValue")
  @Schema(description = "")
  public String getSearchValue() {
    return searchValue;
  }

  public void setSearchValue(String searchValue) {
    this.searchValue = searchValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchManagementEntity searchManagementEntity = (SearchManagementEntity) o;
    return Objects.equals(this.searchType, searchManagementEntity.searchType) &&
        Objects.equals(this.searchValue, searchManagementEntity.searchValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(searchType, searchValue);
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchManagementEntity {\n");
    
    sb.append("    searchType: ").append(toIndentedString(searchType)).append("\n");
    sb.append("    searchValue: ").append(toIndentedString(searchValue)).append("\n");
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