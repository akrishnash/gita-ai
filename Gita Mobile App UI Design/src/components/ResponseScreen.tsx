import { useState, useEffect } from 'react';
import { ReflectionResult } from '../types/reflection';
import { getAIProvider } from '../utils/aiConfig';
import { resetAIProvider } from '../utils/aiConfig';

interface ResponseScreenProps {
  reflection: ReflectionResult;
  problem: string;
  onViewSilent: () => void;
  onReturnHome: () => void;
  onAlternatePerspective?: (alternate: ReflectionResult) => void;
}

export function ResponseScreen({ 
  reflection, 
  problem,
  onViewSilent, 
  onReturnHome,
  onAlternatePerspective 
}: ResponseScreenProps) {
  const [displayReflection, setDisplayReflection] = useState(reflection.reflection.text);
  const [isRefining, setIsRefining] = useState(false);
  const [alternateButtonLabel, setAlternateButtonLabel] = useState('Another way to see this');

  // Rotate button labels
  const buttonLabels = [
    'Another way to see this',
    'A different lens',
    'Sit with another thought',
    'See it differently',
  ];

  useEffect(() => {
    // Set random button label
    setAlternateButtonLabel(buttonLabels[Math.floor(Math.random() * buttonLabels.length)]);
    
    // Attempt AI refinement if enabled
    const attemptAIRefinement = async () => {
      const provider = getAIProvider();
      if (provider && reflection.reflection.text) {
        setIsRefining(true);
        try {
          const refined = await provider.refineReflection(
            problem,
            {
              sanskrit: reflection.verse.sanskrit,
              english: reflection.verse.english,
              context: reflection.verse.context,
            },
            reflection.reflection
          );
          if (refined) {
            setDisplayReflection(refined);
          }
        } catch (error) {
          // Silently fail - use base reflection
          console.error('AI refinement failed:', error);
        } finally {
          setIsRefining(false);
        }
      }
    };

    attemptAIRefinement();
  }, [reflection, problem]);

  const handleAlternatePerspective = () => {
    if (reflection.alternatePerspective && onAlternatePerspective) {
      const alternate: ReflectionResult = {
        ...reflection,
        reflection: reflection.alternatePerspective.reflection,
        anchor: reflection.alternatePerspective.anchor,
        alternatePerspective: undefined, // Prevent infinite nesting
      };
      onAlternatePerspective(alternate);
    }
  };

  const heading = reflection.heading || 'Reflection';

  return (
    <div className="min-h-screen py-12 px-8 animate-fadeIn">
      <div className="flex flex-col gap-12 max-w-md mx-auto">
        {/* Verse Card */}
        <div className="bg-white rounded-2xl p-8 border border-[#E6E1D8]">
          <p 
            className="text-[#2B2B2B] mb-6 font-serif text-center"
            style={{ fontSize: '20px', lineHeight: '1.8' }}
          >
            {reflection.verse.sanskrit}
          </p>
          <p 
            className="text-[#6E6E6E] mb-4 text-center italic"
            style={{ fontSize: '13px', lineHeight: '1.7' }}
          >
            {reflection.verse.transliteration}
          </p>
          <p 
            className="text-[#2B2B2B] text-center"
            style={{ fontSize: '15px', lineHeight: '1.7' }}
          >
            {reflection.verse.english}
          </p>
        </div>

        {/* Context Section */}
        <div>
          <h2 
            className="text-[#6E6E6E] mb-4 uppercase tracking-wider"
            style={{ fontSize: '11px', letterSpacing: '0.1em' }}
          >
            Context
          </h2>
          <p 
            className="text-[#2B2B2B]"
            style={{ fontSize: '15px', lineHeight: '1.8' }}
          >
            {reflection.verse.context}
          </p>
        </div>

        {/* Reflection Section */}
        <div>
          <h2 
            className="text-[#6E6E6E] mb-4 uppercase tracking-wider"
            style={{ fontSize: '11px', letterSpacing: '0.1em' }}
          >
            {heading}
          </h2>
          {isRefining ? (
            <p 
              className="text-[#6E6E6E] italic"
              style={{ fontSize: '15px', lineHeight: '1.8' }}
            >
              {reflection.reflection.text}
            </p>
          ) : (
            <p 
              className="text-[#2B2B2B]"
              style={{ fontSize: '15px', lineHeight: '1.8' }}
            >
              {displayReflection}
            </p>
          )}
        </div>

        {/* Anchor Line */}
        <div className="text-center py-6">
          <p 
            className="text-[#D6A84F]"
            style={{ fontSize: '16px', letterSpacing: '0.01em' }}
          >
            {reflection.anchor.text}
          </p>
        </div>

        {/* Alternate Perspective Button */}
        {reflection.alternatePerspective && (
          <div className="flex justify-center">
            <button
              onClick={handleAlternatePerspective}
              className="text-[#6E6E6E] underline underline-offset-2 hover:text-[#2B2B2B] transition-colors"
              style={{ fontSize: '13px' }}
            >
              {alternateButtonLabel}
            </button>
          </div>
        )}

        {/* Actions */}
        <div className="flex flex-col items-center gap-6 pt-4">
          <button
            onClick={onViewSilent}
            className="text-[#6E6E6E] underline underline-offset-2"
            style={{ fontSize: '13px' }}
          >
            View in silence
          </button>
          <button
            onClick={onReturnHome}
            className="text-[#2B2B2B]"
            style={{ fontSize: '14px' }}
          >
            ← Return home
          </button>
        </div>

        {/* Optional Footer Microcopy */}
        {reflection.alternatePerspective && (
          <p 
            className="text-center text-[#6E6E6E]/40"
            style={{ fontSize: '11px', fontStyle: 'italic' }}
          >
            The same truth often returns in different words.
          </p>
        )}
      </div>
    </div>
  );
}
