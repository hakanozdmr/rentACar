# Performance & Monitoring

## 🌟 Eklenen Özellikler

### ✅ 1. Web Vitals Monitoring
**Dosya**: `frontend/src/index.tsx`, `frontend/src/reportWebVitals.ts`

**Özellikler**:
- CLS (Cumulative Layout Shift)
- FID (First Input Delay)
- FCP (First Contentful Paint)
- LCP (Largest Contentful Paint)
- TTFB (Time to First Byte)

**Geliştirme**: Console'da tüm metrikler loglanır
**Production**: TODO - Analytics servisine gönderilecek

### ✅ 2. Performance Hooks
**Dosya**: `frontend/src/hooks/usePerformance.tsx`

#### usePerformance
Component render sürelerini izler:
```typescript
const { renderCount } = usePerformance('ComponentName');
// Development'ta 100ms üzeri render'lar warning verir
```

#### useDebounce
Aşırı function call'ları önler:
```typescript
const debouncedSearch = useDebounce((value: string) => {
  // Search logic
}, 300);
```

#### useThrottle
Function execution rate'ini sınırlar:
```typescript
const throttledScroll = useThrottle(() => {
  // Scroll handler
}, 100);
```

#### useAsyncPerformance
Async işlemlerin süresini ölçer:
```typescript
const { measureAsync } = useAsyncPerformance();
const result = await measureAsync('OperationName', async () => {
  // Async operation
});
// Development'ta süre loglanır
```

### ✅ 3. Loading Skeletons
**Dosya**: `frontend/src/components/LoadingSkeleton.tsx`

**Variant'lar**:
- `table`: Tablo görünümü için skeleton
- `card`: Kart görünümü için skeleton
- `dashboard`: Dashboard widget'ları için
- `text`: Basit metin skeleton

**Kullanım**:
```typescript
<LoadingSkeleton variant="table" rows={8} cols={8} />
```

**Uygulanan Sayfalar**:
- ✅ RentalsPage
- ⏳ Dashboard (TODO)
- ⏳ CarsPage (TODO)

### ✅ 4. Error Boundary
**Dosya**: `frontend/src/components/ErrorBoundary.tsx`

**Özellikler**:
- Uygulama çapında hata yakalama
- User-friendly error mesajı
- Development'ta detaylı error stack
- "Tekrar Dene" butonu
- "Ana Sayfaya Dön" butonu
- Console'a error logging

**Kullanım**: `App.tsx`'de en üst seviyede wrap edildi

### ✅ 5. Query Client Optimizations
**Dosya**: `frontend/src/App.tsx`

**Ayar'lar**:
```typescript
{
  retry: 1,
  refetchOnWindowFocus: false,
  staleTime: 5 * 60 * 1000, // 5 dakika
  cacheTime: 10 * 60 * 1000, // 10 dakika
}
```

**Faydalar**:
- ✅ Gereksiz re-fetch'leri önler
- ✅ Cache'den hızlı yükleme
- ✅ Network trafiğini azaltır
- ✅ Kullanıcı deneyimini iyileştirir

---

## 📊 Monitoring Metrikleri

### Web Vitals Kriterleri

| Metrik | İyi | Kötü | Amaç |
|--------|-----|------|------|
| LCP | < 2.5s | > 4s | İlk içerik render |
| FID | < 100ms | > 300ms | İlk etkileşim |
| CLS | < 0.1 | > 0.25 | Görsel stabilite |
| FCP | < 1.8s | > 3s | İlk resim |
| TTFB | < 600ms | > 1.3s | Server yanıt |

### Console Log Formatı
```
[Performance] { name: 'LCP', value: '1.23s', rating: 'good' }
```

---

## 🔧 Kullanım Örnekleri

### Performance Hook Kullanımı
```typescript
import { usePerformance } from '../hooks/usePerformance';

const MyComponent = () => {
  usePerformance('MyComponent');
  
  return <div>Content</div>;
};
```

### Debounce ile Arama
```typescript
import { useState } from 'react';
import { useDebounce } from '../hooks/usePerformance';

const SearchComponent = () => {
  const [searchTerm, setSearchTerm] = useState('');
  
  const debouncedSearch = useDebounce((value: string) => {
    // API call
    searchAPI(value);
  }, 300);
  
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setSearchTerm(e.target.value);
    debouncedSearch(e.target.value);
  };
  
  return <input value={searchTerm} onChange={handleChange} />;
};
```

