# 📚 API Documentation - Flight Booking Assistant

## Visão Geral

Esta documentação detalha todas as APIs REST disponíveis na aplicação Flight Booking Assistant.

## Base URL

```
http://localhost:8080
```

## Autenticação

A aplicação usa Spring Security. Para desenvolvimento local, a autenticação está configurada para permitir acesso sem credenciais.

## Endpoints

### 1. Bookings API

#### GET /api/bookings-tools/bookings

Obtém detalhes de uma reserva específica.

**Query Parameters:**

| Nome | Tipo | Obrigatório | Descrição |
|------|------|-------------|-----------|
| bookingNumber | string | Sim | Número da reserva (3-10 dígitos) |
| firstName | string | Sim | Primeiro nome do cliente (2-50 caracteres) |
| lastName | string | Sim | Sobrenome do cliente (2-50 caracteres) |

**Exemplo de Requisição:**

```bash
curl -X GET "http://localhost:8080/api/bookings-tools/bookings?bookingNumber=101&firstName=John&lastName=Doe"
```

**Resposta de Sucesso (200 OK):**

```json
{
  "bookingNumber": "101",
  "firstName": "John",
  "lastName": "Doe",
  "date": "2025-11-21",
  "bookingStatus": "CONFIRMED",
  "from": "LAX",
  "to": "JFK",
  "seatNumber": "12A",
  "bookingClass": "ECONOMY"
}
```

**Respostas de Erro:**

- `404 Not Found` - Reserva não encontrada
- `400 Bad Request` - Parâmetros inválidos
- `500 Internal Server Error` - Erro no servidor

---

#### PUT /api/bookings-tools/bookings

Altera a data e rota de uma reserva existente.

**Request Body:**

```json
{
  "bookingNumber": "101",
  "firstName": "John",
  "lastName": "Doe",
  "newDate": "2025-12-01",
  "from": "LAX",
  "to": "SFO"
}
```

**Campos:**

| Nome | Tipo | Obrigatório | Descrição |
|------|------|-------------|-----------|
| bookingNumber | string | Sim | Número da reserva |
| firstName | string | Sim | Primeiro nome do cliente |
| lastName | string | Sim | Sobrenome do cliente |
| newDate | string | Sim | Nova data (formato: YYYY-MM-DD) |
| from | string | Sim | Código IATA do aeroporto de origem (3 caracteres) |
| to | string | Sim | Código IATA do aeroporto de destino (3 caracteres) |

**Exemplo de Requisição:**

```bash
curl -X PUT "http://localhost:8080/api/bookings-tools/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingNumber": "101",
    "firstName": "John",
    "lastName": "Doe",
    "newDate": "2025-12-01",
    "from": "LAX",
    "to": "SFO"
  }'
```

**Resposta de Sucesso:**

- `204 No Content` - Reserva alterada com sucesso

**Respostas de Erro:**

- `400 Bad Request` - Dados inválidos ou alteração não permitida (menos de 24h antes do voo)
- `404 Not Found` - Reserva não encontrada
- `500 Internal Server Error` - Erro no servidor

**Regras de Negócio:**

- Alterações permitidas até 24 horas antes do voo
- Taxas aplicáveis:
  - Economy: $50
  - Premium Economy: $30
  - Business: Grátis

---

#### DELETE /api/bookings-tools/bookings

Cancela uma reserva existente.

**Request Body:**

```json
{
  "bookingNumber": "101",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Campos:**

| Nome | Tipo | Obrigatório | Descrição |
|------|------|-------------|-----------|
| bookingNumber | string | Sim | Número da reserva |
| firstName | string | Sim | Primeiro nome do cliente |
| lastName | string | Sim | Sobrenome do cliente |

**Exemplo de Requisição:**

```bash
curl -X DELETE "http://localhost:8080/api/bookings-tools/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingNumber": "101",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

**Resposta de Sucesso:**

- `204 No Content` - Reserva cancelada com sucesso

**Respostas de Erro:**

- `400 Bad Request` - Dados inválidos ou cancelamento não permitido (menos de 48h antes do voo)
- `404 Not Found` - Reserva não encontrada
- `500 Internal Server Error` - Erro no servidor

**Regras de Negócio:**

- Cancelamento permitido até 48 horas antes do voo
- Taxas de cancelamento:
  - Economy: $75
  - Premium Economy: $50
  - Business: $25

---

### 2. Utility APIs

#### GET /api/bookings-tools/utils/current-datetime

Retorna a data e hora atual do servidor.

**Exemplo de Requisição:**

```bash
curl -X GET "http://localhost:8080/api/bookings-tools/utils/current-datetime"
```

**Resposta de Sucesso (200 OK):**

```json
{
  "dateTime": "21/11/2025 01:30:00"
}
```

---

#### GET /api/bookings-tools/utils/sum-integers

Soma dois números inteiros.

**Query Parameters:**

| Nome | Tipo | Obrigatório | Descrição |
|------|------|-------------|-----------|
| numberA | integer | Sim | Primeiro número |
| numberB | integer | Sim | Segundo número |

