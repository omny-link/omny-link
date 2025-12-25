<script lang="ts">
  import { onMount } from 'svelte';
  import { goto } from '$app/navigation';
  import keycloak, { initKeycloak, fetchUserAccount, keycloakStore } from '$lib/keycloak';
  import { getGravatarUrl } from '$lib/gravatar';
  import { fetchAccounts as fetchAccountsAPI } from '$lib/cust-mgmt';
  import { colorSchemeStore } from '$lib/colorScheme';
  import { tenantConfigStore } from '$lib/tenantConfig';
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
  let colorScheme: 'light' | 'dark' = 'dark';
  // Derived: only show loading message when no cached data
  let showLoading: boolean = false;

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
  });

  async function fetchAccounts(nextPage: number): Promise<void> {
    if (loading || allLoaded) return;
    loading = true;
    isFiltering = true;
    try {
      const data = await fetchAccountsAPI(tenant, nextPage);
      if (Array.isArray(data) && data.length > 0) {
        // Replace cached results on first page to prevent duplicates
        if (nextPage === 1) {
          accounts = data;
        } else {
          accounts = [...accounts, ...data];
        }
        // Save updated accounts to localStorage for SWR behavior
        saveCachedAccounts(tenant, accounts);
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
      showLoading = loading && accounts.length === 0;
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

  // Tag list helper for badge display
  function getTagList(tags: string | string[] | undefined): string[] {
    if (!tags || tags === '-') return [];
    const tagString = Array.isArray(tags) ? tags.join(',') : tags;
    return tagString.split(',').map((t: string) => t.trim()).filter((t) => t.length > 0);
  }

  // Compute readable text color (black/white) for a given background color
  function getTextColorForBg(color: string): string {
    if (!color) return '#ffffff';

    function hexToRgb(hex: string): { r: number; g: number; b: number } | null {
      const h = hex.replace('#', '').trim();
      if (h.length === 3) {
        const r = parseInt(h[0] + h[0], 16);
        const g = parseInt(h[1] + h[1], 16);
        const b = parseInt(h[2] + h[2], 16);
        return { r, g, b };
      }
      if (h.length === 6 || h.length === 8) {
        const r = parseInt(h.substring(0, 2), 16);
        const g = parseInt(h.substring(2, 4), 16);
        const b = parseInt(h.substring(4, 6), 16);
        return { r, g, b };
      }
      return null;
    }

    function parseRgb(str: string): { r: number; g: number; b: number } | null {
      const m = str.match(/rgba?\(\s*(\d+)\s*,\s*(\d+)\s*,\s*(\d+)/i);
      if (!m) return null;
      return { r: parseInt(m[1], 10), g: parseInt(m[2], 10), b: parseInt(m[3], 10) };
    }

    function hslToRgb(h: number, s: number, l: number): { r: number; g: number; b: number } {
      s /= 100; l /= 100;
      const k = (n: number) => (n + h / 30) % 12;
      const a = s * Math.min(l, 1 - l);
      const f = (n: number) => l - a * Math.max(-1, Math.min(k(n) - 3, Math.min(9 - k(n), 1)));
      return { r: Math.round(255 * f(0)), g: Math.round(255 * f(8)), b: Math.round(255 * f(4)) };
    }

    function parseHsl(str: string): { r: number; g: number; b: number } | null {
      const m = str.match(/hsla?\(\s*(\d+)\s*,\s*(\d+)%\s*,\s*(\d+)%/i);
      if (!m) return null;
      return hslToRgb(parseInt(m[1], 10), parseInt(m[2], 10), parseInt(m[3], 10));
    }

    let rgb: { r: number; g: number; b: number } | null = null;
    const c = color.trim();

    if (c.startsWith('#')) {
      rgb = hexToRgb(c);
    } else if (c.toLowerCase().startsWith('rgb')) {
      rgb = parseRgb(c);
    } else if (c.toLowerCase().startsWith('hsl')) {
      rgb = parseHsl(c);
    } else if (typeof window !== 'undefined') {
      try {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        if (ctx) {
          ctx.fillStyle = c;
          const canonical = ctx.fillStyle as string;
          if (canonical.startsWith('#')) {
            rgb = hexToRgb(canonical);
          } else if (canonical.toLowerCase().startsWith('rgb')) {
            rgb = parseRgb(canonical);
          }
        }
      } catch (_) {
        // ignore
      }
    }

    if (!rgb) return '#ffffff';

    // Relative luminance
    const sr = rgb.r / 255, sg = rgb.g / 255, sb = rgb.b / 255;
    const rLin = sr <= 0.03928 ? sr / 12.92 : Math.pow((sr + 0.055) / 1.055, 2.4);
    const gLin = sg <= 0.03928 ? sg / 12.92 : Math.pow((sg + 0.055) / 1.055, 2.4);
    const bLin = sb <= 0.03928 ? sb / 12.92 : Math.pow((sb + 0.055) / 1.055, 2.4);
    const luminance = 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin;

    return luminance > 0.5 ? '#000000' : '#ffffff';
  }

  function onSearchKeydown(e: KeyboardEvent): void {
    if (e.key === 'Enter') {
      e.preventDefault();
      filterAccounts();
    }
  }

  // SWR-style manual refresh: keep current list and search
  function refreshAccountsSWR(): void {
    // do not clear searchQuery or current accounts
    allLoaded = false;
    page = 1;
    showLoading = false; // keep showing cached data
    fetchAccounts(1); // background refresh; page 1 will replace to avoid duplicates
  }

  // localStorage helpers for stale-while-refresh
  function getCacheKey(tenantId: string): string {
    return `accounts-cache:${tenantId}`;
  }

  function loadCachedAccounts(tenantId: string): void {
    try {
      const raw = localStorage.getItem(getCacheKey(tenantId));
      if (!raw) return;
      const parsed = JSON.parse(raw) as { data: Account[]; timestamp: number };
      if (Array.isArray(parsed.data)) {
        accounts = parsed.data;
        filterAccounts();
        // Avoid showing loading message since we have cached data
        showLoading = false;
      }
    } catch (e) {
      // Ignore cache errors silently
    }
  }

  function saveCachedAccounts(tenantId: string, data: Account[]): void {
    try {
      const payload = JSON.stringify({ data, timestamp: Date.now() });
      localStorage.setItem(getCacheKey(tenantId), payload);
    } catch (e) {
      // Ignore storage errors silently
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
      // Load tenant-specific configuration
      await tenantConfigStore.load(tenant);
      // Try cached accounts first (stale-while-refresh)
      loadCachedAccounts(tenant);
      // Begin background refresh
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

<div class="d-flex align-items-center flex-nowrap mb-3">
  <h2 class="display-5 mb-0 me-3 text-nowrap">
    Accounts
    {#if !loading}
      ({filteredAccounts.length})
    {/if}
  </h2>
  <input 
    type="text" 
    class="form-control flex-grow-1 me-3" 
    style="min-width: 300px;"
    placeholder="Search accounts..." 
    bind:value={searchQuery}
    on:keydown={onSearchKeydown}
    on:blur={filterAccounts}
    aria-label="Search accounts"
  />
  {#if !loading}
    <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'} ms-auto" aria-label="Refresh accounts" on:click={refreshAccountsSWR}>
      <i class="bi bi-arrow-clockwise"></i>
    </button>
  {/if}
</div>

{#if showLoading}
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
              src={getGravatarUrl(account.owner)} 
              alt={account.owner || 'Owner'} 
              title={account.owner || ''}
              class="rounded-circle"
              style="width: 32px; height: 32px;"
            />
          </td>
          <td>{formatDate(account.firstContact)}</td>
          <td>{formatDate(account.lastUpdated)}</td>
          <td style="max-width: 20rem;">
            {#if getTagList(account.tags).length > 0}
              {#each getTagList(account.tags) as tag}
                <span class="badge me-1 mb-1" style="background-color: {tag}; color: {getTextColorForBg(tag)}">{tag}</span>
              {/each}
            {:else}
              <span class="text-muted">-</span>
            {/if}
          </td>
          <td>
            <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'} ms-auto" aria-label="View account" on:click={() => goto(`/accounts/${account.id || account.selfRef?.split('/').pop()}`)}>
              <i class="bi bi-eye"></i>
            </button>
            <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'} ms-auto" aria-label="Edit account" on:click={() => goto(`/accounts/${account.id || account.selfRef?.split('/').pop()}?mode=edit`)}>
              <i class="bi bi-pencil"></i>
            </button>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}

<style>
  :global(body) {
    transition: background-color 0.3s ease, color 0.3s ease;
  }

  :global(body.light-mode) {
    --bg-color: #ffffff;
    --text-color: #212529;
    --border-color: #dee2e6;
    --hover-bg: rgba(0, 0, 0, 0.05);
    --overlay-bg: rgba(255, 255, 255, 0.8);
    --table-stripe: #f8f9fa;
  }

  :global(body.dark-mode) {
    --bg-color: #212529;
    --text-color: #ffffff;
    --border-color: #495057;
    --hover-bg: rgba(255, 255, 255, 0.1);
    --overlay-bg: rgba(0, 0, 0, 0.5);
    --table-stripe: #2c3034;
  }

  .sortable {
    cursor: pointer;
    user-select: none;
  }
  
  .sortable:hover {
    background-color: var(--hover-bg, rgba(255, 255, 255, 0.1));
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
    background-color: var(--overlay-bg, rgba(0, 0, 0, 0.3));
    z-index: 9999;
    pointer-events: none; /* keep UI interactive while showing spinner */
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
