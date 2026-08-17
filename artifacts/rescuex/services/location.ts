import * as Location from 'expo-location';
import { Platform } from 'react-native';

export type LocationSnapshot = {
  latitude: number;
  longitude: number;
  accuracy: number | null;
  updatedAt: string;
};

export async function getCurrentLocation(): Promise<LocationSnapshot> {
  if (Platform.OS === 'web' && typeof navigator !== 'undefined' && navigator.geolocation) {
    return new Promise((resolve, reject) => {
      navigator.geolocation.getCurrentPosition(
        (position) =>
          resolve({
            latitude: position.coords.latitude,
            longitude: position.coords.longitude,
            accuracy: position.coords.accuracy,
            updatedAt: new Date().toISOString(),
          }),
        reject,
        { enableHighAccuracy: true, timeout: 7000 },
      );
    });
  }

  const permission = await Location.requestForegroundPermissionsAsync();
  if (permission.status !== Location.PermissionStatus.GRANTED) {
    throw new Error('Location permission is required to share your emergency location.');
  }
  const current = await Location.getCurrentPositionAsync({ accuracy: Location.Accuracy.Balanced });
  return {
    latitude: current.coords.latitude,
    longitude: current.coords.longitude,
    accuracy: current.coords.accuracy,
    updatedAt: new Date().toISOString(),
  };
}