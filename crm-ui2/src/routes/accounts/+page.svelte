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
  let isFiltering: boolean = false;
  let page: number = 1;
  let allLoaded: boolean = false;
  let sortColumn: string = 'updated';
  let sortDirection: SortDirection = 'desc';

  async function fetchAccounts(nextPage: number): Promise<void> {
    if (loading || allLoaded) return;
    loading = true;
    isFiltering = true;
    try {
      const data = await fetchAccountsAPI(tenant, nextPage);
      if (Array.isArray(data) && data.length > 0) {
        accounts = [...accounts, ...data];
        // Re-filter after each page load to avoid race conditions
        filterAccounts();
        page = nextPage;
        // Load next page in background
        fetchAccounts(nextPage + 1);
      } else {
        allLoaded = true;
      }
    } finally {
      loading = false;
      isFiltering = false;
    }
  }

  function filterAccounts(): void {
    isFiltering = true;
    const queryRaw = searchQuery.trim();
    if (!queryRaw) {
      filteredAccounts = accounts;
    } else {
      const tokens = queryRaw.toLowerCase().split(/\s+/).filter(Boolean);
      const requireActive = tokens.includes('active');
      const requireInactive = tokens.includes('!active');

      filteredAccounts = accounts.filter(account => {
        const stage = account.stage?.toLowerCase();

        const tokenMatches = (tok: string): boolean => {
          if (tok === 'active' || tok === '!active') return true;

          if (tok.startsWith('owner:')) {
            const val = tok.substring(6);
            return !!(account.owner && account.owner.indexOf(val) !== -1);
          }
          if (tok.startsWith('type:')) {
            const val = tok.substring(5).replace(/ /g, '_');
            return !!(account.accountType && account.accountType.toLowerCase().replace(/ /g, '_').indexOf(val) === 0);
          }
          if (tok.startsWith('enquiry:')) {
            const val = tok.substring(8).replace(/ /g, '_');
            return !!(account.enquiryType && account.enquiryType.toLowerCase().replace(/ /g, '_').indexOf(val) === 0);
          }
          if (tok.startsWith('stage:')) {
            const val = tok.substring(6).replace(/ /g, '_');
            return !!(stage && stage.replace(/ /g, '_').indexOf(val) === 0);
          }
          if (tok.startsWith('#')) {
            const val = tok.substring(1);
            return !!(account.tags && account.tags.toLowerCase().indexOf(val) !== -1);
          }
          if (tok.startsWith('updated>')) {
            const val = tok.substring(8);
            return new Date(account.lastUpdated || 0) > new Date(val);
          }
          if (tok.startsWith('updated<')) {
            const val = tok.substring(8);
            return new Date(account.lastUpdated || 0) < new Date(val);
          }
          if (tok.startsWith('created>')) {
            const val = tok.substring(8);
            return new Date(account.firstContact || 0) > new Date(val);
          }
          if (tok.startsWith('created<')) {
            const val = tok.substring(8);
            return new Date(account.firstContact || 0) < new Date(val);
          }

          // Free text token: OR across common fields
          return (
            (account.id && tok.indexOf(account.id) >= 0) ||
            (account.name && account.name.toLowerCase().includes(tok)) ||
            (account.email && account.email.toLowerCase().includes(tok)) ||
            (account.orgCode && account.orgCode.toLowerCase().indexOf(tok) >= 0) ||
            (account.phone1 && account.phone1.indexOf(tok) >= 0) ||
            (account.phone2 && account.phone2.indexOf(tok) >= 0)
          );
        };

        const allTokensMatch = tokens.every(tokenMatches);
        const activeOk = !requireActive || !inactiveStage(stage);
        const inactiveOk = !requireInactive || inactiveStage(stage);
        return allTokensMatch && activeOk && inactiveOk;
      });
    }
    // Reapply sort after filtering
    if (sortColumn) {
      applySortToFiltered();
    }
    isFiltering = false;
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
          aVal = new Date(a.lastUpdated || 0);
          bVal = new Date(b.lastUpdated || 0);
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

  function onSearchKeydown(e: KeyboardEvent): void {
    if (e.key === 'Enter') {
      e.preventDefault();
      filterAccounts();
    }
  }

  onMount(async () => {
    // Wait for keycloak to initialize first
    await initKeycloak();
    
    if (!keycloak.authenticated) {
      await keycloak.login({ redirectUri: window.location.href });
    }
    
    // Subscribe to keycloak store
    keycloakStore.subscribe(state => {
      userInfo = state.userInfo;
      // Update search query when userInfo changes
      if (userInfo?.username && !searchQuery) {
        searchQuery = `active owner:${userInfo.username}`;
        // Re-filter when search query is set programmatically
        filterAccounts();
      }
    });
    
    // Fetch user account info to get tenant
    if (keycloak.authenticated) {
      const { tenant: userTenant } = await fetchUserAccount();
      tenant = userTenant;
      fetchAccounts(1);
    }
  });


  function inactiveStage(stage: string | undefined): boolean {
    const inactiveStages = ['closed', 'deleted', 'archived'];
    return stage ? inactiveStages.includes(stage) : false;
  }
</script>

{#if isFiltering}
<div class="activity-overlay">
  <img src="https://crm.knowprocess.com/images/icon/omny-link-icon.svg" alt="Loading" class="activity-spinner" />
</div>
{/if}

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
    on:keydown={onSearchKeydown}
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

  .activity-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    display: flex;
    align-items: center;
    justify-content: center;
    background-color: rgba(0, 0, 0, 0.3);
    z-index: 9999;
  }

  .activity-spinner {
    width: 128px;
    height: 128px;
    animation: spin 2s linear infinite;
  }

  @keyframes spin {
    from {
      transform: rotate(0deg);
    }
    to {
      transform: rotate(360deg);
    }
  }
</style>
