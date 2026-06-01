<div align="center">
  <img src="assets/logoUnivAQ.png" alt="Logo Università degli Studi dell'Aquila" width="180">
  <p><strong><big><big><big>Università degli Studi dell'Aquila</big></big></big></strong></p>
  <p><strong><big>Corso di Sviluppo Web Avanzato [DT0209]</big></strong></p>
  <p><strong><big>A.A. 2025/2026</big></strong></p>
</div>

---

<div align="center">

<p>
  <strong><big><big><big>MasterEat API - Documentazione</big></big></big></strong>
</p>  

<p>
  <strong>Repository ufficiale:
  <a href="https://github.com/odoardy/MasterEat.git">https://github.com/odoardy/MasterEat.git</a></strong>
</p>

<p><strong>Membri del team:</strong></p>

<table>
  <thead>
    <tr>
      <th>Studente</th>
      <th>Matricola</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Davide Odoardi</td>
      <td>292216</td>
    </tr>
    <tr>
      <td>Alessandro Marinucci</td>
      <td>261682</td>
    </tr>
    <tr>
      <td>Antonino Campellone</td>
      <td>292594</td>
    </tr>
  </tbody>
</table>

</div>

---

## Contributi dei membri del gruppo

Il progetto è stato sviluppato in collaborazione tra tutti i membri del gruppo. Le aree indicate rappresentano i contributi prevalenti, non una separazione esclusiva delle attività svolte.

| Membro | Contributo prevalente |
| --- | --- |
| **Davide Odoardi** | Architettura complessiva del progetto, sviluppo backend e integrazione delle componenti della web application e dei servizi RESTful. |
| **Antonino Campellone** | Interfaccia web, template HTML/FreeMarker e rifinitura del layout CSS responsive. |
| **Alessandro Marinucci** | Schema dati, predisposizione dei contenuti demo e supporto ai test funzionali. |

Le principali scelte progettuali e le verifiche finali sono state discusse congiuntamente dal gruppo.

## 1. Introduzione

MasterEat è un servizio RESTful per una piattaforma di ristorazione e consegna. Il progetto complessivo integra attività di Web Engineering e Sviluppo Web Avanzato; questa relazione riguarda esclusivamente la parte di Sviluppo Web Avanzato.

Il lavoro SWA si concentra sulla realizzazione di API REST, sulla documentazione del contratto tramite OpenAPI, su un client JavaScript minimale per il test delle operazioni principali e sulla verifica manuale dei flussi applicativi richiesti dalla specifica WebDelivery Services.

## 2. Obiettivi della parte SWA

La parte SWA implementa le API REST richieste dalla specifica WebDelivery Services. Gli obiettivi principali sono:

- progettare endpoint RESTful coerenti con risorse e collezioni;
- gestire autenticazione tramite token interno;
- esporre operazioni su menu, prodotti e caratteristiche;
- permettere la creazione e la gestione degli ordini;
- gestire gli stati dell'ordine e il relativo storico;
- documentare le API in formato OpenAPI;
- fornire un client JavaScript semplice per testare alcune operazioni principali.

## 3. Tecnologie e dipendenze

Le tecnologie riportate derivano dal `pom.xml`, dallo schema SQL e dal client statico presente nel progetto.

| Area | Tecnologia / dipendenza | Uso nel progetto |
| --- | --- | --- |
| Server | Java 17 | Versione impostata da `maven.compiler.release`. |
| Server | Maven WAR | Packaging dell'applicazione come archivio WAR, con `finalName` pari a `MasterEat`. |
| Server | Tomcat 11 | Ambiente di deploy previsto per WAR Jakarta Servlet 6. |
| Server | Jakarta Servlet API 6.0.0 | API servlet con scope `provided`, fornita dal container. |
| Server | Jakarta Servlet Filter | Filtro CORS dedicato alle API REST sotto `/api/*`. |
| Server | Jersey 3.1.11 | Implementazione JAX-RS/Jakarta REST tramite `jersey-container-servlet`. |
| Server | Jersey HK2 3.1.11 | Supporto all'iniezione/integrazione Jersey. |
| Server | Jersey JSON Jackson 3.1.11 | Serializzazione e deserializzazione JSON nelle API. |
| Server | Jackson | Usato attraverso il provider JSON di Jersey. La property `jackson.version` è presente, ma il `pom.xml` non dichiara una dipendenza Jackson diretta separata. |
| Server | org.mindrot:jbcrypt:0.4 | Hashing delle password utente con BCrypt, salt per hash e costo adattivo. |
| Server | MySQL Connector/J 9.7.0 | Driver JDBC per l'accesso a MySQL. |
| Database | MySQL | Database relazionale definito dagli script SQL nella cartella `database/`. |
| Client | HTML, CSS, JavaScript vanilla | Client statico in `src/main/webapp/swa-client/`. |
| Client | Fetch API | Invio delle richieste HTTP dal client SWA. |
| Client | Nessun framework frontend | Non sono presenti React, Vite o librerie frontend. |
| Documentazione | OpenAPI 3.0.3 | Specifica in `docs/OpenAPI.yaml`. |
| Documentazione | Swagger Editor | Strumento indicato per lettura e validazione della specifica. |

