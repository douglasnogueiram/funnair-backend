# Flight Booking Assistant - Local Development Setup

## Quick Start

### 1. Start Infrastructure Services (Docker)

```bash
docker-compose up -d postgres chroma loki tempo grafana prometheus
```

### 2. Set Environment Variables

```bash
export OPENAI_API_KEY=your-openai-api-key-here
export LOKI_URL=http://localhost:3100/loki/api/v1/push
```

### 3. Run the Application

```bash
./mvnw spring-boot:run
```

### 4. Access the Application

- **Application UI**: http://localhost:8080
- **Grafana (observability)**: http://localhost:3000
- **Prometheus**: http://localhost:9091
- **Tempo**: http://localhost:3200

---

## Configuration

The application uses `application.properties` with environment variable support:

| Variable | Default | Description |
|----------|---------|-------------|
| `OPENAI_API_KEY` | *(required)* | Your OpenAI API key |
| `DB_HOST` | `localhost` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `vector_store` | Database name |
| `DB_USER` | `postgres` | Database username |
| `DB_PASSWORD` | `postgres` | Database password |
| `CHROMA_HOST` | `http://localhost` | Chroma vector store host |
| `CHROMA_PORT` | `8000` | Chroma port |
| `LOKI_URL` | `http://localhost:3100/loki/api/v1/push` | Loki logging endpoint |
| `MCP_SERVER_URL` | `http://localhost:8085` | MCP server URL |

---

## Stopping Services

### Stop Infrastructure Only
```bash
docker-compose stop
```

### Stop and Remove Containers
```bash
docker-compose down
```

### Stop and Remove Volumes (clean slate)
```bash
docker-compose down -v
```

---

## Troubleshooting

### Application won't start
- Check if infrastructure services are running: `docker-compose ps`
- Verify OpenAI API key is set: `echo $OPENAI_API_KEY`
- Check logs: `./mvnw spring-boot:run` (logs will appear in console)

### Database connection issues
- Ensure Postgres is healthy: `docker-compose ps postgres`
- Check connection: `psql -h localhost -U postgres -d vector_store`

### Observability not working
- Check Loki: `curl http://localhost:3100/ready`
- Check Tempo: `curl http://localhost:3200/ready`
- Verify `LOKI_URL` environment variable is set

---

## Development Workflow

1. **Make code changes**
2. **Restart application**: `Ctrl+C` then `./mvnw spring-boot:run`
3. **Hot reload** (for frontend): Vaadin dev mode is enabled by default

---

## Performance

**Local development startup time**: ~40-60 seconds
- Much faster than Docker (~5+ minutes)
- Instant code reload
- Better debugging experience
