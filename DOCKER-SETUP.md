# Docker Setup for Store Management System

This guide explains how to run the Store Management System using Docker containers.

## Prerequisites

1. Install [Docker Desktop](https://www.docker.com/products/docker-desktop/) for Windows
2. Make sure Docker Engine and Docker Compose are running
3. You'll need at least 4GB of free RAM for running all containers

## Quick Start

1. Copy the sample environment file:
   ```bash
   cp env.sample .env
   ```
   
2. Build and start all services:
   ```bash
   docker-compose up -d
   ```

3. Access the applications:
   - Frontend: http://localhost:80
   - Auth Service API: http://localhost:8081
   - Auth Service Swagger UI: http://localhost:8081/swagger-ui
   - Inventory Service API: http://localhost:8082
   - Inventory Service Swagger UI: http://localhost:8082/swagger-ui.html

## Individual Services

### Database (PostgreSQL)
- Port: 5432
- Databases: authdb, inventorydb
- Default credentials: postgres/postgres (change in .env)

### Auth Service
- Port: 8081
- Spring Boot Java application
- Manages authentication and authorization

### Inventory Service
- Port: 8082
- Spring Boot Java application
- Manages inventory-related operations

### Frontend
- Port: 80
- React application served by Nginx
- Communicates with both backend services

## Configuration

All configuration is done through environment variables in the `.env` file:

- `POSTGRES_USER` & `POSTGRES_PASSWORD`: Database credentials
- `JWT_SECRET`: Shared secret for JWT token signing
- Service ports: `AUTH_SERVICE_PORT`, `INVENTORY_SERVICE_PORT`, `FRONTEND_PORT`
- Logging levels for different components

## Common Commands

### Start all services
```bash
docker-compose up -d
```

### View logs
```bash
# All services
docker-compose logs

# Specific service
docker-compose logs auth-service
docker-compose logs inventory-service
docker-compose logs frontend
```

### Stop all services
```bash
docker-compose down
```

### Rebuild services after code changes
```bash
docker-compose build
docker-compose up -d
```

### Access database
```bash
docker-compose exec postgres psql -U postgres
```

## Development Workflow

When developing:

1. **Local Development**: Make and test changes on your local machine without Docker
2. **Test in Containers**: Run with Docker to ensure everything works in a containerized environment
3. **CI/CD Integration**: Push to GitHub to trigger automated builds and testing (future)

## Troubleshooting

### Container not starting
Check logs: `docker-compose logs [service-name]`

### Database connection issues
1. Verify PostgreSQL container is running: `docker-compose ps`
2. Check credentials in .env file match what services are using

### Frontend not connecting to backend
1. Check network connectivity between containers
2. Verify API URLs in frontend configuration
