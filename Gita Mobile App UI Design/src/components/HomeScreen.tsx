import { useState } from 'react';

interface HomeScreenProps {
  onSubmit: (problem: string) => void;
  onViewHistory: () => void;
  onViewSettings?: () => void;
}

export function HomeScreen({ onSubmit, onViewHistory, onViewSettings }: HomeScreenProps) {
  const [input, setInput] = useState('');

  const handleSubmit = () => {
    if (input.trim()) {
      onSubmit(input.trim());
    }
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen px-8 animate-fadeIn">
      <div className="flex-1 flex flex-col items-center justify-center w-full">
        <h1 className="text-[#2B2B2B] mb-3 font-serif" style={{ fontSize: '48px', fontWeight: 400, letterSpacing: '0.02em' }}>
          Gita
        </h1>
        <p className="text-[#6E6E6E] mb-16" style={{ fontSize: '15px', letterSpacing: '0.01em' }}>
          A quiet place to think
        </p>

        <div className="w-full mb-8">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="What's troubling you right now?"
            className="w-full h-48 px-6 py-5 bg-white border border-[#E6E1D8] rounded-xl resize-none text-[#2B2B2B] placeholder:text-[#6E6E6E]/40 focus:outline-none focus:border-[#D6A84F] transition-colors"
            style={{ fontSize: '16px', lineHeight: '1.7' }}
          />
        </div>

        <button
          onClick={handleSubmit}
          disabled={!input.trim()}
          className="px-8 py-3 bg-[#2B2B2B] text-[#FAF7F2] rounded-full disabled:opacity-30 disabled:cursor-not-allowed transition-opacity"
          style={{ fontSize: '15px', letterSpacing: '0.01em' }}
        >
          Reflect
        </button>
      </div>

      <div className="pb-12 flex flex-col items-center gap-6 w-full">
        <p className="text-[#6E6E6E]/60 text-center" style={{ fontSize: '13px' }}>
          You don't need the right words.
        </p>
        <div className="flex gap-6">
          <button
            onClick={onViewHistory}
            className="text-[#6E6E6E] underline underline-offset-2"
            style={{ fontSize: '13px' }}
          >
            Past reflections
          </button>
          {onViewSettings && (
            <button
              onClick={onViewSettings}
              className="text-[#6E6E6E] underline underline-offset-2"
              style={{ fontSize: '13px' }}
            >
              Settings
            </button>
          )}
        </div>
      </div>
    </div>
  );
}
