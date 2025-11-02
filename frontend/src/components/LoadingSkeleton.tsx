import React from 'react';
import {
  Box,
  Skeleton,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Grid,
  Card,
  CardContent,
  CardHeader,
} from '@mui/material';

interface LoadingSkeletonProps {
  variant?: 'table' | 'card' | 'text' | 'dashboard';
  rows?: number;
  cols?: number;
}

export const LoadingSkeleton: React.FC<LoadingSkeletonProps> = ({
  variant = 'text',
  rows = 5,
  cols = 6,
}) => {
  const renderSkeleton = () => {
    switch (variant) {
      case 'table':
        return (
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  {Array.from({ length: cols }).map((_, index) => (
                    <TableCell key={index}>
                      <Skeleton variant="text" width={100} />
                    </TableCell>
                  ))}
                </TableRow>
              </TableHead>
              <TableBody>
                {Array.from({ length: rows }).map((_, rowIndex) => (
                  <TableRow key={rowIndex}>
                    {Array.from({ length: cols }).map((_, colIndex) => (
                      <TableCell key={colIndex}>
                        <Skeleton variant="text" />
                      </TableCell>
                    ))}
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        );

      case 'card':
        return (
          <Grid container spacing={3}>
            {Array.from({ length: rows }).map((_, index) => (
              <Grid item xs={12} sm={6} md={4} lg={3} key={index}>
                <Card>
                  <Skeleton variant="rectangular" height={200} />
                  <CardContent>
                    <Skeleton variant="text" width="60%" height={32} />
                    <Skeleton variant="text" width="80%" height={24} />
                    <Skeleton variant="text" width="40%" height={24} />
                    <Box display="flex" gap={1} mt={1}>
                      <Skeleton variant="circular" width={40} height={40} />
                      <Skeleton variant="circular" width={40} height={40} />
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        );

      case 'dashboard':
        return (
          <Grid container spacing={3}>
            {Array.from({ length: 4 }).map((_, index) => (
              <Grid item xs={12} sm={6} md={3} key={index}>
                <Card>
                  <CardHeader
                    avatar={<Skeleton variant="circular" width={40} height={40} />}
                    title={<Skeleton variant="text" width="60%" />}
                    subheader={<Skeleton variant="text" width="80%" />}
                  />
                  <CardContent>
                    <Skeleton variant="text" width="100%" height={60} />
                  </CardContent>
                </Card>
              </Grid>
            ))}
          </Grid>
        );

      case 'text':
      default:
        return (
          <Box>
            {Array.from({ length: rows }).map((_, index) => (
              <Skeleton
                key={index}
                variant="text"
                width={index === 0 ? '100%' : index === 1 ? '80%' : '60%'}
                height={24}
                sx={{ mb: 1 }}
              />
            ))}
          </Box>
        );
    }
  };

  return <Box>{renderSkeleton()}</Box>;
};

export default LoadingSkeleton;


