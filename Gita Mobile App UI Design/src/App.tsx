import { useState, useEffect } from 'react';
import { HomeScreen } from './components/HomeScreen';
import { PauseScreen } from './components/PauseScreen';
import { ResponseScreen } from './components/ResponseScreen';
import { SilentModeScreen } from './components/SilentModeScreen';
import { HistoryScreen } from './components/HistoryScreen';
import { SettingsScreen } from './components/SettingsScreen';
import { ReflectionResult, ReflectionRecord } from './types/reflection';
import { detectTheme } from './utils/themeDetector';
import { selectVerse, selectReflection, selectAnchor, selectAlternatePerspective, getRandomHeading } from './utils/selectionEngine';
import { saveReflectionToHistory, getReflectionHistory, markVerseAsSeen } from './utils/storage';
import { resetAIProvider } from './utils/aiConfig';

type Screen = 'home' | 'pause' | 'response' | 'silent' | 'history' | 'settings';

export default function App() {
  const [currentScreen, setCurrentScreen] = useState<Screen>('home');
  const [problem, setProblem] = useState('');
  const [currentReflection, setCurrentReflection] = useState<ReflectionResult | null>(null);
  const [history, setHistory] = useState<ReflectionRecord[]>([]);

  // Load history on mount
  useEffect(() => {
    const storedHistory = getReflectionHistory();
    setHistory(storedHistory.reflections);
  }, []);

  const handleSubmitProblem = async (userProblem: string) => {
    setProblem(userProblem);
    setCurrentScreen('pause');

    // Detect theme
    const detected = detectTheme(userProblem);
    
    if (!detected) {
      // Fallback: show error or default response
      // For now, use placeholder
      setTimeout(() => {
        setCurrentScreen('home');
      }, 2000);
      return;
    }

    // Select verse
    const verse = selectVerse(detected.themeId, detected.subthemeId);
    
    if (!verse) {
      // TODO: When gitaMap.ts is available, this will work
      // For now, return to home with a brief delay
      setTimeout(() => {
        setCurrentScreen('home');
      }, 2000);
      return;
    }

    // Simulate thinking time (1.5-2s)
    const delay = 1500 + Math.random() * 500;
    
    setTimeout(async () => {
      // Select reflection and anchor
      const reflection = selectReflection(verse);
      const anchor = selectAnchor(verse, reflection);
      const heading = getRandomHeading();
      
      // Get alternate perspective
      const alternate = selectAlternatePerspective(verse, reflection);

      // Mark verse as seen
      markVerseAsSeen(verse.id, detected.themeId, detected.subthemeId);

      // Create reflection result
      const result: ReflectionResult = {
        verse,
        reflection,
        anchor,
        alternatePerspective: alternate || undefined,
        heading,
      };

      // Save to history
      const record: ReflectionRecord = {
        id: Date.now().toString(),
        date: new Date().toISOString().split('T')[0],
        problem: userProblem,
        themeId: detected.themeId,
        subthemeId: detected.subthemeId,
        verseId: verse.id,
        reflectionAngle: reflection.angle,
        result,
      };

      saveReflectionToHistory(record);
      setHistory(prev => [record, ...prev]);

      setCurrentReflection(result);
      setCurrentScreen('response');
    }, delay);
  };

  const handleViewSilentMode = () => {
    setCurrentScreen('silent');
  };

  const handleReturnFromSilent = () => {
    setCurrentScreen('response');
  };

  const handleViewHistory = () => {
    setCurrentScreen('history');
  };

  const handleViewSettings = () => {
    setCurrentScreen('settings');
  };

  const handleSelectHistoryItem = (record: ReflectionRecord) => {
    setCurrentReflection(record.result);
    setCurrentScreen('response');
  };

  const handleAlternatePerspective = (alternate: ReflectionResult) => {
    setCurrentReflection(alternate);
    // Stay on response screen, just update the reflection
  };

  const handleReturnHome = () => {
    setProblem('');
    setCurrentReflection(null);
    setCurrentScreen('home');
    // Reset AI provider cache when returning home (in case settings changed)
    resetAIProvider();
  };

  return (
    <div className="min-h-screen bg-[#FAF7F2] flex items-center justify-center p-4">
      <div className="w-full max-w-md min-h-screen relative">
        {currentScreen === 'home' && (
          <HomeScreen 
            onSubmit={handleSubmitProblem}
            onViewHistory={handleViewHistory}
            onViewSettings={handleViewSettings}
          />
        )}
        {currentScreen === 'pause' && <PauseScreen />}
        {currentScreen === 'response' && currentReflection && (
          <ResponseScreen
            reflection={currentReflection}
            problem={problem}
            onViewSilent={handleViewSilentMode}
            onReturnHome={handleReturnHome}
            onAlternatePerspective={handleAlternatePerspective}
          />
        )}
        {currentScreen === 'silent' && currentReflection && (
          <SilentModeScreen
            verse={currentReflection.verse.sanskrit}
            onReturn={handleReturnFromSilent}
          />
        )}
        {currentScreen === 'history' && (
          <HistoryScreen
            reflections={history}
            onSelectItem={handleSelectHistoryItem}
            onReturnHome={handleReturnHome}
          />
        )}
        {currentScreen === 'settings' && (
          <SettingsScreen
            onReturnHome={handleReturnHome}
          />
        )}
      </div>
    </div>
  );
}
