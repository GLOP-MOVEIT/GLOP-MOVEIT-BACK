-- V2__seed_notification_levels.sql
-- Seed des 3 niveaux de notification (US Admin)

INSERT INTO notification_level (level_name, priority, description) VALUES
    ('CRITIQUE', 1, 'Incidents, urgences, alertes de sécurité - Livraison < 30s'),
    ('ORGANISATIONNEL', 2, 'Convocations, reports, plannings, validations'),
    ('INFORMATIONNEL', 3, 'Informations générales, rappels');
