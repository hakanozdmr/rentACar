import React, { useState, useCallback } from 'react';
import ConfirmDialog from '../components/ConfirmDialog';

interface ConfirmOptions {
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  severity?: 'error' | 'warning' | 'info';
  icon?: React.ReactNode;
}

interface ConfirmDialogState extends ConfirmOptions {
  open: boolean;
  onConfirm?: () => void;
  isLoading?: boolean;
}

export const useConfirmDialog = () => {
  const [dialogState, setDialogState] = useState<ConfirmDialogState>({
    open: false,
    title: '',
    message: '',
    confirmText: 'Sil',
    cancelText: 'İptal',
    severity: 'error',
    isLoading: false,
  });

  const showConfirm = useCallback((
    options: ConfirmOptions,
    onConfirm: () => void | Promise<void>
  ) => {
    setDialogState({
      ...options,
      open: true,
      onConfirm: async () => {
        try {
          setDialogState(prev => ({ ...prev, isLoading: true }));
          await onConfirm();
          setDialogState(prev => ({ ...prev, open: false, isLoading: false }));
        } catch (error) {
          setDialogState(prev => ({ ...prev, isLoading: false }));
          throw error;
        }
      },
    });
  }, []);

  const handleClose = useCallback(() => {
    setDialogState(prev => ({ ...prev, open: false, isLoading: false }));
  }, []);

  const ConfirmDialogComponent = useCallback(() => (
    <ConfirmDialog
      open={dialogState.open}
      onClose={handleClose}
      onConfirm={dialogState.onConfirm || (() => {})}
      title={dialogState.title}
      message={dialogState.message}
      confirmText={dialogState.confirmText}
      cancelText={dialogState.cancelText}
      isLoading={dialogState.isLoading}
      severity={dialogState.severity}
      icon={dialogState.icon}
    />
  ), [dialogState, handleClose]);

  return {
    showConfirm,
    ConfirmDialogComponent,
    isOpen: dialogState.open,
  };
};



