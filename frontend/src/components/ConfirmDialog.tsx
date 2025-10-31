import React from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  IconButton,
} from '@mui/material';
import {
  Warning as WarningIcon,
  Close as CloseIcon,
} from '@mui/icons-material';

interface ConfirmDialogProps {
  open: boolean;
  onClose: () => void;
  onConfirm: () => void;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  isLoading?: boolean;
  severity?: 'error' | 'warning' | 'info';
  icon?: React.ReactNode;
}

const ConfirmDialog: React.FC<ConfirmDialogProps> = ({
  open,
  onClose,
  onConfirm,
  title,
  message,
  confirmText = 'Sil',
  cancelText = 'İptal',
  isLoading = false,
  severity = 'error',
  icon,
}) => {
  const getSeverityColor = () => {
    switch (severity) {
      case 'error':
        return 'error';
      case 'warning':
        return 'warning';
      case 'info':
        return 'primary';
      default:
        return 'error';
    }
  };

  const getDefaultIcon = () => {
    switch (severity) {
      case 'error':
        return <WarningIcon color="error" sx={{ fontSize: 40 }} />;
      case 'warning':
        return <WarningIcon color="warning" sx={{ fontSize: 40 }} />;
      case 'info':
        return <WarningIcon color="primary" sx={{ fontSize: 40 }} />;
      default:
        return <WarningIcon color="error" sx={{ fontSize: 40 }} />;
    }
  };

  return (
    <Dialog
      open={open}
      onClose={onClose}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 2,
          boxShadow: '0 24px 48px rgba(0, 0, 0, 0.15)',
        }
      }}
    >
      <DialogTitle sx={{ pb: 1, position: 'relative' }}>
        <Box display="flex" alignItems="center" gap={2}>
          {icon || getDefaultIcon()}
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 600 }}>
            {title}
          </Typography>
          <IconButton
            aria-label="close"
            onClick={onClose}
            sx={{
              position: 'absolute',
              right: 8,
              top: 8,
              color: (theme) => theme.palette.grey[500],
            }}
            size="small"
          >
            <CloseIcon />
          </IconButton>
        </Box>
      </DialogTitle>

      <DialogContent sx={{ px: 3, py: 2 }}>
        <Typography variant="body1" color="text.secondary" sx={{ lineHeight: 1.6 }}>
          {message}
        </Typography>
      </DialogContent>

      <DialogActions sx={{ px: 3, py: 2, gap: 1 }}>
        <Button
          onClick={onClose}
          variant="outlined"
          disabled={isLoading}
          sx={{ minWidth: 100 }}
        >
          {cancelText}
        </Button>
        <Button
          onClick={onConfirm}
          color={getSeverityColor() as any}
          variant="contained"
          disabled={isLoading}
          sx={{ minWidth: 100 }}
        >
          {confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default ConfirmDialog;



