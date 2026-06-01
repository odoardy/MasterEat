USE mastereat;

SET NAMES utf8mb4;

-- MySQL Workbench puo bloccare UPDATE con JOIN quando e attivo
-- "safe update mode". Lo disattiviamo solo per questa sessione/script
-- e lo ripristiniamo alla fine.
SET @OLD_SQL_SAFE_UPDATES = @@SQL_SAFE_UPDATES;
SET SQL_SAFE_UPDATES = 0;

-- In Workbench, se uno script fallisce, la connessione puo restare aperta
-- con tabelle temporanee ancora presenti. Questi DROP rendono sicuro rilanciare.
DROP TEMPORARY TABLE IF EXISTS tmp_demo_free_characteristics;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_group_characteristics;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_group_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_groups;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_product_ingredients;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_product_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_products;

-- Tutti gli aggiornamenti del menu demo sono trattati come un blocco unico.
-- In caso di errore prima del COMMIT, eseguire ROLLBACK nella stessa sessione.
START TRANSACTION;

-- ============================================================
-- MasterEat demo menu seed
-- File 03/03: catalog/menu data only.
--
-- Execution order for a clean local setup:
-- 1. 01_schema.sql
-- 2. 02_seed_core_users.sql
-- 3. 03_seed_demo_menu.sql
--
-- This file inserts only catalog/menu data:
-- categories, products, ingredients, product ingredients,
-- characteristic groups, and product characteristics.
--
-- It does not insert users, orders, order rows, order state history,
-- API sessions, email notifications, or manual test data.
--
-- Notes:
-- - No real images are required. The immagini_prodotto table is optional,
--   and the web UI already has placeholders when images are missing.
-- - It does not create or associate "Doppia mozzarella" with
--   "Pizza Margherita"; product/characteristic links use name lookups.
-- ============================================================

-- ============================================================
-- CATEGORIE
-- ============================================================

INSERT INTO categorie_prodotto (
  nome,
  descrizione,
  ordine_visualizzazione,
  attiva
) VALUES
('Pizze', 'Pizze classiche e speciali cotte al forno.', 1, TRUE),
('Panini/Burger', 'Panini caldi, burger e proposte con contorni.', 2, TRUE),
('Bowl/Insalate', 'Bowl complete e insalate fresche.', 3, TRUE),
('Dolci', 'Dessert monoporzione e dolci della casa.', 4, TRUE),
('Bevande', 'Acqua, bibite e bevande analcoliche.', 5, TRUE)
-- La colonna nome ha un vincolo UNIQUE: se la categoria esiste gia,
-- aggiorniamo i metadati invece di creare duplicati.
ON DUPLICATE KEY UPDATE
  descrizione = VALUES(descrizione),
  ordine_visualizzazione = VALUES(ordine_visualizzazione),
  attiva = TRUE,
  aggiornato_il = CURRENT_TIMESTAMP;

-- ============================================================
-- PRODOTTI
-- prodotti.nome non ha un vincolo UNIQUE nello schema: per rendere il seed
-- rieseguibile, inseriamo solo i nomi mancanti e poi aggiorniamo il primo
-- prodotto trovato per ciascun nome demo.
-- ============================================================

-- Tabella di staging: consente di scrivere il menu usando nomi leggibili
-- invece di id numerici, che possono cambiare tra un database e l'altro.
CREATE TEMPORARY TABLE tmp_demo_products (
  nome VARCHAR(150) NOT NULL,
  categoria_nome VARCHAR(100) NOT NULL,
  descrizione VARCHAR(600) NOT NULL,
  prezzo_base DECIMAL(10,2) NOT NULL,
  minuti_preparazione INT UNSIGNED NOT NULL,
  descrizione_preparazione VARCHAR(600) NULL,
  attivo BOOLEAN NOT NULL DEFAULT TRUE,
  PRIMARY KEY (nome)
);