**Exemplo de Requisição:**

```bash
curl -X GET "http://localhost:8080/api/bookings-tools/utils/sum-integers?numberA=10&numberB=20"
```

**Resposta de Sucesso (200 OK):**

```json
{
  "result": 30
}
```

---

#### GET /api/bookings-tools/utils/sum-decimals

Soma dois números decimais.

**Query Parameters:**

| Nome | Tipo | Obrigatório | Descrição |
|------|------|-------------|-----------|
| numberA | double | Sim | Primeiro número |
| numberB | double | Sim | Segundo número |

**Exemplo de Requisição:**

```bash
curl -X GET "http://localhost:8080/api/bookings-tools/utils/sum-decimals?numberA=10.5&numberB=20.3"
```

**Resposta de Sucesso (200 OK):**

```json
{
  "result": 30.8
}
```

---

#### GET /api/bookings-tools/utils/subtract

Subtrai dois números inteiros.

**Query Parameters:**

| Nome | Tipo | Obrigatório | Descrição |
|------|------|-------------|-----------|
| numberA | integer | Sim | Primeiro número (minuendo) |
| numberB | integer | Sim | Segundo número (subtraendo) |

**Exemplo de Requisição:**

```bash
curl -X GET "http://localhost:8080/api/bookings-tools/utils/subtract?numberA=50&numberB=20"
```

**Resposta de Sucesso (200 OK):**

```json
{
  "result": 30
}
```

---

## Códigos de Status HTTP

| Código | Descrição |
|--------|-----------|
| 200 OK | Requisição bem-sucedida |
| 204 No Content | Operação bem-sucedida sem conteúdo de retorno |
| 400 Bad Request | Dados inválidos ou regra de negócio violada |
| 404 Not Found | Recurso não encontrado |
| 500 Internal Server Error | Erro interno do servidor |

## Modelos de Dados

### BookingDetails

```json
{
  "bookingNumber": "string",
  "firstName": "string",
  "lastName": "string",
  "date": "string (YYYY-MM-DD)",
  "bookingStatus": "CONFIRMED | CANCELLED",
  "from": "string (3 chars)",
  "to": "string (3 chars)",
  "seatNumber": "string",
  "bookingClass": "ECONOMY | PREMIUM_ECONOMY | BUSINESS"
}
```

### ChangeBookingRequest

```json
{
  "bookingNumber": "string",
  "firstName": "string",
  "lastName": "string",
  "newDate": "string (YYYY-MM-DD)",
  "from": "string (3 chars)",
  "to": "string (3 chars)"
}
```

### CancelBookingRequest

```json
{
  "bookingNumber": "string",
  "firstName": "string",
  "lastName": "string"
}
```

## Exemplos de Uso com cURL

### Consultar Reserva

```bash
curl -X GET "http://localhost:8080/api/bookings-tools/bookings?bookingNumber=101&firstName=John&lastName=Doe" \
  -H "Accept: application/json"
```

### Alterar Reserva

```bash
curl -X PUT "http://localhost:8080/api/bookings-tools/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingNumber": "101",
    "firstName": "John",
    "lastName": "Doe",
    "newDate": "2025-12-15",
    "from": "LAX",
    "to": "ORD"
  }'
```

### Cancelar Reserva

```bash
curl -X DELETE "http://localhost:8080/api/bookings-tools/bookings" \
  -H "Content-Type: application/json" \
  -d '{
    "bookingNumber": "101",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

## Exemplos com JavaScript (Fetch API)

### Consultar Reserva

```javascript
const response = await fetch(
  'http://localhost:8080/api/bookings-tools/bookings?' + 
  new URLSearchParams({
    bookingNumber: '101',
    firstName: 'John',
    lastName: 'Doe'
  })
);

const booking = await response.json();
console.log(booking);
```

### Alterar Reserva

```javascript
const response = await fetch('http://localhost:8080/api/bookings-tools/bookings', {
  method: 'PUT',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    bookingNumber: '101',
    firstName: 'John',
    lastName: 'Doe',
    newDate: '2025-12-15',
    from: 'LAX',
    to: 'ORD'
  })
});

if (response.ok) {
  console.log('Booking updated successfully');
}
```

### Cancelar Reserva

```javascript
const response = await fetch('http://localhost:8080/api/bookings-tools/bookings', {
  method: 'DELETE',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify({
    bookingNumber: '101',
    firstName: 'John',
    lastName: 'Doe'
  })
});

if (response.ok) {
  console.log('Booking cancelled successfully');
}
```

## Rate Limiting

Atualmente não há rate limiting implementado. Para produção, considere adicionar:

- Spring Cloud Gateway com rate limiting
- Redis para controle de taxa
- Bucket4j para rate limiting local

## Versionamento da API

A API atual não possui versionamento. Para futuras versões, considere:

- Path versioning: `/api/v1/bookings-tools/...`
- Header versioning: `Accept: application/vnd.api.v1+json`
- Query parameter: `/api/bookings-tools/...?version=1`

## Suporte

Para questões ou problemas com a API, abra uma issue no repositório do projeto.
