<script>
  import { onMount } from 'svelte';
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
</script>

 <!-- Sidebar -->
  <nav id="sidebar" class="bg-dark">
    <div class="p-3">
      <h4 class="text-white mb-4"><i class="bi bi-box"></i> My App</h4>
      <ul class="nav flex-column">
        <li class="nav-item">
          <a class="nav-link active" href="/">
            <i class="bi bi-speedometer2 nav-icon"></i> Dashboard
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/accounts">
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
            <span class="me-3">Welcome, {username}</span>
            <button class="btn btn-outline-light btn-sm" on:click={logout}>Logout</button>
          {:else}
            <button class="btn btn-light btn-sm" on:click={login}>Login</button>
          {/if}
        </li>
      </ul>
    </div>
  </nav>

<main class="container mt-4 text-light bg-dark p-4 rounded">
  <slot />
</main>

<style>
    body {
      overflow-x: hidden;
    }

    #sidebar {
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
        margin-left: 0;
      }
    }

    @media (min-width: 769px) {
      #sidebar {
        width: 250px;
      }

      #content {
        margin-left: 250px;
      }
    }

    .nav-icon {
      margin-right: 10px;
    }
  </style>