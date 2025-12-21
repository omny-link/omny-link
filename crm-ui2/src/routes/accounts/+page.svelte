<script>
  import { onMount } from 'svelte';
  import keycloak, { initKeycloak, fetchUserAccount } from '$lib/keycloak';

  let userInfo = null;
  let tenant = 'acme';
  let accounts = [];
  let filteredAccounts = [];
  let searchQuery = '';
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
          filteredAccounts = accounts;
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

  function filterAccounts() {
    if (!searchQuery.trim()) {
      filteredAccounts = accounts;
      return;
    }
    const query = searchQuery.toLowerCase();
    filteredAccounts = accounts.filter(account => 
      (account.name && account.name.toLowerCase().includes(query)) ||
      (account.email && account.email.toLowerCase().includes(query)) ||
      (account.created && account.created.toLowerCase().includes(query))
    );
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

<div class="d-flex align-items-center mb-3">
  <h2 class="display-5 mb-0 me-3">
    Accounts
    {#if !loading}
      ({filteredAccounts.length})
    {/if}
  </h2>
  <input 
    type="text" 
    class="form-control" 
    style="width: 300px;" 
    placeholder="Search accounts..." 
    bind:value={searchQuery}
    on:blur={filterAccounts}
    aria-label="Search accounts"
  />
  {#if !loading}
    <button class="btn btn-dark ms-auto" aria-label="Refresh accounts" on:click={() => { accounts = []; filteredAccounts = []; searchQuery = ''; page = 1; allLoaded = false; fetchAccounts(1); }}>
      <i class="bi bi-arrow-clockwise"></i>
    </button>
  {/if}
</div>

{#if loading}
  <div class="alert alert-info">Loading accounts...</div>
{:else}
  <table class="table table-striped mt-4">
    <thead>
      <tr>
        <th>Name</th>
        <th>Email</th>
        <th>Created</th>
        <!-- Add more columns as needed -->
      </tr>
    </thead>
    <tbody>
      {#each filteredAccounts as account}
        <tr>
          <td>{account.name}</td>
          <td>{account.email}</td>
          <td>{account.created}</td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}

