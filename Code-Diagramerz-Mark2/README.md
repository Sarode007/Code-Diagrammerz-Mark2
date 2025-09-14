# Code Diagrammerz

A web application for generating UML diagrams from Java code.

## Deployment

This application is deployed on Render.com at:
https://code-diagramerz.onrender.com

## Local Development

1. Build: `mvn clean package`
2. Run: `docker build -t code-diagramerz-app .`
3. Start: `docker run -p 8080:8080 code-diagramerz-app`