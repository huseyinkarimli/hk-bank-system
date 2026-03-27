'use client';

import { motion } from 'framer-motion';
import React, { useEffect, useState } from 'react';

interface ToastProps {
  message: string;
  type: 'success' | 'error' | 'info';
  id: string;
}

interface ToastState extends ToastProps {
  visible: boolean;
}

let toastId = 0;

export const useToast = () => {
  const [toasts, setToasts] = useState<ToastState[]>([]);

  const addToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
    const id = String(toastId++);
    const toast: ToastState = { message, type, id, visible: true };

    setToasts((prev) => [...prev, toast]);

    setTimeout(() => {
      setToasts((prev) =>
        prev.map((t) => (t.id === id ? { ...t, visible: false } : t))
      );
      setTimeout(() => {
        setToasts((prev) => prev.filter((t) => t.id !== id));
      }, 300);
    }, 4000);

    return id;
  };

  return { toasts, addToast };
};

export function Toast({ message, type, id, onClose }: ToastProps & { onClose: () => void }) {
  useEffect(() => {
    const timer = setTimeout(onClose, 4000);
    return () => clearTimeout(timer);
  }, [onClose]);

  const colors = {
    success: 'bg-green-500/20 border-green-500/50 text-green-100',
    error: 'bg-red-500/20 border-red-500/50 text-red-100',
    info: 'bg-blue-500/20 border-blue-500/50 text-blue-100',
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: 20 }}
      className={`px-4 py-3 rounded-lg border backdrop-blur-md ${colors[type]}`}
    >
      {message}
    </motion.div>
  );
}

export function ToastContainer({ toasts, onRemove }: { toasts: ToastState[]; onRemove: (id: string) => void }) {
  return (
    <div className="fixed bottom-4 right-4 flex flex-col gap-2 z-50">
      {toasts.map((toast) => (
        <motion.div
          key={toast.id}
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: toast.visible ? 1 : 0, y: toast.visible ? 0 : 20 }}
          exit={{ opacity: 0, y: 20 }}
        >
          <Toast {...toast} onClose={() => onRemove(toast.id)} />
        </motion.div>
      ))}
    </div>
  );
}