INSERT INTO tmp_demo_products (
  nome,
  categoria_nome,
  descrizione,
  prezzo_base,
  minuti_preparazione,
  descrizione_preparazione,
  attivo
) VALUES
('Pizza Margherita', 'Pizze', 'Pizza con pomodoro, mozzarella fiordilatte, basilico e olio EVO.', 6.50, 15, 'Stendere impasto, condire con pomodoro e mozzarella, cuocere in forno.', TRUE),
('Pizza Diavola', 'Pizze', 'Pizza con pomodoro, mozzarella e salame piccante.', 8.00, 18, 'Preparazione classica con salame piccante aggiunto prima della cottura.', TRUE),
('Pizza Vegetariana', 'Pizze', 'Pizza con pomodoro, mozzarella e verdure grigliate di stagione.', 8.50, 18, 'Preparare base rossa con verdure grigliate e mozzarella.', TRUE),
('Classic Burger', 'Panini/Burger', 'Burger di manzo con cheddar, lattuga, pomodoro fresco e salsa burger.', 8.50, 14, 'Cuocere hamburger, tostare pane e assemblare con verdure e salsa.', TRUE),
('Chicken Burger', 'Panini/Burger', 'Burger con pollo croccante, lattuga, pomodoro e salsa yogurt.', 9.00, 15, 'Cuocere pollo croccante, tostare pane e completare con salsa yogurt.', TRUE),
('Veggie Burger', 'Panini/Burger', 'Burger vegetale con lattuga, pomodoro, cipolla e salsa yogurt.', 8.00, 12, 'Cuocere burger vegetale, tostare pane e assemblare con verdure.', TRUE),
('Caesar Bowl', 'Bowl/Insalate', 'Bowl con lattuga, pollo, parmigiano, crostini e salsa yogurt.', 9.50, 10, 'Assemblare ingredienti freschi e completare con salsa.', TRUE),
('Poke Mediterranea', 'Bowl/Insalate', 'Bowl con riso, tonno, ceci, pomodoro fresco, olive e feta.', 10.50, 9, 'Assemblare riso e ingredienti freddi in bowl.', TRUE),
('Insalata Greca', 'Bowl/Insalate', 'Insalata con lattuga, feta, olive, cetrioli, pomodoro fresco e cipolla.', 8.00, 7, 'Tagliare verdure fresche e completare con feta e olio EVO.', TRUE),
('Tiramisu', 'Dolci', 'Dessert al cucchiaio con mascarpone, caffe, savoiardi e cacao.', 4.50, 3, 'Porzionare dessert gia preparato e rifinire con cacao.', TRUE),
('Panna Cotta', 'Dolci', 'Panna cotta con salsa ai frutti di bosco.', 4.00, 3, 'Porzionare dessert e aggiungere salsa ai frutti di bosco.', TRUE),
('Brownie al cioccolato', 'Dolci', 'Brownie morbido al cioccolato servito a porzione.', 3.80, 4, 'Scaldare leggermente e servire.', TRUE),
('Acqua naturale', 'Bevande', 'Bottiglia di acqua naturale da 50cl.', 1.00, 1, NULL, TRUE),
('Coca-Cola 33cl', 'Bevande', 'Lattina di Coca-Cola da 33cl.', 2.50, 1, NULL, TRUE),
('Te freddo limone', 'Bevande', 'Bottiglia di te freddo al limone da 50cl.', 2.50, 1, NULL, TRUE);

INSERT INTO prodotti (
  id_categoria,
  nome,
  descrizione,
  prezzo_base,
  minuti_preparazione,
  descrizione_preparazione,
  attivo
)
SELECT
  c.id,
  p.nome,
  p.descrizione,
  p.prezzo_base,
  p.minuti_preparazione,
  p.descrizione_preparazione,
  p.attivo
FROM tmp_demo_products p
JOIN categorie_prodotto c
  ON c.nome = p.categoria_nome
-- Inserisce solo prodotti non ancora presenti con lo stesso nome.
WHERE NOT EXISTS (
  SELECT 1
  FROM prodotti existing_product
  WHERE existing_product.nome = p.nome
);

-- Mappa ogni prodotto demo al suo id reale nel database.
-- MIN(id) protegge da eventuali duplicati storici creati prima di questo seed.
CREATE TEMPORARY TABLE tmp_demo_product_ids (
  prodotto_nome VARCHAR(150) NOT NULL,
  id_prodotto BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (prodotto_nome)
);

INSERT INTO tmp_demo_product_ids (prodotto_nome, id_prodotto)
SELECT
  p.nome,
  MIN(existing_product.id) AS id_prodotto
FROM tmp_demo_products p
JOIN prodotti existing_product
  ON existing_product.nome = p.nome
GROUP BY p.nome;

-- Normalizza i prodotti demo esistenti senza crearne copie.
-- La WHERE su primary key mantiene l'UPDATE limitato al set risolto sopra.
UPDATE prodotti existing_product
JOIN tmp_demo_product_ids ids
  ON ids.id_prodotto = existing_product.id
JOIN tmp_demo_products p
  ON p.nome = ids.prodotto_nome
