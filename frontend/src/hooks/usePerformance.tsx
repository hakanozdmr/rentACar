import { useEffect, useRef } from 'react';

/**
 * Performance monitoring hook
 * Tracks component render times and performance metrics
 */
export const usePerformance = (componentName: string) => {
  const renderStartTime = useRef<number>(Date.now());
  const renderCount = useRef<number>(0);

  useEffect(() => {
    const renderTime = Date.now() - renderStartTime.current;
    renderCount.current += 1;

    // Log slow renders in development
    if (process.env.NODE_ENV === 'development' && renderTime > 100) {
      console.warn(`[Performance Warning] ${componentName} render took ${renderTime}ms`, {
        renderCount: renderCount.current,
        threshold: '100ms',
      });
    }

    return () => {
      // Cleanup if needed
    };
  });

  useEffect(() => {
    renderStartTime.current = Date.now();
  });

  // Return performance stats if needed
  return {
    renderCount: renderCount.current,
  };
};

/**
 * Debounce hook for performance optimization
 * Prevents excessive function calls
 */
export function useDebounce<T extends (...args: any[]) => any>(
  callback: T,
  delay: number
): T {
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  return ((...args: any[]) => {
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    timeoutRef.current = setTimeout(() => {
      callback(...args);
    }, delay);
  }) as T;
}

/**
 * Throttle hook for performance optimization
 * Limits function execution rate
 */
export function useThrottle<T extends (...args: any[]) => any>(
  callback: T,
  delay: number
): T {
  const lastRunRef = useRef<number>(0);

  return ((...args: any[]) => {
    const now = Date.now();

    if (now - lastRunRef.current >= delay) {
      lastRunRef.current = now;
      callback(...args);
    }
  }) as T;
}

/**
 * Performance monitor for async operations
 */
export function useAsyncPerformance() {
  const measureAsync = async function<T>(
    name: string,
    asyncFn: () => Promise<T>
  ): Promise<T> {
    const startTime = performance.now();

    try {
      const result = await asyncFn();
      const duration = performance.now() - startTime;

      if (process.env.NODE_ENV === 'development') {
        console.log(`[Async Performance] ${name} completed in ${duration.toFixed(2)}ms`);
      }

      return result;
    } catch (error) {
      const duration = performance.now() - startTime;
      console.error(`[Async Performance] ${name} failed after ${duration.toFixed(2)}ms`, error);
      throw error;
    }
  };

  return { measureAsync };
}
