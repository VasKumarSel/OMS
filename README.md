# Order Management System (OMS)

A microservices-based Order Management System built with Spring Boot, featuring real-time market data integration, JWT-based authentication, and Temporal workflow orchestration for reliable order processing.

## 📋 Table of Contents
- [Project Overview](#-project-overview)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)  
- [Setup Instructions](#-setup-instructions)
- [API Documentation](#-api-documentation)
- [JWT Authentication](#-jwt-authentication)
- [Known Limitations](#-known-limitations)

## 🔍 Project Overview

The OMS is designed to handle trading operations with the following key features:

- **Market Data Service**: Provides real-time market prices and status for various trading symbols
- **Order Service**: Handles order creation, validation, execution, and history tracking
- **JWT Authentication**: Secure user authentication with role-based access control
- **Temporal Workflow Integration**: Reliable order processing with built-in retry mechanisms
- **PostgreSQL Database**: Persistent storage for market data, orders, users, and wallets
- **RESTful APIs**: Clean REST interfaces for all operations

### Key Capabilities
- User authentication and authorization (TRADER/ADMIN roles)
- Market data retrieval and validation
- Order creation (MARKET/LIMIT orders for BUY/SELL operations)
- Order status tracking and history
- Wallet management with transaction history
- Real-time order processing workflows

## 🏗 Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                           Client Applications                        │
└─────────────────────┬───────────────────────────────────────────────┘
                      │ HTTP/REST API
        ┌─────────────┼─────────────┐
        │             │             │
┌───────▼──────┐ ┌────▼─────┐       │
│ Order Service│ │Market Data│       │
│   (Port:8080)│ │ Service   │       │
│              │ │(Port:8081)│       │
└───────┬──────┘ └────┬─────┘       │
        │             │             │
        │    ┌────────▼─────────┐   │ Workflow 
        │    │                  │   │ Orchestration
        └────►  Temporal Engine  ◄───┘
             │  (Workflow       │
             │  Orchestrator)   │
             └────────┬─────────┘
                      │
     ┌────────────────┼────────────────┐
     │                │                │
┌────▼──────────┐ ┌───▼───────────┐ ┌─▼──────────┐
│  PostgreSQL   │ │   Cassandra   │ │Elasticsearch│
│(marketdata_db)│ │  (Temporal    │ │ (Temporal   │
│ Business Data │ │   Metadata)   │ │ Visibility) │
└───────────────┘ └───────────────┘ └────────────┘
```

Supporting Infrastructure:
├── Cassandra (Temporal Persistence)
├── Elasticsearch (Temporal Visibility)  
└── Temporal UI (Workflow Monitoring)
```

### Service Communication
- **Order Service → Market Data Service**: REST calls for price validation and market status
- **Temporal Workflow Engine**: Orchestrates order processing workflows between services
- **Order Service → Temporal**: Starts workflows and executes activities
- **Services → PostgreSQL (marketdata_db)**: Business data persistence (orders, users, market data)
- **Temporal → Cassandra**: Workflow state and execution history
- **Temporal → Elasticsearch**: Workflow visibility and search capabilities

## 📋 Prerequisites

### Required Software
- **Java**: OpenJDK 17 or higher
- **Docker & Docker Compose**: For running Temporal infrastructure
- **PostgreSQL**: 12+ (local installation required)
- **Gradle**: 7+ (or use included gradlew)

### Network Ports
Ensure the following ports are available:
- `5432`: PostgreSQL
- `7233`: Temporal gRPC
- `8080`: Order Service
- `8081`: Market Data Service  
- `8082`: Temporal UI
- `9042`: Cassandra
- `9200`: Elasticsearch

## 🚀 Setup Instructions

### 1. Start Temporal Infrastructure

First, start the Temporal server and supporting services:

```bash
# Navigate to project root
cd OMS-main

# Start Temporal infrastructure (Cassandra, Elasticsearch, Temporal server, and UI)
docker-compose up -d

# Verify services are running
docker-compose ps
```

Wait for all services to be healthy (this may take 2-3 minutes):
```bash
# Check service health
docker-compose logs temporal
```

### 2. Setup Local PostgreSQL Database

Create the required database and user in your local PostgreSQL installation:

```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE marketdata_db;
CREATE USER marketdata_user WITH PASSWORD 'admin';
GRANT ALL PRIVILEGES ON DATABASE marketdata_db TO marketdata_user;
GRANT ALL ON SCHEMA public TO marketdata_user;
```

### 3. Build the Project

```bash
# Build Market Data Service
cd market-data-service
./gradlew clean build

# Build Order Service  
cd ../order-service
./gradlew clean build
```

### 4. Run the Services

**Important**: Start services in this order to ensure proper dependency resolution.

#### Start Market Data Service (Terminal 1)
```bash
cd market-data-service
./gradlew bootRun
```

Wait for the service to fully start (look for "Started MarketDataServiceApplication").

#### Start Order Service (Terminal 2)  
```bash
cd order-service
./gradlew bootRun
```

### 5. Verify Setup

Check that all services are running:

```bash
# Market Data Service health
curl http://localhost:8081/api/v1/market/status

# Order Service health (should return 401 - authentication required)
curl http://localhost:8080/api/v1/orders

# Temporal UI
# Open http://localhost:8082 in your browser
```

## 📚 API Documentation

### Authentication Endpoints

#### Login
Generate a JWT token for API access:

```bash
# Login as trader
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "trader1",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "username": "trader1", 
  "role": "TRADER",
  "expiresIn": 86400000
}
```

### Market Data Service APIs

#### Get Market Price
```bash
curl http://localhost:8081/api/v1/market/price/AAPL
```

#### Get Market Status
```bash
curl http://localhost:8081/api/v1/market/status
```

#### Validate Symbol
```bash
curl http://localhost:8081/api/v1/market/validate/AAPL
```

### Order Service APIs

**Note**: All Order Service APIs require JWT authentication. Include the token in the Authorization header:

#### Create Order
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "side": "BUY", 
    "quantity": 100,
    "orderType": "MARKET"
  }'
```

#### Create Limit Order
```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "symbol": "AAPL",
    "side": "BUY",
    "quantity": 50, 
    "orderType": "LIMIT",
    "limitPrice": 150.50
  }'
```

#### Get Order by ID
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8080/api/v1/orders/1
```

#### Get Orders with Filters
```bash
# Get all orders
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8080/api/v1/orders"

# Filter by status
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8080/api/v1/orders?status=FILLED"

# Filter by symbol  
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8080/api/v1/orders?symbol=AAPL"

# Pagination
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8080/api/v1/orders?page=0&size=10&sortBy=createdAt&sortDir=desc"
```

#### Get Order History
```bash
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8080/api/v1/orders/1/history
```

### Order States and Workflow

Orders go through the following states during processing:
1. **PENDING**: Initial state after creation
2. **VALIDATING**: Market data and symbol validation
3. **FRAUD_CHECK**: Risk and compliance validation
4. **EXECUTING**: Order execution in progress
5. **FILLED**: Order successfully executed
6. **REJECTED**: Order rejected (insufficient funds, invalid symbol, etc.)
7. **FAILED**: Technical failure during processing
8. **CANCELLED**: Order cancelled by user or system

### Available Trading Symbols

The system comes pre-configured with the following symbols:
- `AAPL` (Apple Inc.)
- `GOOGL` (Alphabet Inc.)  
- `MSFT` (Microsoft Corporation)
- `TSLA` (Tesla Inc.)
- `AMZN` (Amazon.com Inc.)
- `META` (Meta Platforms Inc.)
- `NFLX` (Netflix Inc.)
- `NVDA` (NVIDIA Corporation)

## 🔐 JWT Authentication

### Pre-configured Users

The system comes with the following test users:

| Username | Password | Role | Initial Balance |
|----------|----------|------|-----------------|
| trader1 | password123 | TRADER | $50,000 |
| trader2 | password123 | TRADER | $25,000 |  
| admin1 | password123 | ADMIN | $100,000 |

### JWT Token Details

- **Algorithm**: HS256
- **Expiration**: 24 hours (86400 seconds)
- **Secret**: Configurable via `app.jwt.secret` property
- **Claims**: userId, username, role, issued timestamp

### Using JWT Tokens

1. **Login** to get a token:
   ```bash
   curl -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username": "trader1", "password": "password123"}'
   ```

2. **Extract the token** from the response and store it:
   ```bash
   export JWT_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
   ```

3. **Include the token** in subsequent API calls:
   ```bash
   curl -H "Authorization: Bearer $JWT_TOKEN" \
     http://localhost:8080/api/v1/orders
   ```

### Token Validation

The system validates tokens on every request to protected endpoints:
- Signature verification using the configured secret
- Expiration time checking
- User existence and active status validation

## ⚠️ Known Limitations

### 1. Production Readiness
- **Database**: No connection pooling optimization
- **Monitoring**: Limited observability and metrics
- **Error Handling**: Basic error responses, needs enhancement

### 2. Service Architecture
- **Shared Database**: Both services use the same PostgreSQL database (marketdata_db)
- **No Load Balancing**: Single instance deployment only
- **Circuit Breaker**: Missing fault tolerance patterns

### 3. Market Data
- **Static Data**: Market prices are not real-time, loaded from SQL file
- **Limited Symbols**: Only few pre-configured trading symbols

### 4. Wallet Management
- **No Multi-Currency**
- **No Margin Trading**: Cash-only transactions

### 5. Scalability
- **Single Instance**: No horizontal scaling support
- **No Caching**: Direct database queries without caching layer
- **No Event Streaming**: Missing event-driven architecture

### 6. Testing & DevOps
- **No Unit test files**: -
- **Manual Deployment**: No CI/CD pipeline configuration
- **No Health Checks**: Basic Spring Actuator endpoints only

### 7. Configuration
- **Environment Configs**: No profile-specific configurations
- **Secrets Management**: Database credentials in plain text
- **No External Config**: All configuration in application.properties

---

Temporal UI at http://localhost:8082 for workflow monitoring.