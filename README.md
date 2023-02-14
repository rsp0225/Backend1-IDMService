# FabFlix

Disclaimer: This project is based on a project for CS 122B (Projects in Databases and Web Applications) at UC Irvine. 
            It is intended for educational and demonstration purposes only.

FabFlix is a web-based e-commerce system that allows customers to search for and order movies. Built using the Spring Boot Framework in Java, it utilizes a single-page design and microservices architecture to ensure a seamless user experience.

## Features
- Search and order movies by title, genre, director, and release year
- Add movies to cart, checkout using Stripe API, and view order history
- Authenticate customers through an API gateway and Identity Management Service
- Obtain movie data from The Movie Database (TMDB)
- Web versions demonstration available
-> https://www.youtube.com/watch?v=gZAP6BBeRf4 

## Technical Details
### Backend
- Spring Boot Framework
- MySQL database with JDBC
- JSON processing with Jackson

### Privileged Microservices:
- Billing Service - handles cart, checkout, and order history
- Movies Service - search and sort movies

### Frontend
- Web App built with React

## Getting Started 
To get started with FabFlix, please refer to the appropriate directory under backend and frontend to view the README files for specific instructions on how to run and use the system.
