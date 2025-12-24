<script lang="ts">
  import { page } from '$app/stores';
  import { get } from 'svelte/store';
  import keycloak from '$lib/keycloak';
  import { getGravatarUrl } from '$lib/gravatar';
  import { colorSchemeStore } from '$lib/colorScheme';

  export let collapsed: boolean = false;
  export let authenticated: boolean = false;
  export let username: string = '';
  export let userEmail: string = '';
  
  let colorScheme: 'light' | 'dark' = 'dark';

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
  });

  export function toggle(): void {
    collapsed = !collapsed;
  }

  function login(): void {
    keycloak.login();
  }

  function logout(): void {
    keycloak.logout({ redirectUri: window.location.origin });
  }

  $: currentPath = get(page).url.pathname;
</script>

<nav id="sidebar" class="{colorScheme === 'dark' ? 'bg-dark' : 'bg-light'} {collapsed ? 'collapsed' : ''}">
  <div class="p-3">
    <div class="d-flex justify-content-between align-items-center mb-4">
      {#if !collapsed}
        <h4 class="{colorScheme === 'dark' ? 'text-white' : 'text-dark'} mb-0">
          <img 
            src={'https://crm.knowprocess.com/images/icon/omny-link-icon.svg' }
            alt="KnowProcess" 
            style="max-width: 75px; max-height: 55px; margin: -0.25rem 0.5rem 0 0 ;" 
          /> 
        </h4>
      {:else}
        <img 
          src={'https://crm.knowprocess.com/images/icon/omny-link-icon.svg'} 
          alt="Icon" 
          style="width: 32px; height: 32px; margin: 0 auto;" 
        />
      {/if}
      <button class="btn btn-link {colorScheme === 'dark' ? 'text-white' : 'text-dark'} p-0" on:click={toggle} aria-label="Toggle sidebar">
        <i class="bi bi-{collapsed ? 'chevron-right' : 'chevron-left'}" style="font-size: 1.25rem;"></i>
      </button>
    </div>

    <ul class="nav flex-column">
      <li class="nav-item">
        <a class="nav-link {currentPath === '/' ? 'active' : ''}" href="/" title="Dashboard">
          <i class="bi bi-speedometer2 nav-icon"></i> {#if !collapsed}<span>Dashboard</span>{/if}
        </a>
      </li>
      <li class="nav-item">
        <a class="nav-link {currentPath.startsWith('/accounts') ? 'active' : ''}" href="/accounts" title="Accounts">
          <i class="bi bi-people nav-icon"></i> {#if !collapsed}<span>Accounts</span>{/if}
        </a>
      </li>
      <li class="nav-item">
        <button class="nav-link" type="button" title="Settings">
          <i class="bi bi-gear nav-icon"></i> {#if !collapsed}<span>Settings</span>{/if}
        </button>
      </li>
      <li class="nav-item">
        {#if authenticated}
          <button class="nav-link" on:click={logout} title="Logout">
            <i class="bi bi-box-arrow-right nav-icon"></i> {#if !collapsed}<span>Logout</span>{/if}
          </button>
        {:else}
          <button class="nav-link" on:click={login} title="Login">
            <i class="bi bi-box-arrow-in-right nav-icon"></i> {#if !collapsed}<span>Login</span>{/if}
          </button>
        {/if}
      </li>
      <li class="nav-item" style="position: absolute; bottom: 1rem; width: calc(100% - 1rem);">
        <a class="nav-link d-flex align-items-center" href="/profile" title="Profile">
          {#if authenticated}
            <img src={getGravatarUrl(userEmail)} alt={username} class="rounded-circle {collapsed ? '' : 'me-2'}" style="width: 32px; height: 32px;" />
            {#if !collapsed}<span>{username}</span>{/if}
          {:else}
            <i class="bi bi-person nav-icon"></i>
            {#if !collapsed}<span>Profile</span>{/if}
          {/if}
        </a>
      </li>
    </ul>
  </div>
</nav>

<style>
  #sidebar {
    position: fixed;
    top: 0;
    left: 0;
    min-height: 100vh;
    transition: all 0.3s ease;
    width: 250px;
  }

  /* Dark mode colors */
  :global(body.dark-mode) #sidebar {
    background-color: #343a40;
  }

  :global(body.dark-mode) #sidebar .nav-link {
    color: #ccc;
  }

  :global(body.dark-mode) #sidebar .nav-link:hover,
  :global(body.dark-mode) #sidebar .nav-link.active {
    background-color: #495057;
    color: #fff;
  }

  /* Light mode colors */
  :global(body.light-mode) #sidebar {
    background-color: #f8f9fa;
    border-right: 1px solid #dee2e6;
  }

  :global(body.light-mode) #sidebar .nav-link {
    color: #495057;
  }

  :global(body.light-mode) #sidebar .nav-link:hover,
  :global(body.light-mode) #sidebar .nav-link.active {
    background-color: #e9ecef;
    color: #212529;
  }

  #sidebar.collapsed {
    width: 50px;
  }

  #sidebar.collapsed .p-3 {
    padding: 0.5rem !important;
  }

  #sidebar .nav-link {
    padding-left: 0;
    margin-left: 0;
    white-space: nowrap;
    display: flex;
    align-items: center;
  }

  #sidebar button.nav-link {
    width: 100%;
    border: none;
    background: none;
    text-align: left;
  }

  #sidebar.collapsed .nav-link {
    justify-content: center;
    padding-left: 0;
    padding-right: 0;
  }

  #sidebar.collapsed .nav-item {
    width: 100%;
  }

  @media (max-width: 768px) {
    #sidebar {
      position: fixed;
      left: -250px;
      width: 250px;
      z-index: 1030;
    }

    #sidebar.collapsed {
      left: -50px;
      width: 50px;
    }
  }

  @media (min-width: 769px) {
    #sidebar {
      width: 250px;
    }

    #sidebar.collapsed {
      width: 60px;
    }
  }

  .nav-icon {
    margin-right: 10px;
  }
</style>