JOIN categorie_prodotto c
  ON c.nome = p.categoria_nome
SET
  existing_product.id_categoria = c.id,
  existing_product.descrizione = p.descrizione,
  existing_product.prezzo_base = p.prezzo_base,
  existing_product.minuti_preparazione = p.minuti_preparazione,
  existing_product.descrizione_preparazione = p.descrizione_preparazione,
  existing_product.attivo = p.attivo,
  existing_product.aggiornato_il = CURRENT_TIMESTAMP
WHERE existing_product.id = ids.id_prodotto;

-- ============================================================
-- INGREDIENTI
-- ============================================================

INSERT INTO ingredienti (
  nome,
  unita_misura,
  allergene,
  attivo
) VALUES
('Farina', 'g', TRUE, TRUE),
('Pomodoro', 'g', FALSE, TRUE),
('Mozzarella', 'g', TRUE, TRUE),
('Basilico', 'g', FALSE, TRUE),
('Salame piccante', 'g', FALSE, TRUE),
('Acqua', 'ml', FALSE, TRUE),
('Olio EVO', 'ml', FALSE, TRUE),
('Sale', 'g', FALSE, TRUE),
('Olive nere', 'g', FALSE, TRUE),
('Verdure grigliate', 'g', FALSE, TRUE),
('Parmigiano', 'g', TRUE, TRUE),
('Pane burger', 'g', TRUE, TRUE),
('Hamburger di manzo', 'g', FALSE, TRUE),
('Petto di pollo', 'g', FALSE, TRUE),
('Burger vegetale', 'g', TRUE, TRUE),
('Lattuga', 'g', FALSE, TRUE),
('Pomodoro fresco', 'g', FALSE, TRUE),
('Cipolla', 'g', FALSE, TRUE),
('Cheddar', 'g', TRUE, TRUE),
('Bacon', 'g', FALSE, TRUE),
('Salsa barbecue', 'g', FALSE, TRUE),
('Salsa yogurt', 'g', TRUE, TRUE),
('Patatine', 'g', FALSE, TRUE),
('Riso', 'g', FALSE, TRUE),
('Tonno', 'g', TRUE, TRUE),
('Ceci', 'g', FALSE, TRUE),
('Feta', 'g', TRUE, TRUE),
('Cetrioli', 'g', FALSE, TRUE),
('Crostini', 'g', TRUE, TRUE),
('Mascarpone', 'g', TRUE, TRUE),
('Caffe', 'ml', FALSE, TRUE),
('Savoiardi', 'g', TRUE, TRUE),
('Cacao', 'g', FALSE, TRUE),
('Panna', 'ml', TRUE, TRUE),
('Frutti di bosco', 'g', FALSE, TRUE),
('Cioccolato', 'g', TRUE, TRUE),
('Coca-Cola', 'ml', FALSE, TRUE),
('Te freddo limone', 'ml', FALSE, TRUE)
-- La colonna nome ha un vincolo UNIQUE anche per gli ingredienti.
-- In riesecuzione aggiorniamo unita, allergene e stato attivo.
ON DUPLICATE KEY UPDATE
  unita_misura = VALUES(unita_misura),
  allergene = VALUES(allergene),
  attivo = TRUE,
  aggiornato_il = CURRENT_TIMESTAMP;

-- Staging delle quantita ingredienti: prima usiamo nomi prodotto/ingrediente,
-- poi li traduciamo negli id effettivi con JOIN verso le tabelle reali.
CREATE TEMPORARY TABLE tmp_demo_product_ingredients (
  prodotto_nome VARCHAR(150) NOT NULL,
  ingrediente_nome VARCHAR(120) NOT NULL,
  quantita DECIMAL(10,3) NOT NULL,
  PRIMARY KEY (prodotto_nome, ingrediente_nome)
);

