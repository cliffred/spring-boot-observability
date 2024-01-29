# Spring Boot with Observability

[![CI workflow](https://github.com/cliffred/spring-boot-observability/actions/workflows/ci.yml/badge.svg)](https://github.com/cliffred/spring-boot-observability/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=cliffred_spring-boot-observability&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=cliffred_spring-boot-observability)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=cliffred_spring-boot-observability&metric=coverage)](https://sonarcloud.io/summary/new_code?id=cliffred_spring-boot-observability)
[![Latest Image](https://ghcr-badge.deta.dev/cliffred/spring-boot-observability/latest_tag?color=%2344cc11&ignore=latest&label=version&trim=)](https://github.com/cliffred/spring-boot-observability/pkgs/container/spring-boot-observability)

Sample project for implementing a complete observability stack using Loki, Tempo, Prometheus and Grafana in Spring boot.

Largely based on https://github.com/blueswen/spring-boot-observability

## Quick start

- Start the application by running  
  `./gradlew bootRun` or run the application from within your IDE
- Find the swagger-ui at http://localhost:8080
- Generate some observability data by running the k6 load test  
  `k6 run k6/script.js`
- View observability data in Grafana  
  goto http://localhost:3000  
  log in with `admin` / `admin` and open the **Spring Boot Observability** dashboard
