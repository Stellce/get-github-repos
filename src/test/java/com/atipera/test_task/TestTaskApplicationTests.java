package com.atipera.test_task;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWebTestClient
class TestTaskApplicationTests {

	@Autowired
	private WebTestClient webTestClient;

	private WireMockServer wireMockServer;

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("github.api.url", () -> "http://localhost:8080");
	}

	@BeforeAll
	void setup() {
		wireMockServer = new WireMockServer();
		wireMockServer.start();
		WireMock.configureFor("localhost", 8080);
	}

	@AfterAll
	void tearDown() {
		wireMockServer.stop();
	}

	@Test
	void shouldReturnUserReposWithBranches_whenUserExists() {
		stubFor(get(urlEqualTo("/users/testUser/repos"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody("""
					    [
					        {"name": "repo1", "owner": {"login": "testUser"}, "fork": false},
					        {"name": "repo2", "owner": {"login": "testUser"}, "fork": true}
					    ]
					""")
			));

		stubFor(get(urlEqualTo("/repos/testUser/repo1/branches"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody("""
					    [
					        {"name": "main", "commit": {"sha": "sha123"}},
					        {"name": "dev", "commit": {"sha": "sha456"}}
					    ]
					""")
			));

		stubFor(get(urlEqualTo("/repos/testUser/repo2/branches"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody("""
					    [
					        {"name": "main", "commit": {"sha": "sha123"}},
					        {"name": "dev", "commit": {"sha": "sha456"}}
					    ]
					""")
			));

		webTestClient.get()
			.uri("/repos?userId=testUser")
			.exchange()
			.expectStatus().isOk()
			.expectBody()
			.jsonPath("$.size()").isEqualTo(1)  // repo2 było forkiem, więc go nie ma
			.jsonPath("$[0].repositoryName").isEqualTo("repo1")
			.jsonPath("$[0].branches.size()").isEqualTo(2);
	}

	@Test
	void shouldReturn404_whenUserNotFound() {
		stubFor(get(urlEqualTo("/users/wrongUser/repos"))
			.willReturn(aResponse()
				.withHeader("Content-Type", "application/json")
				.withBody("""
					    {"status": 404, "message": "Not Found"}
					""")
				.withStatus(404)
			));

		webTestClient.get()
			.uri("/repos?userId=wrongUser")
			.exchange()
			.expectStatus().isNotFound()
			.expectBody()
			.jsonPath("$.status").isEqualTo(404)
			.jsonPath("$.message").isEqualTo("User does not exist");
	}
}
