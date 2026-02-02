package com.innowise.authservice;

import com.innowise.authservice.config.TestContainersConfig;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

  @DynamicPropertySource
  static void registerDynamicProperties(DynamicPropertyRegistry registry) {
    System.out.println("Postgres JDBC URL: " + TestContainersConfig.POSTGRES.getJdbcUrl());

    registry.add("spring.datasource.url", TestContainersConfig.POSTGRES::getJdbcUrl);
    registry.add("spring.datasource.username", TestContainersConfig.POSTGRES::getUsername);
    registry.add("spring.datasource.password", TestContainersConfig.POSTGRES::getPassword);
  }
}