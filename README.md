# E-Commerce API + NL-to-SQL Chatbot

Spring Boot 3.5 · Java 21 · Gradle · PostgreSQL (or H2) · Google Gemini (Spring AI)

---

## Description

An e-commerce REST API with an NL-to-SQL chatbot. Users can ask questions in natural language (`"Show all orders"`) and the chatbot translates them into SQL queries, executes them against the database, and returns a user-friendly response.

The chatbot uses **Google Gemini** (model `gemini-3.1-flash-lite`) via **Spring AI**.

**Tables:** `customers` · `products` · `orders` · `order_items`

---

## Quick Start

### 1. Google Gemini API Key

1. Create a free API key at [Google AI Studio](https://aistudio.google.com/apikey)
2. Add the key to `src/main/resources/application.properties`:

```properties
spring.ai.model.chat=google-genai
spring.ai.google.genai.api-key=YOUR_KEY_HERE
spring.ai.google.genai.chat.options.model=gemini-3.1-flash-lite
```

### 2. Database — pick one

The app supports two database profiles. `h2` is the default — no local database install required, so this is what most participants should use. Switch to `postgres` only if you specifically want to run against a real PostgreSQL instance.

**Option A — H2 (default, no install needed)**

Nothing to set up — H2 stores its data in a local file under `./data/`. Just run the app (see below), no profile switch needed.

A web console is available at `http://localhost:8080/h2-console` while the app is running. **The login form defaults to an unrelated `jdbc:h2:mem:testdb` URL — you must overwrite it**, or you'll get `Database "mem:testdb" not found`. Set:

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:file:./data/ecommerce_db` |
| User Name | `sa` |
| Password | *(leave blank)* |

**Option B — PostgreSQL**

```sql
CREATE DATABASE ecommerce_db;
```

Adjust credentials if needed in `src/main/resources/application-postgres.properties` (defaults to `postgres` / `student123` on `localhost:5432`). Then switch profiles, either by editing `spring.profiles.active` in `src/main/resources/application.properties`, or overriding it without touching the file:

```bash
./gradlew bootRun --args='--spring.profiles.active=postgres'
# or
SPRING_PROFILES_ACTIVE=postgres ./gradlew bootRun
```

Flyway automatically picks the matching migration scripts (`db/migration/postgresql/` or `db/migration/h2/`) and creates the tables + sample data for whichever database is active — no manual step needed either way.

### 3. Run

```bash
./gradlew bootRun
```

---

## Swagger UI

Start the application and open in your browser:

```
http://localhost:8080/swagger-ui.html
```

---

## Chatbot – NL-to-SQL

### Endpoint

```
POST /api/ask
```

### Request Body

```json
{
  "question": "What is the most expensive product?"
}
```

### Response

```json
{
  "answer": "The most expensive product is Dell XPS 15 at €1,299.99."
}
```

### How It Works

```
User question
      ↓
Gemini LLM → generates a SQL SELECT statement
      ↓
Database executes the query (PostgreSQL or H2, whichever profile is active)
      ↓
Gemini LLM → formats the results into a user-friendly response
      ↓
JSON response to the user
```

The system prompt tells the LLM which SQL dialect to target (`nlsql.dialect`, set per profile), so the generated SQL matches whichever database is actually running.

**Security:** the generated SQL is validated — only `SELECT` statements are allowed. `INSERT`, `UPDATE`, `DELETE`, `DROP`, etc. are forbidden.

**Empty results:** if nothing is found in the database (e.g., the user asks for "computer price" but the catalog has "laptop"), the LLM receives a list of all active products and suggests similar alternatives.

---

## API Contract

### Customers

| Method | URL | Description |
|--------|-----|-------------|
| GET    | `/api/customers` | All customers |
| GET    | `/api/customers/{id}` | Customer by ID |
| POST   | `/api/customers` | Create customer |
| PUT    | `/api/customers/{id}` | Update customer |
| DELETE | `/api/customers/{id}` | Delete customer |

### Products

| Method | URL | Description |
|--------|-----|-------------|
| GET    | `/api/products` | All active products |
| GET    | `/api/products?category=Electronics` | Filter by category |
| GET    | `/api/products?minPrice=10&maxPrice=100` | Filter by price |
| GET    | `/api/products/{id}` | Product by ID |
| POST   | `/api/products` | Create product |
| PUT    | `/api/products/{id}` | Update product |
| DELETE | `/api/products/{id}` | Deactivate product |

### Orders

| Method | URL | Description |
|--------|-----|-------------|
| GET    | `/api/orders` | All orders |
| GET    | `/api/orders?status=PENDING` | Filter by status |
| GET    | `/api/orders?customerId={uuid}` | Orders by customer |
| GET    | `/api/orders/{id}` | Order by ID |
| POST   | `/api/orders` | Create order |
| PATCH  | `/api/orders/{id}/status` | Update status |

#### Order Statuses

```
PENDING → CONFIRMED → SHIPPED → DELIVERED
    ↓          ↓
CANCELLED  CANCELLED
```

---

## Example Requests

```bash
# Chatbot – natural language question
curl -X POST http://localhost:8080/api/ask \
  -H "Content-Type: application/json" \
  -d '{"question": "How many customers do we have?"}'

# All customers
curl http://localhost:8080/api/customers

# New customer
curl -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{"email":"test@email.com","firstName":"Test","lastName":"User"}'

# Electronics products
curl http://localhost:8080/api/products?category=Electronics

# New order
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "<uuid>",
    "shippingAddress": "123 Main St, Tallinn",
    "items": [{"productId": "<uuid>", "quantity": 1}]
  }'

