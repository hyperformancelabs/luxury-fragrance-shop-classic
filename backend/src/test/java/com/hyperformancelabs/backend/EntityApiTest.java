package com.hyperformancelabs.backend;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class EntityApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testEmployeeApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/employee"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("Employee API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testBrandApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/brand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("Brand API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testProductApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/product"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("Product API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testCustomerApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("Customer API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testOrderApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("Order API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testCartApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("Cart API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testCartItemApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/cartitem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("CartItem API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testOrderItemApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/orderitem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("OrderItem API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testMaterialApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/material"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("Material API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testConversationApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/conversation"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("Conversation API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testChatMessageApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/chatmessage"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("ChatMessage API Test Result: " + result.getResponse().getContentAsString());
    }

    @Test
    public void testAllEntitiesApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/test/entities/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andReturn();
        
        System.out.println("All Entities API Test Result: " + result.getResponse().getContentAsString());
    }
}
