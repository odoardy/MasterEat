-- ============================================================
-- MasterEat database setup
-- File 01/03: schema only.
--
-- Execution order for a clean local setup:
-- 1. 01_schema.sql
-- 2. 02_seed_core_users.sql
-- 3. 03_seed_demo_menu.sql
--
-- This file creates the database and tables only.
-- It does not insert seed users, menu catalog data, orders,
-- sessions, notifications, or manual test data.
-- ============================================================

CREATE DATABASE IF NOT EXISTS mastereat
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE mastereat;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS notifiche_email;
DROP TABLE IF EXISTS storico_stati_ordine;
DROP TABLE IF EXISTS caratteristiche_riga_ordine;
DROP TABLE IF EXISTS righe_ordine;
DROP TABLE IF EXISTS ordini;
DROP TABLE IF EXISTS ingredienti_prodotto;
DROP TABLE IF EXISTS ingredienti;
DROP TABLE IF EXISTS caratteristiche;
DROP TABLE IF EXISTS gruppi_caratteristiche;
DROP TABLE IF EXISTS immagini_prodotto;
DROP TABLE IF EXISTS prodotti;
DROP TABLE IF EXISTS categorie_prodotto;
DROP TABLE IF EXISTS sessioni_api;
DROP TABLE IF EXISTS utenti;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================

-- UTENTI
-- Tabella unica per cliente, personale e proprietario.
-- Il campo ruolo distingue le tipologie di utente.
-- ============================================================

CREATE TABLE utenti (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
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

  creato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  UNIQUE KEY uq_utenti_username (username),
  UNIQUE KEY uq_utenti_email (email),

  KEY idx_utenti_ruolo (ruolo),
  KEY idx_utenti_attivo (attivo),

  CONSTRAINT chk_cliente_dati_consegna
    CHECK (
      ruolo <> 'CLIENTE'
      OR (
        telefono IS NOT NULL
        AND indirizzo IS NOT NULL
        AND citta IS NOT NULL
      )
    )
);

-- ============================================================
-- SESSIONI API
-- Serve per login/logout REST.
-- Nel client circola il token reale; nel DB salviamo solo hash.
-- ============================================================

