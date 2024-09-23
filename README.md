# Scraper Project

This project is a web scraper that can parse data from websites returning HTML and JSON data. It is designed to be **scalable**, **extendable**, and **efficient**, handling asynchronous scraping tasks with built-in rate limiting. The project is unit-tested using **JUnit** and integrates with **GitHub Actions** for continuous integration.

## Table of Contents

- [Features](#features)
- [Technologies Used](#technologies-used)
- [Project Structure](#project-structure)
- [Setup Instructions](#setup-instructions)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Continuous Integration (CI)](#continuous-integration-ci)
- [Extending the Scraper](#extending-the-scraper)

---

## Features

- **Asynchronous Scraping**: Handles multiple URLs concurrently using `ExecutorService`.
- **Rate Limiting**: Prevents overwhelming servers by applying a configurable rate limit per domain for scraping requests.
- **Supports Multiple Response Types**: Scrapes both HTML and JSON responses, with easy extendability for new content types.
- **Observer Pattern**: Integrated logging using observers to track when scraping starts, succeeds, or fails.
- **Factory Pattern**: A `ResponseHandlerFactory` dynamically selects the appropriate handler for different content types (e.g., HTML, JSON).
- **Singleton Pattern**: `WebScraper` will be a singleton class with given RateLimiter config
- **Queued Request**: To handle millions of web scraping requests, we will queue these requests and process.
---

## Technologies Used

- **Java 17**
- **Maven**: For project management and dependencies
- **JUnit 5**: For unit testing
- **Mockito**: For mocking in tests
- **GitHub Actions**: For continuous integration (CI)

---

## Project Structure

```bash
src/
├── main/
│   ├── java/
│   │   └── org/scraper/
│   │       ├── ScraperService.java
│   │       ├── RateLimiter.java
│   │       └── handler/
│   │           ├── HtmlResponseHandler.java
│   │           ├── JsonResponseHandler.java
│   │           └── ResponseHandlerFactory.java
│   │       └── observer/
│   │           ├── ScraperObserver.java
│   │           └── LoggingObserver.java
│   │       └── command/
│   │           ├── ScrapingCommand.java
│   │           └── WebScraper.java
│   └── resources/
├── test/
│   └── java/
│       └── org/scraper/
│           ├── WebScraperTest.java
│           └── HttpHeadersMock.java
└── pom.xml
