<script>
  import { onMount } from 'svelte';
  import keycloak from '$lib/keycloak';

  let userInfo = null;

  onMount(async () => {
    if (!keycloak.authenticated) {
      await keycloak.login({ redirectUri: window.location.href });
    } else {
      userInfo = keycloak.tokenParsed;
    }
  });
</script>

<h1 class="display-5">Accounts</h1>
{#if userInfo}
  <p class="text-muted">You are logged in as <strong>{userInfo.preferred_username}</strong>.</p>
{/if}
