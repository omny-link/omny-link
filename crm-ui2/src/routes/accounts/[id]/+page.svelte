<script lang="ts">
  import { onMount } from 'svelte';
  import { page } from '$app/stores';
  import { get } from 'svelte/store';
  import { goto } from '$app/navigation';
  import keycloak, { initKeycloak, fetchUserAccount } from '$lib/keycloak';
  import { colorSchemeStore } from '$lib/colorScheme';
  import { 
    fetchAccount,
    fetchContactsByAccount,
    fetchOrdersByContacts as fetchOrdersByContactsAPI,
    deleteAccount as deleteAccountAPI
  } from '$lib/cust-mgmt';
  import type { Account, Contact, Order, PanelStates, ViewMode } from '$lib/types';
  import CustomFieldsPanel from '$lib/components/CustomFieldsPanel.svelte';
  import NotesPanel from '$lib/components/NotesPanel.svelte';
  import DocumentsPanel from '$lib/components/DocumentsPanel.svelte';
  import ContactsPanel from '$lib/components/ContactsPanel.svelte';
  import OrdersPanel from '$lib/components/OrdersPanel.svelte';
  import ActivitiesPanel from '$lib/components/ActivitiesPanel.svelte';

  let accountId: string;
  let tenant: string = 'acme';
  let selectedAccount: Account | null = null;
  let accountContacts: Contact[] = [];
  let accountOrders: Order[] = [];
  let loading: boolean = false;
  let viewMode: ViewMode = 'view';
  let colorScheme: 'light' | 'dark' = 'dark';

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
  });

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
    try {
      return await fetchAccount(tenant, id);
    } catch (error) {
      console.error('Error fetching account:', error);
      return null;
    } finally {
      loading = false;
    }
  }

  async function fetchAccountContacts(id: string): Promise<Contact[]> {
    try {
      return await fetchContactsByAccount(tenant, id);
    } catch (error) {
      console.error('Error fetching contacts:', error);
      return [];
    }
  }

  async function fetchOrdersByContacts(contacts: Contact[]): Promise<Order[]> {
    if (contacts.length === 0) return [];
    
    // Extract contact IDs and join with commas
    const contactIds = contacts
      .map(contact => contact.id || contact.selfRef?.split('/').pop())
      .filter(id => id)
      .join(',');
    
    if (!contactIds) return [];
    
    try {
      return await fetchOrdersByContactsAPI(tenant, contactIds);
    } catch (error) {
      console.error('Error fetching orders:', error);
      return [];
    }
  }

  async function deleteAccount(): Promise<void> {
    if (!selectedAccount?.id) return;
    
    const confirmed = confirm(`Are you sure you want to delete "${selectedAccount.name}"?`);
    if (!confirmed) return;

    try {
      const success = await deleteAccountAPI(tenant, selectedAccount.id);
      if (success) {
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
    accountId = get(page).params.id;
    
    // Load account data and contacts
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
    <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" on:click={backToList}>
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
    <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" on:click={scrollToNotes} title="Jump to notes">
      <i class="bi bi-journal-text"></i> Notes
    </button>
    <div class="dropdown">
      <button class="btn {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'} dropdown-toggle" type="button" id="customActionsDropdown" data-bs-toggle="dropdown" aria-expanded="false">
        <i class="bi bi-three-dots-vertical"></i> Actions
      </button>
      <ul class="dropdown-menu dropdown-menu-end dropdown-menu-dark" aria-labelledby="customActionsDropdown">
        <li><button class="dropdown-item" type="button" on:click={() => alert('Clone feature coming soon')}>Clone Account</button></li>
        <li><button class="dropdown-item" type="button" on:click={() => alert('Export feature coming soon')}>Export to PDF</button></li>
        <li><button class="dropdown-item" type="button" on:click={() => alert('Merge feature coming soon')}>Merge with Another</button></li>
        <li><hr class="dropdown-divider"></li>
        <li><button class="dropdown-item" type="button" on:click={() => alert('Archive feature coming soon')}>Archive Account</button></li>
      </ul>
    </div>
  </div>
</div>

<div class="card {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'}">
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
<div class="card {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'} mt-3">
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

<CustomFieldsPanel 
  customFields={selectedAccount?.customFields}
  {viewMode}
  isOpen={panelStates.customFields}
  onToggle={() => togglePanel('customFields')}
  onChange={(key, value) => {
    if (selectedAccount) {
      selectedAccount.customFields = { ...selectedAccount.customFields, [key]: value };
    }
  }}
/>

<!-- Record History Panel -->
<div class="card {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'} mt-3">
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
            <div class="form-control-plaintext">{formatDate(selectedAccount?.lastUpdated)}</div>
          </div>
        </div>

        <!-- Updated By -->
        <div class="mb-3 row">
          <label class="col-sm-4 col-form-label field-label text-end">Updated By</label>
          <div class="col-sm-8">
            <div class="form-control-plaintext">{selectedAccount?.lastUpdatedBy || '-'}</div>
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

<ContactsPanel
  contacts={accountContacts}
  {viewMode}
  isOpen={panelStates.contacts}
  onToggle={() => togglePanel('contacts')}
  onAdd={() => alert('Add contact feature coming soon')}
  onView={(contact) => alert('View contact feature coming soon')}
  onSetMain={(contact) => alert('Set main contact feature coming soon')}
/>

<OrdersPanel
  orders={accountOrders}
  {viewMode}
  isOpen={panelStates.orders}
  onToggle={() => togglePanel('orders')}
  onView={(order) => alert('View order feature coming soon')}
  onEdit={(order) => alert('Edit order feature coming soon')}
/>

<ActivitiesPanel
  activities={selectedAccount?.activities}
  {viewMode}
  isOpen={panelStates.activities}
  onToggle={() => togglePanel('activities')}
  onView={(activity) => alert('View activity feature coming soon')}
/>

<NotesPanel
  id="notes-panel"
  notes={selectedAccount?.notes}
  {viewMode}
  isOpen={panelStates.notes}
  onToggle={() => togglePanel('notes')}
  onAdd={() => alert('Add note feature coming soon')}
/>

<DocumentsPanel
  documents={selectedAccount?.documents}
  {viewMode}
  isOpen={panelStates.documents}
  onToggle={() => togglePanel('documents')}
  onUpload={() => alert('Upload document feature coming soon')}
  onDelete={(doc) => alert('Delete document feature coming soon')}
/>
{/if}

<style>
  .field-label {
    font-size: 0.875rem;
    color: #6c757d;
  }
</style>
