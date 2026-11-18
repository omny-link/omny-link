# CRM UI2

A proof of concept using modern tools and libraries (Svelte).

## Configuring backend

This UI reads the backend base URL from `VITE_API_BASE_URL`.

1. Copy `.env.example` to `.env`.
2. Set the backend URL you want to use.

Example for hosted backend:

```bash
VITE_API_BASE_URL=https://crm.knowprocess.com
```

## Developing

Once you've created a project and installed dependencies with `npm install` (or `pnpm install` or `yarn`), start a development server:

```bash
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

## Building

To create a production version of your app:

```bash
npm run build
```

You can preview the production build with `npm run preview`.

> To deploy your app, you may need to install an [adapter](https://svelte.dev/docs/kit/adapters) for your target environment.