## 4. Architettura generale

L'architettura applicativa segue una separazione a livelli:

```text
Client HTTP / Client SWA
        |
CorsFilter (/api/*)
        |
JAX-RS Resource
        |
Service
        |
DAO interface
        |
DAO implementation JDBC
        |
MySQL Database
```

Le classi `Resource` nel package `it.univaq.mastereat.api.resources` espongono gli endpoint REST tramite annotazioni JAX-RS. I `Service` contengono la logica applicativa, i controlli principali, le regole sui ruoli e le verifiche di stato. I `DAO` definiscono le operazioni di accesso ai dati, mentre le classi in `dao.impl` implementano tali operazioni usando JDBC e MySQL.

I `DTO` rappresentano gli oggetti usati per request e response JSON. I `model` rappresentano le entità interne del dominio e riflettono più da vicino la struttura dati applicativa. Le classi in `util` e `config` gestiscono aspetti trasversali: token, hash password, connessione al database e configurazione JAX-RS. La classe `MasterEatApplication` registra il package delle risorse REST e il supporto Jackson; il mapping `/api/*` è configurato in `WEB-INF/web.xml`.

Le API condividono il DataSource JNDI dell'applicazione (`jdbc/MasterEatDB`). La configurazione Tomcat completa è descritta in `docs/DocumentazioneWE.md`, mentre l'esempio operativo è disponibile in `database/README.md`.

Il filtro `it.univaq.mastereat.api.CorsFilter` è un `jakarta.servlet.Filter` annotato con `@WebFilter(filterName = "CorsFilter", urlPatterns = "/api/*")`. Intercetta solo le API REST/SWA pubblicate sotto `/api/*`: la webapp session-based non passa da questo filtro e mantiene il proprio comportamento separato.

## 5. Autenticazione e autorizzazione

L'autenticazione avviene tramite login con username e password su `POST /auth/login`. In caso di credenziali valide, il server genera un token interno opaco e lo restituisce al client, che lo invia nelle richieste protette tramite header `Authorization: Bearer <token>`. Il token viene validato lato server e associato all'utente autenticato e al relativo ruolo applicativo. Il logout avviene tramite `POST /auth/logout` e comporta la revoca logica della sessione nella tabella `sessioni_api`.

Le password utente sono salvate con BCrypt tramite `org.mindrot:jbcrypt`: ogni hash include un salt e usa un costo computazionale configurabile per rendere più onerosi i tentativi di brute force.

I ruoli previsti sono:

- `CLIENTE`;
- `PERSONALE`;
- `PROPRIETARIO`.

I casi principali sono:

- `401 Unauthorized`: token mancante, scaduto, revocato o non valido;
- `403 Forbidden`: utente autenticato ma ruolo non autorizzato o accesso a risorsa non propria;
- il cliente opera sui propri ordini dove previsto;
- personale e proprietario accedono alle operazioni gestionali;
- alcune operazioni sono più restrittive, ad esempio la disattivazione di caratteristiche è riservata al proprietario.

## 6. CORS e richieste preflight

La Same-Origin Policy dei browser limita le richieste JavaScript verso origini diverse da quella della pagina. CORS consente al server di dichiarare esplicitamente quali richieste cross-origin sono ammesse, tramite header HTTP restituiti nelle risposte.

