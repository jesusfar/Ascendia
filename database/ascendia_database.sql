-- ============================================================
-- ASCENDIA — Sistema de Gestión Integral de Oportunidades
--            Educativas Globales para Jóvenes
-- ============================================================
-- Autor: Jesús Fariña (VINF015570)
-- Materia: Seminario de Práctica — Licenciatura en Informática
-- Universidad Siglo 21 — Profesora: Ana Carolina Ferreyra
-- Base de datos: MySQL 8.0
-- Codificación: UTF-8 (utf8mb4)
-- Normalización: Tercera Forma Normal (3FN)
-- ============================================================

-- ===========================================
-- SECCIÓN 1: CREACIÓN DEL ESQUEMA
-- ===========================================

DROP DATABASE IF EXISTS ascendia;
CREATE DATABASE ascendia
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE ascendia;

-- ===========================================
-- SECCIÓN 2: CREACIÓN DE TABLAS (DDL)
-- ===========================================

-- Tabla: pais
-- Justificación: Normaliza los países evitando redundancia 
-- en universidades y usuarios. Incluye coordenadas para 
-- geolocalización en el globo 3D.
CREATE TABLE pais (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(100) NOT NULL,
  codigo_iso  CHAR(3) NOT NULL UNIQUE,
  continente  VARCHAR(50) NOT NULL,
  latitud     DECIMAL(10,7),
  longitud    DECIMAL(10,7),
  INDEX idx_continente (continente)
) ENGINE=InnoDB;

-- Tabla: area_estudio
-- Justificación: Normaliza las categorías de oportunidades.
-- En 2FN se separó del texto libre para evitar inconsistencias.
CREATE TABLE area_estudio (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(100) NOT NULL UNIQUE,
  descripcion TEXT
) ENGINE=InnoDB;

-- Tabla: universidad
-- Justificación: Cada universidad pertenece a un país (FK).
-- Incluye coordenadas propias para ubicación precisa en el globo.
CREATE TABLE universidad (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  nombre      VARCHAR(200) NOT NULL,
  pais_id     INT NOT NULL,
  ciudad      VARCHAR(100),
  latitud     DECIMAL(10,7),
  longitud    DECIMAL(10,7),
  sitio_web   VARCHAR(255),
  FOREIGN KEY (pais_id) REFERENCES pais(id)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  INDEX idx_pais (pais_id)
) ENGINE=InnoDB;