INSERT INTO tmp_demo_product_ingredients (
  prodotto_nome,
  ingrediente_nome,
  quantita
) VALUES
('Pizza Margherita', 'Farina', 250.000),
('Pizza Margherita', 'Pomodoro', 80.000),
('Pizza Margherita', 'Mozzarella', 100.000),
('Pizza Margherita', 'Basilico', 5.000),
('Pizza Margherita', 'Olio EVO', 10.000),
('Pizza Diavola', 'Farina', 250.000),
('Pizza Diavola', 'Pomodoro', 80.000),
('Pizza Diavola', 'Mozzarella', 100.000),
('Pizza Diavola', 'Salame piccante', 60.000),
('Pizza Diavola', 'Olio EVO', 10.000),
('Pizza Vegetariana', 'Farina', 250.000),
('Pizza Vegetariana', 'Pomodoro', 80.000),
('Pizza Vegetariana', 'Mozzarella', 90.000),
('Pizza Vegetariana', 'Verdure grigliate', 120.000),
('Pizza Vegetariana', 'Olio EVO', 10.000),
('Classic Burger', 'Pane burger', 90.000),
('Classic Burger', 'Hamburger di manzo', 150.000),
('Classic Burger', 'Cheddar', 25.000),
('Classic Burger', 'Lattuga', 25.000),
('Classic Burger', 'Pomodoro fresco', 30.000),
('Classic Burger', 'Cipolla', 15.000),
('Chicken Burger', 'Pane burger', 90.000),
('Chicken Burger', 'Petto di pollo', 140.000),
('Chicken Burger', 'Lattuga', 25.000),
('Chicken Burger', 'Pomodoro fresco', 30.000),
('Chicken Burger', 'Salsa yogurt', 20.000),
('Veggie Burger', 'Pane burger', 90.000),
('Veggie Burger', 'Burger vegetale', 130.000),
('Veggie Burger', 'Lattuga', 25.000),
('Veggie Burger', 'Pomodoro fresco', 30.000),
('Veggie Burger', 'Cipolla', 15.000),
('Veggie Burger', 'Salsa yogurt', 20.000),
('Caesar Bowl', 'Lattuga', 90.000),
('Caesar Bowl', 'Petto di pollo', 120.000),
('Caesar Bowl', 'Parmigiano', 25.000),
('Caesar Bowl', 'Crostini', 30.000),
('Caesar Bowl', 'Salsa yogurt', 25.000),
('Poke Mediterranea', 'Riso', 140.000),
('Poke Mediterranea', 'Tonno', 90.000),
('Poke Mediterranea', 'Ceci', 60.000),
('Poke Mediterranea', 'Pomodoro fresco', 50.000),
('Poke Mediterranea', 'Olive nere', 20.000),
('Poke Mediterranea', 'Feta', 35.000),
('Insalata Greca', 'Lattuga', 80.000),
('Insalata Greca', 'Feta', 60.000),
('Insalata Greca', 'Olive nere', 25.000),
('Insalata Greca', 'Cetrioli', 50.000),
('Insalata Greca', 'Pomodoro fresco', 60.000),
('Insalata Greca', 'Cipolla', 15.000),
('Tiramisu', 'Mascarpone', 60.000),
('Tiramisu', 'Caffe', 30.000),
('Tiramisu', 'Savoiardi', 45.000),
('Tiramisu', 'Cacao', 5.000),
('Panna Cotta', 'Panna', 120.000),
('Panna Cotta', 'Frutti di bosco', 40.000),
('Brownie al cioccolato', 'Cioccolato', 70.000),
('Brownie al cioccolato', 'Farina', 30.000),
('Acqua naturale', 'Acqua', 500.000),
('Coca-Cola 33cl', 'Coca-Cola', 330.000),
('Te freddo limone', 'Te freddo limone', 500.000);

INSERT INTO ingredienti_prodotto (
  id_prodotto,
  id_ingrediente,
  quantita
)
SELECT
  product_ids.id_prodotto,
  ingredienti.id,
  product_ingredients.quantita
FROM tmp_demo_product_ingredients product_ingredients
JOIN tmp_demo_product_ids product_ids
  ON product_ids.prodotto_nome = product_ingredients.prodotto_nome
JOIN ingredienti
  ON ingredienti.nome = product_ingredients.ingrediente_nome
-- La primary key di ingredienti_prodotto e (id_prodotto, id_ingrediente):
-- in riesecuzione aggiorniamo solo la quantita.
ON DUPLICATE KEY UPDATE
  quantita = VALUES(quantita);

-- ============================================================
-- GRUPPI CARATTERISTICHE
-- I gruppi sono usati solo per scelte radio mutuamente esclusive.
-- Gli extra cumulabili restano caratteristiche libere e vengono mostrati
-- come "Extra" dalla pagina prodotto web.
-- ============================================================

-- Staging dei gruppi mutuamente esclusivi mostrati come radio button nella UI.
-- Esempi: un solo Impasto, un solo Formato, una sola Cottura.
CREATE TEMPORARY TABLE tmp_demo_groups (
  prodotto_nome VARCHAR(150) NOT NULL,
  nome VARCHAR(100) NOT NULL,
  descrizione VARCHAR(255) NULL,
  obbligatorio BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (prodotto_nome, nome)
);