Per alcune richieste cross-origin il browser invia prima una richiesta di preflight con metodo `OPTIONS`, indicando il metodo e gli header che vorrebbe usare nella richiesta reale. Il filtro CORS intercetta queste richieste sotto `/api/*` e risponde direttamente con `204 No Content`, senza inoltrare la richiesta a Jersey.

Gli header CORS configurati per le risposte API sono:

- `Access-Control-Allow-Origin: *`;
- `Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS`;
- `Access-Control-Allow-Headers: Authorization, Content-Type, Accept`;
- `Access-Control-Max-Age: 3600`.

Il filtro non abilita `Access-Control-Allow-Credentials`: non è quindi previsto il supporto a credenziali cross-origin basate su cookie. L'autenticazione delle API resta basata sul token interno inviato tramite `Authorization: Bearer <token>`.

Il supporto CORS riguarda solo le API REST sotto `/api/*` e non la webapp, che resta session-based e separata. Il client incluso in `/swa-client/` è servito dalla stessa origine del backend e non dipende da CORS, ma il filtro permette anche test e integrazioni da client JavaScript esterni.

## 7. DTO e separazione dai model

Il progetto usa DTO separati dai model per rappresentare il contratto JSON dell'API. I DTO permettono di distinguere la forma pubblica delle request e response dalla struttura interna delle entità di dominio.

Questa scelta consente di:

- evitare l'esposizione diretta delle tabelle o dei model interni;
- controllare i campi restituiti nelle response;
- rendere la specifica OpenAPI più chiara;
- separare l'API pubblica dall'implementazione interna.

La maggior parte delle API principali SWA utilizza DTO dedicati. Eventuali endpoint di supporto non centrali possono usare strutture interne più semplici: ad esempio `GET /prodotti/{idProdotto}/caratteristiche` restituisce una lista di `Caratteristica`.

## 8. Elenco funzionalità realizzate

| N. | Operazione richiesta | Endpoint implementato | Metodo HTTP | Autenticazione/ruolo |
| --- | --- | --- | --- | --- |
| 1 | Login/logout con username e password | `/auth/login`, `/auth/logout` | `POST` | Login pubblico; logout con token |
| 2 | Lettura menu completo | `/menu` | `GET` | Pubblico |
| 3 | Ricerca prodotti nel menu per nome e/o fascia prezzo | `/prodotti?nome=&prezzoMin=&prezzoMax=` | `GET` | Pubblico |
| 4 | Eliminazione/disattivazione caratteristica associata a prodotto | `/prodotti/{idProdotto}/caratteristiche/{idCaratteristica}` | `DELETE` | `PROPRIETARIO` |
| 5 | Inserimento prodotto in ordine con validazione delle caratteristiche | `/ordini/{idOrdine}/prodotti` | `POST` | `CLIENTE` proprietario dell'ordine |
| 6 | Totale ordine e tempo stimato con due endpoint distinti | `/ordini/{idOrdine}/totale`, `/ordini/{idOrdine}/tempo-consegna` | `GET` | `CLIENTE` proprietario dell'ordine |
| 7 | Aggiornamento stato ordine | `/ordini/{idOrdine}/stato` | `PUT` | `PERSONALE` o `PROPRIETARIO` |
| 8 | Lista ordini filtrati per data inserimento e stato corrente | `/ordini?stato=&dataDa=&dataA=` | `GET` | `PERSONALE` o `PROPRIETARIO` |
| 9 | Ordini effettuati da un determinato utente | `/utenti/{idUtente}/ordini` | `GET` | `CLIENTE` proprietario o `PROPRIETARIO` |
| 10 | Ingredienti necessari per un prodotto | `/prodotti/{idProdotto}/ingredienti` | `GET` | `PERSONALE` o `PROPRIETARIO` |
| 11 | Operatori coinvolti nella gestione di un ordine | `/ordini/{idOrdine}/operatori` | `GET` | `PERSONALE` o `PROPRIETARIO` |
| 12 | Annullamento/eliminazione logica ordine | `/ordini/{idOrdine}` | `DELETE` | `CLIENTE` proprietario dell'ordine |
| 13 | Prodotti presenti in un ordine con eventuali caratteristiche | `/ordini/{idOrdine}/prodotti` | `GET` | `CLIENTE` proprietario dell'ordine |

Tutte le operazioni richieste dalla specifica SWA risultano implementate. Gli endpoint indicati nella sezione successiva sono considerati di supporto al flusso applicativo e non sostituiscono le operazioni richieste.