# Update order status
curl -X PATCH http://localhost:8080/api/orders/<uuid>/status \
  -H "Content-Type: application/json" \
  -d '{"status": "CONFIRMED"}'
```

---

## Package Structure

```
src/main/java/com/example/ecommerce/
├── EcommerceApplication.java
├── controller/
│   ├── AskController.java         # POST /api/ask – chatbot endpoint
│   ├── CustomerController.java
│   ├── ProductController.java
│   └── OrderController.java
├── service/
│   ├── NlToSqlService.java        # NL→SQL pipeline (via Spring AI ChatClient / Gemini)
│   ├── CustomerService.java
│   ├── ProductService.java
│   └── OrderService.java
├── repository/
│   ├── CustomerRepository.java
│   ├── ProductRepository.java
│   └── OrderRepository.java
├── entity/
│   ├── Customer.java
│   ├── Product.java
│   ├── Order.java
│   └── OrderItem.java
├── dto/
│   ├── AskRequest.java
│   ├── AskResponse.java
│   └── Dtos.java
└── exception/
    ├── ResourceNotFoundException.java
    ├── BusinessException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.properties          # Shared config + spring.profiles.active
├── application-postgres.properties # PostgreSQL datasource + nlsql.dialect
├── application-h2.properties       # H2 datasource + nlsql.dialect + H2 console
└── db/migration/
    ├── postgresql/
    │   ├── V1__initial_schema.sql
    │   └── V2__seed_data.sql
    └── h2/
        ├── V1__initial_schema.sql
        └── V2__seed_data.sql
```

Flyway's `{vendor}` placeholder (`spring.flyway.locations=classpath:db/migration/{vendor}`) automatically resolves to `postgresql` or `h2` based on the active datasource, so the right migration folder runs without any extra config.

---

## Technologies

| Technology | Version | Purpose |
|------------|---------|---------|
| Spring Boot | 3.5.16 | Application framework |
| Java | 21 | Programming language |
| Gradle | 8.13 | Build tool (via wrapper) |
| PostgreSQL | 15+ | Database (default profile: `postgres`) |
| H2 | 2.3.x | Embedded database, no install needed (fallback profile: `h2`) |
| Flyway | 11.x (`flyway-core` + `flyway-database-postgresql`) | Database migrations |
| Spring AI | 1.1.8 | LLM integration (`spring-ai-starter-model-google-genai`) |
| Google Gemini | `gemini-3.1-flash-lite` | LLM (NL→SQL generation + result summarization) |
| springdoc-openapi | 2.8.13 | Swagger UI |
| Lombok | 1.18.x | Boilerplate reduction |