<script>
  import { onMount } from 'svelte';
  import { applyBootstrapTheme } from '$lib/theme';
  import keycloak from '$lib/keycloak';

  let authenticated = false;
  let username = '';

  onMount(async () => {
    applyBootstrapTheme();

    try {
      await keycloak.init({ onLoad: 'check-sso', pkceMethod: 'S256' });
      authenticated = keycloak.authenticated;
      username = keycloak?.tokenParsed?.preferred_username || '';
    } catch (err) {
      console.error('Keycloak init error:', err);
    }
  });

  function login() {
    keycloak.login();
  }

  function logout() {
    keycloak.logout({ redirectUri: window.location.origin });
  }
</script>

<nav class="navbar navbar-expand-lg navbar-dark bg-primary px-4">
  <a class="navbar-brand" href="/">SvelteApp</a>
  <div class="collapse navbar-collapse">
    <ul class="navbar-nav me-auto">
      <li class="nav-item">
        <a class="nav-link" href="/">Home</a>
      </li>
      <li class="nav-item">
        <a class="nav-link" href="/accounts">Accounts</a>
      </li>
    </ul>
    <div class="d-flex text-white">
      {#if authenticated}
        <span class="me-3">Welcome, {username}</span>
        <button class="btn btn-outline-light btn-sm" on:click={logout}>Logout</button>
      {:else}
        <button class="btn btn-light btn-sm" on:click={login}>Login</button>
      {/if}
    </div>
  </div>
</nav>

<main class="container mt-4 text-light bg-dark p-4 rounded">
  <slot />
</main>
