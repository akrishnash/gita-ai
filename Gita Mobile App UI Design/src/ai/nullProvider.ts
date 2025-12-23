// Default no-op AI provider
// Used when AI is disabled or unavailable

import { AIProvider } from './aiProvider';

export class NullProvider implements AIProvider {
  async refineReflection(): Promise<string | null> {
    // Always returns null - no AI enhancement
    return null;
  }
}

