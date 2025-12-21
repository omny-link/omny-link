<script>
  import { onMount } from 'svelte';
  import keycloak, { initKeycloak, fetchUserAccount } from '$lib/keycloak';

  let userInfo = null;
  let tenant = 'acme';
  let accounts = [];
  let loading = false;
  let page = 1;
  let allLoaded = false;

  // Stale-while-refresh: show what we have, keep loading in background
  async function fetchAccounts(nextPage) {
    if (loading || allLoaded) return;
    loading = true;
    // const url = `http://localhost:8080/${tenant}/accounts/?page=${nextPage}`;
    const url = `https://crm.knowprocess.com/${tenant}/accounts/?page=${nextPage}`;
    const headers = {};
    if (keycloak.authenticated) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }
    try {
      const res = await fetch(url, { headers, mode: "cors" });
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data) && data.length > 0) {
          accounts = [...accounts, ...data];
          page = nextPage;
          // Load next page in background
          fetchAccounts(nextPage + 1);
        } else {
          allLoaded = true;
        }
      } else {
        allLoaded = true;
      }
    } finally {
      loading = false;
    }
  }

  onMount(async () => {
    // Wait for keycloak to initialize first
    await initKeycloak();
    
    if (!keycloak.authenticated) {
      await keycloak.login({ redirectUri: window.location.href });
    }
    
    // Fetch user account info to get tenant
    if (keycloak.authenticated) {
      userInfo = keycloak.tokenParsed;
      const { tenant: userTenant } = await fetchUserAccount();
      tenant = userTenant;
      fetchAccounts(1);
    }
  });
</script>

<h1 class="display-5">Accounts</h1>
{#if userInfo}
  <p class="text-muted">You are logged in as <strong>{userInfo.preferred_username}</strong>.</p>
{/if}

{#if loading}
  <div class="alert alert-info">Loading accounts...</div>
{:else}
  <button class="btn btn-outline-primary mb-3" on:click={() => { accounts = []; page = 1; allLoaded = false; fetchAccounts(1); }}>
    Refresh
  </button>
  <table class="table table-striped mt-4" style="max-width: 900px;">
    <thead>
      <tr>
        <th>Name</th>
        <th>Email</th>
        <th>Created</th>
        <!-- Add more columns as needed -->
      </tr>
    </thead>
    <tbody>
      {#each accounts as account}
        <tr>
          <td>{account.name}</td>
          <td>{account.email}</td>
          <td>{account.created}</td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}

