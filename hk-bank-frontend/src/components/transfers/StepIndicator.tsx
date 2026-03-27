import { Check } from 'lucide-react';

export interface TransferStep {
  number: number;
  label: string;
}

interface StepIndicatorProps {
  steps: TransferStep[];
  currentStep: number;
}

export function StepIndicator({ steps, currentStep }: StepIndicatorProps) {
  return (
    <div className="space-y-6 mb-8">
      <h2 className="text-2xl font-bold text-white">Pul köçürməsi</h2>

      <div className="flex items-center gap-2">
        {steps.map((step, index) => {
          const isCompleted = currentStep > step.number;
          const isCurrent = currentStep === step.number;

          return (
            <div key={step.number} className="contents">
              <div className="flex flex-col items-center">
                <div
                  className={`w-10 h-10 rounded-full flex items-center justify-center font-semibold transition-all ${
                    isCompleted || isCurrent
                      ? 'bg-gradient-to-r from-blue-500 to-cyan-400 text-white'
                      : 'bg-slate-800 text-slate-500'
                  }`}
                >
                  {isCompleted ? <Check size={20} /> : <span>{step.number}</span>}
                </div>
                <p
                  className={`text-xs mt-2 font-medium ${
                    isCurrent ? 'text-white' : 'text-slate-500'
                  }`}
                >
                  {step.label}
                </p>
              </div>

              {index < steps.length - 1 && (
                <div className="flex-1 h-1 mx-1 bg-gradient-to-r from-cyan-500/40 to-transparent rounded-full" />
              )}
            </div>
          );
        })}
      </div>
    </div>
  );
}
