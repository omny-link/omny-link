# Copilot Instructions for CRM UI (Svelte 5)

## Project Overview

This is a Svelte 5 (SvelteKit) based CRM frontend application with Keycloak authentication, Bootstrap theming, and Gravatar integration.

## Technology Stack

- **Framework**: Svelte 5 (SvelteKit)
- **Styling**: Bootstrap 5 with custom dark theme
- **Authentication**: Keycloak
- **Build Tool**: Vite
- **Language**: JavaScript/TypeScript (prefer modern ES6+ syntax)
- **Icons**: Bootstrap Icons

## Code Style & Conventions

### Svelte 5 Components

- Use `<script>`, `<style>`, and markup sections in that order
- Use Svelte 5 runes for reactivity (`$state`, `$derived`, `$effect`, `$props`, etc.)
- Prefer runes over legacy `$:` and `let` reactivity where possible
- Do not use deprecated features like $page store directly; use `$props` and `$state`
- Import Svelte lifecycle methods from 'svelte' (e.g., `onMount`)
- Prefer composition and small, focused components
- Use TypeScript for type safety where possible

### Naming Conventions

- Component files: PascalCase with `.svelte` extension (e.g., `UserProfile.svelte`)
- Route files: lowercase with `+` prefix (e.g., `+page.svelte`, `+layout.svelte`)
- Variables and functions: camelCase
- Constants: UPPER_SNAKE_CASE
- CSS classes: kebab-case or Bootstrap utility classes

### Styling

- Use Bootstrap 5 utility classes first (prefer `bg-dark`, `text-light`, `mb-4`, etc.)
- Apply custom styles in component `<style>` sections (scoped by default)
- Use CSS variables for theming via `applyBootstrapTheme()` from `$lib/theme.js`
- Dark theme is primary: use `bg-dark`, `text-white`, `text-light` classes
- Use Bootstrap Icons with `bi-*` classes (e.g., `bi-speedometer2`, `bi-people`)
- Responsive breakpoints: follow Bootstrap conventions (xs, sm, md, lg, xl, xxl)

### State Management

- Use Svelte stores for shared state (see `$lib/keycloak.ts`)
- Use `$state` for local state
- Use `$derived` for computed values
- Use `$effect` for side effects
- Import and subscribe to `keycloakStore` for authentication state
- Access reactive store values with `$` prefix

### Authentication (Keycloak)

- Import from `$lib/keycloak.ts`: `import keycloak, { initKeycloak, keycloakStore } from '$lib/keycloak'`
- Initialize with `await initKeycloak()` in `onMount`
- Check auth status: `keycloak.authenticated`
- Access user info: `keycloak.tokenParsed.preferred_username`, `keycloak.tokenParsed.email`
- Login: `keycloak.login()`
- Logout: `keycloak.logout({ redirectUri: window.location.origin })`
- Use runes for state and effects where possible

### API Calls

- Place API functions in `$lib/api/` directory
- Use native `fetch` with proper error handling
- Include Keycloak token in Authorization header: `Authorization: Bearer ${keycloak.token}`
- Handle loading and error states in components
- Return proper HTTP status codes

### Navigation & Routing

- Use SvelteKit's file-based routing
- Access current path with `$page.url.pathname`
- Use `<a>` tags with `href` for navigation (SvelteKit handles client-side routing)
- Add `active` class to current route's nav links
- Use `title` attributes for accessibility (especially on collapsed sidebar)

### Layout Patterns

- Fixed sidebar navigation with collapse functionality
- Main content area with responsive margins
- Sidebar width: 250px (expanded), 60px (collapsed)
- Content margin adjusts based on sidebar state
- Mobile-responsive: sidebar off-canvas on small screens

### User Profile Integration

- Use Gravatar for user avatars
- Hash email with MD5 (implementation provided in layout)
- Default avatar: `?d=mp` (mystery person)
- Avatar size: 32px for sidebar profile
- Handle missing email gracefully

src/

### File Organization

```

├── routes/          # SvelteKit routes (+page.svelte, +layout.svelte)
├── lib/             # Shared utilities and components
│   ├── api/         # API client functions
│   ├── components/  # Reusable components
│   ├── stores/      # Svelte stores
│   ├── keycloak.ts  # Keycloak configuration
│   └── theme.js     # Bootstrap theme utilities
├── app.css          # Global styles
└── static/          # Static assets (images, fonts)
```

## Migration to Svelte 5

- Refactor local state to use `$state` rune instead of `let` where possible
- Use `$derived` for computed values instead of `$:`
- Use `$effect` for side effects instead of `$:`
- Use `$props` for props
- Prefer runes for new code, but legacy syntax is still supported
- Remove deprecated Svelte 2/3 features

### Best Practices

- Always handle loading and error states
- Use semantic HTML elements
- Make navigation accessible with proper ARIA labels
- Implement responsive design using Bootstrap breakpoints
- Validate user input before API calls
- Log errors to console for debugging
- Use `try/catch` for async operations
- Handle authentication errors gracefully

### Svelte 5 Runes and Patterns

#### Local State with Runes

```js
import { $state } from 'svelte';
const count = $state(0);
```

#### Derived Values

```js
import { $derived } from 'svelte';
const doubled = $derived(() => count * 2);
```

#### Effects

```js
import { $effect } from 'svelte';
$effect(() => {
	console.log('Count changed:', count);
});
```

#### Props

```js
import { $props } from 'svelte';
const { value } = $props();
```

#### Conditional Rendering

```svelte
{#if condition}
	<!-- content -->
{:else if otherCondition}
	<!-- alternative -->
{:else}
	<!-- fallback -->
{/if}
```

#### List Rendering with Keys

```svelte
{#each items as item (item.id)}
	<!-- item content -->
{/each}
```

#### Event Handling

```svelte
<button on:click={handleClick}>Click me</button>
<button class="nav-link" on:click={logout} title="Logout">
	<i class="bi bi-box-arrow-right nav-icon"></i>
</button>
```

#### Current Page Highlighting

```svelte
<script>
	import { page } from '$app/stores';
	$: currentPath = $page.url.pathname;
</script>

<a class="nav-link {currentPath === '/accounts' ? 'active' : ''}" href="/accounts"> Accounts </a>
```

## Sidebar Implementation

- Toggle with `sidebarCollapsed` boolean state
- Icons remain visible when collapsed
- Text labels hidden when collapsed (`{#if !sidebarCollapsed}`)
- Smooth transitions with CSS (`transition: all 0.3s ease`)
- Profile section at bottom with Gravatar
- Responsive: off-canvas on mobile

## Testing

- Write unit tests for utility functions
- Test components with @testing-library/svelte
- Mock Keycloak in tests
- Test responsive behavior at different breakpoints
- Test sidebar collapse/expand functionality

## Deployment Considerations

- Build with `npm run build`
- Static assets in `/static` directory
- Environment variables in `.env` files (not committed)
- Configure Keycloak URLs for production environment
- Ensure proper CORS settings for API calls