### Async Performance İzleme
```typescript
import { useAsyncPerformance } from '../hooks/usePerformance';

const DataComponent = () => {
  const { measureAsync } = useAsyncPerformance();
  
  const fetchData = async () => {
    const data = await measureAsync('FetchData', async () => {
      return await api.get('/data');
    });
    
    return data;
  };
  
  useEffect(() => {
    fetchData();
  }, []);
};
```

### Loading Skeleton Kullanımı
```typescript
import LoadingSkeleton from '../components/LoadingSkeleton';

const MyList = () => {
  const { data, isLoading } = useQuery('items', fetchItems);
  
  if (isLoading) {
    return <LoadingSkeleton variant="table" rows={10} />;
  }
  
  return <div>List content</div>;
};
```

---

## 🚀 Performans İyileştirmeleri

### 1. Code Splitting (TODO)
```typescript
const LazyComponent = React.lazy(() => import('./LazyComponent'));
```

### 2. Image Lazy Loading (TODO)
```typescript
<img loading="lazy" src="..." alt="..." />
```

### 3. Memoization (TODO)
```typescript
const MemoizedComponent = React.memo(Component);
const expensiveValue = useMemo(() => compute(), [deps]);
const cachedCallback = useCallback(() => callback(), [deps]);
```

### 4. Bundle Size Optimization (TODO)
- Webpack bundle analyzer
- Tree shaking
- Dead code elimination

---

## 📈 Monitoring Dashboard (TODO)

### Hedef Özellikler
- [ ] Real-time performance metrics
- [ ] Error tracking dashboard
- [ ] User session replay
- [ ] API response time tracking
- [ ] Slow query detection
- [ ] Memory leak detection

### Entegrasyon Seçenekleri
- **Google Analytics** - Basit ve ücretsiz
- **New Relic** - Enterprise grade
- **Sentry** - Error tracking + Performance
- **DataDog** - Full-stack monitoring
- **LogRocket** - Session replay + analytics

---

## 🐛 Error Tracking (TODO)

### Amaç
- Kullanıcı hatalarını yakalamak
- Hata kategorilerini analiz etmek
- Hızlı düzeltme yapmak

### Entegrasyon
```typescript
// ErrorBoundary içinde
componentDidCatch(error: Error, errorInfo: ErrorInfo) {
  if (process.env.NODE_ENV === 'production') {
    logErrorToService(error, errorInfo);
  }
}
```

---

## 📝 Best Practices

### 1. Component Optimizasyonu
- ✅ Küçük component'ler yaz
- ✅ Gereksiz re-render'ları önle
- ✅ useMemo ve useCallback kullan
- ✅ Props'ları kontrol et

### 2. Query Optimizasyonu
- ✅ StaleTime kullan
- ✅ CacheTime ayarla
- ✅ select kullan (yalnızca gerekli data)
- ✅ Gereksiz refetch'leri engelle

### 3. Bundle Size
- ✅ Lazy loading
- ✅ Dynamic imports
- ✅ Code splitting
- ✅ Unused dependencies kaldır

### 4. Network
- ✅ Request batching
- ✅ Compression kullan
- ✅ CDN kullan
- ✅ Image optimization

---

## 🔍 Debug Tools

### Development
- ✅ React DevTools
- ✅ Redux DevTools (eğer Redux kullanılıyorsa)
- ✅ Chrome DevTools Profiler
- ✅ Performance tab
- ⏳ React Query DevTools (TODO)

### Production
- ✅ Web Vitals
- ✅ Error logging (TODO)
- ✅ Analytics (TODO)
- ⏳ APM tool (TODO)

---

## 📚 Referanslar

- [Web Vitals](https://web.dev/vitals/)
- [React Performance](https://react.dev/learn/render-and-commit)
- [React Query Performance](https://tanstack.com/query/latest/docs/react/guides/window-focus-refetching)
- [Material-UI Performance](https://mui.com/material-ui/integrations/interoperability/)

---

## ✅ Checklist

- [x] Web Vitals monitoring
- [x] Performance hooks (usePerformance, useDebounce, useThrottle)
- [x] Loading skeletons
- [x] Error Boundary
- [x] Query Client optimizations
- [ ] React Query DevTools
- [ ] Sentry integration
- [ ] Performance dashboard
- [ ] Bundle analyzer
- [ ] Code splitting

---

Son Güncelleme: 2025-01-XX


