<script>
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { applyBootstrapTheme } from '$lib/theme';
  import keycloak, { initKeycloak, keycloakStore } from '$lib/keycloak';

  let authenticated = false;
  let username = '';

  onMount(async () => {
    applyBootstrapTheme();

    try {
      await initKeycloak();
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

  $: currentPath = $page.url.pathname;
</script>

 <!-- Sidebar -->
  <nav id="sidebar" class="bg-dark">
    <div class="p-3">
      <h4 class="text-white mb-4"><i class="bi bi-box"></i> My App</h4>
      <ul class="nav flex-column">
        <li class="nav-item">
          <a class="nav-link {currentPath === '/' ? 'active' : ''}" href="/">
            <i class="bi bi-speedometer2 nav-icon"></i> Dashboard
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link {currentPath === '/accounts' ? 'active' : ''}" href="/accounts">
            <i class="bi bi-people nav-icon"></i> Accounts
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="#">
            <i class="bi bi-gear nav-icon"></i> Settings
          </a>
        </li>
        <li class="nav-item">
          {#if authenticated}
            <button class="nav-link btn btn-outline-light" on:click={logout}>
              <i class="bi bi-box-arrow-right nav-icon"></i> Logout
            </button>
          {:else}
            <button class="nav-link btn btn-light" on:click={login}>
              <i class="bi bi-box-arrow-in-right nav-icon"></i> Login
            </button>
          {/if}
        </li>
        <li class="nav-item" style="position: absolute; bottom: 1rem; width: calc(100% - 1rem);">
          <a class="nav-link" href="/profile">
            <i class="bi bi-person nav-icon"></i>
            {#if authenticated}
              <span class="me-3">{username}</span>
            {/if}
          </a>
        </li>
      </ul>
    </div>
  </nav>

<main id="content" class="container mt-4 text-light bg-dark p-4 rounded">
  <slot />
</main>

<style>
    body {
      overflow-x: hidden;
    }

    #sidebar {
      position: fixed;
      top: 0;
      left: 0;
      min-height: 100vh;
      background-color: #343a40;
      transition: all 0.3s ease;
    }

    #sidebar .nav-link {
      color: #ccc;
    }

    #sidebar .nav-link:hover,
    #sidebar .nav-link.active {
      background-color: #495057;
      color: #fff;
    }

    #sidebarCollapse {
      border: none;
    }

    @media (max-width: 768px) {
      #sidebar {
        position: fixed;
        left: -250px;
        width: 250px;
        z-index: 1030;
      }

      #sidebar.active {
        left: 0;
      }

      #content {
        margin-left: auto;
        margin-right: auto;
        max-width: 1200px;
      }
    }

    @media (min-width: 769px) {
      #sidebar {
        width: 250px;
      }

      #content {
        margin-left: 295px;
        margin-right: auto;
        padding-left: 2rem;
        padding-right: 2rem;
      }
    }

    .nav-icon {
      margin-right: 10px;
    }
  </style>