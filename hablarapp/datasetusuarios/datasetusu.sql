CREATE DATABASE app_tshusuarios;
USE app_tshusuarios;
-- =========================================
-- TABLA: terapeuta
-- =========================================
CREATE TABLE terapeuta (
    idCedula INT NOT NULL,
    nombreT VARCHAR(100) NOT NULL,
    correoT VARCHAR(150) NOT NULL,
    contrasenaT VARCHAR(255) NOT NULL,
    PRIMARY KEY (idCedula),
    UNIQUE KEY uk_terapeuta_correo (correoT)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================
-- TABLA: ejercicio
-- =========================================
CREATE TABLE ejercicio (
    id_Ejercicio INT NOT NULL AUTO_INCREMENT,
    nivel INT NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    PRIMARY KEY (id_Ejercicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================
-- TABLA: paciente
-- =========================================
CREATE TABLE paciente (
    id_paciente INT NOT NULL AUTO_INCREMENT,
    nombreP VARCHAR(100) NOT NULL,
    correoP VARCHAR(150) NOT NULL,
    contrasenaP VARCHAR(255) NOT NULL,
    estrella INT DEFAULT 0,
    fk_idCedula INT NOT NULL,
    PRIMARY KEY (id_paciente),
    UNIQUE KEY uk_paciente_correo (correoP),
    KEY idx_paciente_terapeuta (fk_idCedula),
    CONSTRAINT fk_paciente_terapeuta
        FOREIGN KEY (fk_idCedula)
        REFERENCES terapeuta (idCedula)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================
-- TABLA: asignacion
-- =========================================
CREATE TABLE asignacion (
    id_asignacion INT NOT NULL AUTO_INCREMENT,
    fecha DATE DEFAULT NULL,
    fk_terapeutaA INT DEFAULT NULL,
    fk_paciente INT DEFAULT NULL,
    fk_idEjercicio INT DEFAULT NULL,
    completado TINYINT(1) DEFAULT 0,
    PRIMARY KEY (id_asignacion),
    KEY idx_asignacion_terapeuta (fk_terapeutaA),
    KEY idx_asignacion_paciente (fk_paciente),
    KEY idx_asignacion_ejercicio (fk_idEjercicio),
    CONSTRAINT fk_asignacion_terapeuta
        FOREIGN KEY (fk_terapeutaA)
        REFERENCES terapeuta (idCedula)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_asignacion_paciente
        FOREIGN KEY (fk_paciente)
        REFERENCES paciente (id_paciente)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_asignacion_ejercicio
        FOREIGN KEY (fk_idEjercicio)
        REFERENCES ejercicio (id_Ejercicio)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================
-- TABLA: realizar
-- Ahora sí guarda:
-- - evidencia del paciente (archivo_url, tipo_archivo)
-- - resultado futuro del modelo
-- =========================================
CREATE TABLE realizar (
    id_realizar INT NOT NULL AUTO_INCREMENT,
    fechaRealiza DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    porcentaje DECIMAL(5,2) DEFAULT NULL,
    duracion VARCHAR(10) DEFAULT NULL,
    estrellas_ganadas TINYINT DEFAULT 0,

    tipo_archivo ENUM('audio','imagen') DEFAULT NULL,
    archivo_url VARCHAR(255) DEFAULT NULL,

    clasificacion_modelo VARCHAR(50) DEFAULT NULL,
    confianza_modelo DECIMAL(5,2) DEFAULT NULL,

    id_paciente INT DEFAULT NULL,
    id_ejercicio INT DEFAULT NULL,

    PRIMARY KEY (id_realizar),
    KEY idx_realizar_paciente (id_paciente),
    KEY idx_realizar_ejercicio (id_ejercicio),
    KEY idx_realizar_fecha (fechaRealiza),

    CONSTRAINT fk_realizar_paciente
        FOREIGN KEY (id_paciente)
        REFERENCES paciente (id_paciente)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    CONSTRAINT fk_realizar_ejercicio
        FOREIGN KEY (id_ejercicio)
        REFERENCES ejercicio (id_Ejercicio)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- =========================================
-- TABLA: ejercicio_orofacial
-- Catálogo de imagen guía del ejercicio
-- =========================================
CREATE TABLE ejercicio_orofacial (
    id_ejercicio_orofacial INT NOT NULL AUTO_INCREMENT,
    id_ejercicio INT NOT NULL,
    imagen_url VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_ejercicio_orofacial),
    KEY idx_orofacial_ejercicio (id_ejercicio),
    CONSTRAINT fk_orofacial_ejercicio
        FOREIGN KEY (id_ejercicio)
        REFERENCES ejercicio (id_Ejercicio)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
-- =========================================
-- TABLA: ejercicios_fonetico
-- Catálogo de audio guía del ejercicio
-- =========================================
CREATE TABLE ejercicios_fonetico (
    id_ejercicio_fonetico INT NOT NULL AUTO_INCREMENT,
    id_ejercicio INT NOT NULL,
    voz_url VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (id_ejercicio_fonetico),
    KEY idx_fonetico_ejercicio (id_ejercicio),
    CONSTRAINT fk_fonetico_ejercicio
        FOREIGN KEY (id_ejercicio)
        REFERENCES ejercicio (id_Ejercicio)
        ON DELETE CASCADE
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
-- =========================================
-- INSERTS: 24 ejercicios base
-- =========================================
INSERT INTO ejercicio (id_Ejercicio, nivel, descripcion) VALUES
(1, 1, 'ra'),
(2, 1, 're'),
(3, 1, 'ri'),
(4, 1, 'ro'),
(5, 1, 'ru'),
(6, 2, 'rra'),
(7, 2, 'rre'),
(8, 2, 'rri'),
(9, 2, 'rro'),
(10, 2, 'rru'),
(11, 3, 'Ferrocarril'),
(12, 3, 'Cigarro'),
(13, 3, 'Barril'),
(14, 3, 'Rita'),
(15, 3, 'Ruso'),
(16, 3, 'Rama'),
(17, 3, 'Rojo'),
(18, 3, 'Rosa'),
(19, 1, 'Lengua arriba'),
(20, 1, 'Lengua abajo'),
(21, 2, 'Lengua izquierda'),
(22, 2, 'Lengua derecha'),
(23, 3, 'Morder labio superior'),
(24, 3, 'Morder labio inferior');

