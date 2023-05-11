package com.trevorism.model

import io.swagger.v3.oas.annotations.media.Schema

class NamedAction {

    @Schema(description = "Action id")
    String id
    @Schema(description = "Name of the action")
    String name
    @Schema(description = "Endpoint for the task")
    String endpoint
    @Schema(description = "HTTP Method for the task", allowableValues = "get,post,put,patch,delete")
    String httpMethod = "post"
    @Schema(description = "For POST, PUT, and PATCH the request body", allowableValues = "get,post,put,patch,delete")
    String requestJson
}
