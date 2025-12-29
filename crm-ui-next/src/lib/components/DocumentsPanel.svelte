<script lang="ts">
  import { colorSchemeStore } from '$lib/colorScheme';
  import type { Document } from '$lib/types';
  import type { ViewMode } from '$lib/types';
  import { getGravatarUrl } from '$lib/gravatar';

  export let documents: Document[] | undefined = undefined;
  export let viewMode: ViewMode = 'view';
  export let isOpen: boolean = false;
  export let onToggle: (() => void) | undefined = undefined;
  export let onUpload: (() => void) | undefined = undefined;
  export let onDelete: ((doc: Document) => void) | undefined = undefined;

  let colorScheme: 'light' | 'dark' = 'dark';

  // Subscribe to color scheme changes
  colorSchemeStore.subscribe(scheme => {
    colorScheme = scheme;
  });

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

  function getSortedDocuments(documents: Document[]): Document[] {
    return [...documents].sort((a, b) => {
      const dateA = new Date((a as any).uploaded || a.created || 0);
      const dateB = new Date((b as any).uploaded || b.created || 0);
      return dateB.getTime() - dateA.getTime();
    });
  }
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
    <h5 class="mb-0">Documents {#if documents}({documents.length}){/if}</h5>
  </div>
  {#if isOpen}
  <div class="card-body">
    {#if documents && documents.length > 0}
      <table class="table {colorScheme === 'dark' ? 'table-dark' : ''} table-striped">
        <thead>
          <tr>
            <th style="width: 60%">Name</th>
            <th>Uploaded</th>
            <th>Uploaded By</th>
            <th style="width: 7rem;">Actions</th>
          </tr>
        </thead>
        <tbody>
          {#each getSortedDocuments(documents) as doc}
            <tr>
              <td style="width: 60%">{doc.name || (doc as any).fileName || '-'}</td>
              <td>{formatDate((doc as any).uploaded || doc.created)}</td>
              <td>
                <img 
                  src={getGravatarUrl((doc as any).uploaderEmail || (doc as any).uploadedBy || (doc as any).createdBy || '')}
                  alt={(doc as any).uploadedBy || (doc as any).createdBy || 'Author'}
                  title={(doc as any).uploadedBy || (doc as any).createdBy || ''}
                  class="rounded-circle me-2"
                  width="24"
                  height="24"
                />
                {(doc as any).uploadedBy || (doc as any).createdBy || '-'}
              </td>
               <td style="width: 7rem; white-space: nowrap;">
                <button 
                  class="btn btn-sm {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  type="button"
                  on:click={() => window.open(doc.url || (doc as any).link || '#', '_blank')}
                  title="Open document in new tab"
                  aria-label="Open document in new tab"
                >
                  <i class="bi bi-box-arrow-up-right"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
    {:else}
      <div class="text-muted">No documents found</div>
    {/if}
  </div>
  {/if}
</div>
