<script lang="ts">
  import type { Note } from '$lib/types';
  import type { ViewMode } from '$lib/types';

  export let id: string | undefined = undefined;
  export let notes: Note[] | undefined = undefined;
  export let viewMode: ViewMode = 'view';
  export let isOpen: boolean = false;
  export let onToggle: (() => void) | undefined = undefined;
  export let onAdd: (() => void) | undefined = undefined;
  export let onEdit: ((note: Note) => void) | undefined = undefined;
  export let onDelete: ((note: Note) => void) | undefined = undefined;

  function formatDate(dateString: string | undefined): string {
    if (!dateString || dateString === '-') return '-';
    try {
      const date = new Date(dateString);
      if (isNaN(date.getTime())) return dateString;
      return date.toLocaleDateString(navigator.language, {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return dateString;
    }
  }

  function getSortedNotes(notes: Note[]): Note[] {
    return [...notes].sort((a, b) => {
      const dateA = new Date(a.created || (a as any).date || 0);
      const dateB = new Date(b.created || (b as any).date || 0);
      return dateB.getTime() - dateA.getTime();
    });
  }
</script>

<div class="card bg-dark text-light mt-3" {id}>
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
    <h5 class="mb-0">Notes {#if notes}({notes.length}){/if}</h5>
  </div>
  {#if isOpen}
  <div class="card-body">
    {#if notes && notes.length > 0}
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
          {#each getSortedNotes(notes) as note}
            <tr>
              <td>{formatDate(note.created || (note as any).date)}</td>
              <td>{(note as any).author || (note as any).createdBy || '-'}</td>
              <td style="max-width: 40rem; white-space: pre-wrap;">{note.content || (note as any).text || (note as any).note || '-'}</td>
              <td>
                <button 
                  class="btn btn-sm btn-dark" 
                  aria-label="Edit note"
                  on:click={() => onEdit?.(note)}
                >
                  <i class="bi bi-pencil"></i>
                </button>
                <button 
                  class="btn btn-sm btn-dark" 
                  aria-label="Delete note"
                  on:click={() => onDelete?.(note)}
                >
                  <i class="bi bi-trash"></i>
                </button>
              </td>
            </tr>
          {/each}
        </tbody>
      </table>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2" on:click={onAdd}>
          <i class="bi bi-plus"></i> Add Note
        </button>
      {/if}
    {:else}
      <div class="text-muted">No notes found</div>
      {#if viewMode === 'edit'}
        <button class="btn btn-primary btn-sm mt-2" on:click={onAdd}>
          <i class="bi bi-plus"></i> Add Note
        </button>
      {/if}
    {/if}
  </div>
  {/if}
</div>
