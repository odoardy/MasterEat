USE mastereat;

SET NAMES utf8mb4;

-- MySQL Workbench puo bloccare UPDATE con JOIN quando e attivo
-- "safe update mode". Lo disattiviamo solo per questa sessione/script
-- e lo ripristiniamo alla fine.
SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- ============================================================
-- MasterEat core users seed
-- File 02/03: minimal users only.
--
-- Execution order for a clean local setup:
-- 1. 01_schema.sql
-- 2. 02_seed_core_users.sql
-- 3. 03_seed_demo_menu.sql
--
-- This file inserts only the users required by the SWA client
-- and local manual testing:
-- - test_cliente / password / CLIENTE
-- - test_staff / password / PERSONALE
-- - test_owner / password / PROPRIETARIO
--
-- It does not insert menu data, orders, sessions, notifications,
-- or extra demo users such as cliente1/personale1/proprietario.
-- It does not delete manually registered users when re-executed.
-- ============================================================

-- In Workbench, se uno script fallisce, la connessione puo restare aperta
-- con tabelle temporanee ancora presenti. Questo DROP rende sicuro rilanciare.
DROP TEMPORARY TABLE IF EXISTS tmp_seed_core_users;

-- Tutti gli aggiornamenti seed sono trattati come un blocco unico.
-- In caso di errore prima del COMMIT, eseguire ROLLBACK nella stessa sessione.
START TRANSACTION;

-- Tabella di staging: contiene la definizione attesa degli utenti seed.
-- Non e persistente e sparisce alla chiusura della connessione MySQL.
CREATE TEMPORARY TABLE tmp_seed_core_users (
  username VARCHAR(50) NOT NULL,
  email VARCHAR(255) NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  nome VARCHAR(80) NOT NULL,
  cognome VARCHAR(80) NOT NULL,
  telefono VARCHAR(30) NULL,
  indirizzo VARCHAR(255) NULL,
  citta VARCHAR(100) NULL,
  cap VARCHAR(20) NULL,
  ruolo ENUM('CLIENTE', 'PERSONALE', 'PROPRIETARIO') NOT NULL,
  attivo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (username)
);

INSERT INTO tmp_seed_core_users (
  username,
  email,
  password_hash,
  nome,
  cognome,
  telefono,
  indirizzo,
  citta,
  cap,
  ruolo,
  attivo
) VALUES
(
  'test_cliente',
  'test_cliente@example.com',
  '$2a$12$ITZo1wJ4V6rcu1GC6UU0au7XPpgAKL4AakHRR/XGQ39rh1TdGFMjW',
  'Test',
  'Cliente',
  '+392222222222',
  'Via Test 20',
  'L''Aquila',
  '67100',
  'CLIENTE',
  TRUE
),
(
  'test_staff',
  'test_staff@example.com',
  '$2a$12$/UGC455yjGJz4lXYsIjoYejoMZEJn4dpOfkudWPlcGf1dVHZx.nZK',
  'Test',
  'Staff',
  NULL,
  NULL,
  NULL,
  NULL,
  'PERSONALE',
  TRUE
),
(
  'test_owner',
  'test_owner@example.com',
  '$2a$12$RmHUVVfdyyyPPQt0C49Wn.8yoZm9pRRqdnYjClI0UnQCq3SMNeWgi',
  'Test',
  'Owner',
  NULL,
  NULL,
  NULL,
  NULL,
  'PROPRIETARIO',
  TRUE
);

-- Aggiorna gli utenti seed gia presenti usando username come chiave logica.
-- If the target email is already used by another user, keep the current
-- email to avoid overwriting or deleting manually registered accounts.
UPDATE utenti seed_users
JOIN tmp_seed_core_users core_users
  ON core_users.username = seed_users.username
LEFT JOIN utenti email_owner
  ON email_owner.email = core_users.email
 AND email_owner.username <> core_users.username
SET
  seed_users.email = CASE
    WHEN email_owner.id IS NULL THEN core_users.email
    ELSE seed_users.email
  END,
  seed_users.password_hash = core_users.password_hash,
  seed_users.nome = core_users.nome,
  seed_users.cognome = core_users.cognome,
  seed_users.telefono = core_users.telefono,
  seed_users.indirizzo = core_users.indirizzo,
  seed_users.citta = core_users.citta,
  seed_users.cap = core_users.cap,
  seed_users.ruolo = core_users.ruolo,
  seed_users.attivo = core_users.attivo,
  seed_users.aggiornato_il = CURRENT_TIMESTAMP;

-- Inserisce gli utenti seed mancanti solo quando username ed email sono liberi.
-- Questo evita di cancellare o sovrascrivere utenti creati manualmente.
INSERT INTO utenti (
  username,
  email,
  password_hash,
  nome,
  cognome,
  telefono,
  indirizzo,
  citta,
  cap,
  ruolo,
  attivo
)
SELECT
  core_users.username,
  core_users.email,
  core_users.password_hash,
  core_users.nome,
  core_users.cognome,
  core_users.telefono,
  core_users.indirizzo,
  core_users.citta,
  core_users.cap,
  core_users.ruolo,
  core_users.attivo
FROM tmp_seed_core_users core_users
WHERE NOT EXISTS (
  SELECT 1
  FROM utenti existing_username
  WHERE existing_username.username = core_users.username
)
  AND NOT EXISTS (
    SELECT 1
    FROM utenti existing_email
    WHERE existing_email.email = core_users.email
  );

DROP TEMPORARY TABLE IF EXISTS tmp_seed_core_users;

-- Da qui le modifiche diventano definitive.
COMMIT;

-- Ripristina il valore di safe update mode precedente allo script.
SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

-- ============================================================
-- Verification queries
-- ============================================================

SELECT ruolo, COUNT(*) AS utenti
FROM utenti
GROUP BY ruolo;

SELECT username, email, ruolo, attivo
FROM utenti
WHERE username IN ('test_cliente', 'test_staff', 'test_owner')
ORDER BY ruolo, username;
