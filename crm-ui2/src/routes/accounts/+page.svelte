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
  let sortColumn = 'updated';
  let sortDirection = 'desc';
  let selectedAccount = null;
  let viewMode = 'list'; // 'list', 'view', or 'edit'
  let accountContacts = [];
  let accountOrders = [];
  let panelStates = {
    details: true,
    additionalInfo: true,
    customFields: true,
    recordHistory: true,
    contacts: true,
    orders: true,
    activities: true,
    notes: false,
    documents: false
  };

  function togglePanel(panelName) {
    panelStates[panelName] = !panelStates[panelName];
  }

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
          applySortToFiltered();
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

  function sortBy(column) {
    if (sortColumn === column) {
      sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      sortColumn = column;
      sortDirection = 'asc';
    }
    applySortToFiltered();
  }

  function applySortToFiltered() {
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

  function formatDate(dateString) {
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

  function formatTags(tags) {
    if (!tags || tags === '-') return ['-'];
    const tagArray = tags.split(',').map(t => t.trim());
    const lines = [];
    let currentLine = '';
    
    tagArray.forEach((tag, index) => {
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

  // Simple MD5 implementation for gravatar
  function md5(string) {
    function md5cycle(x, k) {
      let a = x[0], b = x[1], c = x[2], d = x[3];
      a = ff(a, b, c, d, k[0], 7, -680876936);
      d = ff(d, a, b, c, k[1], 12, -389564586);
      c = ff(c, d, a, b, k[2], 17, 606105819);
      b = ff(b, c, d, a, k[3], 22, -1044525330);
      a = ff(a, b, c, d, k[4], 7, -176418897);
      d = ff(d, a, b, c, k[5], 12, 1200080426);
      c = ff(c, d, a, b, k[6], 17, -1473231341);
      b = ff(b, c, d, a, k[7], 22, -45705983);
      a = ff(a, b, c, d, k[8], 7, 1770035416);
      d = ff(d, a, b, c, k[9], 12, -1958414417);
      c = ff(c, d, a, b, k[10], 17, -42063);
      b = ff(b, c, d, a, k[11], 22, -1990404162);
      a = ff(a, b, c, d, k[12], 7, 1804603682);
      d = ff(d, a, b, c, k[13], 12, -40341101);
      c = ff(c, d, a, b, k[14], 17, -1502002290);
      b = ff(b, c, d, a, k[15], 22, 1236535329);
      a = gg(a, b, c, d, k[1], 5, -165796510);
      d = gg(d, a, b, c, k[6], 9, -1069501632);
      c = gg(c, d, a, b, k[11], 14, 643717713);
      b = gg(b, c, d, a, k[0], 20, -373897302);
      a = gg(a, b, c, d, k[5], 5, -701558691);
      d = gg(d, a, b, c, k[10], 9, 38016083);
      c = gg(c, d, a, b, k[15], 14, -660478335);
      b = gg(b, c, d, a, k[4], 20, -405537848);
      a = gg(a, b, c, d, k[9], 5, 568446438);
      d = gg(d, a, b, c, k[14], 9, -1019803690);
      c = gg(c, d, a, b, k[3], 14, -187363961);
      b = gg(b, c, d, a, k[8], 20, 1163531501);
      a = gg(a, b, c, d, k[13], 5, -1444681467);
      d = gg(d, a, b, c, k[2], 9, -51403784);
      c = gg(c, d, a, b, k[7], 14, 1735328473);
      b = gg(b, c, d, a, k[12], 20, -1926607734);
      a = hh(a, b, c, d, k[5], 4, -378558);
      d = hh(d, a, b, c, k[8], 11, -2022574463);
      c = hh(c, d, a, b, k[11], 16, 1839030562);
      b = hh(b, c, d, a, k[14], 23, -35309556);
      a = hh(a, b, c, d, k[1], 4, -1530992060);
      d = hh(d, a, b, c, k[4], 11, 1272893353);
      c = hh(c, d, a, b, k[7], 16, -155497632);
      b = hh(b, c, d, a, k[10], 23, -1094730640);
      a = hh(a, b, c, d, k[13], 4, 681279174);
      d = hh(d, a, b, c, k[0], 11, -358537222);
      c = hh(c, d, a, b, k[3], 16, -722521979);
      b = hh(b, c, d, a, k[6], 23, 76029189);
      a = hh(a, b, c, d, k[9], 4, -640364487);
      d = hh(d, a, b, c, k[12], 11, -421815835);
      c = hh(c, d, a, b, k[15], 16, 530742520);
      b = hh(b, c, d, a, k[2], 23, -995338651);
      a = ii(a, b, c, d, k[0], 6, -198630844);
      d = ii(d, a, b, c, k[7], 10, 1126891415);
      c = ii(c, d, a, b, k[14], 15, -1416354905);
      b = ii(b, c, d, a, k[5], 21, -57434055);
      a = ii(a, b, c, d, k[12], 6, 1700485571);
      d = ii(d, a, b, c, k[3], 10, -1894986606);
      c = ii(c, d, a, b, k[10], 15, -1051523);
      b = ii(b, c, d, a, k[1], 21, -2054922799);
      a = ii(a, b, c, d, k[8], 6, 1873313359);
      d = ii(d, a, b, c, k[15], 10, -30611744);
      c = ii(c, d, a, b, k[6], 15, -1560198380);
      b = ii(b, c, d, a, k[13], 21, 1309151649);
      a = ii(a, b, c, d, k[4], 6, -145523070);
      d = ii(d, a, b, c, k[11], 10, -1120210379);
      c = ii(c, d, a, b, k[2], 15, 718787259);
      b = ii(b, c, d, a, k[9], 21, -343485551);
      x[0] = add32(a, x[0]);
      x[1] = add32(b, x[1]);
      x[2] = add32(c, x[2]);
      x[3] = add32(d, x[3]);
    }
    function cmn(q, a, b, x, s, t) {
      a = add32(add32(a, q), add32(x, t));
      return add32((a << s) | (a >>> (32 - s)), b);
    }
    function ff(a, b, c, d, x, s, t) {
      return cmn((b & c) | ((~b) & d), a, b, x, s, t);
    }
    function gg(a, b, c, d, x, s, t) {
      return cmn((b & d) | (c & (~d)), a, b, x, s, t);
    }
    function hh(a, b, c, d, x, s, t) {
      return cmn(b ^ c ^ d, a, b, x, s, t);
    }
    function ii(a, b, c, d, x, s, t) {
      return cmn(c ^ (b | (~d)), a, b, x, s, t);
    }
    function add32(a, b) {
      return (a + b) & 0xFFFFFFFF;
    }
    const n = string.length;
    const state = [1732584193, -271733879, -1732584194, 271733878];
    let i;
    for (i = 64; i <= string.length; i += 64) {
      md5cycle(state, md5blk(string.substring(i - 64, i)));
    }
    string = string.substring(i - 64);
    const tail = [0,0,0,0, 0,0,0,0, 0,0,0,0, 0,0,0,0];
    for (i = 0; i < string.length; i++)
      tail[i>>2] |= string.charCodeAt(i) << ((i % 4) << 3);
    tail[i>>2] |= 0x80 << ((i % 4) << 3);
    if (i > 55) {
      md5cycle(state, tail);
      for (i = 0; i < 16; i++) tail[i] = 0;
    }
    tail[14] = n * 8;
    md5cycle(state, tail);
    return state;
  }
  function md5blk(s) {
    const md5blks = [];
    for (let i = 0; i < 64; i += 4) {
      md5blks[i>>2] = s.charCodeAt(i) + (s.charCodeAt(i+1) << 8) + (s.charCodeAt(i+2) << 16) + (s.charCodeAt(i+3) << 24);
    }
    return md5blks;
  }
  const hex_chr = '0123456789abcdef'.split('');
  function rhex(n) {
    let s = '';
    for (let j = 0; j < 4; j++)
      s += hex_chr[(n >> (j * 8 + 4)) & 0x0F] + hex_chr[(n >> (j * 8)) & 0x0F];
    return s;
  }
  function hex(x) {
    for (let i = 0; i < x.length; i++)
      x[i] = rhex(x[i]);
    return x.join('');
  }

  function getGravatarUrl(email) {
    if (!email || email === '-') return 'https://www.gravatar.com/avatar/?d=mp&s=32';
    const hash = hex(md5(email.toLowerCase().trim()));
    return `https://www.gravatar.com/avatar/${hash}?d=mp&s=32`;
  }

  async function fetchFullAccount(accountId) {
    loading = true;
    const url = `https://crm.knowprocess.com/${tenant}/accounts/${accountId}`;
    const headers = {};
    if (keycloak.authenticated) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }
    try {
      const res = await fetch(url, { headers, mode: "cors" });
      if (res.ok) {
        const data = await res.json();
        return data;
      } else {
        console.error('Failed to fetch account details');
        return null;
      }
    } catch (error) {
      console.error('Error fetching account details:', error);
      return null;
    } finally {
      loading = false;
    }
  }

  async function fetchAccountContacts(accountId) {
    const url = `https://crm.knowprocess.com/${tenant}/contacts/findByAccountId?accountId=${accountId}`;
    const headers = {};
    if (keycloak.authenticated) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }
    try {
      const res = await fetch(url, { headers, mode: "cors" });
      if (res.ok) {
        const data = await res.json();
        return Array.isArray(data) ? data : [];
      } else {
        console.error('Failed to fetch contacts');
        return [];
      }
    } catch (error) {
      console.error('Error fetching contacts:', error);
      return [];
    }
  }

  async function fetchOrdersByContacts(contacts) {
    if (!contacts || contacts.length === 0) return [];
    
    const contactIds = contacts
      .map(c => c.id || c.selfRef?.split('/').pop())
      .filter(id => id)
      .join(',');
    
    if (!contactIds) return [];

    const url = `https://crm.knowprocess.com/${tenant}/orders/findByContacts/${contactIds}`;
    const headers = {};
    if (keycloak.authenticated) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }
    try {
      const res = await fetch(url, { headers, mode: "cors" });
      if (res.ok) {
        const data = await res.json();
        return Array.isArray(data) ? data : [];
      } else {
        console.error('Failed to fetch orders');
        return [];
      }
    } catch (error) {
      console.error('Error fetching orders:', error);
      return [];
    }
  }

  async function viewAccount(account) {
    const accountId = account.id || account.selfRef?.split('/').pop();
    const [fullAccount, contacts] = await Promise.all([
      fetchFullAccount(accountId),
      fetchAccountContacts(accountId)
    ]);
    if (fullAccount) {
      selectedAccount = fullAccount;
      accountContacts = contacts;
      // Fetch orders after we have contacts
      accountOrders = await fetchOrdersByContacts(contacts);
      viewMode = 'view';
    }
  }

  async function editAccount(account) {
    const accountId = account.id || account.selfRef?.split('/').pop();
    const [fullAccount, contacts] = await Promise.all([
      fetchFullAccount(accountId),
      fetchAccountContacts(accountId)
    ]);
    if (fullAccount) {
      selectedAccount = fullAccount;
      accountContacts = contacts;
      // Fetch orders after we have contacts
      accountOrders = await fetchOrdersByContacts(contacts);
      viewMode = 'edit';
    }
  }

  function backToList() {
    selectedAccount = null;
    accountContacts = [];
    accountOrders = [];
    viewMode = 'list';
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

{#if viewMode === 'list'}
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
          Created
          {#if sortColumn === 'created'}
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
          <td>{account.stage || account.accountType || '-'}</td>
          <td>{account.type || account.businessType || '-'}</td>
          <td>
            <img 
              src={getGravatarUrl(account.email || account.ownerEmail)} 
              alt={account.owner || 'Owner'} 
              title={account.owner || ''}
              class="rounded-circle"
              style="width: 32px; height: 32px;"
            />
          </td>
          <td>{formatDate(account.created)}</td>
          <td>{formatDate(account.lastUpdated || account.updated)}</td>
          <td style="max-width: 20rem;">
            {#each formatTags(account.tags) as line, i}
              {line}{#if i < formatTags(account.tags).length - 1}<br />{/if}
            {/each}
          </td>
          <td>
            <button class="btn btn-dark ms-auto" aria-label="View account" on:click={() => viewAccount(account)}>
              <i class="bi bi-eye"></i>
            </button>
            <button class="btn btn-dark ms-auto" aria-label="Edit account" on:click={() => editAccount(account)}>
              <i class="bi bi-pencil"></i>
            </button>
          </td>
        </tr>
      {/each}
    </tbody>
  </table>
{/if}
{:else}
<!-- Detail View -->
<div class="mb-3">
  <button class="btn btn-dark" on:click={backToList}>
    <i class="bi bi-arrow-left"></i> Back to List
  </button>
  <span class="ms-3 h4">{viewMode === 'edit' ? 'Edit' : 'View'} Account</span>
</div>

<div class="card bg-dark text-light">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('details')}>
    <h5 class="mb-0">{selectedAccount?.name || 'Account Details'}</h5>
    {#if panelStates.details}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.details}
  <div class="card-body">
    <div class="row">
      <!-- Column 1 -->
      <div class="col-md-6">
        <!-- ID -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">ID</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.id || ''} readonly />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.id || '-'}</div>
          {/if}
        </div>

        <!-- Parent Org -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Parent Org</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.parentOrg || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.parentOrg || '-'}</div>
          {/if}
        </div>

        <!-- Company Number -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Company Number</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.companyNumber || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.companyNumber || '-'}</div>
          {/if}
        </div>

        <!-- Owner -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Owner</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.owner || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.owner || '-'}</div>
          {/if}
        </div>

        <!-- Existing Customer -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Existing Customer?</label>
          {#if viewMode === 'edit'}
            <select class="form-control" value={selectedAccount?.existingCustomer || ''}>
              <option value="">-</option>
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.existingCustomer || '-'}</div>
          {/if}
        </div>

        <!-- Status -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Status</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.stage || selectedAccount?.accountType || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.stage || selectedAccount?.accountType || '-'}</div>
          {/if}
        </div>

        <!-- Type -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Type</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.type || selectedAccount?.businessType || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.type || selectedAccount?.businessType || '-'}</div>
          {/if}
        </div>

        <!-- Tags -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Tags</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.tags || ''} />
          {:else}
            <div class="form-control-plaintext">
              {#each formatTags(selectedAccount?.tags) as line}
                {line}<br />
              {/each}
            </div>
          {/if}
        </div>

        <!-- No of Employees -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">No of Employees</label>
          {#if viewMode === 'edit'}
            <input type="number" class="form-control" value={selectedAccount?.noOfEmployees || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.noOfEmployees || '-'}</div>
          {/if}
        </div>
      </div>

      <!-- Column 2 -->
      <div class="col-md-6">
        <!-- Website -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Website</label>
          {#if viewMode === 'edit'}
            <input type="url" class="form-control" value={selectedAccount?.website || ''} />
          {:else}
            <div class="form-control-plaintext">
              {#if selectedAccount?.website}
                <a href={selectedAccount.website} target="_blank" rel="noopener noreferrer">{selectedAccount.website}</a>
              {:else}
                -
              {/if}
            </div>
          {/if}
        </div>

        <!-- Email -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Email</label>
          {#if viewMode === 'edit'}
            <input type="email" class="form-control" value={selectedAccount?.email || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.email || '-'}</div>
          {/if}
        </div>

        <!-- Email Confirmed -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Email Confirmed?</label>
          {#if viewMode === 'edit'}
            <select class="form-control" value={selectedAccount?.emailConfirmed || ''}>
              <option value="">-</option>
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.emailConfirmed || '-'}</div>
          {/if}
        </div>

        <!-- Opt-in -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Opt-in?</label>
          {#if viewMode === 'edit'}
            <select class="form-control" value={selectedAccount?.doNotCall || selectedAccount?.emailOptIn || ''}>
              <option value="">-</option>
              <option value="true">Yes</option>
              <option value="false">No</option>
            </select>
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.doNotCall || selectedAccount?.emailOptIn || '-'}</div>
          {/if}
        </div>

        <!-- Phone -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Phone</label>
          {#if viewMode === 'edit'}
            <input type="tel" class="form-control" value={selectedAccount?.phone || selectedAccount?.phoneNumber || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.phone || selectedAccount?.phoneNumber || '-'}</div>
          {/if}
        </div>

        <!-- Address Line 1 -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Address Line 1</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.address1 || selectedAccount?.addressLine1 || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.address1 || selectedAccount?.addressLine1 || '-'}</div>
          {/if}
        </div>

        <!-- Address Line 2 -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Address Line 2</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.address2 || selectedAccount?.addressLine2 || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.address2 || selectedAccount?.addressLine2 || '-'}</div>
          {/if}
        </div>

        <!-- Address Town -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Address Town</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.town || selectedAccount?.addressTown || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.town || selectedAccount?.addressTown || '-'}</div>
          {/if}
        </div>

        <!-- Address County/City -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Address County / City</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.countyOrCity || selectedAccount?.addressCounty || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.countyOrCity || selectedAccount?.addressCounty || '-'}</div>
          {/if}
        </div>

        <!-- Postcode -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Postcode</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.postCode || selectedAccount?.zipCode || ''} />
          {:else}
            <div class="form-control-plaintext">{selectedAccount?.postCode || selectedAccount?.zipCode || '-'}</div>
          {/if}
        </div>

        <!-- Twitter -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Twitter</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.twitter || ''} />
          {:else}
            <div class="form-control-plaintext">
              {#if selectedAccount?.twitter}
                <a href="https://twitter.com/{selectedAccount.twitter}" target="_blank" rel="noopener noreferrer">{selectedAccount.twitter}</a>
              {:else}
                -
              {/if}
            </div>
          {/if}
        </div>

        <!-- LinkedIn -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">LinkedIn</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.linkedin || ''} />
          {:else}
            <div class="form-control-plaintext">
              {#if selectedAccount?.linkedin}
                <a href={selectedAccount.linkedin} target="_blank" rel="noopener noreferrer">{selectedAccount.linkedin}</a>
              {:else}
                -
              {/if}
            </div>
          {/if}
        </div>

        <!-- Facebook -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Facebook</label>
          {#if viewMode === 'edit'}
            <input type="text" class="form-control" value={selectedAccount?.facebook || ''} />
          {:else}
            <div class="form-control-plaintext">
              {#if selectedAccount?.facebook}
                <a href={selectedAccount.facebook} target="_blank" rel="noopener noreferrer">{selectedAccount.facebook}</a>
              {:else}
                -
              {/if}
            </div>
          {/if}
        </div>
      </div>
    </div>

    {#if viewMode === 'edit'}
      <div class="mt-3">
        <button class="btn btn-primary me-2">Save Changes</button>
        <button class="btn btn-secondary" on:click={backToList}>Cancel</button>
      </div>
    {/if}
  </div>
  {/if}
</div>

<!-- Additional Info Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('additionalInfo')}>
    <h5 class="mb-0">Additional Information</h5>
    {#if panelStates.additionalInfo}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.additionalInfo}
  <div class="card-body">
    {#if viewMode === 'edit'}
      <textarea class="form-control" rows="6" value={selectedAccount?.additionalInformation || ''}></textarea>
    {:else}
      <div class="form-control-plaintext" style="white-space: pre-wrap;">{selectedAccount?.additionalInformation || '-'}</div>
    {/if}
  </div>
  {/if}
</div>

<!-- Custom Fields Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('customFields')}>
    <h5 class="mb-0">Custom Fields</h5>
    {#if panelStates.customFields}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.customFields}
  <div class="card-body">
    {#if selectedAccount?.customFields}
      <div class="row">
        {#each Object.entries(selectedAccount.customFields) as [key, value]}
          <div class="col-md-6 mb-3">
            <label class="form-label text-muted text-uppercase small">{key}</label>
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={value || ''} />
            {:else}
              <div class="form-control-plaintext">{value || '-'}</div>
            {/if}
          </div>
        {/each}
      </div>
    {:else}
      <div class="text-muted">No custom fields defined</div>
    {/if}
  </div>
  {/if}
</div>

<!-- Record History Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('recordHistory')}>
    <h5 class="mb-0">Record History</h5>
    {#if panelStates.recordHistory}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.recordHistory}
  <div class="card-body">
    <div class="row">
      <div class="col-md-6">
        <!-- Created -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Created</label>
          <div class="form-control-plaintext">{formatDate(selectedAccount?.created)}</div>
        </div>

        <!-- Created By -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Created By</label>
          <div class="form-control-plaintext">{selectedAccount?.createdBy || '-'}</div>
        </div>

        <!-- First Contact -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">First Contact</label>
          {#if viewMode === 'edit'}
            <input type="date" class="form-control" value={selectedAccount?.firstContact || ''} />
          {:else}
            <div class="form-control-plaintext">{formatDate(selectedAccount?.firstContact)}</div>
          {/if}
        </div>
      </div>

      <div class="col-md-6">
        <!-- Last Updated -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Last Updated</label>
          <div class="form-control-plaintext">{formatDate(selectedAccount?.lastUpdated || selectedAccount?.updated)}</div>
        </div>

        <!-- Updated By -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Updated By</label>
          <div class="form-control-plaintext">{selectedAccount?.updatedBy || selectedAccount?.lastUpdatedBy || '-'}</div>
        </div>

        <!-- Last Contact -->
        <div class="mb-3">
          <label class="form-label text-muted text-uppercase small">Last Contact</label>
          {#if viewMode === 'edit'}
            <input type="date" class="form-control" value={selectedAccount?.lastContact || ''} />
          {:else}
            <div class="form-control-plaintext">{formatDate(selectedAccount?.lastContact)}</div>
          {/if}
        </div>
      </div>
    </div>
  </div>
  {/if}
</div>

<!-- Contacts Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('contacts')}>
    <h5 class="mb-0">Contacts ({accountContacts.length})</h5>
    {#if panelStates.contacts}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.contacts}
  <div class="card-body">
    {#if accountContacts.length > 0}
      <table class="table table-dark table-striped">
        <thead>
          <tr>
            <th>Main</th>
            <th>Name</th>
            <th>Job Title</th>
            <th>Email</th>
            <th>Opt-in</th>
            <th>Phone</th>
            <th>Twitter</th>
            <th>LinkedIn</th>
            <th>Facebook</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each accountContacts as contact}
            <tr>
              <td>
                <input 
                  type="radio" 
                  name="mainContact" 
                  checked={contact.mainContact || contact.isMainContact}
                  disabled={viewMode !== 'edit'}
                />
              </td>
              <td>{contact.firstName || ''} {contact.lastName || ''}</td>
              <td>{contact.jobTitle || contact.title || '-'}</td>
              <td>{contact.email || '-'}</td>
              <td>{contact.doNotCall || contact.emailOptIn || '-'}</td>
              <td>{contact.phone || contact.phoneNumber || '-'}</td>
              <td>
                {#if contact.twitter}
                  <a href="https://twitter.com/{contact.twitter}" target="_blank" rel="noopener noreferrer">
                    <i class="bi bi-twitter"></i>
                  </a>
                {:else}
                  -
                {/if}
              </td>
              <td>
                {#if contact.linkedin}
                  <a href={contact.linkedin} target="_blank" rel="noopener noreferrer">
                    <i class="bi bi-linkedin"></i>
                  </a>
                {:else}
                  -
                {/if}
              </td>
              <td>
                {#if contact.facebook}
                  <a href={contact.facebook} target="_blank" rel="noopener noreferrer">
                    <i class="bi bi-facebook"></i>
                  </a>
                {:else}
                  -
                {/if}
              </td>
              <td>
                <button class="btn btn-sm btn-dark" aria-label="View contact">
                  <i class="bi bi-eye"></i>
                </button>
                <button class="btn btn-sm btn-dark" aria-label="Edit contact">
                  <i class="bi bi-pencil"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {:else}
      <div class="text-muted">No contacts found</div>
    {/if}
  </div>
  {/if}
</div>

<!-- Orders Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('orders')}>
    <h5 class="mb-0">Orders ({accountOrders.length})</h5>
    {#if panelStates.orders}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.orders}
  <div class="card-body">
    {#if accountOrders.length > 0}
      <table class="table table-dark table-striped">
        <thead>
          <tr>
            <th>ID</th>
            <th>Contact Name</th>
            <th>Order Date</th>
            <th>Delivery Date</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each accountOrders as order}
            <tr>
              <td>{order.id || order.name || '-'}</td>
              <td>{order.contactName || order.contact || '-'}</td>
              <td>{formatDate(order.orderDate || order.date || order.created)}</td>
              <td>{formatDate(order.deliveryDate || order.dueDate)}</td>
              <td>{order.status || order.stage || '-'}</td>
              <td>
                <button class="btn btn-sm btn-dark" aria-label="View order">
                  <i class="bi bi-eye"></i>
                </button>
                <button class="btn btn-sm btn-dark" aria-label="Edit order">
                  <i class="bi bi-pencil"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {:else}
      <div class="text-muted">No orders found</div>
    {/if}
  </div>
  {/if}
</div>

<!-- Activities Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('activities')}>
    <h5 class="mb-0">Activities {#if selectedAccount?.activities}({selectedAccount.activities.length}){/if}</h5>
    {#if panelStates.activities}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.activities}
  <div class="card-body">
    {#if selectedAccount?.activities && selectedAccount.activities.length > 0}
      <table class="table table-dark table-striped">
        <thead>
          <tr>
            <th>Date</th>
            <th>Type</th>
            <th>Description</th>
            <th>User</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each [...selectedAccount.activities].sort((a, b) => {
            const dateA = new Date(a.created || a.date || 0);
            const dateB = new Date(b.created || b.date || 0);
            return dateB - dateA;
          }) as activity}
            <tr>
              <td>{formatDate(activity.created || activity.date)}</td>
              <td>{activity.type || activity.activityType || '-'}</td>
              <td style="max-width: 40rem;">{activity.description || activity.content || '-'}</td>
              <td>{activity.user || activity.createdBy || '-'}</td>
              <td>
                <button class="btn btn-sm btn-dark" aria-label="View activity">
                  <i class="bi bi-eye"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {:else}
      <div class="text-muted">No activities found</div>
    {/if}
  </div>
  {/if}
</div>

<!-- Notes Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('notes')}>
    <h5 class="mb-0">Notes {#if selectedAccount?.notes}({selectedAccount.notes.length}){/if}</h5>
    {#if panelStates.notes}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.notes}
  <div class="card-body">
    {#if selectedAccount?.notes && selectedAccount.notes.length > 0}
      <table class="table table-dark table-striped">
        <thead>
          <tr>
            <th>Date</th>
            <th>Author</th>
            <th>Content</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each [...selectedAccount.notes].sort((a, b) => {
            const dateA = new Date(a.created || a.date || 0);
            const dateB = new Date(b.created || b.date || 0);
            return dateB - dateA;
          }) as note}
            <tr>
              <td>{formatDate(note.created || note.date)}</td>
              <td>{note.author || note.createdBy || '-'}</td>
              <td style="max-width: 40rem; white-space: pre-wrap;">{note.content || note.text || note.note || '-'}</td>
              <td>
                <button class="btn btn-sm btn-dark" aria-label="Edit note">
                  <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-dark" aria-label="Delete note">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2">
          <i class="bi bi-plus"></i> Add Note
        </button>
      {/if}
    {:else}
      <div class="text-muted">No notes found</div>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2">
          <i class="bi bi-plus"></i> Add Note
        </button>
      {/if}
    {/if}
  </div>
  {/if}
</div>

<!-- Documents Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex justify-content-between align-items-center" style="cursor: pointer;" on:click={() => togglePanel('documents')}>
    <h5 class="mb-0">Documents {#if selectedAccount?.documents}({selectedAccount.documents.length}){/if}</h5>
    {#if panelStates.documents}
      <i class="bi bi-chevron-up"></i>
    {:else}
      <i class="bi bi-chevron-down"></i>
    {/if}
  </div>
  {#if panelStates.documents}
  <div class="card-body">
    {#if selectedAccount?.documents && selectedAccount.documents.length > 0}
      <table class="table table-dark table-striped">
        <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Size</th>
            <th>Uploaded</th>
            <th>Uploaded By</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each [...selectedAccount.documents].sort((a, b) => {
            const dateA = new Date(a.uploaded || a.created || 0);
            const dateB = new Date(b.uploaded || b.created || 0);
            return dateB - dateA;
          }) as doc}
            <tr>
              <td>{doc.name || doc.fileName || '-'}</td>
              <td>{doc.type || doc.mimeType || doc.contentType || '-'}</td>
              <td>{doc.size || '-'}</td>
              <td>{formatDate(doc.uploaded || doc.created)}</td>
              <td>{doc.uploadedBy || doc.createdBy || '-'}</td>
              <td>
                <a href={doc.url || doc.link || '#'} class="btn btn-sm btn-dark" target="_blank" rel="noopener noreferrer" aria-label="Download document">
                  <i class="bi bi-download"></i>
                </a>
                <button class="btn btn-sm btn-dark" aria-label="Delete document">
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2">
          <i class="bi bi-plus"></i> Upload Document
        </button>
      {/if}
    {:else}
      <div class="text-muted">No documents found</div>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2">
          <i class="bi bi-plus"></i> Upload Document
        </button>
      {/if}
    {/if}
  </div>
  {/if}
</div>
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