INSERT INTO tmp_demo_groups (
  prodotto_nome,
  nome,
  descrizione,
  obbligatorio
)
SELECT
  nome,
  'Impasto',
  'Scelta del tipo di impasto',
  TRUE
FROM tmp_demo_products
WHERE categoria_nome = 'Pizze';

INSERT INTO tmp_demo_groups (
  prodotto_nome,
  nome,
  descrizione,
  obbligatorio
)
SELECT
  nome,
  'Formato',
  'Scelta del formato',
  TRUE
FROM tmp_demo_products
WHERE categoria_nome IN ('Pizze', 'Panini/Burger', 'Bowl/Insalate');

INSERT INTO tmp_demo_groups (
  prodotto_nome,
  nome,
  descrizione,
  obbligatorio
) VALUES
('Classic Burger', 'Cottura', 'Scelta della cottura del burger', TRUE),
('Chicken Burger', 'Cottura', 'Scelta della finitura del pollo', TRUE);

INSERT INTO gruppi_caratteristiche (
  id_prodotto,
  nome,
  descrizione,
  obbligatorio,
  attivo
)
SELECT
  product_ids.id_prodotto,
  demo_groups.nome,
  demo_groups.descrizione,
  demo_groups.obbligatorio,
  TRUE
FROM tmp_demo_groups demo_groups
JOIN tmp_demo_product_ids product_ids
  ON product_ids.prodotto_nome = demo_groups.prodotto_nome
-- Vincolo UNIQUE su (id_prodotto, nome): aggiorna il gruppo se gia presente.
ON DUPLICATE KEY UPDATE
  descrizione = VALUES(descrizione),
  obbligatorio = VALUES(obbligatorio),
  attivo = TRUE,
  aggiornato_il = CURRENT_TIMESTAMP;

-- Mappa ogni gruppo demo al suo id reale. Serve per collegare correttamente
-- le caratteristiche al gruppo senza assumere id numerici fissi.
CREATE TEMPORARY TABLE tmp_demo_group_ids (
  prodotto_nome VARCHAR(150) NOT NULL,
  gruppo_nome VARCHAR(100) NOT NULL,
  id_gruppo BIGINT UNSIGNED NOT NULL,
  PRIMARY KEY (prodotto_nome, gruppo_nome)
);

INSERT INTO tmp_demo_group_ids (
  prodotto_nome,
  gruppo_nome,
  id_gruppo
)
SELECT
  demo_groups.prodotto_nome,
  demo_groups.nome,
  existing_group.id
FROM tmp_demo_groups demo_groups
JOIN tmp_demo_product_ids product_ids
  ON product_ids.prodotto_nome = demo_groups.prodotto_nome
JOIN gruppi_caratteristiche existing_group
  ON existing_group.id_prodotto = product_ids.id_prodotto
 AND existing_group.nome = demo_groups.nome;

-- ============================================================
-- CARATTERISTICHE MUTUAMENTE ESCLUSIVE
-- ============================================================

-- Caratteristiche appartenenti a un gruppo: il carrello consente una sola
-- scelta per ciascun id_gruppo_caratteristiche.
CREATE TEMPORARY TABLE tmp_demo_group_characteristics (
  prodotto_nome VARCHAR(150) NOT NULL,
  gruppo_nome VARCHAR(100) NOT NULL,
  nome VARCHAR(100) NOT NULL,
  descrizione VARCHAR(255) NULL,
  differenza_prezzo DECIMAL(10,2) NOT NULL,
  selezionata_default BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (prodotto_nome, nome)
);

INSERT INTO tmp_demo_group_characteristics (
  prodotto_nome,
  gruppo_nome,
  nome,
  descrizione,
  differenza_prezzo,
  selezionata_default
)
SELECT nome, 'Impasto', 'Impasto normale', 'Impasto classico', 0.00, TRUE
FROM tmp_demo_products
WHERE categoria_nome = 'Pizze';

INSERT INTO tmp_demo_group_characteristics (
  prodotto_nome,
  gruppo_nome,
  nome,
  descrizione,
  differenza_prezzo,
  selezionata_default
)
SELECT nome, 'Impasto', 'Impasto integrale', 'Impasto con farina integrale', 1.00, FALSE
FROM tmp_demo_products
WHERE categoria_nome = 'Pizze';

