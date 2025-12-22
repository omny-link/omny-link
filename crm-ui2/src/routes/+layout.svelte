<script lang="ts">
  import { onMount } from 'svelte';
  import '../app.css';
  import { applyBootstrapTheme } from '$lib/theme';
  import keycloak, { initKeycloak } from '$lib/keycloak';
  import Sidebar from '$lib/components/Sidebar.svelte';

  let authenticated: boolean = false;
  let username: string = '';
  let userEmail: string = '';
  let sidebarCollapsed: boolean = false;
  let sidebar: any;

  function toggleSidebar(): void {
    if (sidebar) {
      sidebar.toggle();
      sidebarCollapsed = !sidebarCollapsed;
    }
  }

  onMount(async () => {
    applyBootstrapTheme();

    try {
      await initKeycloak();
      authenticated = keycloak.authenticated || false;
      username = keycloak?.tokenParsed?.preferred_username || '';
      userEmail = keycloak?.tokenParsed?.email || username;
    } catch (err) {
      console.error('Keycloak init error:', err);
    }
  });
</script>

<Sidebar 
  bind:this={sidebar}
  bind:collapsed={sidebarCollapsed}
  {authenticated}
  {username}
  {userEmail}
/>

<main id="content" class="{sidebarCollapsed ? 'sidebar-collapsed' : ''} container mt-4 text-light bg-dark p-4 rounded">
  <slot />
</main>


<style>
  #content {
    margin-left: auto;
    margin-right: auto;
    max-width: 1200px;
  }

  @media (min-width: 769px) {
    #content {
      margin-left: 295px;
      margin-right: auto;
      padding-left: 2rem;
      padding-right: 2rem;
      transition: margin-left 0.3s ease;
    }

    /* Sidebar reduces by 200px (250px - 50px), so content margin reduces by same amount */
    #content.sidebar-collapsed {
      margin-left: calc(295px - 200px); /* 95px */
    }
  }
</style>