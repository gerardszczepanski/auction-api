package com.gszczepanski.auctionapi

import com.fasterxml.jackson.databind.ObjectMapper
import com.gszczepanski.auctionapi.AuctionApiApplication
import org.flywaydb.core.Flyway
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

@SpringBootTest(classes = [AuctionApiApplication.class])
@ActiveProfiles('integrationtest')
abstract class IntegrationBaseSpec extends Specification {

    @Autowired
    Flyway flyway

    @Autowired
    WebApplicationContext context

    @Autowired
    ObjectMapper objectMapper

    MockMvc mvc

    MvcWhen mvcWhen

    def setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context)
                .build()

        mvcWhen = MvcWhen.from(mvc, objectMapper)
    }

    void cleanUpDatabase() {
        flyway.clean()
        flyway.migrate()
    }

}
