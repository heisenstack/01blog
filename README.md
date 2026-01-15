## Run (without docker)

BACKEND:
_cd backend/_
run:
`export $(cat ../.env | grep -v '^#' | grep -v '^$' | xargs)`
run:
`mvn spring-boot:run`

DATABASE:
run:

```docker run -d \
  --name blog-db \
  -e POSTGRES_DB=zerooneblog \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=password \
  -p 5432:5432 \
  postgres:17-alpine
```

FRONTEND:
run:
`ng serve`

## Run (with docker)

pwd = 01blog
run:
`docker-compose up -d`