## 9. Endpoint di supporto o extra documentati

Oltre alle 13 operazioni richieste, la specifica OpenAPI documenta anche alcuni endpoint di supporto e diagnostica:

| Endpoint | Metodo | Scopo |
| --- | --- | --- |
| `/prodotti/{id}` | `GET` | Dettaglio pubblico di un prodotto. |
| `/prodotti/{idProdotto}/caratteristiche` | `GET` | Caratteristiche attive disponibili per un prodotto. |
| `/ordini` | `POST` | Creazione di un ordine in stato `BOZZA`. |
| `/ordini/{idOrdine}/conferma` | `POST` | Conferma di un ordine in bozza, con passaggio a `INSERITO`. |
| `/health` | `GET` | Endpoint diagnostico per la verifica applicativa dell'API Jersey. |
| `/db-health` | `GET` | Endpoint diagnostico per la verifica di connessione al database MySQL. |

Questi endpoint completano il flusso applicativo necessario per testare le operazioni principali. Non rappresentano funzionalità complesse aggiunte fuori specifica, ma supporti pratici per creazione, conferma, consultazione e diagnostica.

## 10. Scelte REST e struttura URL

Le URL sono orientate a risorse e collezioni. Esempi:

- `/prodotti`;
- `/prodotti/{idProdotto}`;
- `/prodotti/{idProdotto}/ingredienti`;
- `/prodotti/{idProdotto}/caratteristiche/{idCaratteristica}`;
- `/ordini/{idOrdine}/prodotti`;
- `/utenti/{idUtente}/ordini`.

I verbi HTTP sono usati in modo coerente con l'operazione:

- `GET` per lettura e ricerca;
- `POST` per creazione e azioni applicative;
- `PUT` per aggiornamento dello stato;
- `DELETE` per annullamento o disattivazione logica.

Alcuni endpoint rappresentano transizioni specifiche del ciclo di vita dell’ordine. Ad esempio, `/ordini/{idOrdine}/conferma` distingue la conferma dell’ordine dalla modifica generica dell’ordine, mentre `/ordini/{idOrdine}/stato` espone l’aggiornamento controllato dello stato operativo. Questa scelta rende esplicite le regole applicative associate alle transizioni e permette di separare la composizione dell’ordine dalla sua gestione operativa.

## 11. Gestione stati ordine

Gli stati dell'ordine previsti dal model `StatoOrdine` e dallo schema SQL sono:

- `BOZZA`;
- `INSERITO`;
- `IN_PREPARAZIONE`;
- `PRONTO`;
- `IN_CONSEGNA`;
- `CONSEGNATO`;
- `ANNULLATO`.

`BOZZA` indica un ordine creato ma non ancora confermato. `INSERITO` indica un ordine confermato dal cliente o dal proprietario. Gli stati `IN_PREPARAZIONE`, `PRONTO`, `IN_CONSEGNA` e `CONSEGNATO` descrivono l'avanzamento operativo gestito da personale o proprietario. `ANNULLATO` rappresenta la cancellazione logica.

Le transizioni operative sono progressive:

```text
INSERITO -> IN_PREPARAZIONE -> PRONTO -> IN_CONSEGNA -> CONSEGNATO
```

Il servizio non permette salti arbitrari tra stati: per ogni stato operativo è ammesso solo lo stato successivo previsto. Lo storico stati registra:

- stato precedente;
- stato nuovo;
- utente modificatore;
- timestamp;
- nota.

## 12. Annullamento logico dell'ordine

L'endpoint:

```http
DELETE /ordini/{idOrdine}
```

non elimina fisicamente il record dalla tabella `ordini`. L'operazione imposta lo stato dell'ordine a `ANNULLATO`, valorizza `annullato_il`, registra il motivo di annullamento e inserisce una riga nello storico stati.

L'annullamento è ammesso solo per ordini in stato `BOZZA` o `INSERITO` e solo dal cliente proprietario dell'ordine. Questa scelta è coerente con la richiesta progettuale: l'eliminazione dell'ordine è trattata come cambio di stato e non come delete fisico, preservando tracciabilità e integrità storica.

## 13. Gestione caratteristiche prodotto

Le caratteristiche sono associate ai prodotti nella tabella `caratteristiche`. L'endpoint:

