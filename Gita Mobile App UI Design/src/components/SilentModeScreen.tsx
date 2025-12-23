interface SilentModeScreenProps {
  verse: string;
  onReturn: () => void;
}

export function SilentModeScreen({ verse, onReturn }: SilentModeScreenProps) {
  return (
    <div 
      className="flex flex-col items-center justify-center min-h-screen px-12 cursor-pointer animate-fadeIn"
      onClick={onReturn}
    >
      <p 
        className="text-[#2B2B2B] text-center font-serif"
        style={{ fontSize: '28px', lineHeight: '2', letterSpacing: '0.02em' }}
      >
        {verse}
      </p>
      
      <div className="absolute bottom-12 left-0 right-0 text-center">
        <p 
          className="text-[#6E6E6E]/50"
          style={{ fontSize: '11px', letterSpacing: '0.05em' }}
        >
          Tap to return
        </p>
      </div>
    </div>
  );
}
