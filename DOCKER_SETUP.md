# Flight Booking Assistant - Docker Compose Setup

## Quick Start

### Prerequisites
- Docker and Docker Compose installed
- OpenAI API key
- Java 21 and Maven (for building the application)

### Running with Docker Compose

1. **Build the application locally** (faster than building in Docker)
   ```bash
   ./mvnw clean package -DskipTests
   ```

2. **Set your OpenAI API key** (choose one method):

   **Option A: Create a `.env` file** (recommended)
   ```bash
   cp .env.example .env
   # Edit .env and add your OpenAI API key
   ```

   **Option B: Export environment variable**
   ```bash
   export OPENAI_API_KEY=your-api-key-here
   ```

3. **Start all services**
   ```bash
   docker-compose up -d
   ```

4. **Check application health**
   ```bash
   # Wait for the application to start (may take 1-2 minutes)
   curl http://localhost:8080/actuator/health
   ```

5. **Access the application**
   - Application UI: http://localhost:8080
   - Grafana (observability): http://localhost:3000
   - Prometheus: http://localhost:9091
   - Tempo: http://localhost:3200
   - MailDev: http://localhost:3001

### Stopping Services

```bash
docker-compose down
```

To remove volumes as well:
```bash
docker-compose down -v
```

## Environment Variables

See `.env.example` for all available environment variables.

### Required
- `OPENAI_API_KEY` - Your OpenAI API key

### Optional (with defaults)
All other variables have sensible defaults for Docker Compose deployment.

## Development

### Running Locally (without Docker)

1. **Start infrastructure services only**
   ```bash
   docker-compose up -d postgres chroma loki tempo grafana prometheus
   ```

2. **Set environment variables**
   ```bash
   export OPENAI_API_KEY=your-api-key-here
   export LOKI_URL=http://localhost:3100/loki/api/v1/push
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

## Troubleshooting

### Application fails to start
- Check logs: `docker-compose logs app`
- Ensure OpenAI API key is set correctly
- Verify all dependent services are healthy: `docker-compose ps`

### Database connection issues
- Check postgres is healthy: `docker-compose ps postgres`
- View postgres logs: `docker-compose logs postgres`

### Observability not working
- Check Loki logs: `docker-compose logs loki`
- Check Tempo logs: `docker-compose logs tempo`
- Verify Grafana datasources at http://localhost:3000

## Architecture

The application uses:
- **PostgreSQL** (with pgvector) - Main database
- **Chroma** - Vector store for RAG
- **Loki** - Log aggregation
- **Tempo** - Distributed tracing
- **Grafana** - Observability dashboards
- **Prometheus** - Metrics collection
- **MailDev** - Email testing