INSERT INTO tmp_demo_group_characteristics (
  prodotto_nome,
  gruppo_nome,
  nome,
  descrizione,
  differenza_prezzo,
  selezionata_default
)
SELECT nome, 'Impasto', 'Impasto senza glutine', 'Impasto senza glutine', 2.00, FALSE
FROM tmp_demo_products
WHERE categoria_nome = 'Pizze';

INSERT INTO tmp_demo_group_characteristics (
  prodotto_nome,
  gruppo_nome,
  nome,
  descrizione,
  differenza_prezzo,
  selezionata_default
)
SELECT nome, 'Formato', 'Formato normale', 'Porzione standard', 0.00, TRUE
FROM tmp_demo_products
WHERE categoria_nome IN ('Pizze', 'Panini/Burger', 'Bowl/Insalate');

INSERT INTO tmp_demo_group_characteristics (
  prodotto_nome,
  gruppo_nome,
  nome,
  descrizione,
  differenza_prezzo,
  selezionata_default
)
SELECT
  nome,
  'Formato',
  'Formato grande',
  'Porzione maggiorata',
  CASE
    WHEN categoria_nome = 'Panini/Burger' THEN 2.50
    ELSE 2.00
  END,
  FALSE
FROM tmp_demo_products
WHERE categoria_nome IN ('Pizze', 'Panini/Burger', 'Bowl/Insalate');

INSERT INTO tmp_demo_group_characteristics (
  prodotto_nome,
  gruppo_nome,
  nome,
  descrizione,
  differenza_prezzo,
  selezionata_default
) VALUES
('Classic Burger', 'Cottura', 'Cottura media', 'Burger cotto al punto', 0.00, TRUE),
('Classic Burger', 'Cottura', 'Cottura ben cotta', 'Burger ben cotto', 0.00, FALSE),
('Chicken Burger', 'Cottura', 'Cottura standard', 'Pollo cotto standard', 0.00, TRUE),
('Chicken Burger', 'Cottura', 'Cottura croccante', 'Pollo piu croccante', 0.50, FALSE);

INSERT INTO caratteristiche (
  id_prodotto,
  id_gruppo_caratteristiche,
  nome,
  descrizione,
  differenza_prezzo,
  selezionata_default,
  attiva
)
SELECT
  product_ids.id_prodotto,
  group_ids.id_gruppo,
  characteristics.nome,
  characteristics.descrizione,
  characteristics.differenza_prezzo,
  characteristics.selezionata_default,
  TRUE
FROM tmp_demo_group_characteristics characteristics
JOIN tmp_demo_product_ids product_ids
  ON product_ids.prodotto_nome = characteristics.prodotto_nome
JOIN tmp_demo_group_ids group_ids
  ON group_ids.prodotto_nome = characteristics.prodotto_nome
 AND group_ids.gruppo_nome = characteristics.gruppo_nome
-- Vincolo UNIQUE su (id_prodotto, nome): in riesecuzione aggiorniamo prezzo,
-- default, descrizione e gruppo invece di duplicare opzioni.
ON DUPLICATE KEY UPDATE
  id_gruppo_caratteristiche = VALUES(id_gruppo_caratteristiche),
  descrizione = VALUES(descrizione),
  differenza_prezzo = VALUES(differenza_prezzo),
  selezionata_default = VALUES(selezionata_default),
  attiva = TRUE,
  aggiornato_il = CURRENT_TIMESTAMP;

-- ============================================================
-- EXTRA CUMULABILI
-- id_gruppo_caratteristiche resta NULL per consentire piu' extra insieme.
-- ============================================================

-- Extra liberi: nella UI sono checkbox e possono essere sommati tra loro.
-- Per questo id_gruppo_caratteristiche resta NULL.
CREATE TEMPORARY TABLE tmp_demo_free_characteristics (
  prodotto_nome VARCHAR(150) NOT NULL,
  nome VARCHAR(100) NOT NULL,
  descrizione VARCHAR(255) NULL,
  differenza_prezzo DECIMAL(10,2) NOT NULL,
  PRIMARY KEY (prodotto_nome, nome)
);

INSERT INTO tmp_demo_free_characteristics (
  prodotto_nome,
  nome,
  descrizione,
  differenza_prezzo
)
SELECT nome, 'Verdure grigliate', 'Aggiunta di verdure grigliate', 1.20
FROM tmp_demo_products
WHERE categoria_nome = 'Pizze';

INSERT INTO tmp_demo_free_characteristics (
  prodotto_nome,
  nome,
  descrizione,
  differenza_prezzo
)
SELECT nome, 'Mozzarella extra', 'Aggiunta di mozzarella extra', 1.50
FROM tmp_demo_products
WHERE categoria_nome = 'Pizze'
  AND nome <> 'Pizza Margherita';