```http
DELETE /prodotti/{idProdotto}/caratteristiche/{idCaratteristica}
```

disattiva logicamente la caratteristica per quel prodotto impostando `attiva = FALSE`. L'operazione è riservata al ruolo `PROPRIETARIO`.

Se la caratteristica esiste ma non è associata attivamente al prodotto, viene restituito `404 Not Found`. La motivazione è che la risorsa richiesta dall'endpoint non è la caratteristica in astratto, ma l'associazione attiva prodotto-caratteristica. Se tale associazione non esiste o è già inattiva, la risorsa richiesta non è trovata.

Nel seed SQL la caratteristica `id = 3`, "Doppia mozzarella", per il prodotto `1` è presente ma ha `attiva = FALSE`. Non deve quindi essere usata nei test come caratteristica valida per il prodotto `1`. Per il prodotto `1`, caratteristiche attive coerenti con il seed sono ad esempio `id = 1` "Impasto normale" e `id = 2` "Impasto integrale".

Quando un prodotto viene aggiunto a un ordine tramite `POST /ordini/{idOrdine}/prodotti`, la validazione è server-side: le caratteristiche devono essere attive, associate al prodotto, non duplicate e al massimo una per gruppo. Se il prodotto ha gruppi caratteristiche obbligatori, deve essere indicata una caratteristica valida per ogni gruppo obbligatorio. Se manca una scelta obbligatoria, l'API restituisce `400 Bad Request`.

## 14. Calcolo totale e tempo stimato

Il totale dell'ordine è disponibile tramite:

```http
GET /ordini/{idOrdine}/totale
```

Il tempo stimato è disponibile tramite:

```http
GET /ordini/{idOrdine}/tempo-consegna
```

Il totale considera prezzo del prodotto, quantità e differenze di prezzo delle caratteristiche selezionate. Il DAO calcola il totale usando le righe d'ordine e gli snapshot salvati al momento dell'inserimento.

Il tempo stimato è calcolato in modo prototipale come somma dei tempi di preparazione delle righe, moltiplicati per la quantità.

## 15. OpenAPI

La specifica si trova in:

```text
docs/OpenAPI.yaml
```

Il formato usato è OpenAPI `3.0.3`. Il file è consultabile e validabile con Swagger Editor e contiene:

- server locale `http://localhost:8080/MasterEat/api`;
- tag per le principali aree funzionali;
- security scheme `BearerAuth`;
- paths degli endpoint REST;
- request body;
- response e codici HTTP;
- schemas dei DTO;
- esempi JSON;
- descrizioni dei ruoli e delle principali scelte progettuali.

Per aprire la specifica:

1. aprire `https://editor.swagger.io/`;
2. copiare e incollare il contenuto di `docs/OpenAPI.yaml`;
3. in alternativa, importare il file se lo strumento lo consente.

## 16. Client JavaScript SWA

Il client SWA si trova nella cartella:

```text
src/main/webapp/swa-client/
```

I file principali sono:

- `index.html`;
- `style.css`;
- `app.js`.

L'URL locale previsto è:

```text
http://localhost:8080/MasterEat/swa-client/
```

Il client è una console REST dimostrativa per provare le API principali, non una seconda webapp applicativa. Copre:

- login/logout;
- lettura menu;
- ricerca prodotti;
- dettaglio prodotto;
- caratteristiche prodotto;
- ingredienti prodotto;
- creazione ordine in bozza;
- aggiunta prodotto all'ordine;
- righe ordine;
- totale ordine;
- tempo di consegna stimato;
- conferma ordine;
- annullamento ordine;
- ordini filtrati;
- aggiornamento stato;
- operatori ordine.

Il client usa JavaScript vanilla e la Fetch API. Salva `baseURL`, token e dati dell'utente in `localStorage`, invia l'header `Authorization: Bearer <token>` quando necessario e mostra per ogni richiesta metodo, URL, status HTTP e body JSON. Non usa framework frontend né un test client generico.

## 17. Esempi JSON principali

### Richiesta login

```json
{
  "username": "test_cliente",
  "password": "password"
}
```

### Risposta login

```json
{
  "token": "token-opaco-generato-dal-server",
  "idUtente": 6,
  "username": "test_cliente",
  "ruolo": "CLIENTE"
}
```

