<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import keycloak, { initKeycloak, fetchUserAccount, keycloakStore } from '$lib/keycloak';
  import { getGravatarUrl } from '$lib/gravatar';
  import { fetchAccounts as fetchAccountsAPI } from '$lib/cust-mgmt';
  import type { Account, SortDirection, UserInfo } from '$lib/types';

  let userInfo: UserInfo | null = null;
  let tenant: string = 'acme';
  let accounts: Account[] = [];
  let filteredAccounts: Account[] = [];
  let searchQuery: string = "";
  let loading: boolean = false;
  let page: number = 1;
  let allLoaded: boolean = false;
  let sortColumn: string = 'updated';
  let sortDirection: SortDirection = 'desc';

  async function fetchAccounts(nextPage: number): Promise<void> {
    if (loading || allLoaded) return;
    loading = true;
    try {
      const data = await fetchAccountsAPI(tenant, nextPage);
      if (Array.isArray(data) && data.length > 0) {
        accounts = [...accounts, ...data];
        filteredAccounts = accounts;
        applySortToFiltered();
        page = nextPage;
        // Load next page in background
        fetchAccounts(nextPage + 1);
      } else {
        allLoaded = true;
      }
    } finally {
      loading = false;
    }
  }

  function filterAccounts(): void {
    if (!searchQuery.trim()) {
      filteredAccounts = accounts;
    } else {
      const query = searchQuery.toLowerCase();
      filteredAccounts = accounts.filter(account => 
        (account.name && account.name.toLowerCase().includes(query)) ||
        (account.email && account.email.toLowerCase().includes(query)) ||
        (account.created && account.created.toLowerCase().includes(query))
      );
    }
    // Reapply sort after filtering
    if (sortColumn) {
      applySortToFiltered();
    }
  }

  function sortBy(column: string): void {
    if (sortColumn === column) {
      sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      sortColumn = column;
      sortDirection = 'asc';
    }
    applySortToFiltered();
  }

  function applySortToFiltered(): void {
    filteredAccounts = [...filteredAccounts].sort((a, b) => {
      let aVal, bVal;
      
      switch(sortColumn) {
        case 'name':
          aVal = (a.name || '').toLowerCase();
          bVal = (b.name || '').toLowerCase();
          break;
        case 'status':
          aVal = (a.stage || a.accountType || '').toLowerCase();
          bVal = (b.stage || b.accountType || '').toLowerCase();
          break;
        case 'type':
          aVal = (a.type || a.businessType || '').toLowerCase();
          bVal = (b.type || b.businessType || '').toLowerCase();
          break;
        case 'created':
          aVal = new Date(a.created || 0);
          bVal = new Date(b.created || 0);
          break;
        case 'updated':
          aVal = new Date(a.lastUpdated || a.updated || 0);
          bVal = new Date(b.lastUpdated || b.updated || 0);
          break;
        default:
          return 0;
      }
      
      if (aVal < bVal) return sortDirection === 'asc' ? -1 : 1;
      if (aVal > bVal) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  function formatDate(dateString: string | undefined): string {
    if (!dateString || dateString === '-') return '-';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;
      return date.toLocaleDateString(navigator.language, {
        year: 'numeric',
        month: 'short',
        day: 'numeric'
      });
    } catch (e) {
      return dateString;
    }
  }

  function formatTags(tags: string | string[] | undefined): string[] {
    if (!tags || tags === '-') return ['-'];
    const tagString = typeof tags === 'string' ? tags : tags.join(', ');
    const tagArray = tagString.split(',').map((t: string) => t.trim());
    const lines: string[] = [];
    let currentLine = '';
    
    tagArray.forEach((tag: string, index: number) => {
      const separator = index > 0 ? ', ' : '';
      const testLine = currentLine + separator + tag;
      
      if (currentLine && testLine.length > 20) {
        lines.push(currentLine);
        currentLine = tag;
      } else {
        currentLine = testLine;
      }
    });
    
    if (currentLine) {
      lines.push(currentLine);
    }
    
    return lines.length > 0 ? lines : ['-'];
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
        <th class="sortable" on:click={() => sortBy('name')}>
          Name
          {#if sortColumn === 'name'}
            <i class="bi bi-arrow-{sortDirection === 'asc' ? 'up' : 'down'}"></i>
          {/if}
        </th>
        <th class="sortable" on:click={() => sortBy('status')}>
          Status
          {#if sortColumn === 'status'}
            <i class="bi bi-arrow-{sortDirection === 'asc' ? 'up' : 'down'}"></i>
          {/if}
        </th>
        <th class="sortable" on:click={() => sortBy('type')}>
          Type
          {#if sortColumn === 'type'}
            <i class="bi bi-arrow-{sortDirection === 'asc' ? 'up' : 'down'}"></i>
          {/if}
        </th>
        <th>Owner</th>
        <th class="sortable" on:click={() => sortBy('created')}>
          First Contact
          {#if sortColumn === 'firstContact'}
            <i class="bi bi-arrow-{sortDirection === 'asc' ? 'up' : 'down'}"></i>
          {/if}
        </th>
        <th class="sortable" on:click={() => sortBy('updated')}>
          Last Updated
          {#if sortColumn === 'updated'}
            <i class="bi bi-arrow-{sortDirection === 'asc' ? 'up' : 'down'}"></i>
          {/if}
        </th>
        <th>Tags</th>
        <th>Actions</th>
      </tr>
    </thead>
    <tbody>
      {#each filteredAccounts as account}
        <tr>
          <td>{account.name}</td>
          <td>{account.stage || '-'}</td>
          <td>{account.accountType || '-'}</td>
          <td>
            <img 
              src={getGravatarUrl(account.email || account.ownerEmail)} 
              alt={account.owner || 'Owner'} 
              title={account.owner || ''}
              class="rounded-circle"
              style="width: 32px; height: 32px;"
            />
          </td>
          <td>{formatDate(account.firstContact)}</td>
          <td>{formatDate(account.lastUpdated)}</td>
          <td style="max-width: 20rem;">
            {#each formatTags(account.tags) as line, i}
              {line}{#if i < formatTags(account.tags).length - 1}<br />{/if}
            {/each}
          </td>
          <td>
            <button class="btn btn-dark ms-auto" aria-label="View account" on:click={() => goto(`/accounts/${account.id || account.selfRef?.split('/').pop()}`)}>
              <i class="bi bi-eye"></i>
            </button>
            <button class="btn btn-dark ms-auto" aria-label="Edit account" on:click={() => goto(`/accounts/${account.id || account.selfRef?.split('/').pop()}`)}>
              <i class="bi bi-pencil"></i>
            </button>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}

<style>
  .sortable {
    cursor: pointer;
    user-select: none;
  }
  
  .sortable:hover {
    background-color: rgba(255, 255, 255, 0.1);
  }
</style>
