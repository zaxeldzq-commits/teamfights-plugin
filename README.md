# Teamfights (Bard + Teams) — Paper 1.21.11

Plugin para Paper/Purpur 1.21.11 pensado para teamfights estilo HCF, compatible con
**NethPot** (el plugin de combate que restaura el PvP 1.8/1.9, sin attack-cooldown de 1.9+).

Este plugin **no reemplaza a NethPot**: asume que NethPot (u otro plugin de combate legacy)
ya está instalado en el server para el hit registration/knockback. Este plugin solo agrega
la capa de "clases" (Bard) + teams + balance de daño.

## Compilar

Necesitas JDK 21 y Maven, con conexión a internet (para bajar `paper-api` del repo de PaperMC):

```bash
mvn clean package
```

El jar queda en `target/Teamfights.jar`.

> Si tu build exacto de 1.21.11 no encuentra `1.21.11-R0.1-SNAPSHOT` en el repo de Paper,
> revisa qué versiones hay disponibles en
> https://repo.papermc.io/service/rest/repository/browse/maven-public/io/papermc/paper/paper-api/
> y ajusta la versión en `pom.xml` (por ejemplo si tu server corre un pre-release específico).

## Instalar

1. Copia `Teamfights.jar` a la carpeta `plugins/` del server.
2. Asegúrate de tener **NethPot** también en `plugins/`.
3. Inicia el server, se generará `plugins/Teamfights/config.yml` y `teams.yml`.

## Comandos

- `/bard` — da el kit completo del Bard (varas holdable + notas clickeables).
- `/team create <nombre>` — crea un team.
- `/team invite <jugador>` — invita (solo el líder).
- `/team accept <team>` — acepta una invitación.
- `/team leave` — sales del team.
- `/team disband` — disuelve el team (solo líder).
- `/team list` — lista miembros.
- `/team info` — info del team.
- `/team ff` — activa/desactiva friendly fire SOLO para tu team (además hay un switch global en config).
- `/teamfights reload` — recarga config.yml (op / permiso `teamfights.admin`).

## Cómo funciona el Bard

- **Holdables** (varas): las sostienes en la mano secundaria (tecla F por defecto) y mientras
  las tengas ahí, te dan un efecto pasivo a ti mismo (Speed I / Strength I / Resistance I).
  Se re-aplican cada segundo así que se mantienen mientras la sostengas, y desaparecen apenas
  la sueltas o cambias de item.
- **Clickeables** (notas): click derecho para lanzar un efecto de área a ti + tu team dentro
  de un radio (config `bard.clickable-radius`, default 8 bloques). Tienen cooldown individual.

## Balance (para que nadie muera de 1 hit)

- Todos los amplifiers están topados a nivel I o II como máximo (nada de Strength II/III ni
  Speed III que sí rompían el balance en HCF viejo).
- `combat.enable-damage-cap` + `combat.max-damage-percent` en config.yml: ningún golpe puede
  quitar más de un % (55% por defecto) de la vida máxima de la víctima, sin importar combos de
  potions/encantamientos. Ajústalo a tu gusto (súbelo si tu server es más "sweaty", bájalo si
  quieres fights más largos).
- Los clickeables tienen cooldowns de 20-45s para que no se puedan spammear en loop.

## Ideas para extender (no incluidas todavía)

- GUI con `/bard menu` para armar tu kit en vez de dar todo automáticamente.
- Más clases (Archer, Miner, Bridger, etc.) siguiendo el mismo patrón de `BardEffectDefinition`.
- Integración con un plugin de Factions/Kits existente para restringir el kit por clase elegida.
- Chat de team (`/team chat` o prefijo automático en el chat global).

Si quieres, dime cuál de estas quieres que te agregue primero y seguimos iterando.
