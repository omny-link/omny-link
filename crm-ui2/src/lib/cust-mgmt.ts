/**
 * Customer Management API Service
 * Centralized API calls to crm.knowprocess.com
 */

import type { Account, Contact, Order, Note } from '$lib/types';
import keycloak from '$lib/keycloak';

const API_BASE = 'https://crm.knowprocess.com';

/**
 * Get authorization headers with Keycloak token
 */
function getHeaders(): Record<string, string> {
  const headers: Record<string, string> = {};
  if (keycloak.authenticated && keycloak.token) {
    headers['Authorization'] = `Bearer ${keycloak.token}`;
  }
  return headers;
}

/**
 * Get tenant from environment or default
 */
export function getTenant(): string {
  return (window as any).tenant || 'default';
}

/**
 * Fetch paginated accounts
 */
export async function fetchAccounts(tenant: string, page: number = 0): Promise<Account[]> {
  const url = `${API_BASE}/${tenant}/accounts/?page=${page}`;
  const headers = getHeaders();
  
  try {
    const res = await fetch(url, { headers, mode: 'cors' });
    if (res.ok) {
      return await res.json();
    }
  } catch (error) {
    console.error('Error fetching accounts:', error);
  }
  return [];
}

/**
 * Fetch full account details by ID
 */
export async function fetchAccount(tenant: string, id: string): Promise<Account | null> {
  const url = `${API_BASE}/${tenant}/accounts/${id}`;
  const headers = getHeaders();
  
  try {
    const res = await fetch(url, { headers, mode: 'cors' });
    if (res.ok) {
      return await res.json();
    }
  } catch (error) {
    console.error('Error fetching account:', error);
  }
  return null;
}

/**
 * Delete an account
 */
export async function deleteAccount(tenant: string, accountId: string): Promise<boolean> {
  const url = `${API_BASE}/${tenant}/accounts/${accountId}`;
  const headers = getHeaders();
  
  try {
    const res = await fetch(url, {
      method: 'DELETE',
      headers,
      mode: 'cors'
    });
    return res.ok;
  } catch (error) {
    console.error('Error deleting account:', error);
    return false;
  }
}

/**
 * Fetch contacts by account ID
 */
export async function fetchContactsByAccount(tenant: string, accountId: string): Promise<Contact[]> {
  const url = `${API_BASE}/${tenant}/contacts/findByAccountId?accountId=${accountId}`;
  const headers = getHeaders();
  
  try {
    const res = await fetch(url, { headers, mode: 'cors' });
    if (res.ok) {
      return await res.json();
    }
  } catch (error) {
    console.error('Error fetching contacts:', error);
  }
  return [];
}

/**
 * Fetch orders by contact IDs (comma-separated)
 */
export async function fetchOrdersByContacts(tenant: string, contactIds: string): Promise<Order[]> {
  if (!contactIds) return [];
  
  const url = `${API_BASE}/${tenant}/orders/findByContacts/${contactIds}`;
  const headers = getHeaders();
  
  try {
    const res = await fetch(url, { headers, mode: 'cors' });
    if (res.ok) {
      return await res.json();
    }
  } catch (error) {
    console.error('Error fetching orders:', error);
  }
  return [];
}

/**
 * Toggle note favorite status
 */
export async function toggleNoteFavorite(
  tenant: string,
  noteId: string,
  favorite: boolean
): Promise<boolean> {
  const url = `${API_BASE}/${tenant}/notes/${noteId}/favorite`;
  
  try {
    const res = await fetch(url, {
      credentials: 'omit',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8',
      },
      body: `favorite=${favorite}`,
      method: 'POST',
      mode: 'cors'
    });
    return res.ok;
  } catch (error) {
    console.error('Error toggling note favorite:', error);
    return false;
  }
}
