package com.moveit.championship.client;

import com.moveit.championship.dto.LocationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationClientTest {

    @Mock
    private RestTemplate restTemplate;

    private LocationClient locationClient;

    private final String locationServiceUrl = "http://localhost:8084";

    @BeforeEach
    void setUp() {
        locationClient = new LocationClient(restTemplate, locationServiceUrl);
    }

    @Test
    @DisplayName("Should successfully fetch location by ID")
    void getLocationById_shouldReturnLocation_whenServiceResponds() {
        // Given
        Integer locationId = 1;
        LocationDTO expectedLocation = new LocationDTO(
                1,
                "Stade de France",
                48.9244,
                2.3601,
                "Avenue Jules Rimet",
                "Entrée E",
                "Entrée B",
                "Grand stade national"
        );

        when(restTemplate.getForObject(
                eq(locationServiceUrl + "/locations/" + locationId),
                eq(LocationDTO.class)
        )).thenReturn(expectedLocation);

        // When
        LocationDTO result = locationClient.getLocationById(locationId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLocationId()).isEqualTo(1);
        assertThat(result.getName()).isEqualTo("Stade de France");
        assertThat(result.getLatitude()).isEqualTo(48.9244);
        assertThat(result.getLongitude()).isEqualTo(2.3601);
        
        verify(restTemplate, times(1)).getForObject(anyString(), eq(LocationDTO.class));
    }

    @Test
    @DisplayName("Should return null when location service is unavailable")
    void getLocationById_shouldReturnNull_whenServiceUnavailable() {
        // Given
        Integer locationId = 999;
        
        when(restTemplate.getForObject(anyString(), eq(LocationDTO.class)))
                .thenThrow(new RestClientException("Connection refused"));

        // When
        LocationDTO result = locationClient.getLocationById(locationId);

        // Then
        assertThat(result).isNull();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(LocationDTO.class));
    }

    @Test
    @DisplayName("Should return null when location not found")
    void getLocationById_shouldReturnNull_whenLocationNotFound() {
        // Given
        Integer locationId = 999;
        
        when(restTemplate.getForObject(anyString(), eq(LocationDTO.class)))
                .thenReturn(null);

        // When
        LocationDTO result = locationClient.getLocationById(locationId);

        // Then
        assertThat(result).isNull();
        verify(restTemplate, times(1)).getForObject(anyString(), eq(LocationDTO.class));
    }

    @Test
    @DisplayName("Should construct correct URL for location request")
    void getLocationById_shouldConstructCorrectUrl() {
        // Given
        Integer locationId = 42;
        String expectedUrl = locationServiceUrl + "/locations/42";

        LocationDTO mockLocation = new LocationDTO();
        when(restTemplate.getForObject(eq(expectedUrl), eq(LocationDTO.class)))
                .thenReturn(mockLocation);

        // When
        locationClient.getLocationById(locationId);

        // Then
        verify(restTemplate).getForObject(eq(expectedUrl), eq(LocationDTO.class));
    }

    @Test
    @DisplayName("Should handle network exceptions gracefully")
    void getLocationById_shouldHandleNetworkException() {
        // Given
        Integer locationId = 1;
        
        when(restTemplate.getForObject(anyString(), eq(LocationDTO.class)))
                .thenThrow(new RuntimeException("Network timeout"));

        // When
        LocationDTO result = locationClient.getLocationById(locationId);

        // Then
        assertThat(result).isNull();
    }
}
