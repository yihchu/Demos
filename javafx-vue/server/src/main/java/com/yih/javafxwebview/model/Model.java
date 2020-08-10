package com.yih.javafxwebview.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel
@Data
public class Model {

    @ApiModelProperty(notes = "Model.Name")
    private String name;
}
