-- V1__create_notification_tables.sql
-- Création des tables pour le service de notifications

-- Table des niveaux de notification (Critique, Organisationnel, Informationnel)
CREATE TABLE notification_level (
    level_id SERIAL PRIMARY KEY,
    level_name VARCHAR(50) NOT NULL UNIQUE,
    priority INTEGER NOT NULL,
    description VARCHAR(255)
);

-- Table des notifications
-- notification_type est un enum (VARCHAR) stocké directement dans la table
CREATE TABLE notification (
    notification_id BIGSERIAL PRIMARY KEY,
    notification_user_id BIGINT NOT NULL,
    notification_type VARCHAR(50) NOT NULL,
    level_id INTEGER NOT NULL,
    notification_security INTEGER,
    notification_competition INTEGER,
    notification_name VARCHAR(255) NOT NULL,
    notification_body TEXT,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    notification_start_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_notification_level FOREIGN KEY (level_id) 
        REFERENCES notification_level(level_id) ON DELETE RESTRICT
);

-- Index pour optimiser les requêtes fréquentes
CREATE INDEX idx_notification_user ON notification(notification_user_id);
CREATE INDEX idx_notification_type ON notification(notification_type);
CREATE INDEX idx_notification_level ON notification(level_id);
CREATE INDEX idx_notification_read ON notification(is_read);
CREATE INDEX idx_notification_start_date ON notification(notification_start_date);
CREATE INDEX idx_notification_user_read ON notification(notification_user_id, is_read);
CREATE INDEX idx_notification_user_level ON notification(notification_user_id, level_id);
