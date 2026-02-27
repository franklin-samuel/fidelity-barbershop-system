# NaGaragem Barbershop Management System

NaGaragem is a management system for barbershops, made to accomplish the goal of have all control over your babershop. The system has many modules, including roles system, catalog management, sales flow
and data analyses to better vision about the business. Metrics which can change the game, all in a unique system.

## Technologies
The system is built in Java +21 + Spring Boot, conecting to a database hosted at neon server, which uses a free postgres azure instance. Our API is actually hosted at render, using a CI/CD pipeline that is based on
a Github Workflow that do tests and get up our Docker Image to Docker Hub, then trigger a deploy hook to render that is looking for our Docker Hub and start a automatic deploy looking for our currently image.

About the frontend, we've got the Web App and Mobile App as well, the web is made in React + Next and the mobile app is made in React Native. The web app is hosted at vercel, which comes already with a automatic deploy 
based on Github pushes at main. Talking about the mobile app, I generate the APK manually and drop it in a Google Drive paste in a versioned way(v1, v2, v3, ...) to disponibilize it to customer.

Tech Stack: Java, Spring Boot, React, NextJS, React Native, PostgreSQL, Docker.

## Code Architecture
I chose the hexagonal architecture for this project, because it's a way to code I'm actually used to, and don't leaves not to be desired in architectural side.

Modules:
- Domain: domain entities
- Core: All system interfaces(ports)
- Persistence: JPA entities, JPA repositories and mappers to domain
- Business: Business interface implementations, where's the business logic
- Security: Routes protection, authentication configuration, password encrypt, just security
- Web: Expose endpoints
