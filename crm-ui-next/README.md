# CRM UI Next

**Status: experimental**

This is complete rewrite of the CRM user interface in modern TypeScript, using the Svelte framework.

## Features

- ✅ **SvelteKit 5** - Modern reactive framework
- ✅ **TypeScript** - Type-safe development
- ✅ **Bootstrap 5** - Responsive UI components
- ✅ **Keycloak Authentication** - JWT-based security
- ✅ **API Client** - Centralized with automatic token handling
- ✅ **Multi-tenant Support** - Dynamic tenant configuration

## Authentication

This app integrates with Keycloak for JWT authentication. See [JWT-INTEGRATION.md](./JWT-INTEGRATION.md) for details on:
- Automatic token refresh
- 401 error handling and retry
- Environment configuration
- API client usage

## Developing

Once you've created a project and installed dependencies with `npm install` (or `pnpm install` or `yarn`), start a development server:

```bash
npm run dev

# or start the server and open the app in a new browser tab
npm run dev -- --open
```

The development server will:
- Run on `http://localhost:5173` (or next available port)
- Connect to `http://localhost:8080` for API calls
- Use production Keycloak at `https://auth.knowprocess.com`

## Building

To create a production version of your app:

```bash
npm run build
```

You can preview the production build with `npm run preview`.

> To deploy your app, you may need to install an [adapter](https://svelte.dev/docs/kit/adapters) for your target environment.

## Project Structure

```
crm-ui-next/
├── src/
│   ├── lib/                    # Shared libraries
│   │   ├── apiClient.ts       # Centralized API client with JWT
│   │   ├── keycloak.ts        # Keycloak integration
│   │   ├── cust-mgmt.ts       # Customer management API
│   │   ├── env.ts             # Environment configuration
│   │   ├── types.ts           # TypeScript types
│   │   └── components/        # Reusable Svelte components
│   ├── routes/                # SvelteKit routes
│   │   ├── +layout.svelte     # Root layout with auth
│   │   ├── +page.svelte       # Home page
│   │   └── accounts/          # Accounts feature
│   └── app.html               # HTML template
├── static/
│   └── keycloak.json          # Keycloak configuration
├── JWT-INTEGRATION.md         # JWT authentication guide
└── package.json
```

## Backend Requirements

Requires crm-server with JWT authentication enabled. See [JWT-AUTHENTICATION.md](../docs/JWT-AUTHENTICATION.md) in the parent project.
