# Farmacia BD II - Entrega 1 (OLTP)

JPA 2.1 + Hibernate 5.2.10.Final + PostgreSQL. Maven.

## Preparar o banco (Docker, porta 5433)
```
docker run -d --name farmacia-pg -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=farmacia -p 5433:5432 postgres:15
```
O `persistence.xml` ja aponta para `localhost:5433/farmacia` (postgres/postgres). Para outro banco use
`-Dbd2.url=... -Dbd2.user=... -Dbd2.password=...`. As tabelas sao criadas automaticamente (`hbm2ddl.auto=update`).

## Scripts SQL prontos (pasta `sql/`)
- `01_ddl_oltp.sql` - DDL gerado pelo Hibernate
- `02_carga_dados.sql` - todos os dados (INSERTs): carga completa sem precisar rodar o Java
```
psql -U postgres -d farmacia -f sql/01_ddl_oltp.sql
psql -U postgres -d farmacia -f sql/02_carga_dados.sql
```

## Executar (o projeto deve estar em um caminho SEM acentos)
```
mvn compile exec:java -Dexec.mainClass=br.edu.bd2.carga.CargaDados      # carga (use --reset para recarregar)
mvn compile exec:java -Dexec.mainClass=br.edu.bd2.app.Main              # console (CRUD + consultas)
mvn compile exec:java -Dexec.mainClass=br.edu.bd2.app.Main -Dbd2.showSql=true   # mostra o SQL
mvn compile exec:java -Dexec.mainClass=br.edu.bd2.app.ExportarDDL       # gera sql/01_ddl_oltp.sql
```
Se rodar no JDK 17+ o arquivo `.mvn/jvm.config` ja libera o acesso necessario ao Hibernate 5.2.

## Estrutura
- `modelo/` 12 entidades (heranca JOINED, @Embeddable, @Enumerated, 1:1, 1:N bidirecional, N:N)
- `dao/GenericDAO` CRUD com transacao; `consultas/` 10 JPQL (2 @NamedQuery) e 3 Criteria
- `carga/CargaDados` 5 lojas, 200 clientes, 122 medicamentos, 2.000 vendas em 2 anos