### Esempio menu/prodotto semplificato

```json
{
  "categorie": [
    {
      "id": 1,
      "nome": "Pizze",
      "descrizione": "Pizze classiche e speciali",
      "prodotti": [
        {
          "id": 1,
          "nome": "Pizza Margherita",
          "descrizione": "Pizza con pomodoro, mozzarella e basilico.",
          "prezzoBase": 6.50,
          "immagini": [],
          "caratteristiche": [
            {
              "id": 1,
              "nome": "Impasto normale",
              "descrizione": "Impasto classico",
              "differenzaPrezzo": 0.00,
              "selezionataDefault": true
            },
            {
              "id": 2,
              "nome": "Impasto integrale",
              "descrizione": "Impasto con farina integrale",
              "differenzaPrezzo": 1.00,
              "selezionataDefault": false
            }
          ]
        }
      ]
    }
  ],
  "prodottiSenzaCategoria": []
}
```

### Richiesta inserimento prodotto in ordine

```json
{
  "idProdotto": 1,
  "quantita": 2,
  "caratteristiche": [2]
}
```

Il campo `caratteristiche` deve contenere anche le scelte richieste dai gruppi obbligatori del prodotto. Per prodotti senza gruppi obbligatori può essere vuoto.

### Risposta errore per gruppo obbligatorio mancante

```json
{
  "errore": "Seleziona una caratteristica per il gruppo obbligatorio: Impasto."
}
```

### Risposta totale ordine

```json
{
  "idOrdine": 4,
  "totale": 15.00
}
```

### Richiesta aggiornamento stato

```json
{
  "nuovoStato": "IN_PREPARAZIONE"
}
```

### Risposta errore generico

```json
{
  "errore": "Token mancante o non valido"
}
```

## 18. Codici HTTP e gestione errori

| Codice | Significato nel progetto | Esempi |
| --- | --- | --- |
| `200 OK` | Operazione riuscita con body JSON. | Login riuscito, lettura menu, calcolo totale, aggiornamento stato, annullamento ordine. |
| `201 Created` | Risorsa creata. | Creazione ordine in bozza, inserimento prodotto in ordine. |
| `204 No Content` | Operazione riuscita senza body. | Logout riuscito, disattivazione caratteristica. |
| `400 Bad Request` | Parametri o body non validi, transizione non ammessa trattata come richiesta errata. | Prezzi non validi, stato non valido, quantità non positiva, data in formato errato, gruppo obbligatorio mancante, caratteristiche duplicate, più caratteristiche nello stesso gruppo. |
| `401 Unauthorized` | Token mancante, scaduto, revocato o non valido; credenziali errate nel login. | Logout senza token, chiamata protetta senza header, login errato. |
| `403 Forbidden` | Utente autenticato ma non autorizzato per ruolo o proprietà della risorsa. | Cliente su lista ordini operativi, personale su operazione riservata al proprietario, cliente su ordine non proprio. |
| `404 Not Found` | Risorsa non trovata o associazione attiva non presente. | Prodotto non esistente, ordine non esistente, caratteristica non associata attivamente al prodotto. |
| `500 Internal Server Error` | Errore inatteso lato server o database. | Problemi di connessione o eccezioni non gestite nei livelli DAO/Service. |

Il codice `409 Conflict` non risulta usato né documentato nella specifica OpenAPI attuale.

## 19. Test principali

