# Movie booking project

Status: Tested repair source on repair/security-and-project-docs; not merged or deployed

This is an Angular and Spring Boot movie-booking application. The original movies.zip upload is preserved. A conventional source reconstruction was published to repair/security-and-project-docs on 20 November 2026. Default main still holds the original ZIP; the repair branch is the source to inspect. The restoration is maintenance work, not proof that the original upload built unchanged.

## What the uploaded source contains

The Angular files define login and registration screens, user and administrator navigation, movie listing, movie creation, movie editing and booking views. User screens include booking a movie and viewing bookings. Administrator screens include movie and booking management.

The backend defines users, movies and bookings with repository and service layers. The booking service finds a movie, compares requested seats with seats already booked, associates the booking with a user and movie, then saves it. It also contains lookup and deletion operations. The movie service handles create, update, list, lookup and delete operations.

These are observed code paths, not a claim that the current upload runs successfully or safely in production.

## How the technologies are used

### Angular and TypeScript

Components implement movie, booking, authentication and navigation screens. Routes connect user and administrator views.

### Angular forms and HttpClient

Forms collect input and services send requests to movie, booking and authentication endpoints.

### Spring Boot and Java

Controllers expose API endpoints; services contain movie and booking operations. The bundled Maven descriptor targets Java 17.

### Spring Data JPA

Entity and repository files model persistence for users, movies and bookings.

### Spring Security and JWT

Configuration and filters authenticate bearer tokens. The published repair branch adds role and ownership checks; tests cover denied cross-user access. This does not replace a production security review.

### MySQL connector

The original archive included MySQL persistence support but lacked runtime configuration. The repair branch supplies environment-based connection settings. This repair branch adds versioned Flyway migrations.

## Intended booking flow

Users browse movies and request a positive ticket quantity. The backend uses the movie price, checks capacity and persists a user-owned booking. A transactional movie lock addresses concurrent last-seat requests. The interface now makes clear that the demo reserves a count, not specific numbered seats.

The H2 concurrency regression accepts only one of two requests for the final seat. MySQL workflow and restart checks also pass locally, but that does not establish deployed availability, real cinema integration or payment processing.

## Problems found in the original upload

- The source is inside `movies.zip`, which makes ordinary code browsing and review harder.
- Angular components are in a flat archive layout while imports expect conventional per-component directories.
- The Angular workspace and package metadata needed for a clean build are missing.
- The Maven descriptor is stored inside the Angular archive area rather than a normal backend project root.
- Local runtime and database configuration are not supplied.
- Several Angular declarations are duplicated.
- The backend security configuration permits broad API route patterns; authentication and record ownership need explicit tests.

## Local restoration, separate from the original upload

The original archive is retained. Commit fb8f5a73679b2ec4b850386962b8b2178e44e29a publishes conventional Maven and Angular folders, environment-only JWT/database settings, role and ownership checks, trusted pricing and the restored Angular 20 workspace. It is not merged or deployed.

This repair branch adds Flyway MySQL schema migrations, input checks and booking capacity protections on movie edits. Sixteen backend tests pass on isolated H2. A MySQL 8 workflow test and a restart check against the migrated schema pass. The Angular production build passes.

An actual local browser journey verifies admin movie creation, user login, quantity booking, own booking history and cancellation. Changed booking/history screens were checked at 320, 390 and 1280 pixels. The quantity, cancellation, schema and manual-only CI follow-up changes are included on this repair branch, after the initial restoration commit.

This repair branch replaces unsupported homepage discount, review and promotion claims with an explicit software-demo description. It states that bookings reserve a quantity, not numbered seats, and that no real cinema ticket sale or payment processing occurs. The homepage was checked at 320, 390 and 1280 pixels. These homepage changes are included on this repair branch.

## Local setup

See [development setup](docs/LOCAL_SETUP.md) for commands, environment variables and database migration notes.