INSERT INTO tmp_demo_free_characteristics (
  prodotto_nome,
  nome,
  descrizione,
  differenza_prezzo
) VALUES
('Pizza Diavola', 'Extra piccante', 'Aggiunta di peperoncino extra', 0.50),
('Classic Burger', 'Patatine', 'Porzione piccola di patatine', 1.50),
('Classic Burger', 'Bacon', 'Aggiunta di bacon croccante', 1.50),
('Classic Burger', 'Cheddar extra', 'Aggiunta di cheddar', 1.00),
('Classic Burger', 'Salsa barbecue', 'Aggiunta di salsa barbecue', 0.50),
('Chicken Burger', 'Patatine', 'Porzione piccola di patatine', 1.50),
('Chicken Burger', 'Bacon', 'Aggiunta di bacon croccante', 1.50),
('Chicken Burger', 'Salsa yogurt extra', 'Aggiunta di salsa yogurt', 0.50),
('Veggie Burger', 'Patatine', 'Porzione piccola di patatine', 1.50),
('Veggie Burger', 'Verdure extra', 'Aggiunta di verdure fresche', 1.00),
('Veggie Burger', 'Salsa yogurt extra', 'Aggiunta di salsa yogurt', 0.50),
('Caesar Bowl', 'Verdure extra', 'Aggiunta di verdure fresche', 1.00),
('Caesar Bowl', 'Salsa yogurt extra', 'Aggiunta di salsa yogurt', 0.50),
('Poke Mediterranea', 'Feta extra', 'Aggiunta di feta', 1.20),
('Poke Mediterranea', 'Verdure extra', 'Aggiunta di verdure fresche', 1.00),
('Insalata Greca', 'Feta extra', 'Aggiunta di feta', 1.20),
('Insalata Greca', 'Verdure extra', 'Aggiunta di verdure fresche', 1.00),
('Tiramisu', 'Cacao extra', 'Spolverata extra di cacao', 0.50),
('Panna Cotta', 'Frutti di bosco extra', 'Aggiunta di frutti di bosco', 1.00),
('Brownie al cioccolato', 'Panna extra', 'Aggiunta di panna', 0.80);

INSERT INTO caratteristiche (
  id_prodotto,
  id_gruppo_caratteristiche,
  nome,
  descrizione,
  differenza_prezzo,
  selezionata_default,
  attiva
)
SELECT
  product_ids.id_prodotto,
  NULL,
  characteristics.nome,
  characteristics.descrizione,
  characteristics.differenza_prezzo,
  FALSE,
  TRUE
FROM tmp_demo_free_characteristics characteristics
JOIN tmp_demo_product_ids product_ids
  ON product_ids.prodotto_nome = characteristics.prodotto_nome
-- Se un extra esiste gia, lo manteniamo libero (gruppo NULL) e aggiorniamo
-- prezzo/descrizione/stato. Non creiamo mai "Doppia mozzarella".
ON DUPLICATE KEY UPDATE
  id_gruppo_caratteristiche = NULL,
  descrizione = VALUES(descrizione),
  differenza_prezzo = VALUES(differenza_prezzo),
  selezionata_default = FALSE,
  attiva = TRUE,
  aggiornato_il = CURRENT_TIMESTAMP;

-- Pulizia esplicita delle tabelle temporanee prima del COMMIT.
-- Non sarebbe obbligatoria, ma rende piu pulita la riesecuzione in Workbench.
DROP TEMPORARY TABLE IF EXISTS tmp_demo_free_characteristics;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_group_characteristics;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_group_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_groups;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_product_ingredients;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_product_ids;
DROP TEMPORARY TABLE IF EXISTS tmp_demo_products;

-- Da qui le modifiche diventano definitive.
COMMIT;

-- Ripristina il valore di safe update mode precedente allo script.
SET SQL_SAFE_UPDATES = @OLD_SQL_SAFE_UPDATES;

-- ============================================================
-- QUERY DI VERIFICA CONSIGLIATE
-- ============================================================

-- Conteggio categorie demo attive.
SELECT
  COUNT(*) AS categorie_demo_attive
FROM categorie_prodotto
WHERE attiva = TRUE
  AND nome IN ('Pizze', 'Panini/Burger', 'Bowl/Insalate', 'Dolci', 'Bevande');

-- Conteggio prodotti demo attivi.
SELECT
  COUNT(*) AS prodotti_demo_attivi
