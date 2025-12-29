<script lang="ts">
  import { colorSchemeStore } from '$lib/colorScheme';
  import type { Contact } from '$lib/types';
  import type { ViewMode } from '$lib/types';
  import { getGravatarUrl } from '$lib/gravatar';

  export let contacts: Contact[] | undefined = undefined;
  export let viewMode: ViewMode = 'view';
  export let isOpen: boolean = true;
  export let onToggle: (() => void) | undefined = undefined;
  export let onAdd: (() => void) | undefined = undefined;
  export let onView: ((contact: Contact) => void) | undefined = undefined;
  export let onSetMain: ((contact: Contact) => void) | undefined = undefined;

  let colorScheme: 'light' | 'dark' = 'dark';

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
  });
</script>

<div class="card {colorScheme === 'dark' ? 'bg-dark text-light' : 'bg-light text-dark'} mt-3">
  <div 
    class="card-header d-flex align-items-center" 
    style="cursor: pointer;" 
    on:click={onToggle}
    on:keydown={(e) => e.key === 'Enter' && onToggle?.()}
    role="button"
    tabindex="0"
    aria-expanded={isOpen}
  >
    {#if isOpen}
      <i class="bi bi-chevron-down me-2"></i>
    {:else}
      <i class="bi bi-chevron-right me-2"></i>
    {/if}
    <h5 class="mb-0">Contacts ({contacts?.length || 0})</h5>
  </div>
  {#if isOpen}
  <div class="card-body">
    {#if contacts && contacts.length > 0}
      <table class="table {colorScheme === 'dark' ? 'table-dark' : ''} table-striped">
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
            <th style="width: 7rem;">Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each contacts as contact}
            <tr>
              <td>
                <input 
                  type="radio" 
                  name="mainContact" 
                  checked={(contact as any).mainContact || (contact as any).isMainContact}
                  disabled={viewMode !== 'edit'}
                  on:change={() => onSetMain?.(contact)}
                />
              </td>
              <td>
                <img 
                  src={getGravatarUrl(contact.email || '')} 
                  alt="Avatar" 
                  class="rounded-circle me-2" 
                  width="24" 
                  height="24"
                />
                {contact.firstName || ''} {contact.lastName || ''}
              </td>
              <td>{contact.jobTitle || (contact as any).title || '-'}</td>
              <td>{contact.email || '-'}</td>
              <td>{(contact as any).doNotCall || (contact as any).emailOptIn || '-'}</td>
              <td>{contact.phone || (contact as any).phoneNumber || '-'}</td>
              <td>
                {#if (contact as any).twitter}
                  <a href="https://twitter.com/{(contact as any).twitter}" target="_blank" rel="noopener noreferrer" aria-label="Twitter profile">
                    <i class="bi bi-twitter"></i>
                  </a>
                {:else}
                  -
                {/if}
              </td>
              <td>
                {#if (contact as any).linkedin}
                  <a href={(contact as any).linkedin} target="_blank" rel="noopener noreferrer" aria-label="LinkedIn profile">
                    <i class="bi bi-linkedin"></i>
                  </a>
                {:else}
                  -
                {/if}
              </td>
              <td>
                {#if (contact as any).facebook}
                  <a href={(contact as any).facebook} target="_blank" rel="noopener noreferrer" aria-label="Facebook profile">
                    <i class="bi bi-facebook"></i>
                  </a>
                {:else}
                  -
                {/if}
              </td>
              <td style="width: 7rem; white-space: nowrap;">
                <button 
                  class="btn btn-sm btn-dark" 
                  aria-label="View contact"
                  on:click={() => onView?.(contact)}
                >
                  <i class="bi bi-eye"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2" on:click={onAdd}>
          <i class="bi bi-plus"></i> Add Contact
        </button>
      {/if}
    {:else}
      <div class="text-muted">No contacts found</div>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2" on:click={onAdd}>
          <i class="bi bi-plus"></i> Add Contact
        </button>
      {/if}
    {/if}
  </div>
  {/if}
</div>
