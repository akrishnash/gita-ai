export function PauseScreen() {
  // Rotate pause messages for variety
  const messages = [
    "Let's sit with this for a moment.",
    "Taking a pause.",
    "A moment of stillness.",
    "Sitting with this.",
  ];
  
  const message = messages[Math.floor(Math.random() * messages.length)];

  return (
    <div className="flex flex-col items-center justify-center min-h-screen px-8 animate-fadeIn">
      <p className="text-[#2B2B2B] text-center mb-8" style={{ fontSize: '17px', lineHeight: '1.6' }}>
        {message}
      </p>
      <div className="w-24 h-px bg-[#E6E1D8]" />
    </div>
  );
}
