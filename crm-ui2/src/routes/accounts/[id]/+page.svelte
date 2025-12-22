<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { goto } from '$app/navigation';
  import keycloak, { initKeycloak, fetchUserAccount } from '$lib/keycloak';
  import { getGravatarUrl } from '$lib/gravatar';
  import type { Account, Contact, Order, PanelStates, ViewMode } from '$lib/types';

  let accountId: string;
  let tenant: string = 'acme';
  let selectedAccount: Account | null = null;
  let accountContacts: Contact[] = [];
  let accountOrders: Order[] = [];
  let loading: boolean = false;
  let viewMode: ViewMode = 'view';
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
    if (!panelStates.notes) {
      panelStates.notes = true;
    }
    setTimeout(() => {
      const notesPanel = document.getElementById('notes-panel');
      if (notesPanel) {
        notesPanel.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    }, 100);
  }

  function toSentenceCase(str: string): string {
    if (!str) return str;
    return str
      .replace(/([A-Z])/g, ' $1')
      .replace(/^./, (match) => match.toUpperCase())
      .trim();
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
    const tagArray = typeof tags === 'string' ? tags.split(',').map(t => t.trim()) : tags;
    const lines = [];
    let currentLine = '';
    
    for (const tag of tagArray) {
      if (currentLine.length + tag.length + 2 > 60) {
        if (currentLine) lines.push(currentLine);
        currentLine = tag;
      } else {
        currentLine = currentLine ? `${currentLine}, ${tag}` : tag;
      }
    }
    
    if (currentLine) lines.push(currentLine);
    
    return lines.length > 0 ? lines : ['-'];
  }

  async function fetchFullAccount(id: string): Promise<Account | null> {
    loading = true;
    const url = `https://crm.knowprocess.com/${tenant}/accounts/${id}`;
    const headers: Record<string, string> = {};
    if (keycloak.authenticated) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }
    try {
      const res = await fetch(url, { headers, mode: "cors" });
      if (res.ok) {
        return await res.json();
      }
    } catch (error) {
      console.error('Error fetching account:', error);
    } finally {
      loading = false;
    }
    return null;
  }

  async function fetchAccountContacts(id: string): Promise<Contact[]> {
    const url = `https://crm.knowprocess.com/${tenant}/accounts/${id}/contacts`;
    const headers: Record<string, string> = {};
    if (keycloak.authenticated) {
      headers['Authorization'] = `Bearer ${keycloak.token}`;
    }
    try {
      const res = await fetch(url, { headers, mode: "cors" });
      if (res.ok) {
        return await res.json();
      }
    } catch (error) {
      console.error('Error fetching contacts:', error);
    }
    return [];
  }

  async function fetchOrdersByContacts(contacts: Contact[]): Promise<Order[]> {
    if (contacts.length === 0) return [];
    
    const orderPromises = contacts.map(async (contact) => {
      const contactId = contact.id || contact.selfRef?.split('/').pop();
      if (!contactId) return [];
      
      const url = `https://crm.knowprocess.com/${tenant}/contacts/${contactId}/orders`;
      const headers: Record<string, string> = {};
      if (keycloak.authenticated) {
        headers['Authorization'] = `Bearer ${keycloak.token}`;
      }
      try {
        const res = await fetch(url, { headers, mode: "cors" });
        if (res.ok) {
          return await res.json();
        }
      } catch (error) {
        console.error(`Error fetching orders for contact ${contactId}:`, error);
      }
      return [];
    });

    const ordersArrays = await Promise.all(orderPromises);
    return ordersArrays.flat();
  }

  async function deleteAccount(): Promise<void> {
    if (!selectedAccount) return;
    
    const confirmed = confirm(`Are you sure you want to delete "${selectedAccount.name}"?`);
    if (!confirmed) return;

    const url = `https://crm.knowprocess.com/${tenant}/accounts/${selectedAccount.id}`;
    const headers: Record<string, string> = {};
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
        goto('/accounts');
      } else {
        alert('Failed to delete account');
      }
    } catch (error) {
      console.error('Error deleting account:', error);
      alert('Error deleting account');
    }
  }

  function backToList(): void {
    goto('/accounts');
  }

  function toggleEditMode(): void {
    viewMode = viewMode === 'view' ? 'edit' : 'view';
  }

  onMount(async () => {
    await initKeycloak();
    
    if (!keycloak.authenticated) {
      await keycloak.login({ redirectUri: window.location.href });
      return;
    }
    
    // Get tenant from user account
    const { tenant: userTenant } = await fetchUserAccount();
    tenant = userTenant;
    
    // Get account ID from URL
    accountId = $page.params.id;
    
    // Load account data
    const [account, contacts] = await Promise.all([
      fetchFullAccount(accountId),
      fetchAccountContacts(accountId)
    ]);
    
    if (account) {
      selectedAccount = account;
      accountContacts = contacts;
      accountOrders = await fetchOrdersByContacts(contacts);
    } else {
      alert('Account not found');
      goto('/accounts');
    }
  });
</script>

{#if loading}
  <div class="alert alert-info">Loading account...</div>
{:else if selectedAccount}
<!-- Detail View -->
<div class="mb-3 d-flex justify-content-between align-items-center">
  <div>
    <button class="btn btn-dark" on:click={backToList}>
      <i class="bi bi-arrow-left"></i> Back to List
    </button>
    <span class="ms-3 h4">{viewMode === 'edit' ? 'Edit' : 'View'} Account</span>
  </div>
  <div class="d-flex gap-2">
    <button class="btn btn-{viewMode === 'edit' ? 'secondary' : 'primary'}" on:click={toggleEditMode}>
      <i class="bi bi-{viewMode === 'edit' ? 'eye' : 'pencil'}"></i> {viewMode === 'edit' ? 'View' : 'Edit'}
    </button>
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
        <button class="btn btn-secondary" on:click={toggleEditMode}>Cancel</button>
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
                  <a href="https://twitter.com/{contact.twitter}" target="_blank" rel="noopener noreferrer" aria-label="Twitter profile">
                    <i class="bi bi-twitter"></i>
                  </a>
                {:else}
                  -
                {/if}
              </td>
              <td>
                {#if contact.linkedin}
                  <a href={contact.linkedin} target="_blank" rel="noopener noreferrer" aria-label="LinkedIn profile">
                    <i class="bi bi-linkedin"></i>
                  </a>
                {:else}
                  -
                {/if}
              </td>
              <td>
                {#if contact.facebook}
                  <a href={contact.facebook} target="_blank" rel="noopener noreferrer" aria-label="Facebook profile">
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
            return dateB.getTime() - dateA.getTime();
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
            return dateB.getTime() - dateA.getTime();
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
            return dateB.getTime() - dateA.getTime();
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
  .field-label {
    font-size: 0.875rem;
    color: #6c757d;
  }
</style>
