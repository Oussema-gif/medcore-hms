# MedCore HMS — Full Frontend Redesign Bundle

This package is a complete frontend snapshot based on the supplied MedCore HMS frontend snapshot, with a unified visual redesign applied across the application.

## Included

- Workspace-first MedCore application shell
- Compact contextual navigation rail
- Global command palette (`Ctrl+K` / `Cmd+K`)
- Responsive mobile navigation
- New dark clinical design system and tokens
- New dashboard visual language
- Redesigned patient directory
- Redesigned patient clinical workspace
- Updated shared buttons, inputs, tables, cards, alerts, pagination, forms, tabs and detail surfaces
- Existing routes and API calls preserved from the supplied frontend snapshot
- Existing backend contract is not modified

## Use

1. Back up your current `frontend` directory.
2. Extract this package as the frontend project directory, or copy its contents over your current frontend directory.
3. Keep your existing `.env.local` API base URL if needed.
4. Run:

```bash
npm install
npm run lint
npm run build
npm run dev
```

## Important

This bundle was reconstructed from the uploaded source snapshot and does not contain `node_modules`. The isolated build environment used for packaging could not install dependencies because registry access was unavailable, so `npm run lint` / `npm run build` must be run in your Fedora project environment.

The redesign intentionally preserves the existing business logic and backend integration rather than replacing the application with mock data.
