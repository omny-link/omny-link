<script lang="ts">
  import { onMount } from 'svelte';
  import keycloak, { initKeycloak, fetchUserAccount } from '$lib/keycloak';
  import { getGravatarUrl } from '$lib/gravatar';

  interface Account {
    id?: string;
    name: string;
    email?: string;
    phone?: string;
    website?: string;
    industry?: string;
    [key: string]: any;
  }

  interface Contact {
    id?: string;
    firstName: string;
    lastName: string;
    email?: string;
    [key: string]: any;
  }

  interface Order {
    id?: string;
    name: string;
    [key: string]: any;
  }

  interface PanelStates {
    details: boolean;
    additionalInfo: boolean;
    customFields: boolean;
    recordHistory: boolean;
    contacts: boolean;
    orders: boolean;
    activities: boolean;
    notes: boolean;
    documents: boolean;
  }

  let userInfo: any = null;
  let tenant: string = 'acme';
  let accounts: Account[] = [];
  let filteredAccounts: Account[] = [];
  let searchQuery: string = '';
  let loading: boolean = false;
  let page: number = 1;
  let allLoaded: boolean = false;
  let sortColumn: string = 'updated';
  let sortDirection: 'asc' | 'desc' = 'desc';
  let selectedAccount: Account | null = null;
  let viewMode: 'list' | 'view' | 'edit' = 'list';
  let accountContacts: Contact[] = [];
  let accountOrders: Order[] = [];
  let panelStates: PanelStates = {
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

  function togglePanel(panelName: keyof PanelStates): void {
    panelStates[panelName] = !panelStates[panelName];
  }

  function scrollToNotes(): void {
    // Open the notes panel if closed
    if (!panelStates.notes) {
      panelStates.notes = true;
    }
    // Wait for panel to open, then scroll
    setTimeout(() => {
      const notesPanel = document.getElementById('notes-panel');
      if (notesPanel) {
        notesPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  async function deleteAccount(): Promise<void> {
    if (!selectedAccount) return;
    
    const confirmed = confirm(`Are you sure you want to delete "${selectedAccount.name}"?`);
    if (!confirmed) return;

    const url = `https://crm.knowprocess.com/${tenant}/accounts/${selectedAccount.id}`;
    const headers = {};
    if (keycloak.authenticated) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }

    try {
      const res = await fetch(url, {
        method: 'DELETE',
        headers,
        mode: 'cors'
      });

      if (res.ok) {
        alert('Account deleted successfully');
        // Remove from local array
        accounts = accounts.filter(a => a.id !== selectedAccount.id);
        filteredAccounts = filteredAccounts.filter(a => a.id !== selectedAccount.id);
        // Go back to list
        backToList();
      } else {
        alert('Failed to delete account');
      }
    } catch (error) {
      console.error('Error deleting account:', error);
      alert('Error deleting account');
    }
  }

  // Stale-while-refresh: show what we have, keep loading in background
  async function fetchAccounts(nextPage: number): Promise<void> {
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

  function toSentenceCase(str: string): string {
    if (!str) return str;
    // Convert camelCase to Sentence case
    // e.g., "firstName" -> "First name", "emailAddress" -> "Email address"
    return str
      .replace(/([A-Z])/g, ' $1') // Add space before capital letters
      .replace(/^./, (match) => match.toUpperCase()) // Capitalize first letter
      .trim();
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

  async function fetchFullAccount(accountId: string): Promise<void> {
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

  async function fetchAccountContacts(accountId: string): Promise<void> {
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

  async function fetchOrdersByContacts(contacts: Contact[]): Promise<void> {
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

  async function viewAccount(account: Account): Promise<void> {
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

  async function editAccount(account: Account): Promise<void> {
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

  function backToList(): void {
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
<div class="mb-3 d-flex justify-content-between align-items-center">
  <div>
    <button class="btn btn-dark" on:click={backToList}>
      <i class="bi bi-arrow-left"></i> Back to List
    </button>
    <span class="ms-3 h4">{viewMode === 'edit' ? 'Edit' : 'View'} Account</span>
  </div>
  <div class="d-flex gap-2">
    <button class="btn btn-danger" on:click={deleteAccount} title="Delete account">
      <i class="bi bi-trash"></i> Delete
    </button>
    <button class="btn btn-dark" on:click={scrollToNotes} title="Jump to notes">
      <i class="bi bi-journal-text"></i> Notes
    </button>
    <div class="dropdown">
      <button class="btn btn-dark dropdown-toggle" type="button" id="customActionsDropdown" data-bs-toggle="dropdown" aria-expanded="false">
        <i class="bi bi-three-dots-vertical"></i> Actions
      </button>
      <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark" aria-labelledby="customActionsDropdown">
        <li><a class="dropdown-item" href="#" on:click|preventDefault={() => alert('Clone feature coming soon')}>Clone Account</a></li>
        <li><a class="dropdown-item" href="#" on:click|preventDefault={() => alert('Export feature coming soon')}>Export to PDF</a></li>
        <li><a class="dropdown-item" href="#" on:click|preventDefault={() => alert('Merge feature coming soon')}>Merge with Another</a></li>
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item" href="#" on:click|preventDefault={() => alert('Archive feature coming soon')}>Archive Account</a></li>
      </ul>
    </div>
  </div>
</div>

<div class="card bg-dark text-light">
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('details')}>
    {#if panelStates.details}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">{selectedAccount?.name || 'Account Details'}</h5>
  </div>
  {#if panelStates.details}
  <div class="card-body">
    <div class="row">
      <!-- Column 1 -->
      <div class="col-md-6">
        <!-- ID -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">ID</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.id || ''} readonly />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.id || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Parent Org -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Parent Org</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.parentOrg || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.parentOrg || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Company Number -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Company Number</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.companyNumber || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.companyNumber || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Owner -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Owner</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.owner || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.owner || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Existing Customer -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Existing Customer?</label>
          <div class="col-sm-8">
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
        </div>

        <!-- Status -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Status</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.stage || selectedAccount?.accountType || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.stage || selectedAccount?.accountType || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Type -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Type</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.type || selectedAccount?.businessType || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.type || selectedAccount?.businessType || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Tags -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Tags</label>
          <div class="col-sm-8">
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
        </div>

        <!-- No of Employees -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">No of Employees</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="number" class="form-control" value={selectedAccount?.noOfEmployees || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.noOfEmployees || '-'}</div>
            {/if}
          </div>
        </div>
      </div>

      <!-- Column 2 -->
      <div class="col-md-6">
        <!-- Website -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Website</label>
          <div class="col-sm-8">
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
        </div>

        <!-- Email -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Email</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="email" class="form-control" value={selectedAccount?.email || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.email || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Email Confirmed -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Email Confirmed?</label>
          <div class="col-sm-8">
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
        </div>

        <!-- Opt-in -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Opt-in?</label>
          <div class="col-sm-8">
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
        </div>

        <!-- Phone -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Phone</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="tel" class="form-control" value={selectedAccount?.phone || selectedAccount?.phoneNumber || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.phone || selectedAccount?.phoneNumber || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Address Line 1 -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Address Line 1</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.address1 || selectedAccount?.addressLine1 || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.address1 || selectedAccount?.addressLine1 || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Address Line 2 -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Address Line 2</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.address2 || selectedAccount?.addressLine2 || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.address2 || selectedAccount?.addressLine2 || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Address Town -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Address Town</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.town || selectedAccount?.addressTown || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.town || selectedAccount?.addressTown || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Address County/City -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Address County / City</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.countyOrCity || selectedAccount?.addressCounty || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.countyOrCity || selectedAccount?.addressCounty || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Postcode -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Postcode</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={selectedAccount?.postCode || selectedAccount?.zipCode || ''} />
            {:else}
              <div class="form-control-plaintext">{selectedAccount?.postCode || selectedAccount?.zipCode || '-'}</div>
            {/if}
          </div>
        </div>

        <!-- Twitter -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Twitter</label>
          <div class="col-sm-8">
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
        </div>

        <!-- LinkedIn -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">LinkedIn</label>
          <div class="col-sm-8">
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
        </div>

        <!-- Facebook -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Facebook</label>
          <div class="col-sm-8">
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
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('additionalInfo')}>
    {#if panelStates.additionalInfo}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Additional Information</h5>
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
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('customFields')}>
    {#if panelStates.customFields}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Custom Fields</h5>
  </div>
  {#if panelStates.customFields}
  <div class="card-body">
    {#if selectedAccount?.customFields}
      {#each Object.entries(selectedAccount.customFields) as [key, value]}
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">{toSentenceCase(key)}</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="text" class="form-control" value={value || ''} />
            {:else}
              <div class="form-control-plaintext">{value || '-'}</div>
            {/if}
          </div>
        </div>
      {/each}
    {:else}
      <div class="text-muted">No custom fields defined</div>
    {/if}
  </div>
  {/if}
</div>

<!-- Record History Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('recordHistory')}>
    {#if panelStates.recordHistory}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Record History</h5>
  </div>
  {#if panelStates.recordHistory}
  <div class="card-body">
    <div class="row">
      <div class="col-md-6">
        <!-- Created -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Created</label>
          <div class="col-sm-8">
            <div class="form-control-plaintext">{formatDate(selectedAccount?.created)}</div>
          </div>
        </div>

        <!-- Created By -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Created By</label>
          <div class="col-sm-8">
            <div class="form-control-plaintext">{selectedAccount?.createdBy || '-'}</div>
          </div>
        </div>

        <!-- First Contact -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">First Contact</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="date" class="form-control" value={selectedAccount?.firstContact || ''} />
            {:else}
              <div class="form-control-plaintext">{formatDate(selectedAccount?.firstContact)}</div>
            {/if}
          </div>
        </div>
      </div>

      <div class="col-md-6">
        <!-- Last Updated -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Last Updated</label>
          <div class="col-sm-8">
            <div class="form-control-plaintext">{formatDate(selectedAccount?.lastUpdated || selectedAccount?.updated)}</div>
          </div>
        </div>

        <!-- Updated By -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Updated By</label>
          <div class="col-sm-8">
            <div class="form-control-plaintext">{selectedAccount?.updatedBy || selectedAccount?.lastUpdatedBy || '-'}</div>
          </div>
        </div>

        <!-- Last Contact -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Last Contact</label>
          <div class="col-sm-8">
            {#if viewMode === 'edit'}
              <input type="date" class="form-control" value={selectedAccount?.lastContact || ''} />
            {:else}
              <div class="form-control-plaintext">{formatDate(selectedAccount?.lastContact)}</div>
            {/if}
          </div>
        </div>
      </div>
    </div>
  </div>
  {/if}
</div>

<!-- Contacts Panel -->
<div class="card bg-dark text-light mt-3">
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('contacts')}>
    {#if panelStates.contacts}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Contacts ({accountContacts.length})</h5>
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
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('orders')}>
    {#if panelStates.orders}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Orders ({accountOrders.length})</h5>
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
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('activities')}>
    {#if panelStates.activities}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Activities {#if selectedAccount?.activities}({selectedAccount.activities.length}){/if}</h5>
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
<div class="card bg-dark text-light mt-3" id="notes-panel">
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('notes')}>
    {#if panelStates.notes}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Notes {#if selectedAccount?.notes}({selectedAccount.notes.length}){/if}</h5>
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
  <div class="card-header d-flex align-items-center" style="cursor: pointer;" on:click={() => togglePanel('documents')}>
    {#if panelStates.documents}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Documents {#if selectedAccount?.documents}({selectedAccount.documents.length}){/if}</h5>
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

  .field-label {
    font-size: 0.875rem;
    color: #6c757d;
  }
</style>
