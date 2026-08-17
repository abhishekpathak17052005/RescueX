# RescueX

RescueX is an AI-assisted emergency coordination mobile MVP: users can activate SOS, share a location, complete a calm AI check-in, and follow a responder workflow in clearly labeled demo mode.

## Run & Operate

- `pnpm --filter @workspace/api-server run dev` — run the API server
- `pnpm --filter @workspace/rescuex run dev` — run the Expo mobile app
- `pnpm run typecheck` — full typecheck across all packages
- `pnpm run build` — typecheck + build all packages
- `pnpm --filter @workspace/api-spec run codegen` — regenerate API hooks and Zod schemas from the OpenAPI spec
- `pnpm --filter @workspace/db run push` — push DB schema changes (dev only)
- Optional production env: `DATABASE_URL`, `JWT_SECRET`, `VAPI_API_KEY`, `VAPI_ASSISTANT_ID`, and `FRONTEND_URL` (see `artifacts/api-server/.env.example`)

## Stack

- pnpm workspaces, Node.js 24, TypeScript 5.9
- API: Express 5
- DB: PostgreSQL + Drizzle ORM
- Validation: Zod (`zod/v4`), `drizzle-zod`
- API codegen: Orval (from OpenAPI spec)
- Build: esbuild (CJS bundle)

## Where things live

- `artifacts/rescuex/app/` — Expo Router screens for the user, emergency, responder, history, contacts, profile, and settings flows.
- `artifacts/rescuex/state/RescueXContext.tsx` — persisted demo state and incident lifecycle.
- `artifacts/rescuex/services/location.ts` — native Expo Location with a web geolocation fallback.
- `artifacts/rescuex/components/RescueUI.tsx` — shared mobile UI primitives and emergency timeline/map treatments.
- `artifacts/api-server/src/routes/rescuex.ts` — backend contract for auth, incidents, contacts, responder status, and Vapi webhook handling.
- `artifacts/api-server/src/lib/rescuex-store.ts` — in-memory demo store used when production services are not configured.

## Architecture decisions

- Demo Mode is the default so a hackathon presentation never depends on Vapi, push providers, a GPS fix, or a live database.
- Location is requested only when the user activates SOS or explicitly refreshes location; the app does not continuously track.
- The responder workflow shares the same incident lifecycle as the user view so each action updates the same persisted demo incident.
- The backend keeps the Vapi provider optional and returns an explicit `provider: "demo"` response when live credentials are absent.

## Product

The mobile MVP includes a low-tap SOS confirmation flow, current-location sharing, simulated AI emergency information collection, incident summaries, dynamic response timelines, emergency contact CRUD, history, profile role switching, responder queue/incident actions, and safe offline-friendly demo behavior.

## User preferences

No additional user preferences recorded.

## Gotchas

- The mobile app uses AsyncStorage for its demo state; the API server uses an in-memory store until a production database adapter is configured.
- Never place Vapi, database, or auth secrets in the Expo bundle. Keep them on the API server.
- RescueX is not an official emergency service and must not automatically contact real emergency services during development.

## Pointers

- See the `pnpm-workspace` skill for workspace structure, TypeScript setup, and package details
