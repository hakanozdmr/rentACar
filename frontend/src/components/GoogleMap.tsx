import React, { useEffect, useRef, useState } from 'react';
import { Wrapper, Status } from '@googlemaps/react-wrapper';
import { Box, Typography, CircularProgress, Alert } from '@mui/material';
import { LocationOn as LocationIcon } from '@mui/icons-material';

interface GoogleMapProps {
  center: {
    lat: number;
    lng: number;
  };
  zoom?: number;
  carInfo?: {
    plate: string;
    brandName?: string;
    modelName?: string;
    lastUpdate?: string;
    address?: string;
  };
  height?: string;
}

interface MapComponentProps {
  center: { lat: number; lng: number };
  zoom: number;
  carInfo?: GoogleMapProps['carInfo'];
}

const MapComponent: React.FC<MapComponentProps> = ({ center, zoom, carInfo }) => {
  const ref = useRef<HTMLDivElement>(null);
  const [map, setMap] = useState<google.maps.Map | null>(null);
  const [marker, setMarker] = useState<google.maps.Marker | null>(null);
  const [address, setAddress] = useState<string>('');

  useEffect(() => {
    if (ref.current && !map) {
      const newMap = new google.maps.Map(ref.current, {
        center,
        zoom,
        mapTypeId: google.maps.MapTypeId.ROADMAP,
        zoomControl: true,
        streetViewControl: false,
        fullscreenControl: true,
        mapTypeControl: true,
        styles: [
          {
            featureType: 'poi',
            elementType: 'labels',
            stylers: [{ visibility: 'off' }],
          },
        ],
      });

      setMap(newMap);

      // Create marker
      const newMarker = new google.maps.Marker({
        position: center,
        map: newMap,
        title: carInfo?.plate || 'Araç Konumu',
        icon: {
          url: 'data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(`
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="#1976d2">
              <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z"/>
            </svg>
          `),
          scaledSize: new google.maps.Size(32, 32),
          anchor: new google.maps.Point(16, 32),
        },
      });

      setMarker(newMarker);

      // Create info window
      const infoWindow = new google.maps.InfoWindow({
        content: `
          <div style="padding: 8px; min-width: 200px;">
            <h3 style="margin: 0 0 8px 0; color: #1976d2; font-size: 16px;">
              ${carInfo?.plate || 'Araç'}
            </h3>
            ${carInfo?.brandName && carInfo?.modelName ? `
              <p style="margin: 4px 0; color: #666; font-size: 14px;">
                ${carInfo.brandName} ${carInfo.modelName}
              </p>
            ` : ''}
            ${carInfo?.address ? `
              <p style="margin: 4px 0; color: #666; font-size: 12px;">
                📍 ${carInfo.address}
              </p>
            ` : ''}
            ${carInfo?.lastUpdate ? `
              <p style="margin: 4px 0; color: #999; font-size: 12px;">
                Son güncelleme: ${new Date(carInfo.lastUpdate).toLocaleString('tr-TR')}
              </p>
            ` : ''}
          </div>
        `,
      });

      // Add click listener to marker
      newMarker.addListener('click', () => {
        infoWindow.open(newMap, newMarker);
      });

      // Open info window by default
      infoWindow.open(newMap, newMarker);

      // Get address using reverse geocoding
      const geocoder = new google.maps.Geocoder();
      geocoder.geocode({ location: center }, (results, status) => {
        if (status === 'OK' && results && results[0]) {
          setAddress(results[0].formatted_address);
          // Update info window with address
          const updatedContent = `
            <div style="padding: 8px; min-width: 200px;">
              <h3 style="margin: 0 0 8px 0; color: #1976d2; font-size: 16px;">
                ${carInfo?.plate || 'Araç'}
              </h3>
              ${carInfo?.brandName && carInfo?.modelName ? `
                <p style="margin: 4px 0; color: #666; font-size: 14px;">
                  ${carInfo.brandName} ${carInfo.modelName}
                </p>
              ` : ''}
              <p style="margin: 4px 0; color: #666; font-size: 12px;">
                📍 ${results[0].formatted_address}
              </p>
              ${carInfo?.lastUpdate ? `
                <p style="margin: 4px 0; color: #999; font-size: 12px;">
                  Son güncelleme: ${new Date(carInfo.lastUpdate).toLocaleString('tr-TR')}
                </p>
              ` : ''}
            </div>
          `;
          infoWindow.setContent(updatedContent);
        }
      });
    }
  }, [ref, map, center, zoom, carInfo]);

  // Update marker position when center changes
  useEffect(() => {
    if (marker && map) {
      marker.setPosition(center);
      map.setCenter(center);
    }
  }, [center, marker, map]);

  return <Box ref={ref} sx={{ width: '100%', height: '100%' }} />;
};

const render = (status: Status) => {
  switch (status) {
    case Status.LOADING:
      return (
        <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center" height="400px">
          <CircularProgress />
          <Typography variant="body2" sx={{ mt: 2 }}>
            Harita yükleniyor...
          </Typography>
        </Box>
      );
    case Status.FAILURE:
      return (
        <Alert severity="error" sx={{ height: '400px', display: 'flex', alignItems: 'center' }}>
          <Box>
            <Typography variant="h6" gutterBottom>
              Harita yüklenemedi
            </Typography>
            <Typography variant="body2">
              Google Maps API anahtarı eksik veya geçersiz. Lütfen yöneticiye başvurun.
            </Typography>
          </Box>
        </Alert>
      );
    default:
      return (
        <Box display="flex" flexDirection="column" alignItems="center" justifyContent="center" height="400px">
          <Typography variant="body2" color="text.secondary">
            Harita yükleniyor...
          </Typography>
        </Box>
      );
  }
};

const GoogleMap: React.FC<GoogleMapProps> = ({ 
  center, 
  zoom = 15, 
  carInfo, 
  height = '400px' 
}) => {
  const apiKey = process.env.REACT_APP_GOOGLE_MAPS_API_KEY || '';

  if (!apiKey) {
    return (
      <Alert severity="warning" sx={{ height }}>
        <Box>
          <Typography variant="h6" gutterBottom>
            Google Maps API anahtarı bulunamadı
          </Typography>
          <Typography variant="body2">
            Harita görüntülemek için REACT_APP_GOOGLE_MAPS_API_KEY environment variable'ı gerekli.
          </Typography>
        </Box>
      </Alert>
    );
  }

  return (
    <Box sx={{ width: '100%', height }}>
      <Wrapper apiKey={apiKey} render={render}>
        <MapComponent center={center} zoom={zoom} carInfo={carInfo} />
      </Wrapper>
    </Box>
  );
};

export default GoogleMap;
