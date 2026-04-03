# 📋 Instruções para Inicialização do Banco de Dados

## Pré-requisitos

1. PostgreSQL rodando na porta 5432
2. Banco de dados `flight_booking` criado

## Passos para Executar

### 1. Iniciar PostgreSQL (se ainda não estiver rodando)

```bash
docker run --name postgres-flight \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=flight_booking \
  -p 5432:5432 \
  -d postgres
```

### 2. Executar o Script SQL

**Opção A: Via linha de comando**
```bash
psql -h localhost -U postgres -d flight_booking -f src/main/resources/db/init-database.sql
```

**Opção B: Via Docker**
```bash
docker exec -i postgres-flight psql -U postgres -d flight_booking < src/main/resources/db/init-database.sql
```

**Opção C: Via cliente PostgreSQL (pgAdmin, DBeaver, etc.)**
- Abra o arquivo `src/main/resources/db/init-database.sql`
- Execute o script no banco `flight_booking`

### 3. Verificar Dados

```sql
-- Contar registros
SELECT 'Customers' as table_name, COUNT(*) as record_count FROM customers
UNION ALL
SELECT 'Bookings' as table_name, COUNT(*) as record_count FROM bookings;

-- Ver todos os dados
SELECT 
    b.booking_number,
    c.first_name,
    c.last_name,
    b.date,
    b.from_airport,
    b.destination,
    b.seat_number,
    b.booking_class,
    b.booking_status
FROM bookings b
JOIN customers c ON b.customer_id = c.id
ORDER BY b.booking_number;
```

### 4. Iniciar a Aplicação

```bash
./mvnw spring-boot:run
```

## Dados Demo Criados

- **5 Customers**: John Doe, Jane Smith, Michael Johnson, Sarah Williams, Robert Taylor
- **5 Bookings**: 101-105 com datas futuras e diferentes rotas

## Notas

- O script SQL cria as tabelas e insere dados demo
- A aplicação Spring Boot usa `ddl-auto=update` (não recria tabelas)
- Para resetar dados, execute o script novamente (ele faz DROP antes de CREATE)
