<script lang="ts">
  import { colorSchemeStore } from '$lib/colorScheme';
  import type { Document } from '$lib/types';
  import type { ViewMode } from '$lib/types';

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
          {#each getSortedDocuments(documents) as doc}
            <tr>
              <td>{doc.name || (doc as any).fileName || '-'}</td>
              <td>{(doc as any).type || (doc as any).mimeType || (doc as any).contentType || '-'}</td>
              <td>{(doc as any).size || '-'}</td>
              <td>{formatDate((doc as any).uploaded || doc.created)}</td>
              <td>{(doc as any).uploadedBy || (doc as any).createdBy || '-'}</td>
              <td>
                <a 
                  href={doc.url || (doc as any).link || '#'} 
                  class="btn btn-sm {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  target="_blank" 
                  rel="noopener noreferrer" 
                  aria-label="Download document"
                >
                  <i class="bi bi-download"></i>
                </a>
                <button 
                  class="btn btn-sm {colorScheme === 'dark' ? 'btn-dark' : 'btn-light'}" 
                  aria-label="Delete document"
                  on:click={() => onDelete?.(doc)}
                >
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2" on:click={onUpload}>
          <i class="bi bi-plus"></i> Upload Document
        </button>
      {/if}
    {:else}
      <div class="text-muted">No documents found</div>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2" on:click={onUpload}>
          <i class="bi bi-plus"></i> Upload Document
        </button>
      {/if}
    {/if}
  </div>
  {/if}
</div>
