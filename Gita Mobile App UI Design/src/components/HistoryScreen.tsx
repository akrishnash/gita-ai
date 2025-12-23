import { ReflectionRecord } from '../types/reflection';

interface HistoryScreenProps {
  reflections: ReflectionRecord[];
  onSelectItem: (reflection: ReflectionRecord) => void;
  onReturnHome: () => void;
}

export function HistoryScreen({ reflections, onSelectItem, onReturnHome }: HistoryScreenProps) {
  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
      return 'Today';
    } else if (date.toDateString() === yesterday.toDateString()) {
      return 'Yesterday';
    } else {
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
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
            History
          </h1>
          <p 
            className="text-[#6E6E6E]"
            style={{ fontSize: '14px' }}
          >
            Past reflections
          </p>
        </div>

        <div className="flex flex-col gap-4 mb-12">
          {reflections.map((reflection) => (
            <button
              key={reflection.id}
              onClick={() => onSelectItem(reflection)}
              className="bg-white rounded-xl p-6 border border-[#E6E1D8] text-left hover:border-[#D6A84F]/30 transition-colors"
            >
              <p 
                className="text-[#6E6E6E] mb-3"
                style={{ fontSize: '12px' }}
              >
                {formatDate(reflection.date)}
              </p>
              <p 
                className="text-[#D6A84F] mb-2"
                style={{ fontSize: '15px', letterSpacing: '0.01em' }}
              >
                {reflection.result.anchor.text}
              </p>
              <p 
                className="text-[#6E6E6E] line-clamp-2"
                style={{ fontSize: '14px', lineHeight: '1.6' }}
              >
                {reflection.problem}
              </p>
            </button>
          ))}
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
