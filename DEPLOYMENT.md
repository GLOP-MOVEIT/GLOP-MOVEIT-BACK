# Guide de déploiement

## Prérequis

Avant de commencer, assurez-vous de disposer des éléments suivants :

- Une machine virtuelle (VM) accessible via SSH
- Docker et Docker Compose installés sur la VM
- Git installé sur la VM
- Les accès aux dépôts Git du front-end et du back-end

---

## Étape 1 — Préparer la VM

Connectez-vous à votre VM via SSH :

```bash
ssh user@adresse-ip-de-la-vm
```

Vérifiez que Docker est bien installé :

```bash
docker --version
docker compose version
```

---

## Étape 2 — Cloner les dépôts

Clonez le dépôt back-end :

```bash
git clone git@github.com:GLOP-MOVEIT/GLOP-MOVEIT-BACK.git
```

Clonez le dépôt front-end :

```bash
git clone git@github.com:GLOP-MOVEIT/GLOP-MOVEIT-FRONT.git
```

---

## Étape 3 — Lancer le back-end

Placez-vous dans le dossier du back-end :

```bash
cd GLOP-MOVEIT-BACK
```

Lancez les services avec Docker Compose :

```bash
docker compose up -d --build
```

> L'option `-d` permet de lancer les conteneurs en arrière-plan.  
> L'option `--build` force la reconstruction des images avant le démarrage.

---

## Étape 4 — Lancer le front-end

Placez-vous dans le dossier du front-end :

```bash
cd GLOP-MOVEIT-FRONT
```

Lancez les services en mode production :

```bash
docker compose up -d --build --profile=prod
```

> L'option `--profile=prod` active les services spécifiques au profil de production définis dans le `docker-compose.yml`.

---

## Vérification

Pour vérifier que tous les conteneurs tournent correctement :

```bash
docker ps
```

Pour consulter les logs d'un conteneur en cas de problème :

```bash
docker compose logs -f
```