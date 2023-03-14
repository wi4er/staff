package com.example.staff

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@SpringBootTest
@AutoConfigureMockMvc
class StaffApplicationTests {
    @Autowired
    private val mockMvc: MockMvc? = null

    @Test
    fun contextLoads() {
        mockMvc
            ?.perform(get("/"))
            ?.andExpect {
                println(it.response.contentAsString)
            }
    }
}
