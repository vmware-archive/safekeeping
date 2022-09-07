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
import com.vmware.safekeeping.cxf.rest.model.AbstractResultActionArchive;
import com.vmware.safekeeping.cxf.rest.model.ResultActionArchiveItem;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;
import javax.validation.Valid;

/**
 * ResultActionArchiveItemsList
 */
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.JavaJerseyServerCodegen", date = "2022-09-06T23:20:18.330Z[GMT]")public class ResultActionArchiveItemsList extends AbstractResultActionArchive implements OneOfTaskResultResult  {
  @JsonProperty("Items")
  private List<ResultActionArchiveItem> items = null;

  public ResultActionArchiveItemsList items(List<ResultActionArchiveItem> items) {
    this.items = items;
    return this;
  }

  public ResultActionArchiveItemsList addItemsItem(ResultActionArchiveItem itemsItem) {
    if (this.items == null) {
      this.items = new ArrayList<ResultActionArchiveItem>();
    }
    this.items.add(itemsItem);
    return this;
  }

  /**
   * Get items
   * @return items
   **/
  @JsonProperty("Items")
  @Schema(description = "")
  @Valid
  public List<ResultActionArchiveItem> getItems() {
    return items;
  }

  public void setItems(List<ResultActionArchiveItem> items) {
    this.items = items;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResultActionArchiveItemsList resultActionArchiveItemsList = (ResultActionArchiveItemsList) o;
    return Objects.equals(this.items, resultActionArchiveItemsList.items) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(items, super.hashCode());
  }


  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResultActionArchiveItemsList {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    items: ").append(toIndentedString(items)).append("\n");
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
