import { useState, useEffect } from 'react';
import { getAIApiKey, setAIApiKey, isAIEnabled } from '../utils/storage';

interface SettingsScreenProps {
  onReturnHome: () => void;
}

export function SettingsScreen({ onReturnHome }: SettingsScreenProps) {
  const [apiKey, setApiKey] = useState('');
  const [isEnabled, setIsEnabled] = useState(false);
  const [showKey, setShowKey] = useState(false);

  useEffect(() => {
    const storedKey = getAIApiKey();
    setIsEnabled(isAIEnabled());
    if (storedKey) {
      // Show masked version
      setApiKey(storedKey.substring(0, 4) + '•'.repeat(storedKey.length - 8) + storedKey.substring(storedKey.length - 4));
    }
  }, []);

  const handleSave = () => {
    if (apiKey.trim()) {
      // If user is editing, they need to enter full key
      // For now, if it contains dots, assume it's masked - don't save
      if (!apiKey.includes('•')) {
        setAIApiKey(apiKey.trim());
        setIsEnabled(true);
        setShowKey(false);
        // Reset to show masked version
        const fullKey = getAIApiKey();
        if (fullKey) {
          setApiKey(fullKey.substring(0, 4) + '•'.repeat(fullKey.length - 8) + fullKey.substring(fullKey.length - 4));
        }
      }
    } else {
      setAIApiKey(null);
      setIsEnabled(false);
    }
  };

  const handleClear = () => {
    setAIApiKey(null);
    setIsEnabled(false);
    setApiKey('');
    setShowKey(false);
  };

  const handleEdit = () => {
    const storedKey = getAIApiKey();
    if (storedKey) {
      setApiKey(storedKey);
      setShowKey(true);
    } else {
      setShowKey(true);
      setApiKey('');
    }
  };

  return (
    <div className="min-h-screen py-12 px-8 animate-fadeIn">
      <div className="max-w-md mx-auto">
        <div className="mb-12">
          <h1 
            className="text-[#2B2B2B] mb-2"
            style={{ fontSize: '28px', letterSpacing: '0.01em' }}
          >
            Settings
          </h1>
          <p 
            className="text-[#6E6E6E]"
            style={{ fontSize: '14px' }}
          >
            Optional enhancements
          </p>
        </div>

        <div className="flex flex-col gap-8 mb-12">
          {/* AI Enhancement Section */}
          <div className="bg-white rounded-xl p-6 border border-[#E6E1D8]">
            <h2 
              className="text-[#2B2B2B] mb-2"
              style={{ fontSize: '16px', letterSpacing: '0.01em' }}
            >
              Optional AI enhancement
            </h2>
            <p 
              className="text-[#6E6E6E] mb-4"
              style={{ fontSize: '13px', lineHeight: '1.6' }}
            >
              Your key never leaves this device.
            </p>
            
            {!showKey && !isEnabled ? (
              <button
                onClick={handleEdit}
                className="px-6 py-2 bg-[#2B2B2B] text-[#FAF7F2] rounded-full text-sm"
              >
                Add API key
              </button>
            ) : (
              <div className="flex flex-col gap-4">
                <div>
                  <input
                    type={showKey ? 'text' : 'password'}
                    value={apiKey}
                    onChange={(e) => setApiKey(e.target.value)}
                    placeholder="Paste API key (stored locally)"
                    className="w-full px-4 py-3 bg-white border border-[#E6E1D8] rounded-lg text-[#2B2B2B] placeholder:text-[#6E6E6E]/40 focus:outline-none focus:border-[#D6A84F] transition-colors"
                    style={{ fontSize: '14px' }}
                  />
                </div>
                <div className="flex gap-3">
                  <button
                    onClick={handleSave}
                    className="px-6 py-2 bg-[#2B2B2B] text-[#FAF7F2] rounded-full text-sm flex-1"
                  >
                    Save
                  </button>
                  {isEnabled && (
                    <button
                      onClick={handleClear}
                      className="px-6 py-2 border border-[#E6E1D8] text-[#6E6E6E] rounded-full text-sm"
                    >
                      Clear
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>

        <div className="flex justify-center pt-4">
          <button
            onClick={onReturnHome}
            className="text-[#2B2B2B]"
            style={{ fontSize: '14px' }}
          >
            ← Return home
          </button>
        </div>
      </div>
    </div>
  );
}

