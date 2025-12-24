/**
 * CRM Type Definitions
 * Shared types and interfaces for the CRM application
 */

/**
 * Account entity representing a company or organization
 */
export interface Account {
  id?: string;
  name: string;
  email?: string;
  phone?: string;
  phone1?: string;
  website?: string;
  industry?: string;
  selfRef?: string;
  companyNumber?: string;
  accountType?: string;
  stage?: string;
  address1?: string;
  address2?: string;
  town?: string;
  countyOrCity?: string;
  postCode?: string;
  description?: string;
  tags?: string;
  parentOrg?: string;
  owner?: string;
  businessType?: string;
  type?: string;
  firstContact?: string;
  created?: string;
  createdBy?: string;
  lastUpdated?: string;
  lastUpdatedBy?: string;
  activities?: Activity[];
  notes?: Note[];
  documents?: Document[];
  [key: string]: any;
}

/**
 * Contact entity representing an individual person
 */
export interface Contact {
  id?: string;
  firstName: string;
  lastName: string;
  email?: string;
  phone?: string;
  jobTitle?: string;
  accountId?: string;
  [key: string]: any;
}

/**
 * Order entity representing a sales order or opportunity
 */
export interface Order {
  id?: string;
  name: string;
  stage?: string;
  price?: number;
  date?: string;
  contactId?: string;
  accountId?: string;
  [key: string]: any;
}

/**
 * Activity entity representing an interaction or event
 */
export interface Activity {
  id?: string;
  type?: string;
  content?: string;
  occurred?: string;
  [key: string]: any;
}

/**
 * Note entity representing a text note or memo
 */
export interface Note {
  id?: string;
  name?: string;
  content?: string;
  favorite?: boolean;
  confidential?: boolean;
  created?: string;
  createdBy?: string;
  lastUpdated?: string;
  lastUpdatedBy?: string;
  [key: string]: any;
}

/**
 * Document entity representing an attached file or document
 */
export interface Document {
  id?: string;
  name?: string;
  url?: string;
  created?: string;
  [key: string]: any;
}

/**
 * Panel states for collapsible sections in detail views
 */
export interface PanelStates {
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

/**
 * Sort direction type
 */
export type SortDirection = 'asc' | 'desc';

/**
 * View mode type for detail pages
 */
export type ViewMode = 'view' | 'edit';

/**
 * User information from Keycloak authentication
 */
export interface UserInfo {
  username?: string;
  email?: string;
  tenant?: string;
  preferred_username?: string;
  given_name?: string;
  family_name?: string;
  name?: string;
  email_verified?: boolean;
  sub?: string;
  realm_access?: {
    roles?: string[];
  };
  resource_access?: Record<string, { roles?: string[] }>;
  attributes?: Record<string, any>;
  [key: string]: any;
}