| Area | Endpoint | Ruolo | Input | Risultato atteso | Codice HTTP atteso | Esito |
| --- | --- | --- | --- | --- | --- | --- |
| Autenticazione | `POST /auth/login` | Pubblico | `test_cliente` / `password` | Token e dati utente | `200` | Riuscito |
| Autenticazione | `POST /auth/login` | Pubblico | Password errata | Errore credenziali | `401` | Riuscito |
| CORS | `OPTIONS /auth/login` | Pubblico | Origin esterna, metodo richiesto `POST`, header `Content-Type, Authorization` | Header CORS e risposta senza body | `204` | Riuscito |
| Menu | `GET /menu` | Pubblico | Nessuno | Menu completo | `200` | Riuscito |
| Prodotti | `GET /prodotti` | Pubblico | `nome=pizza`, fascia prezzo | Lista prodotti filtrata | `200` | Riuscito |
| Ordini | `POST /ordini` | `CLIENTE` | Token cliente | Ordine in `BOZZA` | `201` | Riuscito |
| Ordini | `POST /ordini/{idOrdine}/prodotti` | `CLIENTE` proprietario | Prodotto 1, quantità, caratteristica attiva | Riga ordine creata | `201` | Riuscito |
| Ordini | `POST /ordini/{idOrdine}/prodotti` | `CLIENTE` proprietario | Prodotto con gruppo obbligatorio, `caratteristiche: []` | Errore gruppo obbligatorio mancante | `400` | Riuscito |
| Ordini | `POST /ordini/{idOrdine}/prodotti` | `CLIENTE` proprietario | Prodotto con una caratteristica valida per ogni gruppo obbligatorio | Riga ordine creata | `201` | Riuscito |
| Ordini | `GET /ordini/{idOrdine}/totale` | `CLIENTE` proprietario | ID ordine | Totale calcolato | `200` | Riuscito |
| Ordini | `GET /ordini/{idOrdine}/tempo-consegna` | `CLIENTE` proprietario | ID ordine | Minuti stimati | `200` | Riuscito |
| Ordini operativi | `GET /ordini?stato=&dataDa=&dataA=` | `PERSONALE` / `PROPRIETARIO` | Filtri opzionali | Lista ordini filtrata | `200` | Riuscito |
| Ordini operativi | `GET /ordini` | `CLIENTE` | Token cliente | Accesso negato | `403` | Riuscito |
| Stati | `PUT /ordini/{idOrdine}/stato` | `PERSONALE` / `PROPRIETARIO` | Transizione progressiva valida | Stato aggiornato | `200` | Riuscito |
| Stati | `PUT /ordini/{idOrdine}/stato` | `PERSONALE` / `PROPRIETARIO` | Transizione non valida | Errore transizione | `400` | Gestito dal codice |
| Ingredienti | `GET /prodotti/{idProdotto}/ingredienti` | `PERSONALE` / `PROPRIETARIO` | ID prodotto | Lista ingredienti | `200` | Riuscito |
| Ingredienti | `GET /prodotti/{idProdotto}/ingredienti` | Nessuno | Senza token | Token mancante/non valido | `401` | Riuscito |
| Caratteristiche | `DELETE /prodotti/{idProdotto}/caratteristiche/{idCaratteristica}` | `PROPRIETARIO` | Caratteristica attiva | Disattivazione logica | `204` | Riuscito |
| Caratteristiche | `DELETE /prodotti/1/caratteristiche/3` | `PROPRIETARIO` | Caratteristica seed inattiva | Associazione attiva non trovata | `404` | Riuscito |
| Annullamento | `DELETE /ordini/{idOrdine}` | `CLIENTE` proprietario | Ordine `BOZZA` o `INSERITO` | Stato `ANNULLATO` | `200` | Riuscito |
| Database | `ordini`, `storico_stati_ordine` | Verifica SQL | Ordine di test annullato | Record mantenuto e storico coerente | Non HTTP | Riuscito |
| Autenticazione | `POST /auth/logout` | Autenticato | Token valido | Sessione revocata | `204` | Riuscito |
| Client SWA | Console REST | Browser | Operazioni principali del client aggiornato | Login, catalogo, ordini cliente e staff/proprietario funzionanti | Vari | Riuscito |
| Sicurezza password | Seed utenti | Verifica SQL/file seed | Hash BCrypt diversi per utenti seed con stessa password | Hash `$2a$12$...` distinti | Non HTTP | Riuscito |

## 20. Istruzioni rapide di esecuzione

1. Avviare MySQL.
2. Verificare che lo schema del database e i dati seed siano stati caricati dagli script presenti nella cartella `database`.
3. Verificare il DataSource JNDI condiviso `jdbc/MasterEatDB` secondo `docs/DocumentazioneWE.md` e `database/README.md`.
4. Avviare Tomcat 11.
5. Effettuare il deploy del WAR `MasterEat`.
6. Usare come base API:

```text
http://localhost:8080/MasterEat/api
```

7. Aprire il client SWA:

```text
http://localhost:8080/MasterEat/swa-client/
```

8. Consultare la specifica OpenAPI caricando `docs/OpenAPI.yaml` in Swagger Editor.
9. Per verificare la compilazione:

```bash
mvn -q -DskipTests clean package
```
