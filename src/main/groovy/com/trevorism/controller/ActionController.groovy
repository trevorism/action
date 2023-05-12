package com.trevorism.controller

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.trevorism.data.FastDatastoreRepository
import com.trevorism.data.Repository
import com.trevorism.data.model.filtering.FilterBuilder
import com.trevorism.data.model.filtering.SimpleFilter
import com.trevorism.https.DefaultSecureHttpClient
import com.trevorism.https.SecureHttpClient
import com.trevorism.model.NamedAction
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.Put
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import com.trevorism.secure.Secure
import com.trevorism.secure.Roles

@Controller("action")
class ActionController {

    private Repository<NamedAction> repository = new FastDatastoreRepository<>(NamedAction)
    private SecureHttpClient secureHttpClient = new DefaultSecureHttpClient()
    private Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").create()

    @Tag(name = "Action Operations")
    @Operation(summary = "Get a list of all NamedActions **Secure")
    @Get(value = "/", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    List<NamedAction> list() {
        repository.list()
    }

    @Tag(name = "Action Operations")
    @Operation(summary = "Get a NamedAction by Id **Secure")
    @Get(value = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    NamedAction get(String id) {
        repository.get(id)
    }

    @Tag(name = "Action Operations")
    @Operation(summary = "Create a NamedAction **Secure")
    @Post(value = "/", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    NamedAction create(@Body NamedAction action) {
        repository.create(action)
    }

    @Tag(name = "Action Operations")
    @Operation(summary = "Update a NamedAction **Secure")
    @Put(value = "/{id}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    NamedAction update(String id, @Body NamedAction action) {
        repository.update(id, action)
    }

    @Tag(name = "Action Operations")
    @Operation(summary = "Delete a NamedAction **Secure")
    @Delete(value = "/{id}", produces = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    NamedAction delete(String id) {
        repository.delete(id)
    }

    @Tag(name = "Action Operations")
    @Operation(summary = "Invoke an action **Secure")
    @Post(value = "/{name}", produces = MediaType.APPLICATION_JSON, consumes = MediaType.APPLICATION_JSON)
    @Secure(Roles.USER)
    String invoke(String name, @Body Map<String, String> override) {
        NamedAction action = getByName(name)
        if (!action)
            throw new RuntimeException("Action not found: $name")

        String request = override.size() == 0 ? action.requestJson : gson.toJson(override)
        String correlationId = UUID.randomUUID().toString()

        switch(action.httpMethod.toUpperCase()){
            case "GET":
                return secureHttpClient.get(action.endpoint, correlationId)
            case "POST":
                return secureHttpClient.post(action.endpoint, request, correlationId)
            case "PUT":
                return secureHttpClient.put(action.endpoint, request, correlationId)
            case "PATCH":
                return secureHttpClient.patch(action.endpoint, correlationId)
            case "DELETE":
                return secureHttpClient.delete(action.endpoint, correlationId)
            default:
                throw new RuntimeException("Unsupported HTTP Method: ${action.httpMethod}")
        }
    }

    private NamedAction getByName(String name) {
        def task = repository.filter(new FilterBuilder().addFilter(new SimpleFilter("name", "=", name.toLowerCase())).build())
        if (!task)
            return null
        return task[0]
    }
}