-- Tabla: usuario
-- Justificación: Gestiona registro y autenticación.
-- password_hash almacena hash bcrypt (nunca texto plano, RNF09).
-- rol diferencia permisos (usuario vs admin).
CREATE TABLE usuario (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  nombre           VARCHAR(100) NOT NULL,
  email            VARCHAR(150) NOT NULL UNIQUE,
  password_hash    VARCHAR(255) NOT NULL,
  pais_id          INT,
  nivel_educativo  VARCHAR(50),
  fecha_registro   DATETIME DEFAULT CURRENT_TIMESTAMP,
  rol              ENUM('usuario','admin') DEFAULT 'usuario',
  FOREIGN KEY (pais_id) REFERENCES pais(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  INDEX idx_email (email),
  INDEX idx_rol (rol)
) ENGINE=InnoDB;

-- Tabla: oportunidad
-- Justificación: Entidad central del sistema. Incluye tipo como
-- ENUM (normalizado en vez de tabla separada por baja cardinalidad),
-- coordenadas para geolocalización, estado con ciclo de vida
-- (BORRADOR→ACTIVA→VENCIDA→ELIMINADA per diagrama de estados).
CREATE TABLE oportunidad (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  titulo           VARCHAR(300) NOT NULL,
  descripcion      TEXT,
  tipo             ENUM('beca','curso','intercambio',
                        'pasantia','voluntariado') NOT NULL,
  universidad_id   INT,
  area_estudio_id  INT,
  fecha_limite     DATE,
  url_oficial      VARCHAR(500),
  idioma           VARCHAR(50) DEFAULT 'Español',
  modalidad        ENUM('presencial','virtual','hibrida'),
  latitud          DECIMAL(10,7),
  longitud         DECIMAL(10,7),
  estado           ENUM('borrador','activa','vencida','eliminada')
                     DEFAULT 'activa',
  fecha_publicacion DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (universidad_id) REFERENCES universidad(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  FOREIGN KEY (area_estudio_id) REFERENCES area_estudio(id)
    ON UPDATE CASCADE ON DELETE SET NULL,
  INDEX idx_tipo (tipo),
  INDEX idx_estado (estado),
  INDEX idx_fecha_limite (fecha_limite),
  INDEX idx_universidad (universidad_id),
  INDEX idx_area (area_estudio_id)
) ENGINE=InnoDB;

-- Tabla: favorito
-- Justificación: Resuelve la relación N:M entre usuario y
-- oportunidad. UNIQUE evita duplicados. ON DELETE CASCADE
-- elimina favoritos si se borra el usuario u oportunidad.
CREATE TABLE favorito (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id      INT NOT NULL,
  oportunidad_id  INT NOT NULL,
  fecha_guardado  DATETIME DEFAULT CURRENT_TIMESTAMP,
  UNIQUE (usuario_id, oportunidad_id),
  FOREIGN KEY (usuario_id) REFERENCES usuario(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (oportunidad_id) REFERENCES oportunidad(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- Tabla: preferencia_usuario
-- Justificación: Resuelve la relación N:M entre usuario y
-- area_estudio para el sistema de sugerencias personalizadas.
CREATE TABLE preferencia_usuario (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  usuario_id       INT NOT NULL,
  area_estudio_id  INT NOT NULL,
  UNIQUE (usuario_id, area_estudio_id),
  FOREIGN KEY (usuario_id) REFERENCES usuario(id)
    ON UPDATE CASCADE ON DELETE CASCADE,
  FOREIGN KEY (area_estudio_id) REFERENCES area_estudio(id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;


-- ===========================================
-- SECCIÓN 3: INSERCIÓN DE DATOS (DML)
-- ===========================================

-- 3.1 Países (datos reales con coordenadas de capital)
INSERT INTO pais (id,nombre,codigo_iso,continente,latitud,longitud) VALUES
 (1,'Argentina','ARG','America del Sur',-34.6037389,-58.3815704),
 (2,'Alemania','DEU','Europa',52.5200066,13.4049540),
 (3,'Espana','ESP','Europa',40.4167754,-3.7037902),
 (4,'Estados Unidos','USA','America del Norte',38.9071923,-77.0368707),
 (5,'Brasil','BRA','America del Sur',-15.7942287,-47.8821658),
 (6,'Francia','FRA','Europa',48.8566140,2.3522219),
 (7,'Japon','JPN','Asia',35.6894875,139.6917064),
 (8,'Australia','AUS','Oceania',-33.8688197,151.2092955),
 (9,'Mexico','MEX','America del Norte',19.4326077,-99.1332080),
 (10,'Colombia','COL','America del Sur',4.7109886,-74.0721064),
 (11,'Austria','AUT','Europa',48.2081743,16.3738189),
 (12,'Belgica','BEL','Europa',50.8503396,4.3517103),
 (13,'Canada','CAN','America del Norte',45.4215296,-75.6971931),
 (14,'Chile','CHL','America del Sur',-33.4488897,-70.6692655),
 (15,'China','CHN','Asia',39.9041999,116.4073963),
 (16,'Dinamarca','DNK','Europa',55.6760968,12.5683371),
 (17,'Finlandia','FIN','Europa',60.1698557,24.9383791),
 (18,'Hong Kong','HKG','Asia',22.3193039,114.1693611),
 (19,'India','IND','Asia',28.6139391,77.2090212),
 (20,'Indonesia','IDN','Asia',-6.2087634,106.8455700),
 (21,'Irlanda','IRL','Europa',53.3498053,-6.2603097),
 (22,'Italia','ITA','Europa',41.9027835,12.4963655),
 (23,'Kazajistan','KAZ','Asia',51.1605227,71.4703558),
 (24,'Malasia','MYS','Asia',3.1389025,101.6868993),
 (25,'Paises Bajos','NLD','Europa',52.3675734,4.9041389),
 (26,'Nueva Zelanda','NZL','Oceania',-41.2864603,174.7762045),
 (27,'Noruega','NOR','Europa',59.9138688,10.7522454),
 (28,'Peru','PER','America del Sur',-12.0463731,-77.0427934),
 (29,'Catar','QAT','Asia',25.2854473,51.5310398),
 (30,'Corea del Sur','KOR','Asia',37.5665350,126.9779692),
 (31,'Rusia','RUS','Europa',55.7558260,37.6172999),
 (32,'Arabia Saudita','SAU','Asia',24.7135517,46.6752957),
 (33,'Singapur','SGP','Asia',1.3520830,103.8198360),
 (34,'Sudafrica','ZAF','Africa',-25.7478676,28.2292712),
 (35,'Suecia','SWE','Europa',59.3293235,18.0686415),
 (36,'Suiza','CHE','Europa',46.9479739,7.4474468),
 (37,'Taiwan','TWN','Asia',25.0329636,121.5654268),
 (38,'Emiratos Arabes Unidos','ARE','Asia',24.4538352,54.3774014),
 (39,'Reino Unido','GBR','Europa',51.5073509,-0.1277583);

-- 3.2 Áreas de estudio
INSERT INTO area_estudio (nombre, descripcion) VALUES
('Ingeniería y Tecnología', 'Carreras de ingeniería, informática, telecomunicaciones y tecnología aplicada.'),
('Ciencias Sociales',       'Sociología, ciencias políticas, relaciones internacionales, antropología.'),
('Ciencias de la Salud',    'Medicina, enfermería, farmacia, salud pública y biotecnología.'),
('Economía y Negocios',     'Administración, contabilidad, finanzas, comercio internacional, marketing.'),
('Arte y Humanidades',      'Filosofía, literatura, artes visuales, música, historia y lingüística.'),
('Ciencias Exactas',        'Matemática, física, química, astronomía y ciencias ambientales.'),
('Derecho',                 'Derecho internacional, derechos humanos, derecho ambiental, derecho digital.'),
('Educación',               'Pedagogía, didáctica, educación especial, tecnología educativa.');

-- 3.3 Universidades (datos reales con coordenadas reales)
INSERT INTO universidad (id,nombre,pais_id,ciudad,latitud,longitud,sitio_web) VALUES
 (1,'Universidad de Buenos Aires',1,'Buenos Aires',-34.5997100,-58.3736100,'https://www.uba.ar'),
 (2,'Technische Universität Berlin',2,'Berlín',52.5125300,13.3269300,'https://www.tu.berlin'),
 (3,'Universidad de Salamanca',3,'Salamanca',40.9607800,-5.6689500,'https://www.usal.es'),
 (4,'Massachusetts Institute of Technology (MIT)',4,'Cambridge, MA',42.3601000,-71.0942000,'https://www.mit.edu'),
 (5,'Universidade de São Paulo (USP)',5,'São Paulo',-23.5587000,-46.7319700,'https://www.usp.br'),
 (6,'Sorbonne Université',6,'París',48.8479400,2.3563900,'https://www.sorbonne-universite.fr'),
 (7,'The University of Tokyo',7,'Tokio',35.7126000,139.7620000,'https://www.u-tokyo.ac.jp'),
 (8,'The University of Melbourne',8,'Melbourne',-37.7983000,144.9610600,'https://www.unimelb.edu.au'),
 (9,'Universidad Nacional Autónoma de México (UNAM)',9,'Ciudad de México',19.3320600,-99.1871400,'https://www.unam.mx'),
 (10,'Universidad de los Andes',10,'Bogotá',4.6012700,-74.0649200,'https://www.uniandes.edu.co'),
 (11,'Imperial College London',39,NULL,51.4988000,-0.1749000,NULL),
 (12,'Stanford University',4,NULL,37.4275000,-122.1697000,NULL),
 (13,'University of Oxford',39,NULL,51.7548000,-1.2544000,NULL),
 (14,'Harvard University',4,NULL,42.3770000,-71.1167000,NULL),
 (15,'University of Cambridge',39,NULL,52.2053000,0.1218000,NULL),
 (16,'ETH Zurich (Swiss Federal Institute of Technology)',36,NULL,47.3763000,8.5480000,NULL),
 (17,'National University of Singapore (NUS)',33,NULL,1.2966000,103.7764000,NULL),
 (18,'UCL (University College London)',39,NULL,51.5246000,-0.1340000,NULL),
 (19,'California Institute of Technology (Caltech)',4,NULL,34.1377000,-118.1253000,NULL),
 (20,'The University of Hong Kong',18,NULL,22.2830000,114.1378000,NULL),
 (21,'Nanyang Technological University, Singapore (NTU Singapore)',33,NULL,1.3483000,103.6831000,NULL),
 (22,'University of Chicago',4,NULL,41.7897000,-87.5997000,NULL),
 (23,'Peking University',15,NULL,39.9928000,116.3055000,NULL),
 (24,'University of Pennsylvania',4,NULL,39.9522000,-75.1932000,NULL),
 (25,'Cornell University',4,NULL,42.4534000,-76.4735000,NULL),
 (26,'Tsinghua University',15,NULL,40.0000000,116.3263000,NULL),
 (27,'University of California, Berkeley (UCB)',4,NULL,37.8719000,-122.2585000,NULL),
 (28,'The University of New South Wales',8,NULL,-33.9173000,151.2313000,NULL),
 (29,'Yale University',4,NULL,41.3163000,-72.9223000,NULL),
 (30,'École Polytechnique Fédérale de Lausanne',36,NULL,46.5191000,6.5668000,NULL),
 (31,'Technical University of Munich',2,NULL,48.1496000,11.5679000,NULL),
 (32,'Johns Hopkins University',4,NULL,39.3299000,-76.6205000,NULL),
 (33,'Princeton University',4,NULL,40.3431000,-74.6551000,NULL),
 (34,'The University of Sydney',8,NULL,-33.8886000,151.1873000,NULL),
 (35,'McGill University',13,NULL,45.5048000,-73.5772000,NULL),
 (36,'PSL University',6,NULL,48.8493000,2.3420000,NULL),
 (37,'University of Toronto',13,NULL,43.6629000,-79.3957000,NULL),
 (38,'Fudan University',15,NULL,31.2989000,121.5036000,NULL),
 (39,'King''s College London (KCL)',39,NULL,51.5115000,-0.1160000,NULL),
 (40,'Australian National University',8,NULL,-35.2777000,149.1185000,NULL),
 (41,'The Chinese University of Hong Kong',18,NULL,22.4196000,114.2068000,NULL),
 (42,'University of Edinburgh',39,NULL,55.9445000,-3.1880000,NULL),
 (43,'The University of Manchester',39,NULL,53.4668000,-2.2339000,NULL),
 (44,'Monash University',8,NULL,-37.9105000,145.1362000,NULL),
 (45,'Columbia University',4,NULL,40.8075000,-73.9626000,NULL),
 (46,'Seoul National University',30,NULL,37.4599000,126.9519000,NULL),
 (47,'University of British Columbia',13,NULL,49.2606000,-123.2460000,NULL),
 (48,'Institut Polytechnique de Paris',6,NULL,48.7134000,2.2086000,NULL),
 (49,'Northwestern University',4,NULL,42.0565000,-87.6753000,NULL),
 (50,'The University of Queensland',8,NULL,-27.4975000,153.0137000,NULL),
 (51,'The Hong Kong University of Science and Technology',18,NULL,22.3364000,114.2655000,NULL),
 (52,'University of Michigan-Ann Arbor',4,NULL,42.2780000,-83.7382000,NULL),
 (53,'University of California, Los Angeles (UCLA)',4,NULL,34.0689000,-118.4452000,NULL),
 (54,'Delft University of Technology',25,NULL,51.9989000,4.3733000,NULL),
 (55,'Shanghai Jiao Tong University',15,NULL,31.0240000,121.4336000,NULL),
 (56,'Zhejiang University',15,NULL,30.2638000,120.1236000,NULL),
 (57,'Yonsei University',30,NULL,37.5658000,126.9386000,NULL),
 (58,'University of Bristol',39,NULL,51.4566000,-2.6049000,NULL),
 (59,'Carnegie Mellon University',4,NULL,40.4433000,-79.9436000,NULL),
 (60,'The University of Amsterdam',25,NULL,52.3558000,4.9553000,NULL),
 (61,'The Hong Kong Polytechnic University',18,NULL,22.3049000,114.1796000,NULL),
 (62,'New York University (NYU)',4,NULL,40.7295000,-73.9965000,NULL),
 (63,'London School of Economics and Political Science (LSE)',39,NULL,51.5144000,-0.1165000,NULL),
 (64,'Kyoto University',7,NULL,35.0262000,135.7809000,NULL),
 (65,'Ludwig-Maximilians-Universität München',2,NULL,48.1500000,11.5800000,NULL),
 (66,'Universiti Malaya (UM)',24,NULL,3.1200000,101.6544000,NULL),
 (67,'KU Leuven',12,NULL,50.8778000,4.7005000,NULL),
 (68,'Korea University',30,NULL,37.5894000,127.0326000,NULL),
 (69,'Duke University',4,NULL,36.0014000,-78.9382000,NULL),
 (70,'City University of Hong Kong',18,NULL,22.3372000,114.1739000,NULL),
 (71,'National Taiwan University (NTU)',37,NULL,25.0173000,121.5398000,NULL),
 (72,'The University of Auckland',26,NULL,-36.8523000,174.7681000,NULL),
 (73,'University of California, San Diego (UCSD)',4,NULL,32.8801000,-117.2340000,NULL),
 (74,'King Fahd University of Petroleum & Minerals',32,NULL,26.3099000,50.1456000,NULL),
 (75,'University of Texas at Austin',4,NULL,30.2849000,-97.7341000,NULL),
 (76,'Brown University',4,NULL,41.8268000,-71.4025000,NULL),
 (77,'Université Paris-Saclay',6,NULL,48.7097000,2.1670000,NULL),
 (78,'University of Illinois at Urbana-Champaign',4,NULL,40.1020000,-88.2272000,NULL),
 (79,'Lund University',35,NULL,55.7047000,13.1910000,NULL),
 (80,'The University of Warwick',39,NULL,52.3793000,-1.5615000,NULL),
 (81,'Trinity College Dublin, The University of Dublin',21,NULL,53.3438000,-6.2546000,NULL),
 (82,'University of Birmingham',39,NULL,52.4508000,-1.9305000,NULL),
 (83,'The University of Western Australia',8,NULL,-31.9805000,115.8172000,NULL),
 (84,'KTH Royal Institute of Technology',35,NULL,59.3498000,18.0705000,NULL),
 (85,'University of Glasgow',39,NULL,55.8721000,-4.2882000,NULL),
 (86,'Ruprecht-Karls-Universität Heidelberg',2,NULL,49.4106000,8.7064000,NULL),
 (87,'University of Washington',4,NULL,47.6553000,-122.3035000,NULL),
 (88,'Adelaide University',8,NULL,-34.9206000,138.6063000,NULL),
 (89,'Pennsylvania State University',4,NULL,40.7982000,-77.8599000,NULL),
 (90,'Tokyo Institute of Technology',7,NULL,35.6045000,139.6839000,NULL),
 (91,'University of Leeds',39,NULL,53.8067000,-1.5550000,NULL),
 (92,'University of Southampton',39,NULL,50.9369000,-1.3962000,NULL),
 (93,'Boston University',4,NULL,42.3505000,-71.1054000,NULL),
 (94,'Freie Universität Berlin',2,NULL,52.4573000,13.2924000,NULL),
 (95,'Purdue University',4,NULL,40.4237000,-86.9212000,NULL),
 (96,'The University of Osaka',7,NULL,34.8202000,135.5234000,NULL),
 (97,'The University of Sheffield',39,NULL,53.3814000,-1.4880000,NULL),
 (98,'Uppsala University',35,NULL,59.8586000,17.6389000,NULL),
 (99,'Durham University',39,NULL,54.7681000,-1.5780000,NULL),
 (100,'University of Alberta',13,NULL,53.5232000,-113.5263000,NULL),
 (101,'University of Technology Sydney',8,NULL,-33.8837000,151.2006000,NULL),
 (102,'The University of Nottingham',39,NULL,52.9386000,-1.1950000,NULL),
 (103,'Karlsruhe Institute of Technology (KIT)',2,NULL,49.0094000,8.4110000,NULL),
 (104,'Politecnico di Milano',22,NULL,45.4781000,9.2275000,NULL),
 (105,'University of Zurich (UZH)',36,NULL,47.3744000,8.5480000,NULL),
 (106,'University of Copenhagen',16,NULL,55.6802000,12.5720000,NULL),
 (107,'Pohang University of Science And Technology (POSTECH)',30,NULL,36.0141000,129.3224000,NULL),
 (108,'Nanjing University',15,NULL,32.0569000,118.7789000,NULL),
 (109,'Utrecht University',25,NULL,52.0853000,5.1740000,NULL),
 (110,'Lomonosov Moscow State University',31,NULL,55.7030000,37.5309000,NULL),
 (111,'Rheinisch-Westfälische Technische Hochschule Aachen',2,NULL,50.7797000,6.0660000,NULL),
 (112,'Technical University of Denmark',16,NULL,55.7856000,12.5217000,NULL),
 (113,'Tohoku University',7,NULL,38.2539000,140.8733000,NULL),
 (114,'Queen Mary University of London (QMUL)',39,NULL,51.5249000,-0.0400000,NULL),
 (115,'University of Wisconsin-Madison',4,NULL,43.0766000,-89.4125000,NULL),
 (116,'Qatar University',29,NULL,25.3770000,51.4890000,NULL),
 (117,'University of St Andrews',39,NULL,56.3417000,-2.7923000,NULL),
 (118,'Aalto University',17,NULL,60.1841000,24.8301000,NULL),
 (119,'University of California, Davis',4,NULL,38.5382000,-121.7617000,NULL),
 (120,'Pontificia Universidad Católica de Chile',14,NULL,-33.4413000,-70.6407000,NULL),
 (121,'University of Helsinki',17,NULL,60.1699000,24.9534000,NULL),
 (122,'University College Dublin',21,NULL,53.3065000,-6.2200000,NULL),
 (123,'Leiden University',25,NULL,52.1575000,4.4850000,NULL),
 (124,'Rice University',4,NULL,29.7174000,-95.4018000,NULL),
 (125,'University of Oslo',27,NULL,59.9399000,10.7218000,NULL),
 (126,'University of Waterloo',13,NULL,43.4723000,-80.5449000,NULL),
 (127,'Georgia Institute of Technology',4,NULL,33.7756000,-84.3963000,NULL),
 (128,'Indian Institute of Technology Delhi (IITD)',19,NULL,28.5457000,77.1926000,NULL),
 (129,'RMIT University',8,NULL,-37.8080000,144.9631000,NULL),
 (130,'Sungkyunkwan University',30,NULL,37.5879000,126.9936000,NULL),
 (131,'Universiti Kebangsaan Malaysia (UKM)',24,NULL,2.9292000,101.7810000,NULL),
 (132,'Sapienza University of Rome',22,NULL,41.9028000,12.5156000,NULL),
 (133,'Indian Institute of Technology Bombay (IITB)',19,NULL,19.1334000,72.9133000,NULL),
 (134,'Humboldt-Universität zu Berlin',2,NULL,52.5170000,13.3937000,NULL),
 (135,'Aarhus University',16,NULL,56.1685000,10.2039000,NULL),
 (136,'University of Bath',39,NULL,51.3779000,-2.3276000,NULL),
 (137,'University of Science and Technology of China',15,NULL,31.8376000,117.2630000,NULL),
 (138,'Universiti Putra Malaysia (UPM)',24,NULL,2.9994000,101.7072000,NULL),
 (139,'Universiti Sains Malaysia (USM)',24,NULL,5.3569000,100.3019000,NULL),
 (140,'Newcastle University',39,NULL,54.9783000,-1.6178000,NULL),
 (141,'Alma Mater Studiorum - University of Bologna',22,NULL,44.4969000,11.3520000,NULL),
 (142,'Macquarie University',8,NULL,-33.7756000,151.1123000,NULL),
 (143,'Eindhoven University of Technology',25,NULL,51.4489000,5.4907000,NULL),
 (144,'Erasmus University Rotterdam',25,NULL,51.9166000,4.5250000,NULL),
 (145,'University of North Carolina, Chapel Hill',4,NULL,35.9049000,-79.0469000,NULL),
 (146,'King Saud University',32,NULL,24.7257000,46.6283000,NULL),
 (147,'Texas A&M University',4,NULL,30.6186000,-96.3365000,NULL),
 (148,'University of Southern California',4,NULL,34.0224000,-118.2851000,NULL),
 (149,'Stockholm University',35,NULL,59.3628000,18.0583000,NULL),
 (150,'University of Groningen',25,NULL,53.2194000,6.5665000,NULL),
 (151,'University of Liverpool',39,NULL,53.4068000,-2.9663000,NULL),
 (152,'University of Cape Town',34,NULL,-33.9577000,18.4612000,NULL),
 (153,'Western University',13,NULL,43.0096000,-81.2737000,NULL),
 (154,'University of Vienna',11,NULL,48.2130000,16.3600000,NULL),
 (155,'Universiti Teknologi Malaysia (UTM)',24,NULL,1.5590000,103.6370000,NULL),
 (156,'Wageningen University & Research',25,NULL,51.9851000,5.6639000,NULL),
 (157,'University of Exeter',39,NULL,50.7371000,-3.5351000,NULL),
 (158,'University of Geneva',36,NULL,46.1977000,6.1423000,NULL),
 (159,'Lancaster University',39,NULL,54.0104000,-2.7877000,NULL),
 (160,'University of Basel',36,NULL,47.5584000,7.5836000,NULL),
 (161,'Hanyang University',30,NULL,37.5572000,127.0458000,NULL),
 (162,'University of Barcelona',3,NULL,41.3867000,2.1639000,NULL),
 (163,'Michigan State University',4,NULL,42.7018000,-84.4822000,NULL),
 (164,'Ghent University',12,NULL,51.0479000,3.7278000,NULL),
 (165,'King Abdul Aziz University (KAU)',32,NULL,21.4963000,39.2447000,NULL),
 (166,'Nagoya University',7,NULL,35.1547000,136.9660000,NULL),
 (167,'Chalmers University of Technology',35,NULL,57.6896000,11.9730000,NULL),
 (168,'Al-Farabi Kazakh National University',23,NULL,43.2250000,76.9066000,NULL),
 (169,'Washington University in St. Louis',4,NULL,38.6488000,-90.3108000,NULL),
 (170,'University of Montreal',13,NULL,45.5048000,-73.6136000,NULL),
 (171,'University of York',39,NULL,53.9484000,-1.0536000,NULL),
 (172,'Hokkaido University',7,NULL,43.0746000,141.3400000,NULL),
 (173,'Kyushu University',7,NULL,33.5950000,130.2170000,NULL),
 (174,'Universitat Autónoma de Barcelona',3,NULL,41.5008000,2.1076000,NULL),
 (175,'Arizona State University',4,NULL,33.4219000,-111.9332000,NULL),
 (176,'McMaster University',13,NULL,43.2609000,-79.9192000,NULL),
 (177,'Universidad de Chile',14,NULL,-33.4443000,-70.6506000,NULL),
 (178,'National Tsing Hua University',37,NULL,24.7961000,120.9967000,NULL),
 (179,'Khalifa University of Science and Technology',38,NULL,24.4205000,54.4350000,NULL),
 (180,'Tongji University',15,NULL,31.2820000,121.5020000,NULL),
 (181,'University of California, Santa Barbara (UCSB)',4,NULL,34.4139000,-119.8489000,NULL),
 (182,'Indian Institute of Technology Madras (IITM)',19,NULL,12.9915000,80.2337000,NULL),
 (183,'Cardiff University',39,NULL,51.4866000,-3.1791000,NULL),
 (184,'Emory University',4,NULL,33.7971000,-84.3222000,NULL),
 (185,'Curtin University',8,NULL,-32.0031000,115.8942000,NULL),
 (186,'University of Bern',36,NULL,46.9505000,7.4386000,NULL),
 (187,'University of Wollongong',8,NULL,-34.4054000,150.8791000,NULL),
 (188,'Wuhan University',15,NULL,30.5390000,114.3600000,NULL),
 (189,'Tecnológico de Monterrey (ITESM)',9,NULL,25.6514000,-100.2899000,NULL),
 (190,'University Complutense Madrid',3,NULL,40.4489000,-3.7287000,NULL),
 (191,'UNIVERSITAS INDONESIA',20,NULL,-6.3624000,106.8272000,NULL),
 (192,'Ohio State University',4,NULL,40.0067000,-83.0305000,NULL),
 (193,'Queen''s University, Ontario',13,NULL,44.2253000,-76.4951000,NULL),
 (194,'Université Catholique de Louvain (UCL)',12,NULL,50.6682000,4.6129000,NULL),
 (195,'Universität Hamburg',2,NULL,53.5666000,9.9847000,NULL),
 (196,'University of Reading',39,NULL,51.4414000,-0.9456000,NULL),
 (197,'Vrije Universiteit Amsterdam',25,NULL,52.3338000,4.8656000,NULL),
 (198,'Waseda University',7,NULL,35.7099000,139.7197000,NULL),
 (199,'University of Otago',26,NULL,-45.8655000,170.5139000,NULL),
 (200,'Vienna University of Technology',11,NULL,48.1995000,16.3696000,NULL),
 (201,'National Yang Ming Chiao Tung University',37,NULL,24.7866000,120.9970000,NULL),
 (202,'Queen''s University Belfast',39,NULL,54.5844000,-5.9341000,NULL),
 (203,'Universidad Nacional de Córdoba (UNC)',1,NULL,-31.4388000,-64.1890000,NULL),
 (204,'Universidad Siglo 21',1,NULL,-31.3230400,-64.2218800,NULL),
 (205,'Universidad Nacional de La Plata',1,NULL,-34.9114000,-57.9536000,NULL),
 (206,'Pontificia Universidad Católica Argentina Santa María de los Buenos Aires - UCA',1,NULL,-34.6159000,-58.3658000,NULL),
 (207,'Universidad Austral - Argentina',1,NULL,-34.4550000,-58.8665000,NULL),
 (208,'Universidad de Palermo',1,NULL,-34.5983000,-58.4217000,NULL),
 (209,'Universidad Torcuato Di Tella',1,NULL,-34.5485000,-58.4471000,NULL),
 (210,'Instituto Tecnológico de Buenos Aires (ITBA)',1,NULL,-34.6377000,-58.4016000,NULL),
 (211,'Universidad Nacional de Rosario',1,NULL,-32.9468000,-60.6339000,NULL),
 (212,'Universidad de San Andrés',1,NULL,-34.4487000,-58.5465000,NULL),
 (213,'Universidad Argentina de la Empresa -UADE',1,NULL,-34.6164000,-58.3813000,NULL),
 (214,'Universidad Tecnológica Nacional (UTN)',1,NULL,-34.5986000,-58.4202000,NULL),
 (215,'Universidad Nacional de Cuyo',1,NULL,-32.8860000,-68.8790000,NULL),
 (216,'Universidad Nacional de San Martín (UNSAM)',1,NULL,-34.5773000,-58.5274000,NULL),
 (217,'Universidade Estadual de Campinas (Unicamp)',5,NULL,-22.8172000,-47.0696000,NULL),
 (218,'Universidade Federal do Rio de Janeiro',5,NULL,-22.8627000,-43.2234000,NULL),
 (219,'Universidade Estadual Paulista "Júlio de Mesquita Filho" (UNESP)',5,NULL,-23.5558000,-46.6396000,NULL),
 (220,'Pontificia Universidade Católica do Rio de Janeiro (PUC - Rio)',5,NULL,-22.9795000,-43.2324000,NULL),
 (221,'Universidade Federal de Minas Gerais',5,NULL,-19.8707000,-43.9679000,NULL),
 (222,'Universidad Nacional de Colombia',10,NULL,4.6382000,-74.0840000,NULL),
 (223,'Pontificia Universidad Católica del Perú (PUCP)',28,NULL,-12.0696000,-77.0802000,NULL);

-- 3.4 Usuarios (password_hash simulado — en producción se usa bcrypt)
INSERT INTO usuario (nombre, email, password_hash, pais_id, nivel_educativo, rol) VALUES
('Jesús Fariña',       'jesus.farina@email.com',    '$2a$10$abcdefghijklmnopqrstuuABCDEFG', 1, 'Universitario', 'admin'),
('María García',       'maria.garcia@email.com',    '$2a$10$xyzxyzxyzxyzxyzxyzxyzuXYZXYZX', 1, 'Universitario', 'usuario'),
('Hans Müller',        'hans.muller@email.com',     '$2a$10$qwertyuiopasdfghjklzuQWERTYU', 2, 'Posgrado',      'usuario'),
('Ana López',          'ana.lopez@email.com',       '$2a$10$mnbvcxzlkjhgfdsapoiuMNBVCXZ', 3, 'Universitario', 'usuario'),
('Carlos Rodríguez',   'carlos.rodriguez@email.com','$2a$10$1234567890abcdef12345u1234567', 9, 'Secundario',    'usuario');

-- 3.5 Oportunidades educativas (datos realistas)
INSERT INTO oportunidad (titulo, descripcion, tipo, universidad_id, area_estudio_id, fecha_limite, url_oficial, idioma, modalidad, latitud, longitud, estado) VALUES
('Beca DAAD para Maestría en Ingeniería',
 'El Servicio Alemán de Intercambio Académico (DAAD) ofrece becas completas para programas de maestría en universidades alemanas. Cubre matrícula, manutención mensual de 934 EUR y seguro médico.',
 'beca', 2, 1, '2026-10-15', 'https://www.daad.de/en/study-and-research-in-germany/', 'Alemán/Inglés', 'presencial', 52.5125300, 13.3269300, 'activa'),

('Erasmus Mundus Joint Masters',
 'Programa de la Comisión Europea que financia maestrías conjuntas entre universidades europeas. Incluye beca mensual, viajes y seguro.',
 'beca', 6, 2, '2026-09-01', 'https://erasmus-plus.ec.europa.eu/', 'Inglés/Francés', 'presencial', 48.8479400, 2.3563900, 'activa'),

('Curso Online: Inteligencia Artificial (MIT OpenCourseWare)',
 'Curso gratuito de introducción a la IA ofrecido por el MIT a través de su plataforma de educación abierta.',
 'curso', 4, 1, '2026-12-31', 'https://ocw.mit.edu/', 'Inglés', 'virtual', 42.3601000, -71.0942000, 'activa'),

('Intercambio Académico UBA-UNAM',
 'Programa de movilidad estudiantil entre la UBA y la UNAM. Duración de un semestre con reconocimiento de materias.',
 'intercambio', 9, 4, '2026-08-15', 'https://www.unam.mx/movilidad', 'Español', 'presencial', 19.3320600, -99.1871400, 'activa'),

('Pasantía en Investigación — University of Tokyo',
 'Programa de pasantías de investigación para estudiantes de grado y posgrado en áreas STEM. Duración: 3 meses.',
 'pasantia', 7, 6, '2026-07-01', 'https://www.u-tokyo.ac.jp/en/research/', 'Inglés/Japonés', 'presencial', 35.7126000, 139.7620000, 'activa'),

('Voluntariado Educativo en Comunidades Rurales — Colombia',
 'Programa de voluntariado para enseñar tecnología en escuelas rurales de Colombia. Incluye alojamiento y alimentación.',
 'voluntariado', 10, 8, '2026-11-30', 'https://www.uniandes.edu.co/voluntariado', 'Español', 'presencial', 4.6012700, -74.0649200, 'activa'),

('Beca Fulbright para Doctorado en Estados Unidos',
 'Beca completa para estudios de doctorado en universidades estadounidenses. Cubre matrícula, estipendio mensual y pasajes.',
 'beca', 4, 3, '2026-06-30', 'https://fulbright.edu.ar/', 'Inglés', 'presencial', 42.3601000, -71.0942000, 'activa'),

('Curso de Derecho Internacional Humanitario — Salamanca',
 'Curso de especialización en derecho internacional humanitario ofrecido por la Universidad de Salamanca.',
 'curso', 3, 7, '2026-05-20', 'https://www.usal.es/cursos', 'Español', 'hibrida', 40.9607800, -5.6689500, 'vencida'),

('Beca de Investigación en Ciencias Ambientales — Melbourne',
 'Beca de investigación para estudiantes de posgrado en ciencias ambientales y cambio climático.',
 'beca', 8, 6, '2025-12-01', 'https://www.unimelb.edu.au/research', 'Inglés', 'presencial', -37.7983000, 144.9610600, 'vencida'),

('Programa de Innovación Tecnológica — USP (borrador)',
 'Borrador de programa de innovación tecnológica de la USP pendiente de publicación.',
 'curso', 5, 1, NULL, NULL, 'Portugués', 'virtual', -23.5587000, -46.7319700, 'borrador');

-- 3.6 Favoritos (usuarios guardan oportunidades de interés)
INSERT INTO favorito (usuario_id, oportunidad_id) VALUES
(2, 1),  -- María guarda Beca DAAD
(2, 2),  -- María guarda Erasmus
(2, 4),  -- María guarda Intercambio UBA-UNAM
(3, 1),  -- Hans guarda Beca DAAD
(3, 5),  -- Hans guarda Pasantía Tokyo
(4, 2),  -- Ana guarda Erasmus
(4, 6),  -- Ana guarda Voluntariado Colombia
(5, 3),  -- Carlos guarda Curso MIT AI
(5, 4);  -- Carlos guarda Intercambio UBA-UNAM

-- 3.7 Preferencias de usuario (áreas de interés)
INSERT INTO preferencia_usuario (usuario_id, area_estudio_id) VALUES
(2, 1),  -- María: Ingeniería
(2, 4),  -- María: Economía
(3, 1),  -- Hans: Ingeniería
(3, 6),  -- Hans: Ciencias Exactas
(4, 2),  -- Ana: Ciencias Sociales
(4, 5),  -- Ana: Arte y Humanidades
(5, 1),  -- Carlos: Ingeniería
(5, 8);  -- Carlos: Educación


-- ===========================================
-- SECCIÓN 4: CONSULTAS FUNCIONALES (SELECT)
-- Cada consulta responde a un requerimiento
-- ===========================================

-- Consulta 1 (RF07): Búsqueda multi-criterio — Becas activas en Europa
-- Simula el CU-07 con filtros combinados
SELECT o.id, o.titulo, o.tipo, o.fecha_limite, o.idioma, o.modalidad,
       u.nombre AS universidad, p.nombre AS pais, p.continente
FROM oportunidad o
  JOIN universidad u ON o.universidad_id = u.id
  JOIN pais p ON u.pais_id = p.id
WHERE o.estado = 'activa'
  AND o.tipo = 'beca'
  AND p.continente = 'Europa'
ORDER BY o.fecha_limite ASC;

-- Consulta 2 (RF07): Búsqueda por área de estudio y modalidad
SELECT o.titulo, o.tipo, ae.nombre AS area, o.modalidad, o.fecha_limite
FROM oportunidad o
  JOIN area_estudio ae ON o.area_estudio_id = ae.id
WHERE o.estado = 'activa'
  AND ae.nombre = 'Ingeniería y Tecnología'
  AND o.modalidad IN ('virtual', 'hibrida')
ORDER BY o.fecha_limite;

-- Consulta 3 (RF12): Oportunidades próximas a vencer (panel de destacados)
SELECT o.titulo, o.tipo, o.fecha_limite,
       DATEDIFF(o.fecha_limite, CURDATE()) AS dias_restantes,
       p.nombre AS pais
FROM oportunidad o
  JOIN universidad u ON o.universidad_id = u.id
  JOIN pais p ON u.pais_id = p.id
WHERE o.estado = 'activa'
  AND o.fecha_limite BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 90 DAY)
ORDER BY o.fecha_limite ASC;

-- Consulta 4 (RF10): Favoritos de un usuario con detalle
SELECT f.fecha_guardado, o.titulo, o.tipo, o.fecha_limite,
       u.nombre AS universidad, p.nombre AS pais
FROM favorito f
  JOIN oportunidad o ON f.oportunidad_id = o.id
  JOIN universidad u ON o.universidad_id = u.id
  JOIN pais p ON u.pais_id = p.id
WHERE f.usuario_id = 2  -- María García
ORDER BY f.fecha_guardado DESC;

-- Consulta 5 (RF13): Reporte — Cantidad de oportunidades por tipo
SELECT tipo, COUNT(*) AS cantidad,
       ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM oportunidad), 1) AS porcentaje
FROM oportunidad
WHERE estado = 'activa'
GROUP BY tipo
ORDER BY cantidad DESC;

-- Consulta 6 (RF13): Reporte — Ranking de países por oportunidades activas
SELECT p.nombre AS pais, p.continente, COUNT(o.id) AS total_oportunidades
FROM pais p
  LEFT JOIN universidad u ON p.id = u.pais_id
  LEFT JOIN oportunidad o ON u.id = o.universidad_id AND o.estado = 'activa'
GROUP BY p.id, p.nombre, p.continente
HAVING total_oportunidades > 0
ORDER BY total_oportunidades DESC;

-- Consulta 7 (RF14): Sugerencias personalizadas basadas en preferencias
-- Para el usuario 2 (María), busca oportunidades activas en sus áreas de interés
SELECT o.titulo, o.tipo, ae.nombre AS area, o.fecha_limite, p.nombre AS pais
FROM oportunidad o
  JOIN area_estudio ae ON o.area_estudio_id = ae.id
  JOIN universidad u ON o.universidad_id = u.id
  JOIN pais p ON u.pais_id = p.id
WHERE o.estado = 'activa'
  AND o.area_estudio_id IN (
    SELECT area_estudio_id FROM preferencia_usuario WHERE usuario_id = 2
  )
  AND o.id NOT IN (
    SELECT oportunidad_id FROM favorito WHERE usuario_id = 2
  )
ORDER BY o.fecha_limite ASC;

-- Consulta 8 (RF11): Datos para el globo 3D — Oportunidades con coordenadas
SELECT o.id, o.titulo, o.tipo, o.latitud, o.longitud,
       u.nombre AS universidad, p.nombre AS pais
FROM oportunidad o
  JOIN universidad u ON o.universidad_id = u.id
  JOIN pais p ON u.pais_id = p.id
WHERE o.estado = 'activa'
  AND o.latitud IS NOT NULL
  AND o.longitud IS NOT NULL;


-- ===========================================
-- SECCIÓN 5: ACTUALIZACIONES (UPDATE)
-- ===========================================

-- 5.1 Cambiar estado de oportunidad vencida (transición ACTIVA→VENCIDA)
-- Esto se ejecutaría automáticamente con un CRON o scheduled task
UPDATE oportunidad
SET estado = 'vencida'
WHERE estado = 'activa'
  AND fecha_limite < CURDATE();

-- 5.2 Actualizar perfil de usuario (CU-06)
UPDATE usuario
SET nivel_educativo = 'Posgrado',
    nombre = 'María García López'
WHERE id = 2;

-- 5.3 Actualizar coordenadas de una universidad
UPDATE universidad
SET latitud = 52.5130000, longitud = 13.3280000
WHERE id = 2;

-- 5.4 Republicar oportunidad (transición BORRADOR→ACTIVA)
UPDATE oportunidad
SET estado = 'activa',
    fecha_limite = '2026-12-15',
    url_oficial = 'https://www.usp.br/innovacion',
    fecha_publicacion = CURRENT_TIMESTAMP
WHERE id = 10;


-- ===========================================
-- SECCIÓN 6: ELIMINACIÓN (DELETE)
-- ===========================================

-- 6.1 Eliminar un favorito (CU-09: usuario quita de favoritos)
DELETE FROM favorito
WHERE usuario_id = 5 AND oportunidad_id = 3;

-- 6.2 Borrado LÓGICO de oportunidad (transición →ELIMINADA)
-- NO se usa DELETE físico para mantener integridad referencial
-- con favoritos y estadísticas históricas.
UPDATE oportunidad
SET estado = 'eliminada'
WHERE id = 9;

-- 6.3 Eliminar preferencia de usuario
DELETE FROM preferencia_usuario
WHERE usuario_id = 4 AND area_estudio_id = 5;

-- Nota: El borrado físico de oportunidades (DELETE FROM oportunidad)
-- no se implementa en el sistema por diseño. Se usa borrado lógico
-- (cambio de estado a 'eliminada') para preservar la integridad
-- referencial con las tablas favorito y para reportes históricos.
