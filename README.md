# MasterEat

Progetto realizzato per i corsi di Web Engineering e Sviluppo Web Avanzato.

| Studente | Matricola |
| --- | --- |
| Davide Odoardi | 292216 |
| Alessandro Marinucci | 261682 |
| Antonino Campellone | 292594 |

## Componenti del progetto

| Componente | Descrizione |
| --- | --- |
| Web Engineering | Web application server-side con Servlet Jakarta, FreeMarker, sessioni HTTP, aree cliente/personale/proprietario. |
| Sviluppo Web Avanzato | API REST Jersey/JAX-RS sotto `/api/*`, autenticazione Bearer token, OpenAPI e client JavaScript dimostrativo. |
| Database | Schema MySQL, seed utenti e menu demo. |
| Documentazione | Relazioni WE/SWA, specifica OpenAPI, navigation diagram e schema ER. |

## Documentazione

| Documento | Contenuto |
| --- | --- |
| [`docs/DocumentazioneWE.md`](docs/DocumentazioneWE.md) | Documentazione della web application server-side. |
| [`docs/DocumentazioneSWA.md`](docs/DocumentazioneSWA.md) | Documentazione delle API REST e del client SWA. |
| [`docs/OpenAPI.yaml`](docs/OpenAPI.yaml) | Specifica OpenAPI 3.0.3. |
| [`docs/diagrams/NavigationDiagram.mmd`](docs/diagrams/NavigationDiagram.mmd) | Navigation diagram. |
| [`docs/diagrams/ER-Diagram.mmd`](docs/diagrams/ER-Diagram.mmd) | Schema ER. |
| [`database/README.md`](database/README.md) | Setup MySQL, DataSource JNDI Tomcat e test email locale. |

## Prerequisiti

- JDK 17
- Maven
- Tomcat 11
- MySQL
- MySQL Connector/J nella `lib` di Tomcat
- FakeSMTP opzionale per test email

## Configurazione database

1. Eseguire in ordine:
   - `database/01_schema.sql`
   - `database/02_seed_core_users.sql`
   - `database/03_seed_demo_menu.sql`
2. Configurare localmente il DataSource JNDI `jdbc/MasterEatDB` in Tomcat.
3. Consultare [`database/README.md`](database/README.md) per la guida completa.

## Build e deploy

```bash
mvn -q -DskipTests clean package
```

- WAR generato in `target/MasterEat.war`
- Deploy previsto su Tomcat 11
- Context path `/MasterEat`

## URL utili

| Risorsa | URL |
| --- | --- |
| Web application WE | `http://localhost:8080/MasterEat/` |
| Homepage | `http://localhost:8080/MasterEat/home` |
| API REST | `http://localhost:8080/MasterEat/api` |
| Client SWA | `http://localhost:8080/MasterEat/swa-client/` |
| Health API | `http://localhost:8080/MasterEat/api/health` |
| Database health | `http://localhost:8080/MasterEat/api/db-health` |

## Utenti seed

| Username | Password | Ruolo |
| --- | --- | --- |
| `test_cliente` | `password` | `CLIENTE` |
| `test_staff` | `password` | `PERSONALE` |
| `test_owner` | `password` | `PROPRIETARIO` |

Le password degli utenti seed sono persistite come hash BCrypt.

## Email locali con FakeSMTP

FakeSMTP è opzionale e serve solo per test locale. Avviare un server SMTP su
`localhost:2525` per intercettare le notifiche di ordine confermato e ordine in
consegna. Un errore SMTP non blocca l'operazione applicativa. Dettagli in
[`database/README.md`](database/README.md).
