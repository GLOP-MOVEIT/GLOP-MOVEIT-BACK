package com.moveit.notification.entity;

/**
 * Représente la cible d'une subscription ou d'une notification.
 * GLOBAL  : concerne tous les utilisateurs (pas de targetId nécessaire).
 * COMPETITION : concerne une compétition spécifique (targetId = competitionId).
 * CHAMPIONSHIP : concerne un championnat spécifique (targetId = championshipId).
 */
public enum TargetType {
    GLOBAL,
    COMPETITION,
    CHAMPIONSHIP
}

