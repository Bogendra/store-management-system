# Service Check Instructions

Please make sure both backend services are running before using the frontend:

## 1. Auth Service (Port 8081)
```bash
cd c:/Users/bbk04/Desktop/Bogendra/Projects/store-management-system/services/auth-service
mvn spring-boot:run
```

## 2. Inventory Service (Port 8082)
```bash
cd c:/Users/bbk04/Desktop/Bogendra/Projects/store-management-system/services/inventory-service
mvn spring-boot:run
```

## 3. Verify Services
You should be able to access:
- Auth Service Swagger UI: http://localhost:8081/swagger-ui.html
- Inventory Service Swagger UI: http://localhost:8082/swagger-ui.html

If both services are running, try the frontend again.
