# FantasyFUT web edition

A browser version of the SP4 group project, using the existing Java player/club
models, fixture generator, match simulator and point calculator. The JavaFX app
remains a separate entry point. This module builds without JavaFX or desktop media.

## Run locally

Requires Java 21+ and Maven. From the repository root:

```sh
mvn -f web/pom.xml package
java -jar web/target/fantasyfut-web.jar
```

Open http://localhost:7080. Create an account with a unique demo password, select
or quick-pick a 4–4–2 squad, save it, and simulate your first gameweek.

`SERVER_ORIGIN` is the exact browser origin used for CSRF checks. It defaults to
`http://localhost:7080`. Set it if using a different host/port. `PORT` defaults to
7080. `DATABASE_PATH` defaults to `./fantasy-web.sqlite` relative to the process.

## Rules and persistence

- 526 players and 20 clubs are loaded from the repository CSV seed files at build
  time; no external sports API is required. Rosters are the project's dataset,
  not current live football data.
- Each account has its own simulated season of 38 gameweeks. A season reset clears
  only that manager's results and points, keeping the account and lineup.
- Exactly 11 distinct players: 1 GK, 4 DEF, 4 MID and 2 FWD. The lineup locks after
  the first gameweek, preventing changes from altering earlier scores.
- A complete gameweek is written in one SQLite transaction; the request includes
  the expected week to reject duplicate/stale submissions.
- The web adapter aggregates separate scorer/assister events before calculating
  points and advances the gameweek once per round. A scorer or assister always
  receives at least one minute of playing time.
- Goal +4, assist +2, yellow −1, red −3. Playing 90 minutes adds 2; playing 1–89
  adds 1. These are the original project's scoring rules, not official FPL rules.
- The leaderboard shows total points and gameweeks played across independently
  simulated seasons. Manager names and lineups are public; passwords are not.
- Accounts use bcrypt password hashes. Sessions expire after 30 minutes of
  inactivity and require signing in again after an application restart.
- Mutations require the session CSRF token and exact Origin. Production cookies
  are Secure and HttpOnly. Caddy overwrites X-Real-IP for authentication throttling.
  The Docker app port is not published to the host.
- There is no password recovery or email collection in this student demo.

## Production

URL: https://fantasyfut.antwi.dk
Server directory: `/opt/fantasyfut`

Build locally, then copy `Dockerfile`, `.dockerignore`, `compose.yaml` and
`target/fantasyfut-web.jar` to the server, retaining their relative paths.

```sh
cd /opt/fantasyfut
docker compose up -d --build
docker compose ps
docker compose logs --tail=100
```

Add this block to the existing `/opt/cit/Caddyfile`, preserving its other sites:

```caddyfile
fantasyfut.antwi.dk {
    reverse_proxy fantasyfut-app:7080 {
        header_up X-Real-IP {remote_host}
    }
}
```

Validate, then reload:

```sh
docker exec cit-caddy-1 caddy validate --config /etc/caddy/Caddyfile
docker exec cit-caddy-1 caddy reload --config /etc/caddy/Caddyfile
```

The named volume `fantasyfut_game_data` persists the new SQLite database, including
WAL files. Old desktop accounts, passwords and results are not imported.
Do not use `docker compose down -v` unless you intend to delete all web game data.

For a consistent backup, briefly stop only FantasyFUT, archive its data, then
restart it. Copy backups off the server:

```sh
cd /opt/fantasyfut
umask 077
docker compose stop app
docker run --rm --volumes-from fantasyfut-app-1 -v "$PWD:/backup" alpine \
  tar -czf /backup/fantasy-backup.tar.gz -C /data .
docker compose start app
```

Tests cover account validation, lineup validation, isolated seasons and resets,
score aggregation, full-season limits, persistence, sessions and CSRF.
