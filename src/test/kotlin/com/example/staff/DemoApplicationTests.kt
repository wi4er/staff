package com.example.staff

import org.jetbrains.exposed.sql.Database
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get

@SpringBootTest
@AutoConfigureMockMvc
class DemoApplicationTests {

	@Autowired
	private val mockMvc: MockMvc? = null

	companion object {
		@BeforeAll
		@JvmStatic
		fun before() {
			Database.connect(
				"jdbc:postgresql://localhost:5432/postgres",
				driver = "org.postgresql.Driver",
				user = "postgres",
				password = "example"
			)
		}
	}


	@Test
	fun contextLoads() {
		mockMvc
			?.perform(get("/"))
			?.andExpect {
				println(it.response.contentAsString)
			}
	}

}