FROM prodotti p
JOIN categorie_prodotto c
  ON c.id = p.id_categoria
WHERE p.attivo = TRUE
  AND c.nome IN ('Pizze', 'Panini/Burger', 'Bowl/Insalate', 'Dolci', 'Bevande');

-- Prodotti attivi per categoria.
SELECT
  c.nome AS categoria,
  COUNT(p.id) AS prodotti_attivi
FROM categorie_prodotto c
LEFT JOIN prodotti p
  ON p.id_categoria = c.id
 AND p.attivo = TRUE
WHERE c.nome IN ('Pizze', 'Panini/Burger', 'Bowl/Insalate', 'Dolci', 'Bevande')
GROUP BY c.id, c.nome, c.ordine_visualizzazione
ORDER BY c.ordine_visualizzazione, c.nome;

-- Elenco prodotti demo con prezzo e tempo preparazione.
SELECT
  c.nome AS categoria,
  p.nome AS prodotto,
  p.prezzo_base,
  p.minuti_preparazione,
  p.attivo
FROM prodotti p
JOIN categorie_prodotto c
  ON c.id = p.id_categoria
WHERE c.nome IN ('Pizze', 'Panini/Burger', 'Bowl/Insalate', 'Dolci', 'Bevande')
  AND p.nome IN (
    'Pizza Margherita',
    'Pizza Diavola',
    'Pizza Vegetariana',
    'Classic Burger',
    'Chicken Burger',
    'Veggie Burger',
    'Caesar Bowl',
    'Poke Mediterranea',
    'Insalata Greca',
    'Tiramisu',
    'Panna Cotta',
    'Brownie al cioccolato',
    'Acqua naturale',
    'Coca-Cola 33cl',
    'Te freddo limone'
  )
ORDER BY c.ordine_visualizzazione, p.nome;

-- Gruppi caratteristiche con nomi leggibili.
SELECT
  p.nome AS prodotto,
  g.nome AS gruppo,
  g.obbligatorio,
  g.attivo,
  COUNT(c.id) AS caratteristiche_attive
FROM gruppi_caratteristiche g
JOIN prodotti p
  ON p.id = g.id_prodotto
LEFT JOIN caratteristiche c
  ON c.id_gruppo_caratteristiche = g.id
 AND c.attiva = TRUE
WHERE p.nome IN (
    'Pizza Margherita',
    'Pizza Diavola',
    'Pizza Vegetariana',
    'Classic Burger',
    'Chicken Burger',
    'Veggie Burger',
    'Caesar Bowl',
    'Poke Mediterranea',
    'Insalata Greca'
  )
GROUP BY p.nome, g.nome, g.obbligatorio, g.attivo
ORDER BY p.nome, g.nome;

-- Caratteristiche associate ai prodotti, incluse libere/extra.
SELECT
  p.nome AS prodotto,
  COALESCE(g.nome, 'Extra') AS gruppo_visualizzato,
  c.nome AS caratteristica,
  c.differenza_prezzo,
  c.selezionata_default,
  c.attiva
FROM caratteristiche c
JOIN prodotti p
  ON p.id = c.id_prodotto
LEFT JOIN gruppi_caratteristiche g
  ON g.id = c.id_gruppo_caratteristiche
WHERE p.nome IN (
    'Pizza Margherita',
    'Pizza Diavola',
    'Pizza Vegetariana',
    'Classic Burger',
    'Chicken Burger',
    'Veggie Burger',
    'Caesar Bowl',
    'Poke Mediterranea',
    'Insalata Greca',
    'Tiramisu',
    'Panna Cotta',
    'Brownie al cioccolato'
  )
ORDER BY p.nome, gruppo_visualizzato, c.nome;

-- Ingredienti associati ai prodotti demo.
SELECT
  p.nome AS prodotto,
  COUNT(ip.id_ingrediente) AS ingredienti_associati
FROM prodotti p
LEFT JOIN ingredienti_prodotto ip
  ON ip.id_prodotto = p.id
WHERE p.nome IN (
    'Pizza Margherita',
    'Pizza Diavola',
    'Pizza Vegetariana',
    'Classic Burger',
    'Chicken Burger',
    'Veggie Burger',
    'Caesar Bowl',
    'Poke Mediterranea',
    'Insalata Greca',
    'Tiramisu',
    'Panna Cotta',
    'Brownie al cioccolato',
    'Acqua naturale',
    'Coca-Cola 33cl',
    'Te freddo limone'
  )
GROUP BY p.id, p.nome
ORDER BY p.nome;
