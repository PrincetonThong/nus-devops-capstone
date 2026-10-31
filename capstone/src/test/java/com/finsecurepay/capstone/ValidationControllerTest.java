
package com.finsecurepay.capstone;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ValidationControllerTest {

    @Autowired
    private MockMvc testMVC;

    @Test
    void testProbeHealthEndpoint() throws Exception {
        // /health endpt returns { "status": "up", "service": "finsecurepay", "version": "local" }
        testMVC.perform(get("/health"))
            .andExpect( status().isOk() )
            .andExpect( jsonPath("$.status").value("up") )
            .andExpect( jsonPath("$.service").value("finsecurepay") );
    }

    @Test
    void testValidTransactionEndpoint() throws Exception {

        String payloadJson = """
        {
            "transactionId": "TX-1",
            "amount": 150.50,
            "currency": "SGD",
            "sourceAccount": "1001",
            "destinationAccount": "1002"
        }
        """;

        testMVC.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(payloadJson))
            .andExpect( status().isOk() )
            .andExpect( jsonPath("$.valid").value(true) )
            .andExpect( jsonPath("$.status").value("ACCEPTED") )
            .andExpect( jsonPath("$.reasons").isEmpty() );
    }

    @Test
    void testInvalidTransactionEndpoint() throws Exception {

        String payloadJson = """
        {
            "transactionId": "TX-2",
            "amount": -150.50,
            "currency": "XYZ",
            "sourceAccount": "1001",
            "destinationAccount": "1001"
        }
        """;

        testMVC.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(payloadJson))
            .andExpect( status().isUnprocessableEntity() )
            .andExpect( jsonPath("$.valid").value(false) )
            .andExpect( jsonPath("$.status").value("REJECTED") )
            .andExpect( jsonPath("$.reasons.length()").value(3) );
    }

    @Test
    void testMissingSourceAccountEndpoint() throws Exception {

        String payloadJson = """
        {
            "transactionId": "TX-3",
            "amount": 150.50,
            "currency": "SGD",
            "soruceAccount": "1001",
            "destinationAccount": "1002"
        }
        """;

        testMVC.perform(post("/validate").contentType(MediaType.APPLICATION_JSON).content(payloadJson))
            .andExpect( status().isUnprocessableEntity() )
            .andExpect( jsonPath("$.valid").value(false) )
            .andExpect( jsonPath("$.reasons[0]").value("Source account is required") );
    }
}
