package com.gszczepanski.auctionapi

import com.fasterxml.jackson.databind.ObjectMapper
import groovy.util.logging.Slf4j
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActions

import static com.google.common.base.Preconditions.checkArgument
import static java.lang.String.format
import static java.util.Objects.nonNull
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@Slf4j
class MvcWhen {

    private final MockMvc mvc
    private final ObjectMapper objectMapper

    private MvcWhen(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mvc = mockMvc
        this.objectMapper = objectMapper
    }

    static MvcWhen from(MockMvc mockMvc, ObjectMapper objectMapper) {
        checkArgument(nonNull(mockMvc), "mockMvc is null")
        checkArgument(nonNull(objectMapper), "objectMapper is null")

        return new MvcWhen(mockMvc, objectMapper)
    }

    ResultActions sendGet(String url, Object... urlReplacementArgs) throws Exception {

        return mvc.perform(get(format(url, urlReplacementArgs))
                .accept(APPLICATION_JSON_UTF8)
        )
    }

}
