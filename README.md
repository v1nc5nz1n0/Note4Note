**Note4Note** è una web application per la gestione di un sistema di note condivise, progettata con un'architettura moderna a microservizi, containerizzata e gestita con un approccio DevOps.

---

## Architettura

L'applicazione segue un'architettura a microservizi con una netta separazione tra backend e frontend.

- **Backend**: Sviluppato in **Java 21** con **Spring Boot 3**. Espone un'API RESTful documentata tramite OpenAPI/Swagger per tutta la logica di business.
- **Frontend**: Un'applicazione single-page minimale e funzionale realizzata in **HTML, CSS e JavaScript Vanilla**.
- **Database**: Utilizza uno stack ibrido:
  - **PostgreSQL**: Per la persistenza principale dei dati relazionali (utenti, note, tag, condivisioni).
  - **MongoDB**: Per l'indicizzazione e le funzionalità di ricerca full-text sulle note e mediante tag.
- **Sicurezza**: L'autenticazione è gestita tramite **JWT (JSON Web Tokens)**, con access e refresh token.
- **DevOps**: L'intera applicazione è containerizzata con **Docker** e il ciclo di vita è automatizzato tramite una pipeline **CI/CD** su **GitHub Actions**.

---

## Esecuzione in Locale (Ambiente di Sviluppo)

Per eseguire l'applicazione in locale, sono necessari **Docker** e **Docker Compose**.

### 1. Configurazione Iniziale

1.  Clona il repository:
    ```bash
    git clone https://github.com/v1nc5nz1n0/Note4Note.git
    cd Note4Note
    ```

2.  Crea un file `.env` nella root del progetto. Questo file conterrà tutte le credenziali e i secret necessari. Copia il contenuto seguente e personalizzalo se necessario:

    ```env
    # PostgreSQL Settings
    POSTGRES_DB=notefournote
    POSTGRES_USER=dbuser
    POSTGRES_PASSWORD=dbpass

    # MongoDB Settings
    MONGO_USER=dbuser
    MONGO_PASSWORD=dbpass

    # JWT Settings
    JWT_SECRET="YourLongSecretForSignJwtTokens"
    JWT_ACCESS_EXPIRATION=3600000
    JWT_REFRESH_EXPIRATION=86400000
    ```

### 2. Avvio dell'Applicazione

Una volta configurato il file `.env`, avvia l'intera architettura con un singolo comando:

```bash
docker-compose up --build
```

Questo comando avvierà tutti i servizi definiti nel file `docker-compose.yml`:
-   **notefournote**: Il backend Spring Boot (live-reload attivo sulla cartella `./backend`).
-   **postgres**: Il database PostgreSQL.
-   **mongodb**: Il database MongoDB.
-   **adminer**: Un'interfaccia web per gestire PostgreSQL (disponibile su `http://localhost:8081`).
-   **mongo-express**: Un'interfaccia web per gestire MongoDB (disponibile su `http://localhost:8082`).

L'applicazione sarà accessibile all'URL: `http://localhost:8080/dashboard`.

---

## API Endpoints e Documentazione

L'API REST del backend è completamente documentata utilizzando OpenAPI 3.

-   **Documentazione Swagger UI**: `http://localhost:8080/swagger-ui.html`

### Principali Endpoint:
-   `POST /api/v1/auth/register`: Registra un nuovo utente.
-   `POST /api/v1/auth/login`: Effettua il login e ottiene i token JWT (access e refresh).
-   `POST /api/v1/notes`: Crea una nuova nota.
-   `GET /api/v1/notes`: Ottiene tutte le note visibili all'utente (proprie e con lui condivise).
-   `GET /api/v1/notes/{noteId}`: Ottiene una nota specifica mediante id.
-   `POST /api/v1/notes/{noteId}/share`: Condivide una nota con altri utenti.
-   `GET /api/v1/notes/search`: Cerca note per testo e/o tag.

---

## Test con Postman

Nella cartella `/postman` del repository è presente una collection Postman (`NoteFourNote.postman_collection.json`) che puoi importare per testare tutte le funzionalità dell'API.

La collection è pre-configurata per:
1.  Registrare i 3 utenti di default: `pippo`, `pluto`, `paperino`.
2.  Effettuare il login con ciascun utente.
3.  Creare circa 10 note distribuite tra i vari utenti.
4.  Condividere alcune di queste note.

Per eseguire i test, importa la collection e lancia il "Runner" di Postman seguendo l'ordine delle cartelle (da 1 a 4). E' incluso anche il json dell'ambiente 'local', necessario non solo per l'url base ma anche per contenere tutte le variabili utilizzate durante l'esecuzione di chiamate sequenziali (token, id delle note, etc). Alle requests infatti sono associati script pre-chiamata (popolare il token di accesso) e post-chiamata (http status code, location header etc), al fine di guidare l'utente passo passo nell'esecuzione del test.

---

## Limitazioni

-   **Test Automatici**: Il progetto include una pipeline CI/CD ma mancano test unitari e di integrazione, che rappresentano il primo passo per migliorare la robustezza.
-   **Frontend**: Al momento non è gestito il refresh del token di accesso. Alla scadenza, l'utente viene riportato alla pagina di login.
