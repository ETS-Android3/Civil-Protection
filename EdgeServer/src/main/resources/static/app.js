var map;
document.addEventListener("DOMContentLoaded", function () {});

function getDevices() {
	axios
		.get("http://localhost:8080/api/devices/")
		.then(function (response) {
			populateMarkers(response.data);
		})
}

function populateMarkers(devicesData) {
	console.log(devicesData);

	devicesData.androidDevicesList.forEach(function (androidDevice) {
		const marker = new google.maps.Marker({
			position: {
				lat: androidDevice.lat,
				lng: androidDevice.lng
			},
			map: map,
			icon: {
				url: 'images/android-device-icon.svg',
				scaledSize: new google.maps.Size(40, 46),
				anchor: new google.maps.Point(20, 46),
			}
		});
	});

	devicesData.iotDevicesList.forEach(function (iotDevice) {

		var markerIconUrl = 'images/iot-device-icon.svg'
		if (iotDevice.dangerLevel == 'DANGER_LEVEL_MEDIUM') {
			markerIconUrl = 'images/iot-medium-danger-icon.svg';
		} else if (iotDevice.dangerLevel == 'DANGER_LEVEL_HIGH') {
			markerIconUrl = 'images/iot-high-danger-icon.svg';
		}

		const marker = new google.maps.Marker({
			position: {
				lat: iotDevice.lat,
				lng: iotDevice.lng
			},
			map: map,
			icon: {
				url: markerIconUrl,
				scaledSize: new google.maps.Size(40, 46),
				anchor: new google.maps.Point(20, 46),
			}
		});

		if (
			iotDevice.smoke == null &&
			iotDevice.gas == null &&
			iotDevice.temperature == null &&
			iotDevice.uv == null
		) {
			const rangeMarker = new google.maps.Marker({
				position: {
					lat: iotDevice.lat,
					lng: iotDevice.lng
				},
				title: 'range',
				optimized: false,
				map: map,
				icon: {
					url: 'images/range-deactive.svg',
					scaledSize: new google.maps.Size(40, 40),
					anchor: new google.maps.Point(20, 20),
				}
			});
		} else {
			const rangeMarker = new google.maps.Marker({
				position: {
					lat: iotDevice.lat,
					lng: iotDevice.lng
				},
				title: 'range',
				optimized: false,
				map: map,
				icon: {
					url: 'images/range-active.svg',
					scaledSize: new google.maps.Size(40, 40),
					anchor: new google.maps.Point(20, 20),
				}
			});
		}

	});

}

function initMap() {
	const lat = 37.968483044347;
	const lng = 23.766819125476;
	const MapOptions = {
		mapTypeId: 'roadmap',
		zoom: 18,
		center: { lat, lng }
	}

	const mapDiv = document.getElementById('map');
	map = new google.maps.Map(mapDiv, MapOptions);
    setInterval("getDevices()", 1000);
}