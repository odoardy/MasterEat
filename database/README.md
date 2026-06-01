# MasterEat database scripts

Questa cartella contiene gli script MySQL da eseguire in ordine per creare
un ambiente locale pulito con schema, utenti minimi e menu demo.

## Ordine di esecuzione

1. `01_schema.sql`
   - Crea il database `mastereat`.
   - Crea tutte le tabelle e i vincoli.
   - Non inserisce utenti, menu, ordini, sessioni o notifiche.

2. `02_seed_core_users.sql`
   - Inserisce solo gli utenti minimi usati dal client SWA e dai test locali.
   - Non cancella utenti registrati manualmente se rieseguito su un DB non pulito.

3. `03_seed_demo_menu.sql`
   - Inserisce categorie, prodotti, ingredienti, associazioni ingredienti-prodotto,
     gruppi caratteristiche e caratteristiche prodotto.
   - Non inserisce utenti, ordini, righe ordine, sessioni API, storico stati o notifiche.

## Credenziali seed

Tutti gli utenti seed usano la password `password`, salvata con BCrypt come
previsto da `PasswordHasher`.

| Username | Password | Ruolo |
| --- | --- | --- |
| `test_cliente` | `password` | `CLIENTE` |
| `test_staff` | `password` | `PERSONALE` |
| `test_owner` | `password` | `PROPRIETARIO` |

Su un database creato da zero con questi script ci sarà un solo utente
`PROPRIETARIO`: `test_owner`.

## Configurazione DataSource Tomcat

L'applicazione ottiene le connessioni tramite il DataSource JNDI
`java:comp/env/jdbc/MasterEatDB`. Le credenziali non devono essere salvate nel
repository: configurarle solo nel Tomcat locale.

Creare localmente il file:

```text
$CATALINA_BASE/conf/Catalina/localhost/MasterEat.xml
```

con una risorsa `jdbc/MasterEatDB` simile alla seguente:

```xml
<Context>
    <Resource
        name="jdbc/MasterEatDB"
        auth="Container"
        type="javax.sql.DataSource"
        driverClassName="com.mysql.cj.jdbc.Driver"
        url="jdbc:mysql://localhost:3306/mastereat?useSSL=false&amp;allowPublicKeyRetrieval=true&amp;serverTimezone=Europe/Rome"
        username="INSERIRE_UTENTE_LOCALE"
        password="INSERIRE_PASSWORD_LOCALE"
        maxTotal="20"
        maxIdle="10"
        maxWaitMillis="10000"
    />
</Context>
```

Il MySQL Connector/J deve essere disponibile nella `lib` di Tomcat, ad esempio
in `$CATALINA_BASE/lib` o `$CATALINA_HOME/lib`, per permettere al container di
creare il pool JNDI.

## Test email con FakeSMTP

MasterEat invia email di test tramite SMTP locale verso `localhost:2525`.
FakeSMTP deve essere avviato esternamente: non è una dipendenza Maven del
progetto e le email non vengono spedite realmente, ma intercettate localmente.

Configurazione predefinita:

- host SMTP: `localhost`
- porta SMTP: `2525`
- autenticazione: disattivata
- STARTTLS: disattivato
- mittente: `noreply@mastereat.local`
- timeout SMTP: `2000 ms`

È possibile sovrascrivere la configurazione con system properties Java o
variabili d'ambiente:

- `MASTEREAT_SMTP_HOST`
- `MASTEREAT_SMTP_PORT`
- `MASTEREAT_MAIL_FROM`
- `MASTEREAT_MAIL_ENABLED`

Flusso manuale di verifica:

1. Avviare FakeSMTP su `localhost:2525`.
2. Avviare Tomcat con MasterEat.
3. Accedere come cliente, creare e confermare un ordine.
4. Verificare in FakeSMTP la mail `ORDINE_CONFERMATO`.
5. Accedere come staff/proprietario e avanzare l'ordine fino a `IN_CONSEGNA`.
6. Verificare in FakeSMTP la mail `ORDINE_IN_CONSEGNA`.
7. Spegnere FakeSMTP e ripetere una conferma o un passaggio a `IN_CONSEGNA`:
   l'operazione applicativa deve riuscire, l'errore deve essere loggato e la
   notifica deve risultare `FALLITA` in `notifiche_email`.

## Note per MySQL Workbench

- Aprire ed eseguire gli script nell'ordine indicato.
- Se uno script fallisce dentro una transazione, eseguire `ROLLBACK;` prima di
  rilanciarlo nella stessa connessione.
- Gli script seed usano tabelle temporanee e le eliminano con
  `DROP TEMPORARY TABLE IF EXISTS`, quindi sono pensati per essere rilanciati
  in locale senza duplicare i dati gestiti dagli script.
- `01_schema.sql` ricrea le tabelle con `DROP TABLE IF EXISTS`: usarlo solo per
  un setup pulito o quando si vuole azzerare il database locale.