CREATE TABLE sessioni_api (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_utente BIGINT UNSIGNED NOT NULL,
  token_hash CHAR(64) NOT NULL,
  creato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  scade_il DATETIME NOT NULL,
  revocato_il DATETIME NULL,

  PRIMARY KEY (id),

  UNIQUE KEY uq_sessioni_api_token_hash (token_hash),
  KEY idx_sessioni_api_utente (id_utente),
  KEY idx_sessioni_api_scade_il (scade_il),
  KEY idx_sessioni_api_revocato_il (revocato_il),

  CONSTRAINT fk_sessioni_api_utente
    FOREIGN KEY (id_utente)
    REFERENCES utenti(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT chk_sessioni_api_scadenza
    CHECK (scade_il > creato_il)
);

-- ============================================================
-- CATEGORIE PRODOTTO
-- Servono per organizzare il menu.
-- Esempi: Pizze, Bevande, Dolci.
-- ============================================================

CREATE TABLE categorie_prodotto (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  nome VARCHAR(100) NOT NULL,
  descrizione TEXT NULL,
  ordine_visualizzazione INT UNSIGNED NOT NULL DEFAULT 0,
  attiva BOOLEAN NOT NULL DEFAULT TRUE,

  creato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  UNIQUE KEY uq_categorie_prodotto_nome (nome),
  KEY idx_categorie_prodotto_attiva_ordine (attiva, ordine_visualizzazione)
);

-- ============================================================
-- PRODOTTI
-- Contiene i dati visibili al cliente e alcuni dati interni.
-- descrizione_preparazione è interna.
-- ============================================================

CREATE TABLE prodotti (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_categoria BIGINT UNSIGNED NULL,

  nome VARCHAR(150) NOT NULL,
  descrizione TEXT NOT NULL,

  prezzo_base DECIMAL(10,2) NOT NULL,
  minuti_preparazione INT UNSIGNED NOT NULL,
  descrizione_preparazione TEXT NULL,

  attivo BOOLEAN NOT NULL DEFAULT TRUE,

  creato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  KEY idx_prodotti_categoria (id_categoria),
  KEY idx_prodotti_nome (nome),
  KEY idx_prodotti_prezzo_base (prezzo_base),
  KEY idx_prodotti_attivo_categoria (attivo, id_categoria),
  FULLTEXT KEY ft_prodotti_nome_descrizione (nome, descrizione),

  CONSTRAINT fk_prodotti_categoria
    FOREIGN KEY (id_categoria)
    REFERENCES categorie_prodotto(id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,

  CONSTRAINT chk_prodotti_prezzo_base
    CHECK (prezzo_base >= 0),

  CONSTRAINT chk_prodotti_minuti_preparazione
    CHECK (minuti_preparazione > 0)
);

-- ============================================================
-- IMMAGINI PRODOTTO
-- Supporta multipart/form-data.
-- Il file sta su disco; qui salviamo metadati e percorso.
-- ============================================================

CREATE TABLE immagini_prodotto (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_prodotto BIGINT UNSIGNED NOT NULL,

  nome_file_originale VARCHAR(255) NOT NULL,
  nome_file_salvato VARCHAR(255) NOT NULL,
  percorso_file VARCHAR(500) NOT NULL,

  tipo_contenuto VARCHAR(100) NOT NULL,
  dimensione_byte BIGINT UNSIGNED NOT NULL,

  testo_alternativo VARCHAR(255) NULL,
  ordine_visualizzazione INT UNSIGNED NOT NULL DEFAULT 0,
  principale BOOLEAN NOT NULL DEFAULT FALSE,

  caricata_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  UNIQUE KEY uq_immagini_prodotto_percorso (percorso_file),
  KEY idx_immagini_prodotto_prodotto (id_prodotto),
  KEY idx_immagini_prodotto_ordine (id_prodotto, ordine_visualizzazione),
  KEY idx_immagini_prodotto_principale (id_prodotto, principale),

  CONSTRAINT fk_immagini_prodotto_prodotto
    FOREIGN KEY (id_prodotto)
    REFERENCES prodotti(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT chk_immagini_prodotto_dimensione
    CHECK (dimensione_byte > 0),

  CONSTRAINT chk_immagini_prodotto_tipo
    CHECK (tipo_contenuto IN ('image/jpeg', 'image/png', 'image/webp', 'image/gif'))
);

-- ============================================================
-- GRUPPI CARATTERISTICHE
-- Rappresentano gruppi di mutua esclusione.
-- Esempio: gruppo "Impasto": normale / integrale.
-- ============================================================

CREATE TABLE gruppi_caratteristiche (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_prodotto BIGINT UNSIGNED NOT NULL,

  nome VARCHAR(100) NOT NULL,
  descrizione TEXT NULL,

  obbligatorio BOOLEAN NOT NULL DEFAULT FALSE,
  attivo BOOLEAN NOT NULL DEFAULT TRUE,

  creato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  UNIQUE KEY uq_gruppi_caratteristiche_prodotto_nome (id_prodotto, nome),
  UNIQUE KEY uq_gruppi_caratteristiche_prodotto_id (id_prodotto, id),

  KEY idx_gruppi_caratteristiche_prodotto (id_prodotto),
  KEY idx_gruppi_caratteristiche_attivo (attivo),

  CONSTRAINT fk_gruppi_caratteristiche_prodotto
    FOREIGN KEY (id_prodotto)
    REFERENCES prodotti(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

-- ============================================================
-- CARATTERISTICHE
-- Opzioni selezionabili per un prodotto.
-- id_gruppo_caratteristiche NULL = caratteristica libera.
-- id_gruppo_caratteristiche NOT NULL = scelta mutuamente esclusiva.
-- ============================================================

CREATE TABLE caratteristiche (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_prodotto BIGINT UNSIGNED NOT NULL,
  id_gruppo_caratteristiche BIGINT UNSIGNED NULL,

  nome VARCHAR(100) NOT NULL,
  descrizione TEXT NULL,

  differenza_prezzo DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  selezionata_default BOOLEAN NOT NULL DEFAULT FALSE,
  attiva BOOLEAN NOT NULL DEFAULT TRUE,

  creato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  UNIQUE KEY uq_caratteristiche_prodotto_nome (id_prodotto, nome),

  KEY idx_caratteristiche_prodotto (id_prodotto),
  KEY idx_caratteristiche_gruppo (id_gruppo_caratteristiche),
  KEY idx_caratteristiche_attiva (attiva),
  KEY idx_caratteristiche_default (id_prodotto, selezionata_default),

  CONSTRAINT fk_caratteristiche_prodotto
    FOREIGN KEY (id_prodotto)
    REFERENCES prodotti(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_caratteristiche_gruppo_stesso_prodotto
    FOREIGN KEY (id_prodotto, id_gruppo_caratteristiche)
    REFERENCES gruppi_caratteristiche(id_prodotto, id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

-- ============================================================
-- INGREDIENTI
-- Ingredienti generali riutilizzabili da più prodotti.
-- ============================================================

CREATE TABLE ingredienti (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

  nome VARCHAR(120) NOT NULL,
  unita_misura VARCHAR(20) NOT NULL,
  allergene BOOLEAN NOT NULL DEFAULT FALSE,
  attivo BOOLEAN NOT NULL DEFAULT TRUE,

  creato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  aggiornato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  UNIQUE KEY uq_ingredienti_nome (nome),
  KEY idx_ingredienti_attivo (attivo),
  KEY idx_ingredienti_allergene (allergene),

  CONSTRAINT chk_ingredienti_unita_misura
    CHECK (unita_misura <> '')
);

-- ============================================================
-- INGREDIENTI PRODOTTO
-- Relazione molti-a-molti tra prodotti e ingredienti.
-- Include la quantità richiesta per la preparazione.
-- ============================================================

CREATE TABLE ingredienti_prodotto (
  id_prodotto BIGINT UNSIGNED NOT NULL,
  id_ingrediente BIGINT UNSIGNED NOT NULL,

  quantita DECIMAL(10,3) NOT NULL,

  PRIMARY KEY (id_prodotto, id_ingrediente),

  KEY idx_ingredienti_prodotto_ingrediente (id_ingrediente),

  CONSTRAINT fk_ingredienti_prodotto_prodotto
    FOREIGN KEY (id_prodotto)
    REFERENCES prodotti(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_ingredienti_prodotto_ingrediente
    FOREIGN KEY (id_ingrediente)
    REFERENCES ingredienti(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT chk_ingredienti_prodotto_quantita
    CHECK (quantita > 0)
);

-- ============================================================
-- ORDINI
-- BOZZA = carrello non ancora confermato.
-- INSERITO = ordine confermato dal cliente.
-- ANNULLATO = ordine annullato logicamente, non cancellato.
-- ============================================================

CREATE TABLE ordini (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_cliente BIGINT UNSIGNED NOT NULL,

  stato ENUM(
    'BOZZA',
    'INSERITO',
    'IN_PREPARAZIONE',
    'PRONTO',
    'IN_CONSEGNA',
    'CONSEGNATO',
    'ANNULLATO'
  ) NOT NULL DEFAULT 'BOZZA',

  creato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  confermato_il DATETIME NULL,

  orario_consegna_richiesto DATETIME NULL,
  minuti_consegna_stimati INT UNSIGNED NULL,
  distanza_consegna_km DECIMAL(8,2) NULL,

  prezzo_totale DECIMAL(10,2) NOT NULL DEFAULT 0.00,

  indirizzo_consegna_snapshot VARCHAR(255) NOT NULL,
  citta_consegna_snapshot VARCHAR(100) NOT NULL,
  cap_consegna_snapshot VARCHAR(20) NULL,
  telefono_consegna_snapshot VARCHAR(30) NOT NULL,

  annullato_il DATETIME NULL,
  motivo_annullamento TEXT NULL,

  PRIMARY KEY (id),

  KEY idx_ordini_cliente (id_cliente),
  KEY idx_ordini_stato_creato_il (stato, creato_il),
  KEY idx_ordini_creato_il (creato_il),
  KEY idx_ordini_orario_consegna (orario_consegna_richiesto),

  CONSTRAINT fk_ordini_cliente
    FOREIGN KEY (id_cliente)
    REFERENCES utenti(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT chk_ordini_prezzo_totale
    CHECK (prezzo_totale >= 0),

  CONSTRAINT chk_ordini_minuti_consegna
    CHECK (minuti_consegna_stimati IS NULL OR minuti_consegna_stimati > 0),

  CONSTRAINT chk_ordini_distanza_consegna
    CHECK (distanza_consegna_km IS NULL OR distanza_consegna_km >= 0),

  CONSTRAINT chk_ordini_annullato
    CHECK (
      stato <> 'ANNULLATO'
      OR annullato_il IS NOT NULL
    )
);

-- ============================================================
-- RIGHE ORDINE
-- Prodotti inseriti in un ordine.
-- I campi snapshot conservano nome/prezzo/tempo al momento dell'ordine.
-- ============================================================

CREATE TABLE righe_ordine (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_ordine BIGINT UNSIGNED NOT NULL,
  id_prodotto BIGINT UNSIGNED NOT NULL,

  nome_prodotto_snapshot VARCHAR(150) NOT NULL,
  prezzo_base_snapshot DECIMAL(10,2) NOT NULL,
  minuti_preparazione_snapshot INT UNSIGNED NOT NULL,

  quantita INT UNSIGNED NOT NULL DEFAULT 1,
  totale_riga DECIMAL(10,2) NOT NULL DEFAULT 0.00,

  creata_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),

  KEY idx_righe_ordine_ordine (id_ordine),
  KEY idx_righe_ordine_prodotto (id_prodotto),

  CONSTRAINT fk_righe_ordine_ordine
    FOREIGN KEY (id_ordine)
    REFERENCES ordini(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_righe_ordine_prodotto
    FOREIGN KEY (id_prodotto)
    REFERENCES prodotti(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT chk_righe_ordine_prezzo_base
    CHECK (prezzo_base_snapshot >= 0),

  CONSTRAINT chk_righe_ordine_minuti_preparazione
    CHECK (minuti_preparazione_snapshot > 0),

  CONSTRAINT chk_righe_ordine_quantita
    CHECK (quantita > 0),

  CONSTRAINT chk_righe_ordine_totale_riga
    CHECK (totale_riga >= 0)
);

-- ============================================================
-- CARATTERISTICHE RIGA ORDINE
-- Caratteristiche selezionate per una specifica riga ordine.
-- Anche qui usiamo snapshot per preservare lo storico.
-- ============================================================

CREATE TABLE caratteristiche_riga_ordine (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  id_riga_ordine BIGINT UNSIGNED NOT NULL,

  id_caratteristica BIGINT UNSIGNED NOT NULL,
  id_gruppo_caratteristiche_snapshot BIGINT UNSIGNED NULL,

  nome_caratteristica_snapshot VARCHAR(100) NOT NULL,
  differenza_prezzo_snapshot DECIMAL(10,2) NOT NULL DEFAULT 0.00,

  PRIMARY KEY (id),

  UNIQUE KEY uq_caratteristiche_riga_ordine_riga_caratteristica
    (id_riga_ordine, id_caratteristica),

  UNIQUE KEY uq_caratteristiche_riga_ordine_riga_gruppo
    (id_riga_ordine, id_gruppo_caratteristiche_snapshot),

  KEY idx_caratteristiche_riga_ordine_caratteristica (id_caratteristica),
  KEY idx_caratteristiche_riga_ordine_gruppo_snapshot (id_gruppo_caratteristiche_snapshot),

  CONSTRAINT fk_caratteristiche_riga_ordine_riga
    FOREIGN KEY (id_riga_ordine)
    REFERENCES righe_ordine(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_caratteristiche_riga_ordine_caratteristica
    FOREIGN KEY (id_caratteristica)
    REFERENCES caratteristiche(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT,

  CONSTRAINT fk_caratteristiche_riga_ordine_gruppo_snapshot
    FOREIGN KEY (id_gruppo_caratteristiche_snapshot)
    REFERENCES gruppi_caratteristiche(id)
    ON UPDATE CASCADE
    ON DELETE RESTRICT
);

-- ============================================================
-- STORICO STATI ORDINE
-- Tiene traccia di ogni cambio stato e dell'utente che lo effettua.
-- Utente = personale o proprietario; in alcuni casi anche cliente.
-- ============================================================

CREATE TABLE storico_stati_ordine (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

  id_ordine BIGINT UNSIGNED NOT NULL,

  stato_precedente ENUM(
    'BOZZA',
    'INSERITO',
    'IN_PREPARAZIONE',
    'PRONTO',
    'IN_CONSEGNA',
    'CONSEGNATO',
    'ANNULLATO'
  ) NULL,

  stato_nuovo ENUM(
    'BOZZA',
    'INSERITO',
    'IN_PREPARAZIONE',
    'PRONTO',
    'IN_CONSEGNA',
    'CONSEGNATO',
    'ANNULLATO'
  ) NOT NULL,

  id_utente_modifica BIGINT UNSIGNED NULL,
  modificato_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  nota TEXT NULL,

  PRIMARY KEY (id),

  KEY idx_storico_stati_ordine_ordine (id_ordine),
  KEY idx_storico_stati_ordine_utente (id_utente_modifica),
  KEY idx_storico_stati_ordine_data (modificato_il),
  KEY idx_storico_stati_ordine_stato_nuovo (stato_nuovo),

  CONSTRAINT fk_storico_stati_ordine_ordine
    FOREIGN KEY (id_ordine)
    REFERENCES ordini(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE,

  CONSTRAINT fk_storico_stati_ordine_utente
    FOREIGN KEY (id_utente_modifica)
    REFERENCES utenti(id)
    ON UPDATE CASCADE
    ON DELETE SET NULL,

  CONSTRAINT chk_storico_stati_ordine_cambio
    CHECK (stato_precedente IS NULL OR stato_precedente <> stato_nuovo)
);

-- ============================================================
-- NOTIFICHE EMAIL
-- Utile per tracciare email di conferma ordine e ordine in consegna.
-- ============================================================

CREATE TABLE notifiche_email (
  id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,

  id_ordine BIGINT UNSIGNED NOT NULL,
  email_destinatario VARCHAR(255) NOT NULL,

  tipo ENUM(
    'ORDINE_CONFERMATO',
    'ORDINE_IN_CONSEGNA'
  ) NOT NULL,

  oggetto VARCHAR(255) NOT NULL,

  stato ENUM('DA_INVIARE', 'INVIATA', 'FALLITA') NOT NULL DEFAULT 'DA_INVIARE',

  creata_il DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  inviata_il DATETIME NULL,
  messaggio_errore TEXT NULL,

  PRIMARY KEY (id),

  KEY idx_notifiche_email_ordine (id_ordine),
  KEY idx_notifiche_email_stato (stato),
  KEY idx_notifiche_email_tipo (tipo),

  CONSTRAINT fk_notifiche_email_ordine
    FOREIGN KEY (id_ordine)
    REFERENCES ordini(id)
    ON UPDATE CASCADE
    ON DELETE CASCADE
);

